## Sample Distributed Database
#
# - Three Spring boot App server running on port 8081, 8082 and 8083 is used 
#   as a database that stores Person data (List<Person>).
#
# - Each spring boot server connects to a standalone zookeeper server during 
#   startup passed as VM argument (-Dzk.url=localhost:2181).
#
# - Each spring boot app server will maintain and store the cluster-info in 
#   its memory. This cluster-info will tell current active servers, the current 
#   leader of the cluster, and all nodes that are part of this cluster.
#
# - We will create 2 GET APIs, to get info about the cluster and person data and 
#   1 PUT API to save Person data.
#
# - Any Person update request coming to the App server will be sent to Leader and 
#   which will broadcast the update request to all live servers/followers.
#
# - Any server coming up after being dead will sync Person data from the leader.
#
#
## Implementation
#
# In the implementation, we mainly will focus on:
#
# 1. The zookeeper operations and algorithms that we need to implement to 
#    solve the leader election problem and to maintain active/inactive 
#    servers list.
#
# 2. The listeners/watchers implementation that is needed for an app server 
#    to get notified in the event of leader change or any server going down.
#
# 3. Tasks that our spring boot app server(database) needs to perform during 
#    startup like creating necessary nodes, registering watchers, etc.
#
# 4. API to update the person data.
#
## Zookeeper operations
#
# We need the following zookeeper operations:
#
# - To create all the required znodes(/election, /all_nodes, /live_nodes) before we start our application server.
#
# - To create ephemeral znodes inside /live_nodes.
#
# - To create ephemeral sequential znodes inside /election.
#
# - get children operations on /live_nodes, /all_nodes, and /election znodes.
#   Example get operation for the leader:
#
# public String getLeaderNodeData2() {
    if (!zkClient.exists("/election")) {
      throw new RuntimeException("No node /election2 exists");
    }
    
    // fetch all children under /election
    List<String> nodesInElection = zkClient.getChildren("/election");
    
    //get the least sequenced znode, say "node-00000001", this znode will be considered leader
    Collections.sort(nodesInElection);
    String masterZNode = nodesInElection.get(0);
    
    //get the data associated with znode "/election", which will give "host:port" of leader
    return getZNodeData("/election".concat("/").concat(masterZNode));
  }
#
# - APIs to register our watchers, to capture cluster state change and leader change.
#
## Listeners/Watchers
#
# We need four watchers in our application:
#
# 1. Watcher for any change in children of /all_nodes, to identify and server addition/deletion to/from the cluster, and update local ClusterInfo object.
#
#  AllNodesChangeListener
#
# 2. Watcher for change in children in /live_nodes, to capture if any server goes down and then update the local ClusterInfo object
#
#  LiveNodeChangeListener
#
# 3. Watchers to capture the change in leader, listening to the change in children of znode /election. Then fetch the least sequenced znode from the list of children and make it a new leader server.
#
#  MasterChangeListenerApproach2
#
# 4. Watcher for every new session establishment with zookeeper. Application session with zookeeper might end if the zookeeper doesn’t receive any ping within the configured session timeout, this could happen due to temporary network failure or GC pause or any other reason.
Once the session of a server is killed by the zookeeper, Zookeeper will delete all ephemeral znodes created by this server, leading to the deletion of znode under /live_nodes.
So, if the session is established at any later point, we need to re-sync data from the current master and create znode in /live_nodes to notify all other servers that an existing server has become active.
#
# ConnectStateChangeListener
#
## Application Startup Tasks
#
# OnStartupApplication.java runs during application startup and performs the below tasks:

# 1. Create all parent znodes /election, /live_nodes, /all_nodes, if they do not exist.
# 2. Add the server to cluster by creating znode under /all_nodes, with znode name as host:port string and update the local ClusterInfo object.
# 3. Set ephemeral sequential znode in the /election, to set up a leader for the cluster, with suffix “node-” and data as “host:port”.
# 4. Get the current leader from the zookeeper and set it to ClusterInfo object.
# 5. sync all Person data from the leader server.
# 6. Once the sync completes, announce this server as active by adding a child znode under /live_nodes with “host:port” string as the znode name and then update the ClusterInfo object.
# 7. In the final step register all listeners/watchers to get notification from the zookeeper.
#
#  OnStartupApplication.java
#
## Update request API for Person Data

In our system, all write requests are processed by the leader, and then the leader will broadcast “Person” data to all active servers.

We need an API for an update request that needs to perform the below task:
(i) If the update request is from the leader, then save it to the local database(List<Person>).
(ii) If the request is coming from the client and the server receiving request is one of the followers, then it should forward an update request to the leader.
(iii) Once the leader receives the update request, it will broadcast the update to all followers and save it in its local storage also.

## PUT /person/{id}/{name} API


#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
##
#
#
#
#
#
#
