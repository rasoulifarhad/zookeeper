## Start a Zookeeper server instance
#
#  $ docker run --network zoo_net --name zoo1 --restart always -d zookeeper:3.8.0
#  
# This image includes EXPOSE 2181 2888 3888 8080 (the zookeeper client port, follower port, election port, AdminServer port respectively), 
# so standard container linking will make it automatically available to the linked containers. Since the Zookeeper "fails fast" it's better 
# to always restart it.
#  
## Connect to Zookeeper from an application in another Docker container
#
#  $ docker run --network zoo_net --name zoo1  -d application-that-uses-zookeeper
#
## Connect to Zookeeper from the Zookeeper command line client
#
#  $ docker run -it --rm --network zoo_net zookeeper:3.8.0 zkCli.sh -server zookeeper
#
#
  
