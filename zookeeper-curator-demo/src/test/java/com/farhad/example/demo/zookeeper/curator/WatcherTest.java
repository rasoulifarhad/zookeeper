package com.farhad.example.demo.zookeeper.curator;

// import java.util.concurrent.TimeUnit.* ;
// import org.hamcrest.Matchers.* ;
// import org.apache.curator.utils.CloseableUtils;
// import org.awaitility.Awaitility.* ;
// import java.time.Duration.*;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.framework.CuratorFramework;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;

public class WatcherTest extends AbstractTest {
    
    @Test
    public void watched_test() throws Exception {

        try ( CuratorFramework client = getClient() ) {
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            String value = "my-value" ;

            client.create().forPath( key() );
            List<String> changes = new ArrayList<>();
    
            async
                .watched()
                .getData()
                .forPath( key())
                .event()
                .thenAccept( watchedEvent -> {
                    try {
                        changes.add( new String( client
                                                    .getData()
                                                    .forPath(
                                                        watchedEvent.getPath()) ) );
                    } catch(Exception ex){
    
                    }
                } );
    
            async
                .setData()
                .forPath( key(),value.getBytes());
    
            await()
                .untilAsserted( () -> assertThat( changes.size() ).isEqualTo(1) );
            }
    }

}
