package com.farhad.example.distlock.redis.service;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import com.farhad.example.distlock.common.Constants;
import com.farhad.example.distlock.common.LockService;
import java.util.concurrent.TimeUnit;
import com.farhad.example.distlock.common.TupleTime;

// import java.util.UUID;
// import java.util.concurrent.Executors;
// import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile(Constants.REDIS_PROFILE)
@RequiredArgsConstructor
@Slf4j
public class RedisLockService implements LockService{

    private static final TupleTime DEFAULT_WAIT = new TupleTime(0, TimeUnit.NANOSECONDS);

    private static final String LOCK_KEY = "lockKey";
    private final LockRegistry lockRegistry ;
    @Override
    public String lock() {
        return properLock() ;
    }

    public String properLock() {
        Lock lock = null ;
        try{
            lock = lockRegistry.obtain(LOCK_KEY);
        } catch(Exception exx) {
            log.info("Unable to obtain lock: {}",LOCK_KEY);
            return String.format("Can not obtain %s from lock registry" ,LOCK_KEY);
        }
        String retVal = null ;
        try{
            if (lock.tryLock()) {
                log.info("jdbc lock sucessfull");
                retVal =  "jdbc lock sucessfull" ;
            } else {
                log.info("jdbc lock unsucessfull");
                retVal =  "jdbc lock unsucessfull" ;
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return retVal ;
    }

    // @Override
    // public void failLock() {
    //     // TODO Auto-generated method stub
        
    // }
    
     
}
