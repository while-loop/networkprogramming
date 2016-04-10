#!/bin/bash

# default mode and server port
port=4232
host="localhost"
mode="-t"


while getopts "h:u:p:d:" flag; do
case "$flag" in
        u)      mode="-u";;
        t)      mode="-t";;
esac
done

shift $(( OPTIND-2 ))

if [ -n "$1" ]
	then
		host=$1
fi

if [ -n "$2" ]
	then
		port=$2
fi



cd bin/
java -classpath ".:../lib/*" main/Client -h $host -p $port $mode
