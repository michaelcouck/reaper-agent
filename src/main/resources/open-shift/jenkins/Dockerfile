FROM openjdk:8-jdk

RUN apt-get update
RUN apt-get -y upgrade

RUN mkdir -p /root/jenkins

COPY jenkins.war /root/jenkins
COPY jenkins.sh /root/jenkins

EXPOSE 22 443 80 8080

ENTRYPOINT ["/bin/bash", "--", "/root/jenkins/jenkins.sh"]
