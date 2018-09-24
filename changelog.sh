#!/bin/bash
newversion=$1
lastversion=$2
if [[ -z $lastversion ]];then
  tag=$( git describe --tags --abbrev=0 )
  lastversion="${tag%-*}"
fi
if [[ -z $newversion ]];then
  newversion="HEAD"
fi
echo -e "## Change Log for ${lastversion}..${newversion}\n"
git log --oneline ${lastversion}..HEAD | grep "#" | awk '{printf("  * %s\n",$0)}'
echo -e "\n## Translations"
echo '```'
cat uSkyBlock-Core/target/classes/gettext-report.txt
echo '```'