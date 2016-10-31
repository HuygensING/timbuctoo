#!/bin/sh

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

./build.sh
while inotifywait -r ../../timbuctoo-instancev4/src ../; 
  do ./build.sh
done