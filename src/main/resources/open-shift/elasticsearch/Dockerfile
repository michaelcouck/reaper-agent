FROM ubuntu:16.04

# Install maven & common packages, java, ssh etc.
RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get install -y java-common openjdk-8-jdk maven git openssh-server vim apt-utils

# Set up ssh directories and allow root login without password
RUN mkdir /var/run/sshd
RUN echo 'root:screencast' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config
# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

# Set the java home and syn links
RUN { \
                echo '#!/bin/sh'; \
                echo 'set -e'; \
                echo; \
                echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
        } > /usr/local/bin/docker-java-home \
        && chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin:$JAVA_HOME/bin
ENV JAVA_ALPINE_VERSION 8.151.12-r0

RUN apt-get -y install wget apt-transport-https
RUN echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-6.x.list
RUN wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -

RUN apt-get update
RUN apt-get install -y software-properties-common python-software-properties elasticsearch kibana

WORKDIR /root
COPY run.sh /root
RUN chmod 777 /root/*
ENTRYPOINT ["/root/run.sh"]

EXPOSE 22 80 443 5601 8080 9200