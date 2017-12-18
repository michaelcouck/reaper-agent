#! /bin/bash -e

echo 'Starting elastic search'
# update-rc.d elasticsearch defaults 95 10
/etc/init.d/elasticsearch start

ps -ef 

# echo 'Enabling elastic search'
# /bin/systemctl daemon-reload
# /bin/systemctl enable elasticsearch.service
# echo 'Starting elastic search again'
# /bin/systemctl start elasticsearch.service

# ls -lh
# ls -lh /root
# ls -lh /root/lib

java -version
echo $JAVA_HOME
java -Dlocalhost-jmx-uri=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi -Dreaper-web-socket-uri=ws://ikube.be:8090/reaper-websocket -jar /root/reaper-agent-1.0-SNAPSHOT.jar
echo 'Started the reaper'

# echo 'Starting the reaper'
# cd /root
# find . -maxdepth 1 -name "*.jar" | xargs -n1 java -jar
