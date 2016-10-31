#!/bin/sh
./build.sh
while inotifywait -r ../timbuctoo-instancev4/src .; 
  do ./build.sh
done