#!/bin/bash
tag=$( git describe --tags --abbrev=0 )
tstamp=$( date +%Y%m%d%H%M )
tag="${tag%-*}"
./createchangelog.sh $tag $tstamp
tag="$tag-$tstamp"
git commit -a -m "committing changes for $tag"
git push
git tag $tag
git push --tags
