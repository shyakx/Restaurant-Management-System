package com.restaurant.menu.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * User entity with JWT authentication support and role-based access control.
 */
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    
    @Column(unique = true, nullable = false)
    var username: String = ""
    
    @Column(nullable = false)
    var password: String = ""
    
    @Column(nullable = false)
    var email: String = ""
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    var roles: Set<String> = setOf("ROLE_USER")
    
    @Column(nullable = false)
    var enabled: Boolean = true
    
    // Default constructor for JPA
    constructor()
    
    // Constructor for user creation
    constructor(username: String, password: String, email: String, roles: Set<String> = setOf("ROLE_USER"), enabled: Boolean = true) {
        this.username = username
        this.password = password
        this.email = email
        this.roles = roles
        this.enabled = enabled
    }
    
    // Copy function for immutable updates
    fun copy(
        id: Long? = this.id,
        username: String = this.username,
        password: String = this.password,
        email: String = this.email,
        roles: Set<String> = this.roles,
        enabled: Boolean = this.enabled
    ): User {
        val newUser = User(username, password, email, roles, enabled)
        newUser.id = id
        return newUser
    }
    
    // Entity comparison methods
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "User(id=$id, username=$username, email=$email, enabled=$enabled)"
    }
}
