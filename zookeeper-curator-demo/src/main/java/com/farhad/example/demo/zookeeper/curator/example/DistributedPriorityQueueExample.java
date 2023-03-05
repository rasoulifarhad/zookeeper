package com.farhad.example.demo.zookeeper.curator.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.queue.DistributedPriorityQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedPriorityQueueExample {
    
    private static final String PATH = "/com/farhad/example/basic/priorityQueue";



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

        DistributedPriorityQueue<String> priorityQueue =  null; 

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
            priorityQueue = builder.buildPriorityQueue(0);
            priorityQueue.start();

            for (int i = 0; i < 10; i++) {
                
                int priority = ( int ) ( Math.random() * 100 );
                log.info( "#### Messsage: {} , Priority: {}", "message-" + i, priority );

                priorityQueue.put( "message-" + i, priority);

                Thread.sleep( ( int ) ( Math.random() * 50 ) );

            }

            Thread.sleep( 20_000 );


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(priorityQueue);
            CloseableUtils.closeQuietly(client);
        }

    }

}
