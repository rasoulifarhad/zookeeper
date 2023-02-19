package com.farhad.example.si.zookeeper.annotation;

public class TestAnnotation {
    
    /**
     * In the  example, every invocation of the test() method results in a message with a payload created from its return value.
     * 
     * Each message is sent to the channel named auditChannel.
     * 
     * One of the benefits of this technique is that you can avoid the duplication of the same channel name across multiple annotations. 
     * You also can provide a level of indirection between your own, potentially domain-specific, annotations and those provided by the 
     * framework.
     * 
     * You can also annotate the class, which lets you apply the properties of this annotation on every public method of that class.
     */
    @Audit
    public String test() {
        return "hiiiiiiiii";
    }
}
