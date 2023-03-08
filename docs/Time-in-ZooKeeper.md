## Time in ZooKeeper 

ZooKeeper tracks time multiple ways:

- **Zxid**

  Every change to the ZooKeeper state receives a stamp in the form of a zxid (ZooKeeper Transaction Id). This exposes the 
  total ordering of all changes to ZooKeeper. Each change will have a unique zxid and if zxid1 is smaller than zxid2 then 
  zxid1 happened before zxid2.

- **Version numbers**

  Every change to a node will cause an increase to one of the version numbers of that node. The three version numbers are 
  version (number of changes to the data of a znode), cversion (number of changes to the children of a znode), and aversion 
  (number of changes to the ACL of a znode).

- **Ticks**

  When using multi-server ZooKeeper, servers use ticks to define timing of events such as status uploads, session timeouts, 
  connection timeouts between peers, etc. The tick time is only indirectly exposed through the minimum session timeout (2 
  times the tick time); if a client requests a session timeout less than the minimum session timeout, the server will tell 
  the client that the session timeout is actually the minimum session timeout.

- **Real time**

  ZooKeeper doesn't use real time, or clock time, at all except to put timestamps into the stat structure on znode creation 
  and znode modification.


