package com.restaurant.menu.filter

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*

@Component
class CorrelationIdFilter : OncePerRequestFilter() {
    
    companion object {
        private const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        private const val CORRELATION_ID_MDC_KEY = "correlationId"
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = getOrGenerateCorrelationId(request)
        
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        response.addHeader(CORRELATION_ID_HEADER, correlationId)
        
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY)
        }
    }
    
    private fun getOrGenerateCorrelationId(request: HttpServletRequest): String {
        return request.getHeader(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()
    }
}
