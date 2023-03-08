## Consistency Guarantees

ZooKeeper is a high performance, scalable service. Both reads and write operations are designed to be fast, though 
reads are faster than writes. The reason for this is that in the case of reads, ZooKeeper can serve older data, 
which in turn is due to ZooKeeper's consistency guarantees:

- **Sequential Consistency**

  Updates from a client will be applied in the order that they were sent.

- **Atomicity**

  Updates either succeed or fail -- there are no partial results.

- **Single System Image**

  A client will see the same view of the service regardless of the server that it connects to.

- **Reliability**

  Once an update has been applied, it will persist from that time forward until a client overwrites 
  the update. This guarantee has two corollaries:

  1. If a client gets a successful return code, the update will have been applied. On some failures 
     (communication errors, timeouts, etc) the client will not know if the update has applied or not. 
     We take steps to minimize the failures, but the guarantee is only present with successful return 
     codes. (This is called the monotonicity condition in Paxos.)

  2. Any updates that are seen by the client, through a read request or successful update, will never 
     be rolled back when recovering from server failures.

- **Timeliness**

  The clients view of the system is guaranteed to be up-to-date within a certain time bound (on the order of tens 
  of seconds). Either system changes will be seen by a client within this bound, or the client will detect a 
  service outage.

Using these consistency guarantees it is easy to build higher level functions such as leader election, barriers, 
queues, and read/write revocable locks solely at the ZooKeeper client (no additions needed to ZooKeeper). See 
Recipes and Solutions for more details. 

#### Sometimes developers mistakenly assume one other guarantee that ZooKeeper does not in fact make. This is:
Simultaneously Consistent Cross-Client Views
 
- ZooKeeper does not guarantee that at every instance in time, two different clients will have identical views of 
  ZooKeeper data. Due to factors like network delays, one client may perform an update before another client gets 
  notified of the change. Consider the scenario of two clients, A and B. If client A sets the value of a znode /a 
  from 0 to 1, then tells client B to read /a, client B may read the old value of 0, depending on which server it 
  is connected to. If it is important that Client A and Client B read the same value, Client B should should call 
  the sync() method from the ZooKeeper API method before it performs its read.
 
- So, ZooKeeper by itself doesn't guarantee that changes occur synchronously across all servers, but ZooKeeper 
  primitives can be used to construct higher level functions that provide useful client synchronization. (For 
  more information, see the ZooKeeper Recipes. [tbd:..]). 










