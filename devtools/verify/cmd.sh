#!/bin/sh

git clone -b ${local_ref} /root/timbuctoo /root/build &&  
cd /root/build/ &&
rm -r /root/build/src/spec/resources/specrunstate/database &&
mv /root/timbuctoo-prod-db /root/build/src/spec/resources/specrunstate/database &&
mvn ${maventarget}

mavenResult=$?

mkdir -p /root/timbuctoo/target &&
cp -R /root/build/target/concordion /root/timbuctoo/target/concordion &&
echo 'concordion result has been placed in the ./target folder'

exit $mavenResult
