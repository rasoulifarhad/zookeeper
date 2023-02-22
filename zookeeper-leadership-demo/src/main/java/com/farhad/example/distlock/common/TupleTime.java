package com.farhad.example.distlock.common;

import java.util.concurrent.TimeUnit;
import lombok.Data;

@Data
public class TupleTime {
    
    private final long duration;
    private final TimeUnit timeUnit;

}
