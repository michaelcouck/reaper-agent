#! /bin/bash -e

PWD=$(pwd)
export A=JMX_URI=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
export B=WEB_SOCKET_URI=ws://ikube.be:8090/reaper-websocket
export C=REAPER_ZIP=https://ikube.be/artifactory/libs-release-local/com/pxs/reaper-agent/1.0-SNAPSHOT/reaper-agent-1.0-SNAPSHOT-linux.zip

# docker stop $(docker ps -a -q)
# docker rm $(docker ps -a -q)
# docker rmi -f $(docker images -q)

cd $PWD/couchbase
docker build -t michaelcouck/couchbase:latest .
docker run -e $A -e $B -e $C -itd --name couchbase michaelcouck/couchbase
docker push michaelcouck/couchbase:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../elasticsearch
docker build -t michaelcouck/elasticsearch:latest .
docker run -p 80:80 -p 443:443 -p 5601:5601 -p 8080:8080 -p 9200:9200 -e $A -e $B -e $C -itd --name elasticsearch michaelcouck/elasticsearch
docker push michaelcouck/elasticsearch:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../java-8-maven
docker build -t michaelcouck/java-8-maven:latest .
docker run -p 8080:8080 -e $A -e $B -e $C -itd --name java-8-maven michaelcouck/java-8-maven
docker push michaelcouck/java-8-maven:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../jenkins
docker build -t michaelcouck/jenkins:latest .
docker run -p 80:80 -p 443:443 -p 8080:8080 -e $A -e $B -e $C -itd --name jenkins michaelcouck/jenkins
docker push michaelcouck/jenkins:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)