package com.farhad.example.demo.zookeeper.self;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
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
public class AsyncMaster implements Watcher, Runnable {

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

        String ctx = String.format(SERVER_CTX_PATTERN, serverId); 
        zk.create(MASTER_PATH, serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, 
                    new StringCallback() {

                        @Override
                        public void processResult(int rc, String path, Object ctx, String name) {
                            Code code = Code.get(rc);

                            switch ( code ) {

                                case OK:
            
                                    log.info("create master Ok");
                                    sleep(10);
                                    stopZK();
            
                                    break;
            
                                case NODEEXISTS:  
                                                
                                    log.info("Node  exists");            
                                    checkForMaster();
                                    break;
            
                                case SESSIONEXPIRED:  
                                       
                                    log.info("session expired in create");
                                    sleep(10);
                                    break;
            
                                default:  
                                    
                                    log.info("code is: {}", code);
                                    checkForMaster();
                            }
            
                        }

                    }, ctx);

    }

    public void checkForMaster(){

        DataCallback callback = new DataCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

                Code code = Code.get(rc);

                switch ( code ) {

                    case OK:

                        String masterId = new String(data);
                        if ( masterId.equals(serverId) ) {
                            log.info("stop now");
                            stopZK();
                        } else {
                            checkForMaster(); 
                        }

                        break;

                    case NONODE:  

                        log.info("Node not exists");            
                        createMasterNode();
                        break;

                    case NODEEXISTS:  
                                    
                        log.info("Node  exists");            
                        createMasterNode();
                        break;

                    case SESSIONEXPIRED:  
                           
                        log.info("session expired in check");
                        sleep(10);
                        break;

                    default:  
                        
                        log.info("code is: {}", code);
                        checkForMaster();
                }
                
            }
            
        };

        zk.getData(MASTER_PATH, true, callback, null);
    }

    public void registerForMaster(){
        checkForMaster();
    }

    @Override
    public void run() {
        
        startZK();

        registerForMaster();
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

    public static void main(String[] args) throws InterruptedException {
    }

    
}
