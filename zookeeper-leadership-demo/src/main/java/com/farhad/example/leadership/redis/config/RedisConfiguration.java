package com.farhad.example.leadership.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@Profile("leadership-redis")
public class RedisConfiguration {
    
    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {

        return new RedisLockRegistry(redisConnectionFactory, "redis-lock");
    }

}
