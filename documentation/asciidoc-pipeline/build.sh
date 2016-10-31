#!/bin/sh

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

cd .. #go to the 
echo generating documentation XML >&2
asciidoctor \
  -a stylesheet=readthedocs.css \
  -r asciidoctor-diagram \
  -r ./asciidoc-pipeline/macros/featureMacro.rb \
  -D ./asciidoc-pipeline/output \
  index.adoc
#  -b docbook5 \

cp readthedocs.css concordion.css api.html ./asciidoc-pipeline/output 
#cd ./asciidoc-pipeline/output

#echo converting XML to html  >&2
#xsltproc --xinclude ../docbook-html5/docbook-html5.xsl index.xml > index.html

#makes it easier to have chrome open the original xhtml source without applying html fixes 
#(such as transforming the self closing <span /> into a "<span>" opening tag without a closing tag)
#cp index.html index-as-xml-tree.xml
