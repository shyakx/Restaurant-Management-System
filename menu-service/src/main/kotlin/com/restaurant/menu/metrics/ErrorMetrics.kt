package com.restaurant.menu.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ErrorMetrics(meterRegistry: MeterRegistry) {
    
    private val resourceNotFoundCounter: Counter = Counter.builder("exceptions")
        .tag("type", "ResourceNotFoundException")
        .tag("service", "menu-service")
        .register(meterRegistry)
    
    private val illegalArgumentCounter: Counter = Counter.builder("exceptions")
        .tag("type", "IllegalArgumentException")
        .tag("service", "menu-service")
        .register(meterRegistry)
    
    private val validationFailedCounter: Counter = Counter.builder("exceptions")
        .tag("type", "ValidationException")
        .tag("service", "menu-service")
        .register(meterRegistry)
    
    private val internalServerErrorCounter: Counter = Counter.builder("exceptions")
        .tag("type", "InternalServerError")
        .tag("service", "menu-service")
        .register(meterRegistry)
    
    private val runtimeErrorCounter: Counter = Counter.builder("exceptions")
        .tag("type", "RuntimeException")
        .tag("service", "menu-service")
        .register(meterRegistry)
    
    fun incrementResourceNotFound() = resourceNotFoundCounter.increment()
    fun incrementIllegalArgumentException() = illegalArgumentCounter.increment()
    fun incrementValidationFailed() = validationFailedCounter.increment()
    fun incrementInternalServerError() = internalServerErrorCounter.increment()
    fun incrementRuntimeError() = runtimeErrorCounter.increment()
}
