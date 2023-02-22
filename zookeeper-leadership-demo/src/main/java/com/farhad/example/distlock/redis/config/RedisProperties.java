package com.farhad.example.distlock.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("redis")
@Getter
@Setter
public class RedisProperties {
 
    private static final int DEFAULT_PORT = 6379;

    private String host;
    private Integer port;
    private String password;

    public int getPort(){
        return port == null ? DEFAULT_PORT : port;
    }
}
