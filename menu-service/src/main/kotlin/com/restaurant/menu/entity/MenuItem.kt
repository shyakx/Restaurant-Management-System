package com.restaurant.menu.entity

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import com.restaurant.menu.dto.MenuItemResponse

/**
 * Menu item entity with validation.
 */
@Entity
@Table(name = "menu_items")
data class MenuItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    @NotBlank(message = "Menu item name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,
    
    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have maximum 8 integer digits and 2 decimal places")
    val price: BigDecimal,
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    val category: String? = null,
    
    val available: Boolean = true
) {
    constructor() : this(
        id = null,
        name = "",
        description = null,
        price = BigDecimal.ZERO,
        category = null,
        available = true
    )
}

fun MenuItem.toResponse(): MenuItemResponse {
    return MenuItemResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price.toDouble(),
        category = this.category,
        available = this.available
    )
}
