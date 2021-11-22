Example Web frontend for Kubernetes/Openshift. It contains

 - A second Executor for the actuator endpoints. Prevents starving of 
   healthcheck and monitoring endpoints
 
 - Implementation of a health indicator which returns 500 to healthchecks,
   throttling incoming traffic.

 - Prometheus monitoring.


Classes 
=======

This are all the classes implementing bits and pieces for monitoring. The "normal" classes for the MVC example are not desctibed here. The application is a simple message store, not persistent, allowing the creation, delettion and modification of messages. 

CustomizeBoundResourcesExecutor
--------------------------------
Allocates a bounded resources executor and register counter and gauges. 

ExecutorHealthIndicator
-----------------------
Healthcheck which also checks the executor metrics for overflow. If the thread and queue counts exceed maximum values, the health indicator is set to false. This could be used for the readyness probe to signal the overload condition, causing the load balancer to stop sending traffic to the pod

ExecutorMetrics
---------------
Simple interface for executor metrics. Implemented by CustomizeBoundResourcesExecutor

InMemoryMessageRepository
-------------------------
Simple repo for holding messages. Also implements business metrics, messages created, deleted, modified and so on.

TomcatConfig.java
-----------------
Define the customization to use a special Executor and use CustomizeBoundResourcesExecutor to create it and set it up. 





See https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector how to use micrometer in Spring Boot 2.

https://stackoverflow.com/questions/17710701/how-to-restrict-access-to-certain-urls-a-specific-port
