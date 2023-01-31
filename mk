#!/usr/bin/env bash
set -e

APP=base


##################################################
# Parse arguments:
#   [clean]
##################################################

CLEAN=""
DONEARGS=0

while [ "$DONEARGS" -eq 0 ]; do
  if [ "$1" == "" ]; then
    DONEARGS=1
  elif [ "$1" == "clean" ]; then
    CLEAN="clean"
    shift 1
  else
    echo "Unrecognized argument: $1"
    exit 1
  fi
done


##################################################
# Perform clean, if requested
#
if [ "$CLEAN" != "" ]; then
  echo "...cleaning $APP"
  mvn clean
  if [ "$DRIVER" -ne "0" ]; then
    if [ -f $LINK ]; then
      unlink $LINK
    fi
  fi
fi

mvn package -DskipTests

cp screenshotter.plist ~/Library/LaunchAgents
mv target/base-1.0.jar ~/screenshotter.jar


# launchctl load ~/Library/LaunchAgents/screenshotter.plist

# or:

# launchctl start screenshotter
