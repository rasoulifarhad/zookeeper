## ZooKeeper Recipes and Solutions

One of the most interesting things about ZooKeeper is that even though ZooKeeper uses asynchronous
notifications, you can use it to build synchronous consistency primitives, such as queues and locks.
As you will see, this is possible because ZooKeeper imposes an overall order on updates, and has
mechanisms to expose this ordering.

**Out of the Box Applications**: Name Service, Configuration, Group Membership

Name service and configuration are two of the primary applications of ZooKeeper. These two functions
are provided directly by the ZooKeeper API.

Another function directly provided by ZooKeeper is group membership. The group is represented by a
node. Members of the group create ephemeral nodes under the group node. Nodes of the members that
fail abnormally will be removed automatically when ZooKeeper detects the failure.

**Locks**

Fully distributed locks that are globally synchronous, meaning at any snapshot in time no two clients 
think they hold the same lock.

First define a lock node.

Note 
  There now exists a Lock implementation in ZooKeeper recipes directory.

Clients wishing to obtain a lock do the following:

  ```
  1. Call create( ) with a pathname of "locknode/guid-lock-" and the sequence and ephemeral flags 
     set. The guid is needed in case the create() result is missed. See the note below.

  2. Call getChildren( ) on the lock node without setting the watch flag (this is important to 
     avoid the herd effect).

  3. If the pathname created in step 1 has the lowest sequence number suffix, the client has the 
     lock and the client exits the protocol.

  4. The client calls exists( ) with the watch flag set on the path in the lock directory with 
     the next lowest sequence number.

  5. if exists( ) returns null, go to step 2. Otherwise, wait for a notification for the pathname 
     from the previous step before going to step 2.
  ```
The unlock protocol is very simple: clients wishing to release a lock simply delete the node they 
created in step 1.

Note:
  ```
  * The removal of a node will only cause one client to wake up since each node is 
    watched by exactly one client.  In this way, you avoid the herd effect.
  * There is no polling or timeouts.
  * Because of the way you implement locking, it is easy to see the amount of lock 
    contention, break locks, debug locking problems, etc.
  ```
**Recoverable Errors and the GUID**
  * If a recoverable error occurs calling create() the client should call getChildren() and check 
    for a node containing the guid used in the path name. This handles the case (noted above) of 
    the create() succeeding on the server but the server crashing before returning the name of 
    the new node.

**Shared Locks**

You can implement shared locks by with a few changes to the lock protocol:

Obtaining a read lock:
  ```
  1. Call create( ) to create a node with pathname "guid-/read-". This is the lock node use later
     in the protocol. Make sure to set both the sequence and ephemeral flags.	
  2. Call getChildren( ) on the lock node without setting the watch flag - this is important, as
     it avoids the herd effect.	
  3. If there are no children with a pathname starting with "write-" and having a lower sequence
     number than the node created in step 1, the client has the lock and can 
     exit the protocol.	
  4. Otherwise, call exists( ), with watch flag, set on the node in lock directory with pathname 
     starting with "write-" having the next lowest sequence number.	
  5. If exists( ) returns false, goto step 2.	
  6. Otherwise, wait for a notification for the pathname from the previous step before going to 
     step 2
  ```
Obtaining a write lock:
  ```
  1. Call create( ) to create a node with pathname "guid-/write-". This is the lock node spoken
     of later in the protocol. Make sure to set both sequence and ephemeral flags.
  2. Call getChildren( ) on the lock node without setting the watch flag - this is important, as 
     it avoids the herd effect.
  3. If there are no children with a lower sequence number than the node created in step 1, the 
     client has the lock and the client exits the protocol.
  4. Call exists( ), with watch flag set, on the node with the pathname that has the next lowest 
     sequence number.
  5. If exists( ) returns false, goto step 2. Otherwise, wait for a notification for the pathname 
     from the previous step before going to step 2.
  ```

