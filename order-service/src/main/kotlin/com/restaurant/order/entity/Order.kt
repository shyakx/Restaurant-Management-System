package com.restaurant.order.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val customerName: String = "",
    
    @Column(nullable = false)
    val customerEmail: String = "",
    
    @Column(nullable = false)
    val customerPhone: String = "",
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    
    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var items: List<OrderItem> = emptyList(),
    
    @Column(nullable = false)
    val createdAt: String = "",
    
    @Column
    var updatedAt: String? = null
) {
    // Default constructor for Hibernate
    constructor() : this(
        id = null,
        customerName = "",
        customerEmail = "",
        customerPhone = "",
        status = OrderStatus.PENDING,
        totalAmount = BigDecimal.ZERO,
        items = emptyList(),
        createdAt = "",
        updatedAt = null
    )
}

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}
