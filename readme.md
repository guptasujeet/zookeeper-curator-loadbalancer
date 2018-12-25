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

    java TaskWorker Worker_1 18005
    java TaskWorker Worker_2 18006
     
     
     
Starting Manager
----------------

    java LoadManager 18000
    
    

Starting ZooKeeper
-----------------
The above assumes that ZooKeeper is running on localhost at port 2181.

You can also run ZooKeeper using the runConfigurations provided.

Testing Load Balancer
---------------------

    for i in 1 2 3 4 5 6 7 8 9 10
    do
       curl 'http://localhost:18000/delegate'
       echo ""
    done
    
    
    
Reference Groovy Example
-------------------------
http://tomaszdziurko.com/2014/07/zookeeper-curator-and-microservices-load-balancing/
 