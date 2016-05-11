#!/bin/sh


export TIMBUCTOO_JENKINS_PROJECT="http://ci.huygens.knaw.nl/job/timbuctoo_develop"

/data/timbuctoo/timbuctoo_update/auto_install.sh >> /data/timbuctoo/update_timbuctoo.log 2>&1
