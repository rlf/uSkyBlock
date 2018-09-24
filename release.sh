#!/bin/bash
if [[ -z $1 ]]; then
  echo "Usage: $0 <newversion>"
  exit -1
fi
newversion=$1
tag=$( git describe --tags --abbrev=0 )
tstamp=$( date +%Y%m%d%H%M )
lastversion="${tag%-*}"
./changelog.sh $newversion $lastversion > changelog.md
git commit -a -m "Changelog for $tag"
git push
git tag $newversion
git push --tags
