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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import us.talabrek.ultimateskyblock.island.IslandCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandLogic;
import us.talabrek.ultimateskyblock.island.LevelLogic;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
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
    Date date;
    private FileConfiguration lastIslandConfig;
    private FileConfiguration orphans;
    private HashMap<String, FileConfiguration> islands;
    private File orphanFile;
    private File lastIslandConfigFile;
    public static World skyBlockWorld;
    private static uSkyBlock instance;
    public List<String> removeList;
    private Location lastIsland;
    private Stack<Location> orphaned;
    private Stack<Location> tempOrphaned;
    private Stack<Location> reverseOrphaned;
    public File directoryPlayers;
    public File directoryIslands;
    public File[] schemFile;
    LinkedHashMap<String, Double> topTen;
    Map<String, Long> infoCooldown;
    Map<String, Long> restartCooldown;
    Map<String, Long> biomeCooldown;
    private final Map<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();
    LinkedHashMap<String, List<String>> challenges;
    HashMap<Integer, Integer> requiredList;
    public boolean purgeActive;

    static {
        uSkyBlock.skyBlockWorld = null;
    }

    public uSkyBlock() {
        // TODO: 08/12/2014 - R4zorax: Most of these should be converted to local variables
        configFiles.clear();
        this.lastIslandConfig = null;
        this.orphans = null;
        this.islands = new HashMap<>();
        this.orphanFile = null;
        this.lastIslandConfigFile = null;
        this.removeList = new ArrayList<>();
        this.orphaned = new Stack<>();
        this.tempOrphaned = new Stack<>();
        this.reverseOrphaned = new Stack<>();
        this.infoCooldown = new HashMap<>();
        this.restartCooldown = new HashMap<>();
        this.biomeCooldown = new HashMap<>();
        this.challenges = new LinkedHashMap<>();
        this.requiredList = new HashMap<>();
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
        this.islandLogic = new IslandLogic(this);

        registerEvents();
        this.getCommand("island").setExecutor(new IslandCommand());
        this.getCommand("challenges").setExecutor(new ChallengesCommand());
        this.getCommand("usb").setExecutor(new DevCommand());
        if (Settings.island_useTopTen) {
            updateTopTen(getInstance().generateTopTen());
        }

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

    public boolean displayTopTen(final CommandSender sender) {
        int i = 1;
        int playerrank = 0;
        sender.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");
        if (topTen == null) {
            sender.sendMessage(ChatColor.RED + "Top ten list not generated yet!");
            return false;
        }
        for (final String playerName : topTen.keySet()) {
            if (i <= 10) {
                sender.sendMessage(String.format(ChatColor.GREEN + "#%2d: %s - Island level %5.2f", i, playerName, topTen.get(playerName)));
            }
            if (playerName != null && playerName.equalsIgnoreCase(sender.getName())) {
                playerrank = i;
            }
            ++i;
        }
        if (playerrank > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank);
        }
        return true;
    }

    public void updateTopTen(final LinkedHashMap<String, Double> map) {
        this.topTen = map;
    }

    public World getWorld() {
        if (uSkyBlock.skyBlockWorld == null) {
            skyBlockWorld = getServer().getWorld(getConfig().getString("options.general.worldName", "skyworld"));
            if (skyBlockWorld == null) {
                uSkyBlock.skyBlockWorld = WorldCreator
                        .name(Settings.general_worldName)
                        .type(WorldType.NORMAL)
                        .environment(World.Environment.NORMAL)
                        .generator(new SkyBlockChunkGenerator())
                        .createWorld();
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv import " + Settings.general_worldName + " normal uSkyBlock");
                }
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
        Location home;
        if (p.getHomeLocation() != null) {
            home = p.getHomeLocation();
        } else if (p.getIslandLocation() != null) {
            home = p.getIslandLocation();
        } else {
            home = p.getHomeLocation();
        }
        if (this.isSafeLocation(home)) {
            return home;
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
        removeIsland(pi.getIslandLocation());
        RegionManager regionManager = WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld());
        regionManager.removeRegion(player + "Island");
        String islandLocation = pi.locationForParty();
        deleteIslandConfig(islandLocation);
        pi.removeFromIsland();
        pi.save();
        removeActivePlayer(player);
        this.saveOrphans();
    }

    public boolean restartPlayerIsland(final Player player, final Location next) {
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return false;
        }
        try {
            removeIsland(next);
            createIsland(player, next);
            next.setY((double) Settings.island_height);
            setNewPlayerIsland(player, next);
            clearPlayerInventory(player);
            changePlayerBiome(player, "OCEAN");
            clearEntitiesNearPlayer(player);
            setRestartCooldown(player);
            return true;
        } catch (Exception e) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            e.printStackTrace();
            return false;
        }
    }

    public void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        ItemStack[] armor = player.getEquipment().getArmorContents();
        player.getEquipment().setArmorContents(new ItemStack[armor.length]);
        player.getEnderChest().clear();
    }

    private void clearEntitiesNearPlayer(Player player) {
        for (final Entity tempent : player.getNearbyEntities((double) (Settings.island_protectionRange / 2), 250.0, (double) (Settings.island_protectionRange / 2))) {
            if (!(tempent instanceof Player)) {
                tempent.remove();
            }
        }
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
                            createIslandConfig(pi.locationForParty(), player);
                            clearIslandConfig(pi.locationForParty(), player);
                            if (!WorldGuardHandler.protectIsland(sender, player, pi)) {
                                sender.sendMessage("Player doesn't have an island or it's already protected!");
                            }
                            getIslandConfig(pi.locationForParty());
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

    public void removeIsland(final Location loc) {
        islandLogic.clearIsland(loc);
    }

    public boolean hasParty(final String playername) {
        if (this.getActivePlayers().containsKey(playername)) {
            return this.getIslandConfig(this.getActivePlayers().get(playername).locationForParty()).getInt("party.currentSize") > 1;
        }
        final PlayerInfo pi = new PlayerInfo(playername);
        return pi.getHasIsland() && this.getTempIslandConfig(pi.locationForParty()).getInt("party.currentSize") > 1;
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
                this.setWarpLocation(this.getActivePlayers().get(player.getName()).locationForParty(), player.getLocation());
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

    public LinkedHashMap<String, Double> generateTopTen() {
        final HashMap<String, Double> tempMap = new HashMap<>();
        final File folder = this.directoryIslands;
        final File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; ++i) {
            FileConfiguration islandConfig = this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", ""));
            if (islandConfig != null && islandConfig.getInt("general.level") > 0) {
                String partyLeader = islandConfig.getString("party.leader");
                PlayerInfo pi = getPlayerInfo(partyLeader);
                if (pi != null) {
                    tempMap.put(pi.getDisplayName(), islandConfig.getDouble("general.level"));
                } else {
                    tempMap.put(partyLeader, islandConfig.getDouble("general.level"));
                }
            }
        }
        TreeMap<String, Double> sortedMap = new TreeMap<>(new TopTenComparator(tempMap));
        sortedMap.putAll(tempMap);
        return new LinkedHashMap<>(sortedMap);
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
            FileConfiguration islandConfig = getIslandConfig(pi);
            WorldGuardHandler.protectIsland(player, islandConfig.getString("party.leader"), pi);
        }
        addActivePlayer(player.getName(), pi);
        uSkyBlock.log(Level.INFO, "Loaded player file for " + player.getName());
        return pi;
    }

    private PlayerInfo loadPlayerAndIsland(String playerName) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName);
        activePlayers.put(playerName, playerInfo);
        if (playerInfo.getHasIsland()) {
            FileConfiguration islandConfig = getIslandConfig(playerInfo.locationForParty());
            if (islandConfig == null) {
                uSkyBlock.log(Level.INFO, "Creating new Island-config File");
                createIslandConfig(playerInfo.locationForParty(), playerName);
                clearIslandConfig(playerInfo.locationForParty(), playerName);
            }
        }
        return playerInfo;
    }

    public void unloadPlayerData(Player player) {
        if (hasIsland(player.getName()) && !hasIslandMembersOnline(player)) {
            removeIslandConfig(getActivePlayers().get(player.getName()).locationForParty());
        }
        removeActivePlayer(player.getName());
    }

    public void clearIslandConfig(final String location, final String leader) {
        FileConfiguration islandConfig = this.getIslandConfig(location);
        islandConfig.set("general.level", 0);
        islandConfig.set("general.warpLocationX", 0);
        islandConfig.set("general.warpLocationY", 0);
        islandConfig.set("general.warpLocationZ", 0);
        islandConfig.set("general.warpActive", false);
        islandConfig.set("log.logPos", 1);
        islandConfig.set("log.1", "\u00a7d[skyblock] The island has been created.");
        this.setupPartyLeader(location, leader);
    }

    public void setupPartyLeader(final String location, final String leader) {
        FileConfiguration islandConfig = this.getIslandConfig(location);
        ConfigurationSection section = islandConfig.createSection("party.members." + leader);
        FileConfiguration.createPath(section, "canChangeBiome");
        FileConfiguration.createPath(section, "canToggleLock");
        FileConfiguration.createPath(section, "canChangeWarp");
        FileConfiguration.createPath(section, "canToggleWarp");
        FileConfiguration.createPath(section, "canInviteOthers");
        FileConfiguration.createPath(section, "canKickOthers");
        islandConfig.set("party.leader", leader);
        islandConfig.set("party.members." + leader + ".canChangeBiome", true);
        islandConfig.set("party.members." + leader + ".canToggleLock", true);
        islandConfig.set("party.members." + leader + ".canChangeWarp", true);
        islandConfig.set("party.members." + leader + ".canToggleWarp", true);
        islandConfig.set("party.members." + leader + ".canInviteOthers", true);
        islandConfig.set("party.members." + leader + ".canKickOthers", true);
        this.saveIslandConfig(location);
    }

    public void setupPartyMember(final String location, final String member) {
        FileConfiguration islandConfig = this.getIslandConfig(location);
        ConfigurationSection section = islandConfig.createSection("party.members." + member);
        FileConfiguration.createPath(section, "canChangeBiome");
        FileConfiguration.createPath(section, "canToggleLock");
        FileConfiguration.createPath(section, "canChangeWarp");
        FileConfiguration.createPath(section, "canToggleWarp");
        FileConfiguration.createPath(section, "canInviteOthers");
        FileConfiguration.createPath(section, "canKickOthers");
        islandConfig.set("party.members." + member + ".canChangeBiome", false);
        islandConfig.set("party.currentSize", islandConfig.getInt("party.currentSize") + 1);
        islandConfig.set("party.members." + member + ".canToggleLock", false);
        islandConfig.set("party.members." + member + ".canChangeWarp", false);
        islandConfig.set("party.members." + member + ".canToggleWarp", false);
        islandConfig.set("party.members." + member + ".canInviteOthers", false);
        islandConfig.set("party.members." + member + ".canKickOthers", false);
        islandConfig.set("party.members." + member + ".canBanOthers", false);
        this.saveIslandConfig(location);
    }

    public void reloadIslandConfig(final String location) {
        File islandConfigFile = new File(directoryIslands, location + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(islandConfigFile);
        islands.put(location, configuration);
        final InputStream defConfigStream = this.getResource("island.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            configuration.setDefaults(defConfig);
        }
        saveIslandConfig(location);
    }

    public FileConfiguration getTempIslandConfig(final String location) {
        File islandFile = new File(this.directoryIslands, location + ".yml");
        return YamlConfiguration.loadConfiguration(islandFile);
    }

    public void createIslandConfig(final String location, final String leader) {
        this.saveDefaultIslandsConfig(location);
        final InputStream defConfigStream = this.getResource("island.yml");
        if (defConfigStream != null) {
            this.islands.put(location, YamlConfiguration.loadConfiguration(defConfigStream));
            this.getIslandConfig(location);
            this.setupPartyLeader(location, leader);
        }
    }

    public FileConfiguration getIslandConfig(final Player player) {
        return getIslandConfig(getPlayerInfo(player).locationForParty());
    }

    public FileConfiguration getIslandConfig(final PlayerInfo info) {
        return getIslandConfig(info.locationForParty());
    }

    public FileConfiguration getIslandConfig(final String location) {
        if (islands.get(location) == null) {
            reloadIslandConfig(location);
        }
        return islands.get(location);
    }

    public void saveIslandConfig(final String location) {
        if (islands.get(location) == null) {
            return;
        }
        File file = new File(this.directoryIslands, location + ".yml");
        try {
            getIslandConfig(location).save(file);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + file, ex);
        }
    }

    public void deleteIslandConfig(final String location) {
        File file = new File(this.directoryIslands, location + ".yml");
        file.delete();
        removeIslandConfig(location);
    }

    public void saveDefaultIslandsConfig(final String location) {
        File file = new File(this.directoryIslands, location + ".yml");
        try {
            getIslandConfig(location).save(file);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + file, ex);
        }
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
        for (int x = px - r; x <= px + r + 16; x += 16) {
            for (int z = pz - r; z <= pz + r + 16; z += 16) {
                Chunk chunk = skyBlockWorld.getChunkAt(x, z);
                chunk.load();
                skyBlockWorld.setBiome(chunk.getX(), chunk.getZ(), biome);
                skyBlockWorld.refreshChunk(chunk.getX(), chunk.getZ());
            }
        }
    }

    public boolean changePlayerBiome(final Player player, final String bName) {
        if (!VaultHandler.checkPerk(player.getName(), "usb.biome." + bName, player.getWorld())) {
            return false;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getBoolean("party.members." + player.getName() + ".canChangeBiome")) {
            setBiome(getPlayerInfo(player).getIslandLocation(), bName);
            setConfigBiome(player, bName);
            return true;
        }
        return false;
    }

    public void listBiomes(final Player player) {
        String biomeList = ", ";
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean", getSkyBlockWorld())) {
            biomeList = "OCEAN, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.forest", getSkyBlockWorld())) {
            biomeList = biomeList + "FOREST, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", getSkyBlockWorld())) {
            biomeList = biomeList + "JUNGLE, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", getSkyBlockWorld())) {
            biomeList = biomeList + "DESERT, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", getSkyBlockWorld())) {
            biomeList = biomeList + "TAIGA, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", getSkyBlockWorld())) {
            biomeList = biomeList + "SWAMPLAND, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", getSkyBlockWorld())) {
            biomeList = biomeList + "MUSHROOM, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", getSkyBlockWorld())) {
            biomeList = biomeList + "HELL, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", getSkyBlockWorld())) {
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
            String cSchem = "";
            for (int i = 0; i < getSchemFile().length; ++i) {
                if (!hasIslandNow) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getSchemFile()[i].getName().substring(0, getSchemFile()[i].getName().lastIndexOf(46));
                    } else {
                        cSchem = getSchemFile()[i].getName();
                    }
                    if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, getSkyBlockWorld()) && WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getSchemFile()[i], next)) {
                        this.setChest(next, player);
                        hasIslandNow = true;
                    }
                }
            }
            if (!hasIslandNow) {
                for (int i = 0; i < getSchemFile().length; ++i) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getSchemFile()[i].getName().substring(0, getSchemFile()[i].getName().lastIndexOf(46));
                    } else {
                        cSchem = getSchemFile()[i].getName();
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName) && WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getSchemFile()[i], next)) {
                        this.setChest(next, player);
                        hasIslandNow = true;
                    }
                }
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                this.generateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, getSkyBlockWorld());
            } else {
                this.oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, getSkyBlockWorld());
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
                    if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
                        final Block blockToChange = getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
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

    public Location getChestSpawnLoc(final Location loc, final Player player) {
        for (int x = -15; x <= 15; ++x) {
            for (int y = -15; y <= 15; ++y) {
                int z = -15;
                while (z <= 15) {
                    if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + (z + 1)).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + (y - 1), loc.getBlockZ() + (z + 1)).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double) (loc.getBlockX() + x), (double) (loc.getBlockY() + (y + 1)), (double) (loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + (z - 1)).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + (y - 1), loc.getBlockZ() + (z - 1)).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double) (loc.getBlockX() + x), (double) (loc.getBlockY() + (y + 1)), (double) (loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double) (loc.getBlockX() + x), (double) (loc.getBlockY() + (y + 1)), (double) (loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double) (loc.getBlockX() + x), (double) (loc.getBlockY() + (y + 1)), (double) (loc.getBlockZ() + (z + 1)));
                        }
                        loc.setY(loc.getY() + 1.0);
                        return loc;
                    } else {
                        ++z;
                    }
                }
            }
        }
        return loc;
    }

    private void setNewPlayerIsland(final Player player, final Location loc) {
        getPlayerInfo(player).startNewIsland(loc);
        player.teleport(this.getChestSpawnLoc(loc, player));
        if (this.getIslandConfig(getPlayerInfo(player).locationForParty()) == null) {
            this.createIslandConfig(getPlayerInfo(player).locationForParty(), player.getName());
        }
        this.clearIslandConfig(getPlayerInfo(player).locationForParty(), player.getName());
        updatePartyNumber(player);
        homeSet(player);
        getPlayerInfo(player).save();
    }

    public void setWarpLocation(final String location, final Location loc) {
        this.getIslandConfig(location).set("general.warpLocationX", loc.getBlockX());
        this.getIslandConfig(location).set("general.warpLocationY", loc.getBlockY());
        this.getIslandConfig(location).set("general.warpLocationZ", loc.getBlockZ());
        this.getIslandConfig(location).set("general.warpActive", true);
        this.saveIslandConfig(location);
    }

    public void buildIslandList() {
        final File folder = directoryPlayers;
        final File[] listOfFiles = folder.listFiles();
        System.out.print(ChatColor.YELLOW + "[uSkyBlock] Building a new island list...");
        for (int i = 0; i < listOfFiles.length; ++i) {
            final PlayerInfo pi = new PlayerInfo(listOfFiles[i].getName());
            if (pi.getHasIsland()) {
                System.out.print("Creating new island file for " + pi.getPlayerName());
                this.createIslandConfig(pi.locationForParty(), pi.getPlayerName());
                this.saveIslandConfig(pi.locationForParty());
            }
        }
        // TODO: 15/12/2014 - R4zorax: Support reading the old-format (at some time)
        System.out.print(ChatColor.YELLOW + "[uSkyBlock] Party list completed.");
    }

    public void removeIslandConfig(final String location) {
        this.islands.remove(location);
    }

    public void updatePartyNumber(final Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        String islandLocation = playerInfo.locationForParty();
        FileConfiguration islandConfig = getIslandConfig(islandLocation);
        // TODO: 14/12/2014 - R4zorax: Move this freaking party-sizing somewhere else
        if (islandConfig.getInt("party.maxSize") < 8 && VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
            islandConfig.set("party.maxSize", 8);
            saveIslandConfig(islandLocation);
            return;
        }
        if (islandConfig.getInt("party.maxSize") < 7 && VaultHandler.checkPerk(player.getName(), "usb.extra.party3", player.getWorld())) {
            islandConfig.set("party.maxSize", 7);
            saveIslandConfig(islandLocation);
            return;
        }
        if (islandConfig.getInt("party.maxSize") < 6 && VaultHandler.checkPerk(player.getName(), "usb.extra.party2", player.getWorld())) {
            islandConfig.set("party.maxSize", 6);
            saveIslandConfig(islandLocation);
            return;
        }
        if (islandConfig.getInt("party.maxSize") < 5 && VaultHandler.checkPerk(player.getName(), "usb.extra.party1", player.getWorld())) {
            islandConfig.set("party.maxSize", 5);
            saveIslandConfig(islandLocation);
        }
    }

    public void changePlayerPermission(final Player player, final String playername, final String perm) {
        if (!getIslandConfig(getPlayerInfo(player).locationForParty()).contains("party.members." + playername + "." + perm)) {
            return;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getBoolean("party.members." + playername + "." + perm)) {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.members." + playername + "." + perm, false);
        } else {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.members." + playername + "." + perm, true);
        }
        saveIslandConfig(getPlayerInfo(player).locationForParty());
    }

    public boolean hasIslandMembersOnline(final Player p) {
        FileConfiguration islandConfig = getIslandConfig(p);
        for (final String member : islandConfig.getConfigurationSection("party.members").getKeys(false)) {
            if (Bukkit.getPlayer(member) != null && !Bukkit.getPlayer(member).isOnline()) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentBiome(Player p) {
        return getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).getString("general.biome").toUpperCase();
    }

    public void setConfigBiome(final Player p, final String biome) {
        getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).set("general.biome", biome);
        saveIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty());
    }

    public boolean isPartyLeader(final Player player) {
        return getIslandConfig(player).getString("party.leader").equalsIgnoreCase(player.getName());
    }

    public void sendMessageToIslandGroup(final String location, final String message) {
        final Iterator<String> temp = getIslandConfig(location).getConfigurationSection("party.members").getKeys(false).iterator();
        this.date = new Date();
        final String dateTxt = DateFormat.getDateInstance(3).format(this.date).toString();
        int currentLogPos = getIslandConfig(location).getInt("log.logPos");
        while (temp.hasNext()) {
            final String player = temp.next();
            if (Bukkit.getPlayer(player) != null) {
                Bukkit.getPlayer(player).sendMessage("\u00a7d[skyblock] " + message);
            }
        }
        getIslandConfig(location).set("log." + ++currentLogPos, "\u00a7d[" + dateTxt + "] " + message);
        if (currentLogPos < 10) {
            getIslandConfig(location).set("log.logPos", currentLogPos);
        } else {
            getIslandConfig(location).set("log.logPos", 0);
        }
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
        Settings.loadPluginConfig(getConfig());
        levelLogic = new LevelLogic(getFileConfiguration("levelConfig.yml"));
    }

    public boolean isSkyWorld(World world) {
        if (world == null) {
            return false;
        }
        return skyBlockWorld.getName().equalsIgnoreCase(world.getName());
    }

    public IslandLogic getIslandLogic() {
        return islandLogic;
    }
}
