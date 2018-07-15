#!/bin/bash
tag=$( git describe --tags --abbrev=0 )
tstamp=$( date +%Y%m%d%H%M )
tag="${tag%-*}"
echo "Release notes for ${tag}..${tag}-$tstamp" > changelog.txt
git log --oneline ${tag}..HEAD | grep "#" >> changelog.txt
tag="$tag-$tstamp"
git commit -a -m "committing changes for $tag"
git push
git tag $tag
git push --tags
