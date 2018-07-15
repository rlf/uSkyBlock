#!/bin/bash
tag=$1
tstamp=$2
echo "# Change Log for ${tag}..${tag}-$tstamp" > changelog.txt
git log --oneline ${tag}..HEAD | grep "#" | awk '{printf("  * %s\n",$0)}' >> changelog.txt
echo "# Translations" >> changelog.txt
echo '```' >> changelog.txt
cat uSkyBlock-Core/target/classes/gettext-report.txt >> changelog.txt
echo '```' >> changelog.txt