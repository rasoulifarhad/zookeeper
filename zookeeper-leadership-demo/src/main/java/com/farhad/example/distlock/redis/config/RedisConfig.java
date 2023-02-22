package com.farhad.example.distlock.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

import com.farhad.example.distlock.common.Constants;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Profile(Constants.REDIS_PROFILE)
public class RedisConfig {
    
    private static final String LOCK_NAME = "lock";
    private final RedisProperties redisProperties ;

    @Bean
    @Profile(Constants.REDIS_PROFILE)
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer); 
        template.setHashValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template ;

    }

    @Bean
    @Profile(Constants.REDIS_PROFILE)
    public RedisConnectionFactory redisConnectionFactory(){
        
        RedisConnectionFactory connectionFactory = null;
        try{
            System.out.println(String.format("Setting up Redis connection to %s:%s", redisProperties.getHost(), redisProperties.getPort()));
            RedisStandaloneConfiguration config =new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
            config.setPassword(redisProperties.getPassword());

            connectionFactory = new LettuceConnectionFactory(config);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Unable to connect to redis at %s:%s");
        }
        return connectionFactory;
    }

    @Bean
    @Profile(Constants.REDIS_PROFILE)
    public RedisLockRegistry redisLockRegistry( RedisConnectionFactory redisConnectionFactory ) {
        return new RedisLockRegistry(redisConnectionFactory, LOCK_NAME);
    }
}
