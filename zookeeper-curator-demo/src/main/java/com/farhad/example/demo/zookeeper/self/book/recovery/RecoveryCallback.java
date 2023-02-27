package com.farhad.example.demo.zookeeper.self.book.recovery;

import java.util.List;

/**
* Callback interface. Called once 
* recovery completes or fails.
*
*/
public interface RecoveryCallback {
    
    final static int OK = 0;
    final static int FAILED = -1;

    public void recoveryComplete(int rc, List<String> tasks);
}
