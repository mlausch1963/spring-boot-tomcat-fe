Example Web frontend for Kubernetes/Openshift. It contains

 - A second Executor for the actuator endpoints. Prevents starving of 
   healthcheck and monitoring endpoints
 
 - Implementation of a health indicator which returns 500 to healthchecks,
   throttling incoming traffic.

 - Prometheus monitoring.



https://stackoverflow.com/questions/17710701/how-to-restrict-access-to-certain-urls-a-specific-port
https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html
