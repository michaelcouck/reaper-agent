#!/usr/bin/env bash
base_directory=$(pwd)
export JAVA_OPTS="-Xms128m -Xmx256m -XX:HeapDumpPath=/tmp -Djava.awt.headless=true"

# Java must be installed on the machines and the JAVA_HOME set
# Alternatively the JDK can be packaged with the agent, but makes the deliverable much larger
# export JAVA_HOME=$base_directory/jdk1.8.0_112
# export TOMCAT_DIR=$base_directory/apache-tomcat-9.0.0.M15
