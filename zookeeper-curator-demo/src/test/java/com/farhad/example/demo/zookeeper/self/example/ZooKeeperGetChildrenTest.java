package com.farhad.example.demo.zookeeper.self.example;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperGetChildrenTest extends AbstractTest{

    private static final String PATH = "defaultPath";
    
    @Test
    public void get_children_test() {

          //given
          final CountDownLatch connectionSignal = new CountDownLatch(1);
          ZooKeeperConnection conn = new ZooKeeperConnection();
          
          try {

            ZooKeeper zk = conn.connect(server.getConnectString());

            Stat stat = zk.exists(PATH, true);

            if ( stat != null ) {

                List<String> children = zk.getChildren(PATH, false);

                for (String child : children) {

                    log.info("Child: {}", child);
                    
                }
            } else {

                log.info("Node dose not exist");
            }
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }
    
}
