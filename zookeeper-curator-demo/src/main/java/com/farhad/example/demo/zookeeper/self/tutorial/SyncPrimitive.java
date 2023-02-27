package com.farhad.example.demo.zookeeper.self.tutorial;


import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * Both classes extend !SyncPrimitive.  barrier , queue
 * 
 * 
 * To keep the examples simple, we create a !ZooKeeper object the first time we instantiate either a barrier object or a 
 * queue object, and we declare a static variable that is a reference to this object.
 * 
 * Alternatively, we could have the application creating a !ZooKeeper object and passing it to the constructor of 
 * Barrier and Queue.
 * 
 * We use the process() method to process notifications triggered due to watches.
 * 
 * A watch is internal structure that enables !ZooKeeper to notify a client of a change to a node. 
 * 
 * For example, if a client is waiting for other clients to leave a barrier, then it can set a watch and wait for modifications 
 * to a particular node, which can indicate that it is the end of the wait. 
 */
@Slf4j  
public class SyncPrimitive  implements Watcher{
    
    static ZooKeeper zk = null;
    static  Integer mutex ;

    String root ;

    SyncPrimitive(String address) {

        if ( zk == null  ) {
            try {

                log.info("Starting ZK:");

                zk = new ZooKeeper(address, 3000, this);

                mutex = new Integer(-1);

                log.info("Finished starting ZK: {}", zk);
    
            } catch( IOException ex ) {
                log.error(ex.toString());
                zk = null ;
            }
        }

    }

    @Override
    public void process(WatchedEvent event) {

        synchronized ( mutex ) {
            mutex.notify();
        }
    } 

}
