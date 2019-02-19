Reaper
======

The reaper-agent is a metrics gathering agent for posting metrics to the monitoring and 
alerting micro service. The target micro service is in the reaper.properties file.

The agent is started as a java application i.e.:

     * java -jar reaper-agent-SNAPSHOT.1.0.jar
     
It will connect to all the JVM(s) that are running on the local operating system, and additionally start an agent 
running on the local operating system that collects metrics from the OS.

Note that if the agent is terminated, the JVM(s) that are attached will not be detached as this functionality is not exposed by the 
current implementations of the JVM.

The agent can also be started as a -javaagent:reaper-agent-SNAPSHOT.1.0.jar, in this way only the target JVM will be intercepted
and attached to rather than all the JVMs on the operating system. This type of connection is not continuously tested, and might need some fiddling to get it to work again.

The agent pushed data to the reaper micro service over rest. Consequently to collect the data you need an endpoint that is defined to accept that data type.

Questions? michael dot couck at gmail dot com
