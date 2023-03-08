## Consistency Models in Distributed System

1. Linearizability

   Linearizability is the strongest form of consistency model in the distributed system. 

   It is also known as Atomic Consistency. Under this model, the effects of each operation will be visible to other 
   processes at some point between the start and the end of the operation.  

   This is also known as the "linearization point".


        ,-.  
        `-'  
        /|\                                       ┌──────────────────────────────────┐ 
         |      ──────────────────────────────────┤           write(x,v1)            ├──────────────────────────────────
        / \                                       └──────────────────────────────────┘
      Process 1 
        ,-.  
        `-'                                                                                  
        /|\              ┌──────────────────┐                                              ┌──────────────────┐ 
         |      ─────────┤  read(x)=> null  ├──────────────────────────────────────────────┤  read(x)=> v1    ├─────────
        / \              └──────────────────┘                                              └──────────────────┘
      Process 2  
        ,-.  
        `-'  
        /|\                                          ┌───────────────────────┐ 
         |      ─────────────────────────────────────┤ read(x)=> null or v1  ├───────────────────────────────────────────
        / \                                          └───────────────────────┘
      Process 3  

As shown in the above diagram, before the write, all processes must read the old value. After the write, all processes 
must read the new value. When the operation is in-flight, it is unclear whether a process should see the old value or 
the new value.




        ,-.                                                                 linearizable point
        `-'                                                               ║  
        /|\                           ┌───────────────────────────────────╫─────────────────────────────┐ 
         |      ──────────────────────┤           write(x,v1)             ║                             ├───────────
        / \                           └───────────────────────────────────╫─────────────────────────────┘
      Process 1  
        ,-.  
        `-'                                                                                  
        /|\              ┌────────────────╫─┐                                              ┌───────────────╫──┐ 
         |      ─────────┤  read(x)=> null║ ├──────────────────────────────────────────────┤  read(x)=> v1 ║  ├─────────
        / \              └────────────────╫─┘                                              └───────────────╫──┘
      Process 2  
        ,-.  
        `-'  
        /|\                                   ┌────────────────╫──┐   ┌──────────────╫──┐ 
         |      ──────────────────────────────┤ read(x)=> null ║  ├───┤read(x)=> v1  ║  ├───────────────────────────────
        / \                                   └────────────────╫──┘   └──────────────╫──┘
      Process 3  

when we put in the read line as the "linearizable point", all the read before the line should read null 
while all the points after the line should read v1.

In other words, all the read lines should only move forward in time. Once one of the processes has read 
the new value, all other processes should also read the new value.

   Why is it useful?

   Sometimes we require uniqueness constraints across certain fields. 

   For example, unique user_id. If our database is distributed(which is most of the case), the moment one process 
   has accepted the request this user_id 123 has been taken, this information should be immediately visible to all 
   other processes that user_id 123 is already taken. And none shall use this id again.

   How to implement it?

   1. Single write master node with both read and write 

      If serial read and writes are all processed by the same node, then we will have linearizability because 
      there is only one single copy(or as it appears) of data.

      Not all operations require linearizability. we can only read from the masters for those critical operations. 
      The rest of the read operations can be done on the read replica. 

      Also, we can perform partitioning on the data so that not all writes are borne by the same node.  

   2. Consensus Algorithm 

      Consensus algorithms like Paxo can also solve this issue. This is essentially a consensus problem.

      Distributed Transaction is another common way to achieve this.   

   3. Quorum read with synchronous read repair and lossless conflict resolution strategy 

      For Quorum writes, we need to write 2 out of 3 nodes for example. For quorum read, we also need to read 
      2 out of 3 nodes. That will ensure at least one node we read has the latest data. 

      We can set the linearizable point to be the moment new data reaches the majority.

2. Sequential Consistency

   Sequential consistency is also known as "timeline consistency". 

   Sequential consistency is very much like linearizability, except you can read the stale value as long as the 
   overall sequence of write performed by all processes are the same.

   For example, the sequence of updating x to value v1, v2 and v3 happens on process one. The same sequence of 
   updates should also be followed by other processes. However, when process 1 is already reading v3, process 
   2 is allowed to lag behind and read v1 only.

   This consistency model establishes a total global order of all the write operations.

   Why is it useful?

   Sometimes we just want to know which operation happens after which operation in order to decide the latest 
   states of the data. 

   This is useful in conflict resolution for example.

   And we don't need it to be as strict as linearizability in order to gain performance. 

   This consistency model cannot solve our unique user_id problem because we will only come to know the order 
   later. 

   How to implement it?

   1. Lamport Timestamp

      The algorithm follows some simple rules:

      Step1. A process increments its counter before each local event (e.g., message sending event);

      Step2. When a process sends a message, it includes its counter value with the message after executing 
             step 1;

      Step3. On receiving a message, the counter of the recipient is updated, if necessary, to the greater of 
             its current counter and the timestamp in the received message. The counter is then incremented by 
             1 before the message is considered received.[2]

   2. Total Order Broadcast

      Imaging a central queue in the form of a write-ahead log(AWL). When each process wants to do an insertion, 
      it first publishes a message to the queue declaring what it wants to do. When eventually the process receives 
      back the same message. Then it performs the action, if still applicable. This will ensure all processes will 
      end up executing the same set of commands in the same order. This idea is behind "total order broadcast".

      This is actually the idea behind Apache Zookeeper as well. Zookeeper ensures a total order of the data written 
      under its care.

      This process only fits the criteria for sequential consistency but not linearizability because the speed at which 
      the processes consume the central write-ahead log can be different. Hence some other processes might be lagging 
      behind. But you have the guarantee that when they catch up, the writes are all of the same order.


3. Causal Consistency

   In terms of relationships between operations, there are two types: they happen concurrently or they happen one before 
   the other. In the view of causal consistency, the concurrency is not time based. As long as they are not dependent on 
   each other, we can call them concurrent.

   For example write(x, v1) and write(y, v2) are concurrent. It does not matter if they actually happen in parallel in real 
   time. As long as these two operations do not depend on each other, we don't actually care which happens before which. It 
   does not affect the eventual outcome. However, in another example write(x, v1), write(x, v2), the order of the event will 
   actually affect the outcome. They don't have to happen one after another immediately in real time. But one has to happen 
   only after the other. In this case, they are not concurrent operations.

    Causal consistency is a relaxation of sequential consistency. In sequential consistency, we establish a global order of 
    all the operations regardless if one operation actually builds on or depends on the result of the other operation. In 
    causal consistency, we only track the order of the operations that are causally dependent. We do not track the orders 
    of the concurrent operations.

    If causally dependent operation happens concurrently in time, this might result in branching of the operation history. 
    Some systems call this "siblings". Under causal consistency, we should not use last-write-win strategy to just overwrite 
    the values. Resolution of the branches should only be resolved by the user. Or better yet if you can come out with a 
    conflict-free replicated data type (CRDT), then auto-merging is possible.

    Why is it useful?

    This relaxation improves the speed of operations and reduces the cost of synchronization. And most of the time, that's 
    actually enough to serve our purpose. For example, if you are enrolling your credit card for transaction notification. 
    It does not matter at all if your friends enroll their cards first then you enroll yours or vice versa. But what does 
    matter is that if you enroll your card and unenroll it, this order has to be tracked.

    How to implement it?

    1. Version Vector

4. Eventual Consistency

   Eventually, everything will be consistent. yes, eventually. That's the only promise.

   Why is it useful?

   It is the default setting of many asynchronously replicated systems. It's fast and simple and available, at the 
   expense of consistency and potential data loss due to the last-write-win(LWW) conflict resolution strategy.

   How to implement it?

   1. Multi-leader replication

     This setup is usually done across two data center. Each data center has one master write node and several read 
     replica. The two master nodes will asynchronously replicate towards each other. Eventually everything will be 
     in sync. However, if you write(x,v1) on master 1 and write(x,v1) on master 2, there is no guarantee which value 
     read(x) will be eventually, depending on the replication speed or wall clock. 
 
     Wall clock is not reliable in distributed system. Distributed systems don't share the same time in their internal 
     wall clock. The common strategy to resolve the conflict is to see which write has the larger timestamp. This is 
     known as last-write win(LWW) conflict resolution strategy, which results in one of the value being overwritten 
     unnoticed. 

   2. Leaderless replication

      Systems like Cassandra and Dynamo DB are examples of leaderless replication. 

      hey have tunable consistency mechanism built inside which you can adjust your consistency level. If you opt in 
      for lower level of consistency, they actually uses asynchronous replication to spread the writes through gossip 
      protocols. 
