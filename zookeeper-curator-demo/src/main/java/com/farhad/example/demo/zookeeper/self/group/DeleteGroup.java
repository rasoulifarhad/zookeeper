package com.farhad.example.demo.zookeeper.self.group;

import com.farhad.example.demo.zookeeper.self.utils.ConnectionWatcher;
import com.farhad.example.demo.zookeeper.self.utils.MoreZKPaths;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

import org.apache.zookeeper.KeeperException;

@Slf4j
public class DeleteGroup extends ConnectionWatcher {

    void tt(){ 
    }
    
    public void delete( String groupName ) throws KeeperException, InterruptedException  {

        String path = MoreZKPaths.makePath(groupName);

        try {

            List<String> children =  zk.getChildren(path, false);

            for ( String child :  children ) {
                zk.delete(MoreZKPaths.makePath(path, child), -1);
            }
            zk.delete(path, -1);
    
            log.info("Deleted Group: {} at path: {}", groupName, path);
    

        } catch ( KeeperException.NoNodeException ex ) {

            log.error("Group {} dos not exist", groupName,ex);
        }

    }

    public static void main(String[] args) throws Exception {
        DeleteGroup deleteGroup = new DeleteGroup();
        deleteGroup.connect(args[0]);
        deleteGroup.delete(args[1]);
        deleteGroup.close();
    }
}
