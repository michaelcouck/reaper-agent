Reaper
======

The reaper-agent is a metrics gathering agent for posting metrics to the monitoring and 
alerting micro service. The target micro service is in the reaper.properties file.

The agent is started as a java application i.e.:

     * java -jar reaper-agent-SNAPSHOT.1.0.jar
     
It will connect to all the JVM(s) that are running on the local operating system, and additionally start an agent 
running on the local operating system that collects metrics from the OS. In the event there is a JVM on the local host that 
exposes JMX(in the reaper.properties file the port is 1099) then the agent will connect to the JMX beans to collect 
the JVM metrics. This is useful for some implementations of PAAS, like in some case OpenShift, where the PID(s) are not made 
available to processes running on the same pod, possibly for security reasons.

Note that if the agent is terminated, the JVM(s) that are attached will not be detached as this functionality is not exposed by the 
current implementations of the JVM.

The agent can also be started as a -javaagent:reaper-agent-SNAPSHOT.1.0.jar, in this way only the target JVM will be intercepted
and attached to rather than all the JVMs on the operating system.