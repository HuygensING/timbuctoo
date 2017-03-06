#!/bin/sh

if [ -n "$2" ]; then
  echo "$2" > /tools/java_opts
else
  [ -f /tools/java_opts ] && rm /tools/java_opts
fi

if [ "$1" = "rebuild" ]; then
  echo "rebuild" > /tools/trigger
elif [ "$1" = "retest" ]; then
  echo "retest" > /tools/trigger
else  
  echo "" > /tools/trigger
fi
