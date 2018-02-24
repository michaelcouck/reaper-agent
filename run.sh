export JMX_PARAMETERS="-Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.local.only=true  -Dcom.sun.management.jmxremote.rmi.port=1100 -Dcom.sun.management.jmxremote=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false"
# java -jar $JMX_PARAMETERS target/reaper-agent-1.0-SNAPSHOT.jar
java -jar \
    -Dlocalhost-jmx-uri=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi \
    -Dreaper-rest-uri-j-metrics=http://ikube.be:8090/j-metrics \
    -Dreaper-rest-uri-o-metrics=http://ikube.be:8090/o-metrics \
    -Dreaper-web-socket-uri=ws://ikube.be:8090/reaper-websocket \
    target/reaper-agent-1.0-SNAPSHOT.jar