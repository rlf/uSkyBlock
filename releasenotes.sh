#!/bin/bash
tag=$( git describe --tags --abbrev=0 )
tstamp=$( date +%Y%m%d%H%M )
tag="${tag%-*}"
echo "Release notes for ${tag}..HEAD"
git log --oneline ${tag}..HEAD | grep "#"
tag="$tag-$tstamp"
#git commit -a -m "committing changes for $tag"
#git push
#git tag $tag
#git push --tags
