package com.restaurant.menu.config

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.function.Supplier

/**
 * Rate limiting configuration for API protection.
 */
@Configuration
class RateLimitConfig {
    
    @Bean
    fun rateLimitBucket(): Bucket {
        return Bucket.builder()
            .addLimit(
                io.github.bucket4j.Bandwidth.classic(
                    100,
                    io.github.bucket4j.Refill.intervally(100, Duration.ofMinutes(1))
                )
            )
            .build()
    }
    
    @Bean
    fun strictRateLimitBucket(): Bucket {
        return Bucket.builder()
            .addLimit(
                io.github.bucket4j.Bandwidth.classic(
                    20,
                    io.github.bucket4j.Refill.intervally(20, Duration.ofMinutes(1))
                )
            )
            .build()
    }
}
