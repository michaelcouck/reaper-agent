FROM ubuntu:16.04
# FROM ubuntu:17.10

# Install maven
RUN apt-get update
RUN apt-get -y upgrade

RUN apt-get install -y java-common openjdk-8-jdk maven git openssh-server vim

RUN mkdir /var/run/sshd
RUN echo 'root:screencast' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

EXPOSE 22 80 443 8080

CMD ["/usr/sbin/sshd", "-D"]

RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin:$JAVA_HOME/bin

RUN ls -l /usr/lib/jvm

ENV JAVA_VERSION 8u151
ENV JAVA_ALPINE_VERSION 8.151.12-r0
ENV SOURCE_CODE 'https://github.com/michaelcouck/fuck-off-as-a-service.git'
ENV SOURCE_DIRECTORY /root/source

RUN rm -rf /root/source
RUN mkdir -p /root/source
RUN chmod 777 -R /root/source

COPY run.sh /root
RUN chmod 777 -R /root/*
ENTRYPOINT ["/root/run.sh"]
