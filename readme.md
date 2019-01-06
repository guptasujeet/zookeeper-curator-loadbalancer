Overview
---------
This tutorial demonstrates Apache ZooKeeper for load balancing using Apache Curator. 

This tutorial demonstrates following features
 - TaskWorker Registry via Worker
 - TaskWorker Discovery via Manager
 - Dynamic Load Publishing via Worker
 - ProviderStrategy -> BalancedLoadStrategy (Custom Strategy) for Manager
 - Simple REST Worker & Manager
 

Starting Worker
---------------

    mvn exec:java@worker_1
    mvn exec:java@worker_2
    mvn exec:java@worker_3
     
     
     
Starting Manager
----------------

    mvn exec:java@manager
    
    

Starting ZooKeeper
-----------------
The above assumes that ZooKeeper is running on localhost at port 2181.

  - You can also run ZooKeeper using the runConfigurations provided 
  - Or Maven ZooKeeper plugin Start / Stop 
  
        mvn net.revelc.code:zookeeper-maven-plugin:start
        mvn net.revelc.code:zookeeper-maven-plugin:stop

Testing Load Balancer
---------------------

    for i in 1 2 3 4 5 6 7 8 9 10
    do
       curl 'http://localhost:18000/delegate'
       echo ""
    done
    
    
    
Inspired From 
-------------------------
http://tomaszdziurko.com/2014/07/zookeeper-curator-and-microservices-load-balancing/
 