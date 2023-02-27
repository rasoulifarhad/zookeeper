package com.farhad.example.demo.zookeeper.self.kvdb;

import java.nio.charset.Charset;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import com.farhad.example.demo.zookeeper.self.utils.ConnectionWatcher;

public class KeyValueStore extends ConnectionWatcher {
   
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public void  write(String path, String value) throws InterruptedException, KeeperException {
        Stat stat = zk.exists(path, false);
        if ( stat == null ) {
            zk.create(path, value.getBytes(CHARSET), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            zk.setData(path, value.getBytes(CHARSET), -1);
        }
    }

    public String read(String path, Watcher watcher) throws InterruptedException, KeeperException {
        byte[] data = zk.getData(path, watcher, null /* stat */);
        return new String(data, CHARSET);
    }
}
