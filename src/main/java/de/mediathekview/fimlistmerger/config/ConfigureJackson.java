package de.mediathekview.fimlistmerger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class ConfigureJackson {

    /*
     * By Default Apache Camel defines a own ObjectMapper. But then Spring can't autocreate
     * it's intelligent one
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

}
