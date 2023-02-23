package com.farhad.example.demo.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;

@Slf4j
public class CuratorOpsUtils {
  
    
    public static void delete(CuratorFramework client , String path) throws Exception {

        // client.delete().forPath(path);
        client.delete().deletingChildrenIfNeeded().forPath(path);

    }

    public static void guaranteedDelete(CuratorFramework client , String path) throws Exception {

        // client.delete().forPath(path);
        client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);

    }

    public static void create(CuratorFramework client , String path, byte[] payload) throws Exception {

        client.create().creatingParentsIfNeeded().withProtection().forPath(path, payload);
    }

    public static void create(CuratorFramework client , String path) throws Exception {

        client.create().creatingParentsIfNeeded().withProtection().forPath(path);
    }

    public static void createEphemeral(CuratorFramework client , String path, byte[] payload) throws Exception {

        client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public static String createPersistent(CuratorFramework client , String path) throws Exception {

        return client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT).forPath(path);
    }

    public static String createPersistentSequential(CuratorFramework client , String path) throws Exception {

        return client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path);
    }

    public static void createEphemeral(CuratorFramework client , String path) throws Exception {

        client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL).forPath(path);
    }

    public static String createEphemeralSequential(CuratorFramework client , String path, byte[] payload) throws Exception {

        return client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public static String createEphemeralSequential(CuratorFramework client , String path) throws Exception {

        return client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
    }

    public static void setData(CuratorFramework client , String path, byte[] payload) throws Exception {

        client.setData().forPath(path, payload);
    } 

    public static void setDataAsync(CuratorFramework client , String path, byte[] payload) throws Exception {

        CuratorListener  listener = new CuratorListener() {

            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                log.info("setDataAsync: {}",event);
            }
            
        };

        client.getCuratorListenable().addListener(listener);

        // set data for the given node asynchronously. The completion
		// notification
		// is done via the CuratorListener.
        client.setData().inBackground().forPath(path,payload);
    } 

    public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback, String path, byte[] payload) throws Exception {

        client.setData().inBackground(callback).forPath(path, payload);
    }

    /**
	* Get children and set a watcher on the node. The watcher notification
	* will come through the CuratorListener (see setDataAsync() above).
	*/
    public static List<String> watchedGetChildren(CuratorFramework client, String path) throws Exception {

        return client.getChildren().watched().forPath(path);
    }

    /**
    * Get children and set the given watcher on the node.
	*/
    public static List<String> watchedGetChildren(CuratorFramework client, Watcher watcher, String path) throws Exception {

        return client.getChildren().usingWatcher(watcher).forPath(path);
    }

    public static void createOrSetData(CuratorFramework client, String path, byte[] payload) throws Exception {

        client.create().orSetData().forPath(path, payload); 
    } 

}
