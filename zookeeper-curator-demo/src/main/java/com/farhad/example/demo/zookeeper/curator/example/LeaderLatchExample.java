package com.farhad.example.demo.zookeeper.curator.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderLatchExample {
    
    private static final int CLIENT_QTY = 10 ;
    private static final String PATH = "/com/farhad/example/basic/leader" ;

    public static void main(String[] args) {
        
        String hostPort =  "127.0.0.1:2181";
        List<CuratorFramework> clients = new ArrayList<>();
        List<LeaderLatch> latchs = new ArrayList<>() ;

        try {

            for (int i = 0; i < CLIENT_QTY; i++) {
                
                CuratorFramework client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
                clients.add(client);

                LeaderLatch latch = new LeaderLatch(client, PATH, "Client #" + i);
                latchs.add(latch);

                client.start();
                latch.start();
            }

            Thread.sleep(20_000);

            LeaderLatch currentLeader = null ;

            for (int i = 0; i < CLIENT_QTY; i++) {

                LeaderLatch latch = latchs.get(i);
                if ( latch.hasLeadership() ) {
                    currentLeader = latch ;
                }
                
            }

            log.info("#### Current leader is {} ", currentLeader.getId());

            log.info("#### Release the leader {}", currentLeader.getId());
            currentLeader.close();

            latchs.get(0).await(2, TimeUnit.SECONDS);

            log.info("#### Client #0 maybe is elected as a leader or not although it want be");

            log.info("#### The ne leader is {}",latchs.get(0).getLeader().getId());

            log.info("#### Participants: {}", latchs.get(0).getParticipants());

            Thread.sleep(100_000);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            log.info("#### Shutdown");

            for (LeaderLatch leaderLatch : latchs) {
                CloseableUtils.closeQuietly(leaderLatch);
            }

            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
        }
    }


}
