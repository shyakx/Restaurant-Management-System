package com.restaurant.order.controller

import com.restaurant.order.dto.CreateOrderRequest
import com.restaurant.order.dto.UpdateOrderStatusRequest
import com.restaurant.order.dto.OrderResponse
import com.restaurant.order.dto.OrderItemResponse
import com.restaurant.order.entity.Order
import com.restaurant.order.entity.OrderStatus
import com.restaurant.order.service.OrderService
import com.restaurant.order.mapper.OrderMapper
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Order API endpoints
 */
@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toResponse(order))
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderById(id)
        return ResponseEntity.ok(OrderMapper.toResponse(order))
    }

    @GetMapping
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = orderService.getAllOrders(pageable)
        val orderResponses = orders.map { OrderMapper.toResponse(it) }
        return ResponseEntity.ok(orderResponses)
    }

    @GetMapping("/status/{status}")
    fun getOrdersByStatus(
        @PathVariable status: OrderStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = orderService.getOrdersByStatus(status, pageable)
        val orderResponses = orders.map { OrderMapper.toResponse(it) }
        return ResponseEntity.ok(orderResponses)
    }

    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @Valid @RequestBody statusRequest: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        val status = try {
            OrderStatus.valueOf(statusRequest.status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid order status: ${statusRequest.status}")
        }
        val order = orderService.updateOrderStatus(id, status)
        return ResponseEntity.ok(OrderMapper.toResponse(order))
    }

    @GetMapping("/customer/{email}")
    fun getOrdersByCustomerEmail(
        @PathVariable email: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = orderService.getOrdersByCustomerEmail(email, pageable)
        val orderResponses = orders.map { OrderMapper.toResponse(it) }
        return ResponseEntity.ok(orderResponses)
    }

    @GetMapping("/statistics")
    fun getOrderStatistics(): ResponseEntity<Map<String, Long>> {
        val statistics = orderService.getOrderStatistics()
        return ResponseEntity.ok(statistics)
    }
}
