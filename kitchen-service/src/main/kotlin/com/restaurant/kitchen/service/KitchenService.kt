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
        println("=== KAFKA EVENT RECEIVED ===")
        println("Event Type: ${orderEvent.eventType}")
        println("Order ID: ${orderEvent.orderId}")
        println("Customer: ${orderEvent.customerName}")
        println("Items count: ${orderEvent.items.size}")
        println("==============================")
        
        try {
            when (orderEvent.eventType) {
                EventType.ORDER_PLACED -> {
                    println("Processing ORDER_PLACED event")
                    handleOrderPlaced(orderEvent)
                }
                EventType.ORDER_CANCELLED -> {
                    println("Processing ORDER_CANCELLED event")
                    handleOrderCancelled(orderEvent)
                }
                else -> {
                    println("Received event type ${orderEvent.eventType} for order ${orderEvent.orderId}")
                }
            }
        } catch (e: Exception) {
            println("ERROR processing order event: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun handleOrderPlaced(orderEvent: OrderEvent) {
        println("=== HANDLE ORDER PLACED ===")
        println("Creating kitchen order for orderId: ${orderEvent.orderId}")
        
        val kitchenOrder = KitchenOrder(
            orderId = orderEvent.orderId,
            customerName = orderEvent.customerName,
            customerEmail = orderEvent.customerEmail,
            status = KitchenOrderStatus.RECEIVED,
            totalAmount = orderEvent.totalAmount,
            items = mutableListOf(), // Will be set after item creation
            estimatedCompletionTime = java.time.LocalDateTime.now().plusMinutes(30).toString() // Default 30 mins
        )
        
        println("Kitchen order created: ${kitchenOrder.orderId}")

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
        }.toMutableList()
        
        println("Created ${kitchenOrderItems.size} kitchen order items")

        // Update kitchen order with items
        kitchenOrder.items = kitchenOrderItems

        println("Saving kitchen order to database...")
        val savedOrder = kitchenOrderRepository.save(kitchenOrder)
        println("Kitchen order saved with orderId: ${savedOrder.orderId}")
        
        println("Kitchen order received: ${kitchenOrder.orderId} for customer ${kitchenOrder.customerName}")
        println("=== ORDER PLACED HANDLED ===")
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
        kitchenOrder.startedPreparationAt = java.time.LocalDateTime.now().toString()
        kitchenOrder.estimatedCompletionTime = java.time.LocalDateTime.now().plusMinutes(25).toString()

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order preparing event
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_PREPARING)

        return updatedOrder
    }

    fun markAsReady(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.READY
        kitchenOrder.completedAt = java.time.LocalDateTime.now().toString()

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
