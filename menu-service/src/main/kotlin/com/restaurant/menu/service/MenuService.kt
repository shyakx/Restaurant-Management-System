package com.restaurant.menu.service

import com.restaurant.menu.entity.MenuItem
import com.restaurant.menu.repository.MenuItemRepository
import com.restaurant.menu.exception.ResourceNotFoundException
import com.restaurant.menu.event.MenuEventPublisher
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Menu service for managing restaurant menu items and categories.
 */
@Service
class MenuService(
    private val menuItemRepository: MenuItemRepository,
    private val menuEventPublisher: MenuEventPublisher
) {
    
    fun getAllMenuItems(): List<MenuItem> {
        return menuItemRepository.findAll()
    }
    
    fun getMenuItemsByAvailability(available: Boolean): List<MenuItem> {
        return menuItemRepository.findAll().filter { it.available == available }
    }
    
    fun getMenuItemsByCategory(category: String, available: Boolean): List<MenuItem> {
        return menuItemRepository.findAll()
            .filter { it.category == category && it.available == available }
    }
    
    fun getAvailableItemsOrderByPrice(): List<MenuItem> {
        return menuItemRepository.findAll()
            .filter { it.available }
            .sortedBy { it.price }
    }
    
    fun getMenuItemById(id: Long): MenuItem {
        return menuItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Menu item not found with id: $id") }
    }
    
    fun createMenuItem(menuItem: MenuItem): MenuItem {
        if (menuItem.name.isBlank()) {
            throw IllegalArgumentException("Menu item name cannot be blank")
        }
        if (menuItem.price <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Menu item price must be positive")
        }
        
        val newItem = menuItem.copy(id = null)
        val savedItem = menuItemRepository.save(newItem)
        menuEventPublisher.publishMenuItemCreated(savedItem)
        
        return savedItem
    }
    
    fun updateMenuItem(id: Long, menuItem: MenuItem): MenuItem {
        val existingItem = menuItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Menu item not found with id: $id") }
        
        if (menuItem.name.isBlank()) {
            throw IllegalArgumentException("Menu item name cannot be blank")
        }
        if (menuItem.price <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Menu item price must be positive")
        }
        
        val updatedItem = existingItem.copy(
            name = menuItem.name,
            description = menuItem.description,
            price = menuItem.price,
            category = menuItem.category,
            available = menuItem.available
        )
        val savedItem = menuItemRepository.save(updatedItem)
        menuEventPublisher.publishMenuItemUpdated(savedItem)
        
        return savedItem
    }
    
    fun deleteMenuItem(id: Long) {
        val menuItem = menuItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Menu item not found with id: $id") }
        
        menuItemRepository.deleteById(id)
        menuEventPublisher.publishMenuItemDeleted(menuItem)
    }
    
    fun getCategories(): List<String> {
        return menuItemRepository.findAll()
            .mapNotNull { it.category }
            .distinct()
    }
    
    fun getItemsCountByCategory(): Map<String, Long> {
        return menuItemRepository.findAll()
            .filter { it.available }
            .groupBy { it.category ?: "Uncategorized" }
            .mapValues { it.value.size.toLong() }
    }
}
