package com.farhad.example.demo.zookeeper.self.utils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ConnectionWatcher implements Watcher {
    
    private static final int DEFAULT_SESSION_TIMEOUT = 5000 ;

    final CountDownLatch connectedSignal = new CountDownLatch(1);
    
    protected ZooKeeper zk ;
    
    public void connect(String hosts, int sessionTimeout) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, sessionTimeout, this);
        connectedSignal.await();
    }

    public void connect(String hosts) throws IOException, InterruptedException {
        connect(hosts, DEFAULT_SESSION_TIMEOUT);
    }
    
    @Override
    public void process(WatchedEvent event) {
        if ( event.getState() == Event.KeeperState.SyncConnected ) {
            connectedSignal.countDown();
        }
        
    }

    public void close() throws InterruptedException {
        zk.close();
    }
    
}
