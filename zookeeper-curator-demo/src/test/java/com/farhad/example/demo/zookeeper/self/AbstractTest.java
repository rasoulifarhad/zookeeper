package com.farhad.example.demo.zookeeper.self;

import java.nio.ByteBuffer;

import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractTest {
    void tt() {
        
    }

    protected TestingServer server = null ;

    @BeforeEach
    public void before() {

        try {
            server = new TestingServer();
        } catch( Exception ex ) {

            log.error("Error: {}", ex.toString());
            ex.printStackTrace();

        }
    }

    @AfterEach
    public void after() {

        if (server != null ) {
            log.info( "Closing server...." );
            CloseableUtils.closeQuietly(server);
        }
            

    }

    protected void bootstrap(ZooKeeper  zk,String path,int data) {

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(10);
        byte[] byteData = buffer.array();

        bootstrap(zk, path,byteData);
        
    }

    protected void bootstrap(ZooKeeper  zk,String path,byte[] data) {


        try{
            zk.create(path, data, ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
            
        } catch( Exception ex ) {

            log.error("Bootstrap failed: {}", ex.toString());

        }
    }

}
