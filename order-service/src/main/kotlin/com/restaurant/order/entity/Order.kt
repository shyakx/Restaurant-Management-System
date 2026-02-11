package com.restaurant.order.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonManagedReference

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val customerName: String,
    
    @Column(nullable = false)
    val customerEmail: String,
    
    @Column(nullable = false)
    val customerPhone: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    var items: List<OrderItem> = emptyList(),
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    var updatedAt: LocalDateTime? = null
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}
