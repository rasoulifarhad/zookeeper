package com.farhad.example.demo.zookeeper.curator.example;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderSelectorExample {
    
    private static final int CLIENT_QTY = 10;
    private static final String PATH = "/com/farhad/example/basic/leaderSelector/leader" ;

    public static class MasterEligible extends LeaderSelectorListenerAdapter implements Closeable {

        private final String name ;
        private final LeaderSelector leaderSelector ;
        private final AtomicInteger leaderCount = new AtomicInteger();

        public MasterEligible(CuratorFramework client, String path, String name) {
            this.name = name ;
            leaderSelector = new LeaderSelector(client, path, this);
            leaderSelector.autoRequeue();
        }


        public void start() throws IOException {
            leaderSelector.start();
        }
        @Override
        public void close() throws IOException {
            leaderSelector.close();  
        }
    

        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {

            final int waitSeconds = ( int ) ( 5 * Math.random() ) + 1 ;

            log.info("#### {} is now the leader . waiting {} seconds ..." , name, waitSeconds );
            log.info("#### {} has been leader {} time(s) before." , name, leaderCount.getAndIncrement() );
            
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds) );
            } catch (InterruptedException e) {
                   log.error("####  {} was interrupted.", name);
                   Thread.currentThread().interrupt(); 
            } finally {

                log.info("#### {} interrupted.", name);
            }
        }
    
    
    }
    
    public static void main(String[] args) {
        
        String hostPort = "127.0.0.1:2181";
        List<CuratorFramework> clients = new ArrayList<>();
        List<MasterEligible> masterEligibles = new ArrayList<>() ;

        try {
            
            for (int i = 0; i < CLIENT_QTY; i++) {
                
                CuratorFramework client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
                clients.add(client);

                MasterEligible candid = new MasterEligible(client, PATH, "Client #" + i ) ;
                masterEligibles.add(candid);

                client.start();
                candid.start();
            }

            Thread.sleep(300_000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.info("Shutting down...");
            for (MasterEligible masterEligible : masterEligibles) {
                CloseableUtils.closeQuietly(masterEligible);
            }

            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }

        }
    }


}
