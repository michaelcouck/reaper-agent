#! /bin/bash -e

# The code and the directory where it will go
# export SOURCE_CODE=https://github.com/michaelcouck/fuck-off-as-a-service.git
# export SOURCE_DIRECTORY=$(pwd)/source
rm -rf $SOURCE_DIRECTORY
mkdir $SOURCE_DIRECTORY
echo Source code: $SOURCE_CODE, source directory: $SOURCE_DIRECTORY
chmod 777 -R $SOURCE_DIRECTORY
cd $SOURCE_DIRECTORY

# Clone the source into the directory for building
git clone $SOURCE_CODE $SOURCE_DIRECTORY
mvn install

# Print the current directory and list the output of the maven build
echo Working directory: $(pwd)
echo List source directory: $(ls -l)
echo List target directory: $(ls -l target)

# Finds the first jar in the maven target build directory and execute it
echo Executing application artifact: $(find target -maxdepth 1 -name "*.jar")
find target -maxdepth 1 -name "*.jar" | xargs -n1 java -jar &

# System variables to set on command line or in OpenShift for the reaper
# export JMX_URI=service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
# export WEB_SOCKET_URI=ws://ikube.be:8090/reaper-websocket
# export REAPER_ZIP=https://ikube.be/artifactory/libs-release-local/com/pxs/reaper-agent/1.0-SNAPSHOT/reaper-agent-1.0-SNAPSHOT-linux.zip

# Sanity check
echo Java version: $(java -version)
echo Java home: $JAVA_HOME

# We should be (g)root
cd $SOURCE_DIRECTORY

# Install wget first, we'll need it to get the reaper zip
apt-get -y install wget unzip

# Get the agent zip and unpack it
wget --no-check-certificate --no-proxy $REAPER_ZIP
unzip reaper-agent-1.0-SNAPSHOT-linux.zip
chmod 777 -R *
cd reaper

echo Reaper directory: $(ls -l)
java -Dlocalhost-jmx-uri=$JMX_URI -Dreaper-web-socket-uri=$WEB_SOCKET_URI -jar reaper-agent-1.0-SNAPSHOT.jar &