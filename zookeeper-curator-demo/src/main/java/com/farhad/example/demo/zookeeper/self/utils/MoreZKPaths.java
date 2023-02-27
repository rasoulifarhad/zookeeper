package com.farhad.example.demo.zookeeper.self.utils;

import org.apache.curator.utils.ZKPaths;


public class MoreZKPaths {
    
    public static String makePath(String relativePath) {
        return ZKPaths.makePath(ZKPaths.PATH_SEPARATOR, relativePath);
    }

    public static String makePath(String parent, String child) {
        return ZKPaths.makePath(ZKPaths.PATH_SEPARATOR, child);
    }

}
