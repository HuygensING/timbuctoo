#!/usr/bin/env bash

cd -P -- "$(dirname -- "$0")" #go to dir of script even if it was called as a symbolic link

if [ $1 = 'clean' ]; then
  git tag --delete $(git tag --list | grep '^fail')
else
  for sha in $(git log --reverse --pretty=format:'%H' --no-merges $1); do
    git branch -f testbranch $sha && ../verify/cleanverify.sh testbranch test || git tag fail-$sha $sha
  done
fi
