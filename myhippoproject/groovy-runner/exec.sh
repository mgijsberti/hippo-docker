#!/bin/sh

mvn clean package
# Absolute path to the folder with property file.
RESOURCES="/Users/mhodenpijl/IdeaProjects/ro-groovy-runner/src/main/resources"
RUNNER_PROPERTIES=${RESOURCES}/runner.properties
LOG4J=log4j.xml
DEBUG=$1
alias mvnDebug='/usr/share/maven/bin/mvnDebug'

if [ ${DEBUG} ]
then
	mvnDebug exec:java -Dexec.args=${RUNNER_PROPERTIES} -Dlog4j.configuration=${LOG4J} -Dlog4j.debug=true
else
	mvn exec:java -Dexec.args=${RUNNER_PROPERTIES} -Dlog4j.configuration=${LOG4J} -Dlog4j.debug=false
fi
