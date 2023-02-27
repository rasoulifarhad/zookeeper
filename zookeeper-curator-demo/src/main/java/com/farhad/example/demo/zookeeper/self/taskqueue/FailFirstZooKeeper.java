package com.farhad.example.demo.zookeeper.self.taskqueue;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailFirstZooKeeper extends ZooKeeper {
   
    private boolean isFirst = true;

	public FailFirstZooKeeper(String connectString, int sessionTimeout,	Watcher watcher) throws IOException {

		super(connectString, sessionTimeout, watcher);
	}

	@Override
	public String create(String path, byte data[], List<ACL> acl, CreateMode createMode) throws InterruptedException, KeeperException {

		String rc = super.create(path, data, acl, createMode);

		if (isFirst) {
		
            isFirst = false;
		
            throw new KeeperException.ConnectionLossException();
		}
		
        return rc;
	}
}
