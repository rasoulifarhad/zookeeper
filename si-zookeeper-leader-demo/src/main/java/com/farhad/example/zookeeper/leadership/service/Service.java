package com.farhad.example.zookeeper.leadership.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.farhad.example.zookeeper.leadership.annotation.Leader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class Service {

    @Leader
    @Scheduled(cron = "0/30 * * * * *")
    public void runJob() {

        log.info("===== START JOB ====== {}");

    }
    
}
