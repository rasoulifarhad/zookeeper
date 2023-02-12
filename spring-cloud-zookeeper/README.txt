## Start the container
#
# docker-compose up -d
# 
#    note : Exhibitor is accessible at http://localhost:8400/exhibitor/v1/ui/index.html.
# 
## Run app 
# 
#  ./mvnw spring-boot:run
#  
#  
## The endpoints available are:
#
#  - http://localhost:{port}/configuration-properties: This endpoints returns the configuration loaded with @ConfigurationProperties annotation.
#
#  - http://localhost:{port}/annotated-properties: This endpoints returns the configuration loaded with @Value annotations on each field.
#
#  - http://localhost:{port}/refreshed-annotated-properties: This endpoints returns the configuration loaded with @Value annotations on each field, 
#    but declaring the bean with @RefreshScope.
#
#  - http://localhost:{port}/environment-properties: This endpoints returns the configuration loaded by querying the Environment.
#
## All the endpoints return two runtime properties, but each endpoint loads the configuration in a different way. The response looks as follows:
#
#   {
#     "firstProperty": "mapped to sample.firstProperty", 
#     "secondProperty": "mapped to sample.secondProperty" 
#   }
# if you want to play around with the configuration, Exhibitor is accessible at http://localhost:8400/exhibitor/v1/ui/index.html.
#
# Assuming you have an application named zookepperConfig, and you have UAT and PROD environments, then your ZNodes containing 
# configuration (like folders) would look something like this:
#
#  1. /config/zookepperConfig,PROD/
#
#  2. /config/zookepperConfig,UAT/
#
#  3. /config/zookepperConfig/
#
#  4. /config/application,PROD/
#
#  5. /config/application,UAT/
#
#  6. /config/application/
#
# By default, all your configuration lives under the /config ZNode. You can configure this root node by setting 
# the spring.cloud.zookeeper.config.root property.
#
# /config/{application-name} holds the configuration that is specific for your application.
#
# In /config/{application-name},{profile} you’ll find the configuration for your application when running with that 
# specific profile. This is equivalent to the {application-name}-{profile}.properties file in the Git solution. 
#
# The /config/application ZNode contains the configuration that applies to all applications. This ZNode is also 
# configurable by setting the property spring.cloud.zookeeper.config.defaultContext.
#
# Finally, if you want to configure something for all applications for a given profile, for example, the logging level 
# for all applications in the UAT environment, then you should define the properties under 
# the /config/application,{profile} ZNode.
#
# You can see that each of the ZNodes containing properties has a property logging.level.ROOT defined, and for the selected 
# ZNode, which configures the UAT profile for our application, the value is DEBUG (look at the “Data as String” field)
#
#
#     ┌─────────────────────────────────────────┐      ┌─────────────────────────────────────────┐ 
#     │           Client Application A          │      │           Client Application B          │ 
#     │                                         │      │                                         │ 
#     │                ┌─────────────────────┐  │      │                ┌─────────────────────┐  │
#     |                |   RefreshEndpoint   │  |      |                |   RefreshEndpoint   │  |
#     |                └─────────────────────┘  |      |                └─────────────────────┘  |
#     |                          |              |      |                           |             |
#     |                  Refresh |              |      |                   Refresh |             |
#     |                          |              |      |                           |             |
#     |                          V              |      |                           V             |  
#     │                ┌─────────────────────┐  │      │                ┌─────────────────────┐  │
#     │                |  Zookeeper Client   │  │      │                |  Zookeeper Client   │  │
#     │                └─────────────────────┘  │      │                └──────────|──────────┘  │
#     └────────────────────────────────|────────┘      └───────────────────────────|─────────────┘ 
#                                      |                                           |
#                                      |                                           |
#                           Get config |                                           |Get config
#                               ┌──────|──────────────────────────────────┐        | 
#                               │      |        Zookeeper                 │        |
#                               │  ┌───V───────────────────────────────┐  │        |
#                               │  │ /config/zookepperConfig           │  │        |
#                               │  │ /config/zookepperConfig.{profile} │<-│--------/
#                               │  │ /config/application               │  │
#                               │  └───────────────────────────────────┘  │
#                               └─────────────────────────────────────────┘ 
#
#
### Architecture of Zookeeper:
#
# ZooKeeper has a hierarchal namespace, much like a distributed file system (in-memory database containing the entire data tree). Each node 
# in the namespace can have data associated with it as well as its children.
#
## Znode - 
#
# Every node in Zookeeper tree is called Znode and each Znode has a associated stat object.  
#
# Stat object has information about version, timestamp etc. 
#
# Each time if client wants to update it, the client application needs to send version number and if version does not match with current 
# version then update will be failed.
#
# Znodes are the main enitities that a programmer access.
#
## Access Control List (ACL)
#
# Each node has an Access Control List (ACL) that restricts who can do what.
#
## Watchers - 
#
# Zookeeper supports watchers. You can register a onetime watcher with any read operations like getData(), getChildren() and exists(). 
#
# Watcher will get triggered if there’s any change.
#
### Type of Nodes:
#
## Ephemeral Nodes
#
# These Znodes exists as long as the session that created the Znode is active.
#
## Sequence Nodes
#
# When creating this type of node, Zookeeper will add a unique sequence number in name.
#
#
## Note:
#
# Write a zookeeper service class, it contains methods to get Zookeeper connection, create new Znode, update Znode data and read Znode data. 
#
# During container start up, this service is creating a parent Znodes for different environments. /config_server is a root configuration and 
# then it is having child Znodes like /dev, /qa, /prod.
#
# This service is registering a one time watcher. Watcher will get fired once there is any update on Znode data.
#
# Write a controller class, External services will use these apis to get latest config information.
#
### Spring cloud zookeeper-
#
## Service discovery
#
# Spring cloud zookeeper uses zookeeper as service registry,stores all service related information and provides api for service discovery.
#
# All services will registered itself during startup. Caller application can use discovery client for logical service discovery.
#
## Application config
#
# Spring cloud zookeeper provides integration with zookeeper. It also provides auto configuration and annotations to implement 
# service discovery and application configs.
#
# Spring cloud zookeeper can be used as config store as well. 
#
#  It can store application config on basis of profile/environment. It is just an alternative of spring config server. 
#
# Watcher is not implemented as part of spring cloud zookeeper config.
#
#
#
	


