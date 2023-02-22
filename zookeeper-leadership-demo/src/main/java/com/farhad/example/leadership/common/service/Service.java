package com.farhad.example.leadership.common.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import com.farhad.example.leadership.common.annotation.Leader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile({"leadership-zookeeper","leadership-redis"})
public class Service {

    @Leader
    @Scheduled(cron = "0/30 * * * * *")
    public void runJob() {

        log.info("===== START JOB ====== {}");

    }
    
}
