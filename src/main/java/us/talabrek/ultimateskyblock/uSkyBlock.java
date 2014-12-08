package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class uSkyBlock extends JavaPlugin {
    public PluginDescriptionFile pluginFile;
    public Logger log;
    Date date;
    public DecimalFormat df;
    private FileConfiguration levelConfig;
    private FileConfiguration lastIslandConfig;
    private FileConfiguration orphans;
    private HashMap<String, FileConfiguration> islands;
    private File levelConfigFile;
    private File orphanFile;
    private File lastIslandConfigFile;
    public static World skyBlockWorld;
    private static uSkyBlock instance;
    public List<String> removeList;
    List<String> rankDisplay;
    private Location lastIsland;
    private Stack<Location> orphaned;
    private Stack<Location> tempOrphaned;
    private Stack<Location> reverseOrphaned;
    public File directoryPlayers;
    public File directoryIslands;
    public File[] schemFile;
    public String pName;
    public Location islandTestLocation;
    LinkedHashMap<String, Double> topTen;
    HashMap<String, Long> infoCooldown;
    HashMap<String, Long> restartCooldown;
    HashMap<String, Long> biomeCooldown;
    HashMap<String, PlayerInfo> activePlayers;
    LinkedHashMap<String, List<String>> challenges;
    HashMap<Integer, Integer> requiredList;
    public boolean purgeActive;
    private FileConfiguration skyblockData;
    private File skyblockDataFile;
    private SkyBlockMenu menu;

    static {
        uSkyBlock.skyBlockWorld = null;
    }

    public uSkyBlock() {
        super();
        this.df = new DecimalFormat(".#");
        // TODO: 08/12/2014 - R4zorax: Most of these should be converted to local variables
        this.levelConfig = null;
        this.lastIslandConfig = null;
        this.orphans = null;
        this.islands = new HashMap<>();
        this.orphanFile = null;
        this.lastIslandConfigFile = null;
        this.removeList = new ArrayList<>();
        this.orphaned = new Stack<>();
        this.tempOrphaned = new Stack<>();
        this.reverseOrphaned = new Stack<>();
        this.islandTestLocation = null;
        this.infoCooldown = new HashMap<>();
        this.restartCooldown = new HashMap<>();
        this.biomeCooldown = new HashMap<>();
        this.activePlayers = new HashMap<>();
        this.challenges = new LinkedHashMap<>();
        this.requiredList = new HashMap<>();
        this.purgeActive = false;
        this.skyblockData = null;
        this.skyblockDataFile = null;
    }

    public void onDisable() {
        try {
            this.unloadPlayerFiles();
            if (this.lastIsland != null) {
                this.setLastIsland(this.lastIsland);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong saving the island and/or party data!");
            e.printStackTrace();
        }
        this.log.info(String.valueOf(this.pluginFile.getName()) + " v" + this.pluginFile.getVersion() + " disabled.");
    }

    public void onEnable() {
        (uSkyBlock.instance = this).saveDefaultConfig();
        this.menu = new SkyBlockMenu(this);
        this.saveDefaultLevelConfig();
        this.saveDefaultOrphans();
        this.pluginFile = this.getDescription();
        this.log = this.getLogger();
        this.pName = ChatColor.WHITE + "[" + ChatColor.GREEN + this.pluginFile.getName() + ChatColor.WHITE + "] ";
        VaultHandler.setupEconomy();
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        if (Settings.loadPluginConfig(getConfig())) {
            saveConfig();
        }
        this.loadLevelConfig();
        this.registerEvents();
        this.directoryPlayers = new File(this.getDataFolder() + File.separator + "players");
        this.directoryIslands = new File(this.getDataFolder() + File.separator + "islands");
        if (!this.directoryPlayers.exists()) {
            this.directoryPlayers.mkdir();
            this.loadPlayerFiles();
        } else {
            this.loadPlayerFiles();
        }
        if (!this.directoryIslands.exists()) {
            this.directoryIslands.mkdir();
        }
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
        this.getCommand("island").setExecutor(new IslandCommand());
        this.getCommand("challenges").setExecutor(new ChallengesCommand());
        this.getCommand("dev").setExecutor(new DevCommand());
        if (Settings.island_useTopTen) {
            getInstance().updateTopTen(getInstance().generateTopTen());
        }
        this.populateChallengeList();
        this.log.info(String.valueOf(this.pluginFile.getName()) + " v." + this.pluginFile.getVersion() + " enabled.");
        getInstance().getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                    System.out.print("[uSkyBlock] Using vault for permissions");
                    VaultHandler.setupPermissions();
                    try {
                        if (!uSkyBlock.this.getLastIslandConfig().contains("options.general.lastIslandX") && uSkyBlock.this.getConfig().contains("options.general.lastIslandX")) {
                            uSkyBlock.this.getLastIslandConfig();
                            FileConfiguration.createPath(uSkyBlock.this.getLastIslandConfig().getConfigurationSection("options.general"), "lastIslandX");
                            uSkyBlock.this.getLastIslandConfig();
                            FileConfiguration.createPath(uSkyBlock.this.getLastIslandConfig().getConfigurationSection("options.general"), "lastIslandZ");
                            uSkyBlock.this.getLastIslandConfig().set("options.general.lastIslandX", uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"));
                            uSkyBlock.this.getLastIslandConfig().set("options.general.lastIslandZ", uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ"));
                            uSkyBlock.this.saveLastIslandConfig();
                        }
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), (double) uSkyBlock.this.getLastIslandConfig().getInt("options.general.lastIslandX"), (double) Settings.island_height, (double) uSkyBlock.this.getLastIslandConfig().getInt("options.general.lastIslandZ")));
                    } catch (Exception e) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"), (double) Settings.island_height, (double) uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ")));
                    }
                    if (uSkyBlock.this.lastIsland == null) {
                        setLastIsland(new Location(uSkyBlock.getSkyBlockWorld(), 0.0, (double) Settings.island_height, 0.0));
                    }
                    if (Settings.island_protectWithWorldGuard && !Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                        final PluginManager manager = uSkyBlock.getInstance().getServer().getPluginManager();
                        System.out.print("[uSkyBlock] WorldGuard not loaded! Using built in protection.");
                        manager.registerEvents(new ProtectionEvents(), uSkyBlock.getInstance());
                    }
                    uSkyBlock.getInstance().setupOrphans();
                }
            }
        }, 0L);
    }

    public static uSkyBlock getInstance() {
        return uSkyBlock.instance;
    }

    public void loadPlayerFiles() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.isOnline()) {
                final File f = new File(getInstance().directoryPlayers, player.getName());
                final PlayerInfo pi = new PlayerInfo(player.getName());
                if (f.exists()) {
                    final PlayerInfo pi2 = getInstance().readPlayerFile(player.getName());
                    if (pi2 != null) {
                        pi.setIslandLocation(pi2.getIslandLocation());
                        pi.setHomeLocation(pi2.getHomeLocation());
                        pi.setHasIsland(pi2.getHasIsland());
                        if (getIslandConfig(pi.locationForParty()) == null) {
                            getInstance().createIslandConfig(pi.locationForParty(), player.getName());
                        }
                        getInstance().clearIslandConfig(pi.locationForParty(), player.getName());
                        if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                            WorldGuardHandler.protectIsland(player, player.getName(), pi);
                        }
                    }
                    f.delete();
                }
                getInstance().addActivePlayer(player.getName(), pi);
                if (pi.getHasIsland() && getInstance().getTempIslandConfig(pi.locationForParty()) == null) {
                    getInstance().createIslandConfig(pi.locationForParty(), player.getName());
                    System.out.println("Creating new Config File");
                }
                getIslandConfig(pi.locationForParty());
            }
        }
        System.out.print("Island Configs Loaded:");
        getInstance().displayIslandConfigs();
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
        manager.registerEvents(new PlayerJoin(), this);
        if (!Settings.island_protectWithWorldGuard) {
            System.out.print("[uSkyBlock] Using built in protection.");
            manager.registerEvents(new ProtectionEvents(), this);
        } else {
            System.out.print("[uSkyBlock] Using WorldGuard protection.");
        }
    }

    public PlayerInfo readPlayerFile(final String playerName) {
        final File f = new File(this.directoryPlayers, playerName);
        if (!f.exists()) {
            return null;
        }
        try {
            final FileInputStream fileIn = new FileInputStream(f);
            final ObjectInputStream in = new ObjectInputStream(fileIn);
            final PlayerInfo p = (PlayerInfo) in.readObject();
            in.close();
            fileIn.close();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
                sender.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + " - Island level " + topTen.get(playerName));
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

    public Location getLocationString(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final World w = this.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(w, (double) x, (double) y, (double) z);
        }
        return null;
    }

    public String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return String.valueOf(l.getWorld().getName()) + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public void setStringbyPath(final FileConfiguration fc, final File f, final String path, final Object value) {
        fc.set(path, value.toString());
        try {
            fc.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStringbyPath(final FileConfiguration fc, final File file, final String path, final Object stdValue, final boolean addMissing) {
        if (!fc.contains(path)) {
            if (addMissing) {
                this.setStringbyPath(fc, file, path, stdValue);
            }
            return stdValue.toString();
        }
        return fc.getString(path);
    }

    public static World getSkyBlockWorld() {
        if (uSkyBlock.skyBlockWorld == null) {
            uSkyBlock.skyBlockWorld = WorldCreator.name(Settings.general_worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new SkyBlockChunkGenerator()).createWorld();
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv import " + Settings.general_worldName + " normal -g uSkyBlock");
            }
        }
        return uSkyBlock.skyBlockWorld;
    }

    public void clearOrphanedIsland() {
        while (this.hasOrphanedIsland()) {
            this.orphaned.pop();
        }
    }

    public void clearArmorContents(final Player player) {
        player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
    }

    public void getAllFiles(final String path) {
        final File dirpath = new File(path);
        if (!dirpath.exists()) {
            return;
        }
        File[] listFiles;
        for (int length = (listFiles = dirpath.listFiles()).length, i = 0; i < length; ++i) {
            final File f = listFiles[i];
            try {
                if (f.isDirectory()) {
                    this.getAllFiles(f.getAbsolutePath());
                }
            } catch (Exception ex) {
                this.log.warning(ex.getMessage());
            }
        }
    }

    public Location getYLocation(final Location l) {
        for (int y = 0; y < 254; ++y) {
            final int px = l.getBlockX();
            final int py = y;
            final int pz = l.getBlockZ();
            final Block b1 = new Location(l.getWorld(), (double) px, (double) py, (double) pz).getBlock();
            final Block b2 = new Location(l.getWorld(), (double) px, (double) (py + 1), (double) pz).getBlock();
            final Block b3 = new Location(l.getWorld(), (double) px, (double) (py + 2), (double) pz).getBlock();
            if (!b1.getType().equals(Material.AIR) && b2.getType().equals(Material.AIR) && b3.getType().equals(Material.AIR)) {
                return b2.getLocation();
            }
        }
        return l;
    }

    public Location getSafeHomeLocation(final PlayerInfo p) {
        Location home = null;
        if (p.getHomeLocation() == null) {
            if (p.getIslandLocation() != null) {
                home = p.getIslandLocation();
            }
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
        if (!this.getActivePlayers().containsKey(player)) {
            final PlayerInfo pi = new PlayerInfo(player);
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
            }
            this.orphaned.push(pi.getIslandLocation());
            this.removeIsland(pi.getIslandLocation());
            this.deleteIslandConfig(pi.locationForParty());
            pi.removeFromIsland();
            this.saveOrphans();
            pi.savePlayerConfig(player);
        } else {
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
            }
            this.orphaned.push(this.getActivePlayers().get(player).getIslandLocation());
            this.removeIsland(this.getActivePlayers().get(player).getIslandLocation());
            this.deleteIslandConfig(this.getActivePlayers().get(player).locationForParty());
            final PlayerInfo pi = new PlayerInfo(player);
            pi.removeFromIsland();
            this.addActivePlayer(player, pi);
            this.saveOrphans();
        }
    }

    public boolean restartPlayerIsland(final Player player, final Location next) {
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return false;
        }
        try {
            removeIsland(next);
            createIsland(player, next);
            next.setY((double) Settings.island_height);
            this.setNewPlayerIsland(player, next);
            player.getInventory().clear();
            player.getEquipment().clear();
            changePlayerBiome(player, "OCEAN");
            refreshIslandChunks(next);
            clearEntitiesNearPlayer(player);
            setRestartCooldown(player);
            return true;
        } catch (Exception e) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            e.printStackTrace();
            return false;
        }
    }

    private void refreshIslandChunks(Location next) {
        for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
            for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
                getSkyBlockWorld().refreshChunk((next.getBlockX() + x) / 16, (next.getBlockZ() + z) / 16);
            }
        }
    }

    private void clearEntitiesNearPlayer(Player player) {
        for (final Entity tempent : player.getNearbyEntities((double) (Settings.island_protectionRange / 2), 250.0, (double) (Settings.island_protectionRange / 2))) {
            if (!(tempent instanceof Player)) {
                tempent.remove();
            }
        }
    }

    public void devDeletePlayerIsland(final String player) {
        if (!this.getActivePlayers().containsKey(player)) {
            PlayerInfo pi = new PlayerInfo(player);
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
            }
            pi = new PlayerInfo(player);
            pi.savePlayerConfig(player);
        } else {
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
            }
            final PlayerInfo pi = new PlayerInfo(player);
            this.removeActivePlayer(player);
            this.addActivePlayer(player, pi);
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
                            pi.savePlayerConfig(player);
                            getInstance().createIslandConfig(pi.locationForParty(), player);
                            getInstance().clearIslandConfig(pi.locationForParty(), player);
                            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                                if (!WorldGuardHandler.protectIsland(sender, player, pi)) {
                                    sender.sendMessage("Player doesn't have an island or it's already protected!");
                                }
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
                            this.getActivePlayers().get(player).setHomeLocation(new Location(l.getWorld(), (double) (px2 + x2), (double) (py2 + y2 + 3), (double) (pz2 + z2)));
                            this.getActivePlayers().get(player).setHasIsland(true);
                            this.getActivePlayers().get(player).setIslandLocation(b2.getLocation());
                            final PlayerInfo pi2 = this.getActivePlayers().get(player);
                            this.removeActivePlayer(player);
                            this.addActivePlayer(player, pi2);
                            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                                WorldGuardHandler.protectIsland(sender, player, pi2);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int orphanCount() {
        return this.orphaned.size();
    }

    public void removeIsland(final Location loc) {
        if (loc != null) {
            final int px = loc.getBlockX();
            final int py = loc.getBlockY();
            final int pz = loc.getBlockZ();
            for (int x = Settings.island_protectionRange / 2 * -1; x <= Settings.island_protectionRange / 2; ++x) {
                for (int y = 0; y <= 255; ++y) {
                    for (int z = Settings.island_protectionRange / 2 * -1; z <= Settings.island_protectionRange / 2; ++z) {
                        final Block b = new Location(loc.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                        if (!b.getType().equals(Material.AIR)) {
                            if (b.getType().equals(Material.CHEST)) {
                                final Chest c = (Chest) b.getState();
                                final ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
                                c.getInventory().setContents(items);
                            } else if (b.getType().equals(Material.FURNACE)) {
                                final Furnace f = (Furnace) b.getState();
                                final ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
                                f.getInventory().setContents(items);
                            } else if (b.getType().equals(Material.DISPENSER)) {
                                final Dispenser d = (Dispenser) b.getState();
                                final ItemStack[] items = new ItemStack[d.getInventory().getContents().length];
                                d.getInventory().setContents(items);
                            }
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    public void removeIslandBlocks(final Location loc) {
        if (loc != null) {
            System.out.print("Removing blocks from an abandoned island.");
            final int px = loc.getBlockX();
            final int py = loc.getBlockY();
            final int pz = loc.getBlockZ();
            for (int x = -20; x <= 20; ++x) {
                for (int y = -20; y <= 20; ++y) {
                    for (int z = -20; z <= 20; ++z) {
                        final Block b = new Location(loc.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                        if (!b.getType().equals(Material.AIR)) {
                            if (b.getType().equals(Material.CHEST)) {
                                final Chest c = (Chest) b.getState();
                                final ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
                                c.getInventory().setContents(items);
                            } else if (b.getType().equals(Material.FURNACE)) {
                                final Furnace f = (Furnace) b.getState();
                                final ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
                                f.getInventory().setContents(items);
                            } else if (b.getType().equals(Material.DISPENSER)) {
                                final Dispenser d = (Dispenser) b.getState();
                                final ItemStack[] items = new ItemStack[d.getInventory().getContents().length];
                                d.getInventory().setContents(items);
                            }
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }
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
        return !this.orphaned.empty();
    }

    public Location checkOrphan() {
        return this.orphaned.peek();
    }

    public Location getOrphanedIsland() {
        if (this.hasOrphanedIsland()) {
            return this.orphaned.pop();
        }
        return null;
    }

    public void addOrphan(final Location island) {
        this.orphaned.push(island);
    }

    public void removeNextOrphan() {
        this.orphaned.pop();
    }

    public void saveOrphans() {
        String fullOrphan = "";
        this.tempOrphaned = (Stack<Location>) this.orphaned.clone();
        while (!this.tempOrphaned.isEmpty()) {
            this.reverseOrphaned.push(this.tempOrphaned.pop());
        }
        while (!this.reverseOrphaned.isEmpty()) {
            final Location tempLoc = this.reverseOrphaned.pop();
            fullOrphan = fullOrphan + tempLoc.getBlockX() + "," + tempLoc.getBlockZ() + ";";
        }
        this.getOrphans().set("orphans.list", fullOrphan);
        this.saveOrphansFile();
    }

    public void setupOrphans() {
        if (this.getOrphans().contains("orphans.list")) {
            final String fullOrphan = this.getOrphans().getString("orphans.list");
            if (!fullOrphan.isEmpty()) {
                final String[] orphanArray = fullOrphan.split(";");
                this.orphaned = new Stack<>();
                for (int i = 0; i < orphanArray.length; ++i) {
                    final String[] orphanXY = orphanArray[i].split(",");
                    final Location tempLoc = new Location(getSkyBlockWorld(), (double) Integer.parseInt(orphanXY[0]), (double) Settings.island_height, (double) Integer.parseInt(orphanXY[1]));
                    this.orphaned.push(tempLoc);
                }
            }
        }
    }

    public boolean homeTeleport(final Player player) {
        Location homeSweetHome = null;
        if (this.getActivePlayers().containsKey(player.getName())) {
            homeSweetHome = getInstance().getSafeHomeLocation(this.getActivePlayers().get(player.getName()));
        }
        if (homeSweetHome == null) {
            player.performCommand("spawn");
            player.sendMessage(ChatColor.RED + "You are not part of an island. Returning you the spawn area!");
            return true;
        }
        getInstance().removeCreatures(homeSweetHome);
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
        warpSweetWarp = getInstance().getSafeWarpLocation(pi);
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
            pi.savePlayerConfig(player);
        }
        return true;
    }

    public boolean playerIsOnIsland(final Player player) {
        if (this.getActivePlayers().containsKey(player.getName())) {
            if (this.getActivePlayers().get(player.getName()).getHasIsland()) {
                this.islandTestLocation = this.getActivePlayers().get(player.getName()).getIslandLocation();
            }
            if (this.islandTestLocation == null) {
                return false;
            }
            if (player.getLocation().getX() > this.islandTestLocation.getX() - Settings.island_protectionRange / 2 && player.getLocation().getX() < this.islandTestLocation.getX() + Settings.island_protectionRange / 2 && player.getLocation().getZ() > this.islandTestLocation.getZ() - Settings.island_protectionRange / 2 && player.getLocation().getZ() < this.islandTestLocation.getZ() + Settings.island_protectionRange / 2) {
                return true;
            }
        }
        return false;
    }

    public boolean locationIsOnIsland(final Player player, final Location loc) {
        if (this.getActivePlayers().containsKey(player.getName())) {
            if (this.getActivePlayers().get(player.getName()).getHasIsland()) {
                this.islandTestLocation = this.getActivePlayers().get(player.getName()).getIslandLocation();
            }
            if (this.islandTestLocation == null) {
                return false;
            }
            if (loc.getX() > this.islandTestLocation.getX() - Settings.island_protectionRange / 2 && loc.getX() < this.islandTestLocation.getX() + Settings.island_protectionRange / 2 && loc.getZ() > this.islandTestLocation.getZ() - Settings.island_protectionRange / 2 && loc.getZ() < this.islandTestLocation.getZ() + Settings.island_protectionRange / 2) {
                return true;
            }
        }
        return false;
    }

    public boolean playerIsInSpawn(final Player player) {
        return player.getLocation().getX() > Settings.general_spawnSize * -1 && player.getLocation().getX() < Settings.general_spawnSize && player.getLocation().getZ() > Settings.general_spawnSize * -1 && player.getLocation().getZ() < Settings.general_spawnSize;
    }

    public boolean hasIsland(final String playername) {
        if (this.getActivePlayers().containsKey(playername)) {
            return this.getActivePlayers().get(playername).getHasIsland();
        }
        final PlayerInfo pi = new PlayerInfo(playername);
        return pi.getHasIsland();
    }

    public Location getPlayerIsland(final String playername) {
        if (this.getActivePlayers().containsKey(playername)) {
            return this.getActivePlayers().get(playername).getIslandLocation();
        }
        final PlayerInfo pi = new PlayerInfo(playername);
        if (!pi.getHasIsland()) {
            return null;
        }
        return pi.getIslandLocation();
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

    public Stack<SerializableLocation> changeStackToFile(final Stack<Location> stack) {
        final Stack<SerializableLocation> finishStack = new Stack<>();
        final Stack<Location> tempStack = new Stack<>();
        while (!stack.isEmpty()) {
            tempStack.push(stack.pop());
        }
        while (!tempStack.isEmpty()) {
            if (tempStack.peek() != null) {
                finishStack.push(new SerializableLocation(tempStack.pop()));
            } else {
                tempStack.pop();
            }
        }
        return finishStack;
    }

    public Stack<Location> changestackfromfile(final Stack<SerializableLocation> stack) {
        final Stack<SerializableLocation> tempStack = new Stack<>();
        final Stack<Location> finishStack = new Stack<>();
        while (!stack.isEmpty()) {
            tempStack.push(stack.pop());
        }
        while (!tempStack.isEmpty()) {
            if (tempStack.peek() != null) {
                finishStack.push(tempStack.pop().getLocation());
            } else {
                tempStack.pop();
            }
        }
        return finishStack;
    }

    public boolean largeIsland(final Location l) {
        final int blockcount = 0;
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        for (int x = -30; x <= 30; ++x) {
            for (int y = -30; y <= 30; ++y) {
                for (int z = -30; z <= 30; ++z) {
                    final Block b = new Location(l.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                    if (b.getTypeId() != 0 && b.getTypeId() != 8 && b.getTypeId() != 10 && blockcount > 200) {
                        return true;
                    }
                }
            }
        }
        return blockcount > 200;
    }

    public boolean clearAbandoned() {
        int numOffline = 0;
        final OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
        System.out.print("Attemping to add more orphans");
        for (int i = 0; i < oplayers.length; ++i) {
            long offlineTime = oplayers[i].getLastPlayed();
            offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
            if (offlineTime > 250L && getInstance().hasIsland(oplayers[i].getName()) && offlineTime < 50000L) {
                final PlayerInfo pi = new PlayerInfo(oplayers[i].getName());
                final Location l = pi.getIslandLocation();
                int blockcount = 0;
                final int px = l.getBlockX();
                final int py = l.getBlockY();
                final int pz = l.getBlockZ();
                for (int x = -30; x <= 30; ++x) {
                    for (int y = -30; y <= 30; ++y) {
                        for (int z = -30; z <= 30; ++z) {
                            final Block b = new Location(l.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                            if (b.getTypeId() != 0 && b.getTypeId() != 8 && b.getTypeId() != 10) {
                                ++blockcount;
                            }
                        }
                    }
                }
                if (blockcount < 200) {
                    ++numOffline;
                    WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(String.valueOf(oplayers[i].getName()) + "Island");
                    this.orphaned.push(pi.getIslandLocation());
                    pi.setHomeLocation(null);
                    pi.setHasIsland(false);
                    pi.setIslandLocation(null);
                    pi.savePlayerConfig(pi.getPlayerName());
                }
            }
        }
        if (numOffline > 0) {
            System.out.print("Added " + numOffline + " new orphans.");
            this.saveOrphans();
            return true;
        }
        System.out.print("No new orphans to add!");
        return false;
    }

    public LinkedHashMap<String, Double> generateTopTen() {
        final HashMap<String, Double> tempMap = new HashMap<>();
        final File folder = this.directoryIslands;
        final File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; ++i) {
            FileConfiguration islandConfig = this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", ""));
            if (islandConfig != null && islandConfig.getInt("general.level") > 0) {
                tempMap.put(islandConfig.getString("party.leader"), islandConfig.getDouble("general.level"));
            }
        }
        TreeMap<String, Double> sortedMap = new TreeMap<>(new TopTenComparator(tempMap));
        sortedMap.putAll(tempMap);
        return new LinkedHashMap<>(sortedMap);
    }

    public boolean onInfoCooldown(final Player player) {
        return this.infoCooldown.containsKey(player.getName()) && this.infoCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis();
    }

    public boolean onBiomeCooldown(final Player player) {
        return this.biomeCooldown.containsKey(player.getName()) && this.biomeCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis();
    }

    public boolean onRestartCooldown(final Player player) {
        return this.restartCooldown.containsKey(player.getName()) && this.restartCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis();
    }

    public long getInfoCooldownTime(final Player player) {
        if (!this.infoCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (this.infoCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis()) {
            return this.infoCooldown.get(player.getName()) - Calendar.getInstance().getTimeInMillis();
        }
        return 0L;
    }

    public long getBiomeCooldownTime(final Player player) {
        if (!this.biomeCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (this.biomeCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis()) {
            return this.biomeCooldown.get(player.getName()) - Calendar.getInstance().getTimeInMillis();
        }
        return 0L;
    }

    public long getRestartCooldownTime(final Player player) {
        if (!this.restartCooldown.containsKey(player.getName())) {
            return 0L;
        }
        if (this.restartCooldown.get(player.getName()) > Calendar.getInstance().getTimeInMillis()) {
            return this.restartCooldown.get(player.getName()) - Calendar.getInstance().getTimeInMillis();
        }
        return 0L;
    }

    public void setInfoCooldown(final Player player) {
        this.infoCooldown.put(player.getName(), Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownInfo * 1000);
    }

    public void setBiomeCooldown(final Player player) {
        this.biomeCooldown.put(player.getName(), Calendar.getInstance().getTimeInMillis() + Settings.general_biomeChange * 1000);
    }

    public void setRestartCooldown(final Player player) {
        this.restartCooldown.put(player.getName(), Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownRestart * 1000);
    }

    public File[] getSchemFile() {
        return this.schemFile;
    }

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

    public void removeInactive(final List<String> removePlayerList) {
        getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(getInstance(), new Runnable() {
            @Override
            public void run() {
                if (uSkyBlock.getInstance().getRemoveList().size() > 0 && !uSkyBlock.getInstance().isPurgeActive()) {
                    uSkyBlock.getInstance().deletePlayerIsland(uSkyBlock.getInstance().getRemoveList().get(0));
                    System.out.print("[uSkyBlock] Purge: Removing " + uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
                    uSkyBlock.getInstance().deleteFromRemoveList();
                }
            }
        }, 0L, 200L);
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

    public HashMap<String, PlayerInfo> getActivePlayers() {
        return this.activePlayers;
    }

    public PlayerInfo getPlayerInfo(Player name) {
        // TODO: UUID aware
        return activePlayers.get(name.getName());
    }

    public void addActivePlayer(final String player, final PlayerInfo pi) {
        this.activePlayers.put(player, pi);
    }

    public void removeActivePlayer(final String player) {
        if (this.activePlayers.containsKey(player)) {
            this.activePlayers.get(player).savePlayerConfig(player);
            this.activePlayers.remove(player);
            System.out.print("Removing player from memory: " + player);
        }
    }

    public void populateChallengeList() {
        List<String> templist = new ArrayList<>();
        for (int i = 0; i < Settings.challenges_ranks.length; ++i) {
            this.challenges.put(Settings.challenges_ranks[i], templist);
            templist = new ArrayList<>();
        }
        for (final String tempString : Settings.challenges_challengeList) {
            if (this.challenges.containsKey(this.getConfig().getString("options.challenges.challengeList." + tempString + ".rankLevel"))) {
                this.challenges.get(this.getConfig().getString("options.challenges.challengeList." + tempString + ".rankLevel")).add(tempString);
            }
        }
    }

    public String getChallengesFromRank(final Player player, final String rank) {
        this.rankDisplay = this.challenges.get(rank);
        String fullString = "";
        final PlayerInfo pi = this.getActivePlayers().get(player.getName());
        for (final String tempString : this.rankDisplay) {
            if (pi.checkChallenge(tempString) > 0) {
                if (this.getConfig().getBoolean("options.challenges.challengeList." + tempString + ".repeatable")) {
                    fullString = fullString + Settings.challenges_repeatableColor + tempString + ChatColor.DARK_GRAY + " - ";
                } else {
                    fullString = fullString + Settings.challenges_finishedColor + tempString + ChatColor.DARK_GRAY + " - ";
                }
            } else {
                fullString = fullString + Settings.challenges_challengeColor + tempString + ChatColor.DARK_GRAY + " - ";
            }
        }
        if (fullString.length() > 4) {
            fullString = fullString.substring(0, fullString.length() - 3);
        }
        return fullString;
    }

    public int checkRankCompletion(final Player player, final String rank) {
        if (!Settings.challenges_requirePreviousRank) {
            return 0;
        }
        this.rankDisplay = this.challenges.get(rank);
        int ranksCompleted = 0;
        final PlayerInfo pi = this.getActivePlayers().get(player.getName());
        for (final String tempString : this.rankDisplay) {
            if (pi.checkChallenge(tempString) > 0) {
                ++ranksCompleted;
            }
        }
        return this.rankDisplay.size() - Settings.challenges_rankLeeway - ranksCompleted;
    }

    public boolean isRankAvailable(final Player player, final String rank) {
        if (this.challenges.size() < 2) {
            return true;
        }
        for (int i = 0; i < Settings.challenges_ranks.length; ++i) {
            if (Settings.challenges_ranks[i].equalsIgnoreCase(rank)) {
                if (i == 0) {
                    return true;
                }
                if (this.checkRankCompletion(player, Settings.challenges_ranks[i - 1]) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIfCanCompleteChallenge(final Player player, final String challenge) {
        final PlayerInfo pi = this.getActivePlayers().get(player.getName());
        if (!this.isRankAvailable(player, this.getConfig().getString("options.challenges.challengeList." + challenge + ".rankLevel"))) {
            player.sendMessage(ChatColor.RED + "You have not unlocked this challenge yet!");
            return false;
        }
        if (!pi.challengeExists(challenge)) {
            player.sendMessage(ChatColor.RED + "Unknown challenge name (check spelling)!");
            return false;
        }
        if (pi.checkChallenge(challenge) > 0 && !this.getConfig().getBoolean("options.challenges.challengeList." + challenge + ".repeatable")) {
            player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
            return false;
        }
        if (pi.checkChallenge(challenge) > 0 && (this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland") || this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland"))) {
            player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
            return false;
        }
        if (this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onPlayer")) {
            if (!this.hasRequired(player, challenge, "onPlayer")) {
                player.sendMessage(ChatColor.RED + this.getConfig().getString("options.challenges.challengeList." + challenge + ".description"));
                player.sendMessage(ChatColor.RED + "You don't have enough of the required item(s)!");
                return false;
            }
            return true;
        } else if (this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")) {
            if (!this.playerIsOnIsland(player)) {
                player.sendMessage(ChatColor.RED + "You must be on your island to do that!");
            }
            if (!this.hasRequired(player, challenge, "onIsland")) {
                player.sendMessage(ChatColor.RED + this.getConfig().getString("options.challenges.challengeList." + challenge + ".description"));
                player.sendMessage(ChatColor.RED + "You must be standing within 10 blocks of all required items.");
                return false;
            }
            return true;
        } else {
            if (!this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("islandLevel")) {
                return false;
            }
            if (getIslandConfig(getPlayerInfo(player).locationForParty()).getInt("general.level") >= this.getConfig().getInt("options.challenges.challengeList." + challenge + ".requiredItems")) {
                return true;
            }
            player.sendMessage(ChatColor.RED + "Your island must be level " + this.getConfig().getInt("options.challenges.challengeList." + challenge + ".requiredItems") + " to complete this challenge!");
            return false;
        }
    }

    public boolean takeRequired(final Player player, final String challenge, final String type) {
        if (type.equalsIgnoreCase("onPlayer")) {
            final String[] reqList = this.getConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(" ");
            int reqItem = 0;
            int reqAmount = 0;
            int reqMod = -1;
            String[] array;
            for (int length = (array = reqList).length, i = 0; i < length; ++i) {
                final String s = array[i];
                final String[] sPart = s.split(":");
                if (sPart.length == 2) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[1].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[1]);
                    } else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        } else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        }
                    }
                    if (!player.getInventory().contains(reqItem, reqAmount)) {
                        return false;
                    }
                    player.getInventory().removeItem(new ItemStack[]{new ItemStack(reqItem, reqAmount)});
                } else if (sPart.length == 3) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[2].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[2]);
                    } else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        } else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        }
                    }
                    reqMod = Integer.parseInt(sPart[1]);
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount)) {
                        return false;
                    }
                    player.getInventory().removeItem(new ItemStack[]{new ItemStack(reqItem, reqAmount, (short) reqMod)});
                }
            }
            return true;
        }
        return type.equalsIgnoreCase("onIsland") || type.equalsIgnoreCase("islandLevel");
    }

    public boolean hasRequired(final Player player, final String challenge, final String type) {
        final String[] reqList = this.getConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(" ");
        if (type.equalsIgnoreCase("onPlayer")) {
            int reqItem = 0;
            int reqAmount = 0;
            int reqMod = -1;
            String[] array;
            for (int length = (array = reqList).length, n = 0; n < length; ++n) {
                final String s = array[n];
                final String[] sPart = s.split(":");
                if (sPart.length == 2) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[1].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[1]);
                    } else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        } else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        }
                    }
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) 0), reqAmount)) {
                        return false;
                    }
                } else if (sPart.length == 3) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[2].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[2]);
                    } else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        } else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge);
                        } else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getPlayerInfo(player).checkChallengeSinceTimer(challenge));
                        }
                    }
                    reqMod = Integer.parseInt(sPart[1]);
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount)) {
                        return false;
                    }
                }
            }
            if (this.getConfig().getBoolean("options.challenges.challengeList." + challenge + ".takeItems")) {
                this.takeRequired(player, challenge, type);
            }
            return true;
        }
        if (type.equalsIgnoreCase("onIsland")) {
            final int[][] neededItem = new int[reqList.length][2];
            for (int i = 0; i < reqList.length; ++i) {
                final String[] sPart = reqList[i].split(":");
                neededItem[i][0] = Integer.parseInt(sPart[0]);
                neededItem[i][1] = Integer.parseInt(sPart[1]);
            }
            final Location l = player.getLocation();
            final int px = l.getBlockX();
            final int py = l.getBlockY();
            final int pz = l.getBlockZ();
            for (int x = -10; x <= 10; ++x) {
                for (int y = -3; y <= 10; ++y) {
                    for (int z = -10; z <= 10; ++z) {
                        final Block b = new Location(l.getWorld(), (double) (px + x), (double) (py + y), (double) (pz + z)).getBlock();
                        for (int j = 0; j < neededItem.length; ++j) {
                            if (b.getTypeId() == neededItem[j][0]) {
                                final int[] array2 = neededItem[j];
                                final int n2 = 1;
                                --array2[n2];
                            }
                        }
                    }
                }
            }
            for (int k = 0; k < neededItem.length; ++k) {
                if (neededItem[k][1] > 0) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public boolean giveReward(final Player player, final String challenge) {
        final String[] permList = this.getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".permissionReward").split(" ");
        double rewCurrency = 0.0;
        player.sendMessage(ChatColor.GREEN + "You have completed the " + challenge + " challenge!");
        String[] rewList;
        if (getPlayerInfo(player).checkChallenge(challenge) == 0) {
            rewList = this.getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".itemReward").split(" ");
            if (Settings.challenges_enableEconomyPlugin && VaultHandler.hasEcon()) {
                rewCurrency = this.getConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".currencyReward");
            }
        } else {
            rewList = this.getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatItemReward").split(" ");
            if (Settings.challenges_enableEconomyPlugin && VaultHandler.hasEcon()) {
                rewCurrency = this.getConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatCurrencyReward");
            }
        }
        int rewItem = 0;
        int rewAmount = 0;
        double rewBonus = 1.0;
        int rewMod = -1;
        if (Settings.challenges_enableEconomyPlugin && VaultHandler.hasEcon()) {
            if (VaultHandler.checkPerk(player.getName(), "group.memberplus", getSkyBlockWorld())) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.all", getSkyBlockWorld())) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.25", getSkyBlockWorld())) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.50", getSkyBlockWorld())) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.75", getSkyBlockWorld())) {
                rewBonus += 0.1;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.100", getSkyBlockWorld())) {
                rewBonus += 0.2;
            }
            VaultHandler.depositPlayer(player.getName(), rewCurrency * rewBonus);
            if (getPlayerInfo(player).checkChallenge(challenge) > 0) {
                player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
                player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".repeatRewardText"));
                player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
                player.sendMessage(ChatColor.YELLOW + "Repeat currency reward: " + ChatColor.WHITE + this.df.format(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatCurrencyReward") * rewBonus) + " " + VaultHandler.getEcon().currencyNamePlural() + "\u00a7a(+" + this.df.format((rewBonus - 1.0) * 100.0) + "%)");
            } else {
                if (Settings.challenges_broadcastCompletion) {
                    Bukkit.getServer().broadcastMessage(Settings.challenges_broadcastText + player.getName() + " has completed the " + challenge + " challenge!");
                }
                player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
                player.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".rewardText"));
                player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
                player.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + this.df.format(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".currencyReward") * rewBonus) + " " + VaultHandler.getEcon().currencyNamePlural() + "\u00a7a(+" + this.df.format((rewBonus - 1.0) * 100.0) + "%)");
            }
        } else if (getPlayerInfo(player).checkChallenge(challenge) > 0) {
            player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
            player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".repeatRewardText"));
            player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
        } else {
            if (Settings.challenges_broadcastCompletion) {
                Bukkit.getServer().broadcastMessage(Settings.challenges_broadcastText + player.getName() + " has completed the " + challenge + " challenge!");
            }
            player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
            player.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".rewardText"));
            player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
        }
        String[] array;
        for (int length = (array = permList).length, i = 0; i < length; ++i) {
            final String s = array[i];
            if (!s.equalsIgnoreCase("none") && !VaultHandler.checkPerk(player.getName(), s, player.getWorld())) {
                VaultHandler.addPerk(player, s);
            }
        }
        String[] array2;
        for (int length2 = (array2 = rewList).length, j = 0; j < length2; ++j) {
            final String s = array2[j];
            final String[] sPart = s.split(":");
            if (sPart.length == 2) {
                rewItem = Integer.parseInt(sPart[0]);
                rewAmount = Integer.parseInt(sPart[1]);
                player.getInventory().addItem(new ItemStack[]{new ItemStack(rewItem, rewAmount)});
            } else if (sPart.length == 3) {
                rewItem = Integer.parseInt(sPart[0]);
                rewAmount = Integer.parseInt(sPart[2]);
                rewMod = Integer.parseInt(sPart[1]);
                player.getInventory().addItem(new ItemStack[]{new ItemStack(rewItem, rewAmount, (short) rewMod)});
            }
        }
        getPlayerInfo(player).completeChallenge(challenge);
        return true;
    }

    public void reloadData() {
        if (this.skyblockDataFile == null) {
            this.skyblockDataFile = new File(this.getDataFolder(), "skyblockData.yml");
        }
        this.skyblockData = YamlConfiguration.loadConfiguration(this.skyblockDataFile);
        final InputStream defConfigStream = this.getResource("skyblockData.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.skyblockData.setDefaults(defConfig);
        }
    }

    public FileConfiguration getData() {
        if (this.skyblockData == null) {
            this.reloadData();
        }
        return this.skyblockData;
    }

    double dReturns(final double val, final double scale) {
        if (val < 0.0) {
            return -this.dReturns(-val, scale);
        }
        final double mult = val / scale;
        final double trinum = (Math.sqrt(8.0 * mult + 1.0) - 1.0) / 2.0;
        return trinum * scale;
    }

    public void reloadLevelConfig() {
        if (this.levelConfigFile == null) {
            this.levelConfigFile = new File(this.getDataFolder(), "levelConfig.yml");
        }
        this.levelConfig = YamlConfiguration.loadConfiguration(this.levelConfigFile);
        final InputStream defConfigStream = this.getResource("levelConfig.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.levelConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getLevelConfig() {
        if (this.levelConfig == null) {
            this.reloadLevelConfig();
        }
        return this.levelConfig;
    }

    public void saveLevelConfig() {
        if (this.levelConfig == null || this.levelConfigFile == null) {
            return;
        }
        try {
            this.getLevelConfig().save(this.levelConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.levelConfigFile, ex);
        }
    }

    public void saveDefaultLevelConfig() {
        if (this.levelConfigFile == null) {
            this.levelConfigFile = new File(this.getDataFolder(), "levelConfig.yml");
        }
        if (!this.levelConfigFile.exists()) {
            getInstance().saveResource("levelConfig.yml", false);
        }
    }

    public void loadLevelConfig() {
        try {
            this.getLevelConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= 255; ++i) {
            if (this.getLevelConfig().contains("blockValues." + i)) {
                Settings.blockList[i] = this.getLevelConfig().getInt("blockValues." + i);
            } else {
                Settings.blockList[i] = this.getLevelConfig().getInt("general.default");
            }
            if (this.getLevelConfig().contains("blockLimits." + i)) {
                Settings.limitList[i] = this.getLevelConfig().getInt("blockLimits." + i);
            } else {
                Settings.limitList[i] = -1;
            }
            if (this.getLevelConfig().contains("diminishingReturns." + i)) {
                Settings.diminishingReturnsList[i] = this.getLevelConfig().getInt("diminishingReturns." + i);
            } else if (this.getLevelConfig().getBoolean("general.useDiminishingReturns")) {
                Settings.diminishingReturnsList[i] = this.getLevelConfig().getInt("general.defaultScale");
            } else {
                Settings.diminishingReturnsList[i] = -1;
            }
        }
        System.out.print(Settings.blockList[57]);
        System.out.print(Settings.diminishingReturnsList[57]);
        System.out.print(Settings.limitList[57]);
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

    public FileConfiguration getCurrentPlayerConfig(final String player) {
        File file = new File(this.directoryPlayers, player + ".yml");
        return YamlConfiguration.loadConfiguration(file);
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

    public FileConfiguration getIslandConfig(final String location) {
        if (this.islands.get(location) == null) {
            this.reloadIslandConfig(location);
        }
        return this.islands.get(location);
    }

    public void saveIslandConfig(final String location) {
        if (this.islands.get(location) == null) {
            return;
        }
        File file = new File(this.directoryIslands, location + ".yml");
        try {
            getIslandConfig(location).save(file);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + file, ex);
        }
    }

    public void deleteIslandConfig(final String location) {
        File file = new File(this.directoryIslands, location + ".yml");
        // TODO: 06/12/2014 - rlf: This is quite error-prone in Java
        file.delete();
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

    public void saveDefaultLastIslandConfig() {
        if (this.lastIslandConfigFile == null) {
            this.lastIslandConfigFile = new File(this.getDataFolder(), "lastIslandConfig.yml");
        }
        if (!this.lastIslandConfigFile.exists()) {
            getInstance().saveResource("lastIslandConfig.yml", false);
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

    public void saveDefaultOrphans() {
        if (this.orphanFile == null) {
            this.orphanFile = new File(this.getDataFolder(), "orphans.yml");
        }
        if (!this.orphanFile.exists()) {
            getInstance().saveResource("orphans.yml", false);
        }
    }

    public boolean setBiome(final Location loc, final String bName) {
        final int px = loc.getBlockX();
        final int pz = loc.getBlockZ();
        Biome bType = Biome.OCEAN;
        if (bName.equalsIgnoreCase("jungle")) {
            bType = Biome.JUNGLE;
        } else if (bName.equalsIgnoreCase("hell")) {
            bType = Biome.HELL;
        } else if (bName.equalsIgnoreCase("sky")) {
            bType = Biome.SKY;
        } else if (bName.equalsIgnoreCase("mushroom")) {
            bType = Biome.MUSHROOM_ISLAND;
        } else if (bName.equalsIgnoreCase("ocean")) {
            bType = Biome.OCEAN;
        } else if (bName.equalsIgnoreCase("swampland")) {
            bType = Biome.SWAMPLAND;
        } else if (bName.equalsIgnoreCase("taiga")) {
            bType = Biome.TAIGA;
        } else if (bName.equalsIgnoreCase("desert")) {
            bType = Biome.DESERT;
        } else if (bName.equalsIgnoreCase("forest")) {
            bType = Biome.FOREST;
        } else {
            bType = Biome.OCEAN;
        }
        for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
            for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
                getSkyBlockWorld().loadChunk((px + x) / 16, (pz + z) / 16);
            }
        }
        for (int x = Settings.island_protectionRange / 2 * -1; x <= Settings.island_protectionRange / 2; ++x) {
            for (int z = Settings.island_protectionRange / 2 * -1; z <= Settings.island_protectionRange / 2; ++z) {
                getSkyBlockWorld().setBiome(px + x, pz + z, bType);
            }
        }
        for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
            for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
                getSkyBlockWorld().refreshChunk((px + x) / 16, (pz + z) / 16);
            }
        }
        return bType != Biome.OCEAN;
    }

    public boolean changePlayerBiome(final Player player, final String bName) {
        if (!VaultHandler.checkPerk(player.getName(), "usb.biome." + bName, player.getWorld())) {
            return false;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getBoolean("party.members." + player.getName() + ".canChangeBiome")) {
            this.setBiome(getPlayerInfo(player).getIslandLocation(), bName);
            this.setConfigBiome(player, bName);
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
        System.out.println("Creating player island...");
        final Player player = (Player) sender;
        final Location last = getInstance().getLastIsland();
        last.setY((double) Settings.island_height);
        try {
            Location next = getNextIslandLocation(last);
            createIsland(player, next);
            setNewPlayerIsland(player, next);
            player.getInventory().clear();
            player.getEquipment().clear();
            getInstance().changePlayerBiome(player, "OCEAN");
            refreshIslandChunks(next);
            clearEntitiesNearPlayer(player);
            protectWithWorldGuard(sender, player, pi);
        } catch (Exception ex) {
            player.sendMessage("Could not create your Island. Please contact a server moderator.");
            ex.printStackTrace();
            return false;
        }
        System.out.println("Finished creating player island.");
        return true;
    }

    private void protectWithWorldGuard(CommandSender sender, Player player, PlayerInfo pi) {
        if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!WorldGuardHandler.protectIsland(player, sender.getName(), pi)) {
                sender.sendMessage("Player doesn't have an island or it's already protected!");
            }
        }
    }

    private void createIsland(Player player, Location next) throws DataException, IOException, MaxChangedBlocksException {
        boolean hasIslandNow = false;
        if (getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            String cSchem = "";
            for (int i = 0; i < getInstance().getSchemFile().length; ++i) {
                if (!hasIslandNow) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getInstance().getSchemFile()[i].getName().substring(0, getInstance().getSchemFile()[i].getName().lastIndexOf(46));
                    } else {
                        cSchem = getInstance().getSchemFile()[i].getName();
                    }
                    if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, getSkyBlockWorld()) && WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getInstance().getSchemFile()[i], next)) {
                        this.setChest(next, player);
                        hasIslandNow = true;
                    }
                }
            }
            if (!hasIslandNow) {
                for (int i = 0; i < getInstance().getSchemFile().length; ++i) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getInstance().getSchemFile()[i].getName().substring(0, getInstance().getSchemFile()[i].getName().lastIndexOf(46));
                    } else {
                        cSchem = getInstance().getSchemFile()[i].getName();
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName) && WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getInstance().getSchemFile()[i], next)) {
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

    private Location getNextIslandLocation(Location last) {
        while (getInstance().hasOrphanedIsland()) {
            if (!getInstance().islandAtLocation(getInstance().checkOrphan())) {
                break;
            }
            getInstance().removeNextOrphan();
        }
        while (getInstance().hasOrphanedIsland() && !getInstance().checkOrphan().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            getInstance().removeNextOrphan();
        }
        Location next;
        if (getInstance().hasOrphanedIsland() && !getInstance().islandAtLocation(getInstance().checkOrphan())) {
            next = getInstance().getOrphanedIsland();
            getInstance().saveOrphans();
        } else {
            next = this.nextIslandLocation(last);
            getInstance().setLastIsland(next);
            while (getInstance().islandAtLocation(next)) {
                next = this.nextIslandLocation(next);
            }
            while (getInstance().islandInSpawn(next)) {
                next = this.nextIslandLocation(next);
            }
            getInstance().setLastIsland(next);
        }
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
                    final String[] chestItemString = getInstance().getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                    String[] amountdata = new String[2];
                    for (int j = 0; j < chestItemString.length; ++j) {
                        amountdata = chestItemString[j].split(":");
                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
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
        int y = Settings.island_height;
        y = Settings.island_height + 3;
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
                    final String[] chestItemString = getInstance().getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                    String[] amountdata = new String[2];
                    for (int j = 0; j < chestItemString.length; ++j) {
                        amountdata = chestItemString[j].split(":");
                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
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
                                    final String[] chestItemString = getInstance().getConfig().getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                                    String[] amountdata = new String[2];
                                    for (int j = 0; j < chestItemString.length; ++j) {
                                        amountdata = chestItemString[j].split(":");
                                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
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
        getInstance().updatePartyNumber(player);
        getInstance().homeSet(player);
        getPlayerInfo(player).savePlayerConfig(player.getName());
    }

    public void setWarpLocation(final String location, final Location loc) {
        this.getIslandConfig(location).set("general.warpLocationX", loc.getBlockX());
        this.getIslandConfig(location).set("general.warpLocationY", loc.getBlockY());
        this.getIslandConfig(location).set("general.warpLocationZ", loc.getBlockZ());
        this.getIslandConfig(location).set("general.warpActive", true);
        this.saveIslandConfig(location);
    }

    public void buildIslandList() {
        final File folder = getInstance().directoryPlayers;
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
        for (int i = 0; i < listOfFiles.length; ++i) {
            final PlayerInfo pi = new PlayerInfo(listOfFiles[i].getName());
            if (!pi.getHasIsland() && pi.getPartyIslandLocation() != null && this.getTempIslandConfig(pi.locationForPartyOld()) != null && !this.getTempIslandConfig(pi.locationForPartyOld()).contains("party.members." + pi.getPlayerName())) {
                this.setupPartyMember(pi.locationForPartyOld(), pi.getPlayerName());
                this.saveIslandConfig(pi.locationForParty());
            }
        }
        System.out.print(ChatColor.YELLOW + "[uSkyBlock] Party list completed.");
    }

    public void removeIslandConfig(final String location) {
        this.islands.remove(location);
    }

    public void displayIslandConfigs() {
        final Iterator<String> islandList = this.islands.keySet().iterator();
        while (islandList.hasNext()) {
            System.out.print(islandList.next());
        }
    }

    public void updatePartyNumber(final Player player) {
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getInt("party.maxSize") < 8 && VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.maxSize", 8);
            getInstance().saveIslandConfig(getPlayerInfo(player).locationForParty());
            return;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getInt("party.maxSize") < 7 && VaultHandler.checkPerk(player.getName(), "usb.extra.party3", player.getWorld())) {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.maxSize", 7);
            getInstance().saveIslandConfig(getPlayerInfo(player).locationForParty());
            return;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getInt("party.maxSize") < 6 && VaultHandler.checkPerk(player.getName(), "usb.extra.party2", player.getWorld())) {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.maxSize", 6);
            getInstance().saveIslandConfig(getPlayerInfo(player).locationForParty());
            return;
        }
        if (getIslandConfig(getPlayerInfo(player).locationForParty()).getInt("party.maxSize") < 5 && VaultHandler.checkPerk(player.getName(), "usb.extra.party1", player.getWorld())) {
            getIslandConfig(getPlayerInfo(player).locationForParty()).set("party.maxSize", 5);
            getInstance().saveIslandConfig(getPlayerInfo(player).locationForParty());
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
        getInstance().saveIslandConfig(getPlayerInfo(player).locationForParty());
    }

    public boolean checkForOnlineMembers(final Player p) {
        for (final String tString : getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).getConfigurationSection("party.members").getKeys(false)) {
            if (Bukkit.getPlayer(tString) != null && !Bukkit.getPlayer(tString).getName().equalsIgnoreCase(p.getName())) {
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
        getInstance().saveIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty());
    }

    public boolean isPartyLeader(final Player player) {
        return getIslandConfig(player).getString("party.leader").equalsIgnoreCase(player.getName());
    }

    public void sendMessageToIslandGroup(final String location, final String message) {
        final Iterator<String> temp = getIslandConfig(location).getConfigurationSection("party.members").getKeys(false).iterator();
        this.date = new Date();
        final String dateTxt;
        final String myDateString = dateTxt = DateFormat.getDateInstance(3).format(this.date).toString();
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
}
