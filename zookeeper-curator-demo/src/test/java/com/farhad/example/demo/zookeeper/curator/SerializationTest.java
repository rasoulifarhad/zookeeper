package com.farhad.example.demo.zookeeper.curator;

import org.junit.jupiter.api.Test;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
// import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SerializationTest extends AbstractTest {
    

    @Test
    public void typed_model_test() {

        try ( CuratorFramework client = getClient() ) {
            AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

            ModelSpec<StringData> spec = ModelSpec
                                            .builder(
                                                ZPath.parseWithIds( key()), 
                                                JacksonModelSerializer.build(StringData.class))
                                            .build(); 

            ModeledFramework<StringData> modeledClient = ModeledFramework.wrap(async, spec);

            final String value = "my-value" ;
            modeledClient.set(new StringData(value));

            modeledClient
                    .read()
                    .whenComplete( ( v, e ) -> {
                        if (e != null) {
                            fail("Can not read data.");
                        } else {
                            assertThat(v).isNotNull();
                            assertThat(v.getData()).isEqualTo(value);
                        }
                    } );

        }
    }

}
