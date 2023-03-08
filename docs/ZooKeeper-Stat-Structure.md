## ZooKeeper Stat Structure  
   
   The Stat structure for each znode in ZooKeeper is made up of the following fields:
   
      - **czxid**
        The zxid of the change that caused this znode to be created.

      - **mzxid**
        The zxid of the change that last modified this znode.

      - **pzxid**
        The zxid of the change that last modified children of this znode.

      - **ctime**
        The time in milliseconds from epoch when this znode was created.

      - **mtime**
        The time in milliseconds from epoch when this znode was last modified.

      - **version**
        The number of changes to the data of this znode.

      - **cversion**
        The number of changes to the children of this znode.

      - **aversion**
        The number of changes to the ACL of this znode.

      - **ephemeralOwner**
        The session id of the owner of this znode if the znode is an ephemeral node. If it is not an ephemeral 
        node, it will be zero.

      - **dataLength**
        The length of the data field of this znode.

      - **numChildren**
        The number of children of this znode.
   
