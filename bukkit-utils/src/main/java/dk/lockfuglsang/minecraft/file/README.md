# File Utilities

The `FileUtil´ class simplifies resource reading for Bukkit plugins.

## Features

The primary usage of the FileUtil, is to read yml-files from the plugin-datafolder.

The utility supports the following features:

  * Reading files in UTF-8, regardless of system-encoding.
  * Support merging yml files from classpath (jar) with plugin files.
  * Support for `version`ing of individual yml files.
  * Support for localization of configuration files.
  * Support for data-conversion on merges.
  * Support caching of configuration objects (only read from disk once).

## Usage

```java
  @Override
  void onEnable() {
    FileUtil.setDataFolder(getDataFolder()); // init plugin-data-folder
    FileUtil.setLocale(new Locale("en"));    // set locale used in locating translated resources

    YmlConfiguration config = FileUtil.getYmlConfiguration("config.yml");
    if (config.getBoolean("feature.enabled", true)) {
      // do feature stuff
    }
  }
```
