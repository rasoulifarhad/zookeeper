package com.farhad.example.demo.zookeeper.self.example;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

public class ZooKeeperSetDataTest extends AbstractTest {
    
        private static final String PATH = "defaultPath";

    @Test
    public void set_data_test() {

            //given
            ZooKeeperConnection conn = new ZooKeeperConnection();

            String path = PATH ;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(10);
            byte[] data = buffer.array() ;
            try {

                ZooKeeper zk = conn.connect(server.getConnectString());
                ZooKeeperSetData  zooKeeperSetData = new ZooKeeperSetData(zk);
                zooKeeperSetData.update(path, data);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        
    }
}
