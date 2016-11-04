#!/usr/bin/env bash
mavencache="-v $HOME/.m2:/root/.m2"

OPTIND=1
while getopts "ch" opt; do
  case "$opt" in
  c)
    mavencache=""
    ;;
  h)
    echo -e "Usage: $0 BRANCH [test|verify] [-c]\nRun the tests inside a clean docker container\n\n  -c   don't mount parent .m2 folder so that all dependencies are downloaded fresh\n"
    exit 0
    ;;
  \?)
    echo "Invalid option: -$OPTARG use -h for an overview of valid options" >&2
    exit 1
    ;;
  esac
done

maventarget=${2:-verify}

if git rev-parse --verify "$1" 2> /dev/null; then
  local_ref=`git rev-parse --abbrev-ref "$1"`
  docker run \
    --rm \
    -it \
    ${mavencache} \
    -v "$(git rev-parse --show-toplevel)":/root/timbuctoo \
    -e local_ref="$local_ref" \
    -e maventarget="$maventarget" \
    timbuctoo:verifysrc || exit 1
else
  echo "you should specify a branchname";
  exit 2
fi

