CNV 2018-19 - HillClimbing@Cloud
================================
WebServer
---------
Package name : `web-server`.

Compile, instrument and package the code in a jar with:
```
mvn clean compile test assembly:single
```
Run: `java -noverify -jar web-server.jar <port>`

Load-balancer &  Auto-scaler
-------------
Package name : `load-balancer`.
```
mvn clean compile test assembly:single
```
Run: `java -noverify -jar load-balancer.jar`

Project Organization
--------------------

### WEB-SERVER
The changes in the web server are primarily in the metrics module where we store metrics while they are being processed and then we send them to Dynamo. In this module we also have the code responsible for the instrumentation.

### LOAD-BALANCER + AUTO-SCALER + METRICS STORAGE SYSTEM

* In the module Load balancer we have a cloud manager that is responsible for handling all operations with AWS, namely operations like creating new instances, deleting and monitoring.
* In the auto scaler module lays the logic for upscaling and downscaling our system.
* The load balancer contains the logic for balancing the requests between various instances.
* The policies contain the code responsible for making request metrics estimation
* The monitors are active components that poll and check for different system status like healthchecking, progress monitoring, metrics gathering, etc.
* Lastly we have the reverse proxy that is the main web server for the overall system.

In general, all this code runs on the same machine.

### CONFIGURATION FILE
We configure our load balancer using static variables in our autoscaler.