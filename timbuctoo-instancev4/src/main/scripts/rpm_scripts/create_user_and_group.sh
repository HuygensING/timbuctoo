#!/usr/bin/env bash

GROUP_STRING=`getent group datarepository`
if [ ! "$USER_STRING" ]; then
  echo "create group \"datarepository\""
  groupadd datarepository

fi

USER_STRING=`getent passwd datarepository`
if [ ! "$USER_STRING" ]; then
  echo "create user \"datarepository\""
  useradd -g datarepository datarepository

fi
