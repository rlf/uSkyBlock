# bukkit-utils

This module holds general Bukkit Utilities enabling easier Bukkit Plugin creation.

## Utilities

* [FileUtil](src/main/java/dk/lockfuglsang/minecraft/file/README.md) - UTF-8, Locale and merging config files from jar
* [YmlConfiguration](src/main/java/dk/lockfuglsang/minecraft/yml/README.md) - Support for comments in yml-files
* [Commands](src/main/java/dk/lockfuglsang/minecraft/command/README.md) - Framework for easy command-creation

# License

This module is copyrighted by the authors, and licensed for re-use as Apache License 2.0.

# Usage

Put this in your `pom.xml`:

```
  <repositories>
    <repository>
        <id>uSkyBlock-mvn-repo</id>
        <url>https://raw.github.com/rlf/uSkyBlock/mvn-repo/</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
        <groupId>dk.lockfuglsang.minecraft</groupId>
        <artifactId>bukkit-utils</artifactId>
        <version>1.1</version>
    </dependency>
  </dependencies>
```

# Version History

## v1.1

* FileUtil, I18nUtil

## v1.0
Initial release, extracted from the source-code used in uSkyBlock

* YmlConfiguration, Commands