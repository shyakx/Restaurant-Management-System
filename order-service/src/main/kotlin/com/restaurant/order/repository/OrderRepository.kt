package com.restaurant.order.repository

import com.restaurant.order.entity.Order
import com.restaurant.order.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    
    fun findByStatus(status: OrderStatus): List<Order>
    
    fun findByCustomerEmail(customerEmail: String): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate")
    fun findByStatusAndCreatedAtAfter(
        @Param("status") status: OrderStatus, 
        @Param("startDate") startDate: String
    ): List<Order>
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    fun countByStatus(@Param("status") status: OrderStatus): Long
}
