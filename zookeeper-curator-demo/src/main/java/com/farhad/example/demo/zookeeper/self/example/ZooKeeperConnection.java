package com.farhad.example.demo.zookeeper.self.example;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZooKeeperConnection {  
    
    private ZooKeeper zk ;
    CountDownLatch connectedSignal = new CountDownLatch(1);

    public ZooKeeper connect( String connectionString )  throws IOException,InterruptedException {
        
        zk = new ZooKeeper(connectionString, 3000, new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                log.info("Connect Event: {}", event);
                
                if (  event.getState() == Event.KeeperState.SyncConnected) {
                        connectedSignal.countDown();

                }
            }
            
        });

        connectedSignal.await();
        log.info("Connection stablisherd.");
        return zk;

    }

    public void close() throws InterruptedException{
        
        zk.close();

    }
}
