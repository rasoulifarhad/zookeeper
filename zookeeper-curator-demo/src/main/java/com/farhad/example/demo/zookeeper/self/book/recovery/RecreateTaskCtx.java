package com.farhad.example.demo.zookeeper.self.book.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Context for recreate operation.
 */
@Getter
public class RecreateTaskCtx {
    
    String path;
    String task;
    byte[] data;

    public RecreateTaskCtx(String path, String task, byte[] data) {
        this.path = path;
        this.task = task;
        this.data = data;
    }
}
