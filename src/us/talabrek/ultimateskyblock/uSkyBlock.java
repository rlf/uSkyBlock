package us.talabrek.ultimateskyblock;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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

import us.talabrek.ultimateskyblock.async.IslandBuilder;
import us.talabrek.ultimateskyblock.async.IslandRemover;
import us.talabrek.ultimateskyblock.async.TopGenerator;
import us.talabrek.ultimateskyblock.command.island.*;

public class uSkyBlock extends JavaPlugin {
	private static uSkyBlock instance;
	public static World skyBlockWorld = null;

	public static uSkyBlock getInstance() {
		return instance;
	}

	public static World getSkyBlockWorld() {
		if (skyBlockWorld == null) {
			skyBlockWorld = WorldCreator.name(Settings.general_worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generateStructures(false).generator(new SkyBlockChunkGenerator()).createWorld();
			if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv import " + Settings.general_worldName + " normal -g uSkyBlock");
			}
		}

		return skyBlockWorld;
	}

	public static boolean isSkyBlockWorld(World world) {
		return world.equals(getSkyBlockWorld());
	}

	public static Logger getLog() {
		return instance.getLogger();
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
	List<String> rankDisplay;
	public List<String> removeList = new ArrayList<String>();
	HashMap<Integer, Integer> requiredList = new HashMap<Integer, Integer>();
	HashMap<String, Long> restartCooldown = new HashMap<String, Long>();
	public File[] schemFile;
	private ArrayList<File> sfiles;
	private FileConfiguration skyblockData = null;

	private File skyblockDataFile = null;

	private ArrayList<Entry<String, Integer>> mTopList;

	public void addOrphan(final Location island) {
		orphaned.push(island);
		saveOrphans();
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

	public void clearArmorContents(final Player player) {
		player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
	}

	public void clearOrphanedIsland() {
		orphaned.clear();
	}

	public void deleteFromRemoveList() {
		removeList.remove(0);
	}

	public void devDeletePlayerIsland(final String player) {
		PlayerInfo island = getPlayer(player);

		island.clearChallenges();
		island.setIslandLocation(null);
		island.setIslandLevel(0);
		island.setIslandExp(0);

		uSkyBlock.getLog().info("Removed " + island.getPlayerName() + "'s island");
		if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island"))
				WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
		}
	}

	public Location getBedrockNear(Location loc, int range) {
		for (int x = loc.getBlockX() - range; x <= loc.getBlockX() + range; ++x) {
			for (int y = loc.getBlockY() - range; y <= loc.getBlockY() + range; ++y) {
				for (int z = loc.getBlockZ() - range; z <= loc.getBlockZ() + range; ++z) {
					if (loc.getWorld().getBlockAt(x, y, z).getType() == Material.BEDROCK)
						return new Location(loc.getWorld(), x, y, z);
				}
			}
		}

		return null;
	}

	public boolean devSetPlayerIsland(final CommandSender sender, final Location l, final String player) {
		Location bedrock = getBedrockNear(l, 10);

		if (bedrock == null)
			return false;

		bedrock.setY(bedrock.getY() + 3);
		PlayerInfo pi = getPlayer(player);

		pi.setHomeLocation(bedrock);
		pi.setHasIsland(true);
		pi.setIslandLocation(bedrock);

		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			try {
				WorldGuardHandler.protectIsland(pi);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			} catch (IllegalStateException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "An error with WorldGuard occured. See console for details.");
			}
		}

		return true;
	}

	public synchronized boolean displayTopTen(CommandSender sender) {
		int i = 1;
		if (mTopList == null) {
			sender.sendMessage(ChatColor.RED + "The top list has not generated yet.");
			return false;
		}
		int playerrank = mTopList.size();
		sender.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");

		String leader = sender.getName();

		if (sender instanceof Player) {
			PlayerInfo info = getPlayerNoStore(sender.getName());
			if (info.getHasParty())
				leader = info.getPartyLeader();
		}

		for (Entry<String, Integer> entry : mTopList) {
			if (i <= 10) {
				if (hasParty(entry.getKey())) {
					PlayerInfo info = getPlayerNoStore(entry.getKey());
					List<String> members = info.getMembers();
					members.remove(entry.getKey());

					sender.sendMessage(ChatColor.GRAY + "" + i + ": " + ChatColor.GOLD + entry.getKey() + ChatColor.GRAY + members.toString() + ChatColor.WHITE + " - Island level " + ChatColor.YELLOW + entry.getValue());
				} else
					sender.sendMessage(ChatColor.GRAY + "" + i + ": " + ChatColor.GOLD + entry.getKey() + ChatColor.WHITE + " - Island level " + ChatColor.YELLOW + entry.getValue());
			} else if (!(sender instanceof Player))
				break;

			if (entry.getKey().equals(leader))
				playerrank = i;

			i++;
		}

		if (sender instanceof Player)
			sender.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank + " of " + mTopList.size());
		return true;
	}

	public void onEnterSkyBlock(Player player) {
		getOrCreatePlayer(player.getName());
	}

	public void onLeaveSkyBlock(Player player) {
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

	public boolean isActivePlayer(String player) {
		return activePlayers.containsKey(player);
	}

	public synchronized PlayerInfo getOrCreatePlayer(String player) {
		if (isActivePlayer(player))
			return activePlayers.get(player);

		PlayerInfo pi = readPlayerFile(player);

		if (pi == null) {
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

	public synchronized boolean deletePlayerData(String player) {
		if (isActivePlayer(player))
			return false;

		File file = new File(directoryPlayers, player);

		if (!file.exists())
			return false;

		return file.delete();
	}

	public synchronized PlayerInfo getPlayerNoStore(String player) {
		if (isActivePlayer(player))
			return activePlayers.get(player);

		PlayerInfo pi = readPlayerFile(player);

		if (pi != null) {
			if (pi.getHasParty() && pi.getPartyIslandLocation() == null) {
				final PlayerInfo pi2 = readPlayerFile(pi.getPartyLeader());
				pi.setPartyIslandLocation(pi2.getIslandLocation());
				writePlayerFile(player, pi);
			}

			pi.buildChallengeList();
		}

		return pi;
	}

	public synchronized PlayerInfo getPlayer(String player) {
		if (isActivePlayer(player))
			return activePlayers.get(player);

		PlayerInfo pi = readPlayerFile(player);

		if (pi != null) {
			if (pi.getHasParty() && pi.getPartyIslandLocation() == null) {
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

	public synchronized void savePlayer(PlayerInfo info) {
		writePlayerFile(info.getPlayerName(), info);
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
					if (!b.isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
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

	public boolean largeIsland(final Location l) {
		int blockcount = 0;
		final int px = l.getBlockX();
		final int py = l.getBlockY();
		final int pz = l.getBlockZ();
		for (int x = -30; x <= 30; x++) {
			for (int y = -30; y <= 30; y++) {
				for (int z = -30; z <= 30; z++) {
					final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
					if (!b.isEmpty() && !b.isLiquid()) {
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

	public void loadPlayerFiles() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!isSkyBlockWorld(player.getWorld()))
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
		directoryPlayers = new File(getDataFolder(), "players");
		if (!directoryPlayers.exists())
			directoryPlayers.mkdir();

		loadPlayerFiles();

		directorySchematics = new File(getDataFolder(), "schematics");
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

		CommandDispatcher islandCommand = new CommandDispatcher("island", "Allows you to play skyblock");
		islandCommand.setDefault(new IslandDefaultCommand());
		islandCommand.registerCommand(new IslandAcceptCommand());
		islandCommand.registerCommand(new IslandRejectCommand());

		islandCommand.registerCommand(new IslandInviteCommand());
		islandCommand.registerCommand(new IslandBanCommand());
		islandCommand.registerCommand(new IslandKickCommand());
		islandCommand.registerCommand(new IslandLeaveCommand());
		islandCommand.registerCommand(new IslandMakeLeaderCommand());
		islandCommand.registerCommand(new IslandPartyCommand());

		islandCommand.registerCommand(new IslandLevelCommand());
		islandCommand.registerCommand(new IslandRestartCommand());
		islandCommand.registerCommand(new IslandSetHomeCommand());
		islandCommand.registerCommand(new IslandSetWarpCommand());
		islandCommand.registerCommand(new IslandToggleWarpCommand());
		islandCommand.registerCommand(new IslandTopCommand());
		islandCommand.registerCommand(new IslandWarpCommand());

		if (Settings.island_allowIslandLock) {
			islandCommand.registerCommand(new IslandLockCommand());
			islandCommand.registerCommand(new IslandUnlockCommand());
		}

		getCommand("island").setExecutor(islandCommand);
		getCommand("island").setTabCompleter(islandCommand);

		ChallengesCommand challengesCommand = new ChallengesCommand();
		getCommand("challenges").setExecutor(challengesCommand);
		getCommand("challenges").setTabCompleter(challengesCommand);

		getCommand("dev").setExecutor(new DevCommand());

		if (Settings.island_useTopTen)
			Bukkit.getScheduler().runTaskAsynchronously(this, new TopGenerator());

		populateChallengeList();
		log.info(pluginFile.getName() + " v." + pluginFile.getVersion() + " enabled.");
		getInstance().getServer().getScheduler().runTask(getInstance(), new Runnable() {
			public void run() {
				// Force world load
				getSkyBlockWorld();

				if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
					log.info("Using vault for permissions");
					VaultHandler.setupPermissions();
				}
				try {
					lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), uSkyBlock.this.getConfig().getInt("options.general.lastIslandX"), Settings.island_height, uSkyBlock.this.getConfig().getInt("options.general.lastIslandZ"));
				} catch (final Exception e) {
					lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);
				}
				if (lastIsland == null)
					lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);

				if (Settings.island_protectWithWorldGuard && !Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
					final PluginManager manager = uSkyBlock.getInstance().getServer().getPluginManager();
					log.info("WorldGuard not loaded! Using built in protection.");
					manager.registerEvents(new ProtectionEvents(), uSkyBlock.getInstance());
				}
			}
		});
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
		// if (!Settings.island_protectWithWorldGuard) {
		System.out.println("uSkyblock " + "[uSkyBlock] Using built in protection.");
		manager.registerEvents(new ProtectionEvents(), getInstance());
		// }
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

		PlayerInfo from = getPlayer(playerfrom);
		PlayerInfo to = getPlayer(playerto);

		if (from.getHasIsland()) {
			to.setHasIsland(true);
			to.setIslandLocation(from.getIslandLocation());
			to.setIslandLevel(from.getIslandLevel());
			to.setPartyIslandLocation(null);

			from.setHasIsland(false);
			from.setIslandLocation(null);
			from.setIslandLevel(0);
			from.setPartyIslandLocation(to.getIslandLocation());

			savePlayer(from);
			savePlayer(to);
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

	private void writePlayerFile(final String playerName, final PlayerInfo pi) {
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

	public void removeIsland(PlayerInfo island) {
		ArrayList<PlayerInfo> list = new ArrayList<PlayerInfo>(1);
		list.add(island);
		removeIslands(list);
	}

	public void restartIsland(PlayerInfo island) {
		ArrayList<PlayerInfo> list = new ArrayList<PlayerInfo>(1);
		list.add(island);

		IslandRemover remover = new IslandRemover(list);
		remover.then(new IslandBuilder(Bukkit.getPlayer(island.getPlayerName())));
		remover.start();
	}

	public void removeIslands(List<PlayerInfo> islands) {
		IslandRemover remover = new IslandRemover(islands);
		remover.start();
	}

	public synchronized void setTopIslands(ArrayList<Entry<String, Integer>> topList) {
		mTopList = topList;
	}

	public synchronized void removeFromTop(PlayerInfo island) {
		if (mTopList == null || (!island.getHasIsland() && !island.getPlayerName().equals(island.getPartyLeader())))
			return;

		for (int i = 0; i < mTopList.size(); ++i) {
			Entry<String, Integer> entry = mTopList.get(i);

			if (entry.getKey().equals(island.getPlayerName())) {
				mTopList.remove(i);
				return;
			}
		}
	}

	public synchronized void updateTopIsland(PlayerInfo island) {
		if (mTopList == null)
			return;

		String name = island.getPlayerName();
		if (island.getHasParty())
			name = island.getPartyLeader();

		int currentIndex = -1;
		int newIndex = -1;
		for (int i = 0; i < mTopList.size(); ++i) {
			Entry<String, Integer> entry = mTopList.get(i);

			if (entry.getKey().equals(name))
				currentIndex = i;

			if (newIndex == -1 && entry.getValue() < island.getIslandLevel())
				newIndex = i;

			if (currentIndex != -1 && newIndex != -1)
				break;
		}

		if (newIndex == -1)
			newIndex = mTopList.size();

		Entry<String, Integer> entry = new AbstractMap.SimpleEntry<String, Integer>(name, island.getIslandLevel());

		if (currentIndex != -1) {
			if (currentIndex < newIndex) {
				mTopList.add(newIndex, entry);
				mTopList.remove(currentIndex);
			} else {
				mTopList.remove(currentIndex);
				mTopList.add(newIndex, entry);
			}
		} else
			mTopList.add(newIndex, entry);
	}
}