package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
 * Menu API endpoints
 */
@RestController
@RequestMapping("/api/menu")
class MenuController {

    private val logger = LoggerFactory.getLogger(MenuController::class.java)
    private val objectMapper = ObjectMapper()
    
    @Value("\${restaurant.menu.enabled:false}")
    private var menuEnabled: Boolean = false

    private var menuItems: List<MenuItemResponse> = emptyList()

    @PostConstruct
    private fun initializeMenuItems() {
        menuItems = loadMenuItemsFromConfiguration()
        logger.info("Menu initialized with ${menuItems.size} items")
    }

    private fun loadMenuItemsFromConfiguration(): List<MenuItemResponse> {
        return when {
            menuEnabled -> loadFromPropertiesFile()
            else -> emptyList()
        }
    }

    private fun loadFromPropertiesFile(): List<MenuItemResponse> {
        val resource = ClassPathResource("menu.properties")
        
        if (!resource.exists()) {
            throw IllegalStateException("menu.properties file not found in classpath")
        }
        
        val properties = java.util.Properties()
        properties.load(resource.inputStream)
        
        val menuItemsJson = properties.getProperty("restaurant.menu.items")
        
        if (menuItemsJson.isNullOrBlank()) {
            throw IllegalStateException("restaurant.menu.items property is null or blank")
        }
        
        val items = objectMapper.readValue(menuItemsJson, object : com.fasterxml.jackson.core.type.TypeReference<List<MenuItemData>>() {})
            .map { it.toResponse() }
        return items
    }

    data class MenuItemData(
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

    @GetMapping("/items")
    fun getAllMenuItems(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<MenuItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val allItems = menuItems.filter { it.available }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, allItems.size)
        
        return if (startIndex >= allItems.size) {
            Page.empty(pageable)
        } else {
            val items = allItems.subList(startIndex, endIndex)
            org.springframework.data.domain.PageImpl(items, pageable, allItems.size.toLong())
        }
    }

    @GetMapping("/items/{id}")
    fun getMenuItemById(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        val menuItem = menuItems.find { it.id == id && it.available }
        return if (menuItem != null) {
            ResponseEntity.ok(menuItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/items/category/{category}")
    fun getMenuItemsByCategory(
        @PathVariable category: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<MenuItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val filteredItems = menuItems.filter { 
            it.available && it.category.equals(category, ignoreCase = true) 
        }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, filteredItems.size)
        
        return if (startIndex >= filteredItems.size) {
            Page.empty(pageable)
        } else {
            val items = filteredItems.subList(startIndex, endIndex)
            org.springframework.data.domain.PageImpl(items, pageable, filteredItems.size.toLong())
        }
    }

    fun validateMenuItemExists(menuItemId: Long): Boolean {
        return menuItems.any { it.id == menuItemId && it.available }
    }

    fun getMenuItemPrice(menuItemId: Long): BigDecimal? {
        return menuItems.find { it.id == menuItemId && it.available }?.price
    }
}
