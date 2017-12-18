#! /bin/bash -e

PWD=$(pwd)

# docker stop $(docker ps -a -q)
# docker rm $(docker ps -a -q)
# docker rmi -f $(docker images -q)

cd $PWD/couchbase
docker build -t michaelcouck/couchbase:latest .
docker run -itd --name couchbase michaelcouck/couchbase
docker push michaelcouck/couchbase:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../elasticsearch
docker build -t michaelcouck/elasticsearch:latest .
docker run -p 80:80 -p 443:443 -p 5601:5601 -p 8080:8080 -p 9200:9200 -itd --name elasticsearch michaelcouck/elasticsearch
docker push michaelcouck/elasticsearch:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../java-8-maven
docker build -t michaelcouck/java-8-maven:latest .
docker run -p 8080:8080 -itd --name java-8-maven michaelcouck/java-8-maven
docker push michaelcouck/java-8-maven:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd ../jenkins
docker build -t michaelcouck/jenkins:latest .
docker run -p 80:80 -p 443:443 -p 8080:8080 -itd --name jenkins michaelcouck/jenkins
docker push michaelcouck/jenkins:latest
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)