#!/bin/bash
tag=$( git describe --tags --abbrev=0 )
tstamp=$( date +%Y%m%d%H%M )
tag="${tag%-*}"
echo "# Change Log for ${tag}..${tag}-$tstamp" > changelog.txt
git log --oneline ${tag}..HEAD | grep "#" | awk '{printf("  * %s\n",$0)}' >> changelog.txt
echo "# Translations" >> changelog.txt
echo "```" >> changelog.txt
cat uSkyBlock-Plugin/target/gettext-report.txt >> changelog.txt
echo "```" >> changelog.txt
tag="$tag-$tstamp"
git commit -a -m "committing changes for $tag"
git push
git tag $tag
git push --tags
