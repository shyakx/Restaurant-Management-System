package com.restaurant.order.entity

import jakarta.persistence.*
import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    var order: Order? = null,
    
    @Column(nullable = false)
    val menuItemId: Long = 0L,
    
    @Column(nullable = false)
    val menuItemName: String = "",
    
    @Column(nullable = false)
    val quantity: Int = 0,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalPrice: BigDecimal = BigDecimal.ZERO
) {
    // Default constructor for Hibernate
    constructor() : this(
        id = null,
        order = null,
        menuItemId = 0L,
        menuItemName = "",
        quantity = 0,
        unitPrice = BigDecimal.ZERO,
        totalPrice = BigDecimal.ZERO
    )
}
