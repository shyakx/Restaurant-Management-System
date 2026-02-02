package com.restaurant.menu.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT token provider for secure authentication and role-based authorization.
 */
@Component
class JwtTokenProvider {
    
    @Value("\${app.jwt.secret:mySecretKey}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwt.expiration:86400}")
    private val jwtExpirationInMs: Long = 86400 // 24 hours
    
    // HMAC-SHA512 key for secure token signing
    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    /**
     * Generates JWT token with user credentials and roles.
     */
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.name
        val roles = authentication.authorities.map { it.authority }
        
        return Jwts.builder()
            .setSubject(userPrincipal)
            .claim("roles", roles)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationInMs * 1000))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * Extracts username from valid JWT token.
     */
    fun getUsernameFromToken(token: String): String? {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }
    
    /**
     * Extracts user roles from JWT token.
     */
    fun getRolesFromToken(token: String): List<String> {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
            
            @Suppress("UNCHECKED_CAST")
            claims["roles"] as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Validates JWT token signature and expiration.
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
