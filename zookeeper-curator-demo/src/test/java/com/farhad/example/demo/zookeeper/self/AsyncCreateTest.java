package com.farhad.example.demo.zookeeper.self;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;
import static com.farhad.example.demo.zookeeper.Utils.sleep;

@Slf4j
public class AsyncCreateTest {
    

    private static final String PATH = "/async-create-test";
    private static final String DATA = "async-create-test";
    private static final String CTX_PATTERN = "ctx for %s";

    TestingServer server = null ;
    private ZooKeeper zk ;

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
    public void asyncCreate_test() throws Exception {

        ZooKeeper zk = new ZooKeeper(server.getConnectString(), 20000, new TestWatcher());


        zk.create(PATH, DATA.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,new StringCallback(){

            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                    Code code = Code.get(rc);
                    switch( code  ) {
                        case OK: 
                            log.info("Code: {}",code);
                            break;
                        case NODEEXISTS: 
                            log.info("Code: {}",code);
                            break;
                        case SESSIONEXPIRED: 
                            log.info("Code: {}",code);
                            break;
                        default: 
                            log.info("Code unknow: {}",code);
                            
                    }                
            }
            
        },null);

        DataCallback dataCallback = new DataCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                Code code = Code.get(rc);
                log.info("Code for check: {}",code);
                switch( code  ) {
                    case OK: 
                        break;
                    case NODEEXISTS: 
                        break;
                    case SESSIONEXPIRED: 
                        break;
                    default: 
                }                
            }
            
        };

        zk.getData(PATH, true, dataCallback, null);

        sleep(20);
    }

    public static class TestWatcher implements Watcher{

        @Override
        public void process(WatchedEvent event) {
            log.info("Event: {}", event);  
        }
        
    }
}
