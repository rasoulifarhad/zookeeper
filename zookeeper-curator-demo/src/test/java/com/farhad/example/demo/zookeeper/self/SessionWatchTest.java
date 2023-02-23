package com.farhad.example.demo.zookeeper.self;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import static com.farhad.example.demo.zookeeper.Utils.sleep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionWatchTest {
   
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
    public void sessionWatch_test() throws Exception {

        ZooKeeper zk = new ZooKeeper(server.getConnectString(), 2000, new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                log.info(">Event: {}",event);
            }
            
        });

        sleep(50);

    }
}
