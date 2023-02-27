package com.farhad.example.demo.zookeeper.self.example;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperExistsTest extends AbstractTest {
   
    @Test
    public void exists_test() {

        try {
            // Given 
            String path = Constant.DEFAULT_PATH;

            ZooKeeperConnection conn = new ZooKeeperConnection();
            ZooKeeper zk = conn.connect(server.getConnectString());

            // When
            bootstrap(zk, Constant.DEFAULT_PATH, 10);

            // Then
            ZooKeeperExists command = new ZooKeeperExists(zk);
            Stat stat = command.exists(path);
            if ( stat != null  ) {
                log.info("{} Node exists and the node version is: {}", path, stat.getVersion());
            } else {

                log.info("{} Node does not exists", path);
            }

            String apath = Constant.DEFAULT_PATH + "_a";
            Stat astat = command.exists(apath);
            if ( astat != null  ) {
                log.info("{} Node exists and the node version is: {}", apath, astat.getVersion());
            } else {

                log.info("{} Node does not exists", apath);
            }


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
