#!/usr/bin/env bash

GROUP_STRING=`getent group datarepository`
if [ ! "$GROUP_STRING" ]; then
  echo "create group \"datarepository\""
  groupadd datarepository

fi

USER_STRING=`getent passwd datarepository`
if [ ! "$USER_STRING" ]; then
  echo "create user \"datarepository\""
  useradd --system -g datarepository datarepository -d /opt/timbuctoo

fi
