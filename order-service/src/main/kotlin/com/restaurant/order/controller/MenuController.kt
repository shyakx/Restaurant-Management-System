package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import jakarta.annotation.PostConstruct
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils

/**
 * Menu Controller - Provides menu items for order creation and validation.
 */
@RestController
@RequestMapping("/api/menu")
class MenuController(private val environment: Environment) {

    private val logger = LoggerFactory.getLogger(MenuController::class.java)
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
        logger.info("Initializing menu items. Menu enabled: $menuEnabled")
        menuItems = loadMenuItemsFromConfiguration()
        logger.info("Menu items loaded successfully. Total items: ${menuItems.size}")
        menuItems.forEach { item ->
            logger.info("Loaded menu item: ID=${item.id}, Name=${item.name}, Available=${item.available}")
        }
    }

    /**
     * Load menu items from external configuration sources.
     * Priority: Environment variables > Configuration files > Empty list
     */
    private fun loadMenuItemsFromConfiguration(): List<MenuItemResponse> {
        return try {
            logger.info("Loading menu items from configuration. Menu enabled: $menuEnabled")
            when {
                menuEnabled -> {
                    logger.info("Menu is enabled, loading from properties file")
                    loadFromPropertiesFile()
                }
                else -> {
                    logger.warn("Menu is disabled, returning empty list")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.error("Error loading menu items from configuration", e)
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
            logger.info("Loading menu items from properties file")
            val resource = ClassPathResource("menu.properties")
            logger.info("Menu properties resource exists: ${resource.exists()}")
            
            if (!resource.exists()) {
                logger.error("menu.properties file not found in classpath")
                return emptyList()
            }
            
            val properties = java.util.Properties()
            properties.load(resource.inputStream)
            
            val menuItemsJson = properties.getProperty("restaurant.menu.items")
            logger.info("Menu items JSON property: $menuItemsJson")
            
            if (!menuItemsJson.isNullOrBlank()) {
                val items = objectMapper.readValue(menuItemsJson, object : com.fasterxml.jackson.core.type.TypeReference<List<MenuItemData>>() {})
                    .map { it.toResponse() }
                logger.info("Successfully parsed ${items.size} menu items from JSON")
                return items
            } else {
                logger.warn("restaurant.menu.items property is null or blank")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error loading menu items from properties file", e)
            emptyList()
        }
    }

    /**
     * Data class for parsing menu item configuration.
     */
    private data class MenuItemData(
        @JsonProperty("id") val id: Long,
        @JsonProperty("name") val name: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("price") val price: Double,
        @JsonProperty("category") val category: String,
        @JsonProperty("available") val available: Boolean
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
