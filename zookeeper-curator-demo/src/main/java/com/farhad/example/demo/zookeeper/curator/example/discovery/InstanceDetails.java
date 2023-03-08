package com.farhad.example.demo.zookeeper.curator.example.discovery;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonRootName("details")
public class InstanceDetails {
    
    private String description;

    public InstanceDetails() {
        this( "" ) ;
    }
    
}
