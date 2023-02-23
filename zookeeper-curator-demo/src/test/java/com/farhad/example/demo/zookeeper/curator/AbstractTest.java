package com.farhad.example.demo.zookeeper.curator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.curator.utils.CloseableUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;
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
    public void  before() {

        try {
            server = new TestingServer() ;
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
    }

    @AfterEach
    public void after() {
        
        CloseableUtils.closeQuietly(server);
    }

    public AsyncCuratorFramework getAndStartClientAsync(String connectString) {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start(); 
        return  AsyncCuratorFramework.wrap(client);
        
    }


    public AsyncCuratorFramework getAndStartClientAsync() {
        return getAndStartClientAsync(server.getConnectString());
    }

    public CuratorFramework getClient(String connectString,boolean start)  {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        if (start)
            client.start(); 
        return  client;
        
    }

    public CuratorFramework getClient(String connectString)  {
        return getClient(connectString,true);        
    }

    public CuratorFramework getClient()  {

        return getClient(server.getConnectString(),true);
    }

    public CuratorFramework getClient(boolean start)  {

        return getClient(server.getConnectString(),start);
    }

    public String key() {
        return String.format(KEY_FORMAT,UUID.randomUUID().toString());
    }


    public void setNodeData(String path, String data) throws Exception {

        this.setNodeData(path, data.getBytes());
    }

    public void setNodeData(String path, byte[] data) throws Exception {

        try ( CuratorFramework client = getClient() ) { 
            client.setData().forPath(path,data);
        } 

    }

    public void createNode(String path) throws Exception {

        try ( CuratorFramework client = getClient() ) { 

            if (client.checkExists().forPath(path) != null){
                CuratorOpsUtils.delete(client,path);
            }
            CuratorOpsUtils.create(client, path);;
        } 

    }

    public void createPersistNode(String path) throws Exception {

        try ( CuratorFramework client = getClient() ) { 
            client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path);
        } 

    }

    @AllArgsConstructor
    @Getter
    public static class StringData {
        private final String data;
    }

}
