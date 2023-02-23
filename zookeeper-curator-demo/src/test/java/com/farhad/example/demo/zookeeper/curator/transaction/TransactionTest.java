package com.farhad.example.demo.zookeeper.curator.transaction;

import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import org.junit.jupiter.api.Test;

import com.farhad.example.demo.zookeeper.curator.AbstractTest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
@Slf4j
public class TransactionTest extends AbstractTest{

    @Test
    public void transaction_test() {
        
        try ( CuratorFramework client = getClient() ) {

            doTransactionWith(client);

        } catch(Exception ex){
            ex.printStackTrace();
        }

    }

    private Collection<CuratorTransactionResult>  doTransactionWith(CuratorFramework client) throws Exception {

        Collection<CuratorTransactionResult> results = 
                        client.transaction()
                                .forOperations(
                                    client.transactionOp()
                                            .create()
                                            .forPath("/curatorTest", 
                                                            "curatorTest".getBytes()),
                                    client.transactionOp()
                                            .create()
                                            .forPath("/curatorTest/farhad", 
                                                            "curatorTest-farhad".getBytes()),
                                    client.transactionOp()
                                            .create()
                                            .forPath("/curatorTest/farhad/example", 
                                                        "curatorTest-farhad-example".getBytes()),
                                    client.transactionOp()
                                            .create()
                                            .forPath("/curatorTest/farhad/example/transaction", 
                                                            "curatorTest-farhad-example-transaction".getBytes()));

        for(CuratorTransactionResult result  : results) {
            log.info("{} - {}", result.getForPath(),result.getType());
        }

        return results;


    }
    
}
