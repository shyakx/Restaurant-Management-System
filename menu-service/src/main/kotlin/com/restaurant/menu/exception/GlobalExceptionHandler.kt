package com.restaurant.menu.exception

import com.restaurant.menu.metrics.ErrorMetrics
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime
import java.util.*

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val code: String,
    val message: String,
    val path: String,
    val correlationId: String = UUID.randomUUID().toString()
)

@RestControllerAdvice
class GlobalExceptionHandler(
    private val errorMetrics: ErrorMetrics
) {
    
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrCreateCorrelationId()
        
        log.warn("Resource not found [correlationId=$correlationId]: ${ex.message}", ex)
        errorMetrics.incrementResourceNotFound()
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            code = "RESOURCE_NOT_FOUND",
            message = if (ex.message.isNullOrBlank()) "Resource not found" else ex.message!!,
            path = request.getDescription(false).replace("uri=", ""),
            correlationId = correlationId
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrCreateCorrelationId()
        
        log.warn("Invalid argument [correlationId=$correlationId]: ${ex.message}", ex)
        errorMetrics.incrementIllegalArgumentException()
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            code = "INVALID_ARGUMENT",
            message = if (ex.message.isNullOrBlank()) "Invalid argument provided" else ex.message!!,
            path = request.getDescription(false).replace("uri=", ""),
            correlationId = correlationId
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrCreateCorrelationId()
        val errorMessage = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        
        log.warn("Validation failed [correlationId=$correlationId]: $errorMessage", ex)
        errorMetrics.incrementValidationFailed()
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            code = "VALIDATION_FAILED",
            message = "Validation failed: $errorMessage",
            path = request.getDescription(false).replace("uri=", ""),
            correlationId = correlationId
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrCreateCorrelationId()
        
        log.error("Unexpected error [correlationId=$correlationId]: ${if (ex.message.isNullOrBlank()) "Unknown error" else ex.message}", ex)
        errorMetrics.incrementInternalServerError()
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred: ${if (ex.message.isNullOrBlank()) "Unknown error" else ex.message}",
            path = request.getDescription(false).replace("uri=", ""),
            correlationId = correlationId
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrCreateCorrelationId()
        
        log.error("Runtime error [correlationId=$correlationId]: ${if (ex.message.isNullOrBlank()) "Unknown runtime error" else ex.message}", ex)
        errorMetrics.incrementRuntimeError()
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "RUNTIME_ERROR",
            message = "Runtime error: ${if (ex.message.isNullOrBlank()) "Unknown runtime error" else ex.message}",
            path = request.getDescription(false).replace("uri=", ""),
            correlationId = correlationId
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    private fun getOrCreateCorrelationId(): String {
        return MDC.get("correlationId") ?: UUID.randomUUID().toString()
    }
}
