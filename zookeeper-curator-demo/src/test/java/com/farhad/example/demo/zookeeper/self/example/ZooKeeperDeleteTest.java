package com.farhad.example.demo.zookeeper.self.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperDeleteTest extends AbstractTest {
    
    @Test
    public void delete_test() {

        try {
            // Given 
            String path = Constant.DEFAULT_PATH;


            ZooKeeperConnection conn = new ZooKeeperConnection();
            ZooKeeper zk = conn.connect("localhost");

            // When
            bootstrap(zk, Constant.DEFAULT_PATH, 10);

            //Then
            ZooKeeperDelete command = new ZooKeeperDelete(zk);
            command.delete(path); //delete the node with the specified path
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
