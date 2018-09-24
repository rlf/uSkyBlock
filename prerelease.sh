#!/bin/bash
tag=$( git describe --tags --abbrev=0 )
lastversion="${tag%-*}"
tstamp=$( date +%Y%m%d%H%M )
newversion="$lastversion-$tstamp"
./changelog.sh $newversion $lastversion > changelog.md
git commit -a -m "Changelog for $tag"
git push
git tag $newversion
git push --tags
