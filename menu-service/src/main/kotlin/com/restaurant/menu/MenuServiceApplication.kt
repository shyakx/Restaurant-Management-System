package com.restaurant.menu

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class MenuServiceApplication

fun main(args: Array<String>) {
    val dotenv = Dotenv.configure().ignoreIfMissing().load()
    
    // Set system properties from .env for Spring Boot to use
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    
    runApplication<MenuServiceApplication>(*args)
}
