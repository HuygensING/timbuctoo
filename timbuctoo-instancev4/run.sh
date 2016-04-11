#!/bin/bash

cd $(dirname $0)

COMMAND="server"
YAML="./example_config.yaml"
DEBUG_PORT=""
DEBUG_HALT="n"
FLIGHT_CONTROL=""

OPTIND=1
while getopts ":d:pf:c:y:h" opt; do
    case "$opt" in
  d)
    DEBUG_PORT=$OPTARG
    ;;
  p)
    DEBUG_HALT="y"
    ;;
  f)
    FLIGHT_CONTROL="$OPTARG"
    ;;
  c)
    COMMAND=$OPTARG
    ;;
  y)
    YAML=$OPTARG
    ;;
  h)
    echo -e "$0: run the timbuctoo command with some easier to remember switches\n\n-d    allow debugger to attach\n-p    pause until debugger attaches\n-f    make flight recording\n-c    command to run (defaults to server)\n-y    YAML config to load (defaults to ./example_config.yaml)" >&2
    exit 0
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
if [ -n "$DEBUG_PORT" ]; then
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
