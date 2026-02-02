package com.restaurant.menu.repository

import com.restaurant.menu.entity.MenuItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuItemRepository : JpaRepository<MenuItem, Long> {
}
