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

if [ ${maventarget} = verify ]; then
  if [ -e ../timbuctoo-db.zip ]; then
     verifyparam="-v"
     verifydb="$PWD/../timbuctoo-db.zip"
     verifytarget=":/root/timbuctoo-db.zip"
   else
     echo "The verify will fail because you have no prod database available in ../timbuctoo-db.zip"
     exit 1
  fi
fi
if git rev-parse --verify "$1" 2> /dev/null; then
  local_ref=`git rev-parse --abbrev-ref "$1"`
  docker run \
    --rm \
    -it \
    ${mavencache} \
    -v "$(git rev-parse --show-toplevel)":/root/timbuctoo \
    $verifyparam "$verifydb"$verifytarget \
    maven:3.3-jdk-8 \
    bash -c "git clone -b ${local_ref} /root/timbuctoo /root/build && \
    cd /root/build/timbuctoo-instancev4/src/spec/resources && \
    [ -e /root/timbuctoo-db.zip ] && unzip /root/timbuctoo-db.zip; \
    cd /root/build/ && mvn ${maventarget}; \
    cp -R /root/build/timbuctoo-instancev4/target/concordion /root/timbuctoo/timbuctoo-instancev4/target/concordion" \ || exit 1
else
  echo "you should specify a branchname";
  exit 2
fi

