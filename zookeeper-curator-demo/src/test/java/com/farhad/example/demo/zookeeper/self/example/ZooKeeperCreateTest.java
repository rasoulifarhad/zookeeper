package com.farhad.example.demo.zookeeper.self.example;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperCreateTest extends AbstractTest {
    

    @Test
    public void create_test() {  

        String path = Constant.DEFAULT_PATH;

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(10);
        byte[] data = buffer.array();

        try {

            ZooKeeperConnection conn = new ZooKeeperConnection();
            ZooKeeper zk = conn.connect(server.getConnectString());
            ZooKeeperCreate command = new ZooKeeperCreate(zk);

            command.create(path, data);
            conn.close();
            
        }  catch ( KeeperException e ) {
            log.info("KeeperException: {}", e.toString());


        }  catch ( IOException e ) {
            log.info("Exception: {}", e.toString());

        }  catch ( InterruptedException e ) {
            log.info("InterruptedException");
        }

    }
}
