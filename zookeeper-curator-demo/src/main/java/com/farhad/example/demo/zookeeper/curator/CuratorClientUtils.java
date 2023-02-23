package com.farhad.example.demo.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorClientUtils {

    public static CuratorFramework createSimple(String connectionString) {

        // The first retry will wait 1 second - 
        // The second will wait up to 2 seconds -
        // The third will wait up to 4 seconds.
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        return CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
    }

    public static CuratorFramework createWithOptions(String connectionString, 
                                                        RetryPolicy retryPolicy, 
                                                        int connectionTimeoutMs, 
                                                        int sessionTimeoutMs) {

        return CuratorFrameworkFactory.builder()
                                          .connectString(connectionString)
                                          .retryPolicy(retryPolicy)
                                          .connectionTimeoutMs(connectionTimeoutMs)
                                          .sessionTimeoutMs(sessionTimeoutMs)
                                          .build();
    }

    public static CuratorFramework createWithOptions2(String connectionString, 
                                                        RetryPolicy retryPolicy, 
                                                        int connectionTimeoutMs, 
                                                        int sessionTimeoutMs,
                                                        ConnectionStateListener connectionStateListener,
                                                        UnhandledErrorListener unhandledErrorListener) {

                                                            
        CuratorFramework client = CuratorFrameworkFactory.builder()
                                          .connectString(connectionString)
                                          .retryPolicy(retryPolicy)
                                          .connectionTimeoutMs(connectionTimeoutMs)
                                          .sessionTimeoutMs(sessionTimeoutMs)
                                          .build();
        client.getConnectionStateListenable().addListener(connectionStateListener);
        client.getUnhandledErrorListenable().addListener(unhandledErrorListener);

        return client;
    }

}
