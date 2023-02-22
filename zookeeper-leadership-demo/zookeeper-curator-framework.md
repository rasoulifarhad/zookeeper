
## Note (zookeeper)
#
# Guarantees
#
# ZooKeeper is very fast and very simple. Since its goal, though, is to be a basis for the construction of more complicated services, 
# such as synchronization, it provides a set of guarantees. These are:
#
#   - Sequential Consistency - Updates from a client will be applied in the order that they were sent.
#
#   - Atomicity - Updates either succeed or fail. No partial results.
#
#   - Single System Image - A client will see the same view of the service regardless of the server that it connects to.
#
#   - Reliability - Once an update has been applied, it will persist from that time forward until a client overwrites the update.
#
#   - Timeliness - The clients view of the system is guaranteed to be up-to-date within a certain time bound.
#
## The Zookeeper Data Model (ZDM)
#
#                              ┌───────────┐
#                              │     /     │
#                              └─────┬─────┘
#                                    │
#                                    │
#                                    V
#                              ┌───────────┐
#                              │   /zoo    │
#                              └─────┬─────┘
#                                    │
#                                    │ 
#               ┌────────────────────┼────────────────────┐                                
#               │                    │                    │
#               │                    │                    V 
#         ┌───────────┐        ┌───────────┐        ┌───────────┐
#         │ /zoo/duck │        │ /zoo/goat │        │ /zoo/cow  │
#         └───────────┘        └───────────┘        └───────────┘
#
#
## Types of Zookeeper Nodes
#
#     - Persistence
#     - Ephemeral
#     - Sequential
#
## ZDM- Watches
#
# Zookeeper, a watch event is a one-time trigger which is sent to the client that set watch. 
#
# It occurred when data from that watch changes.
#
# Watches are ordered, the order of watch events corresponds to the order of the updates. A client will able to see a watch 
# event for znode before seeing the new data which corresponds to that znode.
#
#
## ZDM- Access Control list
#
# Zookeeper uses ACLs to control access to its znodes. ACL is made up of a pair of (Scheme: id, permission)
#
# Build in ACL schemes:
#
#     world: has a single id, anyone
#     
#     auth: Not use any id, It represents any authenticated user
#     
#     digest: use a username: password
#     
#     host: Allows you to use client’s hostname as ACL id identity
#     
#     IP: use the client host IP address as ACL id identity
#
#  ACL Permissions:
#     
#     CREATE
#     READ
#     WRITE
#     DELETE
#     ADMIN
#     
# E.x. (IP: 192.168.0.0/16, READ)
#
#
## The ZKS – Session States and Lifetime
#
#  - Before executing any request, it is important that the client must establish a session with service
#  - All operations clients are sent to service are automatically associated with a session
#  - The client may connect to any server in the cluster. But it will connect to only a single server
#  - The session provides “order guarantees”. The requests in the session are executed in FIFO order
#  - The main states for a session are 1) Connecting, 2) Connected 3) Closed 4) Not Connected.
#
## Apache ZooKeeper Applications
#
# Apache Zookeeper used for following purposes:
#
#   - Managing the configuration
#   - Naming services
#   - Choosing the leader
#   - Queuing the messages
#   - Managing the notification system
#   - Synchronization
#   - Distributed Cluster Management
#   
## ZooKeeper at Found
#
# At Found we use ZooKeeper extensively for discovery, resource allocation, leader election and high priority notifications.
#
## CuratorFramework
#
# The Curator framework provides a set of high-level APIs that simplify the operation of ZooKeeper.
#
# It adds a lot of features developed using ZooKeeper and can handle the complex connection management and retry mechanisms of the ZooKeeper cluster.
#
# - Automated connection management: 
#
# - Cleanup API
#
#      - Simplified native ZooKeeper methods, events, etc.
#      - Provides a modern streaming interface
#
# - Recipes implementation is provided: 
#
#     - Leader election
#     - Shared lock
#     - Path cache and watcher
#     - Distributed Queue
#     - Distributed Priority Queue
#
# Curator Framework creates CuratorFramework instances in factory mode and builder mode through CuratorFrameworkFactory. 
#
# CuratorFramework instances are thread-safe, you should share the same CuratorFramework instance in your application.
#
#   1. 
#        // these are reasonable arguments for the ExponentialBackoffRetry.
#        // The first retry will wait 1 second - the second will wait up to 2 seconds - the
#        // third will wait up to 4 seconds.
#
#        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
#
#        // The simplest way to get a CuratorFramework instance. This will use default values.
#        // The only required arguments are the connection string and the retry policy
#        return CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
#
#   2. 
#        // using the CuratorFrameworkFactory.builder() gives fine grained control
#        // over creation options. See the CuratorFrameworkFactory.Builder javadoc details
#        return CuratorFrameworkFactory.builder().connectString(connectionString)
#                .retryPolicy(retryPolicy)
#                .connectionTimeoutMs(connectionTimeoutMs)
#                .sessionTimeoutMs(sessionTimeoutMs)
#                // etc. etc.
#                .build();
#
#
# The Curator framework provides a fluent interface. Operations are chained together through the builder 
# so that method calls behave like statements.
#
#   client.create().forPath("/head", new byte[0]);
#   client.delete().inBackground().forPath("/head");
#   client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/head/child", new byte[0]);
#   client.getData().watched().inBackground().forPath("/test");
#
#
# Methods provided by CuratorFramework:
#
#   create()  
#     To start creating an operation, you can call additional methods (such as mode mode or background 
#     execution) and call forPath() at the end to specify the ZNode to operate
#
#   delete() Start the delete operation. 
#
#   checkExists() Begins the operation of checking for the existence of a ZNode.
#
#   getData() Start the operation to get the ZNode node data. 
#
#   setData()
#     Start the operation of setting the ZNode node data. You can call additional methods (version or 
#     background processing) and call forPath() at the end to specify the ZNode to operate
#
#   getChildren() Start getting a list of ZNode's child nodes. 
#
#   inTransaction
#     It starts with an atomic ZooKeeper transaction. You can combine create, setData, check, and/or 
#     delete operations and then call commit() to commit as an atomic operation 
#
#
# Notifications and monitoring of background operations can be published through the ClientListener interface. 
#
# You can register a listener on the CuratorFramework instance through addListener() , which implements 
# the following methods:
#
#   eventReceived() A background operation completes or a monitor is triggered
#
# The types of events and the methods of events are as follows:
#
#  Event Type      Event Methods  
#
#  CREATE          getResultCode() and getPath()
#
#  DELETE          getResultCode() and getPath()
#
#  EXISTS          getResultCode(), getPath() and getStat()
#
#  GETDATA         getResultCode(), getPath(), getStat() and getData()
#
#  SETDATA         getResultCode(), getPath() and getStat()
#
#  CHILDREN        getResultCode(), getPath(), getStat(), getChildren()
#
#  WATCHED         getWatchedEvent()
#
# You can also monitor the status of the connection through the ConnectionStateListener interface.
#
# You can use namespaces to avoid name collisions for nodes of multiple applications. 
#
#    CuratorFramework client = CuratorFrameworkFactory.builder().namespace("MyApp") ... build();
#    ....
#    client.create().forPath("/test", data);
#    // node was actually written to: "/MyApp/test"
#
## How to operate
#
#   import java.util.List;
#   
#   import org.apache.curator.framework.CuratorFramework;
#   import org.apache.curator.framework.api.BackgroundCallback;
#   import org.apache.curator.framework.api.CuratorEvent;
#   import org.apache.curator.framework.api.CuratorListener;
#   import org.apache.zookeeper.CreateMode;
#   import org.apache.zookeeper.Watcher;
#   
#   public class CrudExample {
#   
#       public static void main(String[] args) {
#   
#       }
#   
#       public static void create(CuratorFramework client, String path, byte[] payload) throws Exception {
#           // this will create the given ZNode with the given data
#           client.create().forPath(path, payload);
#       }
#   
#       public static void createEphemeral(CuratorFramework client, String path, byte[] payload) throws Exception {
#           // this will create the given EPHEMERAL ZNode with the given data
#           client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
#       }
#   
#       public static String createEphemeralSequential(CuratorFramework client, String path, byte[] payload) throws Exception {
#           // this will create the given EPHEMERAL-SEQUENTIAL ZNode with the given
#           // data using Curator protection.
#           return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
#       }
#   
#       public static void setData(CuratorFramework client, String path, byte[] payload) throws Exception {
#           // set data for the given node
#           client.setData().forPath(path, payload);
#       }
#   
#       public static void setDataAsync(CuratorFramework client, String path, byte[] payload) throws Exception {
#           // this is one method of getting event/async notifications
#           CuratorListener listener = new CuratorListener() {
#               @Override
#               public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
#                   // examine event for details
#               }
#           };
#           client.getCuratorListenable().addListener(listener);
#           // set data for the given node asynchronously. The completion
#           // notification
#           // is done via the CuratorListener.
#           client.setData().inBackground().forPath(path, payload);
#       }
#   
#       public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback, String path, byte[] payload) throws Exception {
#           // this is another method of getting notification of an async completion
#           client.setData().inBackground(callback).forPath(path, payload);
#       }
#   
#       public static void delete(CuratorFramework client, String path) throws Exception {
#           // delete the given node
#           client.delete().forPath(path);
#       }
#   
#       public static void guaranteedDelete(CuratorFramework client, String path) throws Exception {
#           // delete the given node and guarantee that it completes
#           client.delete().guaranteed().forPath(path);
#       }
#   
#       public static List<String> watchedGetChildren(CuratorFramework client, String path) throws Exception {
#           /**
#            * Get children and set a watcher on the node. The watcher notification
#            * will come through the CuratorListener (see setDataAsync() above).
#            */
#           return client.getChildren().watched().forPath(path);
#       }
#   
#       public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
#           /**
#            * Get children and set the given watcher on the node.
#            */
#           return client.getChildren().usingWatcher(watcher).forPath(path);
#       }
#   }
#
#
# Also mentioned above, CuratorFramework provides the concept of transactions, which can put a group of 
# operations in an atomic transaction. 
#
# The following example demonstrates the operation of a transaction:


#
#   import java.util.Collection;
#   
#   import org.apache.curator.framework.CuratorFramework;
#   import org.apache.curator.framework.api.transaction.CuratorTransaction;
#   import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
#   import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
#   
#   public class TransactionExample {
#   
#       public static void main(String[] args) {
#   
#       }
#   
#       public static Collection<CuratorTransactionResult> transaction(CuratorFramework client) throws Exception {
#           // this example shows how to use ZooKeeper's new transactions
#           Collection<CuratorTransactionResult> results = client.inTransaction().create().forPath("/a/path", "some data".getBytes())
#                   .and().setData().forPath("/another/path", "other data".getBytes())
#                   .and().delete().forPath("/yet/another/path")
#                   .and().commit(); // IMPORTANT!
#                                                                                                                                   // called
#           for (CuratorTransactionResult result : results) {
#               System.out.println(result.getForPath() + " - " + result.getType());
#           }
#           return results;
#       }
#   
#       /*
#        * These next four methods show how to use Curator's transaction APIs in a
#        * more traditional - one-at-a-time - manner
#        */
#       public static CuratorTransaction startTransaction(CuratorFramework client) {
#           // start the transaction builder
#           return client.inTransaction();
#       }
#   
#       public static CuratorTransactionFinal addCreateToTransaction(CuratorTransaction transaction) throws Exception {
#           // add a create operation
#           return transaction.create().forPath("/a/path", "some data".getBytes()).and();
#       }
#   
#       public static CuratorTransactionFinal addDeleteToTransaction(CuratorTransaction transaction) throws Exception {
#           // add a delete operation
#           return transaction.delete().forPath("/another/path").and();
#       }
#   
#       public static void commitTransaction(CuratorTransactionFinal transaction) throws Exception {
#           // commit the transaction
#           transaction.commit();
#       }
#   }
#
### Leader Election
Description
In distributed computing, leader election is the process of designating a single process as the organizer of some task distributed among several computers (nodes). Before the task is begun, all network nodes are unaware which node will serve as the "leader," or coordinator, of the task. After a leader election algorithm has been run, however, each node throughout the network recognizes a particular, unique node as the task leader.
#
##  Participating Classes
LeaderSelector
LeaderSelectorListener
LeaderSelectorListenerAdapter
CancelLeadershipException
#
## Usage
Creating a LeaderSelector
public LeaderSelector(CuratorFramework client,
                      String mutexPath,
                      LeaderSelectorListener listener)
Parameters:
client - the client
mutexPath - the path for this leadership group
listener - listener
public LeaderSelector(CuratorFramework client,
                      String mutexPath,
                      ThreadFactory threadFactory,
                      Executor executor,
                      LeaderSelectorListener listener)
Parameters:
client - the client
mutexPath - the path for this leadership group
threadFactory - factory to use for making internal threads
executor - the executor to run in
listener - listener
#
## General Usage
LeaderSelectors must be started:

leaderSelector.start();
#
# Once started, the takeLeadership() of your listener will be called when you have leadership. Your takeLeadership() method should only return when leadership is being relinquished.
#
# When you are through with the LeaderSelector instance, you should call close:

leaderSelector.close();
#
## Error Handling
The LeaderSelectorListener class extends ConnectionStateListener. When the LeaderSelector is started, it adds the listener to the Curator instance. Users of the LeaderSelector must pay attention to any connection state changes. If an instance becomes the leader, it should respond to notification of being SUSPENDED or LOST. If the SUSPENDED state is reported, the instance must assume that it might no longer be the leader until it receives a RECONNECTED state. If the LOST state is reported, the instance is no longer the leader and its takeLeadership method should exit.

IMPORTANT: The recommended action for receiving SUSPENDED or LOST is to throw CancelLeadershipException. This will cause the LeaderSelector instance to attempt to interrupt and cancel the thread that is executing the takeLeadership method. Because this is so important, you should consider extending LeaderSelectorListenerAdapter. LeaderSelectorListenerAdapter has the recommended handling already written for you.
#
### Leader Latch
Description
In distributed computing, leader election is the process of designating a single process as the organizer of some task distributed among several computers (nodes). Before the task is begun, all network nodes are unaware which node will serve as the "leader," or coordinator, of the task. After a leader election algorithm has been run, however, each node throughout the network recognizes a particular, unique node as the task leader.

NOTE: Curator has two leader election recipes. Which one to use depends on your requirements.

Participating Classes
LeaderLatch
Usage
Creating a LeaderLatch
public LeaderLatch(CuratorFramework client,
                   String latchPath)
Parameters:
client - the client
latchPath - the path for this leadership group
public LeaderLatch(CuratorFramework client,
                   String latchPath,
                   String id)
Parameters:
client - the client
latchPath - the path for this leadership group
id - participant ID
General Usage
LeaderLatches must be started:

leaderLatch.start();
Once started, the LeaderLatch will negotiate with any other LeaderLatch participants that use the same latch path and randomly choose one of them to be the leader. At any time, you can determine if a given instance is the leader by calling:

public boolean hasLeadership()
Return true if leadership is currently held by this instance
Similar to the JDK's CountDownLatch, LeaderLatch has methods that block until leadership is acquired:

public void await()
          throws InterruptedException,
                 EOFException
Causes the current thread to wait until this instance acquires leadership
unless the thread is interrupted or closed.
public boolean await(long timeout,
                     TimeUnit unit)
             throws InterruptedException
Causes the current thread to wait until this instance acquires leadership unless
the thread is interrupted, the specified waiting time elapses or the instance is closed.
&nbsp;
Parameters:
timeout - the maximum time to wait
unit - the time unit of the timeout argument
Returns:
true if the count reached zero and false if the waiting time elapsed before the count
reached zero or the instances was closed
When you are through with the LeaderLatch instance, you must call close. This removes the instance from the leader election and releases leadership if the instance has it. Once leadership is released, another participating instance (if any) will be chosen as leader.

leaderLatch.close();
Error Handling
LeaderLatch instances add a ConnectionStateListener to watch for connection problems. If SUSPENDED or LOST is reported, the LeaderLatch that is the leader will report that it is no longer the leader (i.e. there will not be a leader until the connection is re-established). If a LOST connection is RECONNECTED, the LeaderLatch will delete its previous ZNode and create a new one.

Users of LeaderLatch must take account that connection issues can cause leadership to be lost. i.e. hasLeadership() returns true but some time later the connection is SUSPENDED or LOST. At that point hasLeadership() will return false. It is highly recommended that LeaderLatch users register a ConnectionStateListener.


#
