## Distributed Locks
#
# Purpose
#
# The purpose of this document is to implement distributed locking across multiple JVMs using spring integration.
#
# Use Case
#
# Suppose you have an IP to its State table.
#
# |          IP (PK)         | state |      status     |
# | ------------------------ | ----- | --------------- |
# | 161.185.160.93           |  NY   | completed       |
# | 140.241.27.22            |  MA   | completed       |
# | 209.205.209.130          |       | new             |
# | 98.109.27.225            |       | new             |
#
# Multiple instances read and write to this table. They query by IP and insert a row with IP and new status when it doesn't exist.
#
# Slowly the new rows pile up and someone needs to assign the state for the new IPs.
#
## Who will do it?
#
# An instance not part of this cluster.
#
#  Pros -> no need for locks.
#  Cons -> needs to be constantly up, read, and write logic live in different repositories, prone to mistakes.
#
# One instance of this cluster.
#
#  Pros -> read and write logic live in the same repo that makes it easier to read.
#  Cons -> ensure only one instance grabs the lock.
#
## How to ensure only one instance grabs the lock?
#
# Enter spring integration!
#
# We need to do three things:
# 
#   1. Request the lock.
#
#   2. Announce to other instances on acquiring the lock (using a shared DB).
#
#   3. Work on your task and release the lock.
#
# 1 needs to be done by the user.
#
# 2 is done by spring-integration <- can use redis, jdbc, or zookeeper. This post uses jdbc.
#
# 3 needs to be done by the user.
#
# Let's look at some code
#
# User code: Add the below inside a @Scheduled(fixedRate = 10000)) annotated method.
# 
#  //PART 1 and 2
#  Lock lock = registry.obtain("NEW");
#  boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
#  if (acquired) {
#      //PART 3
#      try {
#          LOG.info("Acquired Lock!");
#          List<IpTable> l = ipTableRepository.findByStatus("NEW");
#          for (IpTable i : l) {
#               LOG.info(i.toString());
#               // call third party service to update state and status
#          }
#      } catch (Exception e) {
#          ReflectionUtils.rethrowRuntimeException(e);
#      } finally {
#          lock.unlock();
#          LOG.info("Released Lock!");
#      }
#  } else {
#      LOG.info("No Lock!");
#  }
#  
#  What is “registry” on line 2?
#
# Here the registry is JdbcLockRegistry. It is a wrapper around any JDBC technology you supply spring integration to store lock information.
#
#  @Bean
#  DefaultLockRepository defaultLockRepository(DataSource dataSource) {
#      return new DefaultLockRepository(dataSource);
#  }
#  
#  @Bean
#  JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
#      return new JdbcLockRegistry(lockRepository);
#  }
#  
# What is ipTableRepository on line 8?
#
# Table housing IP, state, and status.
#  
#  @Repository
#  public interface IpTableRepository extends MongoRepository<IpTable, String> {
#      List<IpTable> findByStatus(String status);
#  }
#  
# IpTable?
#  
#  @Document
#  public class IpTable {
#  
#      @Id
#      private String id;
#      private String ip;
#      private String state;
#      private String status;
#  
#      // getters, setters & tostring
#  }
#  application.properties
#  
#  spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
#  spring.datasource.username=postgres
#  spring.datasource.password=postgres
#  spring.jpa.hibernate.ddl-auto=create
#  server.port=0
#  spring.main.allow-bean-definition-overriding=true
#  
#  
#  spring.data.mongodb.database=test
#  spring.data.mongodb.port=27017
#  spring.data.mongodb.host=localhost
#  docker-compose.yml file?
#  
#  version: "3.9"
#  
#  services:
#    db:
#      image: postgres
#      volumes:
#        - ./data/db:/var/lib/postgresql/data
#      environment:
#        - POSTGRES_DB=postgres
#        - POSTGRES_USER=postgres
#        - POSTGRES_PASSWORD=postgres
#      ports:
#        - '5432:5432'
#    mongo:
#      image: mongo
#      #restart: always
#      ports:
#        - 27017:27017
#  
#  
# Log into postgresql and insert INT_LOCK table. Spring integration needs it.
#
#  psql -h localhost -p 5432 -U postgres -W
#  //postgres is the password
#  CREATE TABLE INT_LOCK  (
#          LOCK_KEY CHAR(36) NOT NULL,
#          REGION VARCHAR(100) NOT NULL,
#          CLIENT_ID CHAR(36),
#          CREATED_DATE TIMESTAMP NOT NULL,
#          constraint INT_LOCK_PK primary key (LOCK_KEY, REGION)
#  );
#
# Log into mongo (robo-mongo) and insert fake data.
# 
#  db.getCollection('ipTable').insert( { ip: "161.185.160.93", state: "NY", status: "COMPLETED" } )
#  db.getCollection('ipTable').createIndex( { ip: 1 } )
#  db.getCollection('ipTable').insert( { ip: "140.241.27.22", state: "MA", status: "COMPLETED" } )
#  db.getCollection('ipTable').insert( { ip: "209.205.209.130", state: "", status: "NEW" } )
#  db.getCollection('ipTable').insert( { ip: "98.109.27.225", state: "", status: "NEW" } )
#  
#  
#  
#  
