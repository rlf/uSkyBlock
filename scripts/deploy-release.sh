#!/usr/bin/env bash

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then

  echo -e "Running release script...\n"
  echo -e "Publishing javadocs and artifacts...\n"
  cd $HOME

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/po-utils/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/uskyblock/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-API/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/uskyblock/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Core/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/uskyblock/

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-FAWE/target/mvn-repo/ \
  dool1@shell.xs4all.nl:WWW/maven/uskyblock/

  echo -e "Publishing javadocs...\n"

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock/po-utils/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/po-utils/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-API/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-API/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Core/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-Core/

  rsync -r --delete --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-FAWE/target/site/apidocs/ \
  dool1@shell.xs4all.nl:WWW/javadocs/release/uSkyBlock-FAWE/

  echo -e "Publishing final plugin release...\n"

  rsync -r --quiet $HOME/build/uskyblock/uSkyBlock/uSkyBlock-Plugin/target/uSkyBlock-*.jar \
  dool1@shell.xs4all.nl:WWW/downloads/release/uSkyBlock/

fi
