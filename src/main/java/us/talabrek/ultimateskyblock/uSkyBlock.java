package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.challenge.ChallengesCommand;
import us.talabrek.ultimateskyblock.event.PlayerEvents;
import us.talabrek.ultimateskyblock.island.*;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class uSkyBlock extends JavaPlugin {
    private final Map<String, FileConfiguration> configFiles = new ConcurrentHashMap<>();

    private SkyBlockMenu menu;
    private ChallengeLogic challengeLogic;
    private LevelLogic levelLogic;
    private IslandLogic islandLogic;

    private static String pName = "";
    private FileConfiguration lastIslandConfig;
    private FileConfiguration orphans;
    private File orphanFile;
    private File lastIslandConfigFile;
    public static volatile World skyBlockWorld;
    private static uSkyBlock instance;
    public List<String> removeList;
    private Location lastIsland;
    private Stack<Location> orphaned;
    private Stack<Location> tempOrphaned;
    private Stack<Location> reverseOrphaned;
    public File directoryPlayers;
    public File directoryIslands;
    public File[] schemFile;
    Map<String, Long> infoCooldown;
    Map<String, Long> restartCooldown;
    Map<String, Long> biomeCooldown;
    private final Map<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();
    public boolean purgeActive;

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
        this.removeList = new ArrayList<>();
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
            if (this.lastIsland != null) {
                this.setLastIsland(this.lastIsland);
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
                // TODO: 09/12/2014 - R4zorax: Also replace + backup if jar-version is newer than local version
                if (!configFile.exists()) {
                    try (InputStream in = getClassLoader().getResourceAsStream(configName)) {
                        // copy from jar
                        Files.copy(in, Paths.get(configFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log(Level.WARNING, "Unable to create config file " + configFile, e);
                    }
                }
                if (configFile.exists()) {
                    // FORCE utf8 - don't rely on super.getConfig() or FileConfiguration.load()
                    readConfig(config, configFile);
                }
            } catch (Exception e) {
                log(Level.SEVERE, "Unable to handle config-file " + configName, e);
            }
            configFiles.put(configName, config);
        }
        return configFiles.get(configName);
    }

    public static void readConfig(FileConfiguration config, File configFile) {
        try (Reader rdr = new InputStreamReader(new FileInputStream(configFile), "UTF-8")) {
            config.load(rdr);
        } catch (InvalidConfigurationException | IOException e) {
            log(Level.SEVERE, "Unable to read config file " + configFile, e);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return getFileConfiguration("config.yml");
    }

    public void onEnable() {
        instance = this;
        configFiles.clear();
        activePlayers.clear();
        createFolders();
        uSkyBlock.pName = "[" + getDescription().getName() + "] ";
        VaultHandler.setupEconomy();
        if (Settings.loadPluginConfig(getConfig())) {
            saveConfig();
        }

        this.challengeLogic = new ChallengeLogic(getFileConfiguration("challenges.yml"), this);
        this.menu = new SkyBlockMenu(this, challengeLogic);
        this.levelLogic = new LevelLogic(getFileConfiguration("levelConfig.yml"));
        this.islandLogic = new IslandLogic(this, directoryIslands);

        registerEvents();
        this.getCommand("island").setExecutor(new IslandCommand());
        this.getCommand("challenges").setExecutor(new ChallengesCommand());
        this.getCommand("usb").setExecutor(new DevCommand());

        getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                    log(Level.INFO, "Using vault for permissions");
                    VaultHandler.setupPermissions();
                    try {
                        FileConfiguration config = getLastIslandConfig();
                        if (!config.contains("options.general.lastIslandX") && uSkyBlock.this.getConfig().contains("options.general.lastIslandX")) {
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
            }
        }, 0L);
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

    // TODO: UUID support
    public void unloadPlayerFiles() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (this.getActivePlayers().containsKey(player.getName())) {
                this.removeActivePlayer(player.getName());
            }
        }
    }

    public void registerEvents() {
        final PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new PlayerEvents(this), this);
    }

    public World getWorld() {
        if (uSkyBlock.skyBlockWorld == null) {
            skyBlockWorld = Bukkit.getWorld(Settings.general_worldName);
            if (skyBlockWorld == null) {
                uSkyBlock.skyBlockWorld = WorldCreator
                        .name(Settings.general_worldName)
                        .type(WorldType.NORMAL)
                        .generateStructures(false)
                        .environment(World.Environment.NORMAL)
                        .generator(new SkyBlockChunkGenerator())
                        .createWorld();
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv import " + Settings.general_worldName + " normal -g uSkyBlock");
                }
                uSkyBlock.skyBlockWorld.save();
            }
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
        return !ground.getType().equals(Material.AIR) && !ground.getType().equals(Material.LAVA) && !ground.getType().equals(Material.STATIONARY_LAVA) && !ground.getType().equals(Material.CACTUS) && ((air1.getType().equals(Material.AIR) || air1.getType().equals(Material.CROPS) || air1.getType().equals(Material.LONG_GRASS) || air1.getType().equals(Material.RED_ROSE) || air1.getType().equals(Material.YELLOW_FLOWER) || air1.getType().equals(Material.DEAD_BUSH) || air1.getType().equals(Material.SIGN_POST) || air1.getType().equals(Material.SIGN)) && air2.getType().equals(Material.AIR));
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

    public void deletePlayerIsland(final String player) {
        final PlayerInfo pi = activePlayers.containsKey(player) ? activePlayers.get(player) : new PlayerInfo(player);
        orphaned.push(pi.getIslandLocation());
        islandLogic.clearIsland(pi.getIslandLocation());
        RegionManager regionManager = WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld());
        regionManager.removeRegion(player + "Island");
        String islandLocation = pi.locationForParty();
        islandLogic.deleteIslandConfig(islandLocation);
        pi.removeFromIsland();
        pi.save();
        removeActivePlayer(player);
        saveOrphans();
        // TODO: 17/12/2014 - R4zorax: Teleport players on the island
    }

    public boolean restartPlayerIsland(final Player player, final Location next) {
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return false;
        }
        try {
            islandLogic.clearIsland(next);
            createIsland(player, next);
            changePlayerBiome(player, "OCEAN");
            next.setY((double) Settings.island_height);
            setNewPlayerIsland(player, next);
            clearPlayerInventory(player);
            clearEntitiesNearPlayer(player);
            setRestartCooldown(player);
            player.sendMessage(ChatColor.RED + "Close your eyes!");
            getServer().getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    player.performCommand("spawn");
                    player.sendMessage(ChatColor.YELLOW + "Warping you to your new island!" + ChatColor.GREEN + " Get ready!");
                    getServer().getScheduler().runTaskLater(uSkyBlock.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            uSkyBlock.getInstance().homeTeleport(player);
                        }
                    }, 10);
                }
            }, 10);
            return true;
        } catch (Exception e) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            e.printStackTrace();
            return false;
        }
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

    public boolean devSetPlayerIsland(final Player sender, final Location l, final String player) {
        if (!this.getActivePlayers().containsKey(player)) {
            final PlayerInfo pi = new PlayerInfo(player);
            final int px = l.getBlockX();
            final int py = l.getBlockY();
            final int pz = l.getBlockZ();
            for (int x = -10; x <= 10; ++x) {
                for (int y = -10; y <= 10; ++y) {
                    for (int z = -10; z <= 10; ++z) {
                        final Block b = new Location(l.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                        if (b.getTypeId() == 7) {
                            pi.setHomeLocation(new Location(l.getWorld(), (double) (px + x), (double) (py + y + 3), (double) (pz + z)));
                            pi.setHasIsland(true);
                            pi.setIslandLocation(b.getLocation());
                            pi.save();
                            islandLogic.createIsland(pi.locationForParty(), player);
                            if (!WorldGuardHandler.protectIsland(sender, player, pi)) {
                                sender.sendMessage("Player doesn't have an island or it's already protected!");
                            }
                            return true;
                        }
                    }
                }
            }
        } else {
            final int px2 = l.getBlockX();
            final int py2 = l.getBlockY();
            final int pz2 = l.getBlockZ();
            for (int x2 = -10; x2 <= 10; ++x2) {
                for (int y2 = -10; y2 <= 10; ++y2) {
                    for (int z2 = -10; z2 <= 10; ++z2) {
                        final Block b2 = new Location(l.getWorld(), (double) (px2 + x2), (double) (py2 + y2), (double) (pz2 + z2)).getBlock();
                        if (b2.getTypeId() == 7) {
                            PlayerInfo playerInfo = this.getActivePlayers().get(player);
                            playerInfo.setHomeLocation(new Location(l.getWorld(), (double) (px2 + x2), (double) (py2 + y2 + 3), (double) (pz2 + z2)));
                            playerInfo.setHasIsland(true);
                            playerInfo.setIslandLocation(b2.getLocation());
                            removeActivePlayer(player);
                            addActivePlayer(player, playerInfo);
                            WorldGuardHandler.protectIsland(sender, player, playerInfo);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int orphanCount() {
        return orphaned.size();
    }

    public Location getLastIsland() {
        if (this.lastIsland.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            return this.lastIsland;
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
        orphaned.push(island);
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
            player.sendMessage(ChatColor.RED + "You are not part of an island. Returning you the spawn area!");
            return true;
        }
        removeCreatures(homeSweetHome);
        player.teleport(homeSweetHome);
        player.sendMessage(ChatColor.GREEN + "Teleporting you to your island.");
        return true;
    }

    public boolean warpTeleport(final Player player, final PlayerInfo pi) {
        Location warpSweetWarp = null;
        if (pi == null) {
            player.sendMessage(ChatColor.RED + "That player does not exist!");
            return true;
        }
        warpSweetWarp = getSafeWarpLocation(pi);
        if (warpSweetWarp == null) {
            player.sendMessage(ChatColor.RED + "Unable to warp you to that player's island!");
            return true;
        }
        player.teleport(warpSweetWarp);
        player.sendMessage(ChatColor.GREEN + "Teleporting you to " + pi.getPlayerName() + "'s island.");
        return true;
    }

    public boolean homeSet(final Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
            player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
            return true;
        }
        if (this.playerIsOnIsland(player)) {
            if (this.getActivePlayers().containsKey(player.getName())) {
                this.getActivePlayers().get(player.getName()).setHomeLocation(player.getLocation());
            }
            player.sendMessage(ChatColor.GREEN + "Your skyblock home has been set to your current location.");
            return true;
        }
        player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
        return true;
    }

    public boolean warpSet(final Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
            player.sendMessage(ChatColor.RED + "You must be closer to your island to set your warp!");
            return true;
        }
        if (this.playerIsOnIsland(player)) {
            if (this.getActivePlayers().containsKey(player.getName())) {
                islandLogic.getIslandInfo(getPlayerInfo(player)).setWarpLocation(player.getLocation());
            }
            player.sendMessage(ChatColor.GREEN + "Your skyblock incoming warp has been set to your current location.");
            return true;
        }
        player.sendMessage(ChatColor.RED + "You must be closer to your island to set your warp!");
        return true;
    }

    public boolean homeSet(final String player, final Location loc) {
        if (this.getActivePlayers().containsKey(player)) {
            this.getActivePlayers().get(player).setHomeLocation(loc);
        } else {
            final PlayerInfo pi = new PlayerInfo(player);
            pi.setHomeLocation(loc);
            pi.save();
        }
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
        if (loc == null) {
            return true;
        }
        final int px = loc.getBlockX();
        final int py = loc.getBlockY();
        final int pz = loc.getBlockZ();
        for (int x = -2; x <= 2; ++x) {
            for (int y = -50; y <= 50; ++y) {
                for (int z = -2; z <= 2; ++z) {
                    final Block b = new Location(loc.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                    if (b.getTypeId() != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean islandInSpawn(final Location loc) {
        return loc == null || (loc.getX() > -50.0 && loc.getX() < 50.0 && loc.getZ() > -50.0 && loc.getZ() < 50.0);
    }

    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        return new SkyBlockChunkGenerator();
    }

    public boolean onInfoCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.infoCooldown")
                && infoCooldown.containsKey(player.getName())
                && infoCooldown.get(player.getName()) > System.currentTimeMillis();
    }

    public boolean onBiomeCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.biomeCooldown")
                && biomeCooldown.containsKey(player.getName())
                && biomeCooldown.get(player.getName()) > System.currentTimeMillis();
    }

    public boolean onRestartCooldown(final Player player) {
        return !player.hasPermission("usb.exempt.restartCooldown")
                && restartCooldown.containsKey(player.getName())
                && this.restartCooldown.get(player.getName()) > System.currentTimeMillis();
    }

    public long getInfoCooldownTime(final Player player) {
        if (!infoCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (infoCooldown.get(player.getName()) > System.currentTimeMillis()) {
            return infoCooldown.get(player.getName()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public long getBiomeCooldownTime(final Player player) {
        if (!biomeCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (biomeCooldown.get(player.getName()) > System.currentTimeMillis()) {
            return biomeCooldown.get(player.getName()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public long getRestartCooldownTime(final Player player) {
        if (!this.restartCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (this.restartCooldown.get(player.getName()) > System.currentTimeMillis()) {
            return this.restartCooldown.get(player.getName()) - System.currentTimeMillis();
        }
        return 0L;
    }

    public void setInfoCooldown(final Player player) {
        infoCooldown.put(player.getName(), System.currentTimeMillis() + Settings.general_cooldownInfo * 1000);
    }

    public void setBiomeCooldown(final Player player) {
        biomeCooldown.put(player.getName(), System.currentTimeMillis() + Settings.general_biomeChange * 1000);
    }

    public void setRestartCooldown(final Player player) {
        this.restartCooldown.put(player.getName(), System.currentTimeMillis() + Settings.general_cooldownRestart * 1000);
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

    public List<String> getRemoveList() {
        return this.removeList;
    }

    public void addToRemoveList(final String string) {
        this.removeList.add(string);
    }

    public void deleteFromRemoveList() {
        this.removeList.remove(0);
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
            playerInfo.setDisplayName(player.getDisplayName());
        }
        return playerInfo;
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        PlayerInfo playerInfo = activePlayers.get(playerName);
        if (playerInfo == null) {
            playerInfo = loadPlayerAndIsland(playerName);
            activePlayers.put(playerName, playerInfo);
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
            log(Level.INFO, "Removing player from memory: " + player);
        }
    }

    public PlayerInfo loadPlayerData(Player player) {
        final PlayerInfo pi = loadPlayerAndIsland(player.getName());
        if (pi.getHasIsland()) {
            IslandInfo islandInfo = islandLogic.getIslandInfo(pi);
            WorldGuardHandler.protectIsland(player, islandInfo.getLeader(), pi);
        }
        addActivePlayer(player.getName(), pi);
        uSkyBlock.log(Level.INFO, "Loaded player file for " + player.getName());
        return pi;
    }

    private PlayerInfo loadPlayerAndIsland(String playerName) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName);
        activePlayers.put(playerName, playerInfo);
        return playerInfo;
    }

    public void unloadPlayerData(Player player) {
        if (hasIsland(player.getName()) && !hasIslandMembersOnline(player)) {
            islandLogic.removeIslandFromMemory(getPlayerInfo(player).locationForParty());
        }
        removeActivePlayer(player.getName());
    }



    public void reloadIslandConfig(final String location) {
        islandLogic.reloadIsland(location);
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
        for (int x = px-r; x <= px+r; x++) {
            for (int z = pz-r; z <= pz+r; z++) {
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
        player.sendMessage(ChatColor.YELLOW + "You have access to the following Biomes:");
        player.sendMessage(ChatColor.GREEN + biomeList.substring(0, biomeList.length() - 2));
        player.sendMessage(ChatColor.YELLOW + "Use /island biome <biomename> to change your biome. You must wait " + Settings.general_biomeChange / 60 + " minutes between each biome change.");
    }

    public boolean createIsland(final CommandSender sender, final PlayerInfo pi) {
        log(Level.INFO, "Creating player island...");
        final Player player = (Player) sender;
        final Location last = getLastIsland();
        last.setY((double) Settings.island_height);
        try {
            Location next = getNextIslandLocation(last);
            createIsland(player, next);
            setNewPlayerIsland(player, next);
            player.getInventory().clear();
            player.getEquipment().clear();
            changePlayerBiome(player, "OCEAN");
            clearEntitiesNearPlayer(player);
            protectWithWorldGuard(sender, player, pi);
        } catch (Exception ex) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            ex.printStackTrace();
            return false;
        }
        log(Level.INFO, "Finished creating player island.");
        return true;
    }

    private void protectWithWorldGuard(CommandSender sender, Player player, PlayerInfo pi) {
        if (!WorldGuardHandler.protectIsland(player, sender.getName(), pi)) {
            sender.sendMessage("Player doesn't have an island or it's already protected!");
        }
    }

    private void createIsland(Player player, Location next) throws DataException, IOException, MaxChangedBlocksException {
        boolean hasIslandNow = false;
        if (getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            for (File schemFile : getSchemFile()) {
                // First run-through - try to set the island the player has permission for.
                String cSchem = schemFile.getName();
                if (cSchem.lastIndexOf('.') > 0) {
                    cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                }
//                if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, skyBlockWorld)
//                        && WorldEditHandler.loadIslandSchematic(player, skyBlockWorld, schemFile, next)) {
              if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, skyBlockWorld)
            		  && WorldEditHandler.loadIslandSchematic(skyBlockWorld, schemFile, next)) {
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
//                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)
//                            && WorldEditHandler.loadIslandSchematic(player, skyBlockWorld, schemFile, next)) {
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)
                            && WorldEditHandler.loadIslandSchematic(skyBlockWorld, schemFile, next)) {
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

    // TODO: 13/12/2014 - R4zorax: Move island logic somewhere else... (IslandLogic comes to mind)
    private Location getNextIslandLocation(Location last) {
        while (getInstance().hasOrphanedIsland()) {
            if (!getInstance().islandAtLocation(getInstance().checkOrphan())) {
                break;
            }
            removeNextOrphan();
        }
        while (getInstance().hasOrphanedIsland() && !getInstance().checkOrphan().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            removeNextOrphan();
        }
        Location next;
        if (getInstance().hasOrphanedIsland() && !getInstance().islandAtLocation(getInstance().checkOrphan())) {
            next = getOrphanedIsland();
            saveOrphans();
        } else {
            next = nextIslandLocation(last);
            setLastIsland(next);
            while (islandAtLocation(next)) {
                next = nextIslandLocation(next);
            }
        }
        while (islandInSpawn(next)) {
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
                    int x = px + (dx % 2 == 0 ? dx/2 : -dx/2);
                    int z = pz + (dz % 2 == 0 ? dz/2 : -dz/2);
                    int y = py + (dy % 2 == 0 ? dy/2 : -dy/2);
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
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx/2 : -dx/2);
                    int z = pz + (dz % 2 == 0 ? dz/2 : -dz/2);
                    int y = py + (dy % 2 == 0 ? dy/2 : -dy/2);
                    if (world.getBlockAt(x, y, z).getType() == Material.AIR
                            && world.getBlockAt(x, y+1, z).getType() == Material.AIR
                            && world.getBlockAt(x, y-1, z).getType().isSolid()) {
                        // look at the old location
                        Location spawnLocation = new Location(world, x, y, z);
                        Location d = loc.clone().subtract(spawnLocation);
                        spawnLocation.setDirection(d.toVector());
                        return spawnLocation;
                    }
                }
            }
        }
        return null;
    }

    public Location getChestSpawnLoc(final Location loc, final Player player) {
        return findNearestSpawnLocation(findChestLocation(loc));
    }

    private void setNewPlayerIsland(final Player player, final Location loc) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        playerInfo.startNewIsland(loc);
        player.teleport(getChestSpawnLoc(loc, player).add(0.5, 0.1, 0.5)); // Center on block.
        IslandInfo info = islandLogic.createIsland(playerInfo.locationForParty(), player.getName());
        info.updatePartyNumber(player);
        homeSet(player);
        playerInfo.resetAllChallenges();
        playerInfo.save();
    }

    public void buildIslandList() {
        final File folder = directoryPlayers;
        final File[] listOfFiles = folder.listFiles();
        log(Level.INFO, "Building a new island list...");
        for (int i = 0; i < listOfFiles.length; ++i) {
            final PlayerInfo pi = new PlayerInfo(listOfFiles[i].getName());
            if (pi.getHasIsland()) {
                log(Level.INFO, "Creating new island file for " + pi.getPlayerName());
                islandLogic.createIsland(pi.locationForParty(), pi.getPlayerName());
            }
        }
        // TODO: 15/12/2014 - R4zorax: Support reading the old-format (at some time)
        log(Level.INFO, "Party list completed.");
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

    public void sendMessageToIslandGroup(final String location, final String message) {
        IslandInfo islandInfo = getIslandInfo(location);
        islandInfo.sendMessageToIslandGroup(message);
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

    public static String correctFormatting(String format) {
        if (format == null || format.trim().isEmpty()) {
            return "";
        }
        return format.replaceAll("&([0-9a-fklmor])", "\u00a7$1");
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    public static void log(Level level, String message, Throwable t) {
        getInstance().getLogger().log(level, pName + message, t);
    }

    public ChallengeLogic getChallengeLogic() {
        return challengeLogic;
    }

    public LevelLogic getLevelLogic() {
        return levelLogic;
    }

    @Override
    public void reloadConfig() {
        reloadConfigs();
        Settings.loadPluginConfig(getConfig());
    }

    private void reloadConfigs() {
        // Update all of the loaded configs.
        for (Map.Entry<String, FileConfiguration> e : configFiles.entrySet()){
            File configFile = new File(getDataFolder(), e.getKey());
            readConfig(e.getValue(), configFile);
        }

        this.challengeLogic = new ChallengeLogic(getFileConfiguration("challenges.yml"), this);
        this.menu = new SkyBlockMenu(this, challengeLogic);
        this.levelLogic = new LevelLogic(getFileConfiguration("levelConfig.yml"));
        this.islandLogic = new IslandLogic(this, directoryIslands);
    }

    public boolean isSkyWorld(World world) {
        if (world == null) {
            return false;
        }
        return skyBlockWorld.getName().equalsIgnoreCase(world.getName());
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
}
