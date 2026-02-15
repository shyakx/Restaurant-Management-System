package com.restaurant.order.service

import com.restaurant.order.dto.*
import com.restaurant.order.entity.*
import com.restaurant.order.repository.OrderRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        const val ORDER_TOPIC = "order-events"
    }

    @CacheEvict(value = ["orders"], allEntries = true)
    fun createOrder(request: CreateOrderRequest): Order {
        // Create order with default total amount (will be calculated later)
        val order = Order(
            customerName = request.customerName,
            customerEmail = request.customerEmail,
            customerPhone = request.customerPhone,
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal.ZERO, // Will be updated after items are created
            items = emptyList() // Will be set after item creation
        )

        // Create order items with default values
        val orderItems = request.items.map { itemRequest ->
            OrderItem(
                order = order,
                menuItemId = itemRequest.menuItemId,
                menuItemName = "Menu Item ${itemRequest.menuItemId}", // Default name
                quantity = itemRequest.quantity,
                unitPrice = BigDecimal.ONE, // Default price
                totalPrice = BigDecimal.ONE * itemRequest.quantity.toBigDecimal() // Calculate total
            ).also { item ->
                // Ensure the order reference is set
                item.order = order
            }
        }

        // Update order with items
        order.items = orderItems
        
        // Calculate total amount (simple quantity sum for now)
        val totalAmount = BigDecimal(orderItems.sumOf { it.quantity })
        order.totalAmount = totalAmount

        val savedOrder = orderRepository.save(order)

        // Publish order placed event
        val orderEvent = OrderEvent(
            orderId = savedOrder.id!!,
            customerName = savedOrder.customerName,
            customerEmail = savedOrder.customerEmail,
            customerPhone = savedOrder.customerPhone,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount,
            items = savedOrder.items.map { item ->
                OrderItemEvent(
                    menuItemId = item.menuItemId,
                    menuItemName = item.menuItemName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            eventType = EventType.ORDER_PLACED
        )

        kafkaTemplate.send(ORDER_TOPIC, orderEvent.orderId.toString(), orderEvent)

        return savedOrder
    }

    @Cacheable(value = ["orders"], key = "#id")
    fun getOrderById(id: Long): Order {
        return orderRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Order not found with id: $id") }
    }

    @Cacheable(value = ["orders"], key = "'all-orders'")
    fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    @Cacheable(value = ["orders"], key = "'status-' + #status")
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orderRepository.findByStatus(status)
    }

    @CacheEvict(value = ["orders"], allEntries = true)
    fun updateOrderStatus(id: Long, status: OrderStatus): Order {
        val order = getOrderById(id)
        val updatedOrder = order.copy(
            status = status,
            updatedAt = java.time.LocalDateTime.now().toString()
        )
        
        val savedOrder = orderRepository.save(updatedOrder)

        // Publish status update event
        val orderEvent = OrderEvent(
            orderId = savedOrder.id!!,
            customerName = savedOrder.customerName,
            customerEmail = savedOrder.customerEmail,
            customerPhone = savedOrder.customerPhone,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount,
            items = savedOrder.items.map { item ->
                OrderItemEvent(
                    menuItemId = item.menuItemId,
                    menuItemName = item.menuItemName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            eventType = when (status) {
                OrderStatus.CONFIRMED -> EventType.ORDER_CONFIRMED
                OrderStatus.PREPARING -> EventType.ORDER_PREPARING
                OrderStatus.READY -> EventType.ORDER_READY
                OrderStatus.COMPLETED -> EventType.ORDER_COMPLETED
                OrderStatus.CANCELLED -> EventType.ORDER_CANCELLED
                else -> EventType.ORDER_PLACED
            }
        )

        kafkaTemplate.send(ORDER_TOPIC, orderEvent.orderId.toString(), orderEvent)

        return savedOrder
    }

    fun getOrdersByCustomerEmail(customerEmail: String): List<Order> {
        return orderRepository.findByCustomerEmail(customerEmail)
    }

    fun getOrderStatistics(): Map<String, Long> {
        return OrderStatus.values().associate { status ->
            status.name to orderRepository.countByStatus(status)
        }
    }
}
