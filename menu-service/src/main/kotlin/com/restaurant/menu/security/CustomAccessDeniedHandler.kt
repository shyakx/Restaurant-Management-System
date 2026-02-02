package com.restaurant.menu.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * Handles 403 Forbidden responses for authenticated users without required permissions.
 */
@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_FORBIDDEN
        
        val body = mapOf(
            "status" to HttpServletResponse.SC_FORBIDDEN,
            "code" to "FORBIDDEN",
            "message" to "You do not have permission to access this resource",
            "path" to request.requestURI,
            "timestamp" to System.currentTimeMillis()
        )
        
        val objectMapper = ObjectMapper()
        objectMapper.writeValue(response.outputStream, body)
    }
}
