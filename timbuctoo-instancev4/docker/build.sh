#!/usr/bin/env bash
mavencache="-v $HOME/.m2:/root/.m2"

OPTIND=1
while getopts "c" opt; do
  case "$opt" in
  c)
    mavencache=""
    ;;
  \?)
    echo "Invalid option: -$OPTARG use -h for an overview of valid options" >&2
    exit 1
    ;;
  esac
done

maventarget=${2:-test}

if git rev-parse --verify "$1" 2> /dev/null; then
  local_ref="$1"
  docker run \
    --rm \
    -it \
    ${mavencache} \
    -v "$(git rev-parse --show-toplevel)":/root/timbuctoo \
    maven:3.3-jdk-8 \
    bash -c "git clone -b ${local_ref} /root/timbuctoo /root/build && cd /root/build && mvn ${maventarget}" \
  || exit 1
else
  echo "you should specify a branchname";
  exit 2
fi

