package com.restaurant.order.controller

import com.restaurant.order.dto.CreateOrderRequest
import com.restaurant.order.entity.Order
import com.restaurant.order.entity.OrderStatus
import com.restaurant.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<Order> {
        val order = orderService.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<Order> {
        val order = orderService.getOrderById(id)
        return ResponseEntity.ok(order)
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable status: OrderStatus): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByStatus(status)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestBody statusRequest: Map<String, String>
    ): ResponseEntity<Order> {
        val status = OrderStatus.valueOf(statusRequest["status"]?.uppercase() ?: "PENDING")
        val order = orderService.updateOrderStatus(id, status)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/customer/{email}")
    fun getOrdersByCustomerEmail(@PathVariable email: String): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByCustomerEmail(email)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/statistics")
    fun getOrderStatistics(): ResponseEntity<Map<String, Long>> {
        val statistics = orderService.getOrderStatistics()
        return ResponseEntity.ok(statistics)
    }
}
