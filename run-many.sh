for number in {1..25}
do
    sleep 1
    java -jar target/reaper-agent-1.0-SNAPSHOT.jar &
done
exit 0

