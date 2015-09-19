package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;
import us.talabrek.ultimateskyblock.api.IslandLevel;
import us.talabrek.ultimateskyblock.api.IslandRank;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;
import us.talabrek.ultimateskyblock.async.AsyncBalancedExecutor;
import us.talabrek.ultimateskyblock.async.BalancedExecutor;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.async.SyncBalancedExecutor;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.challenge.ChallengesCommand;
import us.talabrek.ultimateskyblock.command.AdminCommand;
import us.talabrek.ultimateskyblock.command.IslandCommand;
import us.talabrek.ultimateskyblock.command.IslandTalkCommand;
import us.talabrek.ultimateskyblock.command.PartyTalkCommand;
import us.talabrek.ultimateskyblock.command.admin.DebugCommand;
import us.talabrek.ultimateskyblock.event.ExploitEvents;
import us.talabrek.ultimateskyblock.event.GriefEvents;
import us.talabrek.ultimateskyblock.event.ItemDropEvents;
import us.talabrek.ultimateskyblock.event.MenuEvents;
import us.talabrek.ultimateskyblock.event.PlayerEvents;
import us.talabrek.ultimateskyblock.event.SpawnEvents;
import us.talabrek.ultimateskyblock.handler.ConfirmHandler;
import us.talabrek.ultimateskyblock.handler.CooldownHandler;
import us.talabrek.ultimateskyblock.handler.MultiverseCoreHandler;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.imports.impl.USBImporterExecutor;
import us.talabrek.ultimateskyblock.island.IslandGenerator;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandLogic;
import us.talabrek.ultimateskyblock.island.IslandScore;
import us.talabrek.ultimateskyblock.island.LevelLogic;
import us.talabrek.ultimateskyblock.island.OrphanLogic;
import us.talabrek.ultimateskyblock.island.task.RecalculateRunnable;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.PerkLogic;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.player.PlayerLogic;
import us.talabrek.ultimateskyblock.player.PlayerNotifier;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.I18nUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.PlayerUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;
import us.talabrek.ultimateskyblock.uuid.FilePlayerDB;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangeListener;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.util.BlockUtil.isBreathable;
import static us.talabrek.ultimateskyblock.util.FileUtil.getFileConfiguration;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.LocationUtil.centerOnBlock;

public class uSkyBlock extends JavaPlugin implements uSkyBlockAPI {


    private static final String CN = uSkyBlock.class.getName();
    private static final String[][] depends = new String[][]{
            new String[]{"Vault", "1.4"},
            new String[]{"WorldEdit", "5.5"},
            new String[]{"WorldGuard", "5.9"},
            new String[]{"AsyncWorldEdit", "2.0", "optional"},
    };
    private static String missingRequirements = null;
    private static final Random RND = new Random(System.currentTimeMillis());

    private SkyBlockMenu menu;
    private ChallengeLogic challengeLogic;
    private LevelLogic levelLogic;
    private IslandLogic islandLogic;
    private OrphanLogic orphanLogic;
    private PerkLogic perkLogic;

    public IslandGenerator islandGenerator;
    private PlayerNotifier notifier;

    private USBImporterExecutor importer;
    private BalancedExecutor executor;
    private BalancedExecutor asyncExecutor;

    private static String pName = "";
    private FileConfiguration lastIslandConfig;
    private File lastIslandConfigFile;
    public static volatile World skyBlockWorld;
    private static uSkyBlock instance;
    private Location lastIsland;
    public File directoryPlayers;
    public File directoryIslands;

    private volatile boolean purgeActive;
    private volatile boolean protectAllActive;

    private BukkitTask autoRecalculateTask;

    static {
        uSkyBlock.skyBlockWorld = null;
    }

    private PlayerDB playerDB;
    private ConfirmHandler confirmHandler;

    private CooldownHandler cooldownHandler;
    private PlayerLogic playerLogic;

    private PlayerNameChangeManager playerNameChangeManager;

    private Map<String, Biome> validBiomes = new HashMap<String, Biome>() {
        {
            put("ocean", Biome.OCEAN);
            put("jungle", Biome.JUNGLE);
            put("hell", Biome.HELL);
            put("sky", Biome.SKY);
            put("mushroom", Biome.MUSHROOM_ISLAND);
            put("swampland", Biome.SWAMPLAND);
            put("taiga", Biome.TAIGA);
            put("desert", Biome.DESERT);
            put("forest", Biome.FOREST);
            put("plains", Biome.PLAINS);
            put("extreme_hills", Biome.EXTREME_HILLS);
            put("flower_forest", Biome.FLOWER_FOREST);
        }
    };

    public uSkyBlock() {
        lastIslandConfig = null;
        lastIslandConfigFile = null;
        purgeActive = false;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        try {
            if (lastIsland != null) {
                setLastIsland(lastIsland);
            }
            uSkyBlock.skyBlockWorld = null; // Force a reload on config.
        } catch (Exception e) {
            log(Level.INFO, tr("Something went wrong saving the island and/or party data!"), e);
        }
        playerLogic.shutdown();
        playerNameChangeManager.shutdown();
        DebugCommand.disableLogging(null);
    }

    @Override
    public FileConfiguration getConfig() {
        return getFileConfiguration("config.yml");
    }

    @Override
    public void onEnable() {
        skyBlockWorld = null; // Force a re-import or what-ever...
        missingRequirements = null;
        instance = this;
        FileUtil.init(getDataFolder());
        executor = new SyncBalancedExecutor(Bukkit.getScheduler());
        asyncExecutor = new AsyncBalancedExecutor(Bukkit.getScheduler());
        uSkyBlock.pName = "[" + getDescription().getName() + "] ";
        reloadConfigs();

        getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                    try {
                        FileConfiguration config = getLastIslandConfig();
                        if (!config.contains("options.general.lastIslandX") && getConfig().contains("options.general.lastIslandX")) {
                            FileConfiguration.createPath(config.getConfigurationSection("options.general"), "lastIslandX");
                            FileConfiguration.createPath(config.getConfigurationSection("options.general"), "lastIslandZ");
                            config.set("options.general.lastIslandX", uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"));
                            config.set("options.general.lastIslandZ", uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ"));
                            saveLastIslandConfig();
                        }
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), (double) config.getInt("options.general.lastIslandX"), (double) Settings.island_height, (double) config.getInt("options.general.lastIslandZ")));
                    } catch (Exception e) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(),
                                (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"),
                                (double) Settings.island_height,
                                (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ")));
                    }
                    if (lastIsland == null) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0));
                    }
                }
                WorldGuardHandler.setupGlobal(getSkyBlockWorld());
                getServer().getScheduler().runTaskLater(instance, new Runnable() {
                    @Override
                    public void run() {
                        for (Player player : getServer().getOnlinePlayers()) {
                            playerLogic.loadPlayerDataAsync(player);
                        }
                    }
                }, 50L);
            }
        }, 50L);
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (Exception e) {
            log(Level.WARNING, "Failed to submit metrics data", e);
        }
        log(Level.INFO, getVersionInfo(false));
    }

    public synchronized boolean isRequirementsMet(CommandSender sender) {
        if (missingRequirements == null) {
            PluginManager pluginManager = getServer().getPluginManager();
            missingRequirements = "";
            for (String[] pluginReq : depends) {
                if (pluginReq.length > 2 && pluginReq[2].equals("optional")) {
                    continue;
                }
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
        directoryPlayers = new File(getDataFolder() + File.separator + "players");
        if (!directoryPlayers.exists()) {
            directoryPlayers.mkdirs();
        }
        directoryIslands = new File(getDataFolder() + File.separator + "islands");
        if (!directoryIslands.exists()) {
            directoryIslands.mkdirs();
        }
        IslandInfo.setDirectory(directoryIslands);
    }

    public static uSkyBlock getInstance() {
        return uSkyBlock.instance;
    }

    public void registerEvents(PlayerDB playerDB) {
        final PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PlayerNameChangeListener(this), this);
        manager.registerEvents(playerNameChangeManager, this);
        manager.registerEvents(new PlayerEvents(this), this);
        manager.registerEvents(new MenuEvents(this), this);
        manager.registerEvents(new ExploitEvents(this), this);
        if (getConfig().getBoolean("options.protection.enabled", true)) {
            manager.registerEvents(new GriefEvents(this), this);
            if (getConfig().getBoolean("options.protection.item-drops", true)) {
                manager.registerEvents(new ItemDropEvents(this), this);
            }
        }
        if (getConfig().getBoolean("options.island.spawn-limits.enabled", true)) {
            manager.registerEvents(new SpawnEvents(this), this);
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
            setupWorld(skyBlockWorld);
        }
        return uSkyBlock.skyBlockWorld;
    }

    private void setupWorld(World skyWorld) {
        if (Settings.general_spawnSize > 0) {
            if (LocationUtil.isEmptyLocation(skyWorld.getSpawnLocation())) {
                skyWorld.setSpawnLocation(0, Settings.island_height, 0);
            }
            Location worldSpawn = skyWorld.getSpawnLocation();
            if (!isSafeLocation(worldSpawn)) {
                Block spawnBlock = skyWorld.getBlockAt(worldSpawn).getRelative(BlockFace.DOWN);
                spawnBlock.setType(Material.BEDROCK);
                Block air1 = spawnBlock.getRelative(BlockFace.UP);
                air1.setType(Material.AIR);
                air1.getRelative(BlockFace.UP).setType(Material.AIR);
            }
        }
    }

    public static World getSkyBlockWorld() {
        return getInstance().getWorld();
    }

    public Location getSafeHomeLocation(final PlayerInfo p) {
        Location home = findNearestSafeLocation(p.getHomeLocation());
        if (home == null) {
            home = findNearestSafeLocation(p.getIslandLocation());
        }
        return home;
    }

    public Location getSafeWarpLocation(final PlayerInfo p) {
        IslandInfo islandInfo = getIslandInfo(p);
        if (islandInfo != null) {
            Location warp = findNearestSafeLocation(islandInfo.getWarpLocation());
            if (warp == null) {
                warp = findNearestSafeLocation(islandInfo.getIslandLocation());
            }
            return warp;
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
        return ground.getType().isSolid() && isBreathable(air1) && isBreathable(air2);
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
                    if (e instanceof Monster && e.getCustomName() == null) { // Remove all monsters that are not named
                        e.remove();
                    }
                }
            }
        }
    }

    private void postDelete(final PlayerInfo pi) {

        pi.save();
    }

    private void postDelete(final IslandInfo islandInfo) {
        WorldGuardHandler.removeIslandRegion(islandInfo.getName());
        islandLogic.deleteIslandConfig(islandInfo.getName());
        orphanLogic.save();
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
        PlayerInfo pi = playerLogic.getPlayerInfo(player);
        final PlayerInfo finalPI = pi;
        final IslandInfo islandInfo = getIslandInfo(pi);
        Location islandLocation = islandInfo.getIslandLocation();
        for (String member : islandInfo.getMembers()) {
            pi = playerLogic.getPlayerInfo(member);
            islandInfo.removeMember(pi);
        }
        islandLogic.clearIsland(islandLocation, new Runnable() {
            @Override
            public void run() {
                postDelete(finalPI);
                postDelete(islandInfo);
                if (runner != null) runner.run();
            }
        });
    }

    public boolean restartPlayerIsland(final Player player, final Location next) {
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return false;
        }
        final PlayerInfo playerInfo = getPlayerInfo(player);
        if (playerInfo != null) {
            playerInfo.setIslandGenerating(true);
        }
        if (isSkyWorld(player.getWorld())) {
            // Clear first, since the player could log out and we NEED to make sure their inventory gets cleared.
            clearPlayerInventory(player);
            clearEntitiesNearPlayer(player);
        }
        islandLogic.clearIsland(next, new Runnable() {
            @Override
            public void run() {
                generateIsland(player, playerInfo, next);
            }
        });
        return true;
    }

    public void clearPlayerInventory(Player player) {
        getLogger().entering(CN, "clearPlayerInventory", player);
        if (!isSkyWorld(player.getWorld())) {
            getLogger().finer("not clearing, since player is not in skyworld");
            return;
        }
        if (getConfig().getBoolean("options.restart.clearInventory", true)) {
            player.getInventory().clear();
        }
        if (getConfig().getBoolean("options.restart.clearArmor", true)) {
            ItemStack[] armor = player.getEquipment().getArmorContents();
            player.getEquipment().setArmorContents(new ItemStack[armor.length]);
        }
        if (getConfig().getBoolean("options.restart.clearEnderChest", true)) {
            player.getEnderChest().clear();
        }
        getLogger().exiting(CN, "clearPlayerInventory");
    }

    private void clearEntitiesNearPlayer(Player player) {
        getLogger().entering(CN, "clearEntitiesNearPlayer", player);
        for (final Entity entity : player.getNearbyEntities((double) (Settings.island_radius), 255.0, (double) (Settings.island_radius))) {
            if (!validEntity(entity)) {
                entity.remove();
            }
        }
        getLogger().exiting(CN, "clearEntitiesNearPlayer");
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
            for (int y = -30; y <= 30; ++y) {
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

    public synchronized boolean devSetPlayerIsland(final Player sender, final Location l, final String player) {
        final PlayerInfo pi = playerLogic.getPlayerInfo(player);

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
                sender.sendMessage(tr("\u00a74Player is already assigned to this island!"));
                deleteOldIsland = false;
            }
            Runnable resetIsland = new Runnable() {
                @Override
                public void run() {
                    pi.setHomeLocation(null);
                    pi.setHasIsland(true);
                    pi.setIslandLocation(newLoc);
                    pi.setHomeLocation(getSafeHomeLocation(pi));
                    IslandInfo island = islandLogic.createIslandInfo(pi.locationForParty(), player);
                    WorldGuardHandler.updateRegion(sender, island);
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

    public Location getLastIsland() {
        if (lastIsland != null && isSkyWorld(lastIsland.getWorld())) {
            return lastIsland;
        }
        setLastIsland(new Location(getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0));
        return new Location(getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0);
    }

    public void setLastIsland(final Location island) {
        getLastIslandConfig().set("options.general.lastIslandX", island.getBlockX());
        getLastIslandConfig().set("options.general.lastIslandZ", island.getBlockZ());
        saveLastIslandConfig();
        lastIsland = island;
    }

    public boolean homeTeleport(final Player player, boolean force) {
        getLogger().entering(CN, "homeTeleport", player);
        try {
            Location homeSweetHome = null;
            PlayerInfo playerInfo = playerLogic.getPlayerInfo(player);
            if (playerInfo != null) {
                homeSweetHome = getSafeHomeLocation(playerInfo);
            }
            if (homeSweetHome == null) {
                player.sendMessage(tr("\u00a74You are not part of an island. Returning you the spawn area!"));
                spawnTeleport(player);
                return true;
            }
            removeCreatures(homeSweetHome);
            player.sendMessage(tr("\u00a7aTeleporting you to your island."));
            safeTeleport(player, homeSweetHome, force);
            return true;
        } finally {
            getLogger().exiting(CN, "homeTeleport");
        }
    }

    public void safeTeleport(final Player player, final Location homeSweetHome, boolean force) {
        log(Level.FINER, "safeTeleport " + player + " to " + homeSweetHome + (force ? " with force" : ""));
        int delay = getConfig().getInt("options.island.islandTeleportDelay", 5);
        if (player.hasPermission("usb.mod.bypassteleport") || (delay == 0) || force) {
            player.setVelocity(new org.bukkit.util.Vector());
            loadChunkAt(homeSweetHome);
            player.teleport(homeSweetHome);
            player.setVelocity(new org.bukkit.util.Vector());
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", delay));
            getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.setVelocity(new org.bukkit.util.Vector());
                    loadChunkAt(homeSweetHome);
                    player.teleport(homeSweetHome);
                    player.setVelocity(new org.bukkit.util.Vector());
                }
            }, TimeUtil.secondsAsTicks(delay));
        }
    }

    private void loadChunkAt(Location homeSweetHome) {
        if (!homeSweetHome.getWorld().isChunkLoaded(homeSweetHome.getBlockX() >> 4, homeSweetHome.getBlockZ() >> 4)) {
            homeSweetHome.getWorld().loadChunk(homeSweetHome.getBlockX() >> 4, homeSweetHome.getBlockZ() >> 4);
        }
    }

    public boolean warpTeleport(final Player player, final PlayerInfo pi, boolean force) {
        Location warpSweetWarp = null;
        if (pi == null) {
            player.sendMessage(tr("\u00a74That player does not exist!"));
            return true;
        }
        warpSweetWarp = getSafeWarpLocation(pi);
        if (warpSweetWarp == null) {
            player.sendMessage(tr("\u00a74Unable to warp you to that player's island!"));
            return true;
        }
        player.sendMessage(tr("\u00a7aTeleporting you to {0}'s island.", pi.getDisplayName()));
        safeTeleport(player, warpSweetWarp, force);
        return true;
    }

    public void spawnTeleport(final Player player) {
        spawnTeleport(player, false);
    }

    public void spawnTeleport(final Player player, boolean force) {
        getLogger().entering(CN, "spawnTeleport", new Object[]{player});

        int delay = getConfig().getInt("options.island.islandTeleportDelay", 5);
        final Location spawnLocation = getWorld().getSpawnLocation();
        if (player.hasPermission("usb.mod.bypassteleport") || (delay == 0) || force) {
            if (Settings.extras_sendToSpawn) {
                execCommand(player, "op:spawn");
            } else {
                loadChunkAt(spawnLocation);
                player.teleport(spawnLocation);
            }
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", delay));
            getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (Settings.extras_sendToSpawn) {
                        execCommand(player, "op:spawn");
                    } else {
                        loadChunkAt(spawnLocation);
                        player.teleport(spawnLocation);
                    }
                }
            }, TimeUtil.secondsAsTicks(delay));
        }
        getLogger().exiting(CN, "spawnTeleport");
    }

    public boolean homeSet(final Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
            player.sendMessage(tr("\u00a74You must be closer to your island to set your skyblock home!"));
            return true;
        }
        if (playerIsOnIsland(player)) {
            PlayerInfo playerInfo = playerLogic.getPlayerInfo(player);
            if (playerInfo != null && isSafeLocation(player.getLocation())) {
                playerInfo.setHomeLocation(player.getLocation());
                playerInfo.save();
                player.sendMessage(tr("\u00a7aYour skyblock home has been set to your current location."));
            } else {
                player.sendMessage(tr("\u00a74Your current location is not a safe home-location."));
            }
            return true;
        }
        player.sendMessage(tr("\u00a74You must be closer to your island to set your skyblock home!"));
        return true;
    }

    public boolean playerIsOnIsland(final Player player) {
        return locationIsOnIsland(player, player.getLocation()) || playerIsTrusted(player);
    }

    private boolean playerIsTrusted(Player player) {
        String islandName = WorldGuardHandler.getIslandNameAt(player.getLocation());
        if (islandName != null) {
            IslandInfo islandInfo = islandLogic.getIslandInfo(islandName);
            if (islandInfo != null && islandInfo.getTrustees().contains(player.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean locationIsOnIsland(final Player player, final Location loc) {
        if (!isSkyWorld(player.getWorld())) {
            return false;
        }
        PlayerInfo playerInfo = playerLogic.getPlayerInfo(player);
        if (playerInfo != null) {
            Location p = playerInfo.getIslandLocation();
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

    public boolean hasIsland(final Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        return playerInfo != null && playerInfo.getHasIsland();
    }

    public boolean islandAtLocation(final Location loc) {
        return ((WorldGuardHandler.getIntersectingRegions(loc).size() > 0)
                //|| (findBedrockLocation(loc) != null)
                || islandLogic.hasIsland(loc)
        );

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

    public boolean isPurgeActive() {
        return purgeActive;
    }

    public void activatePurge() {
        purgeActive = true;
    }

    public void deactivatePurge() {
        purgeActive = false;
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return playerLogic.getPlayerInfo(player);
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        return playerLogic.getPlayerInfo(playerName);
    }

    public void reloadLastIslandConfig() {
        if (lastIslandConfigFile == null) {
            lastIslandConfigFile = new File(getDataFolder(), "lastIslandConfig.yml");
        }
        lastIslandConfig = YamlConfiguration.loadConfiguration(lastIslandConfigFile);
        final InputStream defConfigStream = getResource("lastIslandConfig.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            lastIslandConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getLastIslandConfig() {
        if (lastIslandConfig == null) {
            reloadLastIslandConfig();
        }
        return lastIslandConfig;
    }

    public void saveLastIslandConfig() {
        if (lastIslandConfig == null || lastIslandConfigFile == null) {
            return;
        }
        try {
            getLastIslandConfig().save(lastIslandConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + lastIslandConfigFile, ex);
        }
    }

    public boolean setBiome(final Location loc, final String bName) {
        Biome biome = getBiome(bName);
        if (biome == null) return false;
        setBiome(loc, biome);
        return true;
    }

    public Biome getBiome(String bName) {
        if (bName == null) return null;
        return validBiomes.get(bName.toLowerCase());
    }

    private void setBiome(Location loc, Biome biome) {
        int r = Settings.island_radius;
        final int px = loc.getBlockX();
        final int pz = loc.getBlockZ();
        for (int x = px - r; x <= px + r; x++) {
            for (int z = pz - r; z <= pz + r; z++) {
                // Set the biome in the world.
                skyBlockWorld.setBiome(x, z, biome);
                // Refresh the chunks so players can see it without relogging!
                // Unfortunately, it doesn't work - though it should (We filed a bug report about it to SPIGOT)
                skyBlockWorld.refreshChunk(x, z);
            }
        }
    }

    public boolean biomeExists(String biomeName) {
        if (biomeName == null) return false;
        return validBiomes.containsKey(biomeName.toLowerCase());
    }

        public boolean changePlayerBiome(Player player, String bName) {
        if (!biomeExists(bName)) throw new UnsupportedOperationException();

        if (!VaultHandler.checkPerk(player.getName(), "usb.biome." + bName, skyBlockWorld)) return false;

        PlayerInfo playerInfo = getPlayerInfo(player);
        IslandInfo islandInfo = islandLogic.getIslandInfo(playerInfo);
        if (islandInfo.hasPerm(player.getName(), "canChangeBiome")) {
            if (!setBiome(playerInfo.getIslandLocation(), bName)) {
                return false;
            }
            islandInfo.setBiome(bName);
            return true;
        }
        return false;
    }

    public boolean createIsland(final Player player, final PlayerInfo pi) {
        getLogger().entering(CN, "createIsland", new Object[]{player, pi});
        try {
            if (isSkyWorld(player.getWorld())) {
                spawnTeleport(player, true);
            }
            if (pi != null) {
                pi.setIslandGenerating(true);
            }
            final Location last = getLastIsland();
            last.setY((double) Settings.island_height);
            try {
                final Location next = getNextIslandLocation(last);
                generateIsland(player, pi, next);
            } catch (Exception ex) {
                player.sendMessage(tr("Could not create your Island. Please contact a server moderator."));
                log(Level.SEVERE, "Error creating island", ex);
                return false;
            }
            log(Level.INFO, "Finished creating player island.");
            return true;
        } finally {
            getLogger().exiting(CN, "createIsland");
        }
    }

    private void generateIsland(final Player player, final PlayerInfo pi, final Location next) {
        final PlayerPerk playerPerk = new PlayerPerk(pi, perkLogic.getPerk(player));
        player.sendMessage(tr("\u00a7eGetting your island ready, please be patient, it can take a while."));
        final Runnable generateTask = new Runnable() {
            @Override
            public void run() {
                next.getWorld().loadChunk(next.getBlockX() >> 4, next.getBlockZ() >> 4, false);
                islandGenerator.setChest(next, playerPerk);
                setNewPlayerIsland(player, next);
                changePlayerBiome(player, "OCEAN");
                WorldGuardHandler.protectIsland(player, pi);
                getCooldownHandler().resetCooldown(player, "restart", Settings.general_cooldownRestart);

                if (pi != null) {
                    pi.setIslandGenerating(false);
                }
                getServer().getScheduler().runTaskLater(uSkyBlock.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (player != null && player.isOnline()) {
                            getLogger().log(Level.FINE, "porting player back to the island");
                            homeTeleport(player, true);
                            clearPlayerInventory(player);
                            clearEntitiesNearPlayer(player);
                        }
                    }
                }, getConfig().getInt("options.restart.teleportDelay", 20));
            }
        };
        Runnable createTask = new Runnable() {
            @Override
            public void run() {
                final String schem = islandGenerator.createIsland(uSkyBlock.this, playerPerk, next);
                Bukkit.getScheduler().runTaskLater(uSkyBlock.this, generateTask, getConfig().getInt("options.advanced.delayAfterIslandCreation." + schem, 0));
            }
        };
        if (orphanLogic.wasOrphan(next)) {
            // Create a WG region to be used for deleting it
            player.sendMessage(tr("\u00a7eYay! We found a vacancy closer to spawn. \u00a79Clearing it for you..."));
            IslandInfo tempInfo = islandLogic.createIslandInfo(LocationUtil.getIslandName(next), pi.getPlayerName());
            WorldGuardHandler.protectIsland(this, player, tempInfo);
            islandLogic.clearIsland(next, createTask);
        } else {
            createTask.run();
        }
    }

    private synchronized Location getNextIslandLocation(Location last) {
        Location next = orphanLogic.getNextValidOrphan();
        if (next == null) {
            next = last;
            // Ensure the found location is valid (or find one that is).
            while (islandInSpawn(next) || islandAtLocation(next)) {
                next = nextIslandLocation(next);
            }
        }
        setLastIsland(next);
        return next;
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

    /**
     * Finds the nearest block to loc that is a chest.
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
        loadChunkAt(loc);
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
        return findNearestSafeLocation(new Location(loc.getWorld(), px, py, pz), loc);
    }

    Location findNearestSafeLocation(Location loc) {
        return findNearestSafeLocation(loc, null);
    }

    Location findNearestSafeLocation(Location loc, Location lookAt) {
        if (loc == null) {
            return null;
        }
        loadChunkAt(loc);
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
                    Location spawnLocation = new Location(world, x, y, z);
                    if (isSafeLocation(spawnLocation)) {
                        // look at the old location
                        spawnLocation = centerOnBlock(spawnLocation);
                        if (lookAt != null) {
                            Location d = centerOnBlock(lookAt).subtract(spawnLocation);
                            spawnLocation.setDirection(d.toVector());
                        } else {
                            spawnLocation.setYaw(loc.getYaw());
                            spawnLocation.setPitch(loc.getPitch());
                        }
                        log(Level.FINER, "found safe location " + spawnLocation + " near " + loc + ", looking at " + lookAt);
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
        setNewPlayerIsland(getPlayerInfo(player), loc);
    }

    private void setNewPlayerIsland(final PlayerInfo playerInfo, final Location loc) {
        playerInfo.startNewIsland(loc);
        
        Location chestSpawnLocation = getChestSpawnLoc(loc);
        if (chestSpawnLocation != null) {
            playerInfo.setHomeLocation(chestSpawnLocation);
        } else {
            log(Level.SEVERE, "Could not find a safe chest within 15 blocks of the island spawn. Bad schematic!");
        }
        IslandInfo info = islandLogic.createIslandInfo(playerInfo.locationForParty(), playerInfo.getPlayerName());
        Player onlinePlayer = playerInfo.getPlayer();
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            info.updatePermissionPerks(onlinePlayer, perkLogic.getPerk(onlinePlayer));
        }
        if (challengeLogic.isResetOnCreate()) {
            playerInfo.resetAllChallenges();
        }
        playerInfo.save();
    }

    public String getCurrentBiome(Player p) {
        return getIslandInfo(p).getBiome();
    }

    public IslandInfo getIslandInfo(Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        return islandLogic.getIslandInfo(playerInfo);
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

    public PerkLogic getPerkLogic() {
        return perkLogic;
    }

    @Override
    public void reloadConfig() {
        reloadConfigs();
    }

    private void reloadConfigs() {
        createFolders();
        HandlerList.unregisterAll(this);
        if (playerLogic != null) {
            playerLogic.shutdown();
        }
        if (islandLogic != null) {
            islandLogic.shutdown();
        }
        VaultHandler.setupEconomy();
        VaultHandler.setupPermissions();
        if (Settings.loadPluginConfig(getConfig())) {
            saveConfig();
        }
        I18nUtil.clearCache();
        // Update all of the loaded configs.
        FileUtil.reload();

        playerDB = new FilePlayerDB(new File(getDataFolder(), "uuid2name.yml"));
        PlayerUtil.loadConfig(playerDB, getConfig());
        islandGenerator = new IslandGenerator(getDataFolder(), getConfig());
        perkLogic = new PerkLogic(this, islandGenerator);
        challengeLogic = new ChallengeLogic(getFileConfiguration("challenges.yml"), this);
        menu = new SkyBlockMenu(this, challengeLogic);
        levelLogic = new LevelLogic(this, getFileConfiguration("levelConfig.yml"));
        orphanLogic = new OrphanLogic(this);
        islandLogic = new IslandLogic(this, directoryIslands, orphanLogic);
        notifier = new PlayerNotifier(getConfig());
        playerLogic = new PlayerLogic(this);
        playerNameChangeManager = new PlayerNameChangeManager(this, playerDB);
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
        confirmHandler = new ConfirmHandler(this, getConfig().getInt("options.advanced.confirmTimeout", 10));
        cooldownHandler = new CooldownHandler(this);
        getCommand("island").setExecutor(new IslandCommand(this, menu));
        getCommand("challenges").setExecutor(new ChallengesCommand(this));
        getCommand("usb").setExecutor(new AdminCommand(this, confirmHandler));
        getCommand("islandtalk").setExecutor(new IslandTalkCommand(this));
        getCommand("partytalk").setExecutor(new PartyTalkCommand(this));
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

    public OrphanLogic getOrphanLogic() {
        return orphanLogic;
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
        Matcher m = Pattern.compile("^\\{p=(?<prob>0?\\.[0-9]+)\\}(.*)$").matcher(command);
        if (m.matches()) {
            double p = Double.parseDouble(m.group("prob"));
            command = m.group(2);
            if (RND.nextDouble() > p) {
                return; // Skip the command
            }
        }
        m = Pattern.compile("^\\{d=(?<delay>[0-9]+)\\}(.*)$").matcher(command);
        int delay = 0;
        if (m.matches()) {
            delay = Integer.parseInt(m.group("delay"));
            command = m.group(2);
        }
        if (command.contains("{party}")) {
            PlayerInfo playerInfo = getPlayerInfo(player);
            for (String member : getIslandInfo(playerInfo).getMembers()) {
                doExecCommand(player, command.replaceAll("\\{party\\}", member), delay);
            }
        } else {
            doExecCommand(player, command, delay);
        }
    }

    private void doExecCommand(final Player player, final String command, int delay) {
        if (delay == 0) {
            Bukkit.getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    doExecCommand(player, command);
                }
            });
        } else if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    doExecCommand(player, command);
                }
            }, delay);
        } else {
            log(Level.INFO, "WARN: Misconfigured command found, with negative delay! " + command);
        }
    }

    private void doExecCommand(Player player, String command) {
        if (command.startsWith("op:")) {
            if (player.isOp()) {
                player.performCommand(command.substring(3).trim());
            } else {
                player.setOp(true);
                // Prevent privilege escalation if called command throws unhandled exception
                try {
                    player.performCommand(command.substring(3).trim());
                } finally {
                    player.setOp(false);
                }
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
        if (!isSkyWorld(pLoc.getWorld())) {
            return false;
        }
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

    @Override
    public IslandRank getIslandRank(Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        return islandLogic != null && playerInfo != null && playerInfo.getHasIsland() ?
                islandLogic.getRank(playerInfo.locationForParty())
                : null;
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

    public synchronized boolean isProtectAllActive() {
        return protectAllActive;
    }

    public synchronized void setProtectAllActive(boolean protectAllActive) {
        this.protectAllActive = protectAllActive;
    }

    public String getVersionInfo(boolean checkEnabled) {
        PluginDescriptionFile description = getDescription();
        String msg = tr("\u00a77Name: \u00a7b{0}\n", description.getName());
        msg += tr("\u00a77Version: \u00a7b{0}\n", description.getVersion());
        msg += tr("\u00a77Description: \u00a7b{0}\n", description.getDescription());
        msg += tr("\u00a77Language: \u00a7b{0} ({1})\n", getConfig().get("language", "en"), I18nUtil.getI18n().getResources().getLocale());
        msg += tr("\u00a77------------------------------\n");
        msg += tr("\u00a77Server: \u00a7e{0} {1}\n", getServer().getName(), getServer().getVersion());
        for (String[] dep : depends) {
            Plugin dependency = getServer().getPluginManager().getPlugin(dep[0]);
            if (dependency != null) {
                msg += tr("\u00a77------------------------------\n");
                msg += tr("\u00a77Name: \u00a7d{0} ({1})\n", dependency.getName(),
                        checkEnabled ? (dependency.isEnabled() ? tr("\u00a72ENABLED") : tr("\u00a74DISABLED")) : tr("N/A"));
                msg += tr("\u00a77Version: \u00a7d{0}\n", dependency.getDescription().getVersion());
            }
        }
        msg += tr("\u00a77------------------------------\n");
        return msg;
    }

    public PlayerDB getPlayerDB() {
        return playerDB;
    }

    public void calculateScoreAsync(final Player player, String islandName, final Callback<IslandScore> callback) {
        final IslandInfo islandInfo = getIslandInfo(islandName);
        getLevelLogic().calculateScoreAsync(islandInfo.getIslandLocation(), new Callback<IslandScore>() {
            @Override
            public void run() {
                IslandScore score = getState();
                callback.setState(score);
                callback.run();
                islandInfo.setLevel(score.getScore());
                getIslandLogic().updateRank(islandInfo, score);
                fireChangeEvent(new uSkyBlockEvent(player, getInstance(), uSkyBlockEvent.Cause.SCORE_CHANGED));
            }
        });
    }

    public ConfirmHandler getConfirmHandler() {
        return confirmHandler;
    }

    public CooldownHandler getCooldownHandler() {
        return cooldownHandler;
    }

    public PlayerLogic getPlayerLogic() {
        return playerLogic;
    }

    public PlayerNameChangeManager getPlayerNameChangeManager() {
        return playerNameChangeManager;
    }

    public Map<String, Biome> getValidBiomes() {
        return validBiomes;
    }
}
