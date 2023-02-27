package com.farhad.example.demo.zookeeper.self.utils;

import java.util.Random;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class RandomAmountOfWork {

    private static final int BASE = 5;

    int initTakeTime = BASE;

    private final Random random = new Random(System.currentTimeMillis());

    public int  timeItWillTake() {
        return  initTakeTime + random.nextInt(initTakeTime); // sample work takes initTakeTime-2(initTakeTime) seconds
    }
}
