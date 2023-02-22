package com.farhad.example.demo.zookeeper.self;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
/**
 * Test
 *  1. create 'cluster-group' group (PERSISTENT): create_znode_test()  /cluster-group
 *  2. join members                 (EPHEMERAL) : join()               /cluster-group/{memberName}
 */
@Slf4j
public class ZookeeperTest {
    
    private static final String GROUP_NAME = "cluster-group";

    private static final String MEMBER_NO_01 = "memberNo01";
    private static final String MEMBER_PEREFIX = "memberNo_";

    @Test
    public void connect_test() throws Exception {

        ZooKeeper zk = connect();

        log.info("Connected!");
        zk.close();

    }

    @Test
    public void create_znode_test() throws Exception {
        ZooKeeper zk = connect() ;
        String path = "/" + GROUP_NAME ;

        String createdPath = zk.create( path, 
                                        "this is node data".getBytes(), 
                                        ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                                        CreateMode.PERSISTENT );

        log.info("Created: {}",createdPath);
        zk.close();

    }

    @Test
    public void join_test() throws Exception {

        ZooKeeper zk = this.join(GROUP_NAME, MEMBER_NO_01);
        
        zk.close();
    }

    @Test 
    public void list_child_test() throws Exception {

        //Given
        List<ZooKeeper> zks =  
                        Arrays.asList( 
                                    this.join(GROUP_NAME, MEMBER_PEREFIX + "01"),
                                    this.join(GROUP_NAME, MEMBER_PEREFIX + "02"),
                                    this.join(GROUP_NAME, MEMBER_PEREFIX + "03"),
                                    this.join(GROUP_NAME, MEMBER_PEREFIX + "04"));

        // When
        List<String> children =  list(GROUP_NAME);
        log.info("Children: {}",children);

        zks.forEach( zk -> {
                        try {
                            zk.close();
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
        });

        
    }

    @Test
    public void delete_test() throws Exception {
        //Given
        List<ZooKeeper> zks =  
        Arrays.asList( 
                    this.join(GROUP_NAME, MEMBER_PEREFIX + "01"),
                    this.join(GROUP_NAME, MEMBER_PEREFIX + "02"),
                    this.join(GROUP_NAME, MEMBER_PEREFIX + "03"),
                    this.join(GROUP_NAME, MEMBER_PEREFIX + "04"));

        delete(GROUP_NAME);        
   
        zks.forEach( zk -> {
            try {
                zk.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });
    }
 
    private ZooKeeper connect() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("localhost:2181", 2000, new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            }
            
        }) ;
        latch.await();
        log.info("Connected!");
        return zk ;
    }

    private ZooKeeper join(String groupName,String memberName) throws Exception {
        ZooKeeper zk = connect() ;
        
        String path = "/" + groupName  + "/" + memberName;
        String createdPath = zk.create( path, 
                                        "this is a member".getBytes(), 
                                        ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                                        CreateMode.EPHEMERAL );
        log.info("Member Created: {}" ,createdPath);
        return zk;

    }

    private List<String> list(String groupName) throws Exception {

        ZooKeeper zk = connect() ; 
        String path = "/" + groupName ;
        try {
            return  zk.getChildren(path, false) ;
    
        } catch(KeeperException.NoNodeException ex) {
            log.info( "Group {} doze not exists." , groupName );
            return Collections.<String>emptyList();
            
        } finally {
            zk.close();
        }

    }

    private void delete(String groupName) throws Exception {

        String path = "/" + groupName ;

        ZooKeeper zk = connect() ;

        try {
            List<String> children  = list(GROUP_NAME);

            for( String child : children ) {
                zk.delete(path + "/" + child, -1);
            }
            zk.delete(path, -1);

        } catch(Exception ex) {
            throw ex ;
        } finally {
            zk.close();
        }

    }

    
}
