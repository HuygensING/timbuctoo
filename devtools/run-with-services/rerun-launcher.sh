#!/bin/bash

#http://veithen.github.io/2014/11/16/sigterm-propagation.html
trap 'kill -TERM $timbuctooPid; wait $timbuctooPid' TERM INT

touch /tools/trigger

function startTimbuctoo() {
  if [ -f /app/timbuctoo-instancev4/target/appassembler/bin/timbuctoo ]; then
    [ -f /tools/java_opts ] && export JAVA_OPTS="$(cat /tools/java_opts)"
    echo JAVA_OPTS="${JAVA_OPTS}"
    /app/timbuctoo-instancev4/target/appassembler/bin/timbuctoo server ./timbuctoo-instancev4/docker_config.yaml &
    timbuctooPid=$!
  else 
    unset timbuctooPid
    echo "No compiled version of timbuctoo available at the moment"
  fi
}

if [ -f /root/timbuctoo-prod-db/backup.tar.bz2 ]; then
  echo "Extracting database backup"
  pushd /root/timbuctoo-prod-db/
  tar -xjvf backup.tar.bz2 --strip-components=1
  popd
fi

startTimbuctoo

while inotifywait -q -e close_write /tools/trigger /root/timbuctoo-prod-db/done; do
  #if /tools/trigger contains the text 'rebuild', then rebuild, send the result to build-log, when build is finished remove build-log
  #if it doesn't contain that text then no rebuild is triggered and timbuctoo is simply relaunched
  grep -q "rebuild" < /tools/trigger && mvn clean package -DskipTests &> /app/build-log && rm /app/build-log &
  grep -q "retest" < /tools/trigger && mvn clean package &> /app/build-log && rm /app/build-log &
  #trigger timbuctoo shutdown in the meantime and wait until it is shutdown
  [ -n "$timbuctooPid" ] && kill -TERM $timbuctooPid && wait $timbuctooPid
  #send buildlog to the console and wait until the build is finished (because then the file is removed)
  #-F is different from -f. -f keeps following the same file (even if you remove it) -F will check periodically what
  #file the filename points to
  #this means that you have to check whether the file exists separately, because  tail will just wait until it does
  [ -f /app/build-log ] && tail -F /app/build-log 
  #launch if the launch script is present
  startTimbuctoo
  #and get back to waiting for rebuild triggers
done
