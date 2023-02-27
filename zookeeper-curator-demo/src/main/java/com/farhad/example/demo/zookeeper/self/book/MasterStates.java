package com.farhad.example.demo.zookeeper.self.book;

/*
 * A master process can be either running for
 * primary master, elected primary master, or
 * not elected, in which case it is a backup
 * master.  
 */
public enum MasterStates {
    RUNNING, ELECTED, NOTELECTED
}
