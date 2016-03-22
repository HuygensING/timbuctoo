#!/usr/bin/env bash

# This script checks if the the ci server contains an updated version of the development branch
# TODO make build configurable
if [ ! $1 ]; then
  echo "Specify the url where the Timbuctoo binaries can be downloaded"
  exit 1
fi
if [ ! $2 ]; then
  echo "Specify the url where the Timbuctoo buildnumber can be retrieved"
  exit 1
fi

TIMBUCTOO_BIN_URL="$1"
TIMBUCTOO_BUILD_NO_URL="$2"
TIMBUCTOO_INSTALLER_DIR="/tmp/timbuctoo"
VALID_STATUS=0
INVALID_STATUS=1

check_timbuctoo_status(){
  TIMBUCTOO_STATUS=$(monit status timbuctoo | grep "  status")
  echo "Timboctoo status \"$TIMBUCTOO_STATUS\""
  if [[ $TIMBUCTOO_STATUS =~ "Initializing" ]]; then
    sleep 5s
    check_timbuctoo_config
    return $?
  elif [[ $TIMBUCTOO_STATUS =~ "failed" ]]; then
    return $INVALID_STATUS
  else
    return $VALID_STATUS
  fi
}

check_timbuctoo_config(){
  check_timbuctoo_status
  STATUS=$?
  if [[ $STATUS == $VALID_STATUS ]]; then
    $(monit restart timbuctoo)
    check_timbuctoo_status
    return $?
  else
    return $STATUS
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

# Create the directory to download to installer to if it does not exist
if [ ! -d "$TIMBUCTOO_INSTALLER_DIR" ]; then
  echo "Create directory for Timbuctoo installers \"$TIMBUCTOO_INSTALLER_DIR\""
  mkdir "$TIMBUCTOO_INSTALLER_DIR"
fi

# Retrieve the number of the latest successful build
echo "Retrieve the latest build number"
LAST_SUCCESSFUL_BUILD=$(curl "$TIMBUCTOO_BUILD_NO_URL")
LAST_SUCCESSFUL_BUILD_DIR="$TIMBUCTOO_INSTALLER_DIR/$LAST_SUCCESSFUL_BUILD"

if [ ! -d "$LAST_SUCCESSFUL_BUILD_DIR" ]; then
  check_timbuctoo_config
  STATUS=$?

  if [[ $STATUS == $VALID_STATUS ]]; then
    install_new_version

    check_timbuctoo_status
    STATUS_AFTER_INSTALL=$?

    if [[ $STATUS_AFTER_INSTALL == $VALID_STATUS ]]; then
      echo "Installation successful"
      clean_up $LAST_SUCCESSFUL_BUILD
      exit 0
    else
      echo "Installation failed"
      rollback $LAST_SUCCESSFUL_BUILD
      exit 1
    fi
  else
    echo "Timbuctoo config is invalid"
    exit 1
  fi
else
  echo "Latest build of Timbuctoo is already installed on this server."
fi

