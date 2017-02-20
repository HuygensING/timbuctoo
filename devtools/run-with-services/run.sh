#!/usr/bin/env bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

docker-compose pull && \
docker-compose up -d && \
docker-compose logs -f

echo "Services are still running in the background"
