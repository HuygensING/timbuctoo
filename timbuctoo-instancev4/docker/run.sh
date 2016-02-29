#!/usr/bin/env bash

if [ "$1" = "/bin/bash" -o "$1" = "bash" ]; then
  /bin/bash
else
  echo "Using the following config:"
  echo "======"
  cat "$2"
  echo "======"

  overrides=""
  if [ -n "USE_DUMMY_HANDLE_SERVER" ]; then
    overrides="$overrides -Ddw.persistenceManager.useDummy=true"
  else
    if [ -z "$PERSISTENCE_MANAGER_PRIVATEKEYFILE" -o -z "$PERSISTENCE_MANAGER_CYPHER" -o -z "$PERSISTENCE_MANAGER_NAMINGAUTHORITY" -o -z "$PERSISTENCE_MANAGER_PREFIX" ]; then
      echo "Pass env variable USE_DUMMY_HANDLE_SERVER=yes (docker run -e USE_DUMMY_HANDLE_SERVER=yes timbuctoo) or pass \
      values for PERSISTENCE_MANAGER_PRIVATEKEYFILE, PERSISTENCE_MANAGER_CYPHER, PERSISTENCE_MANAGER_NAMINGAUTHORITY and \
      PERSISTENCE_MANAGER_PREFIX"
      exit 1
    else
      overrides="$overrides -Ddw.persistenceManager.privateKeyFile=$PERSISTENCE_MANAGER_PRIVATEKEYFILE"
      overrides="$overrides -Ddw.persistenceManager.cypher=$PERSISTENCE_MANAGER_CYPHER"
      overrides="$overrides -Ddw.persistenceManager.namingAuthority=$PERSISTENCE_MANAGER_NAMINGAUTHORITY"
      overrides="$overrides -Ddw.persistenceManager.prefix=$PERSISTENCE_MANAGER_PREFIX"
    fi
  fi

  if [ -z "$BASE_URI" ]; then
    if [ -n "$DOCKER_COMPOSE_HOST" ]; then
      #DOCKER_COMPOSE_HOST is set to the value of the DOCKER_HOST variable which contains a string like tcp://<ip>:2376
      #on docker-machine installations (i.e. docker inside virtualbox on windows or mac)
      #we parse the ip from the tcp:// string using the regex below
      DOCKER_COMPOSE_HOST=$(echo $DOCKER_COMPOSE_HOST | sed 's|[a-z]*://\([0-9.]*\):[0-9]*|\1|')
    fi
    #when running docker-compose up on linux the host will be "" and the port will be 8080
    #when running docker-compose up on mac or windows the host will be something like "192.169.99.100" and the port will be 8080
    #when running docker run on linux, mac or windows the host will be "" and the port will be ""
    if [ -n "$DOCKER_COMPOSE_HOST" -o -n "$DOCKER_COMPOSE_PORT" ]; then
      BASE_URI="http://${DOCKER_COMPOSE_HOST:-localhost}:$DOCKER_COMPOSE_PORT/"
    else
      echo "I can't guess at what hostname and port this server is accessed. Therefore I can't generate Url's myself. Please provide the base url using -e 'BASE_URI=http://example.com:8080/'"
      exit 1
    fi
  fi
  overrides="$overrides -Ddw.baseUri=$BASE_URI"

  #detect if the graylog container is linked or the graylog service has been explicitly configured
  GELF_HOST=${GELF_HOST:-$GRAYLOG_PORT_12201_UDP_ADDR}
  GELF_PORT=${GELF_PORT:-$GRAYLOG_PORT_12201_UDP_PORT}
  if [ -n "$GELF_HOST" ]; then
    overrides="$overrides -Ddw.logging.appenders[1].type=gelf"
    overrides="$overrides -Ddw.logging.appenders[1].host=$GELF_HOST"
    overrides="$overrides -Ddw.logging.appenders[1].port=${GELF_PORT:-12201}"
    overrides="$overrides -Ddw.logging.appenders[1].useMarker=true"
    overrides="$overrides -Ddw.logging.appenders[1].includeFullMDC=true"
  else
    overrides="$overrides -Ddw.logging.appenders[1].currentLogFilename=./dropwizard.log"
    overrides="$overrides -Ddw.logging.appenders[1].threshold=WARN"
    overrides="$overrides -Ddw.logging.appenders[1].archivedLogFilenamePattern=./dropwizard.log.%d.gz"
  fi

  GRAPHITE_HOST=${GRAPHITE_HOST:-$GRAPHITE_PORT_2003_TCP_ADDR}
  GRAPHITE_PORT=${GRAPHITE_PORT:-$GRAPHITE_PORT_2003_TCP_PORT}
  if [ -n "$GRAPHITE_HOST" ]; then
    overrides="$overrides -Ddw.metrics.reporters[0].type=graphite"
    overrides="$overrides -Ddw.metrics.reporters[0].host=$GRAPHITE_HOST"
    overrides="$overrides -Ddw.metrics.reporters[0].port=${GRAPHITE_PORT:-2003}"
    overrides="$overrides -Ddw.metrics.reporters[0].prefix=timbuctoo"
  fi

  if [ -n "$ACTIVEMQ_BROKER_URL" ]; then
    overrides="$overrides -Ddw.activeMq.brokerUrl=$ACTIVEMQ_BROKER_URL"
  fi
  #using exec so that dropwizard is the one that receives the SIGINT on docker stop
  JAVA_OPTS="$overrides $JAVA_OPTS" exec ./appassembler/bin/timbuctoo "$1" "$2"
fi