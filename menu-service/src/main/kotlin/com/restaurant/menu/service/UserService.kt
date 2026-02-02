package com.restaurant.menu.service

import com.restaurant.menu.dto.CreateUserRequest
import com.restaurant.menu.dto.UpdateUserRequest
import com.restaurant.menu.dto.ChangePasswordRequest
import com.restaurant.menu.dto.UserResponse
import com.restaurant.menu.entity.User
import com.restaurant.menu.repository.UserRepository
import com.restaurant.menu.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * User management service with secure password handling and audit logging.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val log = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * Creates new user with encrypted password and role assignment.
     */
    fun createUser(request: CreateUserRequest): UserResponse {
        // Validate uniqueness
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username '${request.username}' already exists")
        }
        
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email '${request.email}' already exists")
        }
        
        // Create user with encrypted password
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            roles = request.roles.map { "ROLE_$it" }.toSet(),
            enabled = true
        )
        
        val savedUser = userRepository.save(user)
        
        // Audit logging
        logUserAction(savedUser.id!!, "USER_CREATED", getCurrentUsername())
        
        return savedUser.toResponse()
    }
    
    /**
     * Retrieves all users for admin management.
     */
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { user -> user.toResponse() }
    }
    
    /**
     * Gets user by ID with proper error handling.
     */
    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        return user.toResponse()
    }
    
    /**
     * Gets user by username with proper error handling.
     */
    fun getUserByUsername(username: String): UserResponse {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found with username: $username")
        return user.toResponse()
    }
    
    /**
     * Updates user profile and roles.
     */
    fun updateUser(id: Long, request: UpdateUserRequest, updatedBy: String): UserResponse {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        
        // Create updated user (since User is immutable)
        val updatedUser = user.copy(
            email = request.email ?: user.email,
            roles = request.roles?.map { "ROLE_$it" }?.toSet() ?: user.roles
        )
        
        val savedUser = userRepository.save(updatedUser)
        
        // Log user update
        logUserAction(id, "USER_UPDATED", updatedBy)
        
        return savedUser.toResponse()
    }
    
    fun changePassword(id: Long, request: ChangePasswordRequest, changedBy: String) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        
        // Verify current password if user is changing their own password
        if (changedBy == user.username && !passwordEncoder.matches(request.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }
        
        // Create updated user with new password
        val updatedUser = user.copy(
            password = passwordEncoder.encode(request.newPassword)
        )
        
        userRepository.save(updatedUser)
        
        // Log password change
        logUserAction(id, "PASSWORD_CHANGED", changedBy)
    }
    
    fun deleteUser(id: Long) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        
        userRepository.delete(user)
        
        // Log user deletion
        logUserAction(id, "USER_DELETED", getCurrentUsername())
    }
    
    fun disableUser(id: Long) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        
        val updatedUser = user.copy(enabled = false)
        userRepository.save(updatedUser)
        
        // Log user disable
        logUserAction(id, "USER_DISABLED", getCurrentUsername())
    }
    
    fun enableUser(id: Long) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("User not found with id: $id")
        
        val updatedUser = user.copy(enabled = true)
        userRepository.save(updatedUser)
        
        // Log user enable
        logUserAction(id, "USER_ENABLED", getCurrentUsername())
    }
    
    fun isCurrentUser(userId: Long, username: String): Boolean {
        val user = userRepository.findByIdOrNull(userId)
        return user?.username == username
    }
    
    private fun getCurrentUsername(): String {
        return SecurityContextHolder.getContext().authentication.name
    }
    
    private fun logUserAction(userId: Long, action: String, performedBy: String) {
        log.info("User action: action={}, userId={}, performedBy={}", action, userId, performedBy)
    }
}

// Extension function for entity to DTO conversion
fun User.toResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        username = this.username,
        email = this.email,
        roles = this.roles.map { it.removePrefix("ROLE_") }.toSet(),
        enabled = this.enabled,
        createdAt = null, // Not available in current entity
        updatedAt = null  // Not available in current entity
    )
}
