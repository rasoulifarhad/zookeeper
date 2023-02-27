package com.farhad.example.demo.zookeeper.self.tutorial;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import java.util.List;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * Producer-Consumer Queues
 * 
 * A producer-consumer queue is a distributed data structure that group of processes use to generate and 
 * consume items. 
 * 
 * Producer processes create new elements and add them to the queue. 
 * 
 * Consumer processes remove elements from the list, and process them.
 * 
 * In this implementation, 
 * 
 *    - the elements are simple integers. 
 * 
 *    - The queue is represented by a root node, and 
 * 
 *    - to add an element to the queue, a producer process creates a 
 *      new node, a child of the root node.
 * 
 * A producer process calls "produce()" to add an element to the queue, and passes an 
 * integer as an argument. 
 * 
 * To add an element to the queue, 
 * 
 *   - the method creates a new node using "create()", and 
 * 
 *   - uses the SEQUENCE flag to instruct !ZooKeeper to append the value 
 *     of the sequencer counter associated to the root node. 
 * 
 * In this way, we impose a total order on the elements of the queue, thus guaranteeing that 
 * the oldest element of the queue is the next one consumed.
 * 
 * To consume an element, 
 * 
 *   - a consumer process obtains the children of the root node, 
 *   - reads the node with smallest counter value, and 
 *   - returns the element. 
 * 
 * Note that if there is a conflict, then one of the two contending processes won't be 
 * able to delete the node and the delete operation will throw an exception.
 * 
 * A call to getChildren() returns the list of children in lexicographic order. 
 * 
 * As lexicographic order does not necessary follow the numerical order of the counter 
 * values, we need to decide which element is the smallest.
 * 
 * To decide which one has the smallest counter value, 
 * 
 *   - we traverse the list, and 
 *   - remove the prefix "element" from each one.
 * 
 * Queue test
 * 
 *   Start a producer to create 100 elements
 * 
 *    java SyncPrimitive qTest localhost 100 p
 * 
 * Barrier test
 * 
 *   Start a barrier with 2 participants (start as many times as many participants you'd like to enter)
 * 
 *    java SyncPrimitive bTest localhost 2
 */
@Slf4j  
public class Queue extends SyncPrimitive {
    
    void tt() { 
        
    }

    public Queue(String address)  {
        super(address);
        log.info("");
    }

    public Queue(String address, String name) {

        super(address);

        this.root = name ;

        // Create zk node  name 
        if ( zk != null ) {

            try {

                Stat stat = zk.exists(root, false);

                if ( stat == null ) {

                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }

            } catch (KeeperException ex ) {
                log.error("Keeper exception when instantiating queue: {}", ex.toString());
            } catch (InterruptedException ex ) {
                log.error("Interrupted exception {}", ex.toString());

            }
        }
    }

    /**
     * Add element to the queue.
     * @param i
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean produce( int i ) throws KeeperException, InterruptedException{

        ByteBuffer b = ByteBuffer.allocate(4);

        byte[] value ;

        // Add child with value i
        b.putInt(i);
        value = b.array();

        zk.create(root + "/element" , value, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

        return true ;
    }

    /**
     *  Remove first element from the queue.
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    int consume() throws KeeperException, InterruptedException {

        int retVal = -1 ;

        Stat stat = null ;

        // Get the first element available
        while ( true )  {

            synchronized ( mutex ) {

                List<String> children = zk.getChildren(root, true);

                if ( children.isEmpty() ) {

                    log.info("Goint to wait ");
                    mutex.wait();

                } else {

                    Integer min = new Integer( children.get( 0 ).substring( 7 ) );

                    for ( String child : children ) {

                        Integer childValue = new Integer( child.substring( 7 ) );

                        if ( childValue < min ) {

                            min = childValue;

                        }

                        log.info("Temporary value: {}/element{}", root, min);

                        byte[] data = zk.getData(root + "/element" + min, false, stat);

                        zk.delete(root + "/element" + min, 0);

                        ByteBuffer buffer = ByteBuffer.wrap(data);

                        retVal = buffer.getInt();

                        return retVal;
                    }

                }

            }
        }

    }
    

    
}
