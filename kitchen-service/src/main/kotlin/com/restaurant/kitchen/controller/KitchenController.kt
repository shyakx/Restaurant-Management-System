package com.restaurant.kitchen.controller

import com.restaurant.kitchen.dto.KitchenOrderResponse
import com.restaurant.kitchen.dto.KitchenOrderItemResponse
import com.restaurant.kitchen.dto.KitchenOrderStatus as DtoKitchenOrderStatus
import com.restaurant.kitchen.entity.KitchenOrderStatus as EntityKitchenOrderStatus
import com.restaurant.kitchen.service.KitchenService
import com.restaurant.kitchen.mapper.KitchenOrderMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Kitchen API endpoints
 */
@RestController
@RequestMapping("/api/kitchen")
class KitchenController(
    private val kitchenService: KitchenService
) {

    @GetMapping("/orders")
    fun getAllKitchenOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<KitchenOrderResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = kitchenService.getKitchenOrders(pageable)
        return orders.map { KitchenOrderMapper.toResponse(it) }
    }

    @GetMapping("/orders/{orderId}")
    fun getKitchenOrderById(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.getKitchenOrderById(orderId)
        return ResponseEntity.ok(KitchenOrderMapper.toResponse(order))
    }

    @GetMapping("/orders/status/{status}")
    fun getKitchenOrdersByStatus(
        @PathVariable status: DtoKitchenOrderStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<KitchenOrderResponse>> {
        // Convert DTO status to entity status
        val entityStatus = EntityKitchenOrderStatus.valueOf(status.name)
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = kitchenService.getKitchenOrdersByStatus(entityStatus, pageable)
        val orderResponses = orders.map { KitchenOrderMapper.toResponse(it) }
        return ResponseEntity.ok(orderResponses)
    }

    @PutMapping("/orders/{orderId}/start-preparation")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun startPreparation(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.startPreparation(orderId)
        return ResponseEntity.ok(KitchenOrderMapper.toResponse(order))
    }

    @PutMapping("/orders/{orderId}/ready")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun markAsReady(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsReady(orderId)
        return ResponseEntity.ok(KitchenOrderMapper.toResponse(order))
    }

    @PutMapping("/orders/{orderId}/cancel")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.cancelOrder(orderId)
        return ResponseEntity.ok(KitchenOrderMapper.toResponse(order))
    }

    @PutMapping("/orders/{orderId}/complete")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun markAsCompleted(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsCompleted(orderId)
        return ResponseEntity.ok(KitchenOrderMapper.toResponse(order))
    }

    @GetMapping("/dashboard/stats")
    @Cacheable(value = ["kitchen-stats"], key = "'dashboard'")
    fun getKitchenDashboardStats(): Map<String, Any> {
        // Get orders by status for dashboard
        val receivedOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.RECEIVED)
        val inPreparationOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.IN_PREPARATION)
        val readyOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.READY)

        // Calculate stats
        return mapOf(
            "received" to receivedOrders.size,
            "inPreparation" to inPreparationOrders.size,
            "ready" to readyOrders.size,
            "totalActive" to (receivedOrders.size + inPreparationOrders.size + readyOrders.size)
        )
    }
}
