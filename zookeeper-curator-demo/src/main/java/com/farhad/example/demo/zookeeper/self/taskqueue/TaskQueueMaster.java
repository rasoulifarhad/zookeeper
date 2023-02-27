package com.farhad.example.demo.zookeeper.self.taskqueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskQueueMaster implements Watcher {
    

    HashSet<String> tasks = new HashSet<>();
    HashSet<String> workers = new HashSet<>();
    HashMap<String,String> workerToAssignment = new HashMap<>();
    HashMap<String,String> assignmentToWorker = new HashMap<>();

    AtomicLong eventCounter = new AtomicLong();

    ZooKeeper zk ;

    private void schedule() throws InterruptedException  {

        while( true ) {

            long count = eventCounter.get() ;

            synchronized( this ) {

                HashSet<String> availableWorkers = new HashSet<String>(workers);
				HashSet<String> unassignedTasks = new HashSet<String>(tasks);
				LinkedList<Entry<String, String>> assignmentsToRemove = new LinkedList<Entry<String,String>>();

				// check for dead workers and finished assignments
			    for(Entry<String, String> assignment: workerToAssignment.entrySet()) {
					if (!workers.contains(assignment.getKey()) || !tasks.contains(assignment.getValue())) {
						assignmentsToRemove.add(assignment);
					} else {
						availableWorkers.remove(assignment.getKey());
						unassignedTasks.remove(assignment.getValue());
					}
				}

                // we have to delete the dead worker assignments outside the iterator
				// to avoid the concurrent modification exception
                for(Entry<String, String> assignment: assignmentsToRemove) {

					workerToAssignment.remove(assignment.getKey());
					assignmentToWorker.remove(assignment.getValue());
				}

                Iterator<String> workerIterator = availableWorkers.iterator();
				Iterator<String> taskIterator = unassignedTasks.iterator();

                while( workerIterator.hasNext() && taskIterator.hasNext() ) {

					String worker = workerIterator.next();
					String task = taskIterator.next();

					try {

						// TODO: make the assignment to worker

						workerToAssignment.put(worker, task);

						assignmentToWorker.put(task, worker);

					// } catch(KeeperException e) {
					} catch(Exception e) {
						// no big deal, we'll clean up the worker later
						eventCounter.incrementAndGet();
					}
				}
            }

            synchronized(eventCounter) {

				while (count == eventCounter.get()) {
					eventCounter.wait();
				}
			}

        }

    }

    private void signalEvent() {

	    synchronized(eventCounter) {
			eventCounter.incrementAndGet();
			eventCounter.notifyAll();
		}
    }

    private void startup( ZooKeeper zk ) throws KeeperException, InterruptedException {

        this.zk = zk ;
        becomeMaster();
        setupAssignments();
        setupTasks();

    }

    /**
     * do lock file based master election
     */
    private void becomeMaster() throws KeeperException, InterruptedException {

    }

    synchronized private void setupAssignments() {

        try {

			List<String> children = zk.getChildren("/assign", false);
			
            for(String child: children) {
			
                String path = "/assign/" + child;
			
                try {
			
                    byte assignedBytes[] = zk.getData(path, false, null);
			
                    if (assignedBytes.length > 0) {
			
                        String assignment = new String(assignedBytes);
			
                        workerToAssignment.put(path, assignment);
			
                        assignmentToWorker.put(assignment, path);
			
                    }
			
                    workers.add(path);
			
                } catch( KeeperException.NoNodeException e ) {
					// it's okay, the worker may die, we just move on
				}
			}

		} catch( Exception e )  {
			e.printStackTrace();
		}

		refreshWorkerList();

    }

    synchronized private void setupTasks() {

		try {
		
            List<String> children = zk.getChildren("/tasks", false);

			for(String child: children) {
			
                String path = "/tasks/" + child;
			
                tasks.add(path);
			}
		
        } catch(Exception e) {
			e.printStackTrace();
		}
		
        refreshTaskList();
    }

    private void refreshTaskList() {

        try {
            
            refreshList("/tasks", tasks, new Watcher() {
                
                @Override
                public void process(WatchedEvent arg0) {

                    refreshTaskList();

                }});

            signalEvent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshWorkerList()   {

        try {
			refreshList("/assign", workers, new Watcher() {

				@Override
				public void process(WatchedEvent arg0) {

					refreshWorkerList();
                    
				}});

			signalEvent();

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * maintain the Set represented by children of the znode represented by path
     */
    private void refreshList(String path, Set<String> list, Watcher watcher) {

    }

    @Override
    public void process(WatchedEvent event) {
        log.info("Event:{}",event);        
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

		if (args.length != 1) {
			System.err.println("USAGE: zkhostports");
			System.exit(2);
		}

		String hostPort = args[0];

		TaskQueueMaster tqm = new TaskQueueMaster();

		ZooKeeper zk = new ZooKeeper(hostPort, 10000, tqm);

		tqm.startup(zk);

		tqm.schedule();

	}

}
