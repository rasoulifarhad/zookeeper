package com.farhad.example.demo.zookeeper.self.kvdb;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException;

import com.farhad.example.demo.zookeeper.self.utils.MoreZKPaths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Updater {
  
    public static final String PATH = MoreZKPaths.makePath("config");

    private final KeyValueStore store ;

    private final Random random = new Random();

    public Updater(String hosts) throws IOException, InterruptedException  {

        store = new KeyValueStore();
        store.connect(hosts);
    }

    public void run() throws InterruptedException, KeeperException  {

        while( true ) {

            int value = random.nextInt(100);
            store.write(PATH, Integer.toString(value));
            log.info("Set: {} To: {}", PATH, value);

            TimeUnit.SECONDS.sleep(random.nextInt(10));
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        Updater updater = new Updater(args[0]);
        updater.run();
    }

}
