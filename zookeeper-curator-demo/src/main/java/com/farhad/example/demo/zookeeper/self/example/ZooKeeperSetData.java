package com.farhad.example.demo.zookeeper.self.example;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
public class ZooKeeperSetData {
    
    private final ZooKeeper zk;

    public void update (String path, byte[] data) throws KeeperException, InterruptedException {

        zk.setData(path, data, zk.exists(path, true).getVersion());
    
    }

    public static void main(String[] args) throws InterruptedException,KeeperException {
		String path= Constant.DEFAULT_PATH;
		byte[] data = "Success".getBytes(); //Assign data which is to be updated.

		try {
			ZooKeeperConnection conn = new ZooKeeperConnection();
			ZooKeeper zk = conn.connect("localhost");
            ZooKeeperSetData setData = new ZooKeeperSetData(zk);
			setData.update(path, data); // Update znode data to the specified path
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
