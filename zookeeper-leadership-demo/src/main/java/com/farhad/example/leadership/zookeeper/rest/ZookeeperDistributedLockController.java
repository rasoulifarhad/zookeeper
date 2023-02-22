package com.farhad.example.leadership.zookeeper.rest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

@RestController
@Slf4j
@Profile("leadership-zookeeper")
public class ZookeeperDistributedLockController {
    
    @Autowired
    private ZookeeperLockRegistry zookeeperLockRegistry ;


    @GetMapping("lock/zookeeper")
    public void tryLockZookeeper() {

        Lock lock = this.zookeeperLockRegistry.obtain("zookeeper");
        try {
            // Try to lock within the specified time. If other locks are already locked and the current 
            // thread cannot be locked, it will return false if the lock fails; if the lock succeeds, it 
            // will return true
            if ( lock.tryLock( 3, TimeUnit.SECONDS ) ) {
                log.info( "lock is ready" );
                TimeUnit.SECONDS.sleep(5);
            }
        } catch(InterruptedException e) {
            log. error("obtain lock error", e);

        } finally {
            lock.unlock();
        }
    }

}
