package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;
import us.talabrek.ultimateskyblock.api.IslandLevel;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockScoreChangedEvent;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;
import us.talabrek.ultimateskyblock.async.AsyncBalancedExecutor;
import us.talabrek.ultimateskyblock.async.BalancedExecutor;
import us.talabrek.ultimateskyblock.async.SyncBalancedExecutor;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.challenge.ChallengesCommand;
import us.talabrek.ultimateskyblock.command.AdminCommand;
import us.talabrek.ultimateskyblock.command.IslandCommand;
import us.talabrek.ultimateskyblock.event.*;
import us.talabrek.ultimateskyblock.handler.MultiverseCoreHandler;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.imports.impl.USBImporterExecutor;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandLogic;
import us.talabrek.ultimateskyblock.island.IslandScore;
import us.talabrek.ultimateskyblock.island.LevelLogic;
import us.talabrek.ultimateskyblock.island.task.RecalculateRunnable;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.player.PlayerNotifier;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;
import us.talabrek.ultimateskyblock.util.PlayerUtil;
import us.talabrek.ultimateskyblock.uuid.FilePlayerDB;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangeListener;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.FileUtil.readConfig;

public class uSkyBlock extends JavaPlugin implements uSkyBlockAPI {
    private static final String[][] depends = new String[][]{
            new String[]{"Vault", "1.5"},
            new String[]{"WorldEdit", "6.0"},
            new String[]{"WorldGuard", "6.0"},
    };
    private static String missingRequirements = null;
    private final Map<String, FileConfiguration> configFiles = new ConcurrentHashMap<>();

    private SkyBlockMenu menu;
    private ChallengeLogic challengeLogic;
    private LevelLogic levelLogic;
    private IslandLogic islandLogic;
    private PlayerNotifier notifier;
    private USBImporterExecutor importer;
    private BalancedExecutor executor;
    private BalancedExecutor asyncExecutor;

    private static String pName = "";
    private FileConfiguration lastIslandConfig;
    private FileConfiguration orphans;
    private File orphanFile;
    private File lastIslandConfigFile;
    public static volatile World skyBlockWorld;
    private static uSkyBlock instance;
    private Location lastIsland;
    private Stack<Location> orphaned;
    private Stack<Location> tempOrphaned;
    private Stack<Location> reverseOrphaned;
    public File directoryPlayers;
    public File directoryIslands;
    public File[] schemFile;
    Map<UUID, Long> infoCooldown;
    Map<UUID, Long> restartCooldown;
    Map<UUID, Long> biomeCooldown;
    private final Map<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();
    private boolean purgeActive;

    private BukkitTask autoRecalculateTask;

    static {
        uSkyBlock.skyBlockWorld = null;
    }

    public uSkyBlock() {
        // TODO: 08/12/2014 - R4zorax: Most of these should be converted to local variables
        configFiles.clear();
        this.lastIslandConfig = null;
        this.orphans = null;
        this.orphanFile = null;
        this.lastIslandConfigFile = null;
        this.orphaned = new Stack<>();
        this.tempOrphaned = new Stack<>();
        this.reverseOrphaned = new Stack<>();
        this.infoCooldown = new HashMap<>();
        this.restartCooldown = new HashMap<>();
        this.biomeCooldown = new HashMap<>();
        this.purgeActive = false;
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
        try {
            this.unloadPlayerFiles();
            if (lastIsland != null) {
                setLastIsland(lastIsland);
            }
        } catch (Exception e) {
            log(Level.INFO, "Something went wrong saving the island and/or party data!", e);
        }
    }

    /**
     * System-encoding agnostic config-reader
     */
    public FileConfiguration getFileConfiguration(String configName) {
        // Caching, for your convenience! (and a bigger memory print!)
        if (!configFiles.containsKey(configName)) {
            FileConfiguration config = new YamlConfiguration();
            try {
                // read from datafolder!
                File configFile = new File(getDataFolder(), configName);
                File configFileJar = new File(getDataFolder(), configName + ".org");
                try (InputStream in = getClassLoader().getResourceAsStream(configName)) {
                    // copy from jar
                    Files.copy(in, Paths.get(configFileJar.toURI()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log(Level.WARNING, "Unable to create config file " + configFile, e);
                }
                // TODO: 09/12/2014 - R4zorax: Also replace + backup if jar-version is newer than local version
                FileConfiguration configFolder = new YamlConfiguration();
                FileConfiguration configJar = new YamlConfiguration();
                readConfig(configFolder, configFile);
                readConfig(configJar, configFileJar);
                if (!configFile.exists() || configFolder.getInt("version", 0) < configJar.getInt("version", 0)) {
                    if (configFile.exists()) {
                        log(Level.INFO, "Moving existing config " + configName + " to " + configName + ".bak");
                        Files.move(Paths.get(configFile.toURI()),
                                Paths.get(new File(getDataFolder(), configName + ".bak").toURI()),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.move(Paths.get(configFileJar.toURI()), Paths.get(configFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    config = configJar;
                } else if (configFile.exists()) {
                    // FORCE utf8 - don't rely on super.getConfig() or FileConfiguration.load()
                    readConfig(config, configFile);
                }
                configFileJar.delete();
            } catch (Exception e) {
                log(Level.SEVERE, "Unable to handle config-file " + configName, e);
            }
            configFiles.put(configName, config);
        }
        return configFiles.get(configName);
    }

    @Override
    public FileConfiguration getConfig() {
        return getFileConfiguration("config.yml");
    }

    public void onEnable() {
        missingRequirements = null;
        instance = this;
        executor = new SyncBalancedExecutor(Bukkit.getScheduler());
        asyncExecutor = new AsyncBalancedExecutor(Bukkit.getScheduler());
        configFiles.clear();
        activePlayers.clear();
        uSkyBlock.pName = "[" + getDescription().getName() + "] ";
        reloadConfigs();
        this.getCommand("island").setExecutor(new IslandCommand(this, menu));
        this.getCommand("challenges").setExecutor(new ChallengesCommand());
        this.getCommand("usb").setExecutor(new AdminCommand(instance));
        getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                    log(Level.INFO, "Using vault for permissions");
                    VaultHandler.setupPermissions();
                    try {
                        FileConfiguration config = getLastIslandConfig();
                        if (!config.contains("options.general.lastIslandX") && getConfig().contains("options.general.lastIslandX")) {
                            FileConfiguration.createPath(config.getConfigurationSection("options.general"), "lastIslandX");
                            FileConfiguration.createPath(config.getConfigurationSection("options.general"), "lastIslandZ");
                            config.set("options.general.lastIslandX", getConfig().getInt("options.general.lastIslandX"));
                            config.set("options.general.lastIslandZ", getConfig().getInt("options.general.lastIslandZ"));
                            saveLastIslandConfig();
                        }
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), (double) config.getInt("options.general.lastIslandX"), (double) Settings.island_height, (double) config.getInt("options.general.lastIslandZ")));
                    } catch (Exception e) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"), (double) Settings.island_height, (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ")));
                    }
                    if (uSkyBlock.this.lastIsland == null) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0));
                    }
                    setupOrphans();
                }
                WorldGuardHandler.setupGlobal(getSkyBlockWorld());
                getServer().getScheduler().runTaskLater(instance, new Runnable() {
                    @Override
                    public void run() {
                        for (Player player : getServer().getOnlinePlayers()) {
                            if (isSkyWorld(player.getWorld())) {
                                loadPlayerData(player);
                            }
                        }
                    }
                }, 100);
            }
        }, 150L);
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (Exception e) {
            log(Level.WARNING, "Failed to submit metrics data", e);
        }
    }

    public synchronized boolean isRequirementsMet(CommandSender sender) {
        if (missingRequirements == null) {
            PluginManager pluginManager = getServer().getPluginManager();
            missingRequirements = "";
            for (String[] pluginReq : depends) {
                if (pluginManager.isPluginEnabled(pluginReq[0])) {
                    PluginDescriptionFile desc = pluginManager.getPlugin(pluginReq[0]).getDescription();
                    if (pluginReq[1].compareTo(desc.getVersion()) > 0) {
                        missingRequirements += "\u00a7buSkyBlock\u00a7e depends on \u00a79" + pluginReq[0] + "\u00a7e >= \u00a7av" + pluginReq[1] + "\u00a7e but only \u00a7cv" + desc.getVersion() + "\u00a7e was found!\n";
                    }
                } else {
                    missingRequirements += "\u00a7buSkyBlock\u00a7e depends on \u00a79" + pluginReq[0] + "\u00a7e >= \u00a7av" + pluginReq[1];
                }
            }
        }
        if (missingRequirements.isEmpty()) {
            return true;
        } else {
            sender.sendMessage(missingRequirements.split("\n"));
            return false;
        }
    }

    private void createFolders() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.directoryPlayers = new File(this.getDataFolder() + File.separator + "players");
        if (!this.directoryPlayers.exists()) {
            this.directoryPlayers.mkdirs();
        }
        directoryIslands = new File(this.getDataFolder() + File.separator + "islands");
        if (!directoryIslands.exists()) {
            directoryIslands.mkdirs();
        }
        IslandInfo.setDirectory(directoryIslands);
        File directorySchematics = new File(this.getDataFolder() + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            directorySchematics.mkdir();
        }
        this.schemFile = directorySchematics.listFiles();
        if (this.schemFile == null) {
            System.out.print("[uSkyBlock] No schematic file loaded.");
        } else {
            System.out.print("[uSkyBlock] " + this.schemFile.length + " schematics loaded.");
        }
    }

    public static uSkyBlock getInstance() {
        return uSkyBlock.instance;
    }

    public void unloadPlayerFiles() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (this.getActivePlayers().containsKey(player.getName())) {
                this.removeActivePlayer(player.getName());
                notifier.unloadPlayer(player);
            }
        }
    }

    public void registerEvents(PlayerDB playerDB) {
        final PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new PlayerNameChangeListener(this), this);
        manager.registerEvents(new PlayerNameChangeManager(this, playerDB), this);
        manager.registerEvents(new PlayerEvents(this), this);
        manager.registerEvents(new MenuEvents(this), this);
        manager.registerEvents(new ExploitEvents(this), this);
        if (getConfig().getBoolean("options.protection.enabled", true)) {
            manager.registerEvents(new GriefEvents(this), this);
            if (getConfig().getBoolean("options.protection.item-drops", true)) {
                manager.registerEvents(new ItemDropEvents(this), this);
            }
        }
    }

    public World getWorld() {
        if (uSkyBlock.skyBlockWorld == null) {
            skyBlockWorld = Bukkit.getWorld(Settings.general_worldName);
            if (skyBlockWorld == null || skyBlockWorld.canGenerateStructures() || !(skyBlockWorld.getGenerator() instanceof SkyBlockChunkGenerator)) {
                uSkyBlock.skyBlockWorld = WorldCreator
                        .name(Settings.general_worldName)
                        .type(WorldType.NORMAL)
                        .generateStructures(false)
                        .environment(World.Environment.NORMAL)
                        .generator(new SkyBlockChunkGenerator())
                        .createWorld();
                uSkyBlock.skyBlockWorld.save();
            }
            MultiverseCoreHandler.importWorld(skyBlockWorld);
        }
        return uSkyBlock.skyBlockWorld;
    }

    public static World getSkyBlockWorld() {
        return getInstance().getWorld();
    }

    public void clearOrphanedIsland() {
        while (this.hasOrphanedIsland()) {
            orphaned.pop();
        }
    }

    public Location getSafeHomeLocation(final PlayerInfo p) {
        Location home = null;
        if (p.getHomeLocation() != null) {
            home = p.getHomeLocation();
        } else if (p.getIslandLocation() != null) {
            home = p.getIslandLocation();
        }
        if (this.isSafeLocation(home)) {
            return home;
        }
        if (home == null) {
            return null;
        }
        for (int y = home.getBlockY() + 25; y > 0; --y) {
            final Location n = new Location(home.getWorld(), (double) home.getBlockX(), (double) y, (double) home.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        for (int y = home.getBlockY(); y < 255; ++y) {
            final Location n = new Location(home.getWorld(), (double) home.getBlockX(), (double) y, (double) home.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        final Location island = p.getIslandLocation();
        if (this.isSafeLocation(island)) {
            return island;
        }
        if (island == null) {
            return null;
        }
        for (int y2 = island.getBlockY() + 25; y2 > 0; --y2) {
            final Location n2 = new Location(island.getWorld(), (double) island.getBlockX(), (double) y2, (double) island.getBlockZ());
            if (this.isSafeLocation(n2)) {
                return n2;
            }
        }
        for (int y2 = island.getBlockY(); y2 < 255; ++y2) {
            final Location n2 = new Location(island.getWorld(), (double) island.getBlockX(), (double) y2, (double) island.getBlockZ());
            if (this.isSafeLocation(n2)) {
                return n2;
            }
        }
        return p.getHomeLocation();
    }

    public Location getSafeWarpLocation(final PlayerInfo p) {
        Location warp = null;
        FileConfiguration island = getTempIslandConfig(p.locationForParty());
        if (island.getInt("general.warpLocationX") == 0) {
            if (p.getHomeLocation() == null) {
                if (p.getIslandLocation() != null) {
                    warp = p.getIslandLocation();
                }
            } else {
                warp = p.getHomeLocation();
            }
        } else {
            warp = new Location(uSkyBlock.skyBlockWorld, (double) island.getInt("general.warpLocationX"), (double) island.getInt("general.warpLocationY"), (double) island.getInt("general.warpLocationZ"));
        }
        if (warp == null) {
            System.out.print("Error warping player to " + p.getPlayerName() + "'s island.");
            return null;
        }
        if (this.isSafeLocation(warp)) {
            return warp;
        }
        for (int y = warp.getBlockY() + 25; y > 0; --y) {
            final Location n = new Location(warp.getWorld(), (double) warp.getBlockX(), (double) y, (double) warp.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        for (int y = warp.getBlockY(); y < 255; ++y) {
            final Location n = new Location(warp.getWorld(), (double) warp.getBlockX(), (double) y, (double) warp.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        return null;
    }

    public boolean isSafeLocation(final Location l) {
        if (l == null) {
            return false;
        }
        final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
        final Block air1 = l.getBlock();
        final Block air2 = l.getBlock().getRelative(BlockFace.UP);
        return ground.getType().isSolid() && !air1.getType().isSolid() && !air2.getType().isSolid();
    }

    public void removeCreatures(final Location l) {
        if (!Settings.island_removeCreaturesByTeleport || l == null) {
            return;
        }
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                final Chunk c = l.getWorld().getChunkAt(new Location(l.getWorld(), (double) (px + x * 16), (double) py, (double) (pz + z * 16)));
                Entity[] entities;
                for (int length = (entities = c.getEntities()).length, i = 0; i < length; ++i) {
                    final Entity e = entities[i];
                    if (e.getType() == EntityType.SPIDER || e.getType() == EntityType.CREEPER || e.getType() == EntityType.ENDERMAN || e.getType() == EntityType.SKELETON || e.getType() == EntityType.ZOMBIE) {
                        e.remove();
                    }
                }
            }
        }
    }

    private void postDelete(final PlayerInfo pi) {
        IslandInfo islandInfo = getIslandInfo(pi);
        if (islandInfo != null) {
            postDelete(islandInfo);
        }
        pi.removeFromIsland();
        pi.save();
        removeActivePlayer(pi.getPlayerName());
    }

    private void postDelete(final IslandInfo islandInfo) {
        addOrphan(islandInfo.getIslandLocation());
        WorldGuardHandler.removeIslandRegion(islandInfo.getName());
        islandLogic.deleteIslandConfig(islandInfo.getName());
        saveOrphans();
    }

    public boolean deleteEmptyIsland(String islandName, final Runnable runner) {
        final IslandInfo islandInfo = getIslandInfo(islandName);
        if (islandInfo != null && islandInfo.getMembers().isEmpty()) {
            islandLogic.clearIsland(islandInfo.getIslandLocation(), new Runnable() {
                @Override
                public void run() {
                    postDelete(islandInfo);
                    if (runner != null) {
                        runner.run();
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public void deletePlayerIsland(final String player, final Runnable runner) {
        final PlayerInfo pi = activePlayers.containsKey(player) ? activePlayers.get(player) : new PlayerInfo(player);
        islandLogic.clearIsland(pi.getIslandLocation(), new Runnable() {
            @Override
            public void run() {
                postDelete(pi);
                if (runner != null) runner.run();
            }
        });
    }

    private void postRestart(final Player player, final Location next) {
        createIsland(player, next);
        changePlayerBiome(player, "OCEAN");
        next.setY((double) Settings.island_height);
        setNewPlayerIsland(player, next);
        setRestartCooldown(player);
        getServer().getScheduler().runTaskLater(uSkyBlock.getInstance(), new Runnable() {
            @Override
            public void run() {
                homeTeleport(player);
                clearPlayerInventory(player);
                clearEntitiesNearPlayer(player);
            }
        }, 10);
    }

    public boolean restartPlayerIsland(final Player player, final Location next) {
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return false;
        }
        spawnTeleport(player);
        islandLogic.clearIsland(next, new Runnable() {
            @Override
            public void run() {
                postRestart(player, next);
            }
        });
        return true;
    }

    public void clearPlayerInventory(Player player) {
        if (player.getWorld().getName().equalsIgnoreCase(skyBlockWorld.getName())) {
            player.getInventory().clear();
            ItemStack[] armor = player.getEquipment().getArmorContents();
            player.getEquipment().setArmorContents(new ItemStack[armor.length]);
            player.getEnderChest().clear();
        } else {
            log(Level.SEVERE, "Trying to clear player-inventory of " + player + ", even though they are not in the skyworld!");
        }
    }

    private void clearEntitiesNearPlayer(Player player) {
        for (final Entity entity : player.getNearbyEntities((double) (Settings.island_radius), 255.0, (double) (Settings.island_radius))) {
            if (!validEntity(entity)) {
                entity.remove();
            }
        }
    }

    private boolean validEntity(Entity entity) {
        return (entity instanceof Player) ||
                (entity.getFallDistance() == 0 && !(entity instanceof Monster));
    }

    public Location findBedrockLocation(final Location l) {
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        World world = l.getWorld();
        for (int x = -10; x <= 10; ++x) {
            for (int y = -10; y <= 10; ++y) {
                for (int z = -10; z <= 10; ++z) {
                    final Block b = world.getBlockAt(px + x, py + y, pz + z);
                    if (b.getType() == Material.BEDROCK) {
                        return new Location(world, px + x, py + y, pz + z);
                    }
                }
            }
        }
        return null;
    }

    public boolean devSetPlayerIsland(final Player sender, final Location l, final String player) {
        final PlayerInfo pi = getPlayerInfo(player);
        final Location newLoc = findBedrockLocation(l);
        boolean deleteOldIsland = false;
        if (pi.getHasIsland()) {
            Location oldLoc = pi.getIslandLocation();
            if (newLoc != null && oldLoc != null
                    && !(newLoc.getBlockX() == oldLoc.getBlockX() && newLoc.getBlockZ() == oldLoc.getBlockZ())) {
                deleteOldIsland = true;
            }
        }
        if (newLoc != null) {
            if (newLoc.equals(pi.getIslandLocation())) {
                sender.sendMessage("\u00a74Player is already assigned to this island!");
                return true;
            }
            Runnable resetIsland = new Runnable() {
                @Override
                public void run() {
                    pi.setHomeLocation(null);
                    pi.setHasIsland(true);
                    pi.setIslandLocation(newLoc);
                    pi.setHomeLocation(getSafeHomeLocation(pi));
                    islandLogic.createIsland(pi.locationForParty(), player);
                    WorldGuardHandler.protectIsland(sender, pi);
                    pi.save();
                }
            };
            if (deleteOldIsland) {
                deletePlayerIsland(pi.getPlayerName(), resetIsland);
            } else {
                resetIsland.run();
            }
            return true;
        }
        return false;
    }

    public int orphanCount() {
        return orphaned.size();
    }

    public Location getLastIsland() {
        if (lastIsland != null && isSkyWorld(lastIsland.getWorld())) {
            return lastIsland;
        }
        this.setLastIsland(new Location(getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0));
        return new Location(getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0);
    }

    public void setLastIsland(final Location island) {
        this.getLastIslandConfig().set("options.general.lastIslandX", island.getBlockX());
        this.getLastIslandConfig().set("options.general.lastIslandZ", island.getBlockZ());
        this.saveLastIslandConfig();
        this.lastIsland = island;
    }

    public boolean hasOrphanedIsland() {
        return !orphaned.empty();
    }

    public Location checkOrphan() {
        return orphaned.peek();
    }

    public Location getOrphanedIsland() {
        if (this.hasOrphanedIsland()) {
            return orphaned.pop();
        }
        return null;
    }

    public void addOrphan(final Location island) {
        if (!orphaned.contains(island)) {
            orphaned.push(island);
        }
    }

    public void removeNextOrphan() {
        orphaned.pop();
    }

    public void saveOrphans() {
        String fullOrphan = "";
        this.tempOrphaned = (Stack<Location>) orphaned.clone();
        while (!this.tempOrphaned.isEmpty()) {
            reverseOrphaned.push(this.tempOrphaned.pop());
        }
        while (!reverseOrphaned.isEmpty()) {
            final Location tempLoc = reverseOrphaned.pop();
            if (tempLoc != null) {
                fullOrphan += tempLoc.getBlockX() + "," + tempLoc.getBlockZ() + ";";
            }
        }
        this.getOrphans().set("orphans.list", fullOrphan);
        this.saveOrphansFile();
    }

    public void setupOrphans() {
        if (this.getOrphans().contains("orphans.list")) {
            final String fullOrphan = this.getOrphans().getString("orphans.list");
            if (!fullOrphan.isEmpty()) {
                final String[] orphanArray = fullOrphan.split(";");
                orphaned = new Stack<>();
                for (int i = 0; i < orphanArray.length; ++i) {
                    final String[] orphanXY = orphanArray[i].split(",");
                    final Location tempLoc = new Location(
                            getSkyBlockWorld(), Integer.parseInt(orphanXY[0], 10),
                            Settings.island_height,
                            Integer.parseInt(orphanXY[1], 10));
                    orphaned.push(tempLoc);
                }
            }
        }
    }

    public boolean homeTeleport(final Player player) {
        Location homeSweetHome = null;
        if (this.getActivePlayers().containsKey(player.getName())) {
            homeSweetHome = getSafeHomeLocation(this.getActivePlayers().get(player.getName()));
        }
        if (homeSweetHome == null) {
            player.performCommand("spawn");
            player.sendMessage("\u00a74You are not part of an island. Returning you the spawn area!");
            return true;
        }
        removeCreatures(homeSweetHome);
        safeTeleport(player, homeSweetHome);
        player.sendMessage(ChatColor.GREEN + "Teleporting you to your island.");
        return true;
    }

    private void safeTeleport(Player player, Location homeSweetHome) {
        player.setVelocity(new org.bukkit.util.Vector());
        player.teleport(homeSweetHome);
        player.setVelocity(new org.bukkit.util.Vector());
    }

    public boolean warpTeleport(final Player player, final PlayerInfo pi) {
        Location warpSweetWarp = null;
        if (pi == null) {
            player.sendMessage("\u00a74That player does not exist!");
            return true;
        }
        warpSweetWarp = getSafeWarpLocation(pi);
        if (warpSweetWarp == null) {
            player.sendMessage("\u00a74Unable to warp you to that player's island!");
            return true;
        }
        safeTeleport(player, warpSweetWarp);
        player.sendMessage(ChatColor.GREEN + "Teleporting you to " + pi.getPlayerName() + "'s island.");
        return true;
    }

    public void spawnTeleport(final Player player) {
        execCommand(player, "op:spawn");
    }

    public boolean homeSet(final Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
            player.sendMessage("\u00a74You must be closer to your island to set your skyblock home!");
            return true;
        }
        if (this.playerIsOnIsland(player)) {
            if (this.getActivePlayers().containsKey(player.getName())) {
                this.getActivePlayers().get(player.getName()).setHomeLocation(player.getLocation());
            }
            player.sendMessage(ChatColor.GREEN + "Your skyblock home has been set to your current location.");
            return true;
        }
        player.sendMessage("\u00a74You must be closer to your island to set your skyblock home!");
        return true;
    }

    public boolean playerIsOnIsland(final Player player) {
        return locationIsOnIsland(player, player.getLocation());
    }

    public boolean locationIsOnIsland(final Player player, final Location loc) {
        if (!isSkyWorld(player.getWorld())) {
            return false;
        }
        if (this.getActivePlayers().containsKey(player.getName())) {
            Location p = getPlayerInfo(player).getIslandLocation();
            if (p == null) {
                return false;
            }
            int r = Settings.island_radius;
            CuboidRegion region = new CuboidRegion(
                    new Vector(p.getBlockX() - r, 0, p.getBlockZ() - r),
                    new Vector(p.getBlockX() + r, 255, p.getBlockZ() + r)
            );
            return region.contains(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
        return false;
    }

    public boolean hasIsland(final String playername) {
        return getPlayerInfo(playername).getHasIsland();
    }

    public boolean islandAtLocation(final Location loc) {
        return WorldGuardHandler.getIslandNameAt(loc) != null;
    }

    public boolean islandInSpawn(final Location loc) {
        if (loc == null) {
            return true;
        }
        return WorldGuardHandler.isIslandIntersectingSpawn(loc);
    }

    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        return new SkyBlockChunkGenerator();
    }

    public boolean onInfoCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.infoCooldown")
                && !player.hasPermission("usb.mod.bypasscooldowns")
                && infoCooldown.containsKey(player.getUniqueId())
                && infoCooldown.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public boolean onBiomeCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.biomeCooldown")
                && !player.hasPermission("usb.mod.bypasscooldowns")
                && biomeCooldown.containsKey(player.getUniqueId())
                && biomeCooldown.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public boolean onRestartCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.restartCooldown")
                && !player.hasPermission("usb.mod.bypasscooldowns")
                && restartCooldown.containsKey(player.getUniqueId())
                && this.restartCooldown.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public long getInfoCooldownTime(final Player player) {
        if (!infoCooldown.containsKey(player.getUniqueId())) {
            return 0L;
        }
        if (infoCooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            return infoCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public long getBiomeCooldownTime(final Player player) {
        if (!biomeCooldown.containsKey(player.getUniqueId())) {
            return 0L;
        }
        if (biomeCooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            return biomeCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public long getRestartCooldownTime(final Player player) {
        if (!this.restartCooldown.containsKey(player.getUniqueId())) {
            return 0L;
        }
        if (this.restartCooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            return this.restartCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public void setInfoCooldown(final Player player) {
        infoCooldown.put(player.getUniqueId(), System.currentTimeMillis() + Settings.general_cooldownInfo * 1000);
    }

    public void setBiomeCooldown(final Player player) {
        biomeCooldown.put(player.getUniqueId(), System.currentTimeMillis() + Settings.general_biomeChange * 1000);
    }

    public void setRestartCooldown(final Player player) {
        this.restartCooldown.put(player.getUniqueId(), System.currentTimeMillis() + Settings.general_cooldownRestart * 1000);
    }

    public File[] getSchemFile() {
        return this.schemFile;
    }

    /**
     * Tests for more than one obsidian close by.
     */
    public boolean testForObsidian(final Block block) {
        for (int x = -3; x <= 3; ++x) {
            for (int y = -3; y <= 3; ++y) {
                for (int z = -3; z <= 3; ++z) {
                    final Block testBlock = getSkyBlockWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
                    if ((x != 0 || y != 0 || z != 0) && testBlock.getType() == Material.OBSIDIAN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isPurgeActive() {
        return this.purgeActive;
    }

    public void activatePurge() {
        this.purgeActive = true;
    }

    public void deactivatePurge() {
        this.purgeActive = false;
    }

    private Map<String, PlayerInfo> getActivePlayers() {
        return activePlayers;
    }

    public PlayerInfo getPlayerInfo(Player player) {
        // TODO: UUID aware
        String playerName = player.getName();
        PlayerInfo playerInfo = getPlayerInfo(playerName);
        if (player != null && player.isOnline()) {
            playerInfo.updatePlayerInfo(player);
        }
        return playerInfo;
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        PlayerInfo playerInfo = activePlayers.get(playerName);
        if (playerInfo == null) {
            playerInfo = loadPlayerInfo(playerName);
        }
        return playerInfo;
    }

    public void addActivePlayer(final String player, final PlayerInfo pi) {
        activePlayers.put(player, pi);
    }

    public void removeActivePlayer(final String player) {
        if (activePlayers.containsKey(player)) {
            activePlayers.get(player).save();
            activePlayers.remove(player);
            log(Level.FINE, "Removing player from memory: " + player);
        }
    }

    public PlayerInfo loadPlayerData(Player player) {
        uSkyBlock.log(Level.INFO, "Loading player data for " + player.getName());
        final PlayerInfo pi = loadPlayerInfo(player.getName());
        if (pi.getHasIsland()) {
            WorldGuardHandler.protectIsland(player, pi);
            islandLogic.clearFlatland(player, pi.getIslandLocation(), 200);
        }
        addActivePlayer(player.getName(), pi);
        return pi;
    }

    private PlayerInfo loadPlayerInfo(String playerName) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName);
        activePlayers.put(playerName, playerInfo);
        return playerInfo;
    }

    public void unloadPlayerData(Player player) {
        if (hasIsland(player.getName()) && !hasIslandMembersOnline(player)) {
            islandLogic.removeIslandFromMemory(getPlayerInfo(player).locationForParty());
        }
        removeActivePlayer(player.getName());
        notifier.unloadPlayer(player);
    }

    public FileConfiguration getTempIslandConfig(final String location) {
        File islandFile = new File(this.directoryIslands, location + ".yml");
        return YamlConfiguration.loadConfiguration(islandFile);
    }

    public void reloadLastIslandConfig() {
        if (this.lastIslandConfigFile == null) {
            this.lastIslandConfigFile = new File(this.getDataFolder(), "lastIslandConfig.yml");
        }
        this.lastIslandConfig = YamlConfiguration.loadConfiguration(this.lastIslandConfigFile);
        final InputStream defConfigStream = this.getResource("lastIslandConfig.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.lastIslandConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getLastIslandConfig() {
        if (this.lastIslandConfig == null) {
            this.reloadLastIslandConfig();
        }
        return this.lastIslandConfig;
    }

    public void saveLastIslandConfig() {
        if (this.lastIslandConfig == null || this.lastIslandConfigFile == null) {
            return;
        }
        try {
            this.getLastIslandConfig().save(this.lastIslandConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.lastIslandConfigFile, ex);
        }
    }

    public void reloadOrphans() {
        if (this.orphanFile == null) {
            this.orphanFile = new File(this.getDataFolder(), "orphans.yml");
        }
        this.orphans = YamlConfiguration.loadConfiguration(this.orphanFile);
        final InputStream defConfigStream = this.getResource("orphans.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.orphans.setDefaults(defConfig);
        }
    }

    public FileConfiguration getOrphans() {
        if (this.orphans == null) {
            this.reloadOrphans();
        }
        return this.orphans;
    }

    public void saveOrphansFile() {
        if (this.orphans == null || this.orphanFile == null) {
            return;
        }
        try {
            this.getOrphans().save(this.orphanFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.orphanFile, ex);
        }
    }

    public boolean setBiome(final Location loc, final String bName) {
        Biome biome;
        if (bName.equalsIgnoreCase("jungle")) {
            biome = Biome.JUNGLE;
        } else if (bName.equalsIgnoreCase("hell")) {
            biome = Biome.HELL;
        } else if (bName.equalsIgnoreCase("sky")) {
            biome = Biome.SKY;
        } else if (bName.equalsIgnoreCase("mushroom")) {
            biome = Biome.MUSHROOM_ISLAND;
        } else if (bName.equalsIgnoreCase("ocean")) {
            biome = Biome.OCEAN;
        } else if (bName.equalsIgnoreCase("swampland")) {
            biome = Biome.SWAMPLAND;
        } else if (bName.equalsIgnoreCase("taiga")) {
            biome = Biome.TAIGA;
        } else if (bName.equalsIgnoreCase("desert")) {
            biome = Biome.DESERT;
        } else if (bName.equalsIgnoreCase("forest")) {
            biome = Biome.FOREST;
        } else {
            biome = Biome.OCEAN;
        }
        setBiome(loc, biome);
        return biome != Biome.OCEAN;
    }

    private void setBiome(Location loc, Biome biome) {
        int r = Settings.island_radius;
        final int px = loc.getBlockX();
        final int pz = loc.getBlockZ();
        for (int x = px - r; x <= px + r; x++) {
            for (int z = pz - r; z <= pz + r; z++) {
                skyBlockWorld.setBiome(x, z, biome); // World Coords
            }
        }
    }

    public boolean changePlayerBiome(final Player player, final String bName) {
        if (!VaultHandler.checkPerk(player.getName(), "usb.biome." + bName, skyBlockWorld)) {
            return false;
        }
        PlayerInfo playerInfo = getPlayerInfo(player);
        IslandInfo islandInfo = islandLogic.getIslandInfo(playerInfo);
        if (islandInfo.hasPerm(player.getName(), "canChangeBiome")) {
            setBiome(playerInfo.getIslandLocation(), bName);
            islandInfo.setBiome(bName);
            return true;
        }
        return false;
    }

    public void listBiomes(final Player player) {
        String biomeList = ", ";
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean", skyBlockWorld)) {
            biomeList = "OCEAN, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.forest", skyBlockWorld)) {
            biomeList = biomeList + "FOREST, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", skyBlockWorld)) {
            biomeList = biomeList + "JUNGLE, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", skyBlockWorld)) {
            biomeList = biomeList + "DESERT, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", skyBlockWorld)) {
            biomeList = biomeList + "TAIGA, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", skyBlockWorld)) {
            biomeList = biomeList + "SWAMPLAND, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", skyBlockWorld)) {
            biomeList = biomeList + "MUSHROOM, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", skyBlockWorld)) {
            biomeList = biomeList + "HELL, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", skyBlockWorld)) {
            biomeList = biomeList + "SKY, ";
        }
        player.sendMessage("\u00a7eYou have access to the following Biomes:");
        player.sendMessage(ChatColor.GREEN + biomeList.substring(0, biomeList.length() - 2));
        player.sendMessage("\u00a7eUse /island biome <biomename> to change your biome. You must wait " + Settings.general_biomeChange / 60 + " minutes between each biome change.");
    }

    public boolean createIsland(final Player player, final PlayerInfo pi) {
        if (isSkyWorld(player.getWorld())) {
            spawnTeleport(player);
        }
        log(Level.INFO, "Creating player island...");
        final Location last = getLastIsland();
        last.setY((double) Settings.island_height);
        try {
            Location next = getNextIslandLocation(last);
            createIsland(player, next);
            setNewPlayerIsland(player, next);
            changePlayerBiome(player, "OCEAN");
            protectWithWorldGuard(player, player, pi);
            homeTeleport(player);
            clearPlayerInventory(player);
            clearEntitiesNearPlayer(player);
        } catch (Exception ex) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            log(Level.SEVERE, "Error creating island", ex);
            return false;
        }
        log(Level.INFO, "Finished creating player island.");
        return true;
    }

    private void protectWithWorldGuard(CommandSender sender, Player player, PlayerInfo pi) {
        if (!WorldGuardHandler.protectIsland(player, pi)) {
            sender.sendMessage("Player doesn't have an island or it's already protected!");
        }
    }

    private void createIsland(Player player, Location next) {
        boolean hasIslandNow = false;
        if (getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            for (File schemFile : getSchemFile()) {
                // First run-through - try to set the island the player has permission for.
                String cSchem = schemFile.getName();
                if (cSchem.lastIndexOf('.') > 0) {
                    cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                }
                if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, skyBlockWorld)
                        && WorldEditHandler.loadIslandSchematic(player, skyBlockWorld, schemFile, next)) {
                    setChest(next, player);
                    hasIslandNow = true;
                    break;
                }
            }
            if (!hasIslandNow) {
                for (File schemFile : getSchemFile()) {
                    // 2nd Run through, set the default set schematic (if found).
                    String cSchem = schemFile.getName();
                    if (cSchem.lastIndexOf('.') > 0) {
                        cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)
                            && WorldEditHandler.loadIslandSchematic(player, skyBlockWorld, schemFile, next)) {
                        this.setChest(next, player);
                        hasIslandNow = true;
                        break;
                    }
                }
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                this.generateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, skyBlockWorld);
            } else {
                this.oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, skyBlockWorld);
            }
        }
        next.setY((double) Settings.island_height);
    }

    private synchronized Location getNextIslandLocation(Location last) {
        while (hasOrphanedIsland() && !isSkyWorld(checkOrphan().getWorld())) {
            removeNextOrphan();
        }
        while (hasOrphanedIsland()) {
            if (!islandAtLocation(checkOrphan())) {
                break;
            }
            removeNextOrphan();
        }
        Location next = last;
        if (hasOrphanedIsland() && !islandAtLocation(checkOrphan())) {
            next = getOrphanedIsland();
            saveOrphans();
        }
        while (islandInSpawn(next) || islandAtLocation(next)) {
            next = nextIslandLocation(next);
        }
        setLastIsland(next);
        return next;
    }

    public void generateIslandBlocks(final int x, final int z, final Player player, final World world) {
        final int y = Settings.island_height;
        final Block blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(7);
        this.islandLayer1(x, z, player, world);
        this.islandLayer2(x, z, player, world);
        this.islandLayer3(x, z, player, world);
        this.islandLayer4(x, z, player, world);
        this.islandExtras(x, z, player, world);
    }

    public void oldGenerateIslandBlocks(final int x, final int z, final Player player, final World world) {
        final int y = Settings.island_height;
        for (int x_operate = x; x_operate < x + 3; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(2);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 6; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(2);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 7; ++x_operate) {
            for (int y_operate = y + 7; y_operate < y + 10; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 7; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(18);
                }
            }
        }
        for (int y_operate2 = y + 3; y_operate2 < y + 9; ++y_operate2) {
            final Block blockToChange2 = world.getBlockAt(x + 5, y_operate2, z + 5);
            blockToChange2.setTypeId(17);
        }
        Block blockToChange3 = world.getBlockAt(x + 1, y + 3, z + 1);
        blockToChange3.setTypeId(54);
        final Chest chest = (Chest) blockToChange3.getState();
        final Inventory inventory = chest.getInventory();
        inventory.clear();
        inventory.setContents(Settings.island_chestItems);
        if (Settings.island_addExtraItems) {
            for (int i = 0; i < Settings.island_extraPermissions.length; ++i) {
                if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
                    final String[] chestItemString = getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                    String[] amountdata = new String[2];
                    for (int j = 0; j < chestItemString.length; ++j) {
                        amountdata = chestItemString[j].split(":");
                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0], 10), Integer.parseInt(amountdata[1], 10));
                        inventory.addItem(new ItemStack[]{tempChest[j]});
                    }
                }
            }
        }
        blockToChange3 = world.getBlockAt(x, y, z);
        blockToChange3.setTypeId(7);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 1);
        blockToChange3.setTypeId(12);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 2);
        blockToChange3.setTypeId(12);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 3);
        blockToChange3.setTypeId(12);
    }

    private Location nextIslandLocation(final Location lastIsland) {
        final int x = (int) lastIsland.getX();
        final int z = (int) lastIsland.getZ();
        if (x < z) {
            if (-1 * x < z) {
                lastIsland.setX(lastIsland.getX() + Settings.island_distance);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() + Settings.island_distance);
            return lastIsland;
        } else if (x > z) {
            if (-1 * x >= z) {
                lastIsland.setX(lastIsland.getX() - Settings.island_distance);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() - Settings.island_distance);
            return lastIsland;
        } else {
            if (x <= 0) {
                lastIsland.setZ(lastIsland.getZ() + Settings.island_distance);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() - Settings.island_distance);
            return lastIsland;
        }
    }

    private void islandLayer1(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height;
        y = Settings.island_height + 4;
        for (int x_operate = x - 3; x_operate <= x + 3; ++x_operate) {
            for (int z_operate = z - 3; z_operate <= z + 3; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(2);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z + 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x - 3, y, z - 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x + 3, y, z - 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x + 3, y, z + 3);
        blockToChange2.setTypeId(0);
    }

    private void islandLayer2(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height + 3;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(3);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x + 3, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z - 3);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z + 3);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeId(12);
    }

    private void islandLayer3(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height;
        y = Settings.island_height + 2;
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(3);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 2, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x + 2, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z - 2);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z + 2);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeId(12);
    }

    private void islandLayer4(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height;
        y = Settings.island_height + 1;
        Block blockToChange = world.getBlockAt(x - 1, y, z);
        blockToChange.setTypeId(3);
        blockToChange = world.getBlockAt(x + 1, y, z);
        blockToChange.setTypeId(3);
        blockToChange = world.getBlockAt(x, y, z - 1);
        blockToChange.setTypeId(3);
        blockToChange = world.getBlockAt(x, y, z + 1);
        blockToChange.setTypeId(3);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(12);
    }

    private void islandExtras(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height;
        Block blockToChange = world.getBlockAt(x, y + 5, z);
        blockToChange.setTypeId(17);
        blockToChange = world.getBlockAt(x, y + 6, z);
        blockToChange.setTypeId(17);
        blockToChange = world.getBlockAt(x, y + 7, z);
        blockToChange.setTypeId(17);
        y = Settings.island_height + 8;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
                blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(18);
            }
        }
        blockToChange = world.getBlockAt(x + 2, y, z + 2);
        blockToChange.setTypeId(0);
        blockToChange = world.getBlockAt(x + 2, y, z - 2);
        blockToChange.setTypeId(0);
        blockToChange = world.getBlockAt(x - 2, y, z + 2);
        blockToChange.setTypeId(0);
        blockToChange = world.getBlockAt(x - 2, y, z - 2);
        blockToChange.setTypeId(0);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(17);
        y = Settings.island_height + 9;
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
                blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(18);
            }
        }
        blockToChange = world.getBlockAt(x - 2, y, z);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x + 2, y, z);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z - 2);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z + 2);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(17);
        y = Settings.island_height + 10;
        blockToChange = world.getBlockAt(x - 1, y, z);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x + 1, y, z);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z - 1);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z + 1);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(17);
        blockToChange = world.getBlockAt(x, y + 1, z);
        blockToChange.setTypeId(18);
        blockToChange = world.getBlockAt(x, Settings.island_height + 5, z + 1);
        blockToChange.setTypeId(54);
        blockToChange.setData((byte) 3);
        final Chest chest = (Chest) blockToChange.getState();
        final Inventory inventory = chest.getInventory();
        inventory.clear();
        inventory.setContents(Settings.island_chestItems);
        if (Settings.island_addExtraItems) {
            for (int i = 0; i < Settings.island_extraPermissions.length; ++i) {
                if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
                    final String chestItemString = getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i], "");
                    inventory.addItem(ItemStackUtil.createItemArray(chestItemString));
                }
            }
        }
    }

    public void setChest(final Location loc, final Player player) {
        for (int x = -15; x <= 15; ++x) {
            for (int y = -15; y <= 15; ++y) {
                for (int z = -15; z <= 15; ++z) {
                    if (skyBlockWorld.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
                        final Block blockToChange = skyBlockWorld.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                        final Chest chest = (Chest) blockToChange.getState();
                        final Inventory inventory = chest.getInventory();
                        inventory.clear();
                        inventory.setContents(Settings.island_chestItems);
                        if (Settings.island_addExtraItems) {
                            for (int i = 0; i < Settings.island_extraPermissions.length; ++i) {
                                if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
                                    final String[] chestItemString = getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                                    String[] amountdata = new String[2];
                                    for (int j = 0; j < chestItemString.length; ++j) {
                                        amountdata = chestItemString[j].split(":");
                                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0], 10), Integer.parseInt(amountdata[1], 10));
                                        inventory.addItem(new ItemStack[]{tempChest[j]});
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the neares block to loc that is a chest.
     *
     * @param loc The location to scan for a chest.
     * @return The location of the chest
     */
    public Location findChestLocation(final Location loc) {
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    if (world.getBlockAt(x, y, z).getType() == Material.CHEST) {
                        return new Location(world, x, y, z);
                    }
                }
            }
        }
        return loc;
    }

    private Location findNearestSpawnLocation(Location loc) {
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        Block chestBlock = world.getBlockAt(loc);
        if (chestBlock.getType() == Material.CHEST) {
            BlockFace primaryDirection = null;
            // Start by checking in front of the chest.
            MaterialData data = chestBlock.getState().getData();
            if (data instanceof org.bukkit.material.Chest) {
                primaryDirection = ((org.bukkit.material.Chest) data).getFacing();
            }
            if (primaryDirection == BlockFace.NORTH) {
                // Neg Z
                pz -= 1; // start one block in the north dir.
            } else if (primaryDirection == BlockFace.SOUTH) {
                // Pos Z
                pz += 1; // start one block in the south dir
            } else if (primaryDirection == BlockFace.WEST) {
                // Neg X
                px -= 1; // start one block in the west dir
            } else if (primaryDirection == BlockFace.EAST) {
                // Pos X
                px += 1; // start one block in the east dir
            }
        }
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    Location spawnLocation = new Location(world, x, y, z);
                    if (isSafeLocation(spawnLocation)) {
                        // look at the old location
                        Location d = loc.clone().subtract(spawnLocation);
                        spawnLocation.setDirection(d.toVector());
                        return spawnLocation;
                    }
                }
            }
        }
        return null;
    }

    public Location getChestSpawnLoc(final Location loc) {
        return findNearestSpawnLocation(findChestLocation(loc));
    }

    private void setNewPlayerIsland(final Player player, final Location loc) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        playerInfo.startNewIsland(loc);
        playerInfo.setHomeLocation(getChestSpawnLoc(loc).add(0.5, 0.1, 0.5));
        IslandInfo info = islandLogic.createIsland(playerInfo.locationForParty(), player.getName());
        info.updatePartyNumber(player);
        playerInfo.resetAllChallenges();
        playerInfo.save();
    }

    public boolean hasIslandMembersOnline(final Player p) {
        for (final String member : islandLogic.getIslandInfo(getPlayerInfo(p)).getMembers()) {
            if (Bukkit.getPlayer(member) != null && !Bukkit.getPlayer(member).isOnline()) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentBiome(Player p) {
        return getIslandInfo(p).getBiome();
    }

    public IslandInfo getIslandInfo(Player player) {
        return islandLogic.getIslandInfo(getPlayerInfo(player));
    }

    public boolean isPartyLeader(final Player player) {
        return getIslandInfo(player).getLeader().equalsIgnoreCase(player.getName());
    }

    public IslandInfo getIslandInfo(String location) {
        return islandLogic.getIslandInfo(location);
    }

    public IslandInfo getIslandInfo(PlayerInfo pi) {
        return islandLogic.getIslandInfo(pi);
    }

    public SkyBlockMenu getMenu() {
        return menu;
    }

    public static String stripFormatting(String format) {
        if (format == null || format.trim().isEmpty()) {
            return "";
        }
        return format.replaceAll("(\u00a7|&)[0-9a-fklmor]", "");
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    public static void log(Level level, String message, Throwable t) {
        getInstance().getLogger().log(level, message, t);
    }

    public ChallengeLogic getChallengeLogic() {
        return challengeLogic;
    }

    public LevelLogic getLevelLogic() {
        return levelLogic;
    }

    @Override
    public void reloadConfig() {
        Settings.loadPluginConfig(getConfig());
        reloadConfigs();
    }

    private void reloadConfigs() {
        createFolders();
        HandlerList.unregisterAll(this);
        VaultHandler.setupEconomy();
        if (Settings.loadPluginConfig(getConfig())) {
            saveConfig();
        }
        // Update all of the loaded configs.
        for (Map.Entry<String, FileConfiguration> e : configFiles.entrySet()) {
            File configFile = new File(getDataFolder(), e.getKey());
            readConfig(e.getValue(), configFile);
        }
        PlayerDB playerDB = new FilePlayerDB(new File(getDataFolder(), "uuid2name.yml"));
        PlayerUtil.loadConfig(playerDB, getConfig());
        activePlayers.clear();
        this.challengeLogic = new ChallengeLogic(getFileConfiguration("challenges.yml"), this);
        this.menu = new SkyBlockMenu(this, challengeLogic);
        this.levelLogic = new LevelLogic(getFileConfiguration("levelConfig.yml"));
        this.islandLogic = new IslandLogic(this, directoryIslands);
        this.notifier = new PlayerNotifier(getConfig());
        registerEvents(playerDB);
        if (autoRecalculateTask != null) {
            autoRecalculateTask.cancel();
        }
        int refreshEveryMinute = getConfig().getInt("options.island.autoRefreshScore", 0);
        if (refreshEveryMinute > 0) {
            int refreshTicks = refreshEveryMinute * 1200; // Ticks per minute
            autoRecalculateTask = new RecalculateRunnable(this).runTaskTimer(this, refreshTicks, refreshTicks);
        } else {
            autoRecalculateTask = null;
        }
    }

    public boolean isSkyWorld(World world) {
        if (world == null) {
            return false;
        }
        return getSkyBlockWorld().getName().equalsIgnoreCase(world.getName());
    }

    public boolean isSkyAssociatedWorld(World world) {
        return world.getName().startsWith(skyBlockWorld.getName());
    }

    public IslandLogic getIslandLogic() {
        return islandLogic;
    }

    public void execCommand(Player player, String command) {
        if (command == null || player == null) {
            return;
        }
        if (!isSkyAssociatedWorld(player.getWorld())) {
            return;
        }
        command = command
                .replaceAll("\\{player\\}", player.getName())
                .replaceAll("\\{playerName\\}", player.getDisplayName())
                .replaceAll("\\{position\\}", player.getLocation().toString()); // Figure out what this should be
        if (command.contains("{party}")) {
            PlayerInfo playerInfo = getPlayerInfo(player);
            for (String member : getIslandInfo(playerInfo).getMembers()) {
                doExecCommand(player, command.replaceAll("\\{party\\}", member));
            }
        } else {
            doExecCommand(player, command);
        }
    }

    private void doExecCommand(Player player, String command) {
        if (command.startsWith("op:")) {
            if (player.isOp()) {
                player.performCommand(command.substring(3).trim());
            } else {
                player.setOp(true);
                player.performCommand(command.substring(3).trim());
                player.setOp(false);
            }
        } else if (command.startsWith("console:")) {
            getServer().dispatchCommand(getServer().getConsoleSender(), command.substring(8).trim());
        } else {
            player.performCommand(command);
        }
    }

    public USBImporterExecutor getPlayerImporter() {
        if (importer == null) {
            importer = new USBImporterExecutor(this);
        }
        return importer;
    }

    public boolean playerIsInSpawn(Player player) {
        Location pLoc = player.getLocation();
        Location spawnCenter = new Location(skyBlockWorld, 0, pLoc.getBlockY(), 0);
        return spawnCenter.distance(pLoc) <= Settings.general_spawnSize;
    }

    /**
     * Notify the player, but max. every X seconds.
     */
    public void notifyPlayer(Player player, String msg) {
        notifier.notifyPlayer(player, msg);
    }

    public BalancedExecutor getExecutor() {
        return executor;
    }

    public BalancedExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    public static uSkyBlockAPI getAPI() {
        return getInstance();
    }

    // API

    @Override
    public List<IslandLevel> getTopTen() {
        return getRanks(0, 10);
    }

    @Override
    public List<IslandLevel> getRanks(int offset, int length) {
        return islandLogic != null ? islandLogic.getRanks(offset, length) : Collections.<IslandLevel>emptyList();
    }

    @Override
    public double getIslandLevel(Player player) {
        PlayerInfo info = getPlayerInfo(player);
        if (info != null) {
            IslandInfo islandInfo = getIslandInfo(info);
            if (islandInfo != null) {
                return islandInfo.getLevel();
            }
        }
        return 0;
    }

    public void fireChangeEvent(CommandSender sender, uSkyBlockEvent.Cause cause) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        final uSkyBlockEvent event = new uSkyBlockEvent(player, this, cause);
        fireChangeEvent(event);
    }

    public void fireChangeEvent(final uSkyBlockEvent event) {
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        getServer().getPluginManager().callEvent(event);
                    }
                }
        );
    }

    public IslandScore recalculateScore(Player player, String islandName) {
        IslandInfo islandInfo = getIslandInfo(islandName);
        IslandScore score = getLevelLogic().calculateScore(islandInfo.getIslandLocation());
        islandInfo.setLevel(score.getScore());
        getIslandLogic().updateRank(islandInfo, score);
        fireChangeEvent(new uSkyBlockScoreChangedEvent(player, this, score));
        return score;
    }
}
