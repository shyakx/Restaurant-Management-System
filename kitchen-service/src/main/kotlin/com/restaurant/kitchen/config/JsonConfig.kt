package com.restaurant.kitchen.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
class JsonConfig {

    @Bean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        val objectMapper = ObjectMapper()
        
        // Prevent infinite recursion
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        
        converter.objectMapper = objectMapper
        return converter
    }
}
