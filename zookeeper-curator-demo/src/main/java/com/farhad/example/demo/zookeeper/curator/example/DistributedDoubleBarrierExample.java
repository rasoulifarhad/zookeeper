package com.farhad.example.demo.zookeeper.curator.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedDoubleBarrierExample {
    
    private static final int QTY = 5 ;
    private static final String PATH = "/com/farhad/example/basic/doubleBarrier";
    
    public static void main(String[] args) {
        
        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null ;
        try {
            
            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
            client.start();  
            ExecutorService service = Executors.newFixedThreadPool(QTY);

            for (int i = 0; i < QTY; i++) {
                

                final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, PATH, QTY);
                final int index = i ;
                Callable<Void>  task =  new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        Thread.sleep( (long) ( 3 * Math.random() ) );
                        log.info("#### Client #{} enters", index);
                        barrier.enter();
                        log.info("#### Client #{} begins", index);
                        Thread.sleep( (long) ( 3000 * Math.random() ) );
                        barrier.leave();
                        log.info("#### Client #{} left", index);

                        return null;
                    }
                };

                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(client);
        }
    }

}
