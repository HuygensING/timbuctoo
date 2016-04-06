#!/usr/bin/env bash

# This script checks if the the ci server contains an updated version of the development branch
# it is specific to our build servers
TIMBUCTOO_BIN_URL='http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/nl.knaw.huygens$timbuctoo-instancev4/artifact/nl.knaw.huygens/timbuctoo-instancev4/'
TIMBUCTOO_BUILD_NO_URL='http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/buildNumber/'
TIMBUCTOO_INSTALLER_DIR="/data/timbuctoo/tmp"
VALID_STATUS=0
INVALID_STATUS=1

mkdir -p $TIMBUCTOO_INSTALLER_DIR

normalize_status() {
  status="$1"
  if [[ "$status" =~ pending ]]; then
    echo "busy"
  elif [[ "$status" =~ Initializing ]]; then
    echo "busy"
  elif [[ "$status" =~ Running ]]; then
    echo "up"
  else
    echo "down"
  fi
}

monit_status() {
  monit_log=$(monit summary "$1")
  status=$(echo "$monit_log" | grep '^Process' | sed "s/^Process '[^']*' *//")
  status_length=$(echo "$status" | wc -l)
  if [ "$status_length" -gt 1 ]; then
    exit 1
  fi
  normalize_status "$status"
}

wait_for_monit() {
  target="$1"
  sleep_time="${2:-5}"
  max_sleeps="${3:-60}"
  sleeps=0

  status=$(monit_status "$1" || echo 'FAILURE')

  while [ "$status" = "busy" ]; do
    if [ "$sleeps" -gt "$max_sleeps" ]; then
      echo "timout for launching exceeded"
      exit 1
    fi
    sleep "$sleep_time"
    status=$(monit_status "$1")
  done
  if [ "$status" = "FAILURE" ]; then
    exit 1
  else
    echo "$1 is currently $status"
  fi
}

is_restartable(){
  wait_for_monit timbuctoo
  monit restart timbuctoo
  status=$(wait_for_monit timbuctoo)
  if [[ status = 'up' ]]; then
    return 0
  else
    return 1
  fi
}

install_new_version(){
  echo "Download and install Timbuctoo build $LAST_SUCCESSFUL_BUILD"
  # Download and install the latest development Timbuctoo
  mkdir "$LAST_SUCCESSFUL_BUILD_DIR"
  # Download new latest successful rpm
  wget -r --no-parent -nd  "$TIMBUCTOO_BIN_URL" -P "$LAST_SUCCESSFUL_BUILD_DIR" -erobots=off -R "*index*" &> /dev/null
  monit stop timbuctoo
  rpm -U "$LAST_SUCCESSFUL_BUILD_DIR/*.rpm"
  monit start timbuctoo
  STATUS_AFTER_INSTALL=$(wait_for_monit timbuctoo)
  if [[ status = 'up' ]]; then
    return 0
  else
    return 1
  fi
}

clean_up(){
  # clean up all the old versions
  CURRENT_VERSION=$1 
  VERSIONS=$(ls "$TIMBUCTOO_INSTALLER_DIR")
  for VERSION in $VERSIONS 
  do
    if [ "$VERSION" != "$CURRENT_VERSION" ]; then
      echo "Remove $VERSION"
      rm -rf "$TIMBUCTOO_INSTALLER_DIR/$VERSION"
    fi
  done
}

rollback(){
  FAILED_INSTALLATION_BUILD=$1
  VERSIONS=$(ls "$TIMBUCTOO_INSTALLER_DIR")
  NUMBER_OF_VERSIONS=${#VERSION[@]}

  if [ "$NUMBER_OF_VERSIONS" > 1]; then
    PREV_VERSION=0
    for VERSION in $VERSIONS
    do
      if [ [ $VERSION > $PREV_VERSION ] && [ $VERSION != $FAILED_INSTALLATION_BUILD ] ];then
        $PREV_VERSION = $VERSION
      fi
    done
    if [ $PREV_VERSION > 0 ]; then
      echo "roll back to version $PREV_VERSION"
      rpm -U --oldpackage "$TIMBUCTOO_INSTALLER_DIR/$PREV_VERSION/*.rpm"
      echo "Remove package with version $FAILED_INSTALLATION_BUILD"
      rm -rf "$TIMBUCTOO_INSTALLER_DIR/$FAILED_INSTALLATION_BUILD"
    fi
  else
    echo "Do not rollback first installation."
  fi
}

# Retrieve the number of the latest successful build
LAST_SUCCESSFUL_BUILD=$(curl "$TIMBUCTOO_BUILD_NO_URL")
LAST_SUCCESSFUL_BUILD_DIR="$TIMBUCTOO_INSTALLER_DIR/$LAST_SUCCESSFUL_BUILD"

if [ -d "$LAST_SUCCESSFUL_BUILD_DIR" ]; then
  echo "build $LAST_SUCCESSFUL_BUILD is already installed on this server."
else
  if is_restartable; then
    if install_new_version; then
      echo "Installation successful"
      clean_up $LAST_SUCCESSFUL_BUILD
      exit 0
    else
      echo "Installation failed"
      rollback
      exit 1
    fi
  else
    echo "Current timbuctoo can't be relaunched"
    exit 1
  fi
fi

