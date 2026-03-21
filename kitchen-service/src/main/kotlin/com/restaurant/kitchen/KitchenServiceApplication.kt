package com.restaurant.kitchen

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import java.io.File

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
@EnableCaching
class KitchenServiceApplication

fun main(args: Array<String>) {
    val currentDir = System.getProperty("user.dir")
    val projectRoot = File(currentDir).parentFile.absolutePath
    
    val dotenv = Dotenv.configure()
        .directory(projectRoot)
        .ignoreIfMissing()
        .load()
    
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    
    runApplication<KitchenServiceApplication>(*args)
}
