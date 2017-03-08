#!/bin/bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

DEBUG_PORT="5005"
DEBUG_HALT="n"
MISSION_CONTROL_PORT="7091"

OPTIND=1
while getopts ":fhqQrt" opt; do
    case "$opt" in
  f)
    task=logs
    ;;
  h)
    echo -e "$0: A small wrapper around 'docker-compose up'\n\n\
    -f    tail the logs\n\
    -Q    shut down all containers and remove the volumes\n\
    -q    shutdown all containers and quit\n\
    -r    rebuild before re-launching (skips tests, add -t to run them)\n\
    -t    run tests when rebuilding" >&2
    exit 0
    ;;
  q)
    task=quit
    ;;
  Q)
    task=clean
    ;;
  r)
    REBUILD=y
    ;;
  t)
    RUN_TESTS=y
    ;;
  \?)
    echo "Invalid option: -$OPTARG use -h for an overview of valid options" >&2
    exit 1
    ;;
  :)
    if [ $OPTARG = "d" ]; then
      echo "please specify a port for the debugger (the usual port is 5005, so -d 5005)" >&2
      exit 1
    elif [ $OPTARG = "f" ]; then
      echo "please specify a file to store the flight recording (e.g. -f recording.jfr)" >&2
      exit 1
    fi
    ;;
  esac
done

if [ "$REBUILD" = y ]; then
  if [ "$RUN_TESTS" = y ]; then
    trigger_arg="retest"    
  else
    trigger_arg="rebuild"
  fi
else
  trigger_arg=""
fi

export JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_HALT},address=${DEBUG_PORT}"
if [ "$task" = quit ]; then
  echo "Stopping"
  docker-compose stop
elif [ "$task" = clean ]; then
  echo "Quitting"
  docker-compose down
elif docker-compose exec timbuctoo echo '' 2> /dev/null; then #timbuctoo is already up
  docker-compose exec timbuctoo /tools/trigger.sh "$trigger_arg" "$JAVA_OPTS"
else
  if [ -z "$public_ip" ]; then
    export public_ip=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1')
    if [ $(echo $public_ip | wc -l) != 1 ]; then
      echo -n "Under what ip is your computer reachable from the network? (this should NOT be 127.0.0.<something>, instead it's probably a number such as 10.<something> or 192.168.<something>): "
      read public_ip
      export public_ip
    fi
  fi
  echo "Listening for debug signals on        [${DEBUG_PORT}]"
  echo "halting until debugger is attached    [${DEBUG_HALT}]"
  echo "Running command                       [server]"
  echo "using yaml at                         [$(realpath ../../timbuctoo-instancev4/docker_config.yaml)]"
  echo "pulling new images before launch      [y]"
  echo "using this ip for generating urls:    [$public_ip]"
  echo ""

  echo "Checking for new containers..."
  docker-compose pull &> /dev/null
  docker-compose up -d
  echo "You can now browse timbuctoo on http://$public_ip:8080"
  docker-compose logs -f timbuctoo
  echo -e "\n\nContainers are still running in the background. Use $0 -q to terminate them."
fi
