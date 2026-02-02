package com.restaurant.menu.config

import com.restaurant.menu.entity.User
import com.restaurant.menu.entity.MenuItem
import com.restaurant.menu.repository.UserRepository
import com.restaurant.menu.repository.MenuItemRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Initializes default data on application startup.
 */
@Configuration
class DataInitializer(
    private val userRepository: UserRepository,
    private val menuItemRepository: MenuItemRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    @Bean
    @Transactional
    fun initializeData(): CommandLineRunner {
        return CommandLineRunner {
            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                try {
                    val adminUser = User(
                        username = "admin",
                        password = passwordEncoder.encode("admin123"),
                        email = "admin@restaurant.local",
                        roles = setOf("ROLE_ADMIN", "ROLE_USER"),
                        enabled = true
                    )
                    
                    userRepository.save(adminUser)
                    println("✅ Admin user created successfully")
                } catch (e: Exception) {
                    println("⚠️ Could not create admin user: ${e.message}")
                }
            } else {
                println("✅ Admin user already exists")
            }
            
            // Create regular user if not exists
            if (!userRepository.existsByUsername("user")) {
                try {
                    val regularUser = User(
                        username = "user",
                        password = passwordEncoder.encode("user123"),
                        email = "user@restaurant.local",
                        roles = setOf("ROLE_USER"),
                        enabled = true
                    )
                    
                    userRepository.save(regularUser)
                    println("✅ Regular user created successfully")
                } catch (e: Exception) {
                    println("⚠️ Could not create regular user: ${e.message}")
                }
            } else {
                println("✅ Regular user already exists")
            }
            
            // Create sample menu items if table is empty
            if (menuItemRepository.count() == 0L) {
                try {
                    val sampleMenuItems = listOf(
                        MenuItem(
                            name = "Margherita Pizza",
                            description = "Classic pizza with tomato sauce, mozzarella, and fresh basil",
                            price = BigDecimal("12.99"),
                            category = "Pizza",
                            available = true
                        ),
                        MenuItem(
                            name = "Caesar Salad",
                            description = "Romaine lettuce with Caesar dressing, croutons, and parmesan",
                            price = BigDecimal("8.99"),
                            category = "Salad",
                            available = true
                        ),
                        MenuItem(
                            name = "Grilled Chicken Sandwich",
                            description = "Grilled chicken breast with lettuce, tomato, and mayo on a bun",
                            price = BigDecimal("10.99"),
                            category = "Sandwich",
                            available = true
                        ),
                        MenuItem(
                            name = "Beef Burger",
                            description = "Classic beef patty with lettuce, tomato, onion, and pickles",
                            price = BigDecimal("13.99"),
                            category = "Burger",
                            available = true
                        ),
                        MenuItem(
                            name = "Fish and Chips",
                            description = "Beer-battered cod with golden fries",
                            price = BigDecimal("15.99"),
                            category = "Seafood",
                            available = true
                        )
                    )
                    
                    menuItemRepository.saveAll(sampleMenuItems)
                    println("✅ Sample menu items created successfully")
                } catch (e: Exception) {
                    println("⚠️ Could not create sample menu items: ${e.message}")
                }
            } else {
                println("✅ Menu items already exist")
            }
        }
    }
}
