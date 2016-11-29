#!/bin/sh

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

cd ../../documentation
echo generating documentation as HTML >&2
asciidoctor \
  -a stylesheet=readthedocs.css \
  -r asciidoctor-diagram \
  -r ./asciidoc-pipeline/macros/featureMacro.rb \
  -D ./asciidoc-pipeline/output \
  index.adoc

mkdir -p ./output

cp readthedocs.css concordion.css api.html ./output
