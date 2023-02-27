package com.farhad.example.demo.zookeeper.self.book;


import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.KeeperException.Code;

@Slf4j
public class Client  implements Watcher, Closeable {
    
    private static final String TASK_STATUS_PREFIX = "/status/task-";
    private static final String TASK_STATUS_PATTERN = TASK_STATUS_PREFIX + "%s";
    private static final String TASK_NAME_PREFIX = "/tasks/task-";
    private static final String TASK_NAME_PATTERN = TASK_NAME_PREFIX + "%s";
    
    ZooKeeper zk ;
    String hostPort;

    volatile boolean connected = false ;
    volatile boolean expired = false;
    
    Client( String hostPort ) {
        this.hostPort = hostPort ;
    }

    public void startZK() throws IOException {
        zk = new ZooKeeper(hostPort, 1500, this);
    }
    
    @Override
    public void process(WatchedEvent event) {
        
        log.info("{}", event);
        if ( event.getType() ==  Event.EventType.None) {

            switch ( event.getState() ) { 

                case SyncConnected:

                connected = true ;
                break;
                case Disconnected:

                connected = false ;
                break;
                case Expired:

                expired = true;
                connected = false ;
                log.info(" Exiting due to session expiration ");

                break;

                default:
                   
                break;
            }
        }
        
    }

    boolean isConnected() {
        return connected;
    }

    boolean isExpired()  {
        return this.expired; 
    }

    /**
     * Executes a task and watches for the result 
     */
    void submitTask(String task, TaskObject taskCtx) {
        taskCtx.setTask(task);
        zk.create("/tasks/task-",
                         task.getBytes(), 
                         ZooDefs.Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT_SEQUENTIAL,
                         createTaskCallback,
                         taskCtx);
    }

    StringCallback createTaskCallback = new StringCallback() { 

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:
                    /*
                    * Handling connection loss for a sequential node is a bit
                    * delicate. Executing the ZooKeeper create command again
                    * might lead to duplicate tasks. For now, let's assume
                    * that it is ok to create a duplicate task.
                    */
                    submitTask( (( TaskObject ) ctx ).getTask(), (( TaskObject ) ctx ) );
                    break;

                case OK:

                    log.info("Task Created: {}", name);
                    (( TaskObject ) ctx ).setTaskName( name );
                    watchStatus( name.replace("/tasks/", "/status/"), ctx );
                    break;

                default:
                    log.error( "Something went wrong {}", KeeperException.create(code, path) );
            }
            
        }
        
    };

    protected ConcurrentHashMap<String, Object> ctxMap = new ConcurrentHashMap<String, Object>();

    void watchStatus( String path, Object ctx ) {
        ctxMap.put(path, ctx);
        zk.exists(path, 
                    statusWatcher,
                    existsCallback,
                    ctx);

    }

    Watcher statusWatcher = new Watcher() {

        @Override
        public void process(WatchedEvent event) {
            if ( event.getType() == Event.EventType.NodeCreated ) {
                assert event.getPath().contains(TASK_STATUS_PREFIX);
                assert ctxMap.containsKey(event.getPath());

                zk.getData(event.getPath(), 
                            false, 
                            getDataCallback, 
                            ctxMap.get(event.getPath()));
            }
        }
        
    };

    StatCallback existsCallback = new StatCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:
                    watchStatus( path, ctx );
                    break;

                case OK:
                    if ( stat != null ) {
                        zk.getData(path, false, getDataCallback, ctx);
                        log.info("Statuc node is there: {}",path);
                    }
                    break;
                
                case NONODE:
                    break;

                default:
                    log.error( "Something went wrong when checking if the status node exists: {}", KeeperException.create(code, path) );
                    break;
            }
        }
    };

    DataCallback getDataCallback =  new DataCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    // try again
                    zk.getData(path, false, getDataCallback, ctxMap.get(path));
                    break;

                case OK:

                    // Print result

                    String taskResult = new String(data);
                    log.info("Task: {} , TaskResult: {}", path, taskResult);

                    // Setting the status of the task

                    assert (ctx != null);
                    ( ( TaskObject ) ctx ).setStatus( taskResult.contains("done") );

                    // Delete status znode
                    zk.delete(path, -1, taskDeleteCallback, null);
                    ctxMap.remove(path);

                    break;
                
                case NONODE:

                    log.warn( "Status node is gone!" );
                    break;

                default:

                    log.error( "Something went wrong here, {}", KeeperException.create(code, path) );
                    break;
            }
            
        }
        
    };

    VoidCallback taskDeleteCallback = new VoidCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    // try again
                    zk.delete(path, -1, taskDeleteCallback, null);
                    break;

                case OK:

                    log.info("Successfully deleted {}", path);
                    break;
                
                default:

                    log.error( "Something went wrong here, {}", KeeperException.create(code, path) );
                    break;
            }
            
        }
        
    };


    @Override
    public void close() throws IOException {

        log.info("Closing");
        try{
            zk.close();
        } catch (InterruptedException e) {
            log.warn("ZooKeeper interrupted while closing");
        }

    }

    

    @Slf4j
    public static class TaskObject {
        @Getter
        @Setter
        private String task;

        @Getter
        @Setter
        private String taskName ;

        @Getter
        private boolean done = false ;

        @Getter
        private boolean succesful = false ;

        private CountDownLatch latch = new CountDownLatch(1);

        void setStatus(boolean status) {
            
            this.succesful = status;
            this.done = true;
            this.latch.countDown();
        }

        void waitUntilDone() {
            try{
                this.latch.await();
            } catch ( InterruptedException ex ) {
                log.warn("InterruptedException while waiting for task to get done");
            }
        }
    }


    public static void main(String args[]) throws Exception { 
        Client c = new Client(args[0]);
        c.startZK();
        
        while(!c.isConnected()){
            Thread.sleep(100);
        }   
        
        TaskObject task1 = new TaskObject();
        TaskObject task2 = new TaskObject();
        
        c.submitTask("Sample task", task1);
        c.submitTask("Another sample task", task2);
        
        task1.waitUntilDone();
        task2.waitUntilDone();
    }
    
}
