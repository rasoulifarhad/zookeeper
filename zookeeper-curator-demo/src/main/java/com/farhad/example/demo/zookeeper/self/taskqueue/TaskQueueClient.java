package com.farhad.example.demo.zookeeper.self.taskqueue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

@Slf4j
public class TaskQueueClient implements Watcher{
    
    
    
    
    
    

    @Override
    public void process(WatchedEvent event) {
       
        log.info("Event: {}",event);        

        switch( event.getType() ) {

            case None:
                if ( event.getState() == Event.KeeperState.Expired) {
                    System.exit(1);
                }

                break;
            default:
                break;

        }
    }

    public void waitForFinish(ZooKeeper zk, String taskPath) {
        // TODO: block until the command has finished running
    }


    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

		if (args.length < 3) {
			log.info("USAGE: zkhostports cmd [args...]");
			System.exit(2);
		}

		TaskQueueClient tqc = new TaskQueueClient();

		ZooKeeper zk = new ZooKeeper(args[0], 10000, tqc);

		byte cmdBytes[] = Utils.stringsToBytes(args, 1, args.length);

        // TODO: change to create a task in ZooKeeper. (use the SEQUENCE flag to get a unique id)
		String taskPath = ""; 
		log.info("Submitted as {}", taskPath);

		tqc.waitForFinish(zk, taskPath);

		log.info("Finished");
		
	}
    
}
