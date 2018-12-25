Reference Groovy Example
-------------------------
http://tomaszdziurko.com/2014/07/zookeeper-curator-and-microservices-load-balancing/
 

Starting Worker
---------------

    java TaskWorker Worker_1 18005
    java TaskWorker Worker_2 18006
     
     
     
Starting Manager
----------------

    java Manager 18000
    
    

Starting ZooKeeper
-----------------
The above assumes that ZooKeeper is running on localhost at port 2181

