package com.farhad.example.demo.zookeeper.self.watch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * Conventionally, ZooKeeper applications are broken into two units, one which maintains the 
 * connection, and the other which monitors data. 
 * 
 * In this application, 
 * 
 *    - the class called the Executor maintains the ZooKeeper connection, and 
 *    - the class called the DataMonitor monitors the data in the ZooKeeper tree.
 * 
 * Also, Executor contains the main thread and contains the execution logic.
 * 
 * It is responsible for what little user interaction there is, as well as interaction with the exectuable program you 
 * pass in as an argument and which the sample (per the requirements) shuts down and restarts, according to the state 
 * of the znode.
 * 
 * The Executor object is the primary container of the sample application. It contains both the ZooKeeper object, DataMonitor,
 * 
 *  the Executor's job is to start and stop the executable whose name you pass in on the command line. 
 * 
 * It does this in response to events fired by the ZooKeeper object. 
 * 
 * When Executor.exists() is invoked, the Executor decides whether to start up or shut down per the requirements. Recall 
 * that the requires say to kill the executable when the znode ceases to exist.
 * 
 * When Executor.closing() is invoked, the Executor decides whether or not to shut itself down in response to the ZooKeeper 
 * connection permanently disappearing.
 * 
 * DataMonitor is the object that invokes these methods, in response to changes in ZooKeeper's state.
 */
@Slf4j
public class Executor implements Watcher , DataMonitorListener, Runnable {
    

    String znode ;
    private String fileName ;
    private String[] exec ; 

    private ZooKeeper zk ;
    private DataMonitor dm ;

    Process child;

    public Executor(String hostPort, String znode, String fileName, String[] exec) throws IOException {

        this.fileName = fileName;
        this.exec = exec ;

        zk = new ZooKeeper(hostPort, 300, this);
        dm = new DataMonitor(zk, znode, null, this);

    }

    @Override
    public void run() {

        try {
            synchronized ( this ) {

                while ( !dm.dead() ) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    /***************************************************************************
     * We do process any events ourselves, we just need to forward them on.
     *
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
     */    
    @Override
    public void process(WatchedEvent event) {
        dm.process(event);        
    }


    @Override
    public void closing(int rc) {
        synchronized ( this ) {
            notifyAll();
        }        
    }

    static class StreamWriter extends Thread {

        OutputStream os ;
        InputStream is ;

        StreamWriter( InputStream is, OutputStream os) {

            this.is = is ;
            this.os = os ;
            start();
        }

        public void run() {
            
            byte[] b = new byte[80];
            int rc ;
            try {
                
                while ( ( rc = is.read(b) ) > 0 ) {
                     os.write(b, 0, rc);   
                }

            } catch (IOException e) {
            }

        }
    }

    
    
    @Override
    public void exists(byte[] data) {

        if ( data == null ) {

            if ( child != null ) {
                log.info("killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    // TODO: handle exception
                }
            } 
            child = null ;
        } else {

            if ( child != null ) {
                log.info("stopping child");
                child.destroy();

                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {

                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(data);
                fos.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                log.info("Starting child");

                child = Runtime.getRuntime().exec(exec);
                new StreamWriter( child.getInputStream()    , System.out );
                new StreamWriter(  child.getErrorStream(), System.err);
                
            } catch (IOException e) {
                e.printStackTrace();
            }

        }     
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err
                    .println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
