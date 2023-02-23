package com.farhad.example.demo.zookeeper.self;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event;


import static com.farhad.example.demo.zookeeper.Utils.sleep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChildrenMonitorTest {
    
    private static final String PARENT_PATH = "/parent";

    TestingServer server = null ;

    @BeforeEach
    public void  before() {

        try {
            server = new TestingServer() ;
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
    }

    @AfterEach
    public void after() {
        
        CloseableUtils.closeQuietly(server);
    }

    @Test
    public void childrenMonitor_test() throws Exception {

        ZooKeeper zk = new ZooKeeper(server.getConnectString(), 2000, null);

        final ChildrenCallback callback = new ChildrenCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                
                log.info("Children: {}", children);
                
            }
            
        };
 
        Watcher watcher = new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                
                log.info("Event: {}", event);
                if ( event.getType() == Event.EventType.NodeChildrenChanged ) {
                    log.info("Node Children Changed: {}", event);
                    zk.getChildren(PARENT_PATH, this, callback, null);
                }
            }
            
        };

        zk.getChildren(PARENT_PATH, watcher, callback, null);

        log.info("start sleep....");
        sleep(20);
        log.info(" End ...");
    }
}
