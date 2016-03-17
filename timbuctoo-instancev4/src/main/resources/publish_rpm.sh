#!/usr/bin/env bash
BRANCH="test"
GIT_DIR=$(git rev-parse --git-dir)
TEST_BRANCH_HEAD="$GIT_DIR/refs/heads/$BRANCH"
TEST_BRANCH_REV=$(git rev-parse test)
DEVELOP_BRANCH_REV=$(git rev-parse develop)

# build the latest version
mvn clean package -DbuildRpm -Dpath-to-config=timbuctoo-instancev4/example-config.yaml
# add rpm to git
git add -f timbuctoo-instancev4/target/rpm/timbuctoo-instancev4/RPMS/x86_x64/timbuctoo-instancev4*.rpm
# move to the test-branch
# TODO make branch configurable
git checkout -b "$BRANCH"
# commit the rpm and add it to the branch test
TREE=$(git write-tree)
RELEASE_MOMENT=$(date +"%Y%m%d%H%M")
COMMIT=$(echo "test release $RELEASE_MOMENT" | git commit-tree "$TREE" -p "$TEST_BRANCH_REV" -p "$DEVELOP_BRANCH_REV")
echo "$COMMIT" > "$TEST_BRANCH_HEAD"
git push
