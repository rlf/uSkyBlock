# uSkyBlock

This is a continually updated and custom version of Talabrek's Ultimate SkyBlock plugin, which he has decided to stop supporting.

# Installation

This version depends on the following plugins:

* Bukkit 1.8-R0.1-SNAPSHOT (I.e. the latest Spigot 1.8 release)
* Vault 1.4
* WorldEdit 6.0.0-SNAPSHOT
* WorldGuard 6.0.0-SNAPSHOT

## Releases ![build status](https://travis-ci.org/rlf/uSkyBlock.svg?branch=v2.0.0)

There is a lot of pre-releases (the ones named RX.Y), these are test-releases, and are released here, until we get a working build-server to provide developer-snapshots.

Only promoted releases are considered "safe". Any of the pre-releases will contain various bugs and regressions.

## Config-files

*Note*: The R0.X release candidates all change heavily in most of the config files, so place close attention to the changes between your local copy, and the "default" ones.

# Building/Compiling

Requirements:

* Maven
* Java SDK 1.7 or above
* A local revision of the Spigot build (see http://www.spigotmc.org/threads/bukkit-craftbukkit-spigot-1-8.36598/)

Configure Environment and Run Maven

1. Define the SPIGOT_BASE environment variable to the folder where the BuildTools.jar reside
2. Make sure that the Bukkit folder in that hiearchy contains a /target/bukkit*.jar file
3. Run `mvn install` in the uSkyBlock folder

## Issues

Issues are maintained on the GitHub page: https://github.com/rlf/uSkyBlock/issues

## Contributing

Fork-away, and create pull-requests

## License

TL;DR - This is licensed under GPLv3

### Explanation / History
Originally the uSkyBlock was a continuation of the skySMP plugin, which was licensed under GPLv3
(see http://dev.bukkit.org/bukkit-plugins/skysmp/).

Unfortunately, Talabrek didn't seem to be aware of what software-licensing means, and claimed that his uSkyBlock was his private property.

When he abandoned the project - and the community called for a disclosure of the source-code, Dev-Bukkit decided to pull his plugin from the repository, and Talabrek countered by simply not sharing neither source-code nor binary releases.

Even though this is in violation of the GPLv3 license - no-one really cares.

So even though Talabrek deserves all the credit in the world - for making such an awesome plugin, this continuation is going to continue without him.

## References

* [GPLv3](http://www.gnu.org/copyleft/gpl.html) - [tl;dr Legal](https://www.tldrlegal.com/l/gpl-3.0)
* Dutchy1001s guides and tutorials on uSkyBlock - [http://debocraft.x10.mx/skyblock/](http://debocraft.x10.mx/skyblock/)
