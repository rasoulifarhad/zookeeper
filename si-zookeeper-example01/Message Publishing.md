### Message Publishing
#
# The (Aspect-oriented Programming) AOP message publishing feature lets you construct and send a message as a by-product of a method invocation. 
#
# For example, imagine you have a component and, every time the state of this component changes, you want to be notified by a message. The 
# easiest way to send such notifications is to send a message to a dedicated channel, but how would you connect the method invocation that 
# changes the state of the object to a message sending process, and how should the notification message be structured? The AOP message 
# publishing feature handles these responsibilities with a configuration-driven approach. 
#
## Annotation-driven Configuration with the @Publisher Annotation
#
# The annotation-driven approach lets you annotate any method with the @Publisher annotation to specify a 'channel' attribute.
#
# to switch this functionality on, you must use the @EnablePublisher annotation on some @Configuration class.
#
# The message is constructed from the return value of the method invocation and sent to the channel specified by the 'channel' attribute.
#
# To further manage message structure, you can also use a combination of both @Payload and @Header annotations.
#
# Internally, this message publishing feature of Spring Integration uses both Spring AOP by defining PublisherAnnotationAdvisor and the 
# Spring Expression Language (SpEL), giving you considerable flexibility and control over the structure of the Message it publishes.
#
# The PublisherAnnotationAdvisor defines and binds the following variables:
#   
# return: Binds to a return value, letting you reference it or its attributes (for example, #return.something, where 'something' is an attribute of the object bound to #return)
#   
# exception: Binds to an exception if one is thrown by the method invocation
#   
# args: Binds to method arguments so that you can extract individual arguments by name (for example, #args.fname)
#   
# Consider the following example:
#   
#   @Publisher
#   public String defaultPayload(String fname, String lname) {
#     return fname + " " + lname;
#   }
#
# In the preceding example, the message is constructed with the following structure:
#   
# The message payload is the return type and value of the method. This is the default.
#   
# A newly constructed message is sent to a default publisher channel that is configured with an annotation post processor (covered later in this section).
#   
# The following example is the same as the preceding example, except that it does not use a default publishing channel:
#   
#   @Publisher(channel="testChannel")
#   public String defaultPayload(String fname, @Header("last") String lname) {
#     return fname + " " + lname;
#   }
#   
# Instead of using a default publishing channel, we specify the publishing channel by setting the 'channel' attribute of the @Publisher annotation. We also 
# add a @Header annotation, which results in the message header named 'last' having the same value as the 'lname' method parameter. That header is added to 
# the newly constructed message.
#   
# The following example is almost identical to the preceding example:
#    
#   @Publisher(channel="testChannel")
#   @Payload
#   public String defaultPayloadButExplicitAnnotation(String fname, @Header String lname) {
#     return fname + " " + lname;
#   }
#   
# The only difference is that we use a @Payload annotation on the method to explicitly specify that the return value of the method should be used as the 
# payload of the message.
#   
# The following example expands on the previous configuration by using the Spring Expression Language in the @Payload annotation to further instruct the 
# framework about how the message should be constructed:
#   
#   @Publisher(channel="testChannel")
#   @Payload("#return + #args.lname")
#   public String setName(String fname, String lname, @Header("x") int num) {
#     return fname + " " + lname;
#   }
#   
# In the preceding example, the message is a concatenation of the return value of the method invocation and the 'lname' input argument. The Message header 
# named 'x' has its value determined by the 'num' input argument. That header is added to the newly constructed message.
#   
#   @Publisher(channel="testChannel")
#   public String argumentAsPayload(@Payload String fname, @Header String lname) {
#     return fname + " " + lname;
#   }
#   
# In the preceding example, you see another usage of the @Payload annotation. Here, we annotate a method argument that becomes the payload of the newly 
# constructed message.
#
# As with most other annotation-driven features in Spring, you need to register a post-processor (PublisherAnnotationBeanPostProcessor). The following 
# example shows how to do so:
#   
#   <bean class="org.springframework.integration.aop.PublisherAnnotationBeanPostProcessor"/>
#   
# For a more concise configuration, you can instead use namespace support, as the following example shows:
#   
#   <int:annotation-config>
#       <int:enable-publisher default-publisher-channel="defaultChannel"/>
#   </int:annotation-config>
#   
# For Java configuration, you must use the @EnablePublisher annotation, as the following example shows:
#   
#   @Configuration
#   @EnableIntegration
#   @EnablePublisher("defaultChannel")
#   public class IntegrationConfiguration {
#       ...
#   }
#   
#   
#
# Similar to other Spring annotations (@Component, @Scheduled, and so on), you can also use @Publisher as a meta-annotation. This means that you can define 
# your own annotations that are treated in the same way as the @Publisher itself. The following example shows how to do so:
#   
#   @Target({ElementType.METHOD, ElementType.TYPE})
#   @Retention(RetentionPolicy.RUNTIME)
#   @Publisher(channel="auditChannel")
#   public @interface Audit {
#   ...
#   }
#   
# In the preceding example, we define the @Audit annotation, which is itself annotated with @Publisher. Also note that you can define a channel attribute on 
# the meta-annotation to encapsulate where messages are sent inside of this annotation. Now you can annotate any method with the @Audit annotation, as the 
# following example shows:
#   
#   @Audit
#   public String test() {
#       return "Hello";
#   }
#   
# In the preceding example, every invocation of the test() method results in a message with a payload created from its return value. Each message is sent to 
# the channel named auditChannel. One of the benefits of this technique is that you can avoid the duplication of the same channel name across multiple 
# annotations. You also can provide a level of indirection between your own, potentially domain-specific, annotations and those provided by the framework.
#   
# You can also annotate the class, which lets you apply the properties of this annotation on every public method of that class, as the following example shows:
#   
#   @Audit
#   static class BankingOperationsImpl implements BankingOperations {
#   
#     public String debit(String amount) {
#        . . .
#   
#     }
#   
#     public String credit(String amount) {
#        . . .
#     }
#   
#   }
#
## Asynchronous Publishing
#
# Publishing occurs in the same thread as your component’s execution. So, by default, it is synchronous. This means that the entire message flow has to wait until 
# the publisher’s flow completes. However, developers often want the complete opposite: to use this message-publishing feature to initiate asynchronous flows. For 
# example, you might host a service (HTTP, WS, and so on) which receives a remote request. You may want to send this request internally into a process that might 
# take a while. However, you may also want to reply to the user right away. So, instead of sending inbound requests for processing to the output channel (the 
# conventional way), you can use 'output-channel' or a 'replyChannel' header to send a simple acknowledgment-like reply back to the caller while using the 
# message-publisher feature to initiate a complex flow.
#   
# The service in the following example receives a complex payload (which needs to be sent further for processing), but it also needs to reply to the caller with 
# a simple acknowledgment:
#   
#   public String echo(Object complexPayload) {
#        return "ACK";
#   }
#   
# So, instead of hooking up the complex flow to the output channel, we use the message-publishing feature instead. We configure it to create a new message, by 
# using the input argument of the service method (shown in the preceding example), and send that to the 'localProcessChannel'. To make sure this flow is 
# asynchronous, all we need to do is send it to any type of asynchronous channel (ExecutorChannel in the next example). The following example shows how to an 
# asynchronous publishing-interceptor:
#   
#   <int:service-activator  input-channel="inputChannel" output-channel="outputChannel" ref="sampleservice"/>
#   
#   <bean id="sampleservice" class="test.SampleService"/>
#   
#   <aop:config>
#     <aop:advisor advice-ref="interceptor" pointcut="bean(sampleservice)" />
#   </aop:config>
#   
#   <int:publishing-interceptor id="interceptor" >
#     <int:method pattern="echo" payload="#args[0]" channel="localProcessChannel">
#       <int:header name="sample_header" expression="'some sample value'"/>
#     </int:method>
#   </int:publishing-interceptor>
#   
#   <int:channel id="localProcessChannel">
#     <int:dispatcher task-executor="executor"/>
#   </int:channel>
#   
#   <task:executor id="executor" pool-size="5"/>
#   
# Another way of handling this type of scenario is with a wire-tap. See Wire Tap.
#
#
## Producing and Publishing Messages Based on a Scheduled Trigger
#
# In the preceding sections, we looked at the message-publishing feature, which constructs and publishes messages as by-products of method invocations. However, 
# in those cases, you are still responsible for invoking the method. Spring Integration 2.0 added support for scheduled message producers and publishers with the 
# new expression attribute on the 'inbound-channel-adapter' element. You can schedule based on several triggers, any one of which can be configured on the 'poller' 
# element. Currently, we support cron, fixed-rate, fixed-delay and any custom trigger implemented by you and referenced by the 'trigger' attribute value.
#   
# As mentioned earlier, support for scheduled producers and publishers is provided via the <inbound-channel-adapter> XML element. Consider the following example:
#   
#   <int:inbound-channel-adapter id="fixedDelayProducer"
#          expression="'fixedDelayTest'"
#          channel="fixedDelayChannel">
#       <int:poller fixed-delay="1000"/>
#   </int:inbound-channel-adapter>
#   
# The preceding example creates an inbound channel adapter that constructs a Message, with its payload being the result of the expression defined in the expression 
# attribute. Such messages are created and sent every time the delay specified by the fixed-delay attribute occurs.
#   
# The following example is similar to the preceding example, except that it uses the fixed-rate attribute:
#   
#   <int:inbound-channel-adapter id="fixedRateProducer"
#          expression="'fixedRateTest'"
#          channel="fixedRateChannel">
#       <int:poller fixed-rate="1000"/>
#   </int:inbound-channel-adapter>
#   
# The fixed-rate attribute lets you send messages at a fixed rate (measuring from the start time of each task).
#   
# The following example shows how you can apply a Cron trigger with a value specified in the cron attribute:
#   
#   <int:inbound-channel-adapter id="cronProducer"
#          expression="'cronTest'"
#          channel="cronChannel">
#       <int:poller cron="7 6 5 4 3 ?"/>
#   </int:inbound-channel-adapter>
#
# The following example shows how to insert additional headers into the message:
#   
#   <int:inbound-channel-adapter id="headerExpressionsProducer"
#          expression="'headerExpressionsTest'"
#          channel="headerExpressionsChannel"
#          auto-startup="false">
#       <int:poller fixed-delay="5000"/>
#       <int:header name="foo" expression="6 * 7"/>
#       <int:header name="bar" value="x"/>
#   </int:inbound-channel-adapter>
#
# The additional message headers can take scalar values or the results of evaluating Spring expressions.
#
# If you need to implement your own custom trigger, you can use the trigger attribute to provide a reference to any spring configured bean that implements the 
# org.springframework.scheduling.Trigger # interface. The following example shows how to do so:
#   
#   <int:inbound-channel-adapter id="triggerRefProducer"
#          expression="'triggerRefTest'" channel="triggerRefChannel">
#       <int:poller trigger="customTrigger"/>
#   </int:inbound-channel-adapter>
#   
#   <beans:bean id="customTrigger" class="o.s.scheduling.support.PeriodicTrigger">
#       <beans:constructor-arg value="9999"/>
#   </beans:bean>
#
#
#

