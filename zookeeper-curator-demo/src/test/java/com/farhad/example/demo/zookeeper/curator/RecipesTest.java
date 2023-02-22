package com.farhad.example.demo.zookeeper.curator;

import org.junit.jupiter.api.Test;

import java.time.Duration;
// import java.util.concurrent.TimeUnit.* ;
// import org.hamcrest.Matchers.* ;
// import org.apache.curator.utils.CloseableUtils;
// import org.awaitility.Awaitility.* ;
import java.time.Duration.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.state.ConnectionState;

import java.util.concurrent.atomic.AtomicBoolean;
// import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class RecipesTest extends AbstractTest{
    
    @Test
    public void leadership_test() {

        try ( CuratorFramework client = getClient() ) {

            LeaderSelector leaderSelector = 
                        new LeaderSelector(
                            client, 
                            "/leader/for/job/my-job", 
                            new LeaderSelectorListener() {

                                @Override
                                public void takeLeadership(CuratorFramework client) throws Exception {
                                    log.info("I am the leaderrrr of job 'my-job'") ;
                                    await().during(Duration.ofSeconds(2));
                                }

                                @Override
                                public void stateChanged(CuratorFramework client,ConnectionState newState) {
                                    log.info("Connection State changed: {}",newState) ;
  
                                }
                                
                            }
                        );
            // join to group
            leaderSelector.start();

            // wait until the job A is done among all members
            leaderSelector.close();
        }
        
    }

    @Test
    public void sharedLock_test() throws Exception {

        AtomicInteger count =new AtomicInteger(0)   ;
        Callable<Integer> process = () -> count.incrementAndGet();

        try ( CuratorFramework client = getClient() ) {

            InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(client, "/lock/my-process") ;

            lock.acquire();

            log.info("Count is: {}",process.call()); 
            
            lock.release();
        }
    }

    @Test
    public void counter_test() throws Exception {


        try ( CuratorFramework client = getClient() ) {
            SharedCount counter  = new SharedCount(client, "/counters/big-mans", 0); 
            counter.start();

            counter.setCount(0);
            counter.setCount(counter.getCount() + 10 );

            assertThat(counter.getCount()).isEqualTo(10);
        }
    }
}
