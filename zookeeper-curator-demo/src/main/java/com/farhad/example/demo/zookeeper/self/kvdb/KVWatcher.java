package com.farhad.example.demo.zookeeper.self.kvdb;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KVWatcher implements Watcher{
    
    void tt() {
       
    }

    private final KeyValueStore store;

    public KVWatcher(String hosts) throws InterruptedException, IOException  {

        store = new KeyValueStore();
        store.connect(hosts);

    }

    public void displayConfig() throws InterruptedException, KeeperException {

        String value = store.read(Updater.PATH, this);
        log.info("Read {} as {}", Updater.PATH, value);
    }

    @Override
    public void process(WatchedEvent event) {
        
        log.info("Process incoming event: {}",event);

        if ( event.getType() == Event.EventType.NodeDataChanged ) {

            try {

                displayConfig();

            } catch(InterruptedException ex) {

                log.error("Interrupted. Exiting", ex);
                Thread.currentThread().interrupt();

            } catch(KeeperException ex) {

                log.error("KeeperException: {}",ex.code(), ex);

            }

        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        KVWatcher watcher = new KVWatcher(args[0]);
        watcher.displayConfig();

        Thread.sleep(Long.MAX_VALUE);
    }
    
}
