#!/bin/sh

cd $(dirname $0)
if [ "$1" = '--test' ]; then
	export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
fi

echo running './target/appassembler/bin/timbuctoo server ./example_config.yaml'
./target/appassembler/bin/timbuctoo server ./example_config.yaml
result="$?"
if [ "$result" = 130 ]; then
	exit 0
else 
	exit $result
fi
