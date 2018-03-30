#@IgnoreInspection BashAddShebang

# cd reaper-agent
# git pull
# mvn install -DskipTests=true -DskipITs=true
# cp target/reaper-agent-1.0-SNAPSHOT.jar reaper

# cd ../reaper-microservice
# git pull
# mvn install -DskipTests=true -DskipITs=true

for var in "$@"
do
	echo Deploying to "$var"
	# Kill the agent if it is running on the server already, note this does not kill the agents in the JVMs
	ssh msservice@"$var" 'pkill -9 -f reaper-agent'
	echo Killed agent on "$var"

	# Create the directory to deploy to if it does not exist
	ssh msservice@"$var" 'rm -rf /msservice/app/reaper/deployments/*'
	echo Deleted deployment directory on "$var"
	ssh msservice@"$var" 'mkdir -p /msservice/app/reaper/deployments'
	echo Created deployment directory on "$var"

	# Deploy the agent to the server
	scp -r reaper/* msservice@"$var":/msservice/app/reaper/deployments
	echo Deployed agent to "$var"

	# Start the agent on the target server
	# Check that the agent is running
	ssh msservice@"$var" 'cd /msservice/app/reaper/deployments; java -jar reaper-agent-1.0-SNAPSHOT.jar &'
done

# el5765 el5766 el5767 el5768 el5798 el5769 el5779 el5780 el5781