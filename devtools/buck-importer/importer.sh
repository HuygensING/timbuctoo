#!/usr/bin/env bash

cd "$(buck root)"
buck build //timbuctoo-instancev4/src/main/java/nl/knaw/huygens/timbuctoo/buck:importer
mvn dependency:list -DexcludeTransitive=true -DoutputFile=dependencies

find . -name dependencies |
  xargs java -jar buck-out/gen/timbuctoo-instancev4/src/main/java/nl/knaw/huygens/timbuctoo/buck/importer.jar \
    --RuleOverrides ./buckbuild/overrides \
    --ignores "./buckbuild/ignorefile" \
    --root "$PWD" \
    --targetlocation "//buckbuild/jars" \
    --mavenrepo "http://repo.maven.apache.org/maven2/" \
    --mavenrepo "https://raw.githubusercontent.com/mbknor/mbknor.github.com/master/m2repo/releases" \
    --mavenrepo "http://maven.huygens.knaw.nl/repository/" \
    --mavenrepo "https://jitpack.io"
