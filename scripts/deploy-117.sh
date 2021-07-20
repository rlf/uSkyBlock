#!/usr/bin/env bash

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then

    echo -e "Running 1.17 test release script...\n"
    cd $HOME

    echo -e "Publishing final plugin release...\n"

    rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
    $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Plugin/target/uSkyBlock-*.jar \
    travis@travis.internetpolice.eu:WWW-USB/downloads/testbuilds/1.17/

fi
