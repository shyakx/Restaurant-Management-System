package com.restaurant.order

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.kafka.annotation.EnableKafka
import java.io.File

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableCaching
class OrderServiceApplication

fun main(args: Array<String>) {
    // Load environment variables from project root
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
    
    runApplication<OrderServiceApplication>(*args)
}
