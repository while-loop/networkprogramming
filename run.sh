#!/bin/bash

# default sqlite file and server port
sqliteFile="$(pwd)/np.sqlite"
port=4232

if [ -n "$1" ]
	then
		port=$1
fi

if [ -n "$2" ]
	then
		sqliteFile=$2
fi

cd bin/
java -classpath ".:../lib/*" main/Server -d $sqliteFile -p $port
