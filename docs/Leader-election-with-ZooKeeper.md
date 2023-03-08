## Leader election with ZooKeeper
#
# Each process
#
#  1. Create an ephemeral znode with path /election
#
#  2. if create call succeeds, then lead
#
#  3. Otherwise, watch /election
#
#
## Sessions
#
#  - Abstraction of connection to the ensemble
#
#  - Sessions start on a single server in an ensemble
#
#  - Sessions can move to different servers over time
#
#  - The ensemble leader expires sessions using a timeout scheme
#
# - An ephemeral znode is associated to a session 
#
#    - if session expires,then ephemerals automatically deleted
#
#
#                      client
#
#
#        server     server      server	
#
#
#             ZooKeeper Ensemble
#
#
## in kafka 
#
#  ZooKeeper
#
#    - Stores the metadata of replica groups
#
#    - Leadership and in-sync replicas 
#
## Partition replication and ZooKeeper
#
#                                         Leader=A
#    ISR                                  Epoch=0   
#                                         ISR={A,B,C,D,E}  
#     A      B      C    D     E         zk    
#
#
#                                         Leader=A
#    ISR                                  Epoch=1   
#                                         ISR={A,B,C,D}  
#     A      B      C    D    (E)        zk    
#
## ZooKeeper Guarantees?
#
#   1. Clients will never detect old data.
#   
#   2. Clients will get notified of a change to data 
#      they are watching within a bounded period of 
#      time.
#   
#   3. All requests from a client will be processed in
#      order. 
#   
#   4. All results received by a client will be
#      consistent with results received by all other
#      clients.  
#
## Data Model
#
#  - Hierarchical namespace
#
#  - Each znode has data and children
#
#  - data is read and written in its entirety
#
## ZooKeeper API
#
#  - String create(path, data, acl, flags)
#
#  - void delete(path, expectedVersion)
#
#  - Stat setData(path, data, expectedVersion)
#
#  - (data, Stat) getData(path, watch)
#
#  - Stat exists(path, watch)
#
#  - String[] getChildren(path, watch)
#
#  - void sync(path)
#
#  - List<OpResult> multi(ops)
#
## ZooKeeper Service
#
#  - All servers store a copy of the data (in memory)
#
#  - A leader is elected at startup
#
#  - Followers service clients, all updates go through leader
#
#  - Update responses are sent when a majority of servers have persisted the change
#
## Configuration Management
#                                                                         /config
# Administrator
#                                                                                 param1
#  1. setData(“/config/param1”, "value", -1)
#                                                                                 param2
# Consumer
#                                                                                 param3
#  1. getData("/config/param1", true)
#
## Leader Election
#
#  1. getdata(“/servers/leader”, true)                                     /servers
#
#  2. if successful follow the leader                                             s1  
#     described in the data and exit
#                                                                                 s2
#  3. create(“/servers/leader”,hostname, EPHEMERAL) 
#                                                                                 leader--> contains: "s1"                                
#  4. f successful lead and exit
#
#  5. goto step 1
#
## Cluster Management
#
#  Monitoring process:
#
#   1. Watch on /nodes
#
#   1. On watch trigger do getChildren(/nodes, true)
#
#   1. Track which nodes have gone away                                            /nodes -------------------------------------
#                                                                                               |             |         |  
#  Each Node:                                                                                 node-1       node-2     node-3
#
#   1. Create /nodes/node-${i} as ephemeral nodes
#
#   2. Keep updating /nodes/node-${i} periodically for node status changes 
#      (status updates could be load/iostat/cpu/others)
#
## Work Queues
#
#  Assigner process:                                                             |
#                                                                                | 
#  1. Watch /tasks for published tasks                                           |--- /tasks --------------------------------------
#                                                                                |               |           |          | 
#  2. Pick tasks on watch trigger from /tasks                                    |               |           |          |
#                                                                                |               |           |          | 
#  3. assign it to a machine specific queue by creating                          |             task-1      task-2     task-3
#     create(/machines/m-${i}/task-${j})                                         |
#                                                                                |
#  4. Watch for deletion of tasks from /tasks and /m-${i}                        |
#                                                                                \--- /machines --------------------------------------
#  Machine process:                                                                      |
#                                                                                        |   
#  1. Machines watch for /(/machines/m-${i}) for any creation of tasks                   |
#                                                                                        |------ m-1  
#  2. After executing task-${i} delete task-${i} from /tasks and /m-${i}                          |
#                                                                                                 \--- task-1
#
#
## CoordinaOon is important� 
#
#    CoordinaOon primiOves
#
#    - Semaphores�r
#    - Queues��
#    - Leader election�v
#    - Group membership�e
#    - Barriers�b
#    - Configuration
#
## Work assignment
#   
#  - Master assigns works 
#  - Workers execute tasks assigned by master
#
#  Master crashes�t
# 
#  - Single point of  failure
#
#  - No work is assigned�l
#
#  - Need to select a new master
#
#  Worker crashes
#
#  - Not as bad… Overall system still works
#  - Some tasks will never be executed� 
#  - Need to detect crashed workers
#
#  Worker does not receive assignment
#
#  - Same problem as before
#  - Some tasks may not be executed
#  - Need to guarantee that worker receives assignment
#
## Fallacies of distributed computing
#
# - The network is reliable.
# - Latency is zero.
# - Bandwidth is inﬁnite.
# - The network is secure.
# - Topology doesn't change.
# - There is one administrator.
# - Transport cost is zero.
# - The network is homogeneous.
#
## CAP principle
#
#  - Can’t obtain availability, consistency, and partition tolerance simultaneously� 
#
#
┌ ┐ ─ | └ ┘  ─ ┬ ┴ ├ ┤  │
## ZooKeeper Overview
#
#
#                                                                           Ensemble 
#          ┌────────────┐ ┌────────────┐                              ┌────────────────┐   
#          │            │ │ ZooKeeper  │                              │  ┌──────────┐  │
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │  │
#          │            │ │            │         Session              │  └──────────┘  │
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │   Leader
#                                                                     │  │  Leader  │  │   atomaticaly
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │   broadcast
#          │            │ │ ZooKeeper  │                              │  ┌──────────┐  │   updates
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │  │
#          │            │ │            │         Session              │  └──────────┘  │
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │ 
#                                                                     │  │ Follower │  │
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │
#          │            │ │ ZooKeeper  │                              │  ┌──────────┐  │
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │  │
#          │            │ │            │         Session              │  └──────────┘  │
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │    Replicated
#                                                                     │  │ Follower │  │    System
#                                                                     │  └──────────┘  │ 
#                                                                     └────────────────┘ 
#
#
## Read Operation
#
#                                                                           Ensemble 
#          ┌────────────┐ ┌────────────┐                              ┌────────────────┐   
#          │            │ │ ZooKeeper  │            Read "x"          │  ┌─x=10─────┐  │ Read  
#          │ Client App │ │ Client Lib │<─────────────────────────────┼─>│ Follower │  │ operations   
#          │            │ │            │                              │  └──────────┘  │ processed
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │ locally   
#                                                                     │  │  Leader  │  │   
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │   
#          │            │ │ ZooKeeper  │                              │  ┌──────────┐  │   
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │  │
#          │            │ │            │         Session              │  └──────────┘  │
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │ 
#                                                                     │  │ Follower │  │
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │
#          │            │ │ ZooKeeper  │                              │  ┌──────────┐  │
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │  │
#          │            │ │            │         Session              │  └──────────┘  │
#          └────────────┘ └────────────┘                              │  ┌──────────┐  │
#                                                                     │  │ Follower │  │
#                                                                     │  └──────────┘  │ 
#                                                                     └────────────────┘ 
#
## Write Operation
#
#                                                                           Ensemble 
#          ┌────────────┐ ┌────────────┐                              ┌────────────────┐   
#          │            │ │ ZooKeeper  │         write "x",11         │  ┌─x=11─────┐  │ 
#          │ Client App │ │ Client Lib │<─────────────────────────────┼─>│ Follower │<─┼────┐ 
#          │            │ │            │                              │  └──────────┘  │    │
#          └────────────┘ └────────────┘                              │  ┌─x=11─────┐  │    │
#                                                                     │  │  Leader  │──┼────┤   
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │    │
#          │            │ │ ZooKeeper  │                              │  ┌─x=11─────┐  │    │ 
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │<─┼────┤
#          │            │ │            │         Session              │  └──────────┘  │    │
#          └────────────┘ └────────────┘                              │  ┌─x=11─────┐  │    │
#                                                                     │  │ Follower │<─┼────┤
#          ┌────────────┐ ┌────────────┐                              │  └──────────┘  │    │
#          │            │ │ ZooKeeper  │                              │  ┌─x=11─────┐  │    │
#          │ Client App │ │ Client Lib ├──────────────────────────────┼─>│ Follower │<─┼────┤
#          │            │ │            │         Session              │  └──────────┘  │    │
#          └────────────┘ └────────────┘                              │  ┌─x=11─────┐  │    │
#                                                                     │  │ Follower │<─┼────┘
#                                                                     │  └──────────┘  │ 
#                                                                     └────────────────┘ 
#                                                                        Replicates across a quorum
#
#
## ZooKeeper: Seman7cs of Sessions
#
# - A preﬁx of opera7ons submiUed through a
#   session are executed
#
# - Upon disconnec7on
#
#   - Client lib tries to contact another server
#   - Before session expires: connect to new server
#   - Server must have seen a transac7on id at least as
#     large as the session
#
## ZooKeeper: API
#
#  - Create znodes: create
#
#    - Persistent, sequen7al, ephemeral 
#
#  - Read and modify data: setData, getData
#
#  - Read the children of znode: getChildren
#
#  - Check if znode exists: exists
#
#  - Delete a znode: delete
#
## Order
#
#  - Updates: Totally ordered, linearizable
#
#  - FIFO order for client opera7ons
#
#  - Read: sequen7ally ordered
#
#    	          │ write(x,10) │
#   Client 1:     ├─────────────┤  
#                 │             │
#                                   
#    	                         │ write(x,11) │
#   Client 2:                    ├─────────────┤  
#                                │             │
#
#    	          │ write(x,10) ││ write(x,11) │ 
#   Sequential:   ├─────────────┤├─────────────┤  
#                 │             ││             │
#
## ZooKeeper: Znode changes
#
#  - Znode changes
#
#    - Data is set
#    - Node is created or deleted
#    - Etc ....
#
#  - To learn of znode changes
#
#    - Set a watch
#    - Upon change, client receives a notification
#    - Notification ordered before new updates    
#
## Watches, Locks, and the herd eﬀect
#
#  Herd eﬀect
#
#  - Large number of clients wake up simultaneously
#
#  Load spikes
#
#  - Undesirable 
#
## A solution
#
#  - Use order of clients
#
#  - Each client 
#
#    - Determines the znode z preceding its own znode in the
#      sequential order
#
#    - Watch z
#
#  - A single notiﬁcation is generated upon a crash
#
# Disadvantage for leader election
#
#  - One client is notiﬁed of a leader change
#
## Linearizability
#
#  - Correctness condition
#
#  - Informal deﬁnition
#
#    - Order of operations is equivalent to a sequential
#      execution
#
#    - Equivalent order satisﬁes real time precedence order
#
#
#
#
#    	          │ write(x,10) │     │   read(x)   │      
#   Client 1(c1): ├─────────────┤     ├─────────────┤   
#                 │             │     │             │  
#                                   
#    	                         │   read(x)   │
#   Client 2(c2):                ├─────────────┤  
#                                │             │
#
#    	          │ (c2)read(x) ││ write(x,11) ││ (c1)read(x) │
#   Sequential:   ├─────────────┤├─────────────┤├─────────────┤  
#                 │             ││             ││             │
#
#                         Not Linearizable!!!
#
#
#
#
#    	          │ write(x,10) │     │   read(x)   │      
#   Client 1(c1): ├─────────────┤     ├─────────────┤   
#                 │             │     │             │  
#                                   
#    	                         │   read(x)   │
#   Client 2(c2):                ├─────────────┤  
#                                │             │
#
#    	          │ write(x,11) ││ (c2)read(x) ││ (c1)read(x) │
#   Sequential:   ├─────────────┤├─────────────┤├─────────────┤  
#                 │             ││             ││             │
#
#                         Linearizableeeeee
#
#
Implemen7ng consensus

  - Each process p proposes then decides
  
  - Propose(v)
  
      -  setData “/c/proposal-”, “v”, sequential
  
  - Decide()
  
    -  getChildren “/c”
    
    -  Select znode z with smallest sequence number
    
    - v’ = getData “/c/z”
    
    - Decide upon v’
    
    
## sync

  - Asynchronous operation    
  
  - Before read operations
  
  - Flushes the channel between follower and leader
  
  - Makes operations linearizable
  
  
## Master/Worker System
#
# - Clients
#
#   > Queue tasks to be executed 
#   > Monitor the tasks
#
# - Masters
#
#   > Assign tasks to workers
#
# - Workers
#
#   > Get tasks from the master
#   > Execute tasks                                                                             tasks
#
## Task Queue
#
#  client1    create("/tasks/client1-", cmds, SEQUENTIAL)   // cmds is an array of string       
#    
#  tasks
#    │
#    ├──client1-1
#    │
#    ├──client3-4
#    │
#    └──client1-6
#
## Group Membership
#
#  assign                                worker1 : create("/assign/worker-", "", EPHEMERAL SEQUENTIAL)
#    │
#    ├── worker1
#    │
#    ├── worker2                         Master: listChildren(“/assign”,true)
#    │
#    └── worker3
#
## Leader Election
#
#    /master                              Master: create("/master", hostinfo, EPHEMERAL)
#             
#                                         Backup: getData("/master", true)                            
#
## Conﬁguration
#
#  assign                                Master : setdata(“/assign/worker2”, znode_of_task)
#    │
#    ├── worker1
#    │
#    ├── worker2                         worker2: getdata(“/assign/worker2”, true)
#    │
#    └── worker3
#
## Worker Processing
#
#  - Create a session 
#  - Create the “worker” ephemeral znode 
#  - Watch for the assign znode 
#  - Deal with the watches
#
#    > Processing the assignment 
#
#      - Update status in the task
#
#      - Delete assignment znode when ﬁnished 
# 
#    > What do to with SessionExpired 
#
## Client Processing
#
# - Create a session
#
# - Create a task as a child of the /tasks znode
#
# - Watch the status child of the /tasks znode
#
## Master Processing
#
#  - Create a session
#
#  - Do leader election using master znode
#
#  - Watch the worker list
#
#  - Watch the task queue
#
#  - Watch the assignment queue
#	
#  - Deal with the watches
#
#     > Deal with workers coming and going
#     > Assign new tasks
#     > Watch for completions
#
## Guidelines to ConnectionLoss
#
#  - A process will not see state changes while 
#    disconnected
#
#  - Masters should act very conservatively, they
#    should not assume that they still have 
#    mastership
#
#  - Don't treat as if it's the end of the world. The
#    client library will try to recover the session
 
  
