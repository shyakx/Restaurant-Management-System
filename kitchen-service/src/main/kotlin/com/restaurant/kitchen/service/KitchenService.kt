package com.restaurant.kitchen.service

import com.restaurant.kitchen.dto.EventType
import com.restaurant.kitchen.dto.OrderEvent
import com.restaurant.kitchen.dto.OrderItemEvent
import com.restaurant.kitchen.dto.OrderStatus
import com.restaurant.kitchen.entity.KitchenOrder
import com.restaurant.kitchen.entity.KitchenOrderItem
import com.restaurant.kitchen.entity.KitchenOrderStatus
import com.restaurant.kitchen.repository.KitchenOrderRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class KitchenService(
    private val kitchenOrderRepository: KitchenOrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        const val ORDER_TOPIC = "order-events"
    }

    @KafkaListener(topics = ["order-events"], groupId = "kitchen-service-group")
    fun handleOrderEvent(orderEvent: OrderEvent) {
        when (orderEvent.eventType) {
            EventType.ORDER_PLACED -> handleOrderPlaced(orderEvent)
            EventType.ORDER_CANCELLED -> handleOrderCancelled(orderEvent)
            else -> {
                println("Received event type ${orderEvent.eventType} for order ${orderEvent.orderId}")
            }
        }
    }

    private fun handleOrderPlaced(orderEvent: OrderEvent) {
        val kitchenOrder = KitchenOrder(
            orderId = orderEvent.orderId,
            customerName = orderEvent.customerName,
            customerEmail = orderEvent.customerEmail,
            status = KitchenOrderStatus.RECEIVED,
            totalAmount = orderEvent.totalAmount,
            items = emptyList(), // Will be set after item creation
            estimatedCompletionTime = LocalDateTime.now().plusMinutes(30) // Default 30 mins
        )

        val kitchenOrderItems = orderEvent.items.map { itemEvent ->
            KitchenOrderItem(
                kitchenOrder = kitchenOrder,
                menuItemId = itemEvent.menuItemId,
                menuItemName = itemEvent.menuItemName,
                quantity = itemEvent.quantity,
                unitPrice = itemEvent.unitPrice,
                totalPrice = itemEvent.totalPrice,
                preparationTimeMinutes = 15 // Default preparation time
            )
        }

        // Update kitchen order with items
        kitchenOrder.items = kitchenOrderItems

        kitchenOrderRepository.save(kitchenOrder)
        
        println("Kitchen order received: ${kitchenOrder.orderId} for customer ${kitchenOrder.customerName}")
    }

    private fun handleOrderCancelled(orderEvent: OrderEvent) {
        val kitchenOrder = kitchenOrderRepository.findById(orderEvent.orderId)
        if (kitchenOrder.isPresent) {
            val order = kitchenOrder.get()
            order.status = KitchenOrderStatus.CANCELLED
            kitchenOrderRepository.save(order)
            println("Kitchen order cancelled: ${order.orderId}")
        }
    }

    fun startPreparation(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.IN_PREPARATION
        kitchenOrder.startedPreparationAt = LocalDateTime.now()
        kitchenOrder.estimatedCompletionTime = LocalDateTime.now().plusMinutes(25)

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order preparing event
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_PREPARING)

        return updatedOrder
    }

    fun markAsReady(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.READY
        kitchenOrder.completedAt = LocalDateTime.now()

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order ready event
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_READY)

        return updatedOrder
    }

    fun markAsCompleted(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.COMPLETED

        return kitchenOrderRepository.save(kitchenOrder)
    }

    private fun publishOrderStatusUpdate(orderId: Long, eventType: EventType) {
        val kitchenOrder = kitchenOrderRepository.findById(orderId).orElse(null)
        if (kitchenOrder != null) {
            val orderEvent = OrderEvent(
                orderId = kitchenOrder.orderId,
                customerName = kitchenOrder.customerName,
                customerEmail = kitchenOrder.customerEmail,
                customerPhone = "", // Not stored in kitchen order
                status = when (kitchenOrder.status) {
                    KitchenOrderStatus.IN_PREPARATION -> OrderStatus.PREPARING
                    KitchenOrderStatus.READY -> OrderStatus.READY
                    KitchenOrderStatus.COMPLETED -> OrderStatus.COMPLETED
                    KitchenOrderStatus.CANCELLED -> OrderStatus.CANCELLED
                    else -> OrderStatus.PENDING
                },
                totalAmount = kitchenOrder.totalAmount,
                items = kitchenOrder.items.map { item ->
                    OrderItemEvent(
                        menuItemId = item.menuItemId,
                        menuItemName = item.menuItemName,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        totalPrice = item.totalPrice
                    )
                },
                eventType = eventType
            )

            kafkaTemplate.send(ORDER_TOPIC, orderEvent.orderId.toString(), orderEvent)
        }
    }

    fun getKitchenOrders(): List<KitchenOrder> {
        return kitchenOrderRepository.findAll()
    }

    fun getKitchenOrdersByStatus(status: KitchenOrderStatus): List<KitchenOrder> {
        return kitchenOrderRepository.findByStatus(status)
    }

    fun getKitchenOrderById(orderId: Long): KitchenOrder {
        return kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }
    }
}
