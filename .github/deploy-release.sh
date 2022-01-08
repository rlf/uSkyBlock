#!/usr/bin/env bash

echo -e "Running release script...\n"
echo -e "Publishing javadocs and artifacts...\n"
cd $HOME

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/po-utils/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-API/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-APIv2/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-AWE370/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-Core/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-FAWE/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-Plugin/target/mvn-repo/ \
travis@travis.internetpolice.eu:WWW-USB/maven/uskyblock/

echo -e "Publishing javadocs...\n"

rsync -r --delete --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/po-utils/target/site/apidocs/ \
travis@travis.internetpolice.eu:WWW-USB/javadocs/release/po-utils/

rsync -r --delete --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-API/target/site/apidocs/ \
travis@travis.internetpolice.eu:WWW-USB/javadocs/release/uSkyBlock-API/

rsync -r --delete --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-APIv2/target/site/apidocs/ \
travis@travis.internetpolice.eu:WWW-USB/javadocs/release/uSkyBlock-APIv2/

rsync -r --delete --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-Core/target/site/apidocs/ \
travis@travis.internetpolice.eu:WWW-USB/javadocs/release/uSkyBlock-Core/

echo -e "Publishing final plugin release...\n"

rsync -r --quiet -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-Plugin/target/uSkyBlock-*.jar \
travis@travis.internetpolice.eu:WWW-USB/downloads/release/uSkyBlock/

rsync -r --quiet --no-R --no-implied-dirs -e "ssh -p 2222 -o StrictHostKeyChecking=no" \
$HOME/work/uSkyBlock/uSkyBlock/uSkyBlock-Plugin/target/classes/version.json \
travis@travis.internetpolice.eu:WWW-USB/versions/release.json
