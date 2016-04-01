#!/bin/sh

/data/timbuctoo/timbuctoo_update/auto_install.sh http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/nl.knaw.huygens\$timbuctoo-instancev4/artifact/nl.knaw.huygens/timbuctoo-instancev4/ http://ci.huygens.knaw.nl/job/timbuctoo_develop/lastSuccessfulBuild/buildNumber/ > /var/log/update_timbuctoo.log 2>&1
