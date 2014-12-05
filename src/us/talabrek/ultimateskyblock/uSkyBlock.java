package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.plugin.java.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import java.io.*;
import org.bukkit.generator.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.*;
import org.bukkit.*;
import java.util.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.*;
import java.util.logging.*;
import org.bukkit.block.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import java.text.*;

public class uSkyBlock extends JavaPlugin
{
    public PluginDescriptionFile pluginFile;
    public Logger log;
    Date date;
    public DecimalFormat df;
    private FileConfiguration levelConfig;
    private FileConfiguration lastIslandConfig;
    private FileConfiguration orphans;
    private FileConfiguration tempIsland;
    private FileConfiguration tempPlayer;
    private HashMap<String, FileConfiguration> islands;
    private File levelConfigFile;
    private File orphanFile;
    private File lastIslandConfigFile;
    private File islandConfigFile;
    private File tempIslandFile;
    private File tempPlayerFile;
    public static World skyBlockWorld;
    private static uSkyBlock instance;
    public List<String> removeList;
    List<String> rankDisplay;
    public FileConfiguration configPlugin;
    public File filePlugin;
    private Location lastIsland;
    private Stack<Location> orphaned;
    private Stack<Location> tempOrphaned;
    private Stack<Location> reverseOrphaned;
    public File directoryPlayers;
    public File directoryIslands;
    private File directorySchematics;
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
    public Inventory GUIparty;
    public Inventory GUIpartyPlayer;
    public Inventory GUIisland;
    public Inventory GUIchallenge;
    public Inventory GUIbiome;
    public Inventory GUIlog;
    ItemStack pHead;
    ItemStack sign;
    ItemStack biome;
    ItemStack lock;
    ItemStack warpset;
    ItemStack warptoggle;
    ItemStack invite;
    ItemStack kick;
    ItemStack currentBiomeItem;
    ItemStack currentIslandItem;
    ItemStack currentChallengeItem;
    ItemStack currentLogItem;
    List<String> lores;
    Iterator<String> tempIt;
    private ArrayList<File> sfiles;
    
    static {
        uSkyBlock.skyBlockWorld = null;
    }
    
    public uSkyBlock() {
        super();
        this.df = new DecimalFormat(".#");
        this.levelConfig = null;
        this.lastIslandConfig = null;
        this.orphans = null;
        this.tempIsland = null;
        this.tempPlayer = null;
        this.islands = new HashMap<String, FileConfiguration>();
        this.levelConfigFile = null;
        this.orphanFile = null;
        this.lastIslandConfigFile = null;
        this.islandConfigFile = null;
        this.tempIslandFile = null;
        this.tempPlayerFile = null;
        this.removeList = new ArrayList<String>();
        this.orphaned = new Stack<Location>();
        this.tempOrphaned = new Stack<Location>();
        this.reverseOrphaned = new Stack<Location>();
        this.islandTestLocation = null;
        this.infoCooldown = new HashMap<String, Long>();
        this.restartCooldown = new HashMap<String, Long>();
        this.biomeCooldown = new HashMap<String, Long>();
        this.activePlayers = new HashMap<String, PlayerInfo>();
        this.challenges = new LinkedHashMap<String, List<String>>();
        this.requiredList = new HashMap<Integer, Integer>();
        this.purgeActive = false;
        this.skyblockData = null;
        this.skyblockDataFile = null;
        this.GUIparty = null;
        this.GUIpartyPlayer = null;
        this.GUIisland = null;
        this.GUIchallenge = null;
        this.GUIbiome = null;
        this.GUIlog = null;
        this.pHead = new ItemStack(397, 1, (short)3);
        this.sign = new ItemStack(323, 1);
        this.biome = new ItemStack(6, 1, (short)3);
        this.lock = new ItemStack(101, 1);
        this.warpset = new ItemStack(90, 1);
        this.warptoggle = new ItemStack(69, 1);
        this.invite = new ItemStack(398, 1);
        this.kick = new ItemStack(301, 1);
        this.currentBiomeItem = null;
        this.currentIslandItem = null;
        this.currentChallengeItem = null;
        this.currentLogItem = null;
        this.lores = new ArrayList<String>();
    }
    
    public void onDisable() {
        try {
            this.unloadPlayerFiles();
            if (this.lastIsland != null) {
                this.setLastIsland(this.lastIsland);
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong saving the island and/or party data!");
            e.printStackTrace();
        }
        this.log.info(String.valueOf(this.pluginFile.getName()) + " v" + this.pluginFile.getVersion() + " disabled.");
    }
    
    public void onEnable() {
        (uSkyBlock.instance = this).saveDefaultConfig();
        this.saveDefaultLevelConfig();
        this.saveDefaultOrphans();
        this.pluginFile = this.getDescription();
        this.log = this.getLogger();
        this.pName = ChatColor.WHITE + "[" + ChatColor.GREEN + this.pluginFile.getName() + ChatColor.WHITE + "] ";
        VaultHandler.setupEconomy();
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        this.configPlugin = this.getConfig();
        this.filePlugin = new File(this.getDataFolder(), "config.yml");
        this.loadPluginConfig();
        this.loadLevelConfig();
        this.registerEvents();
        this.directoryPlayers = new File(this.getDataFolder() + File.separator + "players");
        this.directoryIslands = new File(this.getDataFolder() + File.separator + "islands");
        if (!this.directoryPlayers.exists()) {
            this.directoryPlayers.mkdir();
            this.loadPlayerFiles();
        }
        else {
            this.loadPlayerFiles();
        }
        if (!this.directoryIslands.exists()) {
            this.directoryIslands.mkdir();
        }
        this.directorySchematics = new File(this.getDataFolder() + File.separator + "schematics");
        if (!this.directorySchematics.exists()) {
            this.directorySchematics.mkdir();
        }
        this.schemFile = this.directorySchematics.listFiles();
        if (this.schemFile == null) {
            System.out.print("[uSkyBlock] No schematic file loaded.");
        }
        else {
            System.out.print("[uSkyBlock] " + this.schemFile.length + " schematics loaded.");
        }
        this.getCommand("island").setExecutor((CommandExecutor)new IslandCommand());
        this.getCommand("challenges").setExecutor((CommandExecutor)new ChallengesCommand());
        this.getCommand("dev").setExecutor((CommandExecutor)new DevCommand());
        if (Settings.island_useTopTen) {
            getInstance().updateTopTen(getInstance().generateTopTen());
        }
        this.populateChallengeList();
        this.log.info(String.valueOf(this.pluginFile.getName()) + " v." + this.pluginFile.getVersion() + " enabled.");
        getInstance().getServer().getScheduler().runTaskLater((Plugin)getInstance(), (Runnable)new Runnable() {
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
                            uSkyBlock.this.getLastIslandConfig().set("options.general.lastIslandX", (Object)uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"));
                            uSkyBlock.this.getLastIslandConfig().set("options.general.lastIslandZ", (Object)uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ"));
                            uSkyBlock.this.saveLastIslandConfig();
                        }
                        uSkyBlock.access$0(uSkyBlock.this, new Location(uSkyBlock.getSkyBlockWorld(), (double)uSkyBlock.this.getLastIslandConfig().getInt("options.general.lastIslandX"), (double)Settings.island_height, (double)uSkyBlock.this.getLastIslandConfig().getInt("options.general.lastIslandZ")));
                    }
                    catch (Exception e) {
                        uSkyBlock.access$0(uSkyBlock.this, new Location(uSkyBlock.getSkyBlockWorld(), (double)uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"), (double)Settings.island_height, (double)uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ")));
                    }
                    if (uSkyBlock.this.lastIsland == null) {
                        uSkyBlock.access$0(uSkyBlock.this, new Location(uSkyBlock.getSkyBlockWorld(), 0.0, (double)Settings.island_height, 0.0));
                    }
                    if (Settings.island_protectWithWorldGuard && !Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                        final PluginManager manager = uSkyBlock.getInstance().getServer().getPluginManager();
                        System.out.print("[uSkyBlock] WorldGuard not loaded! Using built in protection.");
                        manager.registerEvents((Listener)new ProtectionEvents(), (Plugin)uSkyBlock.getInstance());
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
        int onlinePlayerCount = 0;
        onlinePlayerCount = Bukkit.getServer().getOnlinePlayers().size();
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
                        if (getInstance().getIslandConfig(pi.locationForParty()) == null) {
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
                getInstance().getIslandConfig(pi.locationForParty());
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
        manager.registerEvents((Listener)new PlayerJoin(), (Plugin)this);
        if (!Settings.island_protectWithWorldGuard) {
            System.out.print("[uSkyBlock] Using built in protection.");
            manager.registerEvents((Listener)new ProtectionEvents(), (Plugin)getInstance());
        }
        else {
            System.out.print("[uSkyBlock] Using WorldGuard protection.");
        }
    }
    
    public void loadPluginConfig() {
        try {
            this.getConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Settings.general_maxPartySize = this.getConfig().getInt("options.general.maxPartySize");
            if (Settings.general_maxPartySize < 0) {
                Settings.general_maxPartySize = 0;
            }
        }
        catch (Exception e) {
            Settings.general_maxPartySize = 4;
        }
        try {
            Settings.island_distance = this.getConfig().getInt("options.island.distance");
            if (Settings.island_distance < 50) {
                Settings.island_distance = 50;
            }
        }
        catch (Exception e) {
            Settings.island_distance = 110;
        }
        try {
            Settings.island_protectionRange = this.getConfig().getInt("options.island.protectionRange");
            if (Settings.island_protectionRange > Settings.island_distance) {
                Settings.island_protectionRange = Settings.island_distance;
            }
        }
        catch (Exception e) {
            Settings.island_protectionRange = 100;
        }
        try {
            Settings.general_cooldownInfo = this.getConfig().getInt("options.general.cooldownInfo");
            if (Settings.general_cooldownInfo < 0) {
                Settings.general_cooldownInfo = 0;
            }
        }
        catch (Exception e) {
            Settings.general_cooldownInfo = 60;
        }
        try {
            Settings.general_biomeChange = this.getConfig().getInt("options.general.biomeChange");
            if (Settings.general_biomeChange < 0) {
                Settings.general_biomeChange = 0;
            }
        }
        catch (Exception e) {
            Settings.general_biomeChange = 3600;
        }
        try {
            Settings.general_cooldownRestart = this.getConfig().getInt("options.general.cooldownRestart");
            if (Settings.general_cooldownRestart < 0) {
                Settings.general_cooldownRestart = 0;
            }
        }
        catch (Exception e) {
            Settings.general_cooldownRestart = 60;
        }
        try {
            Settings.island_height = this.getConfig().getInt("options.island.height");
            if (Settings.island_height < 20) {
                Settings.island_height = 20;
            }
        }
        catch (Exception e) {
            Settings.island_height = 120;
        }
        try {
            Settings.challenges_rankLeeway = this.getConfig().getInt("options.challenges.rankLeeway");
            if (Settings.challenges_rankLeeway < 0) {
                Settings.challenges_rankLeeway = 0;
            }
        }
        catch (Exception e) {
            Settings.challenges_rankLeeway = 0;
        }
        if (!this.getConfig().contains("options.extras.obsidianToLava")) {
            this.getConfig().set("options.extras.obsidianToLava", (Object)true);
            this.saveConfig();
        }
        if (!this.getConfig().contains("options.general.spawnSize")) {
            this.getConfig().set("options.general.spawnSize", (Object)50);
            this.saveConfig();
        }
        try {
            Settings.general_spawnSize = this.getConfig().getInt("options.general.spawnSize");
            if (Settings.general_spawnSize < 50) {
                Settings.general_spawnSize = 50;
            }
        }
        catch (Exception e) {
            Settings.general_spawnSize = 50;
        }
        final String[] chestItemString = this.getConfig().getString("options.island.chestItems").split(" ");
        final ItemStack[] tempChest = new ItemStack[chestItemString.length];
        String[] amountdata = new String[2];
        for (int i = 0; i < tempChest.length; ++i) {
            amountdata = chestItemString[i].split(":");
            tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
        }
        Settings.island_chestItems = tempChest;
        Settings.island_allowPvP = this.getConfig().getString("options.island.allowPvP");
        Settings.island_schematicName = this.getConfig().getString("options.island.schematicName");
        if (!Settings.island_allowPvP.equalsIgnoreCase("allow")) {
            Settings.island_allowPvP = "deny";
        }
        final Set<String> permissionList = (Set<String>)this.getConfig().getConfigurationSection("options.island.extraPermissions").getKeys(true);
        Settings.island_addExtraItems = this.getConfig().getBoolean("options.island.addExtraItems");
        Settings.extras_obsidianToLava = this.getConfig().getBoolean("options.extras.obsidianToLava");
        Settings.island_useIslandLevel = this.getConfig().getBoolean("options.island.useIslandLevel");
        Settings.island_extraPermissions = permissionList.toArray(new String[0]);
        Settings.island_protectWithWorldGuard = this.getConfig().getBoolean("options.island.protectWithWorldGuard");
        Settings.extras_sendToSpawn = this.getConfig().getBoolean("options.extras.sendToSpawn");
        Settings.island_useTopTen = this.getConfig().getBoolean("options.island.useTopTen");
        Settings.general_worldName = this.getConfig().getString("options.general.worldName");
        Settings.island_removeCreaturesByTeleport = this.getConfig().getBoolean("options.island.removeCreaturesByTeleport");
        Settings.island_allowIslandLock = this.getConfig().getBoolean("options.island.allowIslandLock");
        Settings.island_useOldIslands = this.getConfig().getBoolean("options.island.useOldIslands");
        final Set<String> challengeList = Settings.challenges_challengeList = (Set<String>)this.getConfig().getConfigurationSection("options.challenges.challengeList").getKeys(false);
        Settings.challenges_broadcastCompletion = this.getConfig().getBoolean("options.challenges.broadcastCompletion");
        Settings.challenges_broadcastText = this.getConfig().getString("options.challenges.broadcastText");
        Settings.challenges_challengeColor = this.getConfig().getString("options.challenges.challengeColor");
        Settings.challenges_enableEconomyPlugin = this.getConfig().getBoolean("options.challenges.enableEconomyPlugin");
        Settings.challenges_finishedColor = this.getConfig().getString("options.challenges.finishedColor");
        Settings.challenges_repeatableColor = this.getConfig().getString("options.challenges.repeatableColor");
        Settings.challenges_requirePreviousRank = this.getConfig().getBoolean("options.challenges.requirePreviousRank");
        Settings.challenges_allowChallenges = this.getConfig().getBoolean("options.challenges.allowChallenges");
        final String[] rankListString = Settings.challenges_ranks = this.getConfig().getString("options.challenges.ranks").split(" ");
    }
    
    public List<Party> readPartyFile() {
        final File f = new File(this.getDataFolder(), "partylist.bin");
        if (!f.exists()) {
            return null;
        }
        try {
            final FileInputStream fileIn = new FileInputStream(f);
            final ObjectInputStream in = new ObjectInputStream(fileIn);
            final List<Party> p = (List<Party>)in.readObject();
            in.close();
            fileIn.close();
            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void writePartyFile(final List<Party> pi) {
        final File f = new File(this.getDataFolder(), "partylist.bin");
        try {
            final FileOutputStream fileOut = new FileOutputStream(f);
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(pi);
            out.flush();
            out.close();
            fileOut.close();
        }
        catch (Exception e) {
            e.printStackTrace();
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
            final PlayerInfo p = (PlayerInfo)in.readObject();
            in.close();
            fileIn.close();
            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean displayTopTen(final Player player) {
        int i = 1;
        int playerrank = 0;
        player.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");
        if (topTen == null) {
            player.sendMessage(ChatColor.RED + "Top ten list not generated yet!");
            return false;
        }
        for (final String playerName : topTen.keySet()) {
            if (i <= 10) {
                player.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + " - Island level " + topTen.get(playerName));
            }
            if (playerName.equalsIgnoreCase(player.getName())) {
                playerrank = i;
            }
            ++i;
        }
        player.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank);
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
            return new Location(w, (double)x, (double)y, (double)z);
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
        fc.set(path, (Object)value.toString());
        try {
            fc.save(f);
        }
        catch (IOException e) {
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
            uSkyBlock.skyBlockWorld = WorldCreator.name(Settings.general_worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator((ChunkGenerator)new SkyBlockChunkGenerator()).createWorld();
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
                Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "mv import " + Settings.general_worldName + " normal -g uSkyBlock");
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
                if (!f.isDirectory()) {
                    this.sfiles.add(f);
                }
                else {
                    this.getAllFiles(f.getAbsolutePath());
                }
            }
            catch (Exception ex) {
                this.log.warning(ex.getMessage());
            }
        }
    }
    
    public Location getYLocation(final Location l) {
        for (int y = 0; y < 254; ++y) {
            final int px = l.getBlockX();
            final int py = y;
            final int pz = l.getBlockZ();
            final Block b1 = new Location(l.getWorld(), (double)px, (double)py, (double)pz).getBlock();
            final Block b2 = new Location(l.getWorld(), (double)px, (double)(py + 1), (double)pz).getBlock();
            final Block b3 = new Location(l.getWorld(), (double)px, (double)(py + 2), (double)pz).getBlock();
            if (!b1.getType().equals((Object)Material.AIR) && b2.getType().equals((Object)Material.AIR) && b3.getType().equals((Object)Material.AIR)) {
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
        }
        else {
            home = p.getHomeLocation();
        }
        if (this.isSafeLocation(home)) {
            return home;
        }
        for (int y = home.getBlockY() + 25; y > 0; --y) {
            final Location n = new Location(home.getWorld(), (double)home.getBlockX(), (double)y, (double)home.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        for (int y = home.getBlockY(); y < 255; ++y) {
            final Location n = new Location(home.getWorld(), (double)home.getBlockX(), (double)y, (double)home.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        final Location island = p.getIslandLocation();
        if (this.isSafeLocation(island)) {
            return island;
        }
        for (int y2 = island.getBlockY() + 25; y2 > 0; --y2) {
            final Location n2 = new Location(island.getWorld(), (double)island.getBlockX(), (double)y2, (double)island.getBlockZ());
            if (this.isSafeLocation(n2)) {
                return n2;
            }
        }
        for (int y2 = island.getBlockY(); y2 < 255; ++y2) {
            final Location n2 = new Location(island.getWorld(), (double)island.getBlockX(), (double)y2, (double)island.getBlockZ());
            if (this.isSafeLocation(n2)) {
                return n2;
            }
        }
        return p.getHomeLocation();
    }
    
    public Location getSafeWarpLocation(final PlayerInfo p) {
        Location warp = null;
        this.getTempIslandConfig(p.locationForParty());
        if (this.tempIsland.getInt("general.warpLocationX") == 0) {
            if (p.getHomeLocation() == null) {
                if (p.getIslandLocation() != null) {
                    warp = p.getIslandLocation();
                }
            }
            else {
                warp = p.getHomeLocation();
            }
        }
        else {
            warp = new Location(uSkyBlock.skyBlockWorld, (double)this.tempIsland.getInt("general.warpLocationX"), (double)this.tempIsland.getInt("general.warpLocationY"), (double)this.tempIsland.getInt("general.warpLocationZ"));
        }
        if (warp == null) {
            System.out.print("Error warping player to " + p.getPlayerName() + "'s island.");
            return null;
        }
        if (this.isSafeLocation(warp)) {
            return warp;
        }
        for (int y = warp.getBlockY() + 25; y > 0; --y) {
            final Location n = new Location(warp.getWorld(), (double)warp.getBlockX(), (double)y, (double)warp.getBlockZ());
            if (this.isSafeLocation(n)) {
                return n;
            }
        }
        for (int y = warp.getBlockY(); y < 255; ++y) {
            final Location n = new Location(warp.getWorld(), (double)warp.getBlockX(), (double)y, (double)warp.getBlockZ());
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
        return !ground.getType().equals((Object)Material.AIR) && !ground.getType().equals((Object)Material.LAVA) && !ground.getType().equals((Object)Material.STATIONARY_LAVA) && !ground.getType().equals((Object)Material.CACTUS) && ((air1.getType().equals((Object)Material.AIR) || air1.getType().equals((Object)Material.CROPS) || air1.getType().equals((Object)Material.LONG_GRASS) || air1.getType().equals((Object)Material.RED_ROSE) || air1.getType().equals((Object)Material.YELLOW_FLOWER) || air1.getType().equals((Object)Material.DEAD_BUSH) || air1.getType().equals((Object)Material.SIGN_POST) || air1.getType().equals((Object)Material.SIGN)) && air2.getType().equals((Object)Material.AIR));
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
                final Chunk c = l.getWorld().getChunkAt(new Location(l.getWorld(), (double)(px + x * 16), (double)py, (double)(pz + z * 16)));
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
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(String.valueOf(player) + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(String.valueOf(player) + "Island");
            }
            this.orphaned.push(pi.getIslandLocation());
            this.removeIsland(pi.getIslandLocation());
            this.deleteIslandConfig(pi.locationForParty());
            pi.removeFromIsland();
            this.saveOrphans();
            pi.savePlayerConfig(player);
        }
        else {
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(String.valueOf(player) + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(String.valueOf(player) + "Island");
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
    
    public void restartPlayerIsland(final Player player, final Location next) {
        boolean hasIslandNow = false;
        if (next.getBlockX() == 0 && next.getBlockZ() == 0) {
            return;
        }
        this.removeIsland(next);
        if (getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            String cSchem = "";
            for (int i = 0; i < getInstance().getSchemFile().length; ++i) {
                if (!hasIslandNow) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getInstance().getSchemFile()[i].getName().substring(0, getInstance().getSchemFile()[i].getName().lastIndexOf(46));
                    }
                    else {
                        cSchem = getInstance().getSchemFile()[i].getName();
                    }
                    if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, getSkyBlockWorld())) {
                        try {
                            if (WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getInstance().getSchemFile()[i], next)) {
                                this.setChest(next, player);
                                hasIslandNow = true;
                            }
                        }
                        catch (MaxChangedBlocksException e) {
                            e.printStackTrace();
                        }
                        catch (DataException e2) {
                            e2.printStackTrace();
                        }
                        catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
            }
            if (!hasIslandNow) {
                for (int i = 0; i < getInstance().getSchemFile().length; ++i) {
                    if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                        cSchem = getInstance().getSchemFile()[i].getName().substring(0, getInstance().getSchemFile()[i].getName().lastIndexOf(46));
                    }
                    else {
                        cSchem = getInstance().getSchemFile()[i].getName();
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)) {
                        try {
                            if (WorldEditHandler.loadIslandSchematic(getSkyBlockWorld(), getInstance().getSchemFile()[i], next)) {
                                this.setChest(next, player);
                                hasIslandNow = true;
                            }
                        }
                        catch (MaxChangedBlocksException e) {
                            e.printStackTrace();
                        }
                        catch (DataException e2) {
                            e2.printStackTrace();
                        }
                        catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                this.generateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, getSkyBlockWorld());
            }
            else {
                this.oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, getSkyBlockWorld());
            }
        }
        next.setY((double)Settings.island_height);
        System.out.println(next.getBlockY());
        this.setNewPlayerIsland(player, next);
        player.getInventory().clear();
        player.getEquipment().clear();
        getInstance().changePlayerBiome(player, "OCEAN");
        for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
            for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
                getSkyBlockWorld().refreshChunk((next.getBlockX() + x) / 16, (next.getBlockZ() + z) / 16);
            }
        }
        for (final Entity tempent : player.getNearbyEntities((double)(Settings.island_protectionRange / 2), 250.0, (double)(Settings.island_protectionRange / 2))) {
            if (!(tempent instanceof Player)) {
                tempent.remove();
            }
        }
    }
    
    public void devDeletePlayerIsland(final String player) {
        if (!this.getActivePlayers().containsKey(player)) {
            PlayerInfo pi = new PlayerInfo(player);
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(String.valueOf(player) + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(String.valueOf(player) + "Island");
            }
            pi = new PlayerInfo(player);
            pi.savePlayerConfig(player);
        }
        else {
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(String.valueOf(player) + "Island")) {
                WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(String.valueOf(player) + "Island");
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
                        final Block b = new Location(l.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
                        if (b.getTypeId() == 7) {
                            pi.setHomeLocation(new Location(l.getWorld(), (double)(px + x), (double)(py + y + 3), (double)(pz + z)));
                            pi.setHasIsland(true);
                            pi.setIslandLocation(b.getLocation());
                            pi.savePlayerConfig(player);
                            getInstance().createIslandConfig(pi.locationForParty(), player);
                            getInstance().clearIslandConfig(pi.locationForParty(), player);
                            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                                WorldGuardHandler.protectIsland(sender, player, pi);
                            }
                            getInstance().getIslandConfig(pi.locationForParty());
                            return true;
                        }
                    }
                }
            }
        }
        else {
            final int px2 = l.getBlockX();
            final int py2 = l.getBlockY();
            final int pz2 = l.getBlockZ();
            for (int x2 = -10; x2 <= 10; ++x2) {
                for (int y2 = -10; y2 <= 10; ++y2) {
                    for (int z2 = -10; z2 <= 10; ++z2) {
                        final Block b2 = new Location(l.getWorld(), (double)(px2 + x2), (double)(py2 + y2), (double)(pz2 + z2)).getBlock();
                        if (b2.getTypeId() == 7) {
                            this.getActivePlayers().get(player).setHomeLocation(new Location(l.getWorld(), (double)(px2 + x2), (double)(py2 + y2 + 3), (double)(pz2 + z2)));
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
                        final Block b = new Location(loc.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
                        if (!b.getType().equals((Object)Material.AIR)) {
                            if (b.getType().equals((Object)Material.CHEST)) {
                                final Chest c = (Chest)b.getState();
                                final ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
                                c.getInventory().setContents(items);
                            }
                            else if (b.getType().equals((Object)Material.FURNACE)) {
                                final Furnace f = (Furnace)b.getState();
                                final ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
                                f.getInventory().setContents(items);
                            }
                            else if (b.getType().equals((Object)Material.DISPENSER)) {
                                final Dispenser d = (Dispenser)b.getState();
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
                        final Block b = new Location(loc.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
                        if (!b.getType().equals((Object)Material.AIR)) {
                            if (b.getType().equals((Object)Material.CHEST)) {
                                final Chest c = (Chest)b.getState();
                                final ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
                                c.getInventory().setContents(items);
                            }
                            else if (b.getType().equals((Object)Material.FURNACE)) {
                                final Furnace f = (Furnace)b.getState();
                                final ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
                                f.getInventory().setContents(items);
                            }
                            else if (b.getType().equals((Object)Material.DISPENSER)) {
                                final Dispenser d = (Dispenser)b.getState();
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
        this.setLastIsland(new Location(getSkyBlockWorld(), 0.0, (double)Settings.island_height, 0.0));
        return new Location(getSkyBlockWorld(), 0.0, (double)Settings.island_height, 0.0);
    }
    
    public void setLastIsland(final Location island) {
        this.getLastIslandConfig().set("options.general.lastIslandX", (Object)island.getBlockX());
        this.getLastIslandConfig().set("options.general.lastIslandZ", (Object)island.getBlockZ());
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
        this.tempOrphaned = (Stack<Location>)this.orphaned.clone();
        while (!this.tempOrphaned.isEmpty()) {
            this.reverseOrphaned.push(this.tempOrphaned.pop());
        }
        while (!this.reverseOrphaned.isEmpty()) {
            final Location tempLoc = this.reverseOrphaned.pop();
            fullOrphan = String.valueOf(fullOrphan) + tempLoc.getBlockX() + "," + tempLoc.getBlockZ() + ";";
        }
        this.getOrphans().set("orphans.list", (Object)fullOrphan);
        this.saveOrphansFile();
    }
    
    public void setupOrphans() {
        if (this.getOrphans().contains("orphans.list")) {
            final String fullOrphan = this.getOrphans().getString("orphans.list");
            if (!fullOrphan.isEmpty()) {
                final String[] orphanArray = fullOrphan.split(";");
                this.orphaned = new Stack<Location>();
                for (int i = 0; i < orphanArray.length; ++i) {
                    final String[] orphanXY = orphanArray[i].split(",");
                    final Location tempLoc = new Location(getSkyBlockWorld(), (double)Integer.parseInt(orphanXY[0]), (double)Settings.island_height, (double)Integer.parseInt(orphanXY[1]));
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
        }
        else {
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
                    final Block b = new Location(loc.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
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
        final Stack<SerializableLocation> finishStack = new Stack<SerializableLocation>();
        final Stack<Location> tempStack = new Stack<Location>();
        while (!stack.isEmpty()) {
            tempStack.push(stack.pop());
        }
        while (!tempStack.isEmpty()) {
            if (tempStack.peek() != null) {
                finishStack.push(new SerializableLocation(tempStack.pop()));
            }
            else {
                tempStack.pop();
            }
        }
        return finishStack;
    }
    
    public Stack<Location> changestackfromfile(final Stack<SerializableLocation> stack) {
        final Stack<SerializableLocation> tempStack = new Stack<SerializableLocation>();
        final Stack<Location> finishStack = new Stack<Location>();
        while (!stack.isEmpty()) {
            tempStack.push(stack.pop());
        }
        while (!tempStack.isEmpty()) {
            if (tempStack.peek() != null) {
                finishStack.push(tempStack.pop().getLocation());
            }
            else {
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
                    final Block b = new Location(l.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
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
                            final Block b = new Location(l.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
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
        final HashMap<String, Double> tempMap = new LinkedHashMap<String, Double>();
        final File folder = this.directoryIslands;
        final File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; ++i) {
            if (this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", "")) != null && this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", "")).getInt("general.level") > 0) {
                tempMap.put(this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", "")).getString("party.leader"), (double)this.getTempIslandConfig(listOfFiles[i].getName().replaceAll(".yml", "")).getInt("general.level"));
            }
        }
        final LinkedHashMap<String, Double> sortedMap = this.sortHashMapByValuesD(tempMap);
        return sortedMap;
    }
    
    public LinkedHashMap<String, Double> sortHashMapByValuesD(final HashMap<String, Double> passedMap) {
        final List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        final List<Double> mapValues = new ArrayList<Double>(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapKeys);
        final LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (final Double val : mapValues) {
            for (final String key : mapKeys) {
                final String comp1 = passedMap.get(key).toString();
                final String comp2 = val.toString();
                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
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
        getInstance().getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)getInstance(), (Runnable)new Runnable() {
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
        List<String> templist = new ArrayList<String>();
        for (int i = 0; i < Settings.challenges_ranks.length; ++i) {
            this.challenges.put(Settings.challenges_ranks[i], templist);
            templist = new ArrayList<String>();
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
                    fullString = String.valueOf(fullString) + Settings.challenges_repeatableColor + tempString + ChatColor.DARK_GRAY + " - ";
                }
                else {
                    fullString = String.valueOf(fullString) + Settings.challenges_finishedColor + tempString + ChatColor.DARK_GRAY + " - ";
                }
            }
            else {
                fullString = String.valueOf(fullString) + Settings.challenges_challengeColor + tempString + ChatColor.DARK_GRAY + " - ";
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
        }
        else if (this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")) {
            if (!this.playerIsOnIsland(player)) {
                player.sendMessage(ChatColor.RED + "You must be on your island to do that!");
            }
            if (!this.hasRequired(player, challenge, "onIsland")) {
                player.sendMessage(ChatColor.RED + this.getConfig().getString("options.challenges.challengeList." + challenge + ".description"));
                player.sendMessage(ChatColor.RED + "You must be standing within 10 blocks of all required items.");
                return false;
            }
            return true;
        }
        else {
            if (!this.getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("islandLevel")) {
                return false;
            }
            if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("general.level") >= this.getConfig().getInt("options.challenges.challengeList." + challenge + ".requiredItems")) {
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
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                    }
                    if (!player.getInventory().contains(reqItem, reqAmount)) {
                        return false;
                    }
                    player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount) });
                }
                else if (sPart.length == 3) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[2].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[2]);
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                    }
                    reqMod = Integer.parseInt(sPart[1]);
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short)reqMod), reqAmount)) {
                        return false;
                    }
                    player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount, (short)reqMod) });
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
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                    }
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short)0), reqAmount)) {
                        return false;
                    }
                }
                else if (sPart.length == 3) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[2].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[2]);
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge);
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challenge));
                        }
                    }
                    reqMod = Integer.parseInt(sPart[1]);
                    if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short)reqMod), reqAmount)) {
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
                        final Block b = new Location(l.getWorld(), (double)(px + x), (double)(py + y), (double)(pz + z)).getBlock();
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
        if (getInstance().getActivePlayers().get(player.getName()).checkChallenge(challenge) == 0) {
            rewList = this.getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".itemReward").split(" ");
            if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
                rewCurrency = this.getConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".currencyReward");
            }
        }
        else {
            rewList = this.getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatItemReward").split(" ");
            if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
                rewCurrency = this.getConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatCurrencyReward");
            }
        }
        int rewItem = 0;
        int rewAmount = 0;
        double rewBonus = 1.0;
        int rewMod = -1;
        if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
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
            VaultHandler.econ.depositPlayer(player.getName(), rewCurrency * rewBonus);
            if (getInstance().getActivePlayers().get(player.getName()).checkChallenge(challenge) > 0) {
                player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
                player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".repeatRewardText"));
                player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
                player.sendMessage(ChatColor.YELLOW + "Repeat currency reward: " + ChatColor.WHITE + this.df.format(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatCurrencyReward") * rewBonus) + " " + VaultHandler.econ.currencyNamePlural() + "�a(+" + this.df.format((rewBonus - 1.0) * 100.0) + "%)");
            }
            else {
                if (Settings.challenges_broadcastCompletion) {
                    Bukkit.getServer().broadcastMessage(String.valueOf(Settings.challenges_broadcastText) + player.getName() + " has completed the " + challenge + " challenge!");
                }
                player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
                player.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".rewardText"));
                player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
                player.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + this.df.format(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".currencyReward") * rewBonus) + " " + VaultHandler.econ.currencyNamePlural() + "�a(+" + this.df.format((rewBonus - 1.0) * 100.0) + "%)");
            }
        }
        else if (getInstance().getActivePlayers().get(player.getName()).checkChallenge(challenge) > 0) {
            player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
            player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + getInstance().getConfig().getString("options.challenges.challengeList." + challenge + ".repeatRewardText"));
            player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
        }
        else {
            if (Settings.challenges_broadcastCompletion) {
                Bukkit.getServer().broadcastMessage(String.valueOf(Settings.challenges_broadcastText) + player.getName() + " has completed the " + challenge + " challenge!");
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
                player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount) });
            }
            else if (sPart.length == 3) {
                rewItem = Integer.parseInt(sPart[0]);
                rewAmount = Integer.parseInt(sPart[2]);
                rewMod = Integer.parseInt(sPart[1]);
                player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount, (short)rewMod) });
            }
        }
        getInstance().getActivePlayers().get(player.getName()).completeChallenge(challenge);
        return true;
    }
    
    public void reloadData() {
        if (this.skyblockDataFile == null) {
            this.skyblockDataFile = new File(this.getDataFolder(), "skyblockData.yml");
        }
        this.skyblockData = (FileConfiguration)YamlConfiguration.loadConfiguration(this.skyblockDataFile);
        final InputStream defConfigStream = this.getResource("skyblockData.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.skyblockData.setDefaults((Configuration)defConfig);
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
        this.levelConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.levelConfigFile);
        final InputStream defConfigStream = this.getResource("levelConfig.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.levelConfig.setDefaults((Configuration)defConfig);
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
        }
        catch (IOException ex) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= 255; ++i) {
            if (this.getLevelConfig().contains("blockValues." + i)) {
                Settings.blockList[i] = this.getLevelConfig().getInt("blockValues." + i);
            }
            else {
                Settings.blockList[i] = this.getLevelConfig().getInt("general.default");
            }
            if (this.getLevelConfig().contains("blockLimits." + i)) {
                Settings.limitList[i] = this.getLevelConfig().getInt("blockLimits." + i);
            }
            else {
                Settings.limitList[i] = -1;
            }
            if (this.getLevelConfig().contains("diminishingReturns." + i)) {
                Settings.diminishingReturnsList[i] = this.getLevelConfig().getInt("diminishingReturns." + i);
            }
            else if (this.getLevelConfig().getBoolean("general.useDiminishingReturns")) {
                Settings.diminishingReturnsList[i] = this.getLevelConfig().getInt("general.defaultScale");
            }
            else {
                Settings.diminishingReturnsList[i] = -1;
            }
        }
        System.out.print(Settings.blockList[57]);
        System.out.print(Settings.diminishingReturnsList[57]);
        System.out.print(Settings.limitList[57]);
    }
    
    public void clearIslandConfig(final String location, final String leader) {
        this.getIslandConfig(location).set("general.level", (Object)0);
        this.getIslandConfig(location).set("general.warpLocationX", (Object)0);
        this.getIslandConfig(location).set("general.warpLocationY", (Object)0);
        this.getIslandConfig(location).set("general.warpLocationZ", (Object)0);
        this.getIslandConfig(location).set("general.warpActive", (Object)false);
        this.getIslandConfig(location).set("log.logPos", (Object)1);
        this.getIslandConfig(location).set("log.1", (Object)"�d[skyblock] The island has been created.");
        this.setupPartyLeader(location, leader);
    }
    
    public void setupPartyLeader(final String location, final String leader) {
        this.getIslandConfig(location).createSection("party.members." + leader);
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canChangeBiome");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canToggleLock");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canChangeWarp");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canToggleWarp");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canInviteOthers");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + leader), "canKickOthers");
        this.getIslandConfig(location).set("party.leader", (Object)leader);
        this.getIslandConfig(location).set("party.members." + leader + ".canChangeBiome", (Object)true);
        this.getIslandConfig(location).set("party.members." + leader + ".canToggleLock", (Object)true);
        this.getIslandConfig(location).set("party.members." + leader + ".canChangeWarp", (Object)true);
        this.getIslandConfig(location).set("party.members." + leader + ".canToggleWarp", (Object)true);
        this.getIslandConfig(location).set("party.members." + leader + ".canInviteOthers", (Object)true);
        this.getIslandConfig(location).set("party.members." + leader + ".canKickOthers", (Object)true);
        this.saveIslandConfig(location);
    }
    
    public void setupPartyMember(final String location, final String member) {
        this.getIslandConfig(location).createSection("party.members." + member);
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canChangeBiome");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canToggleLock");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canChangeWarp");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canToggleWarp");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canInviteOthers");
        this.getIslandConfig(location);
        FileConfiguration.createPath(this.getIslandConfig(location).getConfigurationSection("party.members." + member), "canKickOthers");
        this.getIslandConfig(location).set("party.members." + member + ".canChangeBiome", (Object)false);
        this.getIslandConfig(location).set("party.currentSize", (Object)(this.getIslandConfig(location).getInt("party.currentSize") + 1));
        this.getIslandConfig(location).set("party.members." + member + ".canToggleLock", (Object)false);
        this.getIslandConfig(location).set("party.members." + member + ".canChangeWarp", (Object)false);
        this.getIslandConfig(location).set("party.members." + member + ".canToggleWarp", (Object)false);
        this.getIslandConfig(location).set("party.members." + member + ".canInviteOthers", (Object)false);
        this.getIslandConfig(location).set("party.members." + member + ".canKickOthers", (Object)false);
        this.getIslandConfig(location).set("party.members." + member + ".canBanOthers", (Object)false);
        this.saveIslandConfig(location);
    }
    
    public void reloadIslandConfig(final String location) {
        this.islandConfigFile = new File(this.directoryIslands, String.valueOf(location) + ".yml");
        this.islands.put(location, (FileConfiguration)YamlConfiguration.loadConfiguration(this.islandConfigFile));
        final InputStream defConfigStream = this.getResource("island.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.islands.get(location).setDefaults((Configuration)defConfig);
        }
        this.saveIslandConfig(location);
    }
    
    public FileConfiguration getTempIslandConfig(final String location) {
        this.tempIslandFile = new File(this.directoryIslands, String.valueOf(location) + ".yml");
        return this.tempIsland = (FileConfiguration)YamlConfiguration.loadConfiguration(this.tempIslandFile);
    }
    
    public FileConfiguration getCurrentPlayerConfig(final String player) {
        this.tempPlayerFile = new File(this.directoryPlayers, String.valueOf(player) + ".yml");
        return this.tempPlayer = (FileConfiguration)YamlConfiguration.loadConfiguration(this.tempPlayerFile);
    }
    
    public void createIslandConfig(final String location, final String leader) {
        this.saveDefaultIslandsConfig(location);
        this.islandConfigFile = new File(this.directoryIslands, String.valueOf(location) + ".yml");
        final InputStream defConfigStream = this.getResource("island.yml");
        if (defConfigStream != null) {
            this.islands.put(location, (FileConfiguration)YamlConfiguration.loadConfiguration(defConfigStream));
            this.getIslandConfig(location);
            this.setupPartyLeader(location, leader);
        }
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
        try {
            this.islandConfigFile = new File(this.directoryIslands, String.valueOf(location) + ".yml");
            this.getIslandConfig(location).save(this.islandConfigFile);
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.islandConfigFile, ex);
        }
    }
    
    public void deleteIslandConfig(final String location) {
        (this.islandConfigFile = new File(this.directoryIslands, String.valueOf(location) + ".yml")).delete();
    }
    
    public void saveDefaultIslandsConfig(final String location) {
        try {
            if (this.islandConfigFile == null) {
                this.islandConfigFile = new File(this.directoryIslands, String.valueOf(location) + ".yml");
                this.getIslandConfig(location).save(this.islandConfigFile);
            }
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + this.islandConfigFile, ex);
        }
    }
    
    public void reloadLastIslandConfig() {
        if (this.lastIslandConfigFile == null) {
            this.lastIslandConfigFile = new File(this.getDataFolder(), "lastIslandConfig.yml");
        }
        this.lastIslandConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.lastIslandConfigFile);
        final InputStream defConfigStream = this.getResource("lastIslandConfig.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.lastIslandConfig.setDefaults((Configuration)defConfig);
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
        }
        catch (IOException ex) {
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
        this.orphans = (FileConfiguration)YamlConfiguration.loadConfiguration(this.orphanFile);
        final InputStream defConfigStream = this.getResource("orphans.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.orphans.setDefaults((Configuration)defConfig);
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
        }
        catch (IOException ex) {
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
        }
        else if (bName.equalsIgnoreCase("hell")) {
            bType = Biome.HELL;
        }
        else if (bName.equalsIgnoreCase("sky")) {
            bType = Biome.SKY;
        }
        else if (bName.equalsIgnoreCase("mushroom")) {
            bType = Biome.MUSHROOM_ISLAND;
        }
        else if (bName.equalsIgnoreCase("ocean")) {
            bType = Biome.OCEAN;
        }
        else if (bName.equalsIgnoreCase("swampland")) {
            bType = Biome.SWAMPLAND;
        }
        else if (bName.equalsIgnoreCase("taiga")) {
            bType = Biome.TAIGA;
        }
        else if (bName.equalsIgnoreCase("desert")) {
            bType = Biome.DESERT;
        }
        else if (bName.equalsIgnoreCase("forest")) {
            bType = Biome.FOREST;
        }
        else {
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
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + player.getName() + ".canChangeBiome")) {
            this.setBiome(getInstance().getActivePlayers().get(player.getName()).getIslandLocation(), bName);
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
            biomeList = String.valueOf(biomeList) + "FOREST, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "JUNGLE, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "DESERT, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "TAIGA, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "SWAMPLAND, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "MUSHROOM, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "HELL, ";
        }
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", getSkyBlockWorld())) {
            biomeList = String.valueOf(biomeList) + "SKY, ";
        }
        player.sendMessage(ChatColor.YELLOW + "You have access to the following Biomes:");
        player.sendMessage(ChatColor.GREEN + biomeList.substring(0, biomeList.length() - 2));
        player.sendMessage(ChatColor.YELLOW + "Use /island biome <biomename> to change your biome. You must wait " + Settings.general_biomeChange / 60 + " minutes between each biome change.");
    }
    
    public boolean createIsland(final CommandSender sender, final PlayerInfo pi) {
        System.out.println("Creating player island...");
        final Player player = (Player)sender;
        final Location last = getInstance().getLastIsland();
        last.setY((double)Settings.island_height);
        try {
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
            }
            else {
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
            boolean hasIslandNow = false;
            if (getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                String cSchem = "";
                for (int i = 0; i < getInstance().getSchemFile().length; ++i) {
                    if (!hasIslandNow) {
                        if (getInstance().getSchemFile()[i].getName().lastIndexOf(46) > 0) {
                            cSchem = getInstance().getSchemFile()[i].getName().substring(0, getInstance().getSchemFile()[i].getName().lastIndexOf(46));
                        }
                        else {
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
                        }
                        else {
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
                }
                else {
                    this.oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, getSkyBlockWorld());
                }
            }
            next.setY((double)Settings.island_height);
            System.out.println(next.getBlockY());
            System.out.println("Preparing to set new player information...");
            this.setNewPlayerIsland(player, next);
            System.out.println("Finished setting new player information.");
            player.getInventory().clear();
            player.getEquipment().clear();
            System.out.println("Preparing to set initial player biome...");
            getInstance().changePlayerBiome(player, "OCEAN");
            System.out.println("Finished setting initial player biome.");
            for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
                for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
                    getSkyBlockWorld().refreshChunk((next.getBlockX() + x) / 16, (next.getBlockZ() + z) / 16);
                }
            }
            for (final Entity tempent : player.getNearbyEntities(50.0, 250.0, 50.0)) {
                if (!(tempent instanceof Player)) {
                    tempent.remove();
                }
            }
            if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                WorldGuardHandler.protectIsland(player, sender.getName(), pi);
            }
        }
        catch (Exception ex) {
            player.sendMessage("Could not create your Island. Pleace contact a server moderator.");
            ex.printStackTrace();
            return false;
        }
        System.out.println("Finished creating player island.");
        return true;
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
        final Chest chest = (Chest)blockToChange3.getState();
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
                        inventory.addItem(new ItemStack[] { tempChest[j] });
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
        final int x = (int)lastIsland.getX();
        final int z = (int)lastIsland.getZ();
        if (x < z) {
            if (-1 * x < z) {
                lastIsland.setX(lastIsland.getX() + Settings.island_distance);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() + Settings.island_distance);
            return lastIsland;
        }
        else if (x > z) {
            if (-1 * x >= z) {
                lastIsland.setX(lastIsland.getX() - Settings.island_distance);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() - Settings.island_distance);
            return lastIsland;
        }
        else {
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
        final Chest chest = (Chest)blockToChange.getState();
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
                        inventory.addItem(new ItemStack[] { tempChest[j] });
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
                        final Chest chest = (Chest)blockToChange.getState();
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
                                        inventory.addItem(new ItemStack[] { tempChest[j] });
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
                            return new Location(getSkyBlockWorld(), (double)(loc.getBlockX() + x), (double)(loc.getBlockY() + (y + 1)), (double)(loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + (z - 1)).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + (y - 1), loc.getBlockZ() + (z - 1)).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double)(loc.getBlockX() + x), (double)(loc.getBlockY() + (y + 1)), (double)(loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double)(loc.getBlockX() + x), (double)(loc.getBlockY() + (y + 1)), (double)(loc.getBlockZ() + (z + 1)));
                        }
                        if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && getSkyBlockWorld().getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0) {
                            return new Location(getSkyBlockWorld(), (double)(loc.getBlockX() + x), (double)(loc.getBlockY() + (y + 1)), (double)(loc.getBlockZ() + (z + 1)));
                        }
                        loc.setY(loc.getY() + 1.0);
                        return loc;
                    }
                    else {
                        ++z;
                    }
                }
            }
        }
        return loc;
    }
    
    private void setNewPlayerIsland(final Player player, final Location loc) {
        getInstance().getActivePlayers().get(player.getName()).startNewIsland(loc);
        player.teleport(this.getChestSpawnLoc(loc, player));
        if (this.getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()) == null) {
            this.createIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty(), player.getName());
        }
        this.clearIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty(), player.getName());
        getInstance().updatePartyNumber(player);
        getInstance().homeSet(player);
        getInstance().getActivePlayers().get(player.getName()).savePlayerConfig(player.getName());
    }
    
    public void setWarpLocation(final String location, final Location loc) {
        this.getIslandConfig(location).set("general.warpLocationX", (Object)loc.getBlockX());
        this.getIslandConfig(location).set("general.warpLocationY", (Object)loc.getBlockY());
        this.getIslandConfig(location).set("general.warpLocationZ", (Object)loc.getBlockZ());
        this.getIslandConfig(location).set("general.warpActive", (Object)true);
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
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize") < 8 && VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.maxSize", (Object)8);
            getInstance().saveIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty());
            return;
        }
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize") < 7 && VaultHandler.checkPerk(player.getName(), "usb.extra.party3", player.getWorld())) {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.maxSize", (Object)7);
            getInstance().saveIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty());
            return;
        }
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize") < 6 && VaultHandler.checkPerk(player.getName(), "usb.extra.party2", player.getWorld())) {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.maxSize", (Object)6);
            getInstance().saveIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty());
            return;
        }
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize") < 5 && VaultHandler.checkPerk(player.getName(), "usb.extra.party1", player.getWorld())) {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.maxSize", (Object)5);
            getInstance().saveIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty());
        }
    }
    
    public void changePlayerPermission(final Player player, final String playername, final String perm) {
        if (!getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).contains("party.members." + playername + "." + perm)) {
            return;
        }
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + playername + "." + perm)) {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.members." + playername + "." + perm, (Object)false);
        }
        else {
            getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).set("party.members." + playername + "." + perm, (Object)true);
        }
        getInstance().saveIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty());
    }
    
    public boolean checkForOnlineMembers(final Player p) {
        for (final String tString : getInstance().getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).getConfigurationSection("party.members").getKeys(false)) {
            if (Bukkit.getPlayer(tString) != null && !Bukkit.getPlayer(tString).getName().equalsIgnoreCase(p.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkCurrentBiome(final Player p, final String biome) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).getString("general.biome").equalsIgnoreCase(biome);
    }
    
    public void setConfigBiome(final Player p, final String biome) {
        getInstance().getIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty()).set("general.biome", (Object)biome);
        getInstance().saveIslandConfig(getInstance().getActivePlayers().get(p.getName()).locationForParty());
    }
    
    public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        this.GUIpartyPlayer = Bukkit.createInventory((InventoryHolder)null, 9, String.valueOf(pname) + " <Permissions>");
        final ItemStack pHead = new ItemStack(397, 1, (short)3);
        final SkullMeta meta3 = (SkullMeta)pHead.getItemMeta();
        ItemMeta meta2 = this.sign.getItemMeta();
        meta2.setDisplayName("�hPlayer Permissions");
        this.lores.add("�eClick here to return to");
        this.lores.add("�eyour island group's info.");
        meta2.setLore((List)this.lores);
        this.sign.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.sign });
        this.lores.clear();
        meta3.setDisplayName(String.valueOf(pname) + "'s Permissions");
        this.lores.add("�eHover over an icon to view");
        this.lores.add("�ea permission. Change the");
        this.lores.add("�epermission by clicking it.");
        meta3.setLore((List)this.lores);
        pHead.setItemMeta((ItemMeta)meta3);
        this.GUIpartyPlayer.addItem(new ItemStack[] { pHead });
        this.lores.clear();
        meta2 = this.biome.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canChangeBiome")) {
            meta2.setDisplayName("�aChange Biome");
            this.lores.add("�fThis player �acan�f change the");
            this.lores.add("�fisland's biome. Click here");
            this.lores.add("�fto remove this permission.");
        }
        else {
            meta2.setDisplayName("�cChange Biome");
            this.lores.add("�fThis player �ccannot�f change the");
            this.lores.add("�fisland's biome. Click here");
            this.lores.add("�fto grant this permission.");
        }
        meta2.setLore((List)this.lores);
        this.biome.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.biome });
        this.lores.clear();
        meta2 = this.lock.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canToggleLock")) {
            meta2.setDisplayName("�aToggle Island Lock");
            this.lores.add("�fThis player �acan�f toggle the");
            this.lores.add("�fisland's lock, which prevents");
            this.lores.add("�fnon-group members from entering.");
            this.lores.add("�fClick here to remove this permission.");
        }
        else {
            meta2.setDisplayName("�cToggle Island Lock");
            this.lores.add("�fThis player �ccannot�f toggle the");
            this.lores.add("�fisland's lock, which prevents");
            this.lores.add("�fnon-group members from entering.");
            this.lores.add("�fClick here to add this permission");
        }
        meta2.setLore((List)this.lores);
        this.lock.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.lock });
        this.lores.clear();
        meta2 = this.warpset.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canChangeWarp")) {
            meta2.setDisplayName("�aSet Island Warp");
            this.lores.add("�fThis player �acan�f set the");
            this.lores.add("�fisland's warp, which allows");
            this.lores.add("�fnon-group members to teleport");
            this.lores.add("�fto the island. Click here to");
            this.lores.add("�fremove this permission.");
        }
        else {
            meta2.setDisplayName("�cSet Island Warp");
            this.lores.add("�fThis player �ccannot�f set the");
            this.lores.add("�fisland's warp, which allows");
            this.lores.add("�fnon-group members to teleport");
            this.lores.add("�fto the island. Click here to");
            this.lores.add("�fadd this permission.");
        }
        meta2.setLore((List)this.lores);
        this.warpset.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.warpset });
        this.lores.clear();
        meta2 = this.warptoggle.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canToggleWarp")) {
            meta2.setDisplayName("�aToggle Island Warp");
            this.lores.add("�fThis player �acan�f toggle the");
            this.lores.add("�fisland's warp, allowing them");
            this.lores.add("�fto turn it on or off at anytime.");
            this.lores.add("�fbut not set the location. Click");
            this.lores.add("�fhere to remove this permission.");
        }
        else {
            meta2.setDisplayName("�cToggle Island Warp");
            this.lores.add("�fThis player �ccannot�f toggle the");
            this.lores.add("�fisland's warp, allowing them");
            this.lores.add("�fto turn it on or off at anytime,");
            this.lores.add("�fbut not set the location. Click");
            this.lores.add("�fhere to add this permission.");
        }
        meta2.setLore((List)this.lores);
        this.warptoggle.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.warptoggle });
        this.lores.clear();
        meta2 = this.invite.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canInviteOthers")) {
            meta2.setDisplayName("�aInvite Players");
            this.lores.add("�fThis player �acan�f invite");
            this.lores.add("�fother players to the island if");
            this.lores.add("�fthere is enough room for more");
            this.lores.add("�fmembers. Click here to remove");
            this.lores.add("�fthis permission.");
        }
        else {
            meta2.setDisplayName("�cInvite Players");
            this.lores.add("�fThis player �ccannot�f invite");
            this.lores.add("�fother players to the island.");
            this.lores.add("�fClick here to add this permission.");
        }
        meta2.setLore((List)this.lores);
        this.invite.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.invite });
        this.lores.clear();
        meta2 = this.kick.getItemMeta();
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + pname + ".canKickOthers")) {
            meta2.setDisplayName("�aKick Players");
            this.lores.add("�fThis player �acan�f kick");
            this.lores.add("�fother players from the island,");
            this.lores.add("�fbut they are unable to kick");
            this.lores.add("�fthe island leader. Click here");
            this.lores.add("�fto remove this permission.");
        }
        else {
            meta2.setDisplayName("�cKick Players");
            this.lores.add("�fThis player �ccannot�f kick");
            this.lores.add("�fother players from the island.");
            this.lores.add("�fClick here to add this permission.");
        }
        meta2.setLore((List)this.lores);
        this.kick.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[] { this.kick });
        this.lores.clear();
        return this.GUIpartyPlayer;
    }
    
    public Inventory displayPartyGUI(final Player player) {
        this.GUIparty = Bukkit.createInventory((InventoryHolder)null, 18, "�9Island Group Members");
        final Set<String> memberList = (Set<String>)getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getConfigurationSection("party.members").getKeys(false);
        this.tempIt = memberList.iterator();
        final SkullMeta meta3 = (SkullMeta)this.pHead.getItemMeta();
        final ItemMeta meta2 = this.sign.getItemMeta();
        meta2.setDisplayName("�aGroup Info");
        this.lores.add("Group Members: �2" + getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.currentSize") + "�7/�e" + getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize"));
        if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.currentSize") < getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize")) {
            this.lores.add("�aMore players can be invited to this island.");
        }
        else {
            this.lores.add("�cThis island is full.");
        }
        this.lores.add("�eHover over a player's icon to");
        this.lores.add("�eview their permissions. The");
        this.lores.add("�eleader can change permissions");
        this.lores.add("�eby clicking a player's icon.");
        meta2.setLore((List)this.lores);
        this.sign.setItemMeta(meta2);
        this.GUIparty.addItem(new ItemStack[] { this.sign });
        this.lores.clear();
        while (this.tempIt.hasNext()) {
            final String temp = this.tempIt.next();
            if (temp.equalsIgnoreCase(getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getString("party.leader"))) {
                meta3.setDisplayName("�f" + temp);
                this.lores.add("�a�lLeader");
                this.lores.add("�aCan �fchange the island's biome.");
                this.lores.add("�aCan �flock/unlock the island.");
                this.lores.add("�aCan �fset the island's warp.");
                this.lores.add("�aCan �ftoggle the island's warp.");
                this.lores.add("�aCan �finvite others to the island.");
                this.lores.add("�aCan �fkick others from the island.");
                meta3.setLore((List)this.lores);
                this.lores.clear();
            }
            else {
                meta3.setDisplayName("�f" + temp);
                this.lores.add("�e�lMember");
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canChangeBiome")) {
                    this.lores.add("�aCan �fchange the island's biome.");
                }
                else {
                    this.lores.add("�cCannot �fchange the island's biome.");
                }
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canToggleLock")) {
                    this.lores.add("�aCan �flock/unlock the island.");
                }
                else {
                    this.lores.add("�cCannot �flock/unlock the island.");
                }
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canChangeWarp")) {
                    this.lores.add("�aCan �fset the island's warp.");
                }
                else {
                    this.lores.add("�cCannot �fset the island's warp.");
                }
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canToggleWarp")) {
                    this.lores.add("�aCan �ftoggle the island's warp.");
                }
                else {
                    this.lores.add("�cCannot �ftoggle the island's warp.");
                }
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canInviteOthers")) {
                    this.lores.add("�aCan �finvite others to the island.");
                }
                else {
                    this.lores.add("�cCannot �finvite others to the island.");
                }
                if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + temp + ".canKickOthers")) {
                    this.lores.add("�aCan �fkick others from the island.");
                }
                else {
                    this.lores.add("�cCannot �fkick others from the island.");
                }
                if (player.getName().equalsIgnoreCase(getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getString("party.leader"))) {
                    this.lores.add("�e<Click to change this player's permissions>");
                }
                meta3.setLore((List)this.lores);
                this.lores.clear();
            }
            meta3.setOwner(temp);
            this.pHead.setItemMeta((ItemMeta)meta3);
            this.GUIparty.addItem(new ItemStack[] { this.pHead });
        }
        return this.GUIparty;
    }
    
    public Inventory displayLogGUI(final Player player) {
        this.GUIlog = Bukkit.createInventory((InventoryHolder)null, 9, "�9Island Log");
        ItemMeta meta4 = this.sign.getItemMeta();
        meta4.setDisplayName("�lIsland Log");
        this.lores.add("�eClick here to return to");
        this.lores.add("�ethe main island screen.");
        meta4.setLore((List)this.lores);
        this.sign.setItemMeta(meta4);
        this.GUIlog.addItem(new ItemStack[] { this.sign });
        this.lores.clear();
        this.currentLogItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = this.currentLogItem.getItemMeta();
        meta4.setDisplayName("�e�lIsland Log");
        for (int i = 1; i <= 10; ++i) {
            if (getInstance().getIslandConfig(this.getActivePlayers().get(player.getName()).locationForParty()).contains("log." + i)) {
                this.lores.add(getInstance().getIslandConfig(this.getActivePlayers().get(player.getName()).locationForParty()).getString("log." + i));
            }
        }
        meta4.setLore((List)this.lores);
        this.currentLogItem.setItemMeta(meta4);
        this.GUIlog.setItem(8, this.currentLogItem);
        this.lores.clear();
        return this.GUIlog;
    }
    
    public Inventory displayBiomeGUI(final Player player) {
        this.GUIbiome = Bukkit.createInventory((InventoryHolder)null, 18, "�9Island Biome");
        ItemMeta meta4 = this.sign.getItemMeta();
        meta4.setDisplayName("�hIsland Biome");
        this.lores.add("�eClick here to return to");
        this.lores.add("�ethe main island screen.");
        meta4.setLore((List)this.lores);
        this.sign.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.sign });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.WATER, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Ocean");
            this.lores.add("�fThe ocean biome is the basic");
            this.lores.add("�fstarting biome for all islands.");
            this.lores.add("�fpassive mobs like animals will");
            this.lores.add("�fnot spawn. Hostile mobs will");
            this.lores.add("�fspawn normally.");
            if (this.checkCurrentBiome(player, "OCEAN")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Ocean");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The ocean biome is the basic");
            this.lores.add("�7starting biome for all islands.");
            this.lores.add("�7passive mobs like animals will");
            this.lores.add("�7not spawn. Hostile mobs will");
            this.lores.add("�7spawn normally.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAPLING, 1, (short)1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.forst", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Forest");
            this.lores.add("�fThe forest biome will allow");
            this.lores.add("�fyour island to spawn passive.");
            this.lores.add("�fmobs like animals (including");
            this.lores.add("�fwolves). Hostile mobs will");
            this.lores.add("�fspawn normally.");
            if (this.checkCurrentBiome(player, "FOREST")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Forest");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The forest biome will allow");
            this.lores.add("�7your island to spawn passive.");
            this.lores.add("�7mobs like animals (including");
            this.lores.add("�7wolves). Hostile mobs will");
            this.lores.add("�7spawn normally.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAND, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Desert");
            this.lores.add("�fThe desert biome makes it so");
            this.lores.add("�fthat there is no rain or snow");
            this.lores.add("�fon your island. Passive mobs");
            this.lores.add("�fwon't spawn. Hostile mobs will");
            this.lores.add("�fspawn normally.");
            if (this.checkCurrentBiome(player, "DESERT")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Desert");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The desert biome makes it so");
            this.lores.add("�7that there is no rain or snow");
            this.lores.add("�7on your island. Passive mobs");
            this.lores.add("�7won't spawn. Hostile mobs will");
            this.lores.add("�7spawn normally.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAPLING, 1, (short)3);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Jungle");
            this.lores.add("�fThe jungle biome is bright");
            this.lores.add("�fand colorful. Passive mobs");
            this.lores.add("�f(including ocelots) will");
            this.lores.add("�fspawn. Hostile mobs will");
            this.lores.add("�fspawn normally.");
            if (this.checkCurrentBiome(player, "JUNGLE")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Jungle");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The jungle biome is bright");
            this.lores.add("�7and colorful. Passive mobs");
            this.lores.add("�7(including ocelots) will");
            this.lores.add("�7spawn. Hostile mobs will");
            this.lores.add("�7spawn normally.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.WATER_LILY, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Swampland");
            this.lores.add("�fThe swamp biome is dark");
            this.lores.add("�fand dull. Passive mobs");
            this.lores.add("�fwill spawn normally and");
            this.lores.add("�fslimes have a small chance");
            this.lores.add("�fto spawn at night depending");
            this.lores.add("�fon the moon phase.");
            if (this.checkCurrentBiome(player, "SWAMPLAND")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Swampland");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The swamp biome is dark");
            this.lores.add("�7and dull. Passive mobs");
            this.lores.add("�7will spawn normally and");
            this.lores.add("�7slimes have a small chance");
            this.lores.add("�7to spawn at night depending");
            this.lores.add("�7on the moon phase.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SNOW, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Taiga");
            this.lores.add("�fThe taiga biome has snow");
            this.lores.add("�finstead of rain. Passive");
            this.lores.add("�fmobs will spawn normally");
            this.lores.add("�f(including wolves) and");
            this.lores.add("�fhostile mobs will spawn.");
            if (this.checkCurrentBiome(player, "TAIGA")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Taiga");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The taiga biome has snow");
            this.lores.add("�7instead of rain. Passive");
            this.lores.add("�7mobs will spawn normally");
            this.lores.add("�7(including wolves) and");
            this.lores.add("�7hostile mobs will spawn.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.RED_MUSHROOM, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Mushroom");
            this.lores.add("�fThe mushroom biome is");
            this.lores.add("�fbright and colorful.");
            this.lores.add("�fMooshrooms are the only");
            this.lores.add("�fmobs that will spawn.");
            this.lores.add("�fNo other passive or");
            this.lores.add("�fhostile mobs will spawn.");
            if (this.checkCurrentBiome(player, "MUSHROOM")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Mushroom");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The mushroom biome is");
            this.lores.add("�7bright and colorful.");
            this.lores.add("�7Mooshrooms are the only");
            this.lores.add("�7mobs that will spawn.");
            this.lores.add("�7No other passive or");
            this.lores.add("�7hostile mobs will spawn.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.FIRE, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Hell(Nether)");
            this.lores.add("�fThe hell biome looks");
            this.lores.add("�fdark and dead. Some");
            this.lores.add("�fmobs from the nether will");
            this.lores.add("�fspawn in this biome");
            this.lores.add("�f(excluding ghasts and");
            this.lores.add("�fblazes).");
            if (this.checkCurrentBiome(player, "HELL")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Hell(Nether)");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The hell biome looks");
            this.lores.add("�7dark and dead. Some");
            this.lores.add("�7mobs from the nether will");
            this.lores.add("�7spawn in this biome");
            this.lores.add("�7(excluding ghasts and");
            this.lores.add("�7blazes).");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", player.getWorld())) {
            meta4.setDisplayName("�aBiome: Sky(End)");
            this.lores.add("�fThe sky biome gives your");
            this.lores.add("�fisland a special dark sky.");
            this.lores.add("�fOnly endermen will spawn");
            this.lores.add("�fin this biome.");
            if (this.checkCurrentBiome(player, "SKY")) {
                this.lores.add("�2�lThis is your current biome.");
            }
            else {
                this.lores.add("�e�lClick to change to this biome.");
            }
        }
        else {
            meta4.setDisplayName("�8Biome: Sky(End)");
            this.lores.add("�cYou cannot use this biome.");
            this.lores.add("�7The sky biome gives your");
            this.lores.add("�7island a special dark sky.");
            this.lores.add("�7Only endermen will spawn");
            this.lores.add("�7in this biome.");
        }
        meta4.setLore((List)this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
        this.lores.clear();
        return this.GUIbiome;
    }
    
    public Inventory displayChallengeGUI(final Player player) {
        this.GUIchallenge = Bukkit.createInventory((InventoryHolder)null, 36, "�9Challenge Menu");
        final PlayerInfo pi = getInstance().getActivePlayers().get(player.getName());
        this.populateChallengeRank(player, 0, Material.DIRT, 0, pi);
        this.populateChallengeRank(player, 1, Material.IRON_BLOCK, 9, pi);
        this.populateChallengeRank(player, 2, Material.GOLD_BLOCK, 18, pi);
        this.populateChallengeRank(player, 3, Material.DIAMOND_BLOCK, 27, pi);
        return this.GUIchallenge;
    }
    
    public Inventory displayIslandGUI(final Player player) {
        this.GUIisland = Bukkit.createInventory((InventoryHolder)null, 18, "�9Island Menu");
        if (this.hasIsland(player.getName())) {
            this.currentIslandItem = new ItemStack(Material.ENDER_PORTAL, 1);
            ItemMeta meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lReturn Home");
            this.lores.add("�fReturn to your island's home");
            this.lores.add("�fpoint. You can change your home");
            this.lores.add("�fpoint to any location on your");
            this.lores.add("�fisland using �b/island sethome");
            this.lores.add("�e�lClick here to return home.");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.DIAMOND_ORE, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lChallenges");
            this.lores.add("�fView a list of challenges that");
            this.lores.add("�fyou can complete on your island");
            this.lores.add("�fto earn skybucks, items, perks,");
            this.lores.add("�fand titles.");
            this.lores.add("�e�lClick here to view challenges.");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.EXP_BOTTLE, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lIsland Level");
            this.lores.add("�eCurrent Level: �a" + this.showIslandLevel(player));
            this.lores.add("�fGain island levels by expanding");
            this.lores.add("�fyour skyblock and completing");
            this.lores.add("�fcertain challenges. Rarer blocks");
            this.lores.add("�fwill add more to your level.");
            this.lores.add("�e�lClick here to refresh.");
            this.lores.add("�e�l(must be on island)");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            final SkullMeta meta2 = (SkullMeta)this.currentIslandItem.getItemMeta();
            meta2.setDisplayName("�a�lIsland Group");
            this.lores.add("�eMembers: �2" + this.showCurrentMembers(player) + "/" + this.showMaxMembers(player));
            this.lores.add("�fView the members of your island");
            this.lores.add("�fgroup and their permissions. If");
            this.lores.add("�fyou are the island leader, you");
            this.lores.add("�fcan change the member permissions.");
            this.lores.add("�e�lClick here to view or change.");
            meta2.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta((ItemMeta)meta2);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.SAPLING, 1, (short)3);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lChange Island Biome");
            this.lores.add("�eCurrent Biome: �b" + this.getCurrentBiome(player).toUpperCase());
            this.lores.add("�fThe island biome affects things");
            this.lores.add("�flike grass color and spawning");
            this.lores.add("�fof both animals and monsters.");
            if (this.checkIslandPermission(player, "canChangeBiome")) {
                this.lores.add("�e�lClick here to change biomes.");
            }
            else {
                this.lores.add("�c�lYou can't change the biome.");
            }
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.IRON_FENCE, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lIsland Lock");
            if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("general.locked")) {
                this.lores.add("�eLock Status: �aActive");
                this.lores.add("�fYour island is currently �clocked.");
                this.lores.add("�fPlayers outside of your group");
                this.lores.add("�fare unable to enter your island.");
                if (this.checkIslandPermission(player, "canToggleLock")) {
                    this.lores.add("�e�lClick here to unlock your island.");
                }
                else {
                    this.lores.add("�c�lYou can't change the lock.");
                }
            }
            else {
                this.lores.add("�eLock Status: �8Inactive");
                this.lores.add("�fYour island is currently �aunlocked.");
                this.lores.add("�fAll players are able to enter your");
                this.lores.add("�fisland, but only you and your group");
                this.lores.add("�fmembers may build there.");
                if (this.checkIslandPermission(player, "canToggleLock")) {
                    this.lores.add("�e�lClick here to lock your island.");
                }
                else {
                    this.lores.add("�c�lYou can't change the lock.");
                }
            }
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            if (getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("general.warpActive")) {
                this.currentIslandItem = new ItemStack(Material.PORTAL, 1);
                meta4 = this.currentIslandItem.getItemMeta();
                meta4.setDisplayName("�a�lIsland Warp");
                this.lores.add("�eWarp Status: �aActive");
                this.lores.add("�fOther players may warp to your");
                this.lores.add("�fisland at anytime to the point");
                this.lores.add("�fyou set using �d/island setwarp.");
                if (this.checkIslandPermission(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", getSkyBlockWorld())) {
                    this.lores.add("�e�lClick here to deactivate.");
                }
                else {
                    this.lores.add("�c�lYou can't change the warp.");
                }
            }
            else {
                this.currentIslandItem = new ItemStack(Material.ENDER_STONE, 1);
                meta4 = this.currentIslandItem.getItemMeta();
                meta4.setDisplayName("�a�lIsland Warp");
                this.lores.add("�eWarp Status: �8Inactive");
                this.lores.add("�fOther players can't warp to your");
                this.lores.add("�fisland. Set a warp point using");
                this.lores.add("�d/island setwarp �fbefore activating.");
                if (this.checkIslandPermission(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", getSkyBlockWorld())) {
                    this.lores.add("�e�lClick here to activate.");
                }
                else {
                    this.lores.add("�c�lYou can't change the warp.");
                }
            }
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.CHEST, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lBuy Perks");
            this.lores.add("�fVisit the perk shop to buy");
            this.lores.add("�fspecial abilities for your");
            this.lores.add("�fisland and character, as well");
            this.lores.add("�fas titles and more.");
            this.lores.add("�e�lClick here to open the shop!");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.ENDER_CHEST, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lBuy Donor Perks");
            this.lores.add("�fThis special perk shop is");
            this.lores.add("�fonly available to donors!");
            if (VaultHandler.checkPerk(player.getName(), "group.donor", player.getWorld())) {
                this.lores.add("�e�lClick here to open the shop!");
            }
            else {
                this.lores.add("�a�lClick here to become a donor!");
            }
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.setItem(16, this.currentIslandItem);
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lIsland Log");
            this.lores.add("�fView a log of events from");
            this.lores.add("�fyour island such as member,");
            this.lores.add("�fbiome, and warp changes.");
            this.lores.add("�e�lClick to view the log.");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.BED, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lChange Home Location");
            this.lores.add("�fWhen you teleport to your");
            this.lores.add("�fisland you will be taken to");
            this.lores.add("�fthis location.");
            this.lores.add("�e�lClick here to change.");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.HOPPER, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lChange Warp Location");
            this.lores.add("�fWhen your warp is activated,");
            this.lores.add("�fother players will be taken to");
            this.lores.add("�fthis point when they teleport");
            this.lores.add("�fto your island.");
            this.lores.add("�e�lClick here to change.");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.setItem(15, this.currentIslandItem);
            this.lores.clear();
        }
        else if (VaultHandler.checkPerk(player.getName(), "group.member", getSkyBlockWorld())) {
            this.currentIslandItem = new ItemStack(Material.GRASS, 1);
            ItemMeta meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lStart an Island");
            this.lores.add("�fStart your skyblock journey");
            this.lores.add("�fby starting your own island.");
            this.lores.add("�fComplete challenges to earn");
            this.lores.add("�fitems and skybucks to help");
            this.lores.add("�fexpand your skyblock. You can");
            this.lores.add("�finvite others to join in");
            this.lores.add("�fbuilding your island empire!");
            this.lores.add("�e�lClick here to start!");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            final SkullMeta meta2 = (SkullMeta)this.currentIslandItem.getItemMeta();
            meta2.setDisplayName("�a�lJoin an Island");
            this.lores.add("�fWant to join another player's");
            this.lores.add("�fisland instead of starting");
            this.lores.add("�fyour own? If another player");
            this.lores.add("�finvites you to their island");
            this.lores.add("�fyou can click here or use");
            this.lores.add("�e/island accept �fto join them.");
            this.lores.add("�e�lClick here to accept an invite!");
            this.lores.add("�e�l(You must be invited first)");
            meta2.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta((ItemMeta)meta2);
            this.GUIisland.setItem(4, this.currentIslandItem);
            this.lores.clear();
            this.currentIslandItem = new ItemStack(Material.SIGN, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lIsland Help");
            this.lores.add("�fNeed help with skyblock");
            this.lores.add("�fconcepts or commands? View");
            this.lores.add("�fdetails about them here.");
            this.lores.add("�e�lClick here for help!");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.setItem(8, this.currentIslandItem);
            this.lores.clear();
        }
        else {
            this.currentIslandItem = new ItemStack(Material.BOOK, 1);
            final ItemMeta meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("�a�lWelcome to the Server!");
            this.lores.add("�fPlease read and accept the");
            this.lores.add("�fserver rules to become a");
            this.lores.add("�fmember and start your skyblock.");
            this.lores.add("�e�lClick here to read!");
            meta4.setLore((List)this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
            this.lores.clear();
        }
        return this.GUIisland;
    }
    
    public boolean isPartyLeader(final Player player) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getString("party.leader").equalsIgnoreCase(player.getName());
    }
    
    public boolean checkIslandPermission(final Player player, final String permission) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getBoolean("party.members." + player.getName() + "." + permission);
    }
    
    public String getCurrentBiome(final Player player) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getString("general.biome");
    }
    
    public int showIslandLevel(final Player player) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("general.level");
    }
    
    public int showCurrentMembers(final Player player) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.currentSize");
    }
    
    public int showMaxMembers(final Player player) {
        return getInstance().getIslandConfig(getInstance().getActivePlayers().get(player.getName()).locationForParty()).getInt("party.maxSize");
    }
    
    public void populateChallengeRank(final Player player, final int rankIndex, final Material mat, int location, final PlayerInfo pi) {
        int rankComplete = 0;
        this.currentChallengeItem = new ItemStack(mat, 1);
        ItemMeta meta4 = this.currentChallengeItem.getItemMeta();
        meta4.setDisplayName("�e�lRank: " + Settings.challenges_ranks[rankIndex]);
        this.lores.add("�fComplete most challenges in");
        this.lores.add("�fthis rank to unlock the next rank.");
        meta4.setLore((List)this.lores);
        this.currentChallengeItem.setItemMeta(meta4);
        this.GUIchallenge.setItem(location, this.currentChallengeItem);
        this.lores.clear();
        final String[] challengeList = this.getChallengesFromRank(player, Settings.challenges_ranks[rankIndex]).split(" - ");
        for (int i = 0; i < challengeList.length; ++i) {
            if (rankIndex > 0) {
                rankComplete = getInstance().checkRankCompletion(player, Settings.challenges_ranks[rankIndex - 1]);
                if (rankComplete > 0) {
                    this.currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
                    meta4 = this.currentChallengeItem.getItemMeta();
                    meta4.setDisplayName("�4�lLocked Challenge");
                    this.lores.add("�7Complete " + rankComplete + " more " + Settings.challenges_ranks[rankIndex - 1] + " challenges");
                    this.lores.add("�7to unlock this rank.");
                    meta4.setLore((List)this.lores);
                    this.currentChallengeItem.setItemMeta(meta4);
                    this.GUIchallenge.setItem(++location, this.currentChallengeItem);
                    this.lores.clear();
                    continue;
                }
            }
            if (challengeList[i].charAt(1) == 'e') {
                this.currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)4);
                meta4 = this.currentChallengeItem.getItemMeta();
                meta4.setDisplayName(challengeList[i].replace("�e", "�e�l"));
                challengeList[i] = challengeList[i].replace("�e", "");
                challengeList[i] = challengeList[i].replace("�8", "");
            }
            else if (challengeList[i].charAt(1) == 'a') {
                if (!getInstance().getConfig().contains("options.challenges.challengeList." + challengeList[i].replace("�a", "").replace("�2", "").replace("�e", "").replace("�8", "").toLowerCase() + ".displayItem")) {
                    this.currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
                }
                else {
                    this.currentChallengeItem = new ItemStack(Material.getMaterial(getInstance().getConfig().getInt("options.challenges.challengeList." + challengeList[i].replace("�a", "").replace("�2", "").replace("�e", "").replace("�8", "").toLowerCase() + ".displayItem")), 1);
                }
                meta4 = this.currentChallengeItem.getItemMeta();
                meta4.setDisplayName(challengeList[i].replace("�a", "�a�l"));
                challengeList[i] = challengeList[i].replace("�a", "");
                challengeList[i] = challengeList[i].replace("�8", "");
            }
            else if (challengeList[i].charAt(1) == '2') {
                this.currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)13);
                meta4 = this.currentChallengeItem.getItemMeta();
                meta4.setDisplayName(challengeList[i].replace("�2", "�2�l"));
                challengeList[i] = challengeList[i].replace("�2", "");
                challengeList[i] = challengeList[i].replace("�8", "");
            }
            else {
                this.currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)4);
                meta4 = this.currentChallengeItem.getItemMeta();
                meta4.setDisplayName(challengeList[i].replace("�e", "�e�l"));
                challengeList[i] = challengeList[i].replace("�e", "");
                challengeList[i] = challengeList[i].replace("�8", "");
            }
            this.lores.add("�7" + getInstance().getConfig().getString("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".description"));
            this.lores.add("�eThis challenge requires the following:");
            final String[] reqList = this.getConfig().getString("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".requiredItems").split(" ");
            int reqItem = 0;
            int reqAmount = 0;
            int reqMod = -1;
            String[] array;
            for (int length = (array = reqList).length, j = 0; j < length; ++j) {
                final String s = array[j];
                final String[] sPart = s.split(":");
                if (sPart.length == 2) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[1].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[1]);
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase());
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase()));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase());
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase()));
                        }
                    }
                }
                else if (sPart.length == 3) {
                    reqItem = Integer.parseInt(sPart[0]);
                    final String[] sScale = sPart[2].split(";");
                    if (sScale.length == 1) {
                        reqAmount = Integer.parseInt(sPart[2]);
                    }
                    else if (sScale.length == 2) {
                        if (sScale[1].charAt(0) == '+') {
                            reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase());
                        }
                        else if (sScale[1].charAt(0) == '*') {
                            reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase()));
                        }
                        else if (sScale[1].charAt(0) == '-') {
                            reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase());
                        }
                        else if (sScale[1].charAt(0) == '/') {
                            reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * getInstance().getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeList[i].toLowerCase()));
                        }
                    }
                    reqMod = Integer.parseInt(sPart[1]);
                }
                final ItemStack newItem = new ItemStack(reqItem, reqAmount, (short)reqMod);
                this.lores.add("�f" + newItem.getAmount() + " " + newItem.getType().toString());
            }
            if (pi.checkChallenge(challengeList[i].toLowerCase()) > 0 && getInstance().getConfig().getBoolean("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".repeatable")) {
                if (pi.onChallengeCooldown(challengeList[i].toLowerCase())) {
                    if (pi.getChallengeCooldownTime(challengeList[i].toLowerCase()) / 86400000L >= 1L) {
                        final int days = (int)pi.getChallengeCooldownTime(challengeList[i].toLowerCase()) / 86400000;
                        this.lores.add("�4Requirements will reset in " + days + " days.");
                    }
                    else {
                        final int hours = (int)pi.getChallengeCooldownTime(challengeList[i].toLowerCase()) / 3600000;
                        this.lores.add("�4Requirements will reset in " + hours + " hours.");
                    }
                }
                this.lores.add("�6Item Reward: �a" + getInstance().getConfig().getString("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".repeatRewardText"));
                this.lores.add("�6Currency Reward: �a" + getInstance().getConfig().getInt("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".repeatCurrencyReward"));
                this.lores.add("�6Exp Reward: �a" + getInstance().getConfig().getInt("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".repeatXpReward"));
                this.lores.add("�dTotal times completed: �f" + pi.getChallenge(challengeList[i].toLowerCase()).getTimesCompleted());
                this.lores.add("�e�lClick to complete this challenge.");
            }
            else {
                this.lores.add("�6Item Reward: �a" + getInstance().getConfig().getString("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".rewardText"));
                this.lores.add("�6Currency Reward: �a" + getInstance().getConfig().getInt("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".currencyReward"));
                this.lores.add("�6Exp Reward: �a" + getInstance().getConfig().getInt("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".xpReward"));
                if (getInstance().getConfig().getBoolean("options.challenges.challengeList." + challengeList[i].toLowerCase() + ".repeatable")) {
                    this.lores.add("�e�lClick to complete this challenge.");
                }
                else {
                    this.lores.add("�4�lYou can't repeat this challenge.");
                }
            }
            meta4.setLore((List)this.lores);
            this.currentChallengeItem.setItemMeta(meta4);
            this.GUIchallenge.setItem(++location, this.currentChallengeItem);
            this.lores.clear();
        }
    }
    
    public void sendMessageToIslandGroup(final String location, final String message) {
        final Iterator<String> temp = getInstance().getIslandConfig(location).getConfigurationSection("party.members").getKeys(false).iterator();
        this.date = new Date();
        final String dateTxt;
        final String myDateString = dateTxt = DateFormat.getDateInstance(3).format(this.date).toString();
        int currentLogPos = getInstance().getIslandConfig(location).getInt("log.logPos");
        while (temp.hasNext()) {
            final String player = temp.next();
            if (Bukkit.getPlayer(player) != null) {
                Bukkit.getPlayer(player).sendMessage("�d[skyblock] " + message);
            }
        }
        getInstance().getIslandConfig(location).set("log." + ++currentLogPos, (Object)("�d[" + dateTxt + "] " + message));
        if (currentLogPos < 10) {
            getInstance().getIslandConfig(location).set("log.logPos", (Object)currentLogPos);
        }
        else {
            getInstance().getIslandConfig(location).set("log.logPos", (Object)0);
        }
    }
    
    static /* synthetic */ void access$0(final uSkyBlock uSkyBlock, final Location lastIsland) {
        uSkyBlock.lastIsland = lastIsland;
    }
}
