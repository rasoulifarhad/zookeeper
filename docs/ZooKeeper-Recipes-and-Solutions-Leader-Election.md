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

**Leader Election**

A simple way of doing leader election with ZooKeeper is to use the SEQUENCE|EPHEMERAL flags when 
creating znodes that represent "proposals" of clients. The idea is to have a znode, say "/election",
such that each znode creates a child znode "/election/guid-n_" with both flags SEQUENCE|EPHEMERAL.
With the sequence flag, ZooKeeper automatically appends a sequence number that is greater than anyone
previously appended to a child of "/election". The process that created the znode with the smallest
appended sequence number is the leader.

That's not all, though. It is important to watch for failures of the leader, so that a new client arises as
the new leader in the case the current leader fails. A trivial solution is to have all application processes
watching upon the current smallest znode, and checking if they are the new leader when the smallest znode goes
away (note that the smallest znode will go away if the leader fails because the node is ephemeral). But this
causes a herd effect: upon a failure of the current leader, all other processes receive a notification, and 
execute getChildren on "/election" to obtain the current list of children of "/election". If the number of 
clients is large, it causes a spike on the number of operations that ZooKeeper servers have to process. To 
avoid the herd effect, it is sufficient to watch for the next znode down on the sequence of znodes. If a client
receives a notification that the znode it is watching is gone, then it becomes the new leader in the case that
there is no smaller znode. Note that this avoids the herd effect by not having all clients watching the same 
znode.

Here's the pseudo code:

Let ELECTION be a path of choice of the application. To volunteer to be a leader:
  ```
  1. Create znode z with path "ELECTION/guid-n_" with both SEQUENCE and EPHEMERAL 
     flags;
  
  2. Let C be the children of "ELECTION", and I am the sequence number of z;
  
  3. Watch for changes on "ELECTION/guid-n_j", where j is the largest sequence number
     such that j < i and n_j is a znode in C;
  ```
  
Upon receiving a notification of znode deletion:
  ```
  1. Let C be the new set of children of ELECTION;
  
  2. If z is the smallest node in C, then execute leader procedure;
  
  3. Otherwise, watch for changes on "ELECTION/guid-n_j", where j is the largest 
     sequence number such that j < i and n_j is a znode in C;
  ```
**Notes**:

  * Note that the znode having no preceding znode on the list of children do not imply that the creator
    of this znode is aware that it is the current leader. Applications may consider creating a separate 
    znode to acknowledge that the leader has executed the leader procedure.
  
  * See the note for Locks on how to use the guid in the node.
  
