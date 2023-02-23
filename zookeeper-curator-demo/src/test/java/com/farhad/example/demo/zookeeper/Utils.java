package com.farhad.example.demo.zookeeper;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void sleep(int seconds) {
        try{
            TimeUnit.SECONDS.sleep(seconds);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
