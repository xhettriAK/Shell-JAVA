#!/bin/sh
#
# Use this script to run your program LOCALLY.
#
# Note: Changing this script WILL NOT affect how CodeCrafters runs your program.
#
# Learn more: https://codecrafters.io/program-interface

set -e # Exit early if any commands fail

# Copied from .codecrafters/compile.sh
#
# - Edit this to change how your program compiles locally
# - Edit .codecrafters/compile.sh to change how your program compiles remotely
(
  cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
  mvn -q -B package -Ddir=/tmp/codecrafters-build-shell-java
)

# Copied from .codecrafters/run.sh
#
# - Edit this to change how your program runs locally
# - Edit .codecrafters/run.sh to change how your program runs remotely
#exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 --enable-native-access=ALL-UNNAMED --enable-preview -jar /tmp/codecrafters-build-shell-java/codecrafters-shell.jar "$@"
exec java --enable-native-access=ALL-UNNAMED   --enable-preview -jar /tmp/codecrafters-build-shell-java/codecrafters-shell.jar "$@"