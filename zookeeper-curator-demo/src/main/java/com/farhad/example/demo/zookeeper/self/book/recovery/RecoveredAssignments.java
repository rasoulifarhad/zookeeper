package com.farhad.example.demo.zookeeper.self.book.recovery;


import java.util.ArrayList;
import java.util.List;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import lombok.extern.slf4j.Slf4j;

/**
 * Recover unassigned tasks.
 * 
 * This class implements a task to recover assignments after 
 * a primary master crash. The main idea is to determine the 
 * tasks that have already been assigned and assign the ones
 * that haven't 
 * 
 */
@Slf4j
public class RecoveredAssignments {
    
    // Various lists wew need to keep track of.

    List<String> tasks;
    List<String> assignments;
    List<String> statuses;
    List<String> activeWorkers;
    List<String> assignedWorkers;

    RecoveryCallback cb ;

    ZooKeeper zk ;

    public RecoveredAssignments( ZooKeeper zk ) {

        this.zk = zk; 
        this.assignments = new ArrayList<>();

    }

    // Starts recovery.
    public void recover( RecoveryCallback recoveryCallback ) {

        this.cb =  recoveryCallback ;

        // Read task list with getChildren
        getTasks();

    }

    private void getTasks() {
        zk.getChildren("/tasks", false, tasksCallback, null);
    }

    ChildrenCallback tasksCallback  = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getTasks();
                    break;
                case OK:
                    tasks = children;
                    getAssignedWorkers();
                    break;
                default:
                    log.error("getChildren failed {}",KeeperException.create(code, path));
                    cb.recoveryComplete(RecoveryCallback.FAILED, null);
                    break;
        }
        }
    };

    void getAssignedWorkers() {

        zk.getChildren("/assign", false, assignedWorkersCallback, null);

    }

    ChildrenCallback assignedWorkersCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getAssignedWorkers();
                    break;
                case OK:
                    assignedWorkers = children;
                    getWorkers(children);
                    break;
                default:
                    log.error("getChildren failed {}",KeeperException.create(code, path));
                    cb.recoveryComplete(RecoveryCallback.FAILED, null);
                    break;
        }
        }
    };

    private void getWorkers( Object ctx /* List<String> children */ ) {
        zk.getChildren("/workers", false, workersCallback, ctx);
    }

    ChildrenCallback workersCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getWorkers(ctx);
                    break;
                case OK:

                    log.info("Getting worker assignments for recovery: {}", children.size());   

                    // No worker available yet, so the master is probably let's just return an empty list.
                    if ( children.size() == 0 ) {
                        log.warn( "Empty list of workers, possibly just starting" );
                        cb.recoveryComplete(RecoveryCallback.OK, new ArrayList<>());
                        break;
                    }

                    //
                    // Need to know which of the assigned workers are active.
                    //
                    activeWorkers = children;
                    for(String s : assignedWorkers){
                        getWorkerAssignments("/assign/" + s);
                    }
                    break;
                default:
                    log.error("getChildren failed {}",KeeperException.create(code, path));
                    cb.recoveryComplete(RecoveryCallback.FAILED, null);
                    break;
        }
            
        }
        
    };

    // /assign/{worker}
    private void getWorkerAssignments(String workerAssignmentPath) {
        zk.getChildren(workerAssignmentPath, false, workerAssignmentsCallback, null);

    }

    ChildrenCallback workerAssignmentsCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getWorkerAssignments(path);
                    break;
                case OK:

                    String worker = path.replace("/assign/", "");

                    /*
                     * If the worker is in the list of active
                     * workers, then we add the tasks to the
                     * assignments list. Otherwise, we need to 
                     * re-assign those tasks, so we add them to
                     * the list of tasks.
                     */

                    if ( activeWorkers.contains( worker ) ) {
                          assignments.addAll(children);  
                    } else {

                        for ( String task : children ) {

                            if ( !tasks.contains(task) ) {

                                tasks.add( task );
                                getDataReassign( path, task );

                            } else {

                                /*
                                * If the task is still in the list
                                * we delete the assignment.
                                */
                                deleteAssignment( path + "/" + task );

                            }

                            /*
                             * Delete the assignment parent. 
                             */

                             deleteAssignment(path);
                        }
                    }

                    assignedWorkers.remove(worker);

                   /*
                    * Once we have checked all assignments,
                    *  it is time to check the status of tasks
                    */
                    if ( assignedWorkers.size() == 0 ) {

                        log.info("Getting statuses for recovery");
                        getStatuses();
                    }

                    break;

                case NONODE: 

                    log.info( "No such znode exists: {}", path );
                    break;

                default:
                    
                    log.error("getChildren failed {}",KeeperException.create(code, path));
                    cb.recoveryComplete(RecoveryCallback.FAILED, null);
                    break;
        }
            
        }
        
    };

    /**
     * Get data of task being reassigned.
     * @param path
     * @param task
     */
    void getDataReassign(String path, String task) {

        zk.getData(path, false, getDataReassignCallback, task);
    }

    DataCallback getDataReassignCallback = new DataCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getDataReassign(path, (String) ctx);
                    break;
                case OK: 
                    recreateTask( new RecreateTaskCtx( path, (String) ctx, data ) );
                    break;
                default:
                    log.error("Something went wrong when getting data  {}",KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };

    /**
     * Recreate task znode in /tasks
     * @param ctx Recreate text context
     * 
     */
    void recreateTask( RecreateTaskCtx ctx ) {

        zk.create(
                "/tasks/" + ctx.task, 
                 ctx.data,
                 ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                 CreateMode.PERSISTENT, 
                 recreateTaskCallback, 
                 ctx);
    }

    StringCallback recreateTaskCallback = new StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    recreateTask( (RecreateTaskCtx) ctx );
                    break;
                case OK: 
                    deleteAssignment( ( (RecreateTaskCtx) ctx ).path );
                    break;
                case NODEEXISTS:
                    log.warn("Node shouldn't exist: {}", path);
                default:
                    log.error("Something wwnt wrong when recreating task {}",KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };

    /**
     * Delete assignment of absent worker
     * 
     * @param path Path of znode to be deleted
     */
    void  deleteAssignment( String path )  {
        zk.delete(path, -1, taskDeletionCallback, null);
    }

    VoidCallback taskDeletionCallback = new VoidCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    deleteAssignment(path);
                    break;
                case OK: 
                    log.info("Task correctly deleted: {}", path);
                    break;
                default:
                    log.error("Failed to delete task data {}",KeeperException.create(code, path));
                    break;
            }
            
        }
        
    };

    void getStatuses() {
        zk.getChildren("/status", false, statusCallback, null);
    }

    ChildrenCallback statusCallback = new ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            Code code = Code.get(rc) ;
            switch( code ) {
                case CONNECTIONLOSS: 
                    getStatuses();
                    break;
                case OK: 
                    log.info("Processing assignments for recovery");
                    statuses = children;
                    processAssignments();
                    break;
                default:
                    log.error("getChildren failed {}",KeeperException.create(code, path));
                    cb.recoveryComplete(RecoveryCallback.FAILED, null);
                    break;
            }
            
        }
        
    };

    private  void processAssignments() {
        log.info("Size of tasks: {}", tasks.size());
        // Process list of pending assignments
        for( String assignment : this.assignments){

            log.info("Assignment: {}" , assignment);

            deleteAssignment(String.format("/tasks/%s", assignment));
            tasks.remove(assignment);
        }

        log.info("Size of tasks after assignment filtering: {}", tasks.size());

        for(String status : statuses ) {

            log.info( "Checking task: {} ", status );
            deleteAssignment(String.format("/tasks/%s", status));
            tasks.remove(status);
        }

        log.info("Size of tasks after status filtering: {}", tasks.size());

        // Invoke callback
        cb.recoveryComplete(RecoveryCallback.OK, tasks);
    }
}
