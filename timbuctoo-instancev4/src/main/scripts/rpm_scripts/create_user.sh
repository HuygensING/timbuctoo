#!/usr/bin/env bash

USER_STRING=`getent passwd datarepository`
if [ ! "$USER_STRING" ]; then
  echo "create user \"datarepository\""
  useradd datarepository
fi
