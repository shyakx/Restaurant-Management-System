package com.restaurant.menu.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern

// Create User Request
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]", 
                   message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    val password: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    val roles: Set<String> = setOf("USER")
)

// Update User Request
data class UpdateUserRequest(
    @field:Email(message = "Invalid email format")
    val email: String? = null,
    
    val roles: Set<String>? = null
)

// Change Password Request
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]", 
                   message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    val newPassword: String
)

// User Response
data class UserResponse(
    val id: Long?,
    val username: String,
    val email: String,
    val roles: Set<String>,
    val enabled: Boolean,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?
)

// User Summary Response (for lists)
data class UserSummaryResponse(
    val id: Long?,
    val username: String,
    val email: String,
    val roles: Set<String>,
    val enabled: Boolean
)
