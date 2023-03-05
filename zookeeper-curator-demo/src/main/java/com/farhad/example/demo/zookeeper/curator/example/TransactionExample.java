package com.farhad.example.demo.zookeeper.curator.example;

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionExample {
 
    private static final String TXN_PATH = "/com/farhad/example/basic/transaction/createOps";
    private static final String ANOTHER_TXN_PATH = "/com/farhad/example/basic/transaction/anotherCreateOps";
    private static final String MULTI_TXN_PATH = "/com/farhad/example/basic/multiTransaction";

    public static CuratorTransaction startTransaction( CuratorFramework client ) {
        // start the transaction builder
        
        return client.inTransaction() ;
    }

     public static CuratorTransactionFinal addCreateToTransaction( CuratorTransaction transaction  ) throws Exception {

        // add a create operation

        return transaction
                    .create()
                    .forPath(TXN_PATH, String.format( "create path %s  operation", TXN_PATH ).getBytes() )
                    .and();
    }

    public static CuratorTransactionFinal addAnotherCreateToTransaction( CuratorTransaction transaction  ) throws Exception {

        // add a create operation

        return transaction
                    .create()
                    .forPath(ANOTHER_TXN_PATH   , String.format( "create path %s  operation", ANOTHER_TXN_PATH ).getBytes() )
                    .and();
    }

    public static CuratorTransactionFinal addDeleteToTransaction( CuratorTransaction transaction  ) throws Exception {

        // add a delete operation

        return transaction
                    .delete()
                    .forPath(TXN_PATH  )
                    .and();
    }

    public static CuratorTransactionFinal addAnotherDeleteToTransaction( CuratorTransaction transaction  ) throws Exception {

        // add a delete operation

        return transaction
                    .delete()
                    .forPath( ANOTHER_TXN_PATH  )
                    .and();
    }

    public static Collection<CuratorTransactionResult> commitTransaction( CuratorTransactionFinal transaction  ) throws Exception {

        // commit the transaction
        return transaction.commit();
        
    }

    private static Collection<CuratorTransactionResult> transaction( CuratorFramework client ) throws Exception {

        Collection<CuratorTransactionResult> results = 
                                                commitTransaction( 
                                                    addAnotherDeleteToTransaction( 
                                                        addDeleteToTransaction( 
                                                            addAnotherCreateToTransaction( 
                                                                addCreateToTransaction( 
                                                                    startTransaction( client) ) ) ) ) );
        return results;
    }

    private static Collection<CuratorTransactionResult> mltiTransaction( CuratorFramework client ) throws Exception {

        CuratorOp createOp =  client.transactionOp().create().forPath(ANOTHER_TXN_PATH) ;
        CuratorOp deleteOp = client.transactionOp().delete().forPath(ANOTHER_TXN_PATH) ;


        Collection<CuratorTransactionResult> results = 
                                                client.transaction()
                                                            .forOperations(
                                                                    createOp,
                                                                    deleteOp);
        return results;
                    
    }
   
    public static void main(String[] args) {
        

        String hostPort = "127.0.0.1:2181";
        CuratorFramework client = null ;

        try {

            client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(1000, 3));
            client.getCuratorListenable().addListener(new CuratorListener() {

                @Override
                public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("####  CuratorEvent: {}",event.getType().name());                    
                } 
                
            });
            client.start();

            // Collection<CuratorTransactionResult> results = transaction(client);
            
            // for (CuratorTransactionResult curatorTransactionResult : results) {
            //     log.info("#### {} - {}  ( result: {} - {})", curatorTransactionResult.getForPath(), 
            //                                 curatorTransactionResult.getType(),
            //                                 curatorTransactionResult.getResultPath(),
            //                                 curatorTransactionResult.getResultStat());
            // }

            Collection<CuratorTransactionResult> multiResults = mltiTransaction(client);
            
            for (CuratorTransactionResult curatorTransactionResult : multiResults) {
                log.info("#### {} - {}  ( result: {} - {})", curatorTransactionResult.getForPath(), 
                                            curatorTransactionResult.getType(),
                                            curatorTransactionResult.getResultPath(),
                                            curatorTransactionResult.getResultStat());
            }

            Thread.sleep(20_0000);    


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            CloseableUtils.closeQuietly(client);
        }

    }
}
