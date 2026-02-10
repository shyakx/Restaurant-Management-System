package com.restaurant.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
class NotificationServiceApplication

fun main(args: Array<String>) {
    runApplication<NotificationServiceApplication>(*args)
}
