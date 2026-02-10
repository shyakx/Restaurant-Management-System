package com.restaurant.order.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    
    @Column(nullable = false)
    val menuItemId: Long,
    
    @Column(nullable = false)
    val menuItemName: String,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalPrice: BigDecimal
)
