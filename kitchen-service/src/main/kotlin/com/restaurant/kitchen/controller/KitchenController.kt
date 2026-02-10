package com.restaurant.kitchen.controller

import com.restaurant.kitchen.entity.KitchenOrder
import com.restaurant.kitchen.entity.KitchenOrderStatus
import com.restaurant.kitchen.service.KitchenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kitchen")
class KitchenController(private val kitchenService: KitchenService) {

    @GetMapping("/orders")
    fun getAllKitchenOrders(): ResponseEntity<List<KitchenOrder>> {
        val orders = kitchenService.getKitchenOrders()
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/orders/{orderId}")
    fun getKitchenOrderById(@PathVariable orderId: Long): ResponseEntity<KitchenOrder> {
        val order = kitchenService.getKitchenOrderById(orderId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/orders/status/{status}")
    fun getKitchenOrdersByStatus(@PathVariable status: KitchenOrderStatus): ResponseEntity<List<KitchenOrder>> {
        val orders = kitchenService.getKitchenOrdersByStatus(status)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/orders/{orderId}/start-preparation")
    fun startPreparation(@PathVariable orderId: Long): ResponseEntity<KitchenOrder> {
        val order = kitchenService.startPreparation(orderId)
        return ResponseEntity.ok(order)
    }

    @PutMapping("/orders/{orderId}/ready")
    fun markAsReady(@PathVariable orderId: Long): ResponseEntity<KitchenOrder> {
        val order = kitchenService.markAsReady(orderId)
        return ResponseEntity.ok(order)
    }

    @PutMapping("/orders/{orderId}/complete")
    fun markAsCompleted(@PathVariable orderId: Long): ResponseEntity<KitchenOrder> {
        val order = kitchenService.markAsCompleted(orderId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/dashboard/stats")
    fun getKitchenDashboardStats(): ResponseEntity<Map<String, Any>> {
        val receivedOrders = kitchenService.getKitchenOrdersByStatus(KitchenOrderStatus.RECEIVED)
        val inPreparationOrders = kitchenService.getKitchenOrdersByStatus(KitchenOrderStatus.IN_PREPARATION)
        val readyOrders = kitchenService.getKitchenOrdersByStatus(KitchenOrderStatus.READY)

        val stats = mapOf(
            "received" to receivedOrders.size,
            "inPreparation" to inPreparationOrders.size,
            "ready" to readyOrders.size,
            "totalActive" to (receivedOrders.size + inPreparationOrders.size + readyOrders.size)
        )

        return ResponseEntity.ok(stats)
    }
}
