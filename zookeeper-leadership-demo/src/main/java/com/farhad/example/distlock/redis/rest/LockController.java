package com.farhad.example.distlock.redis.rest;


import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farhad.example.distlock.common.Constants;
import com.farhad.example.distlock.redis.service.RedisLockService;

import lombok.RequiredArgsConstructor;

@RestController
@Profile(Constants.REDIS_PROFILE)
@RequestMapping("/")
@RequiredArgsConstructor
public class LockController {
    private final RedisLockService lockService;
    
    @PutMapping("/lock")
    public String lock() {

        return lockService.lock();

    }

    // @PutMapping("/properLock")
    // public String properLock() {

    //     return lockService.properLock();

    // }
    // @PutMapping("/failLock")
    // public String failLock() {

    //     return lockService.failLock();

    // }

}
