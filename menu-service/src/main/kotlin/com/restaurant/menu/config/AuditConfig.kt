package com.restaurant.menu.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * JPA Auditing configuration for audit trail functionality.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class AuditConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAwareImpl()
    }
}

/**
 * Provides current user information for JPA auditing.
 */
class AuditorAwareImpl : AuditorAware<String> {
    
    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        
        return if (authentication != null && authentication.isAuthenticated) {
            Optional.of(authentication.name)
        } else {
            Optional.of("system")
        }
    }
}
