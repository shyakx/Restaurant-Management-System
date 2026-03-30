package com.restaurant.order.service

import com.restaurant.order.dto.MenuItemResponse
import com.restaurant.order.entity.MenuItem
import com.restaurant.order.repository.MenuItemRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class MenuService(private val menuItemRepository: MenuItemRepository) {

    companion object {
        const val MENU_CACHE = "menu-items"
        const val MENU_STATS_CACHE = "menu-stats"
    }

    // Get all available menu items
    @Cacheable(value = [MENU_CACHE], key = "'all-available'")
    fun getAvailableMenuItems(): List<MenuItem> {
        return menuItemRepository.findByAvailableTrue()
    }

    // Get available items by category
    @Cacheable(value = [MENU_CACHE], key = "'category-' + #category")
    fun getAvailableItemsByCategory(category: String): List<MenuItem> {
        return menuItemRepository.findByCategoryAndAvailableTrue(category)
    }

    // Get menu item by ID (only if available)
    @Cacheable(value = [MENU_CACHE], key = "'item-' + #id")
    fun getMenuItemById(id: Long): MenuItem? {
        return menuItemRepository.findByIdAndAvailableTrue(id).orElse(null)
    }

    // Search menu items by name
    @Cacheable(value = [MENU_CACHE], key = "'search-' + #name")
    fun searchMenuItemsByName(name: String): List<MenuItem> {
        return menuItemRepository.findByNameContainingIgnoreCaseAndAvailableTrue(name)
    }

    // Get items within price range
    @Cacheable(value = [MENU_CACHE], key = "'price-range-' + #minPrice + '-' + #maxPrice")
    fun getItemsInPriceRange(minPrice: Double, maxPrice: Double): List<MenuItem> {
        return menuItemRepository.findAvailableItemsInPriceRange(minPrice, maxPrice)
    }

    // Get items by maximum preparation time
    @Cacheable(value = [MENU_CACHE], key = "'prep-time-' + #maxPrepTime")
    fun getItemsByMaxPreparationTime(maxPrepTime: Int): List<MenuItem> {
        return menuItemRepository.findAvailableItemsByMaxPreparationTime(maxPrepTime)
    }

    // Create new menu item
    @CacheEvict(value = [MENU_CACHE, MENU_STATS_CACHE], allEntries = true)
    fun createMenuItem(request: CreateMenuItemRequest): MenuItem {
        val menuItem = MenuItem(
            name = request.name,
            description = request.description,
            price = request.price,
            category = request.category,
            available = request.available,
            imageUrl = request.imageUrl,
            preparationTime = request.preparationTime,
            ingredients = request.ingredients
        )
        return menuItemRepository.save(menuItem)
    }

    // Update menu item
    @CacheEvict(value = [MENU_CACHE, MENU_STATS_CACHE], allEntries = true)
    fun updateMenuItem(id: Long, request: UpdateMenuItemRequest): MenuItem {
        val existingItem = menuItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Menu item not found with id: $id") }

        val updatedItem = existingItem.copy(
            name = request.name ?: existingItem.name,
            description = request.description ?: existingItem.description,
            price = request.price ?: existingItem.price,
            category = request.category ?: existingItem.category,
            available = request.available ?: existingItem.available,
            imageUrl = request.imageUrl ?: existingItem.imageUrl,
            preparationTime = request.preparationTime ?: existingItem.preparationTime,
            ingredients = request.ingredients ?: existingItem.ingredients,
            updatedAt = LocalDateTime.now()
        )

        return menuItemRepository.save(updatedItem)
    }

    // Update menu item availability
    @CacheEvict(value = [MENU_CACHE, MENU_STATS_CACHE], allEntries = true)
    fun updateMenuItemAvailability(id: Long, available: Boolean): MenuItem {
        val menuItem = menuItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Menu item not found with id: $id") }

        val updatedItem = menuItem.copy(
            available = available,
            updatedAt = LocalDateTime.now()
        )

        return menuItemRepository.save(updatedItem)
    }

    // Delete menu item (soft delete by setting unavailable)
    @CacheEvict(value = [MENU_CACHE, MENU_STATS_CACHE], allEntries = true)
    fun deleteMenuItem(id: Long) {
        val menuItem = menuItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Menu item not found with id: $id") }

        val deletedItem = menuItem.copy(
            available = false,
            updatedAt = LocalDateTime.now()
        )

        menuItemRepository.save(deletedItem)
    }

    // Validation methods for OrderService
    fun validateMenuItemExists(menuItemId: Long): Boolean {
        return menuItemRepository.findByIdAndAvailableTrue(menuItemId).isPresent
    }

    fun getMenuItemPrice(menuItemId: Long): BigDecimal? {
        return menuItemRepository.findByIdAndAvailableTrue(menuItemId)
            .map { it.price }
            .orElse(null)
    }

    fun getMenuItemName(menuItemId: Long): String? {
        return menuItemRepository.findByIdAndAvailableTrue(menuItemId)
            .map { it.name }
            .orElse(null)
    }

    // Get menu statistics
    @Cacheable(value = [MENU_STATS_CACHE], key = "'statistics'")
    fun getMenuStatistics(): Map<String, Long> {
        val allCategories = menuItemRepository.findAll().map { it.category }.distinct()
        return allCategories.associate { category ->
            category to menuItemRepository.countByCategoryAndAvailableTrue(category)
        } + ("total_available" to menuItemRepository.findByAvailableTrue().size.toLong())
    }

    // Get all menu items (including unavailable ones) for admin
    fun getAllMenuItemsForAdmin(): List<MenuItem> {
        return menuItemRepository.findAll()
    }

    // Get menu item by ID (including unavailable ones) for admin
    fun getMenuItemByIdForAdmin(id: Long): MenuItem? {
        return menuItemRepository.findById(id).orElse(null)
    }

    // Convert MenuItem to MenuItemResponse
    fun toResponse(menuItem: MenuItem): MenuItemResponse {
        return MenuItemResponse(
            id = menuItem.id!!,
            name = menuItem.name,
            description = menuItem.description,
            price = menuItem.price,
            category = menuItem.category,
            available = menuItem.available
        )
    }
}

// Request DTOs for menu management
data class CreateMenuItemRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val category: String,
    val available: Boolean = true,
    val imageUrl: String? = null,
    val preparationTime: Int = 15,
    val ingredients: String? = null
)

data class UpdateMenuItemRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val category: String? = null,
    val available: Boolean? = null,
    val imageUrl: String? = null,
    val preparationTime: Int? = null,
    val ingredients: String? = null
)
