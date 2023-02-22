package com.farhad.example.leadership.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAspectJAutoProxy
@Profile({"leadership-zookeeper","leadership-redis"})
public class AspectConfig {
    
}
