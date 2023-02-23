package com.farhad.example.demo.zookeeper.curator.leadership;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.curator.AbstractTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderLatchClientTest extends AbstractTest {
    
    private static final int NUMBER_OF_CLIENTS = 10 ;
    private static final String CLIENTS_PREFIX_NAME = "Client # " ;

    private static final String PATH = "/com/farhad/example/leadership/leaderlatch" ;

    @Test
    public void leaderLatchClient_Test() throws Exception {
        
        List<CuratorFramework> clientsCuratorConnection = new ArrayList<>();
        List<LeaderLatch> leaderLatchClients = new ArrayList<>(); 

        try{

            for (int i = 0 ; i < NUMBER_OF_CLIENTS ; ++i) {

                CuratorFramework client = getClient(false);
                clientsCuratorConnection.add(client);

                LeaderLatch leaderCandidate = new LeaderLatch(client, PATH, CLIENTS_PREFIX_NAME + i);
                leaderLatchClients.add(leaderCandidate);

                client.start();
                leaderCandidate.start();
            }

            Thread.sleep(0000);

            LeaderLatch currentLatch = 
                                        leaderLatchClients
                                                    .stream()
                                                    .filter( ll -> ll.hasLeadership())
                                                    .findFirst()
                                                    .orElseThrow(IllegalArgumentException::new);

            log.info("Current leader: {}",currentLatch.getId());

            Thread.sleep(2000);

            log.info("Releasethe  leader: {}",currentLatch.getId());

            currentLatch.close();

            Thread.sleep(2000L);

            currentLatch = 
                        leaderLatchClients
                                    .stream()
                                    .filter( ll -> ll.hasLeadership())
                                    .findFirst()
                                    .orElseThrow(IllegalArgumentException::new);

            log.info("New Current leader: {}",currentLatch.getId());
            log.info("Participants: {}",currentLatch.getParticipants());

            Thread.sleep(20000L);

            // System.out.println("Press enter/return to quit\n");
			// new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch(Exception ex){
            ex.printStackTrace();
        } finally{
            log.info("Shutting down .....");

            leaderLatchClients.forEach( c -> CloseableUtils.closeQuietly(c) );
            clientsCuratorConnection.forEach( c -> CloseableUtils.closeQuietly(c) );
        }
    }

}
