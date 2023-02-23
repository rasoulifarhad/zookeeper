package com.farhad.example.demo.zookeeper.curator.leadership;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * all participants in a given leader selection must use the same path.
 */
@Slf4j
public class LeaderShipClient extends LeaderSelectorListenerAdapter implements Closeable{ 

    private final String name ;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger  atomicInteger = new AtomicInteger();

    

    public LeaderShipClient(CuratorFramework curatorFramework,String path ,String name) {
        this.name = name;
        // create a leader selector using the  path 
        this.leaderSelector = new LeaderSelector(curatorFramework, path, this);
        this.leaderSelector.autoRequeue();
    }

    public void start() {
        this.leaderSelector.start();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        // Now i am leaderrrrrrrrrr
        // Note:
        // Note:
        // Note:
        // Note:
        // Note:
        // Note:
        // This method should not return until we want to relinquish leadership
        // simulate task with wait
        int sleepTimeSec = (int)(7 * Math.random()) + 1;

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTimeSec));
        } catch( InterruptedException e ) {

            log.info("{} was Interupted.",name);
            Thread.currentThread().interrupt();


        } finally {
            log.info("{} relinquishing leadership.");
        }
    }

    @Override
    public void close() throws IOException {
        this.leaderSelector.close();
    }

    
}
