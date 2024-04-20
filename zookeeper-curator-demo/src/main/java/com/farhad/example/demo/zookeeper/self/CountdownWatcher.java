package com.farhad.example.demo.zookeeper.self;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CountdownWatcher implements Watcher {

    private final String name ;

    private CountDownLatch clientConnected ;
    private KeeperState state ;
    private boolean connected ;

    
    public CountdownWatcher(String name) {
        this.name = name;
        reset() ;
    }


    private synchronized void reset() {

        clientConnected = new CountDownLatch(1);
        state = KeeperState.Disconnected;
        connected = false;
    }


    @Override
    public synchronized void process(WatchedEvent event) {
        log.info("Watcher {} got event {}", name, event);

        state = event.getState();

        if ( state == KeeperState.SyncConnected )  {
            connected = true ;
            clientConnected.countDown();
        } else {
            connected = false ;
        }
        notifyAll();
        
    }

    public synchronized boolean isConnected() {
        return connected ;
    }

    public synchronized KeeperState state() {

        return state ;
    }

    public synchronized void waitForConnected( long timeout ) throws InterruptedException, TimeoutException {
        long expire = System.currentTimeMillis() + timeout ;
        long left = timeout ;

        while ( !connected && left > 0 ) {
            wait(left);
            left = expire - System.currentTimeMillis() ;
        }
        if ( !connected ) {
            throw new TimeoutException( "Did not connect" );
        }
    }

    public synchronized void waitForDisconnected( long timeout ) throws InterruptedException, TimeoutException {
        long expire = System.currentTimeMillis() + timeout ;
        long left = timeout ;

        while ( connected && left > 0 ) {
            
            wait( left );
            left = expire - System.currentTimeMillis() ;
        }
        if ( connected ) {
            throw new TimeoutException( "Did not connect" );
        }
    }
    
}
