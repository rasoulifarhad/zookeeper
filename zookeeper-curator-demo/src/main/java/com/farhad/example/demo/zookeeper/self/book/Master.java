package com.farhad.example.demo.zookeeper.self.book;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import com.farhad.example.demo.zookeeper.self.book.recovery.RecreateTaskCtx;
import com.farhad.example.demo.zookeeper.self.book.recovery.RecoveredAssignments;
import com.farhad.example.demo.zookeeper.self.book.recovery.RecoveryCallback;
import org.apache.zookeeper.data.Stat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Master implements Watcher, Closeable{
    
    private static final String WORKERS_PATH = "/workers";
    private static final String TASKS_PATH   = "/tasks";
    private static final String ASSIGN_PATH  = "/assign";
    private static final String STATUS_PATH  = "/status";
    
    // /assign/{worker}/{task}
    private static final String ASSIGNMENT_PATTERN = "/assign/%s/%s" ;

    private final String hostPort;

    private volatile MasterStates state = MasterStates.RUNNING;
    
    private Random random = new Random(this.hashCode());
    ZooKeeper zk ;
    private String serverId  = Integer.toHexString( random.nextInt() );
    private volatile boolean connected = false ;
    private volatile boolean expired = false ;
    
    protected ChildrenCache tasksCache ;
    protected ChildrenCache workersCache;

    MasterStates getState() {
        return this.state;
    }

    void startZK() throws IOException  {
        zk = new ZooKeeper(hostPort, 15000, this);
    }

    void stopZK() throws InterruptedException, IOException {
        zk.close();
    }

    /**
     * We use it to deal with the different states of a session. 
     */
    @Override
    public void process(WatchedEvent event) {

        log.info("Processing event: {}", event.toString());
        if ( event.getType() == Event.EventType.None ) {

            switch( event.getState() ) {

                case SyncConnected:
                    this.connected = true;
                    break;
                case Disconnected:
                    this.connected = false;
                    break;
                case Expired:
                    this.expired = true;
                    this.connected = false;
                    log.error( "Session expiration" );
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This method creates some parent znodes we need.
     * In the case the master is restarted, then this method does not
     * need to be executed a second time.
     */
    public void bootstrap() {

        createParent( "/workers", new byte[0] ); 
        createParent( "/assign",  new byte[0] ); 
        createParent( "/tasks",   new byte[0] ); 
        createParent( "/status",  new byte[0] ); 
    }

    void createParent( String path, byte[] data ) {

        zk.create(
                    path, 
                    data, 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                    CreateMode.PERSISTENT,
                    createParentCallback,
                    data);
    }

    StringCallback  createParentCallback = new StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
    
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:
                    createParent( path, ( byte[] ) ctx );
                    break;
                case OK:
                    log.info("Parent created: {}", path);
                    break;
                case NODEEXISTS: 
                    log.info("Parent already registered: {}", path);
                    break;
                default:
                    log.error("Something went wrong: {}", KeeperException.create(code, path));
                    break;
            }
        }
    };

    boolean isConnected() {
        return this.connected ;
    }

    boolean isExpired() {
        return this.expired;
    }

        /*
     **************************************
     **************************************
     * Methods related to master election.*
     **************************************
     **************************************
     */
    
    
   /*
    * The story in this callback implementation is the following.
    * We tried to create the master lock znode. If it suceeds, then
    * great, it takes leadership. However, there are a couple of
    * exceptional situations we need to take care of. 
    * 
    * First, we could get a connection loss event before getting
    * an answer so we are left wondering if the operation went through.
    * To check, we try to read the /master znode. If it is there, then
    * we check if this master is the primary. If not, we run for master
    * again. 
    * 
    *  The second case is if we find that the node is already there.
    *  In this case, we call exists to set a watch on the znode.
    */


 
    void masterExists() {
        zk.exists("/master", masterExistsWatcher, masterExistsCallback, null);

    }

    StatCallback masterExistsCallback = new StatCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
                
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:
                    masterExists();
                    break;
                case OK:
                    break;
                case NONODE: 
                    state = MasterStates.RUNNING;
                    runForMaster();
                    log.info("It sounds like the previous master is gone, so let's run for master again."); 
                    break;
                default:
                    checkMaster();
                    break;
            }
        }
        
    };
    
    Watcher masterExistsWatcher = new Watcher() {

        @Override
        public void process(WatchedEvent event) {
            if ( event.getType() == Event.EventType.NodeDeleted ) {
                assert "/master".equals(event.getPath()) ;
                
                runForMaster();
                
            }           
        }
        
    };

    void takeLeadership() {
        log.info("Going for list of workers");

        getWorkers() ;

        recover() ;


    }


    private void recover() {

        RecoveredAssignments recoveredAssignments = new RecoveredAssignments(zk);
        recoveredAssignments.recover(new RecoveryCallback() {

            @Override
            public void recoveryComplete(int rc, List<String> tasks) {
                
                if ( rc ==  RecoveryCallback.FAILED) {
                    log.error("Recovery of assigned tasks failed.");
                    
                } else {
                    log.info( "Assigning recovered tasks" );
                    getTasks();
                }   
            }
            
        });

    }
    /*
     * Run for master. To run for master, we try to create the /master znode,
     * with masteCreateCallback being the callback implementation. 
     * In the case the create call succeeds, the client becomes the master.
     * If it receives a CONNECTIONLOSS event, then it needs to check if the 
     * znode has been created. In the case the znode exists, it needs to check
     * which server is the master.
     */
    
    /**
     * Tries to create a /master lock znode to acquire leadership.
     */
    public void runForMaster() {

        log.info("Running for master");

        zk.create(
                    "/master", 
                    serverId.getBytes(), 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                    CreateMode.EPHEMERAL, 
                    masterCreateCallback, 
                    null);
    }

    StringCallback masterCreateCallback = new StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {

            Code code = Code.get(rc);

            switch( code ) {

                case CONNECTIONLOSS:

                    checkMaster();
                    break ;

                case OK:

                    state = MasterStates.ELECTED;
                    takeLeadership();
                    break ;

                case NODEEXISTS:

                    state = MasterStates.NOTELECTED;
                    masterExists();
                    break;

                default:

                    state = MasterStates.NOTELECTED;
                    log.error("Something went wrong when running for master. {}", KeeperException.create(code, path));

            }

            log.info( "I'm {} the leader {}", (state == MasterStates.ELECTED ? "" : "not "), serverId );
        }
        
    };


    void checkMaster() {

        zk.getData("/master", false, masterCheckCallback, null);
    }

    DataCallback masterCheckCallback = new DataCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    checkMaster();
                    break;

                case OK:

                    if ( serverId.equals( new String( data ) ) ) {

                        state = MasterStates.ELECTED;
                        takeLeadership();

                    } else {

                        state = MasterStates.NOTELECTED;
                        masterExists();

                    }

                    break;

                case NONODE: 

                    runForMaster();
                    break;

                default:

                    log.error("Error when reading data. {}", KeeperException.create(code, path));
                    break;
            }
        }
        
    };


        /*
     ****************************************************
     **************************************************** 
     * Methods to handle changes to the list of workers.*
     ****************************************************
     ****************************************************
     */


    /**
     * This method is here for testing purposes.
     * @return size Size of the worker list
     */ 
    public int getWorkersSize() {
        if (this.workersCache == null) {
            return 0;
        } else {
            return this.workersCache.getList().size();
        }
    }

    void getWorkers() {
        zk.getChildren("/workers", workersChangeWatcher, workersGetChildrenCallback, null);
    }

    Watcher workersChangeWatcher = new Watcher() {

        @Override
        public void process(WatchedEvent event) {
            if ( event.getType() == Event.EventType.NodeChildrenChanged ) {
                assert "/workers".equals(event.getPath());
                getWorkers();
            }
        }
        
    };

    ChildrenCallback  workersGetChildrenCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    getWorkers(); 
                    break;

                case OK:

                    log.info("Succesfully got a list of workers: {} workers", children.size());
                    reassignAndSet(children);
                    break;

                    default:

                    log.error("getChildren failed {}", KeeperException.create(code, path));
                    break;
            }
        }
        
    };

   /*
    *******************
    *******************
    * Assigning tasks.*
    *******************
    *******************
    */


    void reassignAndSet(List<String> children) {

        List<String> toProcess;

        if( this.workersCache == null  ) {
            this.workersCache = new ChildrenCache(children);
            toProcess = null ;
        } else {
            log.info( "Removing and setting" );
            toProcess = this.workersCache.removedAndSet(children);
        }
        
        if ( toProcess != null ) {

            for ( String worker : toProcess ) {
                getAbsentWorkerTasks(worker);
            }
        }
    }

    void getAbsentWorkerTasks(String worker) {
        zk.getChildren(String.format("/assign/%s", worker), false, workerAssignmentCallback, null);
    }


    ChildrenCallback workerAssignmentCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    getAbsentWorkerTasks(path); 
                    break;

                case OK:

                    log.info("Succesfully got a list of assignments: {} tasks", children.size());

                    /**
                     * Reassign the tasks of the absent worker.  
                     */
                    for( String task : children ) {
                        // /assign/{worker}/{task}
                        getDataReassign(path + "/" + task, task);
                    }

                    break;

                default:

                    log.error("getChildren failed {}", KeeperException.create(code, path));
                    break;
            }
        }
    }; 


     /*
     ************************************************
     * Recovery of tasks assigned to absent worker. * 
     ************************************************
     */
    
    /**
     * Get reassigned task data.
     * 
     * 
     * 
     * @param path Path of assigned task  /assign/{worker}/{task}
     * @param task Task name excluding the path prefix
     */
    void getDataReassign(String path, String task) {

        zk.getData(path, false, getDataReassignCallback, task);
    }


    /**
     * Get task data reassign callback.
     */
    DataCallback getDataReassignCallback = new DataCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data,Stat stat) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    getDataReassign(path, ( String ) ctx); 
                    break;

                case OK:

                    recreateTask( new RecreateTaskCtx(path, ( String ) ctx, data) );
                    break;

                default:

                    log.error("Something went wrong when getting data {}", KeeperException.create(code, path));
                    break;
            }
        }
        
    };

    /**
     * Recreate task znode in /tasks
     * 
     * @param ctx Recreate text context
     */
    void recreateTask(RecreateTaskCtx ctx) {

        zk.create(
                    "/tasks/" + ctx.getTask(), 
                    ctx.getData(), 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                    CreateMode.PERSISTENT, 
                    recreateTaskCallback, 
                    ctx);
    }

    //Recreate znode callback

    StringCallback recreateTaskCallback = new StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    recreateTask( ( RecreateTaskCtx ) ctx); 
                    break;

                case OK:

                    deleteAssignment(  ( ( RecreateTaskCtx ) ctx ).getPath()  );
                    break;

                case NODEEXISTS:

                    log.info("Node exists already, but if it hasn't been deleted, then it will eventually, so we keep trying: {}", path);
                    recreateTask( ( RecreateTaskCtx ) ctx);
                    break;

                default:

                    log.error("Something wwnt wrong when recreating task {}", KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };

     /**
     * Delete assignment of absent worker
     * 
     * @param path Path of znode to be deleted
     */
    void deleteAssignment(String path){

        zk.delete(path, -1, taskDeletionCallback, null);
    }

    VoidCallback taskDeletionCallback = new VoidCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                deleteAssignment( path ); 
                    break;

                case OK:

                    log.info("Task correctly deleted: {}", path);
                    break;

                default:

                    log.error("Failed to delete task data {}", KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };

    /*
     ******************************************************
     ******************************************************
     * Methods for receiving new tasks and assigning them.*
     ******************************************************
     ******************************************************
     */

    void getTasks(){

        zk.getChildren("/tasks", tasksChangeWatcher, tasksGetChildrenCallback, null);
    }

    Watcher tasksChangeWatcher = new Watcher() {

        public void process(WatchedEvent event) {

            if(event.getType() == Event.EventType.NodeChildrenChanged) {

                assert "/tasks".equals( event.getPath() );
                
                getTasks();
            }
        }
    };

     ChildrenCallback tasksGetChildrenCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    getTasks(); 
                    break;

                case OK:

                    List<String> toProcess;
                    if( tasksCache  == null) {

                        tasksCache = new ChildrenCache(children);
                        toProcess = children;

                    } else {

                        toProcess = tasksCache.addedAndSet( children );
                    }
                    
                    if( toProcess != null ) {

                        assignTasks(toProcess);
                    } 

                    break;

                default:

                    log.error("getChildren failed {}", KeeperException.create(code, path));
                    break;
            }
            
        }
        
     };


     void assignTasks( List<String> tasks ) {

        for ( String task : tasks ) {

            getTaskData( task );
        }
     }

     void getTaskData(String task) {

        zk.getData(
                task, 
                false, 
                taskDataCallback, 
                task);
     }

     DataCallback taskDataCallback = new DataCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            Code code = Code.get(rc);
            switch( code ) {
 
                case CONNECTIONLOSS:

                    getTaskData( ( String ) ctx );
                    break;

                case OK:

                    // Choose worker at random.   
                    List<String> candidWorkers = workersCache.getList() ;
                    String designatedWorker = candidWorkers.get( random.nextInt( candidWorkers.size() ) );

                    // Assign task to randomly chosen worker.

                    String assignmentPath = "/assign/" + designatedWorker + "/" + ( String ) ctx;
                    log.info( "Assignment path: " + assignmentPath );
                    createAssignment( assignmentPath, data ) ;
                    break;

                default:

                    log.error("Error when trying to get task data. {}", KeeperException.create(code, path));
                    break;
            }
            
        }
        
     };

    void createAssignment(String path, byte[] data) {

        zk.create(
            path, 
            data, 
            ZooDefs.Ids.OPEN_ACL_UNSAFE, 
            CreateMode.PERSISTENT, 
            assignTaskCallback, 
            data);
    }

    StringCallback assignTaskCallback = new StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            Code code = Code.get(rc);
            switch( code ) {

                case CONNECTIONLOSS:

                    createAssignment( path, ( byte[] ) ctx); 
                    break;

                case OK:

                    log.info("Task assigned correctly: {}", name);
                    deleteTask( name.substring( name.lastIndexOf("/") + 1 ) ) ; 
                    break;

                case NODEEXISTS:

                    log.warn("Task already assigned");
                    break;

                default:

                    log.error("Error when trying to assign task. {}", KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };



    /*
     * Once assigned, we delete the task from /tasks
     */
    void deleteTask(String task){

        zk.delete("/tasks/" + task, -1, taskDeleteCallback, null);
    }

    VoidCallback taskDeleteCallback = new VoidCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx) {

            switch (Code.get(rc)) {
                case CONNECTIONLOSS:
                    deleteTask(path);
                    
                    break;
                case OK:
                    log.info("Successfully deleted {}", path);
                    
                    break;
                case NONODE:
                    log.info("Task has been deleted already");
                    
                    break;
                default:
                    log.error("Something went wrong here, {}", KeeperException.create(Code.get(rc), path));
                }            
        }
        
    };

    @Override
    public void close() throws IOException {
        if(zk != null) {
            try{
                zk.close();
            } catch (InterruptedException e) {
                log.warn( "Interrupted while closing ZooKeeper session.", e );
            }
        }        
    }


        /**
     * Main method providing an example of how to run the master.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception { 
        Master m = new Master(args[0]);
        m.startZK();
        
        while(!m.isConnected()){
            Thread.sleep(100);
        }
        /*
         * bootstrap() creates some necessary znodes.
         */
        m.bootstrap();
        
        /*
         * now runs for master.
         */
        m.runForMaster();
        
        while(!m.isExpired()){
            Thread.sleep(1000);
        }   

        m.stopZK();
    }    
    
}
