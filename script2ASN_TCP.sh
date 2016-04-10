#!/bin/bash

# default mode and server port
port=4232
host="localhost"
mode="-t"

cd bin/
java -classpath ".:../lib/*" main/Client -h $host -p $port $mode
