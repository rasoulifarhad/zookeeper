package com.farhad.example.demo.zookeeper.self.tutorial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * A barrier is a primitive that enables a group of processes to synchronize the beginning and the end of a computation. 
 * 
 * The general idea of this implementation is to have a barrier node that serves the purpose of being a parent for 
 * individual process nodes.
 * 
 * Suppose that we call the barrier node "/b1".
 * 
 *   - Each process "p" then creates a node "/b1/p". 
 * 
 *   - Once enough processes have created their corresponding nodes, 
 *     joined processes can start the computation.
 * 
 * In this example, each process instantiates a Barrier object, and its constructor takes as parameters:
 * 
 *   - The address of a !ZooKeeper server (e.g., "zoo1.foo.com:2181");
 * 
 *   - The path of the barrier node on !ZooKeeper (e.g., "/b1");
 * 
 *   - The size of the group of processes.
 * 
 * To enter the barrier, a process calls enter().
 * 
 *   - The process creates a node under the root to represent it, using its 
 *     host name to form the node name. 
 * 
 *   - It then wait until enough processes have entered the barrier.
 * 
 *   -  A process does it by checking the number of children the root node has 
 *      with "getChildren()", and waiting for notifications in the case it does 
 *      not have enough. 
 * 
 *   - To receive a notification when there is a change to the root node, a 
 *     process has to set a watch, and does it through the call to 
 *     "getChildren()"
 * 
 * Once the computation is finished, a process calls leave() to leave the barrier. 
 * 
 *   - First it deletes its corresponding node, and then it gets the children of 
 *     the root node. 
 * 
 *   - If there is at least one child, then it waits for a notification.
 * 
 *   - Upon reception of a notification, it checks once more whether the root 
 *     node has any child.
 */
@Slf4j  
public class Barrier extends SyncPrimitive {
    
    void tt() {   

        
    }
    
    private int size ;
    private String name;
    
    public Barrier(String address)  { 
        super(address);

        log.info("Create barrier node");

        if ( zk != null ) {
            
            try {

                Stat stat = zk.exists(root, false) ;

                if ( stat == null ) {
    
                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    
                }
    
            } catch (KeeperException ex ) {
                log.error("Keeper exception when instantiating queue: {}", ex.toString());
            } catch (InterruptedException ex ) {
                log.error("Interrupted exception {}", ex.toString());

            }
        }

        // node name 
        try { 

            this.name = new String( InetAddress.getLocalHost().getCanonicalHostName().toString() );

        } catch ( UnknownHostException ex ) { 
            log.error(ex.toString());
        }
    }

    /**
     * 
     * @param address
     * @param name
     * @param size
     * @throws KeeperException
     * @throws IOException
     */
    public Barrier(String address, String name, int size) {
        super(address);
        this.root = name ;
        this.size = size;
        log.info("");
    }

    /**
     * Note that enter() throws both !KeeperException and !InterruptedException, so it is reponsibility of 
     * the application to catch and handle such exceptions.
     * @return
     */
    boolean enter() throws KeeperException, InterruptedException {

        zk.create( root + "/" + name, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        while ( true ) {

            synchronized ( mutex ) {

                List<String> children =  zk.getChildren(root, true);

                if ( children.size() < this.size ) {

                    mutex.wait();
 
                } else {

                    return true ;
                }
            }
        } 

    }

    /**
     * Wait until all reach barrier
     * 
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean leave() throws KeeperException, InterruptedException {

        zk.delete(root + "/" + name , 0);

        while ( true ) {

            synchronized ( mutex ) {

                List<String> children = zk.getChildren(root, true);

                if ( children.size() > 0 ) {

                    mutex.wait();
                } else {

                    return true;
                }

            }

        }

    }
    
}
