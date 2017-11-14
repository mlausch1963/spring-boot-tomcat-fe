Example Web frontend for Kubernetes/Openshift. It contains

 - A second Executor for the actuator endpoints. Prevents starving of 
   healthcheck and monitoring endpoints
 
 - Implementation of a health indicator which returns 500 to healthchecks,
   throttling incoming traffic.

 - Prometheus monitoring.

For tomcat:

Using a connectorcustomizer to manipulate the Executor does not work

  - When the customizer is called, there is no executor
  - You cannot set a custom executor

So we use a spring boot event to fix the executor after the embedded container has been initialized.



https://stackoverflow.com/questions/17710701/how-to-restrict-access-to-certain-urls-a-specific-port
https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html
