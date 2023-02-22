package com.farhad.example.leadership.zookeeper.config;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
// import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;
import org.springframework.integration.zookeeper.config.LeaderInitiatorFactoryBean;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;
// import org.springframework.integration.zookeeper.metadata.ZookeeperMetadataStore;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Profile("leadership-zookeeper")
public class ZookeeperConfig {

    public static final String ZOOKEEPER_CLIENT_OP = "localhost:2181" ;
    public static final String ZOOKEEPER_LEADER_STORAGE = "/cluster/leadership" ;
    public static final String ZOOKEEPER_LEADER_ROLE = "cluster-leadership" ;

    @Value("${project.basedir}")
    private String baseDir;

    /**
     * Config Curator that handles the complexity of managing
     * connections to the ZooKeeper cluster and retrying operations
     * @return
     */
    @Bean
    public CuratorFrameworkFactoryBean curatorFramework(){
		// return  CuratorFrameworkFactory.builder()
		// 								.connectString(ZOOKEEPER_CLIENT_OP)
		// 								.retryPolicy(new ExponentialBackoffRetry(10,100,100000))
		// 								.sessionTimeoutMs(30_000)
										
		// 								.build();

        return new CuratorFrameworkFactoryBean(ZOOKEEPER_CLIENT_OP,
                                                new ExponentialBackoffRetry(10,100,100000));
    }

    @Bean
    public LeaderInitiatorFactoryBean leaderInitiator(CuratorFramework client) {

        String zookeeperLeaderDataLocation = String.format("%s%s", baseDir, ZOOKEEPER_LEADER_STORAGE);
        log.info("{}",zookeeperLeaderDataLocation);
        return new LeaderInitiatorFactoryBean()
                                    .setClient(client)
                                    .setPath(zookeeperLeaderDataLocation)
                                    .setRole(ZOOKEEPER_LEADER_ROLE);
    }

    // @Bean
    // public MetadataStore zkStore(CuratorFramework client) {
    //     return new ZookeeperMetadataStore(client);
    // } 

    @Bean
    public ZookeeperLockRegistry zookeeperLockRegistry(CuratorFramework client) {
        return new ZookeeperLockRegistry(client);
    }
}
