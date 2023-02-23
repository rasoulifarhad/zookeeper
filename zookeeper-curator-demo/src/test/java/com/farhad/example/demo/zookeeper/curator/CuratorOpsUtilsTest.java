package com.farhad.example.demo.zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.assertj.core.api.Assertions.*;
// import static org.awaitility.Awaitility.await;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;
import static com.farhad.example.demo.zookeeper.curator.CuratorOpsUtils.*;

@Slf4j
public class CuratorOpsUtilsTest extends AbstractTest {
    

    private static final String PARENT_KEY = "/curatorTest/farhad/example";
    private static final String KEY = "/curatorTest/farhad/example/key";
    private static final String EPHEMERAL_SEQUENTIAL_KEY = "/curatorTest/farhad/example/ephemeralSequentialKey";
    private static final String EPHEMERAL_KEY = "/curatorTest/farhad/example/ephemeralKey";
    private static final String ANOTHER_EPHEMERAL_KEY = "/curatorTest/farhad/example/anotherEphemeralKey";

    @Test
    public void curator_test() throws Exception {
        
        try ( CuratorFramework client = getClient() ) {

            if ( client.checkExists().forPath(KEY) != null ) {

                delete(client, KEY); 
            }

            create(client, KEY, "key is data".getBytes());

            log.info("Path: {} , Data: {}",KEY, client.getData().forPath(KEY));

            String nodePath = createEphemeralSequential(client, EPHEMERAL_SEQUENTIAL_KEY,
                                                    " ephemeralSequentialKey is ephemeral and sequential node.".getBytes());
            log.info("Path: {} . Data: {}",nodePath, client.getData().forPath(nodePath));
            
            setDataAsync(client, KEY, "key updated async".getBytes());

            setDataAsyncWithCallback(client, new BackgroundCallback(){

                @Override
                public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("setDataAsyncWithCallback: {}", event);
                }
            }, KEY, "key updated with async callback".getBytes());

            List<String> watched  =  watchedGetChildren(client, PARENT_KEY);

            log.info("watched: {}",watched);

            createEphemeral(client, EPHEMERAL_KEY,"ephemeralKey is ephemeral  node.".getBytes());

            watched = watchedGetChildren(client, PARENT_KEY);

            log.info("watched: {}",watched);

            createEphemeral(client, ANOTHER_EPHEMERAL_KEY,"anotherEphemeralKey is ephemeral  node.".getBytes());

            // Your Watcher should notify you if there is a change in the connection. If so, the EventType on the WatchedEvent 
            // will be None, Use that as an opportunity to restart your watches:
            watched = watchedGetChildren(client, new Watcher(){

                @Override
                public void process(WatchedEvent event) {
                    if( (event.getType() == Event.EventType.None )  && ( event.getState() == Event.KeeperState.SyncConnected ) ) {
                        try {
                            client.getChildren().usingWatcher(this).forPath(PARENT_KEY);

                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } , PARENT_KEY);
            
            log.info("watched: {}",watched);

            delete(client, PARENT_KEY);

        }

    }
}
