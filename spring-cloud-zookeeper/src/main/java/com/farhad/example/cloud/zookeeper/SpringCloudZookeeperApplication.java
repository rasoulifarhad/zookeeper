package com.farhad.example.cloud.zookeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAutoConfiguration
@RestController
public class SpringCloudZookeeperApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudZookeeperApplication.class, args);
	}

	@RequestMapping("/")
	public String home() {
		return "Hello World";
	}

}
