package com.farhad.example.demo.zookeeper.curator.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedBarrierExample {
    
    private static final String PATH = "/com/farhad/example/basic/barrier";

    private static final int NUM_OF_PROCESS = 5 ;


    public static void main(String[] args) {

        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null; 

        try {
            
            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
            client.start();

            ExecutorService service = Executors.newFixedThreadPool(NUM_OF_PROCESS);

            DistributedBarrier controlBarrier = new DistributedBarrier(client, PATH);
            controlBarrier.setBarrier();

            for (int i = 0; i < NUM_OF_PROCESS; i++) {

                final DistributedBarrier barrier = new DistributedBarrier(client, PATH);

                final int  index = i ;
                Callable<Void> task = new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        Thread.sleep( (long) ( 3 * Math.random() ) );
                        log.info("#### Client #{} waits on Barrier",index);
                        barrier.waitOnBarrier();
                        log.info("#### on Barrier{} begins", index);
                        return null;
                    }
                };
                
                service.submit(task);
                
            }

            Thread.sleep(10000);

            log.info("#### all Barrier instances should wait the condition.");

            controlBarrier.removeBarrier();

            service.shutdown();

            service.awaitTermination(10, TimeUnit.MINUTES);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
        
    }
}
