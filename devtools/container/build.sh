#!/usr/bin/env bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

docker build --tag huygensing/timbuctoo:latest ../../
