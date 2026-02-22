package com.restaurant.kitchen.service

import com.restaurant.kitchen.dto.EventType
import com.restaurant.kitchen.entity.KitchenOrder
import com.restaurant.kitchen.entity.KitchenOrderStatus
import com.restaurant.kitchen.repository.KitchenOrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Automatically progresses orders through kitchen stages for realistic demo
 */
@Service
@Transactional
class OrderProgressionService(
    private val kitchenOrderRepository: KitchenOrderRepository,
    private val kitchenService: KitchenService
) {

    // Progress orders every 30 seconds for demo purposes
    @Scheduled(fixedDelay = 30000)
    fun progressOrders() {
        println("⏰ [PROGRESSION] Checking orders for automatic progression...")
        
        val pendingOrders = kitchenOrderRepository.findByStatus(KitchenOrderStatus.RECEIVED)
        val preparingOrders = kitchenOrderRepository.findByStatus(KitchenOrderStatus.IN_PREPARATION)
        
        // Progress pending orders to preparation after 1 minute
        pendingOrders.forEach { order ->
            val orderTime = LocalDateTime.parse(order.receivedAt)
            if (orderTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
                println("🔄 [PROGRESSION] Auto-progressing order #${order.orderId} to PREPARING")
                kitchenService.startPreparation(order.orderId)
            }
        }
        
        // Progress preparing orders to ready after 2 minutes
        preparingOrders.forEach { order ->
            val prepTime = order.startedPreparationAt?.let { LocalDateTime.parse(it) }
            if (prepTime != null && prepTime.isBefore(LocalDateTime.now().minusMinutes(2))) {
                println("🔄 [PROGRESSION] Auto-progressing order #${order.orderId} to READY")
                kitchenService.markAsReady(order.orderId)
            }
        }
    }
}
