package com.farhad.example.demo.zookeeper.self.group;

import com.farhad.example.demo.zookeeper.self.utils.ConnectionWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

@Slf4j
public class JoinGroup extends ConnectionWatcher {
    

    public void join(String groupName, String memberName) throws KeeperException, InterruptedException {

        String path = ZKPaths.makePath(groupName, memberName);    

        String createdPath = zk.create(path, null /* data */, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        log.info("{} joined", memberName);
        log.info("Created: {}", createdPath);

    }

    public static void main(String[] args) throws Exception {
        JoinGroup joinGroup = new JoinGroup();
        joinGroup.connect(args[0]);
        joinGroup.join(args[1], args[2]);

        // stay alive until process is killed or thread is interrupted
        Thread.sleep(Long.MAX_VALUE);
    }
}
