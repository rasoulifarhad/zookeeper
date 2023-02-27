package com.farhad.example.demo.zookeeper.self.example;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ZooKeeperDelete {
    
    private final ZooKeeper zk ;

    public void delete( String path ) throws KeeperException,InterruptedException {

        zk.delete(path, zk.exists(path, true).getVersion());
        log.info("{} Deleted", path);
    }

    public static void main(String[] args) throws InterruptedException,KeeperException {

        String path = Constant.DEFAULT_PATH;
        try {

           ZooKeeperConnection conn = new ZooKeeperConnection();
           ZooKeeper zk = conn.connect("localhost");
           ZooKeeperDelete command = new ZooKeeperDelete(zk);
           command.delete(path); //delete the node with the specified path
           conn.close();

        } catch(Exception e) {
           System.out.println(e.getMessage()); // catches error messages
        }
     }
}
