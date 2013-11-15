package us.talabrek.ultimateskyblock;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class uSkyBlock extends JavaPlugin {
	private static uSkyBlock instance;
	public static World skyBlockWorld = null;

	public static uSkyBlock getInstance() {
		return instance;
	}

	public static World getSkyBlockWorld() {
		if (skyBlockWorld == null) {
			skyBlockWorld = WorldCreator.name(Settings.general_worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new SkyBlockChunkGenerator()).createWorld();
			WorldCreator.name("uSkyBlock").type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new SkyBlockChunkGenerator()).createWorld();
		}

		return skyBlockWorld;
	}
	
	public static boolean isSkyBlockWorld(World world)
	{
		return world.equals(getSkyBlockWorld());
	}
	
	public static Logger getLog()
	{
		return Logger.getLogger("uSkyBlock");
	}

	HashMap<String, PlayerInfo> activePlayers = new HashMap<String, PlayerInfo>();
	LinkedHashMap<String, List<String>> challenges = new LinkedHashMap<String, List<String>>();
	public FileConfiguration configPlugin;
	public File directoryPlayers;
	private File directorySchematics;
	public File filePlugin;
	HashMap<String, Long> infoCooldown = new HashMap<String, Long>();
	public Location islandTestLocation = null;
	private Location lastIsland;
	public Logger log;
	private Stack<Location> orphaned = new Stack<Location>();
	public PluginDescriptionFile pluginFile;
	public String pName;
	public boolean purgeActive = false;
	List<String> rankDisplay;
	public List<String> removeList = new ArrayList<String>();
	HashMap<Integer, Integer> requiredList = new HashMap<Integer, Integer>();
	HashMap<String, Long> restartCooldown = new HashMap<String, Long>();
	public File[] schemFile;
	private ArrayList<File> sfiles;
	private FileConfiguration skyblockData = null;

	private File skyblockDataFile = null;

	LinkedHashMap<String, Double> topTen;

	public void activatePurge() {
		purgeActive = true;
	}


	public void addOrphan(final Location island) {
		orphaned.push(island);
	}

	public void addToRemoveList(final String string) {
		removeList.add(string);
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
			} else {
				tempStack.pop();
			}
		}
		return finishStack;
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
			} else {
				tempStack.pop();
			}
		}
		return finishStack;
	}

	public boolean checkIfCanCompleteChallenge(final Player player, final String challenge) {
		final PlayerInfo pi = getPlayer(player.getName());

		if (!isRankAvailable(player, getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".rankLevel"))) {
			player.sendMessage(ChatColor.RED + "You have not unlocked this challenge yet!");
			return false;
		}
		if (!pi.challengeExists(challenge)) {
			player.sendMessage(ChatColor.RED + "Unknown challenge name (check spelling)!");
			return false;
		}
		if (pi.checkChallenge(challenge) && !getChallengeConfig().getBoolean("options.challenges.challengeList." + challenge + ".repeatable")) {
			player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
			return false;
		}
		if (pi.checkChallenge(challenge) && (getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland") || getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland"))) {
			player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
			return false;
		}
		if (getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onPlayer")) {
			if (!hasRequired(player, challenge, "onPlayer")) {
				player.sendMessage(ChatColor.RED + getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".description").toString()));
				player.sendMessage(ChatColor.RED + "You don't have enough of the required item(s)!");
				return false;
			}
			return true;
		}
		if (getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")) {
			if (!playerIsOnIsland(player)) {
				player.sendMessage(ChatColor.RED + "You must be on your island to do that!");
			}
			if (!hasRequired(player, challenge, "onIsland")) {
				player.sendMessage(ChatColor.RED + getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".description").toString()));

				player.sendMessage(ChatColor.RED + "You must be standing within 10 blocks of all required items.");
				return false;
			}
			return true;
		}
		if (getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("islandLevel")) {
			if (pi.getIslandLevel() >= getChallengeConfig().getInt("options.challenges.challengeList." + challenge + ".requiredItems")) {
				return true;
			}

			player.sendMessage(ChatColor.RED + "Your island must be level " + getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".requiredItems").toString()) + " to complete this challenge!");
			return false;
		}

		return false;
	}

	public Location checkOrphan() {
		return orphaned.peek();
	}

	public int checkRankCompletion(final OfflinePlayer player, final String rank) {
		if (!Settings.challenges_requirePreviousRank) {
			return 0;
		}
		rankDisplay = challenges.get(rank);
		int ranksCompleted = 0;
		final PlayerInfo pi = getPlayer(player.getName());
		final Iterator<String> itr = rankDisplay.iterator();
		while (itr.hasNext()) {
			final String tempString = itr.next();
			if (pi.checkChallenge(tempString)) {
				ranksCompleted++;
			}

		}

		return rankDisplay.size() - Settings.challenges_rankLeeway - ranksCompleted;
	}

	public boolean clearAbandoned() {
		int numOffline = 0;
		final OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
		System.out.println("uSkyblock " + "Attemping to add more orphans");
		for (final OfflinePlayer oplayer : oplayers) {
			long offlineTime = oplayer.getLastPlayed();
			offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
			if (offlineTime > 250L && getInstance().hasIsland(oplayer.getName()) && offlineTime < 50000L) {
				final PlayerInfo pi = getInstance().readPlayerFile(oplayer.getName());
				final Location l = pi.getIslandLocation();
				int blockcount = 0;
				final int px = l.getBlockX();
				final int py = l.getBlockY();
				final int pz = l.getBlockZ();
				for (int x = -30; x <= 30; x++) {
					for (int y = -30; y <= 30; y++) {
						for (int z = -30; z <= 30; z++) {
							final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
							if (b.getTypeId() != 0 && b.getTypeId() != 8 && b.getTypeId() != 10) {
								blockcount++;
							}
						}
					}
				}
				if (blockcount < 200) {
					numOffline++;
					WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(oplayer.getName() + "Island");
					orphaned.push(pi.getIslandLocation());

					pi.setHomeLocation(null);
					pi.setHasIsland(false);
					pi.setIslandLocation(null);
					writePlayerFile(oplayer.getName(), pi);
				}
			}
		}

		if (numOffline > 0) {
			System.out.println("uSkyblock " + "Added " + numOffline + " new orphans.");
			saveOrphans();
			updateOrphans();
			return true;
		}
		System.out.println("uSkyblock " + "No new orphans to add!");
		return false;
	}

	public void clearArmorContents(final Player player) {
		player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
	}

	public void clearOrphanedIsland() {
		while (hasOrphanedIsland()) {
			orphaned.pop();
		}
	}

	public void deactivatePurge() {
		purgeActive = false;
	}

	public void deleteFromRemoveList() {
		removeList.remove(0);
	}

	public void deletePlayerIsland(final String player) {
		if (!isActivePlayer(player)) {
			PlayerInfo pi = readPlayerFile(player);
			if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
				if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
					WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
				}
			}
			orphaned.push(pi.getIslandLocation());
			removeIsland(pi.getIslandLocation());
			pi = new PlayerInfo(player);
			pi.clearChallenges();
			saveOrphans();
			updateOrphans();
			writePlayerFile(player, pi);
		} else {
			if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
				if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
					WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
				}
			}
			orphaned.push(getPlayer(player).getIslandLocation());
			removeIsland(getPlayer(player).getIslandLocation());
			final PlayerInfo pi = new PlayerInfo(player);
			removeActivePlayer(player);
			addActivePlayer(player, pi);
			saveOrphans();
			updateOrphans();
		}
	}

	public void devDeletePlayerIsland(final String player) {
		if (!isActivePlayer(player)) {
			PlayerInfo pi = readPlayerFile(player);
			if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
				if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
					WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
				}
			}
			pi = new PlayerInfo(player);
			writePlayerFile(player, pi);
		} else {
			if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
				if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
					WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
				}
			}
			final PlayerInfo pi = new PlayerInfo(player);
			removeActivePlayer(player);
			addActivePlayer(player, pi);
		}
	}

	public boolean devSetPlayerIsland(final CommandSender sender, final Location l, final String player) {
		if (!isActivePlayer(player)) {
			final PlayerInfo pi = readPlayerFile(player);
			final int px = l.getBlockX();
			final int py = l.getBlockY();
			final int pz = l.getBlockZ();
			for (int x = -10; x <= 10; x++) {
				for (int y = -10; y <= 10; y++) {
					for (int z = -10; z <= 10; z++) {
						final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						if (b.getTypeId() == 7) {
							pi.setHomeLocation(new Location(l.getWorld(), px + x, py + y + 3, pz + z));
							pi.setHasIsland(true);
							pi.setIslandLocation(b.getLocation());
							writePlayerFile(player, pi);
							if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
								WorldGuardHandler.protectIsland(sender, player);
							}
							return true;
						}
					}
				}
			}
		} else {
			final int px = l.getBlockX();
			final int py = l.getBlockY();
			final int pz = l.getBlockZ();
			for (int x = -10; x <= 10; x++) {
				for (int y = -10; y <= 10; y++) {
					for (int z = -10; z <= 10; z++) {
						final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						if (b.getTypeId() == 7) {
							getPlayer(player).setHomeLocation(new Location(l.getWorld(), px + x, py + y + 3, pz + z));
							getPlayer(player).setHasIsland(true);
							getPlayer(player).setIslandLocation(b.getLocation());
							final PlayerInfo pi = getPlayer(player);
							removeActivePlayer(player);
							addActivePlayer(player, pi);
							if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
								WorldGuardHandler.protectIsland(sender, player);
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean displayTopTen(final Player player) {
		int i = 1;
		int playerrank = 0;
		player.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");
		if (topTen == null) {
			player.sendMessage(ChatColor.RED + "Top ten list not generated yet!");
			return false;
		}

		final PlayerInfo pi2 = getPlayer(player.getName());
		for (final String playerName : topTen.keySet()) {
			if (i <= 10) {
				if (hasParty(playerName)) {
					final PlayerInfo pix = readPlayerFile(playerName);
					final List<?> pMembers = pix.getMembers();
					if (pMembers.contains(playerName)) {
						pMembers.remove(playerName);
					}
					player.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + pMembers.toString() + " - Island level " + topTen.get(playerName).intValue());
				} else {
					player.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + " - Island level " + topTen.get(playerName).intValue());
				}
			}
			if (playerName.equalsIgnoreCase(player.getName())) {
				playerrank = i;
			}
			if (pi2.getHasParty()) {
				if (playerName.equalsIgnoreCase(pi2.getPartyLeader())) {
					playerrank = i;
				}
			}
			i++;
		}
		player.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank);
		return true;
	}

	public LinkedHashMap<String, Double> generateTopTen() {
		final HashMap<String, Double> tempMap = new LinkedHashMap<String, Double>();
		final File folder = directoryPlayers;
		final File[] listOfFiles = folder.listFiles();

		for (final File listOfFile : listOfFiles) {
			PlayerInfo pi;
			if ((pi = getInstance().readPlayerFile(listOfFile.getName())) != null) {
				if (pi.getIslandLevel() > 0 && (!pi.getHasParty() || pi.getPartyLeader().equalsIgnoreCase(pi.getPlayerName()))) {
					tempMap.put(listOfFile.getName(), Double.valueOf(pi.getIslandLevel()));
				}
			}
		}
		final LinkedHashMap<String, Double> sortedMap = sortHashMapByValuesD(tempMap);
		return sortedMap;
	}
	
	public void onEnterSkyBlock(Player player)
	{
		getOrCreatePlayer(player.getName());
	}
	
	public void onLeaveSkyBlock(Player player)
	{
		removeActivePlayer(player.getName());
	}

	private void addActivePlayer(final String player, final PlayerInfo pi) {
		activePlayers.put(player, pi);
	}
	
	private void removeActivePlayer(final String player) {
		if (activePlayers.containsKey(player)) {
			writePlayerFile(player, activePlayers.get(player));

			activePlayers.remove(player);
			System.out.println("uSkyblock " + "Removing player from memory: " + player);
		}
	}
	
	public boolean isActivePlayer(String player)
	{
		return activePlayers.containsKey(player);
	}
	
	public PlayerInfo getOrCreatePlayer(String player)
	{
		if(isActivePlayer(player))
			return activePlayers.get(player);
		
		PlayerInfo pi = readPlayerFile(player);
		
		if (pi == null) 
		{
			System.out.println("uSkyblock " + "Creating a new skyblock file for " + player);
			pi = new PlayerInfo(player);
			writePlayerFile(player, pi);
		}
		
		if (pi.getHasParty() && pi.getPartyIslandLocation() == null) {
			final PlayerInfo pi2 = readPlayerFile(pi.getPartyLeader());
			pi.setPartyIslandLocation(pi2.getIslandLocation());
			writePlayerFile(player, pi);
		}

		pi.buildChallengeList();
		addActivePlayer(player, pi);
		System.out.println("uSkyblock " + "Loaded player file for " + player);
		
		return pi;
	}
	
	public PlayerInfo getPlayer(String player)
	{
		if(isActivePlayer(player))
			return activePlayers.get(player);
		
		PlayerInfo pi = readPlayerFile(player);
		
		if(pi != null)
		{
			if (pi.getHasParty() && pi.getPartyIslandLocation() == null) 
			{
				final PlayerInfo pi2 = readPlayerFile(pi.getPartyLeader());
				pi.setPartyIslandLocation(pi2.getIslandLocation());
				writePlayerFile(player, pi);
			}
	
			pi.buildChallengeList();
			addActivePlayer(player, pi);
			System.out.println("uSkyblock " + "Loaded player file for " + player);
		}
		
		return pi;
	}
	
	public void getAllFiles(final String path) {
		final File dirpath = new File(path);
		if (!dirpath.exists()) {
			return;
		}

		for (final File f : dirpath.listFiles()) {
			try {
				if (!f.isDirectory()) {
					sfiles.add(f);
				} else {
					getAllFiles(f.getAbsolutePath());
				}
			} catch (final Exception ex) {
				log.warning(ex.getMessage());
			}
		}
	}

	public FileConfiguration getChallengeConfig() {
		// TODO create a challenge yml file and return it's fileconfiguration
		// instead of the config.
		// TODO move all challenge options from config to the challenge yml file
		// and optionally rename them (as well as in source)
		return getConfig();
	}

	public String getChallengesFromRank(final OfflinePlayer player, final String rank) {
		rankDisplay = challenges.get(rank);
		String fullString = "";
		final PlayerInfo pi = getPlayer(player.getName());
		final Iterator<String> itr = rankDisplay.iterator();
		while (itr.hasNext()) {
			final String tempString = itr.next();
			if (pi.checkChallenge(tempString)) {
				if (getChallengeConfig().getBoolean("options.challenges.challengeList." + tempString + ".repeatable")) {
					fullString = fullString + ChatColor.translateAlternateColorCodes('&', Settings.challenges_repeatableColor) + tempString + ChatColor.DARK_GRAY + " - ";
				} else {
					fullString = fullString + ChatColor.translateAlternateColorCodes('&', Settings.challenges_finishedColor) + tempString + ChatColor.DARK_GRAY + " - ";
				}
			} else {
				fullString = fullString + ChatColor.translateAlternateColorCodes('&', Settings.challenges_challengeColor) + tempString + ChatColor.DARK_GRAY + " - ";
			}
		}
		if (fullString.length() > 3) {
			fullString = fullString.substring(0, fullString.length() - 2);
		}
		return fullString;
	}

	public FileConfiguration getData() {
		if (skyblockData == null) {
			reloadData();
		}
		return skyblockData;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
		return new SkyBlockChunkGenerator();
	}

	public long getInfoCooldownTime(final Player player) {
		if (infoCooldown.containsKey(player.getName())) {
			if (infoCooldown.get(player.getName()).longValue() > Calendar.getInstance().getTimeInMillis()) {
				return infoCooldown.get(player.getName()).longValue() - Calendar.getInstance().getTimeInMillis();
			}

			return 0L;
		}

		return 0L;
	}

	public Location getLastIsland() {
		if (lastIsland.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			return lastIsland;
		}

		setLastIsland(new Location(getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D));
		return new Location(getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);
	}

	public Location getLocationString(final String s) {
		if (s == null || s.trim() == "") {
			return null;
		}
		final String[] parts = s.split(":");
		if (parts.length == 4) {
			final World w = getServer().getWorld(parts[0]);
			final int x = Integer.parseInt(parts[1]);
			final int y = Integer.parseInt(parts[2]);
			final int z = Integer.parseInt(parts[3]);
			return new Location(w, x, y, z);
		}
		return null;
	}

	public Location getOrphanedIsland() {
		if (hasOrphanedIsland()) {
			return orphaned.pop();
		}

		return null;
	}

	public Location getPlayerIsland(final String playername) {
		if (isActivePlayer(playername)) {
			return getPlayer(playername).getIslandLocation();
		}

		final PlayerInfo pi = getInstance().readPlayerFile(playername);
		if (pi == null) {
			return null;
		}
		return pi.getIslandLocation();
	}

	public List<String> getRemoveList() {
		return removeList;
	}

	public long getRestartCooldownTime(final Player player) {
		if (restartCooldown.containsKey(player.getName())) {
			if (restartCooldown.get(player.getName()).longValue() > Calendar.getInstance().getTimeInMillis()) {
				return restartCooldown.get(player.getName()).longValue() - Calendar.getInstance().getTimeInMillis();
			}

			return 0L;
		}

		return 0L;
	}

	public Location getSafeHomeLocation(final PlayerInfo p) {
		Location home = null;
		if (p.getHomeLocation() == null) {
			if (p.getIslandLocation() == null && p.getHasParty()) {
				home = p.getPartyIslandLocation();
			} else if (p.getIslandLocation() != null) {
				home = p.getIslandLocation();
			}
		} else {
			home = p.getHomeLocation();
		}

		if (isSafeLocation(home)) {
			return home;
		}

		for (int y = home.getBlockY() + 25; y > 0; y--) {
			final Location n = new Location(home.getWorld(), home.getBlockX(), y, home.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		for (int y = home.getBlockY(); y < 255; y++) {
			final Location n = new Location(home.getWorld(), home.getBlockX(), y, home.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		if (p.getHasParty() && !p.getPartyLeader().equalsIgnoreCase(p.getPlayer().getName())) {
			return p.getPartyIslandLocation();
		}
		final Location island = p.getIslandLocation();
		if (isSafeLocation(island)) {
			return island;
		}

		for (int y = island.getBlockY() + 25; y > 0; y--) {
			final Location n = new Location(island.getWorld(), island.getBlockX(), y, island.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		for (int y = island.getBlockY(); y < 255; y++) {
			final Location n = new Location(island.getWorld(), island.getBlockX(), y, island.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		if (p.getHasParty() && !p.getPartyLeader().equalsIgnoreCase(p.getPlayer().getName())) {
			return p.getPartyIslandLocation();
		}
		return p.getHomeLocation();
	}

	public File[] getSchemFile() {
		return schemFile;
	}

	public String getStringbyPath(final FileConfiguration fc, final File file, final String path, final Object stdValue, final boolean addMissing) {
		if (!fc.contains(path)) {
			if (addMissing) {
				setStringbyPath(fc, file, path, stdValue);
			}
			return stdValue.toString();
		}
		return fc.getString(path);
	}

	public String getStringLocation(final Location l) {
		if (l == null) {
			return "";
		}
		return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
	}

	public Location getYLocation(final Location l) {
		for (int y = 0; y < 254; y++) {
			final int px = l.getBlockX();
			final int py = y;
			final int pz = l.getBlockZ();
			final Block b1 = new Location(l.getWorld(), px, py, pz).getBlock();
			final Block b2 = new Location(l.getWorld(), px, py + 1, pz).getBlock();
			final Block b3 = new Location(l.getWorld(), px, py + 2, pz).getBlock();
			if (!b1.getType().equals(Material.AIR) && b2.getType().equals(Material.AIR) && b3.getType().equals(Material.AIR)) {
				return b2.getLocation();
			}
		}
		return l;
	}

	public boolean giveReward(final Player player, final String challenge) {
		final String[] permList = getChallengeConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".permissionReward").split(" ");
		int rewCurrency = 0;
		player.sendMessage(ChatColor.GREEN + "You have completed the " + challenge + " challenge!");
		String[] rewList;
		if (!getInstance().getPlayer(player.getName()).checkChallenge(challenge)) {
			rewList = getChallengeConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".itemReward").split(" ");
			if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
				rewCurrency = getChallengeConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".currencyReward");
			}
		} else {
			rewList = getChallengeConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatItemReward").split(" ");
			if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
				rewCurrency = getChallengeConfig().getInt("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatCurrencyReward");
			}
		}
		int rewItem = 0;
		int rewAmount = 0;
		int rewMod = -1;
		if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
			VaultHandler.econ.depositPlayer(player.getName(), rewCurrency);
			if (getInstance().getPlayer(player.getName()).checkChallenge(challenge)) {
				player.giveExp(getInstance().getChallengeConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
				player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatRewardText").toString())));
				player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatXpReward").toString()));
				player.sendMessage(ChatColor.YELLOW + "Repeat currency reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatCurrencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
			} else {
				if (Settings.challenges_broadcastCompletion) {
					Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', Settings.challenges_broadcastText + player.getName() + " has completed the " + challenge + " challenge!"));
				}
				player.giveExp(getInstance().getChallengeConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
				player.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".rewardText").toString())));
				player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".xpReward").toString()));
				player.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".currencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
			}

		} else if (getInstance().getPlayer(player.getName()).checkChallenge(challenge)) {
			player.giveExp(getInstance().getChallengeConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
			player.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatRewardText").toString())));
			player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatXpReward").toString()));
		} else {
			if (Settings.challenges_broadcastCompletion) {
				Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', Settings.challenges_broadcastText + player.getName() + " has completed the " + challenge + " challenge!"));
			}
			player.giveExp(getInstance().getChallengeConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
			player.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".rewardText").toString())));
			player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(challenge).append(".xpReward").toString()));
		}

		for (final String s : permList) {
			if (!s.equalsIgnoreCase("none")) {
				if (!VaultHandler.checkPerk(player.getName(), s, player.getWorld())) {
					VaultHandler.addPerk(player, s);
				}
			}
		}
		for (final String s : rewList) {
			final String[] sPart = s.split(":");
			if (sPart.length == 2) {
				rewItem = Integer.parseInt(sPart[0]);
				rewAmount = Integer.parseInt(sPart[1]);
				player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount) });
			} else if (sPart.length == 3) {
				rewItem = Integer.parseInt(sPart[0]);
				rewAmount = Integer.parseInt(sPart[2]);
				rewMod = Integer.parseInt(sPart[1]);
				player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount, (short) rewMod) });
			}
		}
		if (!getInstance().getPlayer(player.getName()).checkChallenge(challenge)) {
			getInstance().getPlayer(player.getName()).completeChallenge(challenge);
			getInstance().writePlayerFile(player.getName(), getInstance().getPlayer(player.getName()));
		}

		return true;
	}

	public boolean hasIsland(final String playername) {
		if (isActivePlayer(playername)) {
			return getPlayer(playername).getHasIsland();
		}

		final PlayerInfo pi = getInstance().readPlayerFile(playername);
		if (pi == null) {
			return false;
		}
		return pi.getHasIsland();
	}

	public boolean hasOrphanedIsland() {
		return !orphaned.empty();
	}

	public boolean hasParty(final String playername) {
		if (isActivePlayer(playername)) {
			return getPlayer(playername).getHasParty();
		}

		final PlayerInfo pi = getInstance().readPlayerFile(playername);
		if (pi == null) {
			return false;
		}
		return pi.getHasParty();
	}

	public boolean hasRequired(final Player player, final String challenge, final String type) {
		final String[] reqList = getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(" ");

		if (type.equalsIgnoreCase("onPlayer")) {
			int reqItem = 0;
			int reqAmount = 0;
			int reqMod = -1;
			for (final String s : reqList) {
				final String[] sPart = s.split(":");
				if (sPart.length == 2) {
					reqItem = Integer.parseInt(sPart[0]);
					reqAmount = Integer.parseInt(sPart[1]);
					if (!player.getInventory().contains(reqItem, reqAmount)) {
						return false;
					}
				} else if (sPart.length == 3) {
					reqItem = Integer.parseInt(sPart[0]);
					reqAmount = Integer.parseInt(sPart[2]);
					reqMod = Integer.parseInt(sPart[1]);
					if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount)) {
						return false;
					}
				}
			}
			if (getChallengeConfig().getBoolean("options.challenges.challengeList." + challenge + ".takeItems")) {
				takeRequired(player, challenge, type);
			}
			return true;
		}
		if (type.equalsIgnoreCase("onIsland")) {
			final int[][] neededItem = new int[reqList.length][2];
			for (int i = 0; i < reqList.length; i++) {
				final String[] sPart = reqList[i].split(":");
				neededItem[i][0] = Integer.parseInt(sPart[0]);
				neededItem[i][1] = Integer.parseInt(sPart[1]);
			}
			final Location l = player.getLocation();
			final int px = l.getBlockX();
			final int py = l.getBlockY();
			final int pz = l.getBlockZ();
			for (int x = -10; x <= 10; x++) {
				for (int y = -3; y <= 10; y++) {
					for (int z = -10; z <= 10; z++) {
						final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						for (int i = 0; i < neededItem.length; i++) {
							if (b.getTypeId() == neededItem[i][0]) {
								neededItem[i][1] -= 1;
							}
						}
					}
				}
			}
			for (final int[] element : neededItem) {
				if (element[1] > 0) {
					return false;
				}
			}
			return true;
		}

		return true;
	}

	public boolean homeSet(final Player player) {
		if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
			player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
			return true;
		}
		if (playerIsOnIsland(player)) {
			if (isActivePlayer(player.getName())) {
				getPlayer(player.getName()).setHomeLocation(player.getLocation());
			}

			player.sendMessage(ChatColor.GREEN + "Your skyblock home has been set to your current location.");
			return true;
		}
		player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
		return true;
	}

	public boolean homeSet(final String player, final Location loc) {
		if (isActivePlayer(player)) {
			getPlayer(player).setHomeLocation(loc);
		} else {
			final PlayerInfo pi = getInstance().readPlayerFile(player);
			pi.setHomeLocation(loc);
			getInstance().writePlayerFile(player, pi);
		}

		return true;
	}

	public boolean homeTeleport(final Player player) {
		Location homeSweetHome = null;
		
		if (isActivePlayer(player.getName())) {
			homeSweetHome = getInstance().getSafeHomeLocation(getPlayer(player.getName()));
		}

		if (homeSweetHome == null) {
			player.performCommand("spawn");
			player.sendMessage(ChatColor.RED + "You are not part of an island. Returning you the spawn area!");
			return true;
		}

		getInstance().removeCreatures(homeSweetHome);
		player.teleport(homeSweetHome);
		player.sendMessage(ChatColor.GREEN + "Teleporting you to your island. (/island help for more info)");
		return true;
	}

	public boolean islandAtLocation(final Location loc) {
		if (loc == null) {
			return true;
		}
		final int px = loc.getBlockX();
		final int py = loc.getBlockY();
		final int pz = loc.getBlockZ();
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					final Block b = new Location(loc.getWorld(), px + x, py + y, pz + z).getBlock();
					if (b.getTypeId() != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isPurgeActive() {
		return purgeActive;
	}

	public boolean isRankAvailable(final Player player, final String rank) {
		if (challenges.size() < 2) {
			return true;
		}

		for (int i = 0; i < Settings.challenges_ranks.length; i++) {
			if (Settings.challenges_ranks[i].equalsIgnoreCase(rank)) {
				if (i == 0) {
					return true;
				}

				if (checkRankCompletion(player, Settings.challenges_ranks[i - 1]) <= 0) {
					return true;
				}
			}

		}

		return false;
	}

	public boolean isSafeLocation(final Location l) {
		if (l == null) {
			return false;
		}

		final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
		final Block air1 = l.getBlock();
		final Block air2 = l.getBlock().getRelative(BlockFace.UP);
		if (ground.getType().equals(Material.AIR)) {
			return false;
		}
		if (ground.getType().equals(Material.LAVA)) {
			return false;
		}
		if (ground.getType().equals(Material.STATIONARY_LAVA)) {
			return false;
		}
		if (ground.getType().equals(Material.CACTUS)) {
			return false;
		}
		if ((air1.getType().equals(Material.AIR) || air1.getType().equals(Material.CROPS) || air1.getType().equals(Material.LONG_GRASS) || air1.getType().equals(Material.RED_ROSE) || air1.getType().equals(Material.YELLOW_FLOWER) || air1.getType().equals(Material.DEAD_BUSH) || air1.getType().equals(Material.SIGN_POST) || air1.getType().equals(Material.SIGN)) && air2.getType().equals(Material.AIR)) {
			return true;
		}
		return false;
	}

	public boolean largeIsland(final Location l) {
		int blockcount = 0;
		final int px = l.getBlockX();
		final int py = l.getBlockY();
		final int pz = l.getBlockZ();
		for (int x = -30; x <= 30; x++) {
			for (int y = -30; y <= 30; y++) {
				for (int z = -30; z <= 30; z++) {
					final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
					if (b.getTypeId() != 0 && b.getTypeId() != 8 && b.getTypeId() != 10) {
						blockcount++;
						if (blockcount > 200) {
							return true;
						}
					}
				}
			}
		}
		if (blockcount > 200) {
			return true;
		}
		return false;
	}

	public void loadPlayerFiles() 
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(!isSkyBlockWorld(player.getWorld()))
				continue;
			
			onEnterSkyBlock(player);
		}
	}

	public void loadPluginConfig() {
		try {
			getConfig();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		try {
			Settings.general_maxPartySize = getConfig().getInt("options.general.maxPartySize");
			if (Settings.general_maxPartySize < 0) {
				Settings.general_maxPartySize = 0;
			}
		} catch (final Exception e) {
			Settings.general_maxPartySize = 4;
		}
		try {
			Settings.island_distance = getConfig().getInt("options.island.distance");
			if (Settings.island_distance < 50) {
				Settings.island_distance = 50;
			}
		} catch (final Exception e) {
			Settings.island_distance = 110;
		}
		try {
			Settings.island_protectionRange = getConfig().getInt("options.island.protectionRange");
			if (Settings.island_protectionRange > Settings.island_distance) {
				Settings.island_protectionRange = Settings.island_distance;
			}
		} catch (final Exception e) {
			Settings.island_protectionRange = 100;
		}
		try {
			Settings.general_cooldownInfo = getConfig().getInt("options.general.cooldownInfo");
			if (Settings.general_cooldownInfo < 0) {
				Settings.general_cooldownInfo = 0;
			}
		} catch (final Exception e) {
			Settings.general_cooldownInfo = 60;
		}
		try {
			Settings.general_cooldownRestart = getConfig().getInt("options.general.cooldownRestart");
			if (Settings.general_cooldownRestart < 0) {
				Settings.general_cooldownRestart = 0;
			}
		} catch (final Exception e) {
			Settings.general_cooldownRestart = 60;
		}
		try {
			Settings.island_height = getConfig().getInt("options.island.height");
			if (Settings.island_height < 20) {
				Settings.island_height = 20;
			}
		} catch (final Exception e) {
			Settings.island_height = 120;
		}
		try {
			Settings.challenges_rankLeeway = getChallengeConfig().getInt("options.challenges.rankLeeway");
			if (Settings.challenges_rankLeeway < 0) {
				Settings.challenges_rankLeeway = 0;
			}
		} catch (final Exception e) {
			Settings.island_height = 120;
		}

		if (!getConfig().contains("options.extras.obsidianToLava")) {
			getConfig().set("options.extras.obsidianToLava", Boolean.valueOf(true));
			saveConfig();
		}

		final String[] chestItemString = getConfig().getString("options.island.chestItems").split(" ");
		final ItemStack[] tempChest = new ItemStack[chestItemString.length];
		String[] amountdata = new String[2];
		for (int i = 0; i < tempChest.length; i++) {
			amountdata = chestItemString[i].split(":");
			tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
		}
		Settings.island_chestItems = tempChest;
		Settings.island_allowPvP = getConfig().getString("options.island.allowPvP");
		Settings.island_schematicName = getConfig().getString("options.island.schematicName");
		if (!Settings.island_allowPvP.equalsIgnoreCase("allow")) {
			Settings.island_allowPvP = "deny";
		}
		final Set<?> permissionList = getConfig().getConfigurationSection("options.island.extraPermissions").getKeys(true);
		Settings.island_addExtraItems = getConfig().getBoolean("options.island.addExtraItems");
		Settings.extras_obsidianToLava = getConfig().getBoolean("options.extras.obsidianToLava");
		Settings.island_useIslandLevel = getConfig().getBoolean("options.island.useIslandLevel");
		Settings.island_extraPermissions = permissionList.toArray(new String[0]);
		Settings.island_protectWithWorldGuard = getConfig().getBoolean("options.island.protectWithWorldGuard");
		Settings.extras_sendToSpawn = getConfig().getBoolean("options.extras.sendToSpawn");
		Settings.island_useTopTen = getConfig().getBoolean("options.island.useTopTen");

		Settings.general_worldName = getConfig().getString("options.general.worldName");
		Settings.island_removeCreaturesByTeleport = getConfig().getBoolean("options.island.removeCreaturesByTeleport");
		Settings.island_allowIslandLock = getConfig().getBoolean("options.island.allowIslandLock");
		Settings.island_useOldIslands = getConfig().getBoolean("options.island.useOldIslands");

		final Set<String> challengeList = getChallengeConfig().getConfigurationSection("options.challenges.challengeList").getKeys(false);
		Settings.challenges_challengeList = challengeList;
		Settings.challenges_broadcastCompletion = getChallengeConfig().getBoolean("options.challenges.broadcastCompletion");
		Settings.challenges_broadcastText = getChallengeConfig().getString("options.challenges.broadcastText");
		Settings.challenges_challengeColor = getChallengeConfig().getString("options.challenges.challengeColor");
		Settings.challenges_enableEconomyPlugin = getChallengeConfig().getBoolean("options.challenges.enableEconomyPlugin");
		Settings.challenges_finishedColor = getChallengeConfig().getString("options.challenges.finishedColor");
		Settings.challenges_repeatableColor = getChallengeConfig().getString("options.challenges.repeatableColor");
		Settings.challenges_requirePreviousRank = getChallengeConfig().getBoolean("options.challenges.requirePreviousRank");
		Settings.challenges_allowChallenges = getChallengeConfig().getBoolean("options.challenges.allowChallenges");
		final String[] rankListString = getChallengeConfig().getString("options.challenges.ranks").split(" ");
		Settings.challenges_ranks = rankListString;
	}

	public boolean locationIsOnIsland(final Player player, final Location loc) {
		if (isActivePlayer(player.getName())) {
			if (getPlayer(player.getName()).getHasIsland()) {
				islandTestLocation = getPlayer(player.getName()).getIslandLocation();
			} else if (getPlayer(player.getName()).getHasParty()) {
				islandTestLocation = getPlayer(player.getName()).getPartyIslandLocation();
			}
			if (islandTestLocation == null) {
				return false;
			}
			if (loc.getX() > islandTestLocation.getX() - Settings.island_protectionRange / 2 && loc.getX() < islandTestLocation.getX() + Settings.island_protectionRange / 2 && loc.getZ() > islandTestLocation.getZ() - Settings.island_protectionRange / 2 && loc.getZ() < islandTestLocation.getZ() + Settings.island_protectionRange / 2) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		try {
			unloadPlayerFiles();

			if (lastIsland != null) {
				setLastIsland(lastIsland);
			}

			final File f2 = new File(getDataFolder(), "orphanedIslands.bin");

			if (orphaned != null) {
				if (!orphaned.isEmpty()) {
					SLAPI.save(changeStackToFile(orphaned), f2);
				}
			}
		} catch (final Exception e) {
			System.out.println("Something went wrong saving the island and/or party data!");
			e.printStackTrace();
		}
		log.info(pluginFile.getName() + " v" + pluginFile.getVersion() + " disabled.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		pluginFile = getDescription();
		log = getLogger();
		pName = ChatColor.WHITE + "[" + ChatColor.GREEN + pluginFile.getName() + ChatColor.WHITE + "] ";
		try {
			final Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (final IOException localIOException) {
		}
		VaultHandler.setupEconomy();
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		configPlugin = getConfig();
		filePlugin = new File(getDataFolder(), "config.yml");
		loadPluginConfig();
		registerEvents();
		directoryPlayers = new File(getDataFolder() + File.separator + "players");
		if (!directoryPlayers.exists())
			directoryPlayers.mkdir();
		
		loadPlayerFiles();

		directorySchematics = new File(getDataFolder() + File.separator + "schematics");
		if (!directorySchematics.exists()) {
			directorySchematics.mkdir();
		}
		schemFile = directorySchematics.listFiles();
		if (schemFile == null) {
			System.out.println("uSkyblock " + "[uSkyBlock] No schematic file loaded.");
		} else {
			System.out.println("uSkyblock " + "[uSkyBlock] " + schemFile.length + " schematics loaded.");
		}

		try {
			if (new File(getDataFolder(), "orphanedIslands.bin").exists()) {
				final Stack<SerializableLocation> load = (Stack<SerializableLocation>) SLAPI.load(new File(getDataFolder(), "orphanedIslands.bin"));
				if (load != null) {
					if (!load.isEmpty()) {
						orphaned = changestackfromfile(load);
					}
				}
			} else {
				System.out.println("uSkyblock " + "Creating a new orphan file");
				new File("orphanedIslands.bin");
			}
		} catch (final Exception e) {
			System.out.println("Could not load Island and/or Party data from disk.");
			e.printStackTrace();
		}

		getCommand("island").setExecutor(new IslandCommand());

		ChallengesCommand challengesCommand = new ChallengesCommand();
		getCommand("challenges").setExecutor(challengesCommand);
		getCommand("challenges").setTabCompleter(challengesCommand);

		getCommand("dev").setExecutor(new DevCommand());

		if (Settings.island_useTopTen) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

				@Override
				public void run() {
					getInstance().updateTopTen(getInstance().generateTopTen());
					instance.log.info("Generating Top 10");
				}

			});
		}
		populateChallengeList();
		log.info(pluginFile.getName() + " v." + pluginFile.getVersion() + " enabled.");
		getInstance().getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
			public void run() {
				if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
					System.out.println("uSkyblock " + "[uSkyBlock] Using vault for permissions");
					VaultHandler.setupPermissions();
					try {
						lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"), Settings.island_height, uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ"));
					} catch (final Exception e) {
						lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);
					}
					if (lastIsland == null) {
						lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);
					}

					if (Settings.island_protectWithWorldGuard && !Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
						final PluginManager manager = uSkyBlock.getInstance().getServer().getPluginManager();
						System.out.println("uSkyblock " + "[uSkyBlock] WorldGuard not loaded! Using built in protection.");
						manager.registerEvents(new ProtectionEvents(), uSkyBlock.getInstance());
					}
				}
			}
		}, 0L);
	}

	public boolean onInfoCooldown(final Player player) {
		if (infoCooldown.containsKey(player.getName())) {
			if (infoCooldown.get(player.getName()).longValue() > Calendar.getInstance().getTimeInMillis()) {
				return true;
			}

			return false;
		}

		return false;
	}

	public boolean onRestartCooldown(final Player player) {
		if (restartCooldown.containsKey(player.getName())) {
			if (restartCooldown.get(player.getName()).longValue() > Calendar.getInstance().getTimeInMillis()) {
				return true;
			}

			return false;
		}

		return false;
	}

	public int orphanCount() {
		return orphaned.size();
	}

	public boolean playerIsInSpawn(final Player player) {
		if (player.getLocation().getX() > -55.0D && player.getLocation().getX() < 55.0D && player.getLocation().getZ() > -55.0D && player.getLocation().getZ() < 55.0D) {
			return true;
		}
		return false;
	}

	public boolean playerIsOnIsland(final Player player) {
		if (isActivePlayer(player.getName())) {
			if (getPlayer(player.getName()).getHasIsland()) {
				islandTestLocation = getPlayer(player.getName()).getIslandLocation();
			} else if (getPlayer(player.getName()).getHasParty()) {
				islandTestLocation = getPlayer(player.getName()).getPartyIslandLocation();
			}
			if (islandTestLocation == null) {
				return false;
			}
			if (player.getLocation().getX() > islandTestLocation.getX() - Settings.island_protectionRange / 2 && player.getLocation().getX() < islandTestLocation.getX() + Settings.island_protectionRange / 2 && player.getLocation().getZ() > islandTestLocation.getZ() - Settings.island_protectionRange / 2 && player.getLocation().getZ() < islandTestLocation.getZ() + Settings.island_protectionRange / 2) {
				return true;
			}
		}
		return false;
	}

	public void populateChallengeList() {
		List<String> templist = new ArrayList<String>();
		for (final String challenges_rank : Settings.challenges_ranks) {
			challenges.put(challenges_rank, templist);
			templist = new ArrayList<String>();
		}
		final Iterator<?> itr = Settings.challenges_challengeList.iterator();
		while (itr.hasNext()) {
			final String tempString = (String) itr.next();
			if (challenges.containsKey(getChallengeConfig().getString("options.challenges.challengeList." + tempString + ".rankLevel"))) {
				challenges.get(getChallengeConfig().getString("options.challenges.challengeList." + tempString + ".rankLevel")).add(tempString);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<Party> readPartyFile() {
		final File f = new File(getDataFolder(), "partylist.bin");
		if (!f.exists()) {
			return null;
		}
		try {
			final FileInputStream fileIn = new FileInputStream(f);
			final ObjectInputStream in = new ObjectInputStream(fileIn);

			final List<Party> p = (List<Party>) in.readObject();
			in.close();
			fileIn.close();
			return p;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private PlayerInfo readPlayerFile(final String playerName) {
		final File f = new File(directoryPlayers, playerName);
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
		} catch (EOFException e) {
			log.warning(playerName + " is corrupted, deleting on exit.");
			f.deleteOnExit();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerEvents() {
		final PluginManager manager = getServer().getPluginManager();

		manager.registerEvents(new PlayerJoin(), this);
		if (!Settings.island_protectWithWorldGuard) {
			System.out.println("uSkyblock " + "[uSkyBlock] Using built in protection.");
			manager.registerEvents(new ProtectionEvents(), getInstance());
		}
	}

	public void reloadData() {
		if (skyblockDataFile == null) {
			skyblockDataFile = new File(getDataFolder(), "skyblockData.yml");
		}
		skyblockData = YamlConfiguration.loadConfiguration(skyblockDataFile);

		final InputStream defConfigStream = getResource("skyblockData.yml");
		if (defConfigStream != null) {
			final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			skyblockData.setDefaults(defConfig);
		}
	}

	public void removeCreatures(final Location l) {
		if (!Settings.island_removeCreaturesByTeleport || l == null) {
			return;
		}

		final int px = l.getBlockX();
		final int py = l.getBlockY();
		final int pz = l.getBlockZ();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				final Chunk c = l.getWorld().getChunkAt(new Location(l.getWorld(), px + x * 16, py, pz + z * 16));
				for (final Entity e : c.getEntities()) {
					if (e.getType() == EntityType.SPIDER || e.getType() == EntityType.CREEPER || e.getType() == EntityType.ENDERMAN || e.getType() == EntityType.SKELETON || e.getType() == EntityType.ZOMBIE) {
						e.remove();
					}
				}
			}
		}
	}

	public void removeInactive(final List<String> removePlayerList) {
		getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(getInstance(), new Runnable() {
			public void run() {
				if (uSkyBlock.getInstance().getRemoveList().size() > 0 && !uSkyBlock.getInstance().isPurgeActive()) {
					uSkyBlock.getInstance().deletePlayerIsland(uSkyBlock.getInstance().getRemoveList().get(0));
					System.out.println("uSkyblock " + "[uSkyBlock] Purge: Removing " + uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
					uSkyBlock.getInstance().deleteFromRemoveList();
				}
			}
		}, 0L, 200L);
	}

	public void removeIsland(final Location loc) {
		
	}

	public void removeIslandBlocks(final Location loc) {
		if (loc != null) {
			System.out.println("uSkyblock " + "Removing blocks from an abandoned island.");
			final Location l = loc;
			final int px = l.getBlockX();
			final int py = l.getBlockY();
			final int pz = l.getBlockZ();
			for (int x = -20; x <= 20; x++) {
				for (int y = -20; y <= 20; y++) {
					for (int z = -20; z <= 20; z++) {
						final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
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

	public void removeNextOrphan() {
		try {
			orphaned.pop();
		} catch (final Exception e) {
		}
	}

	public void saveOrphans() {
		try {
			final File f = new File(getDataFolder(), "orphanedIslands.bin");
			SLAPI.save(changeStackToFile(orphaned), f);
		} catch (final Exception e) {
			System.out.println("uSkyblock " + "Error saving orphan file!");
		}
	}

	public void setInfoCooldown(final Player player) {
		infoCooldown.put(player.getName(), Long.valueOf(Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownInfo * 1000));
	}

	public void setLastIsland(final Location island) {
		MemorySection.createPath(getConfig().getConfigurationSection("options.general"), "lastIslandX");
		MemorySection.createPath(getConfig().getConfigurationSection("options.general"), "lastIslandZ");
		getConfig().set("options.general.lastIslandX", Integer.valueOf(island.getBlockX()));
		getConfig().set("options.general.lastIslandZ", Integer.valueOf(island.getBlockZ()));
		saveConfig();
		lastIsland = island;
	}

	public void setRestartCooldown(final Player player) {
		restartCooldown.put(player.getName(), Long.valueOf(Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownRestart * 1000));
	}

	public void setStringbyPath(final FileConfiguration fc, final File f, final String path, final Object value) {
		fc.set(path, value.toString());
		try {
			fc.save(f);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, Double> sortHashMapByValuesD(final HashMap<String, Double> passedMap) {
		final List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		final List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		Collections.sort(mapValues);
		Collections.reverse(mapValues);
		Collections.sort(mapKeys);
		Collections.reverse(mapKeys);

		final LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

		final Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			final Double val = valueIt.next();
			final Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				final String key = keyIt.next();
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

	public boolean takeRequired(final Player player, final String challenge, final String type) {
		if (type.equalsIgnoreCase("onPlayer")) {
			final String[] reqList = getChallengeConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(" ");

			int reqItem = 0;
			int reqAmount = 0;
			int reqMod = -1;
			for (final String s : reqList) {
				final String[] sPart = s.split(":");
				if (sPart.length == 2) {
					reqItem = Integer.parseInt(sPart[0]);
					reqAmount = Integer.parseInt(sPart[1]);
					if (!player.getInventory().contains(reqItem, reqAmount)) {
						return false;
					}

					player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount) });
				} else if (sPart.length == 3) {
					reqItem = Integer.parseInt(sPart[0]);
					reqAmount = Integer.parseInt(sPart[2]);
					reqMod = Integer.parseInt(sPart[1]);
					if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount)) {
						return false;
					}
					player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount, (short) reqMod) });
				}
			}
			return true;
		}
		if (type.equalsIgnoreCase("onIsland")) {
			return true;
		}
		if (type.equalsIgnoreCase("islandLevel")) {
			return true;
		}
		return false;
	}

	public boolean testForObsidian(final Block block) {
		for (int x = -3; x <= 3; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -3; z <= 3; z++) {
					final Block testBlock = getSkyBlockWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
					if ((x != 0 || y != 0 || z != 0) && testBlock.getType() == Material.OBSIDIAN) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean transferIsland(final String playerfrom, final String playerto) {
		if (!isActivePlayer(playerfrom) || !isActivePlayer(playerto)) {
			return false;
		}
		if (getPlayer(playerfrom).getHasIsland()) {
			getPlayer(playerto).setHasIsland(true);
			getPlayer(playerto).setIslandLocation(getPlayer(playerfrom).getIslandLocation());
			getPlayer(playerto).setIslandLevel(getPlayer(playerfrom).getIslandLevel());
			getPlayer(playerto).setPartyIslandLocation(null);
			getPlayer(playerfrom).setHasIsland(false);
			getPlayer(playerfrom).setIslandLocation(null);
			getPlayer(playerfrom).setIslandLevel(0);
			getPlayer(playerfrom).setPartyIslandLocation(getPlayer(playerto).getIslandLocation());
			return true;
		}
		return false;
	}

	public void unloadPlayerFiles() {
		for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++) {
			final Player[] removedPlayers = Bukkit.getServer().getOnlinePlayers();
			if (isActivePlayer(removedPlayers[i].getName())) {
				removeActivePlayer(removedPlayers[i].getName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void updateOrphans() {
		try {
			final File f = new File(getDataFolder(), "orphanedIslands.bin");

			final Stack<SerializableLocation> load = (Stack<SerializableLocation>) SLAPI.load(f);
			if (load != null) {
				orphaned = changestackfromfile(load);
			}
		} catch (final Exception e) {
			System.out.println("uSkyblock " + "Error saving orphan file!");
		}
	}

	public void updateTopTen(final LinkedHashMap<String, Double> map) {
		topTen = map;
	}

	public void writePartyFile(final List<Party> pi) {
		final File f = new File(getDataFolder(), "partylist.bin");
		try {
			final FileOutputStream fileOut = new FileOutputStream(f);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(pi);
			out.flush();
			out.close();
			fileOut.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void writePlayerFile(final String playerName, final PlayerInfo pi) {
		final File f = new File(directoryPlayers, playerName);
		try {
			final FileOutputStream fileOut = new FileOutputStream(f);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(pi);
			out.flush();
			out.close();
			fileOut.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}