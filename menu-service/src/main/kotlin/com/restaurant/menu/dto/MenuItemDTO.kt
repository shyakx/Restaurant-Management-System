package com.restaurant.menu.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MenuItemResponse(
    val id: Long?,
    val name: String,
    val description: String?,
    @JsonProperty("price")
    val price: Double,
    val category: String?,
    val available: Boolean
)

data class CreateMenuItemRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val category: String?,
    val available: Boolean = true
)

data class UpdateMenuItemRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val category: String?,
    val available: Boolean
)
