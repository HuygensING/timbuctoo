#!/usr/bin/env bash

echo "Remove user \"datarepository\""
userdel datarepository

echo "Remove group \"datarepository\""
groupdel datarepository
