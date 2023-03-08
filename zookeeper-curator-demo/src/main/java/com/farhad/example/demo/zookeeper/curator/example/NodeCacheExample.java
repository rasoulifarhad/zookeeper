package com.farhad.example.demo.zookeeper.curator.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeCacheExample {
    
    private final static String PATH = "/com/farhad/example/basic/nodeCache";

    private static void addListener(final NodeCache cache) {

        NodeCacheListener listener = new NodeCacheListener() {

            @Override

            public void nodeChanged() throws Exception {

                if ( cache.getCurrentData() != null ) {
                    log.info("Node changed: {} , value: {}", 
                                                cache.getCurrentData().getPath(), 
                                                new String ( cache.getCurrentData().getData() ));
                }
                
            }
            
        };
    //    StandardListenerManager.standard().addListener(listener);
        cache.getListenable().addListener(listener);
    }

    private static void processCommands(CuratorFramework client, NodeCache cache) {

        printHelp();
        try {
            
            addListener(cache);
            BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

            boolean done = false ;

            while ( !done ) {
                
                log.info("> ");
                String line = in.readLine();
                if ( line == null ) {
                    break;
                }
                String command = line.trim();
                String[] parts = command.split("\\s");
                if ( parts.length == 0 ) {
                    continue;
                }

                String operation = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                if ( operation.equalsIgnoreCase("help") ) {
                    printHelp();
                } else if ( operation.equalsIgnoreCase("q") ) {
                    done = true ;
                } else if ( operation.equalsIgnoreCase("set") ) {
                    setValue(client, command, args);
                } else if ( operation.equalsIgnoreCase("remove") ) {
                    remove(client);
                } else if ( operation.equalsIgnoreCase("show") ) {
                    show(cache);
                }
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private static void show(NodeCache cache) {

        if ( cache.getCurrentData() != null ) {
            log.info("{} = {}", cache.getCurrentData().getPath(), new String( cache.getCurrentData().getData() ) );
        } else {
            log.info("Cache do not set a value");
        }
    }
    private static void remove(CuratorFramework client) {

        try {
            client.delete().forPath(PATH);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    private static void setValue(CuratorFramework client, String command, String[] args ) throws Exception {
        if ( args.length != 1  ) {

            log.info("syntax error (expected set <value>): {} ", command);
            return;
        }

        byte[] data = args[0].getBytes();
        try {
            client.setData().forPath(PATH, data);
        } catch (KeeperException.NoNodeException e) {
            client.create().creatingParentsIfNeeded().forPath(PATH, data);
        }

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

    public static void main(String[] args) throws Exception {

        TestingServer server = new TestingServer(); 
        // String hostPort = "127.0.0.1:2181" ;
        CuratorFramework client = null;
        NodeCache cache =null;

        try {
        
            client = CuratorFrameworkFactory.newClient( server.getConnectString() /*hostPort*/, new ExponentialBackoffRetry(100, 3));
            client.start();

            cache = new NodeCache(client, PATH);
            cache.start();

            processCommands(client, cache);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(server);
        }
    }

}
