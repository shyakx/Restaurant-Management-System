package com.restaurant.menu.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.restaurant.menu.dto.CreateMenuItemRequest
import com.restaurant.menu.dto.UpdateMenuItemRequest
import com.restaurant.menu.dto.MenuItemResponse
import com.restaurant.menu.entity.MenuItem
import com.restaurant.menu.entity.toResponse
import com.restaurant.menu.service.MenuService
import com.restaurant.menu.exception.ResourceNotFoundException

@RestController
@RequestMapping("/api/menu")
class MenuController(private val menuService: MenuService) {
    
    @GetMapping("/items")
    fun getAllMenuItems(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false, defaultValue = "true") available: Boolean
    ): ResponseEntity<List<MenuItemResponse>> {
        val items = when {
            category != null -> menuService.getMenuItemsByCategory(category, available)
            else -> menuService.getMenuItemsByAvailability(available)
        }
        return ResponseEntity.ok(items.map { it.toResponse() })
    }
    
    @GetMapping("/items/all")
    fun getAllMenuItemsRaw(): ResponseEntity<List<MenuItemResponse>> {
        return ResponseEntity.ok(menuService.getAllMenuItems().map { it.toResponse() })
    }
    
    @GetMapping("/items/available/price-sorted")
    fun getAvailableItemsSortedByPrice(): ResponseEntity<List<MenuItemResponse>> {
        return ResponseEntity.ok(menuService.getAvailableItemsOrderByPrice().map { it.toResponse() })
    }
    
    @GetMapping("/items/{id}")
    fun getMenuItemById(@PathVariable id: Long): ResponseEntity<MenuItemResponse> {
        return try {
            val menuItem = menuService.getMenuItemById(id)
            ResponseEntity.ok(menuItem.toResponse())
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/items")
    fun createMenuItem(@Valid @RequestBody request: CreateMenuItemRequest): ResponseEntity<MenuItemResponse> {
        return try {
            val menuItem = MenuItem(
                name = request.name,
                description = request.description,
                price = request.price,
                category = request.category,
                available = request.available
            )
            val createdItem = menuService.createMenuItem(menuItem)
            ResponseEntity.status(HttpStatus.CREATED).body(createdItem.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PutMapping("/items/{id}")
    fun updateMenuItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMenuItemRequest
    ): ResponseEntity<MenuItemResponse> {
        return try {
            val menuItem = MenuItem(
                name = request.name,
                description = request.description,
                price = request.price,
                category = request.category,
                available = request.available
            )
            val updatedItem = menuService.updateMenuItem(id, menuItem)
            ResponseEntity.ok(updatedItem.toResponse())
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @DeleteMapping("/items/{id}")
    fun deleteMenuItem(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            menuService.deleteMenuItem(id)
            ResponseEntity.noContent().build()
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<String>> {
        val categories = menuService.getCategories()
        return ResponseEntity.ok(categories)
    }
    
    @GetMapping("/analytics/category-counts")
    fun getCategoryCounts(): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(menuService.getItemsCountByCategory())
    }
}
