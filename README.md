# uSkyBlock

This is a continually updated and custom version of Talabrek's Ultimate SkyBlock plugin, which he has decided to stop supporting.

# Installation

This version depends on the following plugins:

* Bukkit 1.8-R0.1-SNAPSHOT (I.e. the latest Spigot 1.8 release)
* Vault 1.4
* WorldEdit 6.0.0-SNAPSHOT
* WorldGuard 6.0.0-SNAPSHOT

## Releases [![Build Status](https://api.travis-ci.org/rlf/uSkyBlock.svg)](https://travis-ci.org/rlf/uSkyBlock)

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

Talabrek intended to share the code with the public, but simply didn't have the time available to do so.

Unfortunately, he had registered the plugin as `All rights reserved` on Bukkit, meaning the bukkit staff put the plugin under moderation - further increasing the work-load required to share the plugin.

Those trying to get hold on Talabrek, had a hard time, and eventually multiple developers got hold on different versions of the uSkyBlock plugin, and tried to continue the work in various channels (wesley27 and wolfwork comes to mind).

After app. 1½ months of refactoring, I got the following e-mail from Talabrek:

> Recently, now that a stable 1.8 and the future of spigot is looking hopeful, I have gotten back to work on the plugin. There is much to be done though, and I just don't have the time to do it, so I finally decided to make it available for the public to work on. This is when I noticed the work you and others have done on the plugin.
>
> I don't have the time and energy to devote to actively developing this plugin anymore, but it is like a pet project to me so I would still like to have a role in it's development. You are making the best effort that I have seen, so I would like for you to continue.
>
> If you are interested, I can make my current code available to you (it's much different than what you currently have now, but some parts might be useful).
>
> -Talabrek

So this repository will try to consolidate the many different "branches" out there.

Talabrek is back, allthough not 100%

## References

* [GPLv3](http://www.gnu.org/copyleft/gpl.html) - [tl;dr Legal](https://www.tldrlegal.com/l/gpl-3.0)
* Dutchy1001s guides and tutorials on uSkyBlock - [http://debocraft.x10.mx/skyblock/](http://debocraft.x10.mx/skyblock/)
