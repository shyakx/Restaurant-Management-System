package com.restaurant.kitchen.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.core.annotation.Order

@Component
@Order(1)
class RequestLoggingFilter : Filter {
    
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val method = httpRequest.method
        val uri = httpRequest.requestURI
        val queryString = httpRequest.queryString
        
        println("[$method] $uri${if (queryString != null && queryString.isNotEmpty()) "?$queryString" else ""}")
        
        chain.doFilter(request, response)
    }
}
