#!/bin/bash

port=4232
mode="-u"
time_offest=5

if [ -n "$1" ]
	then
		port="$1"
fi

# create the start and end date
format="%Y-%m-%d:%Hh%Mm%Ss%3NZ"
start=$(date +"$format")
end=$(date +"$format" --date="now + $time_offest seconds")

# commands to send to the client
proj_def="PROJECT_DEFINITION:Expiration;TASKS:1;Create Script;$start;$end;"
add_user="TAKE;USER:Anthony;PROJECT:Expiration;Create Script"
get_proj="GET_PROJECT;Expiration"

# run client and add the project
cd bin/
java -classpath ".:../lib/*" java.Client -p $port $mode <<EOF
$proj_def
$add_user
EOF

# wait 5 seconds so the date has passed
echo "Sleeping for $time_offest seconds..."
sleep $time_offest # wait for the time to pass

# run the program again to get the project
java -classpath ".:../lib/*" java.Client -p $port $mode <<EOF
GET_PROJECT;Expiration
EOF

echo "Finished."