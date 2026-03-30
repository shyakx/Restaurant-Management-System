package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import com.restaurant.order.service.MenuService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import java.math.BigDecimal

/**
 * Menu API endpoints
 */
@RestController
@RequestMapping("/api/menu")
class MenuController(private val menuService: MenuService) {

    private val logger = LoggerFactory.getLogger(MenuController::class.java)

    @GetMapping("/items")
    fun getAllMenuItems(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<MenuItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val allItems = menuService.getAvailableMenuItems().map { menuService.toResponse(it) }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, allItems.size)
        
        return if (startIndex >= allItems.size) {
            Page.empty(pageable)
        } else {
            val items = allItems.subList(startIndex, endIndex)
            PageImpl(items, pageable, allItems.size.toLong())
        }
    }

    @GetMapping("/items/{id}")
    fun getMenuItemById(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        val menuItem = menuService.getMenuItemById(id)
        return if (menuItem != null) {
            ResponseEntity.ok(menuService.toResponse(menuItem))
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
        val filteredItems = menuService.getAvailableItemsByCategory(category)
            .map { menuService.toResponse(it) }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, filteredItems.size)
        
        return if (startIndex >= filteredItems.size) {
            Page.empty(pageable)
        } else {
            val items = filteredItems.subList(startIndex, endIndex)
            PageImpl(items, pageable, filteredItems.size.toLong())
        }
    }

    @GetMapping("/items/search")
    fun searchMenuItems(
        @RequestParam name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<MenuItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val searchResults = menuService.searchMenuItemsByName(name)
            .map { menuService.toResponse(it) }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, searchResults.size)
        
        return if (startIndex >= searchResults.size) {
            Page.empty(pageable)
        } else {
            val items = searchResults.subList(startIndex, endIndex)
            PageImpl(items, pageable, searchResults.size.toLong())
        }
    }

    @GetMapping("/items/price-range")
    fun getMenuItemsInPriceRange(
        @RequestParam minPrice: Double,
        @RequestParam maxPrice: Double,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<MenuItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val itemsInRange = menuService.getItemsInPriceRange(minPrice, maxPrice)
            .map { menuService.toResponse(it) }
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, itemsInRange.size)
        
        return if (startIndex >= itemsInRange.size) {
            Page.empty(pageable)
        } else {
            val items = itemsInRange.subList(startIndex, endIndex)
            PageImpl(items, pageable, itemsInRange.size.toLong())
        }
    }

    @GetMapping("/statistics")
    fun getMenuStatistics(): ResponseEntity<Map<String, Long>> {
        val statistics = menuService.getMenuStatistics()
        return ResponseEntity.ok(statistics)
    }

    // Validation methods for OrderService
    fun validateMenuItemExists(menuItemId: Long): Boolean {
        return menuService.validateMenuItemExists(menuItemId)
    }

    fun getMenuItemPrice(menuItemId: Long): BigDecimal? {
        return menuService.getMenuItemPrice(menuItemId)
    }

    fun getMenuItemName(menuItemId: Long): String? {
        return menuService.getMenuItemName(menuItemId)
    }
}
