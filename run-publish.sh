#!/bin/bash
set -ev
if [ "${TRAVIS_BRANCH}" = "master" ]; then
  gradle bintrayUpload -b scripts/build.gradle
fi
