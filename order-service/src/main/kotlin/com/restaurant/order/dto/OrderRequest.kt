package com.restaurant.order.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.Valid

data class CreateOrderRequest(
    @field:NotBlank(message = "Customer name is required")
    val customerName: String,
    
    @field:NotBlank(message = "Customer email is required")
    @field:Email(message = "Invalid email format")
    val customerEmail: String,
    
    @field:NotBlank(message = "Customer phone is required")
    val customerPhone: String,
    
    @field:NotEmpty(message = "Order items are required")
    @field:Valid
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @field:NotNull(message = "Menu item ID is required")
    val menuItemId: Long,
    
    @field:NotBlank(message = "Menu item name is required")
    val menuItemName: String,
    
    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be positive")
    val quantity: Int,
    
    @field:NotNull(message = "Unit price is required")
    @field:Positive(message = "Unit price must be positive")
    val unitPrice: java.math.BigDecimal
)
