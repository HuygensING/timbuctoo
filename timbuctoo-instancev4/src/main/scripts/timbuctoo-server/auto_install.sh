#!/usr/bin/env bash

# This script checks if the the ci server contains an updated version of the development branch
# TODO make build configurable
TIMBUCTOO_INSTALLER_DIR="/tmp/timbuctoo"

# Create the directory to download to installer to if it does not exist
if [ ! -d "$TIMBUCTOO_INSTALLER_DIR" ]; then
  echo "Create directory for Timbuctoo installers \"$TIMBUCTOO_INSTALLER_DIR\""
  mkdir "$TIMBUCTOO_INSTALLER_DIR"
fi

# Retrieve the number of the latest successful build
echo "Retrieve the latest build number"
LAST_SUCCESSFUL_BUILD=$(curl "http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/buildNumber")
LAST_SUCCESSFUL_BUILD_DIR="$TIMBUCTOO_INSTALLER_DIR/$LAST_SUCCESSFUL_BUILD"

if [ ! -d "$LAST_SUCCESSFUL_BUILD_DIR" ]; then
  echo "Download and install Timbuctoo build $LAST_SUCCESSFUL_BUILD"
  # Download and install the latest development Timbuctoo
  mkdir "$LAST_SUCCESSFUL_BUILD_DIR"
  # Download new latest successful rpm
  wget -r --no-parent -nd  "http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/nl.knaw.huygens\$timbuctoo-instancev4/artifact/nl.knaw.huygens/timbuctoo-instancev4/" -P "$LAST_SUCCESSFUL_BUILD_DIR" -erobots=off -R "*index*" &> /dev/null
else
  echo "Latest build of Timbuctoo is already installed on this server."
fi



