#!/bin/sh
# Start the server and the backup application
base_directory=$(pwd)
. $base_directory/env.sh
# rm -rf logs/*
nohup java -jar reaper-agent-1.0-SNAPSHOT.jar &