### Endpoint Roles
#
# Endpoints can be assigned to roles. Roles let endpoints be started and stopped as a group. 
#
# This is particularly useful when using leadership election, where a set of endpoints can be started or stopped when leadership 
# is granted or revoked,respectively.
# 
# For this purpose the framework registers a SmartLifecycleRoleController bean in the application context with the name 
# IntegrationContextUtils.INTEGRATION_LIFECYCLE_ROLE_CONTROLLER. Whenever it is necessary to control lifecycles, this bean can be 
# injected or @Autowired:
# 
#  <bean class="com.some.project.SomeLifecycleControl">
#      <property name="roleController" ref="integrationLifecycleRoleController"/>
#  </bean>
#
# You can assign endpoints to roles using XML, Java configuration, or programmatically. The following example shows how to configure endpoint roles with XML:
#
#  <int:inbound-channel-adapter id="ica" channel="someChannel" expression="'foo'" role="cluster"
#          auto-startup="false">
#      <int:poller fixed-rate="60000" />
#  </int:inbound-channel-adapter>
#
# The following example shows how to configure endpoint roles for a bean created in Java:
#
#   @Bean
#   @ServiceActivator(inputChannel = "sendAsyncChannel", autoStartup="false")
#   @Role("cluster")
#   public MessageHandler sendAsyncHandler() {
#       return // some MessageHandler
#   }
#
# The following example shows how to configure endpoint roles on a method in Java:
#
#   @Payload("#args[0].toLowerCase()")
#   @Role("cluster")
#   public String handle(String payload) {
#       return payload.toUpperCase();
#   }
#
# The following example shows how to configure endpoint roles by using the SmartLifecycleRoleController in Java:
#
#   @Autowired
#   private SmartLifecycleRoleController roleController;
#   ...
#       this.roleController.addSmartLifeCycleToRole("cluster", someEndpoint);
#   ...
#
# The following example shows how to configure endpoint roles by using an IntegrationFlow in Java:
#
#   IntegrationFlow flow -> flow
#           .handle(..., e -> e.role("cluster"));
#
# Each of these adds the endpoint to the cluster role.
#
# Invoking roleController.startLifecyclesInRole("cluster") and the corresponding stop…​ method starts and stops the endpoints.
# (Any object that implements SmartLifecycle can be programmatically added — not just endpoints.)
#
# The SmartLifecycleRoleController implements ApplicationListener<AbstractLeaderEvent> and it automatically starts and stops its configured 
# SmartLifecycle objects when leadership is granted or revoked (when some bean publishes OnGrantedEvent or OnRevokedEvent, respectively).
#
# When using leadership election to start and stop components, it is important to set the auto-startup XML attribute (autoStartup bean property) 
# to false so that the application context does not start the components during context initialization.
#
# Starting with version 4.3.8, the SmartLifecycleRoleController provides several status methods:
#
#  1. public Collection<String> getRoles() 
#
#  2. public boolean allEndpointsRunning(String role) 
#
#  3. public boolean noEndpointsRunning(String role) 
#
#  4. public Map<String, Boolean> getEndpointsRunningStatus(String role) 
#
#  1. Returns a list of the roles being managed.
#  2. Returns true if all endpoints in the role are running.
#  3. Returns true if none of the endpoints in the role are running.
#  4. Returns a map of component name : running status. The component name is usually the bean name.
#
## Leadership Event Handling
#
# Groups of endpoints can be started and stopped based on leadership being granted or revoked, respectively. 
#
# This is useful in clustered scenarios where shared resources must be consumed by only a single instance.
#
# An example of this is a file inbound channel adapter that is polling a shared directory. 
# (https://docs.spring.io/spring-integration/reference/html/file.html#file-reading)
#
# To participate in a leader election and be notified when elected leader, when leadership is revoked, or on failure to acquire the 
# resources to become leader, an application creates a component in the application context called a “leader initiator”. 
#
# Normally, a leader initiator is a SmartLifecycle, so it starts (optionally) when the context starts and then publishes notifications 
# when leadership changes. 
#
# You can also receive failure notifications by setting the publishFailedEvents to true (starting with version 5.0), for cases when you 
# want to take a specific action if a failure occurs. 
#
# By convention, you should provide a Candidate that receives the callbacks. 
#
# You can also revoke the leadership through a Context object provided by the framework. Your code can also listen for o.s.i.leader.event.AbstractLeaderEvent 
# instances (the super class of OnGrantedEvent and OnRevokedEvent) and respond accordingly (for instance, by using a SmartLifecycleRoleController). 
#
# The events contain a reference to the Context object. 
#
# The following listing shows the definition of the Context interface:
#
#   public interface Context {
#   
#   	boolean isLeader();
#   
#   	void yield();
#   
#   	String getRole();
#   
#   }
#   
# The context provides a reference to the candidate’s role.
#
# Spring Integration provides a basic implementation of a leader initiator that is based on the LockRegistry abstraction. To use it, you need to create 
# an instance as a bean, as the following example shows:
#
#   @Bean
#   public LockRegistryLeaderInitiator leaderInitiator(LockRegistry locks) {
#       return new LockRegistryLeaderInitiator(locks);
#   }
# 
# If the lock registry is implemented correctly, there is only ever at most one leader. 
#
# If the lock registry also provides locks that throw exceptions (ideally, InterruptedException) when they expire or are broken, the duration of the 
# leaderless periods can be as short as is allowed by the inherent latency in the lock implementation. 
#
# By default, the busyWaitMillis property adds some additional latency to prevent CPU starvation in the (more usual) case that the locks are imperfect, 
# and you only know they expired when you try to obtain one again.
#

  

