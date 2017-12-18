FROM couchbase/server:latest

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install software-properties-common -y
RUN add-apt-repository ppa:openjdk-r/ppa -y
RUN apt-get update -y

RUN lsb_release -a
RUN apt-cache search openjdk

RUN apt-get install java-common openjdk-8-jdk git openssh-server -y

RUN update-alternatives --config java
RUN update-alternatives --config javac

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

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

EXPOSE 22 80 443 8080

WORKDIR /root
COPY run.sh /root
RUN chmod 777 /root/*
ENTRYPOINT ["/root/run.sh"]

# CMD ["/usr/sbin/sshd", "-D"]
# RUN ["ifconfig", "-a"]
# CMD ["/usr/bin/supervisord"]

