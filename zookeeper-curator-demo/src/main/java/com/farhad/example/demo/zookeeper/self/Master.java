package com.farhad.example.demo.zookeeper.self;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.KeeperException.Code;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Master implements  Watcher, Runnable {
    
    private static final String MASTER_PATH = "/master";
    private static final String SERVER_CTX_PATTERN = "ctx for %s";

    private final String connectString;
    private final String serverId ;

    private ZooKeeper zk ;
    
    public void startZK() {

        try{
            zk = new ZooKeeper(connectString, 2000, this);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopZK() {
        try{
            zk.close();
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void createMasterNode() {

        try{
            zk.create(MASTER_PATH, serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch( KeeperException | InterruptedException ex ) {
            ex.printStackTrace();
        }
    }

    public boolean checkForMaster(){

        Stat stat = new Stat();
        byte[] data = null ;

        try{

            data = zk.getData(MASTER_PATH, false, stat);
            String currentLeader = new String(data);

            return serverId.equals( currentLeader );

        } catch( KeeperException | InterruptedException ex ) {

            ex.printStackTrace();

        }

        return false;

    }

    public boolean registerForMaster(){

        boolean isLeader = false; 

        while ( true ) {
            if( !checkForMaster() ) {
                
                createMasterNode();

                sleep(5);
            } else {

                isLeader = true ;
                log.info(" master registered with id: {} ",serverId);
                break;
            }
        }
        return isLeader;
    }

    @Override
    public void run() {
        
        startZK();

        boolean isLeader = registerForMaster();

        if ( isLeader ) {
            stopZK();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        log.info("Event: {}", event);
    }

    
    private static void sleep(int seconds) {
        try{
            TimeUnit.SECONDS.sleep(seconds);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
