package com.farhad.example.demo.zookeeper.curator.leadership;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.curator.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderShipClientTest extends AbstractTest {
    
    private static final int NUMBER_OF_CLIENTS = 10 ;
    private static final String CLIENTS_PREFIX_NAME = "Client # " ;

    private static final String PATH = "/com/farhad/example/leadership/leader" ;


    @Test
    public void leadership_test() throws Exception{

        log.info("Create {} clients.each try to be leader . leader wait a random number of seconds and then let another leader selected.");

        List<CuratorFramework>  clientsCuratorConnection = new ArrayList<>();
        List<LeaderShipClient>  leaderCondidates = new ArrayList<>();

        try {

            for ( int i = 0 ; i < NUMBER_OF_CLIENTS ; ++i ) {


                CuratorFramework client = getClient(false);
                clientsCuratorConnection.add(client);
    
                LeaderShipClient leaderShipClient = new LeaderShipClient(client, PATH, CLIENTS_PREFIX_NAME + i);
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
