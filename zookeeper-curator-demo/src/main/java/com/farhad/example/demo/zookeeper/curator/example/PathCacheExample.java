package com.farhad.example.demo.zookeeper.curator.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import lombok.extern.slf4j.Slf4j;

/**
 * Command processor that allows adding/updating/removed nodes in a path.
 * 
 * PathChildrenCache 
 * 
 *     keeps cache of these changes and outputs when updates occurs.
 */
@Slf4j
public class PathCacheExample {
    
    private static final String PATH = "/com/farhad/example/cache";


    public static void main(String[] args) throws Exception {
        
        TestingServer server = new TestingServer();
        CuratorFramework client = null;
        PathChildrenCache cache = null;

        try {
            
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start(); 

            cache = new PathChildrenCache(client, PATH, true);
            cache.start();

            processCommand(client, cache) ;

        } finally {

            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(server);

        }
    }


    private static void processCommand(CuratorFramework client, PathChildrenCache cache) throws Exception {

        printHelp() ;

        try {

            addListener( cache ) ;

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done = false ;

            while ( !done ) {
                log.info(">");
                String line = in.readLine();
                if ( line == null ) {
                    break;
                }

                String command = line.trim();
                String[] parts = command.split("\\s");
                if ( parts.length == 0 ) {

                    continue;
                } 
                
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);
                String operation = parts[0];

                if ( operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?") ) {

                    printHelp();;
                } else if ( operation.equalsIgnoreCase("quit") || operation.equalsIgnoreCase("q") ) {
                    done = true;
                } else if ( operation.equalsIgnoreCase("set") ) {
                    setValue(client, command, args) ;

                } else if ( operation.equalsIgnoreCase("remove") )  {
                    remove(client, command, args);
                } else if ( operation.equalsIgnoreCase("list") ) {
                    list( cache );
                }

                Thread.sleep(1000);
 
                
            }
            
        } finally {

        }
    }


    private static void addListener(PathChildrenCache cache) {

        PathChildrenCacheListener listener = new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                
                switch ( event.getType() ) {
                    case CHILD_ADDED:
                        log.info("Node added: {}", ZKPaths.getNodeFromPath( event.getData().getPath() ));
                        break;
                    case CHILD_UPDATED:
                        log.info("Node changed: {}", ZKPaths.getNodeFromPath( event.getData().getPath() ));
                        break;
                    case CHILD_REMOVED:
                        log.info("Node removed: {}", ZKPaths.getNodeFromPath( event.getData().getPath() ));
                        break;
                default:
                        break;
                }
            }
            
        };
        cache.getListenable().addListener(listener);
    }


    private static void list(PathChildrenCache cache) {

        if ( cache.getCurrentData().size()  == 0 ) {
            log.info("* empty *");
        } else {

            for (ChildData childData : cache.getCurrentData()) {

                log.info("{} = {}",childData.getPath(), new String( childData.getData()));
                
            }
        }
    }


    private static void remove(CuratorFramework client, String command, String[] args) throws Exception {

        if ( args.length != 1  ) {
            log.error("syntax error (expected remove <path>): {}", command);
            return;
        }

        String name = args[0];
        if ( name.contains("/") ) {
            log.error("Invalid node name: {}", name);
            return;
        }

        String path = ZKPaths.makePath(PATH, name);
        try {

            client.delete().forPath(path);
            
        } catch (KeeperException.NoNodeException e) {
            // ignore
        }
    }


    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception {

        if ( args.length != 2 ) {
            log.error("syntax error (expected set <path> <value>): {}", command);
            return;
        }

        String name = args[0] ;
        if ( name.contains("/") ) {
            log.error("Invalid node name: {}", name);
            return;
        }
        String path = ZKPaths.makePath(PATH, name);

        byte[] bytes = args[1].getBytes();

        try {
            client.setData().forPath(path, bytes);          
        } catch (KeeperException.NoNodeException e) {
            client.create().creatingParentContainersIfNeeded().forPath(path, bytes);
        }   
    }


    private static void printHelp() {

        log.info("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:");
        log.info("set <name> <value>: Adds or updates a node with the given name");
        log.info("remove <name>: Deletes the node with the given name");
        log.info("list: List the nodes/values in the cache");
        log.info("quit: Quit the example");
        log.info("");
    }
}
