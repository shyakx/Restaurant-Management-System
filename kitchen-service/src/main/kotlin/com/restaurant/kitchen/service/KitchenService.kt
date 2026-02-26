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

/**
 * Kitchen Service - Manages kitchen order operations.
 */
@Service
@Transactional
class KitchenService(
    private val kitchenOrderRepository: KitchenOrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        const val ORDER_TOPIC = "order-events"
    }

    // Process order events from Kafka
    @KafkaListener(topics = ["order-events"], groupId = "kitchen-service-group")
    fun handleOrderEvent(orderEvent: OrderEvent) {
        try {
            when (orderEvent.eventType) {
                EventType.ORDER_PLACED -> handleOrderPlaced(orderEvent)
                EventType.ORDER_CANCELLED -> handleOrderCancelled(orderEvent)
                EventType.ORDER_PREPARING -> handleOrderPreparing(orderEvent)
                EventType.ORDER_READY -> handleOrderReady(orderEvent)
                else -> println("Unhandled event type: ${orderEvent.eventType} for order ${orderEvent.orderId}")
            }
        } catch (e: Exception) {
            println("Error processing order event: ${e.message}")
        }
    }

    // Handle new order placement
    private fun handleOrderPlaced(orderEvent: OrderEvent) {
        // Create kitchen order with default 30-minute preparation time
        val kitchenOrder = KitchenOrder(
            orderId = orderEvent.orderId,
            customerName = orderEvent.customerName,
            customerEmail = orderEvent.customerEmail,
            status = KitchenOrderStatus.RECEIVED,
            totalAmount = orderEvent.totalAmount,
            items = mutableListOf(),
            estimatedCompletionTime = java.time.LocalDateTime.now().plusMinutes(30).toString()
        )
        
        val kitchenOrderItems = orderEvent.items.map { itemEvent ->
            KitchenOrderItem(
                kitchenOrder = kitchenOrder,
                menuItemId = itemEvent.menuItemId,
                menuItemName = itemEvent.menuItemName,
                quantity = itemEvent.quantity,
                unitPrice = itemEvent.unitPrice,
                totalPrice = itemEvent.totalPrice,
                preparationTimeMinutes = 15
            )
        }.toMutableList()
        
        kitchenOrder.items = kitchenOrderItems

        kitchenOrderRepository.save(kitchenOrder)
    }

    // Handle order cancellation
    private fun handleOrderCancelled(orderEvent: OrderEvent) {
        val kitchenOrder = kitchenOrderRepository.findById(orderEvent.orderId)
        if (kitchenOrder.isPresent) {
            val order = kitchenOrder.get()
            order.status = KitchenOrderStatus.CANCELLED
            kitchenOrderRepository.save(order)
        } else {
            println("Order #${orderEvent.orderId} not found for cancellation")
        }
    }

    // Handle order preparation event
    private fun handleOrderPreparing(orderEvent: OrderEvent) {
        val kitchenOrder = kitchenOrderRepository.findById(orderEvent.orderId)
        if (kitchenOrder.isPresent) {
            val order = kitchenOrder.get()
            order.status = KitchenOrderStatus.IN_PREPARATION
            order.startedPreparationAt = java.time.LocalDateTime.now().toString()
            kitchenOrderRepository.save(order)
        } else {
            println("Order #${orderEvent.orderId} not found for preparation")
        }
    }

    // Handle order ready event
    private fun handleOrderReady(orderEvent: OrderEvent) {
        val kitchenOrder = kitchenOrderRepository.findById(orderEvent.orderId)
        if (kitchenOrder.isPresent) {
            val order = kitchenOrder.get()
            order.status = KitchenOrderStatus.READY
            order.completedAt = java.time.LocalDateTime.now().toString()
            kitchenOrderRepository.save(order)
        } else {
            println("Order #${orderEvent.orderId} not found for ready status")
        }
    }

    // Start order preparation
    fun startPreparation(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        // Update order status and timestamps
        kitchenOrder.status = KitchenOrderStatus.IN_PREPARATION
        kitchenOrder.startedPreparationAt = java.time.LocalDateTime.now().toString()
        kitchenOrder.estimatedCompletionTime = java.time.LocalDateTime.now().plusMinutes(25).toString()

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order preparing event to Kafka
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_PREPARING)

        return updatedOrder
    }

    // Mark order as ready for pickup
    fun markAsReady(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        // Update order status and completion time
        kitchenOrder.status = KitchenOrderStatus.READY
        kitchenOrder.completedAt = java.time.LocalDateTime.now().toString()

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order ready event to Kafka
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_READY)

        return updatedOrder
    }

    // Cancel order and publish cancellation event
    fun cancelOrder(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.CANCELLED

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order cancelled event to Kafka
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_CANCELLED)

        return updatedOrder
    }

    // Mark order as completed after pickup
    fun markAsCompleted(orderId: Long): KitchenOrder {
        val kitchenOrder = kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }

        kitchenOrder.status = KitchenOrderStatus.COMPLETED

        val updatedOrder = kitchenOrderRepository.save(kitchenOrder)

        // Publish order completed event to Kafka
        publishOrderStatusUpdate(updatedOrder.orderId, EventType.ORDER_COMPLETED)

        return updatedOrder
    }

    // Publish kitchen order status updates to Kafka
    private fun publishOrderStatusUpdate(orderId: Long, eventType: EventType) {
        try {
            val kitchenOrder = kitchenOrderRepository.findById(orderId).orElse(null)
            if (kitchenOrder != null) {
                val orderEvent = OrderEvent(
                    orderId = kitchenOrder.orderId,
                    customerName = kitchenOrder.customerName,
                    customerEmail = kitchenOrder.customerEmail,
                    customerPhone = "", // Not stored in kitchen order
                    status = when (kitchenOrder.status) {
                        KitchenOrderStatus.IN_PREPARATION -> com.restaurant.kitchen.dto.OrderStatus.PREPARING
                        KitchenOrderStatus.READY -> com.restaurant.kitchen.dto.OrderStatus.READY
                        KitchenOrderStatus.COMPLETED -> com.restaurant.kitchen.dto.OrderStatus.COMPLETED
                        KitchenOrderStatus.CANCELLED -> com.restaurant.kitchen.dto.OrderStatus.CANCELLED
                        else -> com.restaurant.kitchen.dto.OrderStatus.PENDING
                    },
                    totalAmount = kitchenOrder.totalAmount,
                    items = kitchenOrder.items.map { item ->
                        com.restaurant.kitchen.dto.OrderItemEvent(
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
            } else {
                println("Failed to publish event: Order #$orderId not found")
            }
        } catch (e: Exception) {
            println("Error publishing event: $eventType for order #$orderId - ${e.message}")
            e.printStackTrace()
        }
    }

    // Get all kitchen orders
    fun getKitchenOrders(): List<KitchenOrder> {
        return kitchenOrderRepository.findAll()
    }

    // Get kitchen orders by status
    fun getKitchenOrdersByStatus(status: KitchenOrderStatus): List<KitchenOrder> {
        return kitchenOrderRepository.findByStatus(status)
    }

    // Get kitchen order by ID
    fun getKitchenOrderById(orderId: Long): KitchenOrder {
        return kitchenOrderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Kitchen order not found: $orderId") }
    }
}
