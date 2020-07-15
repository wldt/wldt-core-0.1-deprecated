#!/bin/bash

# Build Classpath
export CLASSPATH="./target/classes"
for file in `ls target/*.jar` ; do export  CLASSPATH=$CLASSPATH':'./target/$file; done
for file in `ls target/dependency` ; do export  CLASSPATH=$CLASSPATH':'./target/dependency/$file; done

# Pass all arguments to java run
java -classpath $CLASSPATH "$@"
