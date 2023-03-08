## ZooKeeper Recipes and Solutions
#
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

**Barriers**

Distributed systems use barriers to block processing of a set of nodes until a condition is met at
which time all the nodes are allowed to proceed.

Barriers are implemented in ZooKeeper by designating a barrier node.

The barrier is in place if the barrier node exists.

Here's the pseudo code:
  ```
  1. Client calls the ZooKeeper API's exists() function on the barrier node, with watch set to true.

  2. if exists() returns false, the barrier is gone and the client proceeds

  3. Else, if exists() returns true, the clients wait for a watch event from ZooKeeper for the barrier
     node.

  4. When the watch event is triggered, the client reissues the exists( ) call, again waiting until the
     barrier node is removed.

**Double Barriers**

Double barriers enable clients to synchronize the beginning and the end of a computation. When enough
processes have joined the barrier, processes start their computation and leave the barrier once they
have finished. This recipe shows how to use a ZooKeeper node as a barrier.

    - b represents the barrier node

    - Every client process p registers with the barrier node on entry and unregisters when it is ready
      to leave.
 
    - A node registers with the barrier node via the Enter procedure , it waits until x client process
      register before proceeding with the computation. (The x here is up to you to determine for your
      system.)

    - Enter procedure:

      1. Create a name n = b + "/" + p

      2. Set watch: exists( b + "/ready", true )
  
      3. Create child: create( n, EPHEMERAL )

      4. L = getChildren( b, false )

      5. if fewer children in L than x, wait for watch event

      6. else create ( b + "/ready", REGULAR )

    - On entering, all processes watch on a ready node and create an ephemeral node as a child of the
      barrier node

    - Each process but the last enters the barrier and waits for the ready node to appear at line 5.

    - The process that creates the xth node, the last process, will see x nodes in the list of children
      and create the ready node, waking up the other processes.

      Note that waiting processes wake up only when it is time to exit, so waiting is efficient.

    - Leave procedure:
  
      1. L = getChildren( b, false )

      2. if no children, exit
  
      3. if p is only process node on L, delete( n ) and exit

      4. if p is the lowest process node in L, wait on highest process node in L

      5. else delete( n ) if  still existsand wait on lowest process node in L

      6. goto 1

    - On exit, you can't use a flag such as ready because you are watching for process nodes to go
      away.

    - By using ephemeral nodes, processes that fail after the barrier has been entered do not
      prevent correct processes from finishing.

    - When processes are ready to leave, they need to delete their process nodes and wait for all
      other processes to do the same.

    - Processes exit when there are no process nodes left as children of b. However, as an efficiency,
      you can use the lowest process node as the ready flag.

    - All other processes that are ready to exit watch for the lowest existing process node to go away,
      and the owner of the lowest process watches for any other process node (picking the highest for
      simplicity) to go away.

    - This means that only a single process wakes up on each node deletion except for the last node,
      which wakes up everyone when it is removed.
