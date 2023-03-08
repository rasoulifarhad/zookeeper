## znodes types

**Ephemeral Nodes**

ZooKeeper also has the notion of ephemeral nodes. These znodes exists as long as the session that created the znode is 
active. When the session ends the znode is deleted. Because of this behavior ephemeral znodes are not allowed to have 
children.

**Sequence Nodes** -- Unique Naming

When creating a znode you can also request that ZooKeeper append a monotonically increasing counter to the end of path. 
This counter is unique to the parent znode. The counter has a format of %010d -- that is 10 digits with 0 (zero) padding 
(the counter is formatted in this way to simplify sorting), i.e. "<path>0000000001". See Queue Recipe for an example use 
of this feature. Note: the counter used to store the next sequence number is a signed int (4bytes) maintained by the parent 
node, the counter will overflow when incremented beyond 2147483647 (resulting in a name "<path>-2147483648").

**Container Nodes**
Added in 3.5.3

ZooKeeper has the notion of container znodes. Container znodes are special purpose znodes useful for recipes such as leader, 
lock, etc. When the last child of a container is deleted, the container becomes a candidate to be deleted by the server at 
some point in the future.

Given this property, you should be prepared to get KeeperException.NoNodeException when creating children inside of container 
znodes. i.e. when creating child znodes inside of container znodes always check for KeeperException.NoNodeException and recreate 
the container znode when it occurs.

**TTL Nodes**
Added in 3.5.3

When creating PERSISTENT or PERSISTENT_SEQUENTIAL znodes, you can optionally set a TTL in milliseconds for the znode. If 
the znode is not modified within the TTL and has no children it will become a candidate to be deleted by the server at 
some point in the future.

Note: TTL Nodes must be enabled via System property as they are disabled by default. See the Administrator's Guide for details. 
If you attempt to create TTL Nodes without the proper System property set the server will throw 
KeeperException.UnimplementedException.



