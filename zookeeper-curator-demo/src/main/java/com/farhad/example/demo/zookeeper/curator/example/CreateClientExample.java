package com.farhad.example.demo.zookeeper.curator.example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateClientExample {

    private static final String PATH = "/com/farhad/example/basic";

    public static CuratorFramework createSimpl(String connectionString) {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        return CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
    }

    public  static CuratorFramework createWithOption( String connectionString, 
                                                        RetryPolicy retryPolicy,
                                                        int connectionTimeoutMs,
                                                        int sessionTimeout  ) {

        return CuratorFrameworkFactory.builder()
                                          .connectString(connectionString)
                                          .retryPolicy(retryPolicy)
                                          .connectionTimeoutMs(connectionTimeoutMs)
                                          .sessionTimeoutMs(sessionTimeout)
                                          .build();
    }

    public static void main(String[] args) {
        
        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null;

        try {
            
            client = createSimpl(hostPort);
            client.start();

            client.create().creatingParentsIfNeeded().forPath(PATH, "basic test".getBytes());
            
            CloseableUtils.closeQuietly(client);

            client = createWithOption(hostPort, 
                                        new ExponentialBackoffRetry(1000, 3), 
                                        1000,
                                        1000);

            client.start();
            log.info("Path: {} , Data: {}", PATH, new String ( client.getData().forPath(PATH) ) );

        } catch (Exception e) {
            // TODO: handle exception
        } finally {

            CloseableUtils.closeQuietly(client);
        }
    }
}
