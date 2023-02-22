package com.farhad.example.distlock.common;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class Constants {

    public static final String REDIS_PROFILE ="Redis";
    public static final String JDBC_PROFILE ="Jdbc";
    public static final String ZOOKEEPER_PROFILE ="Zookeeper";

    public static final LockableActions<String> createActions(String typeOfLock) {

        Supplier<String> onSuccess = () -> {
            log.info("successfully locked with {}",typeOfLock);
            return String.format("successfully locked with %s",typeOfLock);
        };

        Supplier<String> onFailure = () -> {
            log.info("failed to lock with {}",typeOfLock);
            return String.format("sfailed to lock with %s",typeOfLock);
        };

        Function<Exception,String> onError = ex -> {
            log.info("{} lock went off the rails on a crazy train:  {}",typeOfLock,ex);
            return String.format("%s lock went off the rails on a crazy train: %s",typeOfLock,ex);
        };

        return new LockableActions<>(onSuccess, onFailure, onError);

    }
}