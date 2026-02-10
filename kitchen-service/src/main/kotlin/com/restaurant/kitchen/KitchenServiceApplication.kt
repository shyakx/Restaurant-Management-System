package com.restaurant.kitchen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
class KitchenServiceApplication

fun main(args: Array<String>) {
    runApplication<KitchenServiceApplication>(*args)
}
