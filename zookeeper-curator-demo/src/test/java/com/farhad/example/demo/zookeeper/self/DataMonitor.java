package com.farhad.example.demo.zookeeper.self;


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;


import static com.farhad.example.demo.zookeeper.Utils.sleep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Clients can set a watch on a znode. 
 * 
 * A watch will be triggered and removed when the znode changes. 
 * 
 * When a watch is triggered, the client receives a packet saying that the znode has changed. 
 * 
 * If the connection between the client and one of the ZooKeeper servers is broken, the client will 
 * receive a local notification.
 * 
 * New in 3.6.0: Clients can also set permanent, recursive watches on a znode that are not removed when triggered 
 * and that trigger for changes on the registered znode as well as any children znodes recursively.
 * 
 * Clients can set watches on znodes. Changes to that znode trigger the watch and then clear the watch. 
 * 
 * When a watch triggers, ZooKeeper sends the client a notification.
 */
@Slf4j
@RequiredArgsConstructor
public class DataMonitor implements Watcher, StatCallback {
    
    final ZooKeeper zk;
    final DataMonitorListener listener ;
    final String znode;
    final Watcher chainedWatcher ;

    boolean dead ;
    byte[] prevData;

    @Override
    public void process(WatchedEvent event) { 
        
        String path = event.getPath();

        log.info("Event {} @ {} , with type {}", event, new Date(), event.getType());
        
        if ( event.getType() == Event.EventType.None ) {
            // state of  connection has changed
            switch ( event.getState() ) {
                case SyncConnected:
                // watches are automatically re-registered with
                // server and any watches triggered while the client was
                // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED);
                    break; 
                case AuthFailed:
                    break;
                case ConnectedReadOnly:
                    break;
                case Disconnected:
                    break;
                case SaslAuthenticated:
                    break;
                default:
                    break;
            }
        } else {
            if ( path != null && path.equals(znode) ) {
                // Something has changed on the node
                zk.exists(znode, true, this, null);
            }
        }

        if ( chainedWatcher != null ) {
            chainedWatcher.process(event);
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        
        boolean exists;

        log.info("rc: {}", rc);
        log.info("path: {}", path);
        log.info("ctx: {}", ctx);
        log.info("stat: {}", stat);
        
        Code code = Code.get(rc);

        switch ( code ) {

            case OK:

                exists = true;
                break;

            case NONODE:

                exists = false ;
                break;
            
            case SESSIONEXPIRED:
            case NOAUTH:

                dead = true ;
                listener.closing(code);
                return;

            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte[] b = null;

        if ( exists ) {

            try {

                b = zk.getData(znode, false, null);

            } catch( KeeperException ex ) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                ex.printStackTrace();

            } catch( InterruptedException ex ) {

                return;
            }

            log.info( ">Get Data: {}", new String( b ) );
        }

        if ( ( b == null && b !=  prevData ) || ( b != null &&  !Arrays.equals( prevData, b ) ) ) {

            listener.exists( b );
            prevData = b;
        }
    }
    
    public static interface DataMonitorListener {

        /**
         * The ZooKeeper session is no longer valid.
         * @param rc the ZooKeeper reason code
         */
        void closing(Code code);

        /**
         * The existence status of the node has changed.
         * @param data
         */
        void exists(byte data[]);
    }
}
