package com.restaurant.menu.event

import com.restaurant.menu.entity.MenuItem
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Publishes menu events to Kafka for other microservices.
 */
@Component
class MenuEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, MenuEvent>
) {
    
    private val log = LoggerFactory.getLogger(MenuEventPublisher::class.java)
    
    fun publishMenuItemCreated(menuItem: MenuItem) {
        val event = MenuEvent(
            eventType = "MENU_ITEM_CREATED",
            menuItemId = menuItem.id,
            menuItemName = menuItem.name,
            category = menuItem.category,
            price = menuItem.price.toDouble(),
            available = menuItem.available
        )
        kafkaTemplate.send("menu-events", event)
        log.info("Published menu item created event: ${menuItem.name}")
    }
    
    fun publishMenuItemUpdated(menuItem: MenuItem) {
        val event = MenuEvent(
            eventType = "MENU_ITEM_UPDATED",
            menuItemId = menuItem.id,
            menuItemName = menuItem.name,
            category = menuItem.category,
            price = menuItem.price.toDouble(),
            available = menuItem.available
        )
        kafkaTemplate.send("menu-events", event)
        log.info("Published menu item updated event: ${menuItem.name}")
    }
    
    fun publishMenuItemDeleted(menuItem: MenuItem) {
        val event = MenuEvent(
            eventType = "MENU_ITEM_DELETED",
            menuItemId = menuItem.id,
            menuItemName = menuItem.name,
            category = menuItem.category,
            price = menuItem.price.toDouble(),
            available = menuItem.available
        )
        kafkaTemplate.send("menu-events", event)
        log.info("Published menu item deleted event: ${menuItem.name}")
    }
}
