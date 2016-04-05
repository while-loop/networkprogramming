#!/bin/bash

# default mode and server port
mode="-u"
port=4232

if [ -n "$1" ]
	then
		port=$1
fi


cd bin/
java -classpath ".:../lib/*" java.Client -p $port $mode < ../queries
