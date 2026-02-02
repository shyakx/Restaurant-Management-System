package com.restaurant.menu.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import com.restaurant.menu.security.JwtTokenProvider
import com.restaurant.menu.dto.LoginRequest
import com.restaurant.menu.dto.LoginResponse
import com.restaurant.menu.dto.CreateUserRequest
import com.restaurant.menu.dto.UserResponse
import com.restaurant.menu.service.UserService

/**
 * Authentication controller for JWT login and user registration.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService
) {
    
    /**
     * Authenticates user and returns JWT token.
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )
        
        SecurityContextHolder.getContext().authentication = authentication
        
        val token = jwtTokenProvider.generateToken(authentication)
        val roles = authentication.authorities.map { it.authority }
        
        return ResponseEntity.ok(
            LoginResponse(
                token = token,
                type = "Bearer",
                username = authentication.name,
                roles = roles
            )
        )
    }
    
    /**
     * Registers new user with default USER role.
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody createUserRequest: CreateUserRequest): ResponseEntity<UserResponse> {
        // For self-registration, only allow USER role
        val registrationRequest = createUserRequest.copy(roles = setOf("USER"))
        val user = userService.createUser(registrationRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
    
    /**
     * Validates JWT token and returns user information.
     */
    @PostMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        val token = authorization.substring(7) // Remove "Bearer " prefix
        
        return if (jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsernameFromToken(token) ?: ""
            val roles = jwtTokenProvider.getRolesFromToken(token)
            
            ResponseEntity.ok(mapOf(
                "valid" to true,
                "username" to username,
                "roles" to roles
            ))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                "valid" to false,
                "message" to "Invalid token"
            ))
        }
    }
}
