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


