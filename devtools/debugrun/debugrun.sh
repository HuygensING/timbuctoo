#!/bin/bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

cd ../../timbuctoo-instancev4

COMMAND="server"
YAML="./example_config.yaml"
DEBUG_PORT="5005"
DEBUG_HALT="n"
FLIGHT_CONTROL=""

OPTIND=1
while getopts ":d:pDf:c:y:hrtC" opt; do
    case "$opt" in
  C)
    CLEAN=true
    ;;
  c)
    COMMAND=$OPTARG
    ;;
  d)
    DEBUG_PORT=$OPTARG
    ;;
  D)
    NO_DEBUG="y"
    ;;
  f)
    FLIGHT_CONTROL="$OPTARG"
    ;;
  h)
    echo -e "$0: run the timbuctoo command with some easier to remember switches\n\n-d    specify debug port\n-D    do not expose a debugging port\n-p    pause until debugger attaches\n-f    make flight recording\n-c    command to run (defaults to server)\n-y    YAML config to load (defaults to ./example_config.yaml)" >&2
    exit 0
    ;;
  p)
    DEBUG_HALT="y"
    ;;
  r)
    REBUILD=1
    ;;
  t)
    RUN_TESTS=1
    ;;
  y)
    YAML=$OPTARG
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

if [ "$REBUILD" = 1 ]; then
  cd ..
  echo "Rebuilding"
  if [ -z "$RUN_TESTS" ]; then
    skipTests="-DskipTests"
  fi
  mvn package $skipTests -q
  if [ $? != 0 ]; then
    xmllint --xpath //file[error] timbuctoo-instancev4/target/checkstyle-result.xml
    exit 1
  fi
  cd -
fi

JAVA_OPTS="$JAVA_OPTS -Ddw.databasePath=./temp_for_debugrun/database \
-Ddw.authorizationsPath=./temp_for_debugrun/authorizations \
-Ddw.loginsFilePath=./temp_for_debugrun/logins.json \
-Ddw.usersFilePath=./temp_for_debugrun/users.json"

if [ "$CLEAN" = "true" ]; then
  echo "Removing database and auth dirs"
  [ -d ./temp_for_debugrun/authorizations ] && rm -r ./temp_for_debugrun/authorizations
  [ -d ./temp_for_debugrun/database ] && rm -r ./temp_for_debugrun/database
  [ -e ./temp_for_debugrun/logins.json ] && rm ./temp_for_debugrun/logins.json
  [ -e ./temp_for_debugrun/users.json ] && rm ./temp_for_debugrun/users.json
fi

[ -d ./temp_for_debugrun/authorizations ] || mkdir -p ./temp_for_debugrun/authorizations
[ -d ./temp_for_debugrun/database ] || mkdir -p ./temp_for_debugrun/database

[ -e ./temp_for_debugrun/logins.json ] || echo "[]" > ./temp_for_debugrun/logins.json
[ -e ./temp_for_debugrun/users.json ] || echo "[]" > ./temp_for_debugrun/users.json



if [ -z "$NO_DEBUG" ]; then
	JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_HALT},address=${DEBUG_PORT}"
fi
if [ -n "$FLIGHT_CONTROL" ]; then
	JAVA_OPTS="$JAVA_OPTS -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=filename=\"$FLIGHT_CONTROL\",dumponexit=true"
fi

export JAVA_OPTS
CMD="./target/appassembler/bin/timbuctoo $COMMAND $YAML"

echo "Changed directory to: $PWD"
echo "JAVA_OPTS=\"$JAVA_OPTS\""
echo running "$CMD"
echo ""
$CMD

result="$?"
if [ "$result" = 130 ]; then
	exit 0 #ctrl-c is also a successful shutdown
else
	exit $result
fi
