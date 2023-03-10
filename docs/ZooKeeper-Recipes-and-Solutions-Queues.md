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

**Queues**

Distributed queues are a common data structure.

To implement a distributed queue in ZooKeeper,

- first designate a znode to hold the queue, the queue node.

- The distributed clients put something into the queue by calling create() with a pathname ending in
  "queue-", with the sequence and ephemeral flags in the create() call set to true.

- Because the sequence flag is set, the new pathnames will have the form _path-to-queue-node_/queue-X,
  where X is a monotonic increasing number.

- A client that wants to be removed from the queue calls ZooKeeper's getChildren( ) function, with watch
  set to true on the queue node, and begins processing nodes with the lowest number.

- The client does not need to issue another getChildren( ) until it exhausts the list obtained from the
  first getChildren( ) call.

- If there are are no children in the queue node, the reader waits for a watch notification to check the
  queue again.

**Note**:
    There now exists a Queue implementation in ZooKeeper recipes directory. This is distributed with the
    release -- src/recipes/queue directory of the release artifact.

**Priority Queues**

To implement a priority queue, you need only make two simple changes to the generic queue recipe .

- First, to add to a queue, the pathname ends with "queue-YY" where YY is the priority of the element
  with lower numbers representing higher priority (just like UNIX).

- Second, when removing from the queue, a client uses an up-to-date children list meaning that the
  client will invalidate previously obtained children lists if a watch notification triggers for the
  queue node.
