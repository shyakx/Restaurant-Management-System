package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Menu Controller - Simple menu management for order service.
 * Provides basic menu items for order creation and testing.
 */
@RestController
@RequestMapping("/api/menu")
class MenuController {

    // Simple in-memory menu for demo purposes
    private val menuItems = listOf(
        MenuItemResponse(
            id = 1L,
            name = "Classic Burger",
            description = "Juicy beef patty with lettuce, tomato, and onion",
            price = BigDecimal("12.99"),
            category = "Main Course",
            available = true
        ),
        MenuItemResponse(
            id = 2L,
            name = "Caesar Salad",
            description = "Fresh romaine lettuce with parmesan and croutons",
            price = BigDecimal("8.99"),
            category = "Salads",
            available = true
        ),
        MenuItemResponse(
            id = 3L,
            name = "French Fries",
            description = "Crispy golden potato fries with sea salt",
            price = BigDecimal("4.99"),
            category = "Side Dishes",
            available = true
        ),
        MenuItemResponse(
            id = 4L,
            name = "Chocolate Milkshake",
            description = "Creamy vanilla ice cream blended with chocolate syrup",
            price = BigDecimal("6.99"),
            category = "Beverages",
            available = true
        ),
        MenuItemResponse(
            id = 5L,
            name = "Grilled Chicken Sandwich",
            description = "Tender grilled chicken breast with avocado and bacon",
            price = BigDecimal("14.99"),
            category = "Main Course",
            available = true
        )
    )

    // Get all menu items
    @GetMapping("/items")
    fun getAllMenuItems(): ResponseEntity<List<MenuItemResponse>> {
        return ResponseEntity.ok(menuItems.filter { it.available })
    }

    // Get menu item by ID
    @GetMapping("/items/{id}")
    fun getMenuItemById(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        val menuItem = menuItems.find { it.id == id && it.available }
        return if (menuItem != null) {
            ResponseEntity.ok(menuItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Get menu items by category
    @GetMapping("/items/category/{category}")
    fun getMenuItemsByCategory(@PathVariable category: String): ResponseEntity<List<MenuItemResponse>> {
        val filteredItems = menuItems.filter { 
            it.available && it.category.equals(category, ignoreCase = true) 
        }
        return ResponseEntity.ok(filteredItems)
    }
}
