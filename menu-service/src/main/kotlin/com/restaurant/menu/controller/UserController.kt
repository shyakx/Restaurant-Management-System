package com.restaurant.menu.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import com.restaurant.menu.dto.CreateUserRequest
import com.restaurant.menu.dto.UserResponse
import com.restaurant.menu.dto.UpdateUserRequest
import com.restaurant.menu.dto.ChangePasswordRequest
import com.restaurant.menu.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/profile")
    fun getCurrentUser(): ResponseEntity<UserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return ResponseEntity.status(401).build()
        }
        
        val username = authentication.name
        val user = userService.getUserByUsername(username)
        return ResponseEntity.ok(user)
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, authentication.name)")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, authentication.name)")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal currentUser: UserDetails
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(id, request, currentUser.username)
        return ResponseEntity.ok(user)
    }
    
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, authentication.name)")
    fun changePassword(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChangePasswordRequest,
        @AuthenticationPrincipal currentUser: UserDetails
    ): ResponseEntity<Void> {
        userService.changePassword(id, request, currentUser.username)
        return ResponseEntity.ok().build()
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    fun disableUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.disableUser(id)
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    fun enableUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.enableUser(id)
        return ResponseEntity.ok().build()
    }
}
