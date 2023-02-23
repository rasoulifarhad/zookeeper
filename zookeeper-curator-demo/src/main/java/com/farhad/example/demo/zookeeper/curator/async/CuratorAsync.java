package com.farhad.example.demo.zookeeper.curator.async;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncEventException;
import org.apache.curator.x.async.WatchMode;
import org.apache.zookeeper.WatchedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CuratorAsync {
    
    void tt() {
    
    }


    public static void createPath( AsyncCuratorFramework async, String path, byte[] payload  , AtomicBoolean exists) {

        async
           .create()
           .forPath(path,payload)
           .whenComplete( ( name, ex ) -> {
                if ( ex != null ) {
                    exists.set(false);
                    ex.printStackTrace();
                } else {
                    exists.set(true);
                    log.info("Node {} created",name);
                }
           });
        
    }

    public static void createPathAndWatchSimple( AsyncCuratorFramework async, String path, byte[] payload,AtomicBoolean exists ) {

        async.create()
             .forPath(path,payload)
             .whenComplete( ( name, ex ) -> {
                if ( ex != null ) {
                    exists.set(false); 
                    ex.printStackTrace();
                } else {
                    async
                    // because "WatchMode.successOnly" is used the watch stage is only triggered when
                    // the EventType is a node event
                    .with(WatchMode.successOnly)
                    .watched()
                    .checkExists()
                    .forPath(path)
                    .event()
                    .thenAccept( event -> {
                        exists.set(true); 
                        log.info("############  path: {} ,Event Type: {} , Event: {} ############", path, event.getType(), event);
            
                    } );
                }
             });
    } 

    public static void setData( AsyncCuratorFramework async, String path, byte[] payload,AtomicBoolean exists ) {

        async.setData()
             .forPath(path,payload)
             .whenComplete( ( name, ex ) -> {
                if ( ex != null ) {
                    exists.set(false);
                    ex.printStackTrace();
                } else {
                    exists.set(true);
                    log.info("Data {} updated",name);
                }
             });
    } 

    private static void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage, AtomicBoolean exists) {

        watchedStage.thenAccept( event -> {
            exists.set(true); 
            log.info("############ Event Type: {} , Event: {} ############", event.getType(), event);
        } );

        watchedStage.exceptionally( ex -> {
            AsyncEventException asyncEx = ( AsyncEventException ) ex ;
            asyncEx.printStackTrace();
            exists.set(false);
            handleWatchedStage( asyncEx.reset() ,exists);
            return null;

        } );

    }

}
