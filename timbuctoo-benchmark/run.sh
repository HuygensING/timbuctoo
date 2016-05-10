#!/bin/sh

java -jar target/benchmarks.jar -wf 1 -wi 5 -f 1 -i 5 -prof stack
