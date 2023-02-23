package com.farhad.example.demo.zookeeper.curator.async;

// import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.curator.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTest extends AbstractTest {
    
    // Create a sequential ZNode and, once successfully completed, set a watcher on the ZNode. 
    // Note: this code does not deal with errors. Should a connection problem occur or another 
    // exception occur, the completion lambda will never be called.
    @Test
    public void Create_sequential_ZNode_then_watch() {

        AtomicBoolean exists = new AtomicBoolean( false );

        Function<byte[],String> watchTriggered = (byteArray) -> {
                                    log.info("watchTriggered trigered #############################################");
                                    exists.set(true);   
                                    return new String(byteArray);
                                };

        try ( CuratorFramework client = getClient() ) { 
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            AsyncStage<String> stage = 
                                async.create()
                                     .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                                     .forPath("/curatorTest");

            stage.thenAccept( actualPath ->   
                                    async.watched()
                                         .getData()
                                         .forPath(actualPath)
                                         .thenApply( watchTriggered ) ) ;     
                                         
            await().untilAsserted( () -> assertThat( exists ).isTrue() );
        }

    }

    @Test
    public void given_a_sequential_ZNode_when_watched_taht_then_correct() throws Exception {

        //given
        AtomicBoolean exists = new AtomicBoolean( false );

        Function<byte[],String> watchTriggered = (byteArray) -> {
                                    log.info("watchTriggered trigered #############################################");
                                    exists.set(true);   
                                    return new String(byteArray);
                                };

        createPersistNode("/curatorTest");
        setNodeData("/curatorTest" , "initial data");

        // when
        try ( CuratorFramework client = getClient() ) { 
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            AsyncStage<String> stage = 
                                async.create()
                                     .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                                     .forPath("/curatorTest");

            stage.thenAccept( actualPath ->   
                                    async.watched()
                                         .getData()
                                         .forPath(actualPath)
                                         .thenApply( watchTriggered ) ) ;     
                                         
            await().untilAsserted( () -> assertThat( exists ).isTrue() );
        }

    }

    @Test
    public void async_create_path_test() {

        AtomicBoolean exists = new AtomicBoolean( false );

        try ( CuratorFramework client = getClient() ) {
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            exists.set(false);
            CuratorAsync.createPath(async, "/curatorTest","curatorTest".getBytes(),exists);
            await().untilAsserted( () -> assertThat(exists).isTrue() );
            // await().during(Duration.ofSeconds(2));

            exists.set(false);
            CuratorAsync.createPath(async, "/curatorTest/farhad","curatorTest".getBytes(),exists);
            await().untilAsserted( () -> assertThat(exists).isTrue() );
            // await().during(Duration.ofSeconds(2));

            exists.set(false);
            CuratorAsync.createPath(async, "/curatorTest/farhad/example","curatorTest".getBytes(),exists);
            await().untilAsserted( () -> assertThat(exists).isTrue() );
            // await().during(Duration.ofSeconds(2));

            exists.set(false);
            CuratorAsync.createPath(async, "/curatorTest/farhad/example/async", "async data".getBytes(),exists);
            await().untilAsserted( () -> assertThat(exists).isTrue() );
            // await().during(Duration.ofSeconds(2));
        }
    }

        @Test
        public void async_create_path_and_watch_test() {

            
            AtomicBoolean exists = new AtomicBoolean( false );

            try ( CuratorFramework client = getClient() ) {
                AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

                exists.set(false);
                CuratorAsync.createPathAndWatchSimple(async, "/curatorTest","curatorTest".getBytes() ,exists);
                // await().untilAsserted( () -> assertThat(exists).isTrue() );
                await().during(Duration.ofSeconds(2));
    
            }
        }


        String watchTriggered(byte[] barr) {
            log.info("watchTriggered: {}",new String(barr));
            return "ok";
        }
    
    
}
