package com.farhad.example.demo.zookeeper.self.example;


import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ZooKeeperCreate {
    
    private final ZooKeeper zk;

    public void create( String path, byte[] data ) throws KeeperException,InterruptedException  { 

        zk.create(path, data, ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info("{} Created", path);
    }

    public static void main(String[] args) {

		String hostPort = "127.0.0.1:2181";

        String path = Constant.DEFAULT_PATH;

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(10);
        byte[] data = buffer.array();

        try {

            
            ZooKeeperConnection conn = new ZooKeeperConnection();
            ZooKeeper zk = conn.connect(hostPort);
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
