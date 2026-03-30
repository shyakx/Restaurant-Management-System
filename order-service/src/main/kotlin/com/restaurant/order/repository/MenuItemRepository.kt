package com.restaurant.order.repository

import com.restaurant.order.entity.MenuItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MenuItemRepository : JpaRepository<MenuItem, Long> {
    
    // Find all available menu items
    fun findByAvailableTrue(): List<MenuItem>
    
    // Find available items by category
    fun findByCategoryAndAvailableTrue(category: String): List<MenuItem>
    
    // Find menu item by ID and availability
    fun findByIdAndAvailableTrue(id: Long): Optional<MenuItem>
    
    // Find items by name (case-insensitive)
    fun findByNameContainingIgnoreCaseAndAvailableTrue(name: String): List<MenuItem>
    
    // Count available items by category
    fun countByCategoryAndAvailableTrue(category: String): Long
    
    // Custom query to find items within price range
    @Query("SELECT mi FROM MenuItem mi WHERE mi.available = true AND mi.price BETWEEN :minPrice AND :maxPrice")
    fun findAvailableItemsInPriceRange(
        @Param("minPrice") minPrice: Double, 
        @Param("maxPrice") maxPrice: Double
    ): List<MenuItem>
    
    // Find items by preparation time (max minutes)
    @Query("SELECT mi FROM MenuItem mi WHERE mi.available = true AND mi.preparationTime <= :maxPrepTime")
    fun findAvailableItemsByMaxPreparationTime(@Param("maxPrepTime") maxPrepTime: Int): List<MenuItem>
}
