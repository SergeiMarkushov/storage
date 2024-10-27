package com.shemb.storage.configs;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new CamelCaseToUnderscoresNamingStrategy();
    }
}
