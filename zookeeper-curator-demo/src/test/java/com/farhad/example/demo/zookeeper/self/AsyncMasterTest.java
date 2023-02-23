package com.farhad.example.demo.zookeeper.self;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.apache.curator.utils.CloseableUtils;
import static com.farhad.example.demo.zookeeper.Utils.sleep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncMasterTest {
    
    private static final String SERVER_ID_PATTERN = "async-master-%s";
    TestingServer server = null ;

    @BeforeEach
    public void  before() {

        try {
            server = new TestingServer() ;
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
    }

    @AfterEach
    public void after() {
        
        CloseableUtils.closeQuietly(server);
    }

    @Test
    public void asyncMaster_test() {

        int masterCount = 3 ;
        ExecutorService executorService = Executors.newFixedThreadPool(masterCount);
        IntStream.range(0, 3)
                                .forEach( i -> {
                                    AsyncMaster asyncMaster = new AsyncMaster(server.getConnectString(), 
                                                                               String.format(SERVER_ID_PATTERN, i));
                                    executorService.submit(asyncMaster);
                                } );
        log.info("");
        sleep(1000);
        
    }

}
