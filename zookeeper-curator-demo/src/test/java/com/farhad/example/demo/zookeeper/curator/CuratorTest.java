package com.farhad.example.demo.zookeeper.curator;

// import java.util.concurrent.TimeUnit.* ;
// import org.hamcrest.Matchers.* ;
// import org.apache.curator.utils.CloseableUtils;
// import org.awaitility.Awaitility.* ;
// import java.time.Duration.*;

import org.junit.jupiter.api.Test;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.framework.CuratorFramework;
import java.util.concurrent.atomic.AtomicBoolean;
// import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;

public class CuratorTest extends AbstractTest{
    

    @Test
    public void connection_management_test() throws Exception {

        try ( CuratorFramework client = getClient() ) {
            assertThat(client.checkExists().forPath("/")).isNotNull();
        }

    }

    @Test
    public void async_connection_management_test() throws Exception {

        try ( CuratorFramework client = getClient() ) {
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            AtomicBoolean exists = new AtomicBoolean(false) ;

            async
                .checkExists()
                .forPath("/")
                .thenAcceptAsync( s -> exists.set( s != null ) );
                     
            // await().untilAsserted( () -> assertThat(exists).isTrue() );
            await().until( () -> exists.get() );
            // await().untilTrue(isEqual);

        }
    }

    @Test
    public void set_data_test() throws Exception {


        try ( CuratorFramework client = getClient() ) {
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            String value = "my-value" ;

            client.create().forPath( key());
    
            async
                .setData()
                .forPath( key(),value.getBytes());
    
            AtomicBoolean isEqual = new AtomicBoolean(false);
           
            async
                .getData()
                .forPath( key())
                .thenAcceptAsync( data -> isEqual.set( new String(data).equals(value) ) );
    
            await().untilAsserted( () -> assertThat(isEqual).isTrue() );
            // await().until(() -> isEqual.get());
            // await().untilTrue(isEqual);
        }
    }


}
