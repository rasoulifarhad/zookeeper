## ZooKeeper CLI
 ```
 docker exec -it zoo1 bash
 ```
Type  zkCli.sh  to go to intractive mode

List root childeren
 ```
 ls /
 ```
See help
 ```
 help
 ```
#### Read and Write data
 
Create znode	
 ```
 create /zk-demo ''
 create /zk-demo/my-node 'Hello'
 ```
Get data
 ```
 get /zk-demo/my-node
 ```
Change znode data
 ```
 set /zk-demo/my-node 'Goodby'
 ```
Check data
 ```
 get /zk-demo/my-node 
 ```
Deleye znode
 ```
 ls /zk-demo
 delete /zk-demo/my-node
 ls /zk-demo
 delete /zk-demo
 ```
#### sequential and ephemeral znodes

Create sequential znode
 ```
 create -s /zk-demo/sequential one
 create -s /zk-demo/sequential two
 ls /zk-demo
 get /zk-demo/sequential0000000001
 ```
Create ephemeral znode
 ```
 create -e -s /zk-demo/ephemeral data
 ls /zk-demo
 ```
Type quit the login the ephemeral znode must be deleted
 ```
 quit
 ```
#### Watches

Create znode for watch
 ```
 create /zk-demo/watch-this data
 ```
Set watch
 ```
 get -w /zk-demo/watch-this
 ```
Change zknode data
 ```
 set /zk-demo/watch-this data2
 ```
You must see this:
 ```
 WATCHER::
 WatchedEvent state:SyncConnected type:NodeDataChanged path:/zk-demo/watch-this
 ```
Again change data
 ```
 set /zk-demo/watch-this data3
 ```
You not see notification

#### Versioning and ACLs

See metadata of znode
 ```
 get -s /zk-demo/watch-this
 ```
You see :
 ```
 data2
 cZxid = 0x17a
 ctime = Sun Mar 05 18:35:21 UTC 2023
 mZxid = 0x17d
 mtime = Sun Mar 05 18:41:36 UTC 2023
 pZxid = 0x17a
 cversion = 0
 dataVersion = 2
 aclVersion = 0
 ephemeralOwner = 0x0
 dataLength = 5
 numChildren = 0
 ```
