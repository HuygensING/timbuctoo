#!/bin/sh

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

echo  generating XML >&2
asciidoctor \
  -b docbook5 \
  -r asciidoctor-diagram \
  -r ./macros/featureMacro.rb \
  -D ./output \
  README.adoc

cp api.html concordion.css output
cd output

echo converting XML to html  >&2
xsltproc --xinclude ../docbook-html5/docbook-html5.xsl README.xml > README.html
cp README.html READMEh.xml