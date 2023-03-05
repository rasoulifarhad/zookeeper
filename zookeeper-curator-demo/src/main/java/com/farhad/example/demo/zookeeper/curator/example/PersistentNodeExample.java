package com.farhad.example.demo.zookeeper.curator.example;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j  
public class PersistentNodeExample {
    
    private static final String PATH_EPHEMERAL = "/com/farhad/example/basic/ephemeralNode"; 
    private static final String PATH_NORMAL = "/com/farhad/example/basic/normalNode"; 

    public static void main(String[] args) {
        
        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null ;
        PersistentNode node = null ;
        try {
            
            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
            client.getCuratorListenable().addListener(new CuratorListener() {

                @Override
                public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("####  CuratorEvent: {}",event.getType().name());                    
                } 
                
            });

            client.getConnectionStateListenable().addListener( new ConnectionStateListener() {

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    log.info("### client state: {}", newState.name());
                }
                
            } );
            client.start();

            node = new PersistentNode(client, CreateMode.EPHEMERAL, false, PATH_EPHEMERAL, "ephemeral znode".getBytes())  ;
            
            node.start();
            node.waitForInitialCreate(3, TimeUnit.SECONDS);

            String actualPath = node.getActualPath();
            log.info("#### Node: {} , Value: {}",actualPath, new String( client.getData().forPath(actualPath) ));

            client.create().forPath(PATH_NORMAL, "persistent znode".getBytes());
            log.info("#### Node: {} , Value: {}", PATH_NORMAL, new String( client.getData().forPath(PATH_NORMAL) ));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(node);
            CloseableUtils.closeQuietly(client);
        }

    }
}
