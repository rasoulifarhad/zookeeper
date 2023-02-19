package com.farhad.example.si.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.zookeeper.metadata.ZookeeperMetadataStore;

@EnableIntegration
@SpringBootApplication
public class SiZookeeperExample01Application {

	@Value("${zookeeper.connect-string}")
    private String zookeeperServers;

	public static void main(String[] args) {
		SpringApplication.run(SiZookeeperExample01Application.class, args);
	}

	@Bean
    // @Qualifier("curatorConfig")
    CuratorFramework curratorFramework() {
		return  CuratorFrameworkFactory.builder()
										.connectString(zookeeperServers)
										.retryPolicy(new ExponentialBackoffRetry(10,100,100000))
										.sessionTimeoutMs(30_000)
										
										.build();
	}

	@Bean
	public MetadataStore zkStore(CuratorFramework curatorFramework) {
		curatorFramework.start();
		try{

			curatorFramework.blockUntilConnected();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        return new ZookeeperMetadataStore(curatorFramework);
	}

}
