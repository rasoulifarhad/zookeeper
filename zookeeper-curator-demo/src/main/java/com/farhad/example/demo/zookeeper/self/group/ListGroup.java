package com.farhad.example.demo.zookeeper.self.group;

import com.farhad.example.demo.zookeeper.self.utils.ConnectionWatcher;
import com.farhad.example.demo.zookeeper.self.utils.MoreZKPaths;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

import org.apache.zookeeper.KeeperException;

@Slf4j
public class ListGroup extends ConnectionWatcher {
    




    public void list( String groupName ) throws KeeperException, InterruptedException {

        String path = MoreZKPaths.makePath(groupName);

        try {

            List<String> children =  zk.getChildren(path, false);
            log.info("Children: {}", children);
        } catch ( KeeperException.NoNodeException ex ) {
            log.error("Group {} dose not exists", groupName, ex);
        }
    }


    public static void main(String[] args) throws Exception {
        ListGroup listGroup = new ListGroup();
        listGroup.connect(args[0]);
        listGroup.list(args[1]);
        listGroup.close();
    }
}
