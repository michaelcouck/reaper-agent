#! /bin/bash -e

# System variables to set on command line or in OpenShift for the reaper
# JMX_URI=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
# WEB_SOCKET_URI=ws://ikube.be:8090/reaper-websocket
# REAPER_ZIP=https://ikube.be/artifactory/libs-release-local/com/pxs/reaper-agent/1.0-SNAPSHOT/reaper-agent-1.0-SNAPSHOT-linux.zip

# Sanity check
echo Java version: $(java -version)
echo JAva home: $JAVA_HOME

# We must be (g)root
cd /root

# Install wget first, to get the reaper zip from artifactory
apt-get -y install wget unzip

# Get the agent zip and unpack it
wget --no-check-certificate --no-proxy $REAPER_ZIP
unzip reaper-agent-1.0-SNAPSHOT-linux.zip
chmod 777 -R *
cd reaper

echo Reaper directory: $(ls -l)
java -Dlocalhost-jmx-uri=$JMX_URI -Dreaper-web-socket-uri=$WEB_SOCKET_URI -jar reaper-agent-1.0-SNAPSHOT.jar