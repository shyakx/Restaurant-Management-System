package com.restaurant.menu.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

/**
 * Business metrics for monitoring and observability.
 * Tracks key business operations and performance indicators.
 */
@Component
class BusinessMetrics(private val meterRegistry: MeterRegistry) {
    
    // Menu item operations counters
    private val menuItemCreatedCounter: Counter = Counter.builder("menu.items.created")
        .tag("service", "menu-service")
        .description("Number of menu items created")
        .register(meterRegistry)
    
    private val menuItemUpdatedCounter: Counter = Counter.builder("menu.items.updated")
        .tag("service", "menu-service")
        .description("Number of menu items updated")
        .register(meterRegistry)
    
    private val menuItemDeletedCounter: Counter = Counter.builder("menu.items.deleted")
        .tag("service", "menu-service")
        .description("Number of menu items deleted")
        .register(meterRegistry)
    
    private val menuItemViewedCounter: Counter = Counter.builder("menu.items.viewed")
        .tag("service", "menu-service")
        .description("Number of menu items viewed")
        .register(meterRegistry)
    
    // Cache performance metrics
    private val cacheHitCounter: Counter = Counter.builder("cache.hits")
        .tag("service", "menu-service")
        .tag("cache", "menu-items")
        .description("Number of cache hits")
        .register(meterRegistry)
    
    private val cacheMissCounter: Counter = Counter.builder("cache.misses")
        .tag("service", "menu-service")
        .tag("cache", "menu-items")
        .description("Number of cache misses")
        .register(meterRegistry)
    
    // Performance timers
    private val databaseQueryTimer: Timer = Timer.builder("database.query.duration")
        .tag("service", "menu-service")
        .tag("operation", "menu-item")
        .description("Database query duration")
        .register(meterRegistry)
    
    private val kafkaEventTimer: Timer = Timer.builder("kafka.event.duration")
        .tag("service", "menu-service")
        .description("Kafka event publishing duration")
        .register(meterRegistry)
    
    // Business operation methods
    fun incrementMenuItemCreated() = menuItemCreatedCounter.increment()
    fun incrementMenuItemUpdated() = menuItemUpdatedCounter.increment()
    fun incrementMenuItemDeleted() = menuItemDeletedCounter.increment()
    fun incrementMenuItemViewed() = menuItemViewedCounter.increment()
    
    // Cache performance methods
    fun incrementCacheHit() = cacheHitCounter.increment()
    fun incrementCacheMiss() = cacheMissCounter.increment()
    
    // Performance timing methods
    fun recordDatabaseQueryTime(duration: Long) = databaseQueryTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS)
    fun recordKafkaEventTime(duration: Long) = kafkaEventTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS)
    
    // Timer sample usage
    fun <T> timeDatabaseQuery(operation: () -> T): T {
        val sample = Timer.start(meterRegistry)
        return try {
            operation()
        } finally {
            sample.stop(databaseQueryTimer)
        }
    }
    
    fun <T> timeKafkaEvent(operation: () -> T): T {
        val sample = Timer.start(meterRegistry)
        return try {
            operation()
        } finally {
            sample.stop(kafkaEventTimer)
        }
    }
}
