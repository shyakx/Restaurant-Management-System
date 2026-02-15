package com.restaurant.order.controller

import com.restaurant.order.dto.CreateOrderRequest
import com.restaurant.order.dto.UpdateOrderStatusRequest
import com.restaurant.order.dto.OrderResponse
import com.restaurant.order.dto.OrderItemResponse
import com.restaurant.order.entity.Order
import com.restaurant.order.entity.OrderStatus
import com.restaurant.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Order Controller - REST API for order management.
 */
@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    // Create new order
    @PostMapping
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order.toResponse())
    }

    // Get order by ID
    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderById(id)
        return ResponseEntity.ok(order.toResponse())
    }

    // Get all orders
    @GetMapping
    fun getAllOrders(): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(orders.map { it.toResponse() })
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable status: OrderStatus): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getOrdersByStatus(status)
        return ResponseEntity.ok(orders.map { it.toResponse() })
    }

    // Update order status
    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @Valid @RequestBody statusRequest: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        // Parse status with fallback to PENDING for invalid values
        val status = try {
            OrderStatus.valueOf(statusRequest.status.uppercase())
        } catch (e: IllegalArgumentException) {
            OrderStatus.PENDING
        }
        val order = orderService.updateOrderStatus(id, status)
        return ResponseEntity.ok(order.toResponse())
    }

    // Get customer orders by email
    @GetMapping("/customer/{email}")
    fun getOrdersByCustomerEmail(@PathVariable email: String): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getOrdersByCustomerEmail(email)
        return ResponseEntity.ok(orders.map { it.toResponse() })
    }

    // Get order statistics
    @GetMapping("/statistics")
    fun getOrderStatistics(): ResponseEntity<Map<String, Long>> {
        val statistics = orderService.getOrderStatistics()
        return ResponseEntity.ok(statistics)
    }
}

// Extension functions for entity-to-DTO mapping

// Convert Order entity to OrderResponse DTO
fun Order.toResponse(): OrderResponse {
    return OrderResponse(
        id = this.id!!,
        customerName = this.customerName,
        customerEmail = this.customerEmail,
        customerPhone = this.customerPhone,
        status = this.status.name,
        totalAmount = this.totalAmount,
        items = this.items.map { it.toResponse() },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// Convert OrderItem entity to OrderItemResponse DTO
fun com.restaurant.order.entity.OrderItem.toResponse(): OrderItemResponse {
    return OrderItemResponse(
        id = this.id!!,
        menuItemId = this.menuItemId,
        menuItemName = this.menuItemName,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}
