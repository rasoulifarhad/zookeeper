## ZooKeeper Overview
#
# ZooKeeper allows distributed processes to coordinate with each other through a shared hierarchical name space of data 
# registers (we call these registers znodes), much like a file system. 
#
# The name space provided by ZooKeeper is much like that of a standard file system. 
#
# A name is a sequence of path elements separated by a slash ("/"). 
#
# Every znode in ZooKeeper's name space is identified by a path. 
#
# And every znode has a parent whose path is a prefix of the znode with one less element; the exception to this rule is 
# root ("/") which has no parent.
#
# Also, exactly like standard file systems, a znode cannot be deleted if it has any children.
#
# The main differences between ZooKeeper and standard file systems are that every znode can have data associated with it (every 
# file can also be a directory and vice-versa) and znodes are limited to the amount of data that they can have. 
#
# ZooKeeper was designed to store coordination data: status information, configuration, location information, etc. 
# This kind of meta-information is usually measured in kilobytes, if not bytes. 
# ZooKeeper has a built-in sanity check of 1M, to prevent it from being used as a large data store, but in general it is 
# used to store much smaller pieces of data.
#
## The service itself is replicated over a set of machines that comprise the service. 
# These machines maintain an in-memory image of the data tree along with a transaction logs and snapshots in a persistent store. 
#
# Because the data is kept in-memory, ZooKeeper is able to get very high throughput and low latency numbers.
# The downside to an in-memory database is that the size of the database that ZooKeeper can manage is limited by memory. 
# This limitation is further reason to keep the amount of data stored in znodes small.
#
## The servers that make up the ZooKeeper service must all know about each other.As long as a majority of the servers are 
#  available, the ZooKeeper service will be available. 
#
# Clients must also know the list of servers. The clients create a handle to the ZooKeeper service using this list of servers.
#
## Clients only connect to a single ZooKeeper server. The client maintains a TCP connection through which it sends requests, 
#  gets responses, gets watch events, and sends heartbeats. 
#
# If the TCP connection to the server breaks, the client will connect to a different server. When a client first connects to 
# the ZooKeeper service, the first ZooKeeper server will setup a session for the client. 
#
# If the client needs to connect to another server, this session will get reestablished with the new server.
#
## Read requests sent by a ZooKeeper client are processed locally at the ZooKeeper server to which the client is connected. 
#
# If the read request registers a watch on a znode, that watch is also tracked locally at the ZooKeeper server.
#
## Write requests are forwarded to other ZooKeeper servers and go through consensus before a response is generated.  
#
# Sync requests are also forwarded to another server, but do not actually go through consensus. 
#
# Thus, the throughput of read requests scales with the number of servers and the throughput of write requests decreases 
# with the number of servers.
#
## Order is very important to ZooKeeper; almost bordering on obsessive–compulsive disorder. All updates are totally ordered. 
#
# ZooKeeper actually stamps each update with a number that reflects this order. We call this number the zxid (ZooKeeper Transaction 
# Id). 
#
# Each update will have a unique zxid. Reads (and watches) are ordered with respect to updates. Read responses will be stamped with 
# the last zxid processed by the server that services the read.
#
## Note: Barriers
#
# A barrier is a primitive that enables a group of processes to synchronize the beginning and the end of a computation. 
#
# The general idea of this implementation is to have a barrier node that serves the purpose of being a parent for individual process nodes. 
# Suppose that we call the barrier node "/b1". 
#
#   - Each process "p" then creates a node "/b1/p". 
#   - Once enough processes have created their corresponding nodes, joined processes can start the computation. 
#
# In this example, each process instantiates a Barrier object, and its constructor takes as parameters:
#
#    - The address of a !ZooKeeper server (e.g., "zoo1.foo.com:2181");
#    - The path of the barrier node on !ZooKeeper (e.g., "/b1");
#    - The size of the group of processes.
#
#
#
#
#
#
## Time in ZooKeeper
#
# ZooKeeper tracks time multiple ways:
#
#   - Zxid Every change to the ZooKeeper state receives a stamp in the form of a zxid (ZooKeeper Transaction Id). 
#     This exposes the total ordering of all changes to ZooKeeper. Each change will have a unique zxid and if zxid1 
#     is smaller than zxid2 then zxid1 happened before zxid2.
#
#   - Version numbers Every change to a node will cause an increase to one of the version numbers of that node. The 
#     three version numbers are version (number of changes to the data of a znode), cversion (number of changes to 
#     the children of a znode), and aversion (number of changes to the ACL of a znode).
#
#   - Ticks When using multi-server ZooKeeper, servers use ticks to define timing of events such as status uploads, 
#     session timeouts, connection timeouts between peers, etc. The tick time is only indirectly exposed through the 
#     minimum session timeout (2 times the tick time); if a client requests a session timeout less than the minimum 
#     session timeout, the server will tell the client that the session timeout is actually the minimum 
#     session timeout.
#
#   - Real time ZooKeeper doesn't use real time, or clock time, at all except to put timestamps into the stat structure 
#     on znode creation and znode modification.
#
## ZooKeeper Stat Structure
#
# The Stat structure for each znode in ZooKeeper is made up of the following fields:
#   
#   - czxid The zxid of the change that caused this znode to be created.
#   - mzxid The zxid of the change that last modified this znode.
#   - pzxid The zxid of the change that last modified children of this znode.
#   - ctime The time in milliseconds from epoch when this znode was created.
#   - mtime The time in milliseconds from epoch when this znode was last modified.
#   - version The number of changes to the data of this znode.
#   - cversion The number of changes to the children of this znode.
#   - aversion The number of changes to the ACL of this znode.
#   - ephemeralOwner The session id of the owner of this znode if the znode is an ephemeral node. If it is not an ephemeral node, it will be zero.
#   - dataLength The length of the data field of this znode.
#   - numChildren The number of children of this znode.
#
## ZooKeeper Watches
#
# All of the read operations in ZooKeeper - getData(), getChildren(), and exists() - have the option of setting a watch as a side 
# effect. Here is ZooKeeper's definition of a watch: a watch event is one-time trigger, sent to the client that set the watch, which 
# occurs when the data for which the watch was set changes. There are three key points to consider in this definition of a watch:

#   - One-time trigger One watch event will be sent to the client when the data has changed. For example, if a client does a getData
#     ("/znode1", true) and later the data for /znode1 is changed or deleted, the client will get a watch event for /znode1. If /znode1 
#     changes again, no watch event will be sent unless the client has done another read that sets a new watch.
#
#   - Sent to the client This implies that an event is on the way to the client, but may not reach the client before the successful 
#     return code to the change operation reaches the client that initiated the change. Watches are sent asynchronously to watchers. 
#     ZooKeeper provides an ordering guarantee: a client will never see a change for which it has set a watch until it first sees the 
#     watch event. Network delays or other factors may cause different clients to see watches and return codes from updates at different 
#     times. The key point is that everything seen by the different clients will have a consistent order.

#   - The data for which the watch was set This refers to the different ways a node can change. It helps to think of ZooKeeper as maintaining 
#     two lists of watches: data watches and child watches. getData() and exists() set data watches. getChildren() sets child watches. 
#     Alternatively, it may help to think of watches being set according to the kind of data returned. getData() and exists() return information 
#     about the data of the node, whereas getChildren() returns a list of children. Thus, setData() will trigger data watches for the znode 
#     being set (assuming the set is successful). A successful create() will trigger a data watch for the znode being created and a child watch 
#     for the parent znode. A successful delete() will trigger both a data watch and a child watch (since there can be no more children) for a 
#     znode being deleted as well as a child watch for the parent znode.
#
# Watches are maintained locally at the ZooKeeper server to which the client is connected. This allows watches to be lightweight to set, maintain, 
# and dispatch. When a client connects to a new server, the watch will be triggered for any session events. Watches will not be received while 
# disconnected from a server. When a client reconnects, any previously registered watches will be reregistered and triggered if needed. In general 
# this all occurs transparently. There is one case where a watch may be missed: a watch for the existence of a znode not yet created will be missed 
# if the znode is created and deleted while disconnected.
#
# New in 3.6.0: Clients can also set permanent, recursive watches on a znode that are not removed when triggered and that trigger for changes on the 
# registered znode as well as any children znodes recursively.
#
## Semantics of Watches
#
# We can set watches with the three calls that read the state of ZooKeeper: exists, getData, and getChildren. The following list details the 
# events that a watch can trigger and the calls that enable them:
#
#   - Created event: Enabled with a call to exists.
#   - Deleted event: Enabled with a call to exists, getData, and getChildren.
#   - Changed event: Enabled with a call to exists and getData.
#   - Child event: Enabled with a call to getChildren.
#
## Persistent, Recursive Watches
#
# New in 3.6.0: There is now a variation on the standard watch described above whereby you can set a watch that does not get removed when 
# triggered. Additionally, these watches trigger the event types NodeCreated, NodeDeleted, and NodeDataChanged and, optionally, recursively 
# for all znodes starting at the znode that the watch is registered for. Note that NodeChildrenChanged events are not triggered for persistent 
# recursive watches as it would be redundant.
#
# Persistent watches are set using the method addWatch(). The triggering semantics and guarantees (other than one-time triggering) are the same 
# as standard watches. The only exception regarding events is that recursive persistent watchers never trigger child changed events as they are 
# redundant. Persistent watches are removed using removeWatches() with watcher type WatcherType.Any.
#
## Remove Watches
#
# We can remove the watches registered on a znode with a call to removeWatches. Also, a ZooKeeper client can remove watches locally even if there 
# is no server connection by setting the local flag to true. The following list details the events which will be triggered after the successful 
# watch removal.
#
#   - Child Remove event: Watcher which was added with a call to getChildren.
#   - Data Remove event: Watcher which was added with a call to exists or getData.
#   - Persistent Remove event: Watcher which was added with a call to add a persistent watch.
#
## What ZooKeeper Guarantees about Watches
#
# With regard to watches, ZooKeeper maintains these guarantees:
#
#   - Watches are ordered with respect to other events, other watches, and asynchronous replies. The ZooKeeper client libraries ensures 
#     that everything is dispatched in order.
#
#   - A client will see a watch event for a znode it is watching before seeing the new data that corresponds to that znode.
#
#   - The order of watch events from ZooKeeper corresponds to the order of the updates as seen by the ZooKeeper service.
#
## Things to Remember about Watches
#
#   - Standard watches are one time triggers; if you get a watch event and you want to get notified of future changes, you must set another 
#     watch.
#
#   - Because standard watches are one time triggers and there is latency between getting the event and sending a new request to get a watch 
#     you cannot reliably see every change that happens to a node in ZooKeeper. Be prepared to handle the case where the znode changes multiple 
#     times between getting the event and setting the watch again. (You may not care, but at least realize it may happen.)
#
#   - A watch object, or function/context pair, will only be triggered once for a given notification. For example, if the same watch object is 
#     registered for an exists and a getData call for the same file and that file is then deleted, the watch object would only be invoked once 
#     with the deletion notification for the file.
#
#   - When you disconnect from a server (for example, when the server fails), you will not get any watches until the connection is reestablished. 
#     For this reason session events are sent to all outstanding watch handlers. Use session events to go into a safe mode: you will not be receiving 
#     events while disconnected, so your process should act conservatively in that mode.
#

