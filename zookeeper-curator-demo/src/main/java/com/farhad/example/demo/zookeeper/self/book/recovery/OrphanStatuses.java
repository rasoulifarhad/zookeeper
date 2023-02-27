package com.farhad.example.demo.zookeeper.self.book.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Clients are supposed to delete status znodes. If they crash before
 * cleaning up such status znodes, they will hang there forever. This 
 * class cleans up such znodes.
 */
@Slf4j
@RequiredArgsConstructor
public class OrphanStatuses {
    private static final String STATUS_PATERN = "/status/%s";
    private List<String> tasks ;
    private List<String> statuses;
    private final ZooKeeper zk ;

    public void cleanUp() {
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
                    getStatuses();
                    break;

                default:

                    log.error("getChildren failed.", KeeperException.create(code, path));
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

                    getTasks();
                    break;

                case OK:

                    statuses = children;
                    processTasks();
                    break;

                default:

                    log.error("getChildren failed.", KeeperException.create(code, path));
                    break;
            }
        }
        
    };

    void processTasks() {

        for( String task : tasks ) {
            statuses.remove(String.format("status-%S", task));
        }

        for( String status : statuses ) {

            zk.delete(String.format(STATUS_PATERN, status),-1, deleteStatusCallback, null);

        }
    }

    VoidCallback deleteStatusCallback = new VoidCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx) {

            Code code = Code.get(rc) ;
            switch( code ) {

                case CONNECTIONLOSS:

                    zk.delete(path,-1, deleteStatusCallback, null);
                    break;

                case OK:

                    log.info("Succesfully deleted orphan status znode:{}", path);
                    break;

                default:

                    log.error("getChildren failed.", KeeperException.create(code, path));
                    break;
            }
            
        }
        
    } ;

    public static void main(String args[]) throws Exception {
        ZooKeeper zk = new ZooKeeper("localhost:" + args[0], 10000, new Watcher() {
            public void process(WatchedEvent event) {
                log.info( "Received event: " + event.getType() );
            }
        });
        
        (new OrphanStatuses(zk)).cleanUp();
    }
}
