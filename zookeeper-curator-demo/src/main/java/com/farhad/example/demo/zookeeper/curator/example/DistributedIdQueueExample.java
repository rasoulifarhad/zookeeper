package com.farhad.example.demo.zookeeper.curator.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.queue.DistributedIdQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedIdQueueExample {
    
    private static final String PATH = "/com/farhad/example/basic/idQueue";


    private static QueueSerializer<String> createQueueSerializer( ) {
        return new QueueSerializer<String>() {

            @Override
            public String deserialize(byte[] bytes) {
                return new String( bytes );
            }

            @Override
            public byte[] serialize(String item) {
                return item.getBytes();
            }
            
        };
    }

    private static QueueConsumer<String> createQueueConsumer() {

        return new QueueConsumer<String>() {

            @Override
            public void consumeMessage(String message) throws Exception {
                log.info("#### Consume one message: {}", message);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                log.info("#### Connection new state: {}",newState.name());
            }
            
        };
    }
    
    public static void main(String[] args) {
     
        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null;
        DistributedIdQueue<String> idQueue = null;

        try {

            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));

            client.getCuratorListenable().addListener( new CuratorListener() {

                @Override
                public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                        log.info("#### CuratorEvent: {}",event.getType().name());
                }
                
            } );
            client.start();

            QueueConsumer<String> consumer = createQueueConsumer();
            QueueSerializer<String> serializer = createQueueSerializer();

            QueueBuilder<String> builder = QueueBuilder.builder(client, consumer, serializer, PATH);
            
            idQueue = builder.buildIdQueue();
            idQueue.start();

            for (int i = 0; i < 10; i++) {
                
                idQueue.put( "message-" + i, "id-" + i );
                
                Thread.sleep( (long) ( 50 * Math.random() ) );

                idQueue.remove("id-" + i) ;

            }

            Thread.sleep(20_000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(idQueue);
            CloseableUtils.closeQuietly(idQueue);
        }


    }
}
