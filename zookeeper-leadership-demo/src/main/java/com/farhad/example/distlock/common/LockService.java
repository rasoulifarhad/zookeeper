package com.farhad.example.distlock.common;

public interface LockService {
    String lock();
    // void failLock();
    String properLock();
}
