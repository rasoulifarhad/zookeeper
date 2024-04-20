package com.farhad.example.demo.zookeeper.self;

import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSessionExpiration {
    

    public static void main(String[] args) throws Exception {
        
        log.info("Starting zk1");

        TestingServer server = null;
        ZooKeeper zk1 = null;
        ZooKeeper zk2 = null;

        try {
            server = new TestingServer();
            server.start();
    
            // Open a client connection - zk1
            CountdownWatcher watcher1 = new CountdownWatcher("zk1");
            zk1 = new ZooKeeper(server.getConnectString(), 1000, watcher1);
            watcher1.waitForConnected(1000);
                
            zk1.getData("/", false, null);

            log.info("Starting zk2");

            // now attach a second client zk2 with the same sessionid/passwd
            CountdownWatcher watcher2 = new CountdownWatcher("zk2");
            zk2 = new ZooKeeper(server.getConnectString(), 1000, watcher2,zk1.getSessionId(), zk1.getSessionPasswd());

            watcher2.waitForConnected( 1000 );

            // close the second client, the session is now invalid
            log.info("Cloasing zk2");
            zk2.close();

            log.info("Attempting use of zk1");

            try {
                // this will throw session expired exception
                zk1.getData("/", false, null);
            } catch (KeeperException.SessionExpiredException e) {
                log.error("Got session expired on zk1!");
                return ;
            }
        // 3.2.0 and later:
        // There's a gotcha though - In version 3.2.0 and later if you
        // run this on against a quorum (vs standalone) you may get a
        // KeeperException.SessionMovedException instead. This is
        // thrown if a client moved from one server to a second, but
        // then attempts to talk to the first server (should never
        // happen, but could in certain bad situations), this example
        // simulates that situation in the sense that the client with
        // session id zk1.getSessionId() has moved
        // 
        // One way around session moved on a quorum is to have each
        // client connect to the same, single server in the cluster
        // (so pass a single host:port rather than a list). This 
        // will ensure that you get the session expiration, and
        // not session moved exception.
        //
        // Again, if you run against standalone server you won't see
        // this. If you run against a server version 3.1.x or earlier
        // you won't see this.
        // If you run against quorum you need to easily determine which
        // server zk1 is attached to - we are adding this capability
        // in 3.3.0 - and have zk2 attach to that same server.

        log.error("Oops, this should NOT have happened!");
        } finally {
            CloseableUtils.closeQuietly(server);   
        }
    }
}
