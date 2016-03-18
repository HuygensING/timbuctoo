#!/usr/bin/env bash

# This script checks if the the ci server contains an updated version of the development branch
# TODO make build configurable
TIMBUCTOO_INSTALLER_DIR="/tmp/timbuctoo"
VALID_STATUS=0
INVALID_STATUS=1

# Create the directory to download to installer to if it does not exist
if [ ! -d "$TIMBUCTOO_INSTALLER_DIR" ]; then
  echo "Create directory for Timbuctoo installers \"$TIMBUCTOO_INSTALLER_DIR\""
  mkdir "$TIMBUCTOO_INSTALLER_DIR"
fi

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
  wget -r --no-parent -nd  "http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/nl.knaw.huygens\$timbuctoo-instancev4/artifact/nl.knaw.huygens/timbuctoo-instancev4/" -P "$LAST_SUCCESSFUL_BUILD_DIR" -erobots=off -R "*index*" &> /dev/null
  monit stop timbuctoo
  rpm -U "$LAST_SUCCESSFUL_BUILD_DIR/*.rpm"
  monit start timbuctoo
}

rollback(){
  echo "No roll back yet"
}

# Retrieve the number of the latest successful build
echo "Retrieve the latest build number"
LAST_SUCCESSFUL_BUILD=$(curl "http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/buildNumber")
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
      exit 0
    else
      rollback
      exit 1
    fi
  else
    echo "Timbuctoo config is invalid"
    exit 1
  fi
else
  echo "Latest build of Timbuctoo is already installed on this server."
fi

