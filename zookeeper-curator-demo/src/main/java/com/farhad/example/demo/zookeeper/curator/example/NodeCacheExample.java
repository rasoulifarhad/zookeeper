package com.farhad.example.demo.zookeeper.curator.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

public class NodeCacheExample {
    
    private final static String PATH = "/com/farhad/example/basic/nodeCache";


    

    public static void main(String[] args) {

        String hostPort = "127.0.0.1:2181" ;
        CuratorFramework client = null;
        NodeCache cache =null;

        try {
        
            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(100, 3));
            client.start();

            cache = new NodeCache(client, PATH);
            cache.start();

            processCommands(client, cache);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(client);
        }
    }

    private static void addListener() {

    }

    private static void processCommands(CuratorFramework client, NodeCache cache) {

    }

    private static void show() {

    }
    private static void remove() {

    }
    private static void setValue() {

    }

    
    
    private static void printHelp() {
		System.out
				.println("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:\n");
		System.out
				.println("set <value>: Adds or updates a node with the given name");
		System.out.println("remove: Deletes the node with the given name");
		System.out.println("show: Display the node's value in the cache");
		System.out.println("quit: Quit the example");
		System.out.println();
	}
}
