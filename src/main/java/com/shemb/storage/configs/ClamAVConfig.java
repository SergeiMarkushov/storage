package com.shemb.storage.configs;

import io.sensesecure.clamav4j.ClamAV;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class ClamAVConfig {
    @Bean
    public ClamAV clamavClient() {
        return new ClamAV(new InetSocketAddress("localhost", 3310), 500);
    }
}
