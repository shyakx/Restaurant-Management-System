package com.restaurant.order.dto

import java.math.BigDecimal

/**
 * Menu Item Response DTO
 * Represents a menu item that can be ordered
 */
data class MenuItemResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val category: String,
    val available: Boolean
)
