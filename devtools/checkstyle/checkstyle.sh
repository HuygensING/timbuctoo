#!/usr/bin/env bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

cd ../../src

find "$PWD/main" "$PWD/test" -name *.java | xargs bazel run //checkstyle -- -c "$PWD/main/resources/checkstyle_config.xml"
