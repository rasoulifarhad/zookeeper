## ZooKeeper CLI
#
# docker exec -it zoo1 bash
#
# type  zkCli.sh  to go to intractive mode
#
#   # list root childeren
#   ls /
#   # see help
#   help
# 
## read and write dataa
# 
#   # create znode	
#   create /zk-demo ''
#   create /zk-demo/my-node 'Hello'
#   
#   # get data
#   get /zk-demo/my-node
#   
#   # change znode data
#   set /zk-demo/my-node 'Goodby'
#   
#   # check data
#   get /zk-demo/my-node 
#   
#   # deleye znode
#   ls /zk-demo
#   delete /zk-demo/my-node
#   ls /zk-demo
#   delete /zk-demo
#   
## sequential and ephemeral znodes
#
#   # crete sequential znode
#   create -s /zk-demo/sequential one
#   create -s /zk-demo/sequential two
#   ls /zk-demo
#   get /zk-demo/sequential0000000001
#   
#   # create ephemeral znode
#   create -e -s /zk-demo/ephemeral data
#   ls /zk-demo
#   
#   # type quit the login the ephemeral znode must be deleted
#   quit
#   
## Watches
#
#   # create znode for watch
#   create /zk-demo/watch-this data
#   
#   # set watch
#   get -w /zk-demo/watch-this
#   
#   # chande zknode data
#   set /zk-demo/watch-this data2
#   
#   # you must see this 
#
#   WATCHER::
#   WatchedEvent state:SyncConnected type:NodeDataChanged path:/zk-demo/watch-this
#   
#   # again change data
#   set /zk-demo/watch-this data3
#   
#   # you not see notification
#
## Versioning and ACLs
#
#  # see metadata of znode
#  get -s /zk-demo/watch-this
#
#  # you see :
# 
#   data2
#   cZxid = 0x17a
#   ctime = Sun Mar 05 18:35:21 UTC 2023
#   mZxid = 0x17d
#   mtime = Sun Mar 05 18:41:36 UTC 2023
#   pZxid = 0x17a
#   cversion = 0
#   dataVersion = 2
#   aclVersion = 0
#   ephemeralOwner = 0x0
#   dataLength = 5
#   numChildren = 0
#    
#
#
#
# 
   
   