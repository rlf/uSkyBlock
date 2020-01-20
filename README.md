# uSkyBlock

This is a continually updated and custom version of Talabrek's Ultimate SkyBlock plugin.

We are on [Spigot](https://www.spigotmc.org/resources/uskyblock-revived.66795/). Currently [Open Issues](https://github.com/rlf/uSkyBlock/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aopen%20-label%3A%22T%20ready%20for%20test%22%20-label%3A%22T%20tested%20awaiting%20reporter%22)

# Installation

This version depends on the following plugins:

* Bukkit/Spigot 1.15.1-R0.1-SNAPSHOT
* Vault 1.7.x
* WorldEdit 7.1.0-SNAPSHOT
* WorldGuard 7.0.2-SNAPSHOT

## Releases 
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/rlf/uSkyBlock.svg)](http://isitmaintained.com/project/rlf/uSkyBlock "Average time to resolve an issue") [![Percentage of issues still open](http://isitmaintained.com/badge/open/rlf/uSkyBlock.svg)](http://isitmaintained.com/project/rlf/uSkyBlock "Percentage of issues still open")

https://www.spigotmc.org/resources/uskyblock-revived.66795/history

Pre-releases will end in -SNAPSHOT, and is considered **unsafe** for production servers.

Releases have a clean version number, has been tested, and should be safe for production servers.

### Bukkit/Spigot 1.7.9/10 Releases

We provide pre-compiled versions (no support) [here](http://rlf.github.io/uSkyBlock):

* [2.4.9 for Bukkit 1.7.10](http://rlf.github.io/uSkyBlock/releases/bukkit-1.7.10/uSkyBlock-2.4.9.jar)

## Config-files

*Note*: Config files might change quite a bit, and upon activation, the plugin will try to merge the existing ones with the new ones. A backup is kept under the `uSkyBlock/backup` folder.

Please make sure, that the config files are as you expect them to be, before using the plugin or releasing it to "the public".

## Building/Compiling

See (https://github.com/rlf/uSkyBlock/wiki/Building)

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
For further details regarding the API, visit the Wiki page: https://github.com/rlf/uSkyBlock/wiki/uSkyBlock-API

## Contributing

Fork-away, and create pull-requests - we review and accept almost any changes.

But *please* conform with the (https://github.com/rlf/uSkyBlock/wiki/Coding-Guidelines)

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

