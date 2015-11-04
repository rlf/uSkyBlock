# YmlConfiguration

These classes enables plugins to preserve comments when handling `FileConfiguration` objects in Bukkit.

## Usage

```
  FileConfiguration config = new YmlConfiguration();
  config.load(file);
  // Do FileConfiguration stuff

  config.save(file);
```