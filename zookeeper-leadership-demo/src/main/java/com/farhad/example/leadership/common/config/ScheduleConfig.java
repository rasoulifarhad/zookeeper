package com.farhad.example.leadership.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile({"leadership-zookeeper","leadership-redis"})
public class ScheduleConfig {
    
}
