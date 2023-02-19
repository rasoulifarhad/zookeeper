package com.farhad.example.zookeeper.leadership.aspect;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.zookeeper.leader.LeaderInitiator;
import org.aspectj.lang.ProceedingJoinPoint;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class LeaderElectionAspect {
    
    private final LeaderInitiator leaderInitiator;

     /**
     * Execute method annotated with {@link Leader} only if the current node is a leader
     * @param joinPoint
     * @throws Throwable
     */
    @Around(value = "@annotation(com.farhad.example.zookeeper.leadership.annotation)")
    public void aroundLeaderAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        if(this.leaderInitiator.getContext().isLeader()){
            log.info("=====  I'm the leader I'll execute the Scheduled tasks =====");
            joinPoint.proceed();
        }
    }
}
