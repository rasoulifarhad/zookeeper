package com.farhad.example.demo.zookeeper.curator;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
// import java.util.concurrent.TimeUnit.* ;
// import org.hamcrest.Matchers.* ;
// import org.apache.curator.utils.CloseableUtils;
// import org.awaitility.Awaitility.* ;
import java.time.Duration.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.atomic.AtomicBoolean;
// import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LeaderShipClientTest extends AbstractTest{
    
    private static final int NUMBER_OF_CLIENTS = 10 ;
    private static final String CLIENTS_PREFIX_NAME = "Client # " ;

    private static final String path = "/com/farhad/example/leadership/leader" ;


    @Test
    public void leadership_test() throws Exception{

        log.info("Create {} clients.each try to be leader . leader wait a random number of seconds and then let another leader selected.");

        List<CuratorFramework>  clientsCuratorConnection = new ArrayList<>();
        List<LeaderShipClient>  leaderCondidates = new ArrayList<>();

        try {

            for ( int i = 0 ; i < NUMBER_OF_CLIENTS ; i++ ) {


                CuratorFramework client = getClient(false);
                clientsCuratorConnection.add(client);
    
                LeaderShipClient leaderShipClient = new LeaderShipClient(client, path, CLIENTS_PREFIX_NAME + i);
                leaderCondidates.add(leaderShipClient);
    
                client.start();
                leaderShipClient.start();
    
            }
    
            log.info("Press Enter to quit\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

        } finally {
            
            log.info("Shutting down .....");

            leaderCondidates.forEach( c -> CloseableUtils.closeQuietly(c) );
            clientsCuratorConnection.forEach( c -> CloseableUtils.closeQuietly(c) );
        }

    }

}
