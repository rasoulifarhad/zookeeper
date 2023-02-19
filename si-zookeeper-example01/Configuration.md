### Configuration
#
## Configuring the Task Scheduler
#
# In Spring Integration, the ApplicationContext plays the central role of a message bus, and you need to consider only a couple of configuration 
# options. First, you may want to control the central TaskScheduler instance. You can do so by providing a single bean named taskScheduler. This 
# is also defined as a constant, as follows:
#
#    IntegrationContextUtils.TASK_SCHEDULER_BEAN_NAME
#
# By default, Spring Integration relies on an instance of ThreadPoolTaskScheduler
#
# That default TaskScheduler starts up automatically with a pool of ten threads, but see Global Properties. If you provide your own TaskScheduler 
# instance instead, you can set the 'autoStartup' property to false or provide your own pool size value.
#
# When polling consumers provide an explicit task executor reference in their configuration, the invocation of the handler methods happens within 
# that executor’s thread pool and not the main scheduler pool. However, when no task executor is provided for an endpoint’s poller, it is invoked 
# by one of the main scheduler’s threads.
#
## Global Properties
#
# Certain global framework properties can be overridden by providing a properties file on the classpath.
#
# The default properties can be found in org.springframework.integration.context.IntegrationProperties class.
#
# The following listing shows the default values:
#
# 1. spring.integration.channels.autoCreate=true 
# 2. spring.integration.channels.maxUnicastSubscribers=0x7fffffff 
# 3. spring.integration.channels.maxBroadcastSubscribers=0x7fffffff 
# 4. spring.integration.taskScheduler.poolSize=10 
# 5. spring.integration.messagingTemplate.throwExceptionOnLateReply=false 
# 6. spring.integration.readOnly.headers= 
# 7. spring.integration.endpoints.noAutoStartup= 
# 8. spring.integration.channels.error.requireSubscribers=true 
# 9. spring.integration.channels.error.ignoreFailures=true 
#
# 1. When true, input-channel instances are automatically declared as DirectChannel instances when not explicitly found in the application context.
#
# 2. Sets the default number of subscribers allowed on, for example, a DirectChannel. It can be used to avoid inadvertently subscribing multiple 
# endpoints to the same channel. You can override it on individual channels by setting the max-subscribers attribute.

# 3. This property provides the default number of subscribers allowed on, for example, a PublishSubscribeChannel. It can be used to avoid inadvertently 
# subscribing more than the expected number of endpoints to the same channel. You can override it on individual channels by setting the max-subscribers 
# attribute.
#
# 4. The number of threads available in the default taskScheduler bean. See Configuring the Task Scheduler.
#
# 5. When true, messages that arrive at a gateway reply channel throw an exception when the gateway is not expecting a reply (because the sending thread 
# has timed out or already received a reply).
#
# 6. A comma-separated list of message header names that should not be populated into Message instances during a header copying operation. The list is used 
# by the DefaultMessageBuilderFactory bean and propagated to the IntegrationMessageHeaderAccessor instances (see MessageHeaderAccessor API) used to build 
# messages via MessageBuilder (see The MessageBuilder Helper Class). By default, only MessageHeaders.ID and MessageHeaders.TIMESTAMP are not copied during 
# message building. Since version 4.3.2.
#
# 6. A comma-separated list of AbstractEndpoint bean names patterns (xxx*, xxx, *xxx or xxx*yyy) that should not be started automatically during application 
# startup. You can manually start these endpoints later by their bean name through a Control Bus (see Control Bus), by their role with the SmartLifecycleRoleController '
# (see Endpoint Roles), or by Lifecycle bean injection. You can explicitly override the effect of this global property by specifying auto-startup XML annotation or 
# the autoStartup annotation attribute or by calling AbstractEndpoint.setAutoStartup() in the bean definition. Since version 4.3.12.
#
# 7. A boolean flag to indicate that default global errorChannel must be configured with the requireSubscribers option. Since version 5.4.3. See Error Handling 
# for more information.

# 8. A boolean flag to indicate that default global errorChannel must ignore dispatching errors and pass the message to the next handler. Since version 5.5.
#
# Starting with version 5.1, all the merged global properties are printed in the logs after application context startup when a DEBUG logic level 
# is turned on for the org.springframework.integration category. The output looks like this:
#   
#   Spring Integration global properties:
#   
#   spring.integration.endpoints.noAutoStartup=fooService*
#   spring.integration.taskScheduler.poolSize=20
#   spring.integration.channels.maxUnicastSubscribers=0x7fffffff
#   spring.integration.channels.autoCreate=true
#   spring.integration.channels.maxBroadcastSubscribers=0x7fffffff
#   spring.integration.readOnly.headers=
#   spring.integration.messagingTemplate.throwExceptionOnLateReply=true
#
## Annotation Support
#
# In addition to the XML namespace support for configuring message endpoints, you can also use annotations. First, Spring Integration provides the 
# class-level @MessageEndpoint as a stereotype annotation, meaning that it is itself annotated with Spring’s @Component annotation and is therefore 
# automatically recognized as a bean definition by Spring’s component scanning.
#   
# Even more important are the various method-level annotations. They indicate that the annotated method is capable of handling a message. The following 
# example demonstrates both class-level and method-level annotations:
#   
#   @MessageEndpoint
#   public class FooService {
#   
#       @ServiceActivator
#       public void processMessage(Message message) {
#           ...
#       }
#   }
#
# Exactly what it means for the method to “handle” the Message depends on the particular annotation. Annotations available in Spring Integration include:
#   
#   - @Aggregator (see Aggregator)
#    
#   - @Filter (see Filter)
#    
#   - @Router (see Routers)
#    
#   - @ServiceActivator (see Service Activator)
#    
#   - @Splitter (see Splitter)
#    
#   - @Transformer (see Transformer)
#    
#   - @InboundChannelAdapter (see Channel Adapter)
#    
#   - @BridgeFrom (see Configuring a Bridge with Java Configuration)
#    
#   - @BridgeTo (see Configuring a Bridge with Java Configuration)
#    
#   - @MessagingGateway (see Messaging Gateways)
#    
#   - @IntegrationComponentScan (see Configuration and @EnableIntegration)
#
# In most cases, the annotated handler method should not require the Message type as its parameter. Instead, the method parameter type can match 
# the message’s payload type, as the following example shows:
#   
#   public class ThingService {
#   
#       @ServiceActivator
#       public void bar(Thing thing) {
#           ...
#       }
#   
#   }
#
# When the method parameter should be mapped from a value in the MessageHeaders, another option is to use the parameter-level @Header annotation. In 
# general, methods annotated with the Spring Integration annotations can accept the Message itself, the message payload, or a header value (with @Header) 
# as the parameter. In fact, the method can accept a combination, as the following example shows:
#   
#   public class ThingService {
#   
#       @ServiceActivator
#       public void otherThing(String payload, @Header("x") int valueX, @Header("y") int valueY) {
#           ...
#       }
#   
#   }
#   
# You can also use the @Headers annotation to provide all the message headers as a Map, as the following example shows:
#   
#   public class ThingService {
#   
#       @ServiceActivator
#       public void otherThing(String payload, @Headers Map<String, Object> headerMap) {
#           ...
#       }
#   
#   }
#
# For several of these annotations, when a message-handling method returns a non-null value, the endpoint tries to send a reply. This is consistent across 
# both configuration options (namespace and annotations) in that such an endpoint’s output channel is used (if available), and the REPLY_CHANNEL message 
# header value is used as a fallback.
#
# The combination of output channels on endpoints and the reply channel message header enables a pipeline approach, where multiple components have an output 
# channel and the final component allows the reply message to be forwarded to the reply channel (as specified in the original request message). In other words, 
# the final component depends on the information provided by the original sender and can dynamically support any number of clients as a result. This is an 
# example of the return address pattern.
#
# In addition to the examples shown here, these annotations also support the inputChannel and outputChannel properties, as the following example shows:
#   
#   @Service
#   public class ThingService {
#   
#       @ServiceActivator(inputChannel="input", outputChannel="output")
#       public void otherThing(String payload, @Headers Map<String, Object> headerMap) {
#           ...
#       }
#   
#   }
#
# The processing of these annotations creates the same beans as the corresponding XML components — AbstractEndpoint instances and MessageHandler instances 
# (or MessageSource instances for the inbound channel adapter). 
#
# The bean names are generated from the following pattern: [componentName].[methodName].[decapitalizedAnnotationClassShortName].
#
# In the preceding example the bean name is thingService.otherThing.serviceActivator for the AbstractEndpoint and the same name with an additional 
# .handler (.source) suffix for the MessageHandler (MessageSource) bean.
#
# Such a name can be customized using an @EndpointId annotation alongside with these messaging annotations. 
#
# The MessageHandler instances (MessageSource instances) are also eligible to be tracked by the message history.
#
# Starting with version 4.0, all messaging annotations provide SmartLifecycle options (autoStartup and phase) to allow endpoint lifecycle control on 
# application context initialization. 
#
# They default to true and 0, respectively.
#
# To change the state of an endpoint (such as start() or stop()), you can obtain a reference to the endpoint bean by using the BeanFactory (or autowiring) 
# and invoke the methods. 
# 
# Alternatively, you can send a command message to the Control Bus (see Control Bus). 
#
# For these purposes, you should use the beanName mentioned earlier in the preceding paragraph.
#
# Channels automatically created after parsing the mentioned annotations (when no specific channel bean is configured), and the corresponding consumer endpoints, 
# are declared as beans near the end of the context initialization. These beans can be autowired in other services, but they have to be marked with the @Lazy 
# annotation because the definitions, typically, won’t yet be available during normal autowiring processing.
#  
#  @Autowired
#  @Lazy
#  @Qualifier("someChannel")
#  MessageChannel someChannel;
#  ...
#  
#  @Bean
#  Thing1 dependsOnSPCA(@Qualifier("someInboundAdapter") @Lazy SourcePollingChannelAdapter someInboundAdapter) {
#      ...
#  }
#
# Starting with version 6.0, all the messaging annotations are @Repeatable now, so several of the same type can be declared on the same service method with 
# the meaning to create as many endpoints as those annotations are repeated:
#  
#  @Transformer(inputChannel = "inputChannel1", outputChannel = "outputChannel1")
#  @Transformer(inputChannel = "inputChannel2", outputChannel = "outputChannel2")
#  public String transform(String input) {
#      return input.toUpperCase();
#  }
#
## Using the @Poller Annotation
#
#  public class AnnotationService {
#  
#      @Transformer(inputChannel = "input", outputChannel = "output",
#          poller = @Poller(maxMessagesPerPoll = "${poller.maxMessagesPerPoll}", fixedDelay = "${poller.fixedDelay}"))
#      public String handle(String payload) {
#          ...
#      }
#  }
#
# The @Poller annotation provides only simple PollerMetadata options. You can configure the @Poller annotation’s attributes (maxMessagesPerPoll, fixedDelay, 
# fixedRate, and cron) with property placeholders. 
#
# Also, starting with version 5.1, the receiveTimeout option for PollingConsumer s is also provided.
#
#  If it is necessary to provide more polling options (for example, transaction, advice-chain, error-handler, and others), you should configure the PollerMetadata 
# as a generic bean and use its bean name as the @Poller 's value attribute. In this case, no other attributes are allowed (they must be specified on the 
# PollerMetadata bean). 
#
# Note, if inputChannel is a PollableChannel and no @Poller is configured, the default PollerMetadata is used (if it is present in the application context). To 
# declare the default poller by using a @Configuration annotation, use code similar to the following example:
#   
#   @Bean(name = PollerMetadata.DEFAULT_POLLER)
#   public PollerMetadata defaultPoller() {
#       PollerMetadata pollerMetadata = new PollerMetadata();
#       pollerMetadata.setTrigger(new PeriodicTrigger(10));
#       return pollerMetadata;
#   }
#   
# The following example shows how to use the default poller:
#   
#   public class AnnotationService {
#   
#       @Transformer(inputChannel = "aPollableChannel", outputChannel = "output")
#       public String handle(String payload) {
#           ...
#       }
#   }
#   
# The following example shows how to use a named poller:
#   
#   @Bean
#   public PollerMetadata myPoller() {
#       PollerMetadata pollerMetadata = new PollerMetadata();
#       pollerMetadata.setTrigger(new PeriodicTrigger(1000));
#       return pollerMetadata;
#   }
#   
# The following example shows an endpoint that uses the default poller:
#   
#   public class AnnotationService {
#   
#       @Transformer(inputChannel = "aPollableChannel", outputChannel = "output"
#                              poller = @Poller("myPoller"))
#       public String handle(String payload) {
#            ...
#       }
#   }
#   
# Starting with version 4.3.3, the @Poller annotation has the errorChannel attribute for easier configuration of the underlying MessagePublishingErrorHandler. 
# This attribute plays the same role as error-channel in the <poller> XML component. See Endpoint Namespace Support for more information.
#   
# The poller() attribute on the messaging annotations is mutually exclusive with the reactive() attribute. See next section for more information.
#
## Using @Reactive Annotation
#
# The ReactiveStreamsConsumer has been around since version 5.0, but it was applied only when an input channel for the endpoint is a FluxMessageChannel (or 
# any org.reactivestreams.Publisher implementation). Starting with version 5.3, its instance is also created by the framework when the target message handler 
# is a ReactiveMessageHandler independently of the input channel type. The @Reactive sub-annotation (similar to mentioned above @Poller) has been introduced 
# for all the messaging annotations starting with version 5.5. It accepts an optional Function<? super Flux<Message<?>>, ? extends Publisher<Message<?>>> bean 
# reference and, independently of the input channel type and message handler, turns the target endpoint into the ReactiveStreamsConsumer instance. The function 
# is used from the Flux.transform() operator to apply some customization (publishOn(), doOnNext(), log(), retry() etc.) on a reactive stream source from the 
# input channel.
#   
# The following example demonstrates how to change the publishing thread from the input channel independently of the final subscriber and producer to that 
# DirectChannel:
#   
#   @Bean
#   public Function<Flux<?>, Flux<?>> publishOnCustomizer() {
#       return flux -> flux.publishOn(Schedulers.parallel());
#   }
#   
#   @ServiceActivator(inputChannel = "directChannel", reactive = @Reactive("publishOnCustomizer"))
#   public void handleReactive(String payload) {
#       ...
#   }
#   
# The reactive() attribute on the messaging annotations is mutually exclusive with the poller() attribute. See Using the @Poller Annotation and Reactive Streams 
# Support for more information.
#   
## Using the @InboundChannelAdapter Annotation
Version 4.0 introduced the @InboundChannelAdapter method-level annotation. It produces a SourcePollingChannelAdapter integration component based on a MethodInvokingMessageSource for the annotated method. This annotation is an analogue of the <int:inbound-channel-adapter> XML component and has the same restrictions: The method cannot have parameters, and the return type must not be void. It has two attributes: value (the required MessageChannel bean name) and poller (an optional @Poller annotation, as described earlier). If you need to provide some MessageHeaders, use a Message<?> return type and use a MessageBuilder to build the Message<?>. Using a MessageBuilder lets you configure the MessageHeaders. The following example shows how to use an @InboundChannelAdapter annotation:

@InboundChannelAdapter("counterChannel")
public Integer count() {
    return this.counter.incrementAndGet();
}

@InboundChannelAdapter(value = "fooChannel", poller = @Poller(fixed-rate = "5000"))
public String foo() {
    return "foo";
}

Version 4.3 introduced the channel alias for the value annotation attribute, to provide better source code readability. Also, the target MessageChannel bean is resolved in the SourcePollingChannelAdapter by the provided name (set by the outputChannelName option) on the first receive() call, not during the initialization phase. It allows “late binding” logic: The target MessageChannel bean from the consumer perspective is created and registered a bit later than the @InboundChannelAdapter parsing phase.

The first example requires that the default poller has been declared elsewhere in the application context.

Using the @MessagingGateway Annotation

See @MessagingGateway Annotation.   
#
## Using the @IntegrationComponentScan Annotation
The standard Spring Framework @ComponentScan annotation does not scan interfaces for stereotype @Component annotations. To overcome this limitation and allow the configuration of @MessagingGateway (see @MessagingGateway Annotation), we introduced the @IntegrationComponentScan mechanism. This annotation must be placed with a @Configuration annotation and be customized to define its scanning options, such as basePackages and basePackageClasses. In this case, all discovered interfaces annotated with @MessagingGateway are parsed and registered as GatewayProxyFactoryBean instances. All other class-based components are parsed by the standard @ComponentScan.
#
## Messaging Meta-Annotations
Starting with version 4.0, all messaging annotations can be configured as meta-annotations and all user-defined messaging annotations can define the same attributes to override their default values. In addition, meta-annotations can be configured hierarchically, as the following example shows:

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ServiceActivator(inputChannel = "annInput", outputChannel = "annOutput")
public @interface MyServiceActivator {

    String[] adviceChain = { "annAdvice" };
}

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MyServiceActivator
public @interface MyServiceActivator1 {

    String inputChannel();

    String outputChannel();
}
...

@MyServiceActivator1(inputChannel = "inputChannel", outputChannel = "outputChannel")
public Object service(Object payload) {
   ...
}

Configuring meta-annotations hierarchically lets users set defaults for various attributes and enables isolation of framework Java dependencies to user annotations, avoiding their use in user classes. If the framework finds a method with a user annotation that has a framework meta-annotation, it is treated as if the method were annotated directly with the framework annotation.
#
## Annotations on @Bean Methods
Starting with version 4.0, you can configure messaging annotations on @Bean method definitions in @Configuration classes, to produce message endpoints based on the beans, not the methods. It is useful when @Bean definitions are “out-of-the-box” MessageHandler instances (AggregatingMessageHandler, DefaultMessageSplitter, and others), Transformer instances (JsonToObjectTransformer, ClaimCheckOutTransformer, and others), and MessageSource instances (FileReadingMessageSource, RedisStoreMessageSource, and others). The following example shows how to use messaging annotations with @Bean annotations:

@Configuration
@EnableIntegration
public class MyFlowConfiguration {

    @Bean
    @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<String> consoleSource() {
        return CharacterStreamReadingMessageSource.stdin();
    }

    @Bean
    @Transformer(inputChannel = "inputChannel", outputChannel = "httpChannel")
    public ObjectToMapTransformer toMapTransformer() {
        return new ObjectToMapTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = "httpChannel")
    public HttpRequestExecutingMessageHandler httpHandler() {
    HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("https://foo/service");
        handler.setExpectedResponseType(String.class);
        handler.setOutputChannelName("outputChannel");
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "outputChannel")
    public LoggingHandler loggingHandler() {
        return new LoggingHandler("info");
    }

}

Version 5.0 introduced support for a @Bean annotated with @InboundChannelAdapter that returns java.util.function.Supplier, which can produce either a POJO or a Message. The following example shows how to use that combination:

@Configuration
@EnableIntegration
public class MyFlowConfiguration {

    @Bean
    @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
    public Supplier<String> pojoSupplier() {
        return () -> "foo";
    }

    @Bean
    @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
    public Supplier<Message<String>> messageSupplier() {
        return () -> new GenericMessage<>("foo");
    }
}

The meta-annotation rules work on @Bean methods as well (the @MyServiceActivator annotation described earlier can be applied to a @Bean definition).
#
# When you use these annotations on consumer @Bean definitions, if the bean definition returns an appropriate MessageHandler (depending on the annotation type), you must set attributes (such as outputChannel, requiresReply, order, and others), on the MessageHandler @Bean definition itself. Only the following annotation attributes are used: adviceChain, autoStartup, inputChannel, phase, and poller. All other attributes are for the handler.
#
# The bean names are generated with the following algorithm:
#
# The MessageHandler (MessageSource) @Bean gets its own standard name from the method name or name attribute on the @Bean. This works as though there were no messaging annotation on the @Bean method.

The AbstractEndpoint bean name is generated with the following pattern: [@Bean name].[decapitalizedAnnotationClassShortName]. For example, the SourcePollingChannelAdapter endpoint for the consoleSource() definition shown earlier gets a bean name of consoleSource.inboundChannelAdapter. Unlike with POJO methods, the bean method name is not included in the endpoint bean name. See also Endpoint Bean Names.

If @Bean cannot be used directly in the target endpoint (not an instance of a MessageSource, AbstractReplyProducingMessageHandler or AbstractMessageRouter), a respective AbstractStandardMessageHandlerFactoryBean is registered to delegate to this @Bean. The bean name for this wrapper is generated with the following pattern: [@Bean name].[decapitalizedAnnotationClassShortName].[handler (or source)]
#
# When using these annotations on @Bean definitions, the inputChannel must reference a declared bean. Channels are automatically declared iif not present in the application context yet.
#
# With Java configuration, you can use any @Conditional (for example, @Profile) definition on the @Bean method level to skip the bean registration for some conditional reason. The following example shows how to do so:

@Bean
@ServiceActivator(inputChannel = "skippedChannel")
@Profile("thing")
public MessageHandler skipped() {
    return System.out::println;
}

Together with the existing Spring container logic, the messaging endpoint bean (based on the @ServiceActivator annotation), is also not registered.
#
## Creating a Bridge with Annotations
Starting with version 4.0, Java configuration provides the @BridgeFrom and @BridgeTo @Bean method annotations to mark MessageChannel beans in @Configuration classes. These really exists for completeness, providing a convenient mechanism to declare a BridgeHandler and its message endpoint configuration:

@Bean
public PollableChannel bridgeFromInput() {
    return new QueueChannel();
}

@Bean
@BridgeFrom(value = "bridgeFromInput", poller = @Poller(fixedDelay = "1000"))
public MessageChannel bridgeFromOutput() {
    return new DirectChannel();
}
@Bean
public QueueChannel bridgeToOutput() {
    return new QueueChannel();
}

@Bean
@BridgeTo("bridgeToOutput")
public MessageChannel bridgeToInput() {
    return new DirectChannel();
}

You can use these annotations as meta-annotations as well.
#
#
## Note 
#
# IntegrationConsumer
#   
#   	MessageChannel getInputChannel();
#   
#   	MessageChannel getOutputChannel();
#   
#   `	MessageHandler getHandler();
#   
# public class ReactiveStreamsConsumer extends AbstractEndpoint implements IntegrationConsumer {
# }
#
# @FunctionalInterface
# public interface ReactiveMessageHandler {
#   
#   	/**
#   	 * Handle the given message.
#   	 * @param message the message to be handled
#   	 * @return a completion {@link Mono} for the result of the message handling
#   	 */
#   	Mono<Void> handleMessage(Message<?> message);
#   
#
# public class FluxMessageChannel extends AbstractMessageChannel
#		implements Publisher<Message<?>>, ReactiveStreamsSubscribableChannel {
# }
#
#
#   public interface ReactiveStreamsSubscribableChannel extends IntegrationPattern {
#   
#   	void subscribeTo(Publisher<? extends Message<?>> publisher);
#   
#   	@Override
#   	default IntegrationPatternType getIntegrationPatternType() {
#   		return IntegrationPatternType.reactive_channel;
#   	}
#   
#   }
#



