package com.restaurant.order.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "menu_items")
data class MenuItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val name: String = "",
    
    @Column(columnDefinition = "TEXT")
    val description: String = "",
    
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false, length = 100)
    val category: String = "",
    
    @Column(nullable = false)
    val available: Boolean = true,
    
    @Column(length = 500)
    val imageUrl: String? = null,
    
    @Column(nullable = false)
    val preparationTime: Int = 15, // minutes
    
    @Column(columnDefinition = "TEXT")
    val ingredients: String? = null, // JSON string of ingredients list
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val updatedAt: LocalDateTime? = null
) : Serializable {
    
    // Default constructor for Hibernate
    constructor() : this(
        id = null,
        name = "",
        description = "",
        price = BigDecimal.ZERO,
        category = "",
        available = true,
        imageUrl = null,
        preparationTime = 15,
        ingredients = null,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
}
