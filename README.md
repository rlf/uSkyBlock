# uSkyBlock

This is a continually updated and custom version of Talabrek's Ultimate SkyBlock plugin.

# Installation

This version depends on the following plugins:

* Bukkit/Spigot 1.8-R0.1-SNAPSHOT (I.e. the latest Spigot 1.8 release)
* Vault 1.5.x
* WorldEdit 6.0.0-SNAPSHOT
* WorldGuard 6.0.0-SNAPSHOT

## Releases 
[![Build Status](https://api.travis-ci.org/rlf/uSkyBlock.svg)](https://travis-ci.org/rlf/uSkyBlock) [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/rlf/uSkyBlock.svg)](http://isitmaintained.com/project/rlf/uSkyBlock "Average time to resolve an issue") [![Percentage of issues still open](http://isitmaintained.com/badge/open/rlf/uSkyBlock.svg)](http://isitmaintained.com/project/rlf/uSkyBlock "Percentage of issues still open")

https://github.com/rlf/uSkyBlock/releases

Pre-releases will end in a date-stamp, and is considered **unsafe** for production servers.

Releases have a clean version number, has been tested, and should be safe for production servers.

### Compatibilty Releases for Bukkit 1.7.9/10
We have decided to also include `compat` releases, which are compiled to work under Bukkit 1.7.10.
They have not been tested as thouroughly as the 1.8 (`master`) releases - so they are 100% "use at your own risk".

## Config-files

*Note*: Config files might change quite a bit, so before installing the plugin, please move existing config files aside, and manually merge them after the plugin has "installed" it's own versions.

We are working on a feature, that will do this automatically - but until that is released, please follow the above procedure.

# Building/Compiling

Requirements:

* Maven
* Java SDK 1.7 or above
* A local revision of the Spigot build (see http://www.spigotmc.org/threads/bukkit-craftbukkit-spigot-1-8.36598/)

## Configure Environment and Run Maven
Note: If you are using the same environment, as the one you used to build Spigot, everything should simply compile,
since all dependencies are already in your local maven repository.

1. Run `mvn install` in the uSkyBlock folder
2. `target/uSkyBlock.jar` - plugin, ready for installation in Spigot/Bukkit
3. `target/uSkyBlock-api.jar` - jar-file of the API only, only requirement for integrating against USB.


# API
uSkyBlock has an API (since v2.0.1-RC1.65).

To use it, simply drop the api-jar to the classpath of your own plugin, and write some code along these lines:
```java
Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
  uSkyBlockAPI usb = (uSkyBlockAPI) plugin;
  player.sendMessage(String.format(
    "\u00a79Your island score is \u00a74%5.2f!", 
    usb.getIslandLevel(player)
  ));
}
```
For further details regarding the API, visit the Wiki page: https://github.com/rlf/uSkyBlock/wiki/API

## Contributing

Fork-away, and create pull-requests - we review and accept almost any changes.

## License

TL;DR - This is licensed under GPLv3

### Explanation / History
Originally the uSkyBlock was a continuation of the skySMP plugin, which was licensed under GPLv3
(see http://dev.bukkit.org/bukkit-plugins/skysmp/).

Talabrek intended to share the code with the public, but simply didn't have the time available to do so.

Unfortunately, he had registered the plugin as `All rights reserved` on Bukkit, meaning the bukkit staff put the plugin under moderation - further increasing the work-load required to share the plugin.

Those trying to get hold on Talabrek, had a hard time, and eventually multiple developers got their hands on different versions of the uSkyBlock plugin, and tried to continue the work in various channels (wesley27 and wolfwork comes to mind).

On the very last day of 2014, we received the following e-mail from Talabrek:

> Recently, now that a stable 1.8 and the future of spigot is looking hopeful, I have gotten back to work on the plugin. There is much to be done though, and I just don't have the time to do it, so I finally decided to make it available for the public to work on. This is when I noticed the work you and others have done on the plugin.
>
> I don't have the time and energy to devote to actively developing this plugin anymore, but it is like a pet project to me so I would still like to have a role in it's development. You are making the best effort that I have seen, so I would like for you to continue.
>
> If you are interested, I can make my current code available to you (it's much different than what you currently have now, but some parts might be useful).
>
> -Talabrek

So, with Talabreks blessing, this repository will try to consolidate the many different "branches" out there.

## References

* [GPLv3](http://www.gnu.org/copyleft/gpl.html) - [tl;dr Legal](https://www.tldrlegal.com/l/gpl-3.0)
* Dutchy1001s guides and tutorials on uSkyBlock - [http://debocraft.x10.mx/skyblock/](http://debocraft.x10.mx/skyblock/)
