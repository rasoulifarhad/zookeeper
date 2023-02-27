package com.farhad.example.demo.zookeeper.self.utils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;

public class Connection {
    
    private static final int DEFAULT_SESSION_TIMEOUT = 500 ;

    public ZooKeeper connect(String hosts, int sessionTimeout) throws IOException, InterruptedException {

        final CountDownLatch connectedSignal = new CountDownLatch(1);

        ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, event -> {
                    if ( event.getState() ==  Event.KeeperState.SyncConnected) {
                        connectedSignal.countDown();;
                    }
        });

        connectedSignal.await();
        return zk;
    }

    public ZooKeeper connect(String hosts) throws IOException, InterruptedException {
        return connect(hosts, DEFAULT_SESSION_TIMEOUT );
    }

}
