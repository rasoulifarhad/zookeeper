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

**Two-phased Commit**

A two-phase commit protocol is an algorithm that lets all clients in a distributed system agree either
to commit a transaction or abort.

In ZooKeeper, you can implement a two-phased commit by:
```
  * bhaving a coordinator create a transaction node,  say "/app/Tx", and one child node per participating 
    site, say "/app/Tx/s_i".
  * When coordinator creates the child node, it leaves the content undefined.
  * Once each site involved in the transaction receives the transaction from the coordinator, the site reads
    each child node and sets a watch. 
  * Each site then processes the query and votes "commit" or "abort" by writing to its respective node.
  * Once the write completes, the other sites are notified, and as soon as all sites have all votes, they can
    decide either "abort" or "commit".
```
**Note** that a node can decide "abort" earlier if some site votes for "abort".

An interesting aspect of this implementation is that the only role of the coordinator is to decide upon 
the group of sites, to create the ZooKeeper nodes, and to propagate the transaction to the corresponding 
sites. 
In fact, even propagating the transaction can be done through ZooKeeper by writing it in the transaction 
node.

There are two important drawbacks of the approach described above.

  * One is the message complexity, which is O(n²).
  * The second is the impossibility of detecting failures of sites
    through ephemeral nodes.
    
To detect the failure of a site using ephemeral nodes, it is necessary that the site create the node.

To solve the first problem, you can have only the coordinator notified of changes to the transaction
nodes, and then notify the sites once coordinator reaches a decision. Note that this approach is scalable,
but it is slower too, as it requires all communication to go through the coordinator.

To address the second problem, you can have the coordinator propagate the transaction to the sites, and
have each site creating its own ephemeral node.


