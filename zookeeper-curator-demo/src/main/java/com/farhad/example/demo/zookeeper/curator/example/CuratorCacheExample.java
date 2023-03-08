package com.farhad.example.demo.zookeeper.curator.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * example of creating a CuratorCache that listens to events and logs the changes.
 * 
 */
@Slf4j
public class CuratorCacheExample {
    

    private static final String PATH = "/com/farhad/example/cache";

    public static void main(String[] args) throws InterruptedException {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        try ( TestingServer server = new TestingServer() ) {
            try ( CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(100, 3)) ) {
                client.start();

                try ( CuratorCache cache =   CuratorCache.build(client, PATH)) {

                    // there are several ways to set a listener on a CuratorCache. You can watch for individual events
                    // or for all events. Here, we'll use the builder to log individual cache actions
                    CuratorCacheListener listener = CuratorCacheListener.builder()
                                                                .forCreates( node -> log.info("Node created: {} {}", node.getPath(), new String ( node.getData() ) ))
                                                                .forChanges( (oldNode, node )  -> log.info("Node changed. Old: {} {}  New: {} {}", 
                                                                                            oldNode, 
                                                                                            new String ( oldNode.getData() ) , 
                                                                                            node.getPath(), new String ( node.getData() ) ))
                                                                .forDeletes(oldNode -> log.info("Node deleted. Old value: {}", 
                                                                                            oldNode.getPath(), new String ( oldNode.getData() ) ) )
                                                                .forInitialized( () -> log.info("Cache initialized"))
                                                                .build();
                    // register the listener                                
                    cache.listenable().addListener(listener);

                    // the cache must be started
                    cache.start();

                    // now randomly create/change/delete nodes
                    for (int i = 0; i < 2; i++) {

                        int dept = random.nextInt( 1, 4 );
                        String path = makeRandomPath(random, dept);

                        if ( random.nextBoolean() ) {

                            client.create().orSetData().creatingParentsIfNeeded().forPath(path, Long.toString( random.nextLong() ).getBytes() );
                        } else {
                            client.delete().quietly().deletingChildrenIfNeeded().forPath(path);
                        }

                        Thread.sleep(5);
                        
                    }
                    
                } catch (Exception e) {
                    // TODO: handle exception
                }
                
            } catch (Exception e) {
                // TODO: handle exception
            }
            
        } catch (Exception e) {
            // TODO: handle exception
        }
        Thread.sleep(5000);

        
    }

    private static String makeRandomPath( ThreadLocalRandom random, int depth ) {

        if ( depth ==0  ) {
            return PATH;
        } 
        return makeRandomPath( random, depth - 1 ) + "/" + random.nextInt(3);

    }
}
