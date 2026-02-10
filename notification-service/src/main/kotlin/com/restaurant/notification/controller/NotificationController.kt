package com.restaurant.notification.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(@Autowired private val mailSender: JavaMailSender) {

    @PostMapping("/test-email")
    fun sendTestEmail(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, String>> {
        val to = request["to"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Email address is required"))
        val subject = request["subject"] ?: "Test Email from Restaurant System"
        val message = request["message"] ?: "This is a test email from the Restaurant Management System."

        return try {
            val email = SimpleMailMessage()
            email.setTo(to)
            email.subject = subject
            email.text = message
            mailSender.send(email)
            
            ResponseEntity.ok(mapOf("status" to "success", "message" to "Email sent successfully"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("status" to "error", "message" to "Failed to send email: ${e.message}"))
        }
    }

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "healthy", "service" to "notification-service"))
    }
}
