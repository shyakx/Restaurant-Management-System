package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import jakarta.annotation.PostConstruct
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils

/**
 * Menu Controller - Provides menu items for order creation and validation.
 */
@RestController
@RequestMapping("/api/menu")
class MenuController(private val environment: Environment) {

    private val objectMapper = ObjectMapper()
    
    @Value("\${restaurant.menu.enabled:false}")
    private var menuEnabled: Boolean = false

    private var menuItems: List<MenuItemResponse> = emptyList()

    /**
     * Initialize menu items from external configuration.
     * Loads menu items from configuration files or falls back to empty list.
     */
    @PostConstruct
    private fun initializeMenuItems() {
        menuItems = loadMenuItemsFromConfiguration()
    }

    /**
     * Load menu items from external configuration sources.
     * Priority: Environment variables > Configuration files > Empty list
     */
    private fun loadMenuItemsFromConfiguration(): List<MenuItemResponse> {
        return try {
            when {
                menuEnabled -> loadFromPropertiesFile()
                else -> emptyList()
            }
        } catch (e: Exception) {
            // Log error and return empty list to prevent system failure
            emptyList()
        }
    }

    /**
     * Load menu items from properties configuration file.
     * Parses JSON configuration from menu.properties file.
     */
    private fun loadFromPropertiesFile(): List<MenuItemResponse> {
        return try {
            val resource = ClassPathResource("menu.properties")
            val properties = java.util.Properties()
            properties.load(resource.inputStream)
            
            val menuItemsJson = properties.getProperty("restaurant.menu.items")
            if (!menuItemsJson.isNullOrBlank()) {
                objectMapper.readValue(menuItemsJson, object : com.fasterxml.jackson.core.type.TypeReference<List<MenuItemData>>() {})
                    .map { it.toResponse() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Data class for parsing menu item configuration.
     */
    private data class MenuItemData(
        val id: Long,
        val name: String,
        val description: String,
        val price: Double,
        val category: String,
        val available: Boolean
    ) {
        fun toResponse(): MenuItemResponse {
            return MenuItemResponse(
                id = id,
                name = name,
                description = description,
                price = BigDecimal.valueOf(price),
                category = category,
                available = available
            )
        }
    }

    /**
     * Retrieve all available menu items.
     * Returns only items that are currently available for ordering.
     */
    @GetMapping("/items")
    fun getAllMenuItems(): ResponseEntity<List<MenuItemResponse>> {
        return ResponseEntity.ok(menuItems.filter { it.available })
    }

    /**
     * Retrieve a specific menu item by its identifier.
     * Returns 404 if the item is not found or unavailable.
     */
    @GetMapping("/items/{id}")
    fun getMenuItemById(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        val menuItem = menuItems.find { it.id == id && it.available }
        return if (menuItem != null) {
            ResponseEntity.ok(menuItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Retrieve menu items filtered by category.
     * Category comparison is case-insensitive for flexibility.
     */
    @GetMapping("/items/category/{category}")
    fun getMenuItemsByCategory(@PathVariable category: String): ResponseEntity<List<MenuItemResponse>> {
        val filteredItems = menuItems.filter { 
            it.available && it.category.equals(category, ignoreCase = true) 
        }
        return ResponseEntity.ok(filteredItems)
    }

    /**
     * Validate if a menu item exists and is available.
     * Used internally for order validation.
     */
    fun validateMenuItemExists(menuItemId: Long): Boolean {
        return menuItems.any { it.id == menuItemId && it.available }
    }

    /**
     * Get the price of a specific menu item.
     * Returns null if the item is not found or unavailable.
     */
    fun getMenuItemPrice(menuItemId: Long): BigDecimal? {
        return menuItems.find { it.id == menuItemId && it.available }?.price
    }
}
