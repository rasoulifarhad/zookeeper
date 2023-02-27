package com.farhad.example.demo.zookeeper.self.example;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ZooKeeperExists {
     
    private final ZooKeeper zk ;

    // private CountDownLatch connectedSignal = new CountDownLatch(1);

    public Stat exists( String path ) throws KeeperException,InterruptedException {

        
        return zk.exists(path, false);
        // return zk.exists(path, new Watcher() {

        //     @Override
        //     public void process(WatchedEvent event) {
        //         log.info("Exists Event: {}", event);
        //         connectedSignal.countDown();
        //     }
            
        // });

        // connectedSignal.await();
        // return stat;

    }
}
