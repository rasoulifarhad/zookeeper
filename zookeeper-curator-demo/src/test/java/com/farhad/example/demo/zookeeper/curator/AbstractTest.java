package com.farhad.example.demo.zookeeper.curator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.curator.utils.CloseableUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import java.util.UUID;

public class AbstractTest {

    // static final String KEY = "/my_key";
    static final String KEY_FORMAT = "/%s";

    TestingServer server = null ;

    @BeforeEach
    void  before() {

        try {
            server = new TestingServer() ;
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
    }

    @AfterEach
    void after() {
        
        CloseableUtils.closeQuietly(server);
    }

    AsyncCuratorFramework getAndStartClientAsync(String connectString) {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start(); 
        return  AsyncCuratorFramework.wrap(client);
        
    }


    AsyncCuratorFramework getAndStartClientAsync() {
        return getAndStartClientAsync(server.getConnectString());
    }

    CuratorFramework getClient(String connectString,boolean start)  {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        if (start)
            client.start(); 
        return  client;
        
    }

    CuratorFramework getClient(String connectString)  {
        return getClient(connectString,true);        
    }

    CuratorFramework getClient()  {

        return getClient(server.getConnectString(),true);
    }

    CuratorFramework getClient(boolean start)  {

        return getClient(server.getConnectString(),start);
    }

    String key() {
        return String.format(KEY_FORMAT,UUID.randomUUID().toString());
    }


    @AllArgsConstructor
    @Getter
    static class StringData {
        private final String data;
    }

}
