package com.restaurant.order.dto

import jakarta.validation.constraints.NotNull

data class UpdateOrderStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: String = ""
) {
    // Default constructor for Jackson
    constructor() : this("")
}
