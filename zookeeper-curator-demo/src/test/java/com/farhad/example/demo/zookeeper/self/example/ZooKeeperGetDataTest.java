package com.farhad.example.demo.zookeeper.self.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.self.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperGetDataTest extends AbstractTest {

    private static final String PATH = "defaultPath";
    @Test
    public void get_data_test() throws IOException, InterruptedException, KeeperException {

        //given
        final CountDownLatch connectionSignal = new CountDownLatch(1);
        ZooKeeperConnection conn = new ZooKeeperConnection();
        ZooKeeper zk = conn.connect(server.getConnectString());
        int bootstrapPData = 10 ;
        String bootstrapPath = PATH ;
        bootstrap(zk, bootstrapPath, bootstrapPData);
        //when
        Stat stat = zk.exists(bootstrapPath,false);

        if ( stat != null )  {

            byte[] data = zk.getData(bootstrapPath, new Watcher() {

                @Override
                public void process(WatchedEvent event) {
                        
                    log.info("zookeeper getData event : {}", event);
                    if ( event.getType() == Event.EventType.None ) {

                        log.info( "zookeeper getData event : Event.EventType.None" );
                        switch ( event.getState()  ) {

                            case Expired:
                                connectionSignal.countDown();
                                break;
                            default:
                                break;
                        }
                    } else {

                        log.info("zookeeper getData event : not Event.EventType.None");
                        try {
                            byte[] bn = zk.getData(bootstrapPath, false, null);
                            ByteBuffer bbf = ByteBuffer.wrap(bn);
                            
                            log.info("Data: {}", bbf.getInt());
                            connectionSignal.countDown();

                        } catch (KeeperException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            }, null);

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            log.info("Data-2: {}", byteBuffer.getInt());
            connectionSignal.await();

        } else {

            log.info("Node dose not exist: {}", bootstrapPath);
        }
    }
    
}
