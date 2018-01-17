export JMX_PARAMETERS="-Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.local.only=true  -Dcom.sun.management.jmxremote.rmi.port=1100 -Dcom.sun.management.jmxremote=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false"
# java -jar $JMX_PARAMETERS target/reaper-agent-1.0-SNAPSHOT.jar
java -Dreaper-web-socket-uri=ws://ikube.be:8090/reaper-websocket -Dlocalhost-jmx-uri=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi -jar target/reaper-agent-1.0-SNAPSHOT.jar
