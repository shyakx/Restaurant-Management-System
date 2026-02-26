package com.restaurant.notification.service

import com.restaurant.notification.dto.EventType
import com.restaurant.notification.dto.OrderEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Handles email notifications for order events.
 */
@Service
@Transactional
class NotificationService(private val mailSender: JavaMailSender) {

    // Process order events from Kafka
    @KafkaListener(topics = ["order-events"], groupId = "notification-service-group")
    fun handleOrderEvent(orderEvent: OrderEvent) {
        println("Received order event: ${orderEvent.eventType} for order #${orderEvent.orderId}")
        println("Customer: ${orderEvent.customerName} (${orderEvent.customerEmail})")
        println("Order Total: $${orderEvent.totalAmount}")
        
        try {
            when (orderEvent.eventType) {
                EventType.ORDER_PLACED -> sendOrderConfirmation(orderEvent)
                EventType.ORDER_CONFIRMED -> sendOrderConfirmedNotification(orderEvent)
                EventType.ORDER_PREPARING -> sendOrderPreparingNotification(orderEvent)
                EventType.ORDER_READY -> sendOrderReadyNotification(orderEvent)
                EventType.ORDER_COMPLETED -> sendOrderCompletedNotification(orderEvent)
                EventType.ORDER_CANCELLED -> sendOrderCancelledNotification(orderEvent)
                else -> println("Unhandled event type: ${orderEvent.eventType}")
            }
        } catch (e: Exception) {
            println("Error processing notification: ${e.message}")
        }
    }

    // Send order confirmation email
    private fun sendOrderConfirmation(orderEvent: OrderEvent) {
        println("📧 [NOTIFICATION] Sending order confirmation to ${orderEvent.customerEmail}")
        val subject = "Order Confirmation - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Thank you for your order! We've received your order and it's being processed.
            
            Order Details:
            Order ID: #${orderEvent.orderId}
            Total Amount: $${orderEvent.totalAmount}
            Order Time: ${orderEvent.timestamp}
            
            Items:
            ${orderEvent.items.joinToString("\n") { "${it.quantity}x ${it.menuItemName} - $${it.totalPrice}" }}
            
            We'll notify you when your order is ready for pickup.
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
        println("✅ [NOTIFICATION] Order confirmation sent to ${orderEvent.customerEmail} for order #${orderEvent.orderId}")
    }

    // Send order confirmed notification
    private fun sendOrderConfirmedNotification(orderEvent: OrderEvent) {
        val subject = "Order Confirmed - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Your order has been confirmed and is now being prepared in our kitchen.
            
            Order ID: #${orderEvent.orderId}
            
            We'll notify you again when your order is ready for pickup.
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
    }

    // Send order preparing notification
    private fun sendOrderPreparingNotification(orderEvent: OrderEvent) {
        val subject = "Order in Preparation - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Your order is now being prepared by our kitchen staff.
            
            Order ID: #${orderEvent.orderId}
            
            Estimated time for pickup: 20-30 minutes
            We'll notify you when your order is ready.
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
    }

    // Send order ready notification
    private fun sendOrderReadyNotification(orderEvent: OrderEvent) {
        val subject = "Order Ready for Pickup - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Great news! Your order is ready for pickup.
            
            Order ID: #${orderEvent.orderId}
            Ready Time: ${orderEvent.timestamp}
            
            Please visit us to collect your order.
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
    }

    // Send order completed notification
    private fun sendOrderCompletedNotification(orderEvent: OrderEvent) {
        val subject = "Order Completed - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Thank you for dining with us! Your order has been completed.
            
            Order ID: #${orderEvent.orderId}
            Total Amount: $${orderEvent.totalAmount}
            
            We hope you enjoyed your meal. Please visit us again soon!
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
    }

    // Send order cancelled notification
    private fun sendOrderCancelledNotification(orderEvent: OrderEvent) {
        val subject = "Order Cancelled - #${orderEvent.orderId}"
        val message = buildOrderMessage(orderEvent, """
            Dear ${orderEvent.customerName},
            
            Your order has been cancelled.
            
            Order ID: #${orderEvent.orderId}
            
            If you did not request this cancellation, please contact us immediately.
            
            Best regards,
            Restaurant Team
        """.trimIndent())

        sendEmail(orderEvent.customerEmail, subject, message)
    }

    // Send email using JavaMailSender
    private fun sendEmail(to: String, subject: String, text: String) {
        try {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.subject = subject
            message.text = text
            mailSender.send(message)
            println("Email sent successfully to $to - Subject: $subject")
        } catch (e: Exception) {
            println("Email failed to send to $to: ${e.message}")
        }
    }

    private fun buildOrderMessage(orderEvent: OrderEvent, defaultMessage: String): String {
        println(" [NOTIFICATION] Building message for order #${orderEvent.orderId} - Event: ${orderEvent.eventType}")
        return defaultMessage
    }
}
