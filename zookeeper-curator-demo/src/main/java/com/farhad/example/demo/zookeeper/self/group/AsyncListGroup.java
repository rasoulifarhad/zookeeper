package com.farhad.example.demo.zookeeper.self.group;

import com.farhad.example.demo.zookeeper.self.utils.ConnectionWatcher;
import com.farhad.example.demo.zookeeper.self.utils.MoreZKPaths;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;

@Slf4j
public class AsyncListGroup extends ConnectionWatcher {
    
    public void list(final String groupName) throws InterruptedException {  

        ChildrenCallback cb = new ChildrenCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                // TODO Auto-generated method stub
                
            }
            
        };

        String path = MoreZKPaths.makePath(groupName);

        final CountDownLatch latch = new CountDownLatch(1);

        zk.getChildren(path, false, 
                                (rc, path1, ctx, children)  -> {
                                    log.info("Called back for path {} with return code {}", path1, rc);
                                    if ( children == null ) {

                                        log.info("Group {} does not exist", groupName);

                                    } else {

                                        if ( children.isEmpty() ) {

                                            log.info("No members in group {}", groupName);

                                        } else {

                                            log.info("Group: {} , Childreb: {}", groupName, children);
                                        }

                                    }
                                    latch.countDown();

                                }, null  /* optional context object */);
        log.info("Awaiting latch countdown...");
        latch.await();

    }


    public static void main(String[] args) throws Exception {
        AsyncListGroup asyncListGroup = new AsyncListGroup();
        asyncListGroup.connect(args[0]);
        asyncListGroup.list(args[1]);
        asyncListGroup.close();
    }
}
