package com.farhad.example.demo.zookeeper.self.taskqueue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.zookeeper.ZooKeeper;

public class Utils {
   
    public static byte[]  stringsToBytes(String[] args, int i, int length) throws IOException  {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while( i < length ) {
            baos.write(args[i++].getBytes());
            baos.write('\n');
        }
        return baos.toByteArray();

    }


}
