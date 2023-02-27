package com.farhad.example.demo.zookeeper.self.group;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.farhad.example.demo.zookeeper.self.utils.MoreZKPaths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateGroup implements Watcher{
    void tt() {  
        }

    private static final int SESSION_TIMEOUT = 5000;

    private ZooKeeper zk;

    private final CountDownLatch connectedSignal = new CountDownLatch(1);


    public void connect ( String hosts ) throws IOException, InterruptedException {

        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);

        connectedSignal.await();
    }

    @Override
    public void process(WatchedEvent event) {
        
        log.info("Event: {}", event);

        if ( event.getState() ==  Event.KeeperState.SyncConnected ) {

            log.info("Connected ...");
            connectedSignal.countDown();
        }

    }

    public void create( String groupName ) throws KeeperException, InterruptedException {

        String path = MoreZKPaths.makePath(groupName);

        String createdPath = zk.create(path, null /* data */, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        log.info("Path Created: {}", createdPath);
    }

    public void close() throws InterruptedException {

        zk.close();

    }

    public static void main(String[] args) throws Exception {
        CreateGroup createGroup = new CreateGroup();
        createGroup.connect(args[0]);
        createGroup.create(args[1]);
        createGroup.close();
    }
}
