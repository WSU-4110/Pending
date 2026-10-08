#!/bin/bash

set -a
source .env
set +a

export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

echo "Starting Pending server with Java:"
java -version

./gradlew bootRun