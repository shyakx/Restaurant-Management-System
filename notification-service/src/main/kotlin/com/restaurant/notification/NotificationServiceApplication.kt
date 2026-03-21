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
    val inDocker = System.getenv("IN_DOCKER") == "true"
    
    if (!inDocker) {
        val currentDir = System.getProperty("user.dir")
        val projectRoot = File(currentDir).parentFile.absolutePath
        
        val dotenv = Dotenv.configure()
            .directory(projectRoot)
            .ignoreIfMissing()
            .load()
        
        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } else {
        println("Using Docker environment variables")
    }
    
    runApplication<NotificationServiceApplication>(*args)
}
