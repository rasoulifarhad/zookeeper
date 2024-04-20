package com.farhad.example.demo.zookeeper.self;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * Very simple test class which attempts to exercise the removeWatches feature added in 3.5.0
 */
@Slf4j
public class ExerciseRemoveWatch implements Watcher {
    
    private static final boolean USE_REMOVE_CALL = true;

    private TestingServer server ;
    private ZooKeeper zk  ;
    private String rootZNode ;
    private CountDownLatch zkAvail ;

    
    public ExerciseRemoveWatch() throws Exception {

        zkAvail = new CountDownLatch(1);
        server = new TestingServer();
        zk = new ZooKeeper(server.getConnectString(), 30, this);
        zkAvail.await(30, TimeUnit.SECONDS);

        rootZNode = zk.create("/",null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * Exercise the functionality of remove watches added in ZooKeeper 3.5.0
     * 
     * Print some basic mem details to make sure we're in the ballpark
     * @throws InterruptedException
     * @throws KeeperException
     */    
    public void createAndRemoveLotsOfWatches() throws InterruptedException, KeeperException {

        final int maxIter = 100000;
		final int maxLoops = 10000;

        log.info("USE_REMOVE_CALL: {}", USE_REMOVE_CALL);
        log.info("maxIter: {}", maxIter);
        log.info("maxLoops: {}", maxLoops);

        Runtime rt = Runtime.getRuntime() ;

        rt.gc();
        rt.gc();
        rt.gc();

        log.info("Available free mem is {}", rt.freeMemory());

        for (int j = 0; j < maxIter; j++) {
            
            log.info("j: {}", j);
            IgnoringWatcher ignoringWatcher = new IgnoringWatcher();

            for (int i = 0; i < maxLoops; i++) {
                String path = String.format("%s/e-%010d-%010d", rootZNode, j, i);
                zk.exists(path, ignoringWatcher);
                if ( USE_REMOVE_CALL ) {
                    zk.removeWatches(path, ignoringWatcher, WatcherType.Any, true);
                }      
            }
                rt.gc();
            rt.gc();
            rt.gc();
            log.info("Available free mem is {}", rt.freeMemory());
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if ( event.getState() == Event.KeeperState.SyncConnected ) {
            zkAvail.countDown();
        }
    } 


    private static class IgnoringWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            // TODO Auto-generated method stub
            
        }
        
    }

    public static void main(String[] args) throws Exception {
        
        ExerciseRemoveWatch exerciseRemoveWatch = new ExerciseRemoveWatch() ;
        exerciseRemoveWatch.createAndRemoveLotsOfWatches();         
    }
    
}
