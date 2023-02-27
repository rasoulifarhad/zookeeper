package com.farhad.example.demo.zookeeper.self.watch;

import java.util.Arrays;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataMonitor implements StatCallback, Watcher {
    

    private ZooKeeper zk;
    private String znode;
    private Watcher chainedWatcher;
    private DataMonitorListener listener;

    private boolean dead;
    private byte[] prevData ;

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher, DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        // The call to ZooKeeper.exists() checks for the existence of the znode, s
        // ets a watch, and passes a reference to itself (this) as the completion 
        // callback object. 
        zk.exists(znode, true, this, null);

    }

    
    /**
     * When the ZooKeeper.exists() operation completes on the server, the ZooKeeper API 
     * invokes this completion callback on the client
     * 
     * The triggering of the watch, on the other hand, sends an event to the Executor object, 
     * since the Executor registered as the Watcher of the ZooKeeper object.
     * 
     * The code first checks the error codes for znode existence, fatal errors, and recoverable errors. If the 
     * file (or znode) exists, it gets the data from the znode, and then invoke the exists() callback of Executor 
     * if the state has changed. 
     * 
     * Note, it doesn't have to do any Exception processing for the getData call because it has watches pending 
     * for anything that could cause an error: if the node is deleted before it calls ZooKeeper.getData(), the 
     * watch event set by the ZooKeeper.exists() triggers a callback; if there is a communication error, a connection 
     * watch event fires when the connection comes back up.
     * 
     * If the client-side ZooKeeper libraries can re-establish the communication channel (SyncConnected event) to ZooKeeper 
     * before session expiration (Expired event) all of the session's watches will automatically be re-established with the 
     * server (auto-reset of watches is new in ZooKeeper 3.0.0). 
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

        boolean exists ;

        Code code = Code.get(rc);

        switch ( code ) {
            case OK:
                exists = true ;    
                break;
            case NONODE:
                exists = false ;
                break ;
            case SESSIONEXPIRED:
            case NOAUTH:
                dead = true;
                listener.closing(rc);
                return;
            
            default:
                // retry errors
                zk.exists(znode, true, this, null);
                return ;
        }

        byte[] b = null ;
        if ( exists ) {

            try {
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        } 

        if ( ( b == null && b != prevData ) ||  ( b != null && !Arrays.equals( prevData, b ) ) )  {

            listener.exists(b);
            prevData = b;
        }
        
    }



    public boolean dead() {
        return true ;
    }

    /**
     * notice how DataMonitor processes watch events:
     * @param event
     */

    public void process(WatchedEvent event) {

        String path = event.getPath();

        if ( event.getType() == Event.EventType.None ) {

            // We are are being told that the state of the
            // connection has changed
            switch ( event.getState() ) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with 
                    // server and any watches triggered while the client was 
                    // disconnected will be delivered (in order of course)                    
                    break;
                case Expired:
                    //it,s all over
                    dead = true ;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.ordinal());
                    break;
            
                default:
                    break;
            }

        } else {

            if ( path != null && path.equals(znode) ) {
                // Something has changed on the node, let's find out
                zk.exists(znode, true, this, null);

            }

            if ( chainedWatcher != null ) {

                chainedWatcher.process(event);
            }

        }


    }
}
