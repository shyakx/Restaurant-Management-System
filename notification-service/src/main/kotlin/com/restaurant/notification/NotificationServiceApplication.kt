package com.restaurant.notification

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import java.io.File

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
class NotificationServiceApplication

fun main(args: Array<String>) {
    // In Docker, environment variables are already set by docker-compose
    // Only load .env if running locally (not in Docker)
    val inDocker = System.getenv("IN_DOCKER") == "true"
    
    if (!inDocker) {
        // Load environment variables from project root for local development
        val currentDir = System.getProperty("user.dir")
        val projectRoot = File(currentDir).parentFile.absolutePath
        
        val dotenv = Dotenv.configure()
            .directory(projectRoot)
            .ignoreIfMissing()
            .load()
        
        // Set environment variables
        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } else {
        println("Environment: Using Docker environment variables")
    }
    
    runApplication<NotificationServiceApplication>(*args)
}
