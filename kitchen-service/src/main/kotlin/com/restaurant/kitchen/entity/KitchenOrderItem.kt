package com.restaurant.kitchen.entity

import jakarta.persistence.*
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@Entity
@Table(name = "kitchen_order_items")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
data class KitchenOrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitchen_order_id", nullable = false)
    var kitchenOrder: KitchenOrder,
    
    @Column(nullable = false)
    val menuItemId: Long,
    
    @Column(nullable = false)
    val menuItemName: String,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false)
    val unitPrice: java.math.BigDecimal,
    
    @Column(nullable = false)
    val totalPrice: java.math.BigDecimal,
    
    @Column(nullable = false)
    val preparationTimeMinutes: Int = 15, // Default preparation time
    
    @Column
    val notes: String? = null
) {
    // Default constructor for Hibernate
    constructor() : this(
        id = null,
        kitchenOrder = KitchenOrder(),
        menuItemId = 0L,
        menuItemName = "",
        quantity = 0,
        unitPrice = java.math.BigDecimal.ZERO,
        totalPrice = java.math.BigDecimal.ZERO,
        preparationTimeMinutes = 15,
        notes = null
    )
}
