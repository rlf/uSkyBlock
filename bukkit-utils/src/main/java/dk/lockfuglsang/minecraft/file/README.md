# File Utilities

The `FileUtil` class simplifies resource reading for Bukkit plugins.

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

The `config` in the above example is created the following way:
  * Read `config.yml` from the data-folder
    * if a `config_en.yml` exists - read it
    * else if a `config.yml` exist - read it
  * Read `config.yml` from the jar-file (classpath)
    * if a 'config_en.yml` exists - read it
    * else if a `config.yml` exsits - read it
  * Compare `version` read from both configs
    * if `config.yml` is listed in `allwaysOverwrite` - just use the jar-file
    * else if jar-version > file-version or file-version does not exist
      * merge nodes from jar into config - preserving node-comments
        * ignore nodes listed in `merge-ignore` from the existing config-file
        * move nodes listed in `move-nodes` from the jar-file
        * replace nodes listed in `replace-nodes` from the jar-file, if they have default values
        * set `version` to the jar-file version
