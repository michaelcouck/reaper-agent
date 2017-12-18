FROM couchbase/server:latest

RUN apt-get update && apt-get install -y openssh-server vim
RUN mkdir /var/run/sshd
RUN echo 'root:password' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

RUN echo "root:password" | chpasswd

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

EXPOSE 22 80 443 8080

WORKDIR /root
COPY run.sh /root
RUN chmod 777 /root/*
ENTRYPOINT ["/root/run.sh"]

# CMD ["/usr/sbin/sshd", "-D"]
# RUN ["ifconfig", "-a"]
# CMD ["/usr/bin/supervisord"]

