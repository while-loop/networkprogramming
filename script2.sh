#!/bin/bash

# default mode and server port
port=4232

if [ -n "$1" ]
	then
		port=$1
fi

cd bin/
java -classpath ".:../lib/*" java.Client -p $port
