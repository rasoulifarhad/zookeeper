package com.farhad.example.demo.zookeeper.curator.example;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrudExample {
    

    public static void create( CuratorFramework client, String path, byte[] payload ) throws Exception {

        client.create().creatingParentsIfNeeded().forPath(path, payload);
    }

    public static void createEphemeral( CuratorFramework client, String path, byte[] payload ) throws Exception {

        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public static String createEphemeralSequential( CuratorFramework client, String path, byte[] payload ) throws Exception {

        return client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public static void setData( CuratorFramework client, String path, byte[] payload ) throws Exception {

        client.setData().forPath(path, payload);
    }

    public static void setDataAsync( CuratorFramework client, String path, byte[] payload ) throws Exception {

        client.getCuratorListenable().addListener(watchedSetDataAsyncListener());
        

        client.setData().inBackground().forPath(path, payload);
    }

    public static void setDataAsyncWithCallback( CuratorFramework client, 
                                                    BackgroundCallback callback, 
                                                    String path, 
                                                    byte[] payload ) throws Exception {


        client.setData().inBackground(callback).forPath(path, payload);
    }

    public static void delete( CuratorFramework client, String path ) throws Exception {

        client.delete().deletingChildrenIfNeeded().forPath(path);
    }

    public static void guranteedDelete( CuratorFramework client, String path ) throws Exception {

        client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path)  throws Exception {

        // Get children and set a watcher on the node.
        //  The watcher notification will come through the CuratorListener
        client.getCuratorListenable().addListener(watchedGetChildrenListener());

        return client.getChildren().watched().forPath(path);
        
    }

    public static CuratorListener watchedSetDataAsyncListener() {

        return 
                new CuratorListener() {

                        @Override
                        public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                            log.info("####### SetDataAsync Event Received: {}", event);                
                        }
            
                };
    }

    public static CuratorListener watchedGetChildrenListener() {

        return 
                new CuratorListener() {

                        @Override
                        public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                            log.info("####### Event Received: {}", event);                
                        }
            
                };
    }

    public static void attachListener( CuratorFramework client, CuratorListener listener ) {

        client.getCuratorListenable().addListener(listener);
    }

    public static void dettachListener( CuratorFramework client, CuratorListener listener ) {

        client.getCuratorListenable().removeListener(listener);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher)  throws Exception {

        return client.getChildren().usingWatcher(watcher).forPath(path);
    }

    public static void main(String[] args) {
        
        String hostPort  = "127.0.0.1:2181";

        CuratorFramework client = null ;

        try {
            
            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));

            client.start();;

            String basePath = "/com/farhad/example/basic/crud" ;

            if ( client.checkExists().forPath(basePath) != null ) {
                log.info("####### {} Existed so remove that", basePath);
                delete(client, basePath);
                log.info("####### Deleted: {} ", basePath);
            } 
            String firstZNode = basePath + "/first";
            create(client, firstZNode, "first is a node (znode)".getBytes());

            log.info("####### {} : {}", firstZNode, new String( client.getData().forPath(firstZNode ) ));


            String secondZNode = createEphemeralSequential(client, basePath + "/second", "second is a node (znode)".getBytes());

            log.info("####### {} ( RealName: {} )  : {} ", ( basePath + "/second" ), secondZNode, new String( client.getData().forPath(secondZNode) ));

            setDataAsync(client, firstZNode, "first is Async updated".getBytes());
            
            setDataAsyncWithCallback(client, new BackgroundCallback() {

                @Override
                public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("setDataAsyncWithCallback event: {}", event);                    
                }
                
            }, firstZNode, "first is calback updated".getBytes());

            List<String> children = watchedGetChildren(client, basePath);

            log.info("####### Childeren of {} : {}", basePath, children);

            String thirdZNode = basePath + "/third";

            createEphemeral(client, thirdZNode, "third is a node (znode)".getBytes());

            children = watchedGetChildren(client, basePath);

            log.info("####### Childeren of {} : {}", basePath, children);


        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }


}
