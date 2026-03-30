package com.restaurant.order.controller

import com.restaurant.order.dto.MenuItemResponse
import com.restaurant.order.service.MenuService
import com.restaurant.order.service.CreateMenuItemRequest
import com.restaurant.order.service.UpdateMenuItemRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Admin Menu Management API endpoints
 * Requires ADMIN role for all operations
 */
@RestController
@RequestMapping("/api/admin/menu")
@PreAuthorize("hasRole('ADMIN')")
class MenuAdminController(private val menuService: MenuService) {

    private val logger = LoggerFactory.getLogger(MenuAdminController::class.java)

    /**
     * Create a new menu item
     */
    @PostMapping("/items")
    fun createMenuItem(@Valid @RequestBody request: CreateMenuItemRequest): ResponseEntity<MenuItemResponse> {
        logger.info("Creating new menu item: ${request.name}")
        val menuItem = menuService.createMenuItem(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.toResponse(menuItem))
    }

    /**
     * Update an existing menu item
     */
    @PutMapping("/items/{id}")
    fun updateMenuItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMenuItemRequest
    ): ResponseEntity<MenuItemResponse> {
        logger.info("Updating menu item with ID: $id")
        val menuItem = menuService.updateMenuItem(id, request)
        return ResponseEntity.ok(menuService.toResponse(menuItem))
    }

    /**
     * Update menu item availability (enable/disable)
     */
    @PatchMapping("/items/{id}/availability")
    fun updateMenuItemAvailability(
        @PathVariable id: Long,
        @RequestParam available: Boolean
    ): ResponseEntity<MenuItemResponse> {
        logger.info("Updating availability for menu item $id to: $available")
        val menuItem = menuService.updateMenuItemAvailability(id, available)
        return ResponseEntity.ok(menuService.toResponse(menuItem))
    }

    /**
     * Delete a menu item (soft delete - sets unavailable)
     */
    @DeleteMapping("/items/{id}")
    fun deleteMenuItem(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info("Soft deleting menu item with ID: $id")
        menuService.deleteMenuItem(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Get all menu items (including unavailable ones) for admin
     */
    @GetMapping("/items")
    fun getAllMenuItemsForAdmin(): ResponseEntity<List<MenuItemResponse>> {
        val allItems = menuService.getAllMenuItemsForAdmin()
        return ResponseEntity.ok(allItems.map { menuService.toResponse(it) })
    }

    /**
     * Get menu item by ID (including unavailable ones)
     */
    @GetMapping("/items/{id}")
    fun getMenuItemByIdForAdmin(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        val menuItem = menuService.getMenuItemByIdForAdmin(id)
        return if (menuItem != null) {
            ResponseEntity.ok(menuService.toResponse(menuItem))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get menu statistics for admin dashboard
     */
    @GetMapping("/statistics")
    fun getMenuStatisticsForAdmin(): ResponseEntity<Map<String, Long>> {
        val statistics = menuService.getMenuStatistics()
        return ResponseEntity.ok(statistics)
    }

    /**
     * Bulk update menu item prices
     */
    @PutMapping("/items/bulk/price")
    fun bulkUpdatePrices(
        @RequestBody priceUpdates: Map<Long, BigDecimal>
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Bulk updating prices for ${priceUpdates.size} menu items")
        val updatedItems = mutableMapOf<Long, Boolean>()
        
        priceUpdates.forEach { (id, newPrice) ->
            try {
                menuService.updateMenuItem(id, UpdateMenuItemRequest(price = newPrice))
                updatedItems[id] = true
            } catch (e: Exception) {
                logger.error("Failed to update price for item $id", e)
                updatedItems[id] = false
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "total" to priceUpdates.size,
            "updated" to updatedItems.values.count { it },
            "failed" to updatedItems.values.count { !it },
            "details" to updatedItems
        ))
    }

    /**
     * Bulk update menu item availability
     */
    @PutMapping("/items/bulk/availability")
    fun bulkUpdateAvailability(
        @RequestBody availabilityUpdates: Map<Long, Boolean>
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Bulk updating availability for ${availabilityUpdates.size} menu items")
        val updatedItems = mutableMapOf<Long, Boolean>()
        
        availabilityUpdates.forEach { (id, available) ->
            try {
                menuService.updateMenuItemAvailability(id, available)
                updatedItems[id] = true
            } catch (e: Exception) {
                logger.error("Failed to update availability for item $id", e)
                updatedItems[id] = false
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "total" to availabilityUpdates.size,
            "updated" to updatedItems.values.count { it },
            "failed" to updatedItems.values.count { !it },
            "details" to updatedItems
        ))
    }
}
