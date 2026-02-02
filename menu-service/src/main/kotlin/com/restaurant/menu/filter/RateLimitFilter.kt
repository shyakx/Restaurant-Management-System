package com.restaurant.menu.filter

import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Rate limiting filter for API protection.
 */
@Component
class RateLimitFilter(
    private val rateLimitBucket: Bucket,
    private val strictRateLimitBucket: Bucket
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val bucket = if (isStrictOperation(request)) strictRateLimitBucket else rateLimitBucket
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write(
                """
                {
                    "timestamp": "${java.time.LocalDateTime.now()}",
                    "status": 429,
                    "code": "RATE_LIMIT_EXCEEDED",
                    "message": "Too many requests. Please try again later.",
                    "path": "${request.requestURI}"
                }
                """.trimIndent()
            )
        }
    }
    
    private fun isStrictOperation(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val method = request.method
        
        return (method in listOf("POST", "PUT", "DELETE") && path.contains("/menu/items")) ||
               path.contains("/auth/login")
    }
    
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/actuator") || 
               path.startsWith("/health") ||
               path.startsWith("/metrics")
    }
}
