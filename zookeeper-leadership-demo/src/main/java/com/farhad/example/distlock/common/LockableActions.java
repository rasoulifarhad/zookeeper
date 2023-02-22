package com.farhad.example.distlock.common;

import java.util.function.Supplier;

import lombok.Data;

import java.util.function.Function;

@Data
public class LockableActions<T> {
    
    private final Supplier<T> onSuccess;
    private final Supplier<T> onFailure;
    private final Function<Exception,T> onError;

}