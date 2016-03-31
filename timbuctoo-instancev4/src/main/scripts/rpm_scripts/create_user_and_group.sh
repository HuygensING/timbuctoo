#!/usr/bin/env bash

GROUP_STRING=`getent group timbuctoo`
if [ ! "$GROUP_STRING" ]; then
  echo "create group \"timbuctoo\""
  groupadd timbuctoo

fi

USER_STRING=`getent passwd timbuctoo`
if [ ! "$USER_STRING" ]; then
  echo "create user \"timbuctoo\""
  useradd --system -g timbuctoo timbuctoo -d /data/timbuctoo

fi
