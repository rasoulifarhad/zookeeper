package com.farhad.example.zookeeper.leadership.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate method that will be executed in the same time by many instances
 * Only the leader will execute method annotated with {@link Leader}
 *
 * @author Abdelghani ROUSSI
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Leader {
    
}
