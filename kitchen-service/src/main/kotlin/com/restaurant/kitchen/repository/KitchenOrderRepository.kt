package com.restaurant.kitchen.repository

import com.restaurant.kitchen.entity.KitchenOrder
import com.restaurant.kitchen.entity.KitchenOrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface KitchenOrderRepository : JpaRepository<KitchenOrder, Long> {
    
    fun findByStatus(status: KitchenOrderStatus): List<KitchenOrder>
    
    fun findByCustomerEmail(customerEmail: String): List<KitchenOrder>
    
    @Query("SELECT ko FROM KitchenOrder ko WHERE ko.status = :status AND ko.receivedAt >= :startDate")
    fun findByStatusAndReceivedAtAfter(
        status: KitchenOrderStatus, 
        startDate: String
    ): List<KitchenOrder>
    
    @Query("SELECT COUNT(ko) FROM KitchenOrder ko WHERE ko.status = :status")
    fun countByStatus(status: KitchenOrderStatus): Long
    
    @Query("SELECT ko FROM KitchenOrder ko WHERE ko.estimatedCompletionTime <= :now AND ko.status IN ('RECEIVED', 'IN_PREPARATION')")
    fun findOverdueOrders(now: String): List<KitchenOrder>
}
