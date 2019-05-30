#!/usr/bin/env bash

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "master" ]]; then

  echo -e "Publishing artifacts...\n"
  cd $HOME

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-API/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/master/uSkyBlock-API/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Core/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/master/uSkyBlock-Core/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-FAWE/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/master/uSkyBlock-FAWE/

  echo -e "Publishing javadocs...\n"

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-API/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/master/uSkyBlock-API/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-Core/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/master/uSkyBlock-Core/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-FAWE/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/master/uSkyBlock-FAWE/

  echo -e "Publishing final plugin release...\n"

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock-Plugin/target/uSkyBlock-*.jar \
  dool1@shell.xs4all.nl:WWW/downloads/master/uSkyBlock/

fi

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "release" ]]; then

  echo -e "Publishing javadocs and artifacts...\n"
  cd $HOME

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-API/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/release/uSkyBlock-API/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Core/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/release/uSkyBlock-Core/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-FAWE/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/release/uSkyBlock-FAWE/

  echo -e "Publishing javadocs...\n"

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-API/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-API/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-Core/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-Core/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock-FAWE/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-FAWE/

  echo -e "Publishing final plugin release...\n"

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock-Plugin/target/uSkyBlock-*.jar \
  dool1@shell.xs4all.nl:WWW/downloads/release/uSkyBlock/

fi
