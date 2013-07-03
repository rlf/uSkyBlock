package us.talabrek.ultimateskyblock;

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
	public PluginDescriptionFile pluginFile;
	public Logger log;
	/*   57 */public static World skyBlockWorld = null;
	private static uSkyBlock instance;
	/*   59 */public List<String> removeList = new ArrayList<String>();
	List<String> rankDisplay;
	public FileConfiguration configPlugin;
	public File filePlugin;
	private Location lastIsland;
	/*   64 */private Stack<Location> orphaned = new Stack<Location>();
	public File directoryPlayers;
	private File directorySchematics;
	public File[] schemFile;
	public String pName;
	/*   73 */public Location islandTestLocation = null;
	LinkedHashMap<String, Double> topTen;
	/*   75 */HashMap<String, Long> infoCooldown = new HashMap<String, Long>();
	/*   76 */HashMap<String, Long> restartCooldown = new HashMap<String, Long>();
	/*   77 */HashMap<String, PlayerInfo> activePlayers = new HashMap<String, PlayerInfo>();
	/*   78 */LinkedHashMap<String, List> challenges = new LinkedHashMap<String, List>();
	/*   79 */HashMap<Integer, Integer> requiredList = new HashMap<Integer, Integer>();
	/*   80 */public boolean purgeActive = false;
	/*   81 */private FileConfiguration skyblockData = null;
	/*   82 */private File skyblockDataFile = null;
	private ArrayList<File> sfiles;

	public void onDisable() {
		try {
			/*   87 */unloadPlayerFiles();

			/*   89 */if (this.lastIsland != null) {
				/*   91 */setLastIsland(this.lastIsland);
			}

			/*   94 */File f2 = new File(getDataFolder(), "orphanedIslands.bin");

			/*   97 */if (this.orphaned != null) {
				/*   99 */if (!this.orphaned.isEmpty())
					/*  100 */SLAPI.save(changeStackToFile(this.orphaned), f2);
			}
		} catch (Exception e) {
			/*  105 */System.out.println("Something went wrong saving the island and/or party data!");
			/*  106 */e.printStackTrace();
		}
		/*  108 */this.log.info(this.pluginFile.getName() + " v" + this.pluginFile.getVersion() + " disabled.");
	}

	public void onEnable() {
		/*  113 */instance = this;
		/*  114 */saveDefaultConfig();
		/*  115 */this.pluginFile = getDescription();
		/*  116 */this.log = getLogger();
		/*  117 */this.pName = (ChatColor.WHITE + "[" + ChatColor.GREEN + this.pluginFile.getName() + ChatColor.WHITE + "] ");
		try {
			/*  119 */Metrics metrics = new Metrics(this);
			/*  120 */metrics.start();
		} catch (IOException localIOException) {
		}
		/*  125 */VaultHandler.setupEconomy();
		/*  126 */if (!getDataFolder().exists()) {
			/*  127 */getDataFolder().mkdir();
		}

		/*  130 */this.configPlugin = getConfig();
		/*  131 */this.filePlugin = new File(getDataFolder(), "config.yml");
		/*  132 */loadPluginConfig();
		/*  133 */registerEvents();
		/*  134 */this.directoryPlayers = new File(getDataFolder() + File.separator + "players");
		/*  135 */if (!this.directoryPlayers.exists()) {
			/*  136 */this.directoryPlayers.mkdir();
			/*  137 */loadPlayerFiles();
		} else {
			/*  139 */loadPlayerFiles();
		}

		/*  142 */this.directorySchematics = new File(getDataFolder() + File.separator + "schematics");
		/*  143 */if (!this.directorySchematics.exists()) {
			/*  144 */this.directorySchematics.mkdir();
		}
		/*  146 */this.schemFile = this.directorySchematics.listFiles();
		/*  147 */if (this.schemFile == null) {
			/*  149 */System.out.print("[uSkyBlock] No schematic file loaded.");
		}
		/*  151 */else
			System.out.print("[uSkyBlock] " + this.schemFile.length + " schematics loaded.");

		try {
			/*  172 */if (new File(getDataFolder(), "orphanedIslands.bin").exists()) {
				/*  174 */Stack<SerializableLocation> load = (Stack<SerializableLocation>) SLAPI.load(new File(getDataFolder(),
						"orphanedIslands.bin"));
				/*  175 */if (load != null) {
					/*  177 */if (!load.isEmpty())
						/*  178 */this.orphaned = changestackfromfile(load);
				}
			} else {
				/*  182 */System.out.print("Creating a new orphan file");
				/*  183 */new File("orphanedIslands.bin");
			}
		} catch (Exception e) {
			/*  187 */System.out.println("Could not load Island and/or Party data from disk.");
			/*  188 */e.printStackTrace();
		}

		/*  193 */getCommand("island").setExecutor(new IslandCommand());
		/*  194 */getCommand("challenges").setExecutor(new ChallengesCommand());
		/*  195 */getCommand("dev").setExecutor(new DevCommand());

		/*  202 */if (Settings.island_useTopTen)
			/*  203 */getInstance().updateTopTen(getInstance().generateTopTen());
		/*  204 */populateChallengeList();
		/*  205 */this.log.info(this.pluginFile.getName() + " v." + this.pluginFile.getVersion() + " enabled.");
		/*  206 */getInstance().getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
			public void run() {
				/*  210 */if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
					/*  212 */System.out.print("[uSkyBlock] Using vault for permissions");
					/*  213 */VaultHandler.setupPermissions();
					try {
						/*  215 */uSkyBlock.this.lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), uSkyBlock.this.getConfig()
								.getInt("options.general.lastIslandX"), Settings.island_height, uSkyBlock.this.getConfig().getInt(
								"options.general.lastIslandZ"));
					} catch (Exception e) {
						/*  217 */uSkyBlock.this.lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height,
								0.0D);
					}
					/*  219 */if (uSkyBlock.this.lastIsland == null) {
						/*  221 */uSkyBlock.this.lastIsland = new Location(uSkyBlock.getSkyBlockWorld(), 0.0D, Settings.island_height,
								0.0D);
					}

					/*  224 */if ((Settings.island_protectWithWorldGuard)
							&& (!Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
						/*  226 */PluginManager manager = uSkyBlock.getInstance().getServer().getPluginManager();
						/*  227 */System.out.print("[uSkyBlock] WorldGuard not loaded! Using built in protection.");
						/*  228 */manager.registerEvents(new ProtectionEvents(), uSkyBlock.getInstance());
					}
				}
			}
		}, 0L);
	}

	public static uSkyBlock getInstance() {
		/*  237 */return instance;
	}

	public void loadPlayerFiles() {
		/*  242 */int onlinePlayerCount = 0;
		/*  243 */onlinePlayerCount = Bukkit.getServer().getOnlinePlayers().length;
		/*  244 */Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		/*  245 */for (int i = 0; i < onlinePlayerCount; i++) {
			/*  247 */if (onlinePlayers[i].isOnline()) {
				/*  249 */PlayerInfo pi = getInstance().readPlayerFile(onlinePlayers[i].getName());
				/*  250 */if (pi == null) {
					/*  252 */System.out.print("Creating a new skyblock file for " + onlinePlayers[i].getName());
					/*  253 */pi = new PlayerInfo(onlinePlayers[i].getName());
					/*  254 */getInstance().writePlayerFile(onlinePlayers[i].getName(), pi);
				}
				/*  256 */if ((pi.getHasParty()) && (pi.getPartyIslandLocation() == null)) {
					/*  258 */PlayerInfo pi2 = getInstance().readPlayerFile(pi.getPartyLeader());
					/*  259 */pi.setPartyIslandLocation(pi2.getIslandLocation());
					/*  260 */getInstance().writePlayerFile(onlinePlayers[i].getName(), pi);
				}
				/*  262 */pi.buildChallengeList();
				/*  263 */getInstance().addActivePlayer(onlinePlayers[i].getName(), pi);
			}
		}
	}

	public void unloadPlayerFiles() {
		/*  273 */for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++) {
			/*  275 */Player[] removedPlayers = Bukkit.getServer().getOnlinePlayers();
			/*  276 */if (getActivePlayers().containsKey(removedPlayers[i].getName()))
				/*  277 */removeActivePlayer(removedPlayers[i].getName());
		}
	}

	public void registerEvents() {
		/*  286 */PluginManager manager = getServer().getPluginManager();

		/*  289 */manager.registerEvents(new PlayerJoin(), this);
		/*  290 */if (!Settings.island_protectWithWorldGuard) {
			/*  292 */System.out.print("[uSkyBlock] Using built in protection.");
			/*  293 */manager.registerEvents(new ProtectionEvents(), getInstance());
		}
	}

	public void loadPluginConfig() {
		try {
			/*  306 */getConfig();
		} catch (Exception e) {
			/*  308 */e.printStackTrace();
		}

		try {
			/*  313 */Settings.general_maxPartySize = getConfig().getInt("options.general.maxPartySize");
			/*  314 */if (Settings.general_maxPartySize < 0)
				/*  315 */Settings.general_maxPartySize = 0;
		} catch (Exception e) {
			/*  318 */Settings.general_maxPartySize = 4;
		}
		try {
			/*  321 */Settings.island_distance = getConfig().getInt("options.island.distance");
			/*  322 */if (Settings.island_distance < 50)
				/*  323 */Settings.island_distance = 50;
		} catch (Exception e) {
			/*  326 */Settings.island_distance = 110;
		}
		try {
			/*  329 */Settings.island_protectionRange = getConfig().getInt("options.island.protectionRange");
			/*  330 */if (Settings.island_protectionRange > Settings.island_distance)
				/*  331 */Settings.island_protectionRange = Settings.island_distance;
		} catch (Exception e) {
			/*  334 */Settings.island_protectionRange = 100;
		}
		try {
			/*  337 */Settings.general_cooldownInfo = getConfig().getInt("options.general.cooldownInfo");
			/*  338 */if (Settings.general_cooldownInfo < 0)
				/*  339 */Settings.general_cooldownInfo = 0;
		} catch (Exception e) {
			/*  342 */Settings.general_cooldownInfo = 60;
		}
		try {
			/*  345 */Settings.general_cooldownRestart = getConfig().getInt("options.general.cooldownRestart");
			/*  346 */if (Settings.general_cooldownRestart < 0)
				/*  347 */Settings.general_cooldownRestart = 0;
		} catch (Exception e) {
			/*  350 */Settings.general_cooldownRestart = 60;
		}
		try {
			/*  353 */Settings.island_height = getConfig().getInt("options.island.height");
			/*  354 */if (Settings.island_height < 20)
				/*  355 */Settings.island_height = 20;
		} catch (Exception e) {
			/*  358 */Settings.island_height = 120;
		}
		try {
			/*  361 */Settings.challenges_rankLeeway = getConfig().getInt("options.challenges.rankLeeway");
			/*  362 */if (Settings.challenges_rankLeeway < 0)
				/*  363 */Settings.challenges_rankLeeway = 0;
		} catch (Exception e) {
			/*  366 */Settings.island_height = 120;
		}

		/*  369 */if (!getConfig().contains("options.extras.obsidianToLava")) {
			/*  371 */getConfig().set("options.extras.obsidianToLava", Boolean.valueOf(true));
			/*  372 */saveConfig();
		}

		/*  377 */String[] chestItemString = getConfig().getString("options.island.chestItems").split(" ");
		/*  378 */ItemStack[] tempChest = new ItemStack[chestItemString.length];
		/*  379 */String[] amountdata = new String[2];
		/*  380 */for (int i = 0; i < tempChest.length; i++) {
			/*  382 */amountdata = chestItemString[i].split(":");
			/*  383 */tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
		}
		/*  385 */Settings.island_chestItems = tempChest;
		/*  386 */Settings.island_allowPvP = getConfig().getString("options.island.allowPvP");
		/*  387 */Settings.island_schematicName = getConfig().getString("options.island.schematicName");
		/*  388 */if (!Settings.island_allowPvP.equalsIgnoreCase("allow"))
			/*  389 */Settings.island_allowPvP = "deny";
		/*  390 */Set<?> permissionList = getConfig().getConfigurationSection("options.island.extraPermissions").getKeys(true);
		/*  391 */Settings.island_addExtraItems = getConfig().getBoolean("options.island.addExtraItems");
		/*  392 */Settings.extras_obsidianToLava = getConfig().getBoolean("options.extras.obsidianToLava");
		/*  393 */Settings.island_useIslandLevel = getConfig().getBoolean("options.island.useIslandLevel");
		/*  394 */Settings.island_extraPermissions = (String[]) permissionList.toArray(new String[0]);
		/*  395 */Settings.island_protectWithWorldGuard = getConfig().getBoolean("options.island.protectWithWorldGuard");
		/*  396 */Settings.extras_sendToSpawn = getConfig().getBoolean("options.extras.sendToSpawn");
		/*  397 */Settings.island_useTopTen = getConfig().getBoolean("options.island.useTopTen");

		/*  399 */Settings.general_worldName = getConfig().getString("options.general.worldName");
		/*  400 */Settings.island_removeCreaturesByTeleport = getConfig().getBoolean("options.island.removeCreaturesByTeleport");
		/*  401 */Settings.island_allowIslandLock = getConfig().getBoolean("options.island.allowIslandLock");
		/*  402 */Settings.island_useOldIslands = getConfig().getBoolean("options.island.useOldIslands");

		/*  404 */Set<String> challengeList = getConfig().getConfigurationSection("options.challenges.challengeList").getKeys(false);
		/*  405 */Settings.challenges_challengeList = challengeList;
		/*  406 */Settings.challenges_broadcastCompletion = getConfig().getBoolean("options.challenges.broadcastCompletion");
		/*  407 */Settings.challenges_broadcastText = getConfig().getString("options.challenges.broadcastText");
		/*  408 */Settings.challenges_challengeColor = getConfig().getString("options.challenges.challengeColor");
		/*  409 */Settings.challenges_enableEconomyPlugin = getConfig().getBoolean("options.challenges.enableEconomyPlugin");
		/*  410 */Settings.challenges_finishedColor = getConfig().getString("options.challenges.finishedColor");
		/*  411 */Settings.challenges_repeatableColor = getConfig().getString("options.challenges.repeatableColor");
		/*  412 */Settings.challenges_requirePreviousRank = getConfig().getBoolean("options.challenges.requirePreviousRank");
		/*  413 */Settings.challenges_allowChallenges = getConfig().getBoolean("options.challenges.allowChallenges");
		/*  414 */String[] rankListString = getConfig().getString("options.challenges.ranks").split(" ");
		/*  415 */Settings.challenges_ranks = rankListString;
	}

	@SuppressWarnings("unchecked")
	public List<Party> readPartyFile() {
		/*  420 */File f = new File(getDataFolder(), "partylist.bin");
		/*  421 */if (!f.exists()) {
			/*  422 */return null;
		}
		try {
			/*  426 */FileInputStream fileIn = new FileInputStream(f);
			/*  427 */ObjectInputStream in = new ObjectInputStream(fileIn);

			/*  429 */List<Party> p = (List<Party>) in.readObject();
			/*  430 */in.close();
			/*  431 */fileIn.close();
			/*  432 */return p;
		} catch (Exception e) {
			/*  434 */e.printStackTrace();
		}
		/*  436 */return null;
	}

	public void writePartyFile(List<Party> pi) {
		/*  441 */File f = new File(getDataFolder(), "partylist.bin");
		try {
			/*  444 */FileOutputStream fileOut = new FileOutputStream(f);
			/*  445 */ObjectOutputStream out = new ObjectOutputStream(fileOut);
			/*  446 */out.writeObject(pi);
			/*  447 */out.flush();
			/*  448 */out.close();
			/*  449 */fileOut.close();
		} catch (Exception e) {
			/*  451 */e.printStackTrace();
		}
	}

	public PlayerInfo readPlayerFile(String playerName) {
		/*  457 */File f = new File(this.directoryPlayers, playerName);
		/*  458 */if (!f.exists()) {
			/*  459 */return null;
		}
		try {
			/*  463 */FileInputStream fileIn = new FileInputStream(f);
			/*  464 */ObjectInputStream in = new ObjectInputStream(fileIn);
			/*  465 */PlayerInfo p = (PlayerInfo) in.readObject();
			/*  466 */in.close();
			/*  467 */fileIn.close();
			/*  468 */return p;
		} catch (Exception e) {
			/*  470 */e.printStackTrace();
		}
		/*  472 */return null;
	}

	public void writePlayerFile(String playerName, PlayerInfo pi) {
		/*  477 */File f = new File(this.directoryPlayers, playerName);
		try {
			/*  480 */FileOutputStream fileOut = new FileOutputStream(f);
			/*  481 */ObjectOutputStream out = new ObjectOutputStream(fileOut);
			/*  482 */out.writeObject(pi);
			/*  483 */out.flush();
			/*  484 */out.close();
			/*  485 */fileOut.close();
		} catch (Exception e) {
			/*  487 */e.printStackTrace();
		}
	}

	public boolean displayTopTen(Player player) {
		/*  493 */int i = 1;
		/*  494 */int playerrank = 0;
		/*  495 */player.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");
		/*  496 */if (this.topTen == null) {
			/*  498 */player.sendMessage(ChatColor.RED + "Top ten list not generated yet!");
			/*  499 */return false;
		}

		/*  504 */PlayerInfo pi2 = (PlayerInfo) getActivePlayers().get(player.getName());
		/*  505 */for (String playerName : this.topTen.keySet()) {
			/*  507 */if (i <= 10) {
				/*  510 */if (hasParty(playerName)) {
					/*  512 */PlayerInfo pix = readPlayerFile(playerName);
					/*  513 */List<?> pMembers = pix.getMembers();
					/*  514 */if (pMembers.contains(playerName))
						/*  515 */pMembers.remove(playerName);
					/*  516 */player.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + pMembers.toString() + " - Island level "
							+ ((Double) this.topTen.get(playerName)).intValue());
				} else {
					/*  519 */player.sendMessage(ChatColor.GREEN + "#" + i + ": " + playerName + " - Island level "
							+ ((Double) this.topTen.get(playerName)).intValue());
				}
			}
			/*  521 */if (playerName.equalsIgnoreCase(player.getName())) {
				/*  523 */playerrank = i;
			}
			/*  525 */if (pi2.getHasParty()) {
				/*  527 */if (playerName.equalsIgnoreCase(pi2.getPartyLeader()))
					/*  528 */playerrank = i;
			}
			/*  530 */i++;
		}
		/*  532 */player.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank);
		/*  533 */return true;
	}

	public void updateTopTen(LinkedHashMap<String, Double> map) {
		/*  538 */this.topTen = map;
	}

	public Location getLocationString(String s) {
		/*  548 */if ((s == null) || (s.trim() == "")) {
			/*  549 */return null;
		}
		/*  551 */String[] parts = s.split(":");
		/*  552 */if (parts.length == 4) {
			/*  553 */World w = getServer().getWorld(parts[0]);
			/*  554 */int x = Integer.parseInt(parts[1]);
			/*  555 */int y = Integer.parseInt(parts[2]);
			/*  556 */int z = Integer.parseInt(parts[3]);
			/*  557 */return new Location(w, x, y, z);
		}
		/*  559 */return null;
	}

	public String getStringLocation(Location l) {
		/*  569 */if (l == null) {
			/*  570 */return "";
		}
		/*  572 */return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
	}

	public void setStringbyPath(FileConfiguration fc, File f, String path, Object value) {
		/*  585 */fc.set(path, value.toString());
		try {
			/*  587 */fc.save(f);
		} catch (IOException e) {
			/*  589 */e.printStackTrace();
		}
	}

	public String getStringbyPath(FileConfiguration fc, File file, String path, Object stdValue, boolean addMissing) {
		/*  603 */if (!fc.contains(path)) {
			/*  604 */if (addMissing) {
				/*  605 */setStringbyPath(fc, file, path, stdValue);
			}
			/*  607 */return stdValue.toString();
		}
		/*  609 */return fc.getString(path);
	}

	public static World getSkyBlockWorld() {
		/*  618 */if (skyBlockWorld == null) {
			/*  619 */skyBlockWorld = WorldCreator.name(Settings.general_worldName).type(WorldType.FLAT)
					.environment(World.Environment.NORMAL).generator(new SkyBlockChunkGenerator()).createWorld();
			/*  620 */if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
				/*  622 */Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"mv import " + Settings.general_worldName + " normal -g uSkyBlock");
			}
		}

		/*  626 */return skyBlockWorld;
	}

	public void clearOrphanedIsland() {
		/*  630 */while (hasOrphanedIsland())
			/*  631 */this.orphaned.pop();
	}

	public void clearArmorContents(Player player) {
		/*  640 */player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
	}

	public void getAllFiles(String path) {
		/*  652 */File dirpath = new File(path);
		/*  653 */if (!dirpath.exists()) {
			/*  654 */return;
		}

		/*  657 */for (File f : dirpath.listFiles())
			try {
				/*  659 */if (!f.isDirectory())
					/*  660 */this.sfiles.add(f);
				else
					/*  662 */getAllFiles(f.getAbsolutePath());
			} catch (Exception ex) {
				/*  665 */this.log.warning(ex.getMessage());
			}
	}

	public Location getYLocation(Location l) {
		/*  677 */for (int y = 0; y < 254; y++) {
			/*  678 */int px = l.getBlockX();
			/*  679 */int py = y;
			/*  680 */int pz = l.getBlockZ();
			/*  681 */Block b1 = new Location(l.getWorld(), px, py, pz).getBlock();
			/*  682 */Block b2 = new Location(l.getWorld(), px, py + 1, pz).getBlock();
			/*  683 */Block b3 = new Location(l.getWorld(), px, py + 2, pz).getBlock();
			/*  684 */if ((!b1.getType().equals(Material.AIR)) && (b2.getType().equals(Material.AIR))
					&& (b3.getType().equals(Material.AIR))) {
				/*  685 */return b2.getLocation();
			}
		}
		/*  688 */return l;
	}

	public Location getSafeHomeLocation(PlayerInfo p) {
		/*  693 */Location home = null;
		/*  694 */if (p.getHomeLocation() == null) {
			/*  695 */if ((p.getIslandLocation() == null) && (p.getHasParty())) {
				/*  697 */home = p.getPartyIslandLocation();
				/*  698 */} else if (p.getIslandLocation() != null)
				/*  699 */home = p.getIslandLocation();
		}
		/*  701 */else
			home = p.getHomeLocation();

		/*  704 */if (isSafeLocation(home)) {
			/*  705 */return home;
		}

		/*  708 */for (int y = home.getBlockY() + 25; y > 0; y--) {
			/*  709 */Location n = new Location(home.getWorld(), home.getBlockX(), y, home.getBlockZ());
			/*  710 */if (isSafeLocation(n)) {
				/*  711 */return n;
			}
		}
		/*  714 */for (int y = home.getBlockY(); y < 255; y++) {
			/*  715 */Location n = new Location(home.getWorld(), home.getBlockX(), y, home.getBlockZ());
			/*  716 */if (isSafeLocation(n)) {
				/*  717 */return n;
			}
		}
		/*  720 */if ((p.getHasParty()) && (!p.getPartyLeader().equalsIgnoreCase(p.getPlayer().getName()))) {
			/*  721 */return p.getPartyIslandLocation();
		}
		/*  723 */Location island = p.getIslandLocation();
		/*  724 */if (isSafeLocation(island)) {
			/*  725 */return island;
		}

		/*  728 */for (int y = island.getBlockY() + 25; y > 0; y--) {
			/*  729 */Location n = new Location(island.getWorld(), island.getBlockX(), y, island.getBlockZ());
			/*  730 */if (isSafeLocation(n)) {
				/*  731 */return n;
			}
		}
		/*  734 */for (int y = island.getBlockY(); y < 255; y++) {
			/*  735 */Location n = new Location(island.getWorld(), island.getBlockX(), y, island.getBlockZ());
			/*  736 */if (isSafeLocation(n)) {
				/*  737 */return n;
			}
		}
		/*  740 */if ((p.getHasParty()) && (!p.getPartyLeader().equalsIgnoreCase(p.getPlayer().getName()))) {
			/*  741 */return p.getPartyIslandLocation();
		}
		/*  743 */return p.getHomeLocation();
	}

	public boolean isSafeLocation(Location l) {
		/*  747 */if (l == null) {
			/*  748 */return false;
		}

		/*  751 */Block ground = l.getBlock().getRelative(BlockFace.DOWN);
		/*  752 */Block air1 = l.getBlock();
		/*  753 */Block air2 = l.getBlock().getRelative(BlockFace.UP);
		/*  754 */if (ground.getType().equals(Material.AIR))
			/*  755 */return false;
		/*  756 */if (ground.getType().equals(Material.LAVA))
			/*  757 */return false;
		/*  758 */if (ground.getType().equals(Material.STATIONARY_LAVA))
			/*  759 */return false;
		/*  760 */if (ground.getType().equals(Material.CACTUS))
			/*  761 */return false;
		/*  762 */if (((air1.getType().equals(Material.AIR)) || (air1.getType().equals(Material.CROPS))
				|| (air1.getType().equals(Material.LONG_GRASS)) || (air1.getType().equals(Material.RED_ROSE))
				|| (air1.getType().equals(Material.YELLOW_FLOWER)) || (air1.getType().equals(Material.DEAD_BUSH))
				|| (air1.getType().equals(Material.SIGN_POST)) || (air1.getType().equals(Material.SIGN)))
				&& (air2.getType().equals(Material.AIR)))
			/*  763 */return true;
		/*  764 */return false;
	}

	public void removeCreatures(Location l) {
		/*  773 */if ((!Settings.island_removeCreaturesByTeleport) || (l == null)) {
			/*  774 */return;
		}

		/*  777 */int px = l.getBlockX();
		/*  778 */int py = l.getBlockY();
		/*  779 */int pz = l.getBlockZ();
		/*  780 */for (int x = -1; x <= 1; x++)
			/*  781 */for (int z = -1; z <= 1; z++) {
				/*  782 */Chunk c = l.getWorld().getChunkAt(new Location(l.getWorld(), px + x * 16, py, pz + z * 16));
				/*  783 */for (Entity e : c.getEntities())
					/*  784 */if ((e.getType() == EntityType.SPIDER) || (e.getType() == EntityType.CREEPER)
							|| (e.getType() == EntityType.ENDERMAN) || (e.getType() == EntityType.SKELETON)
							|| (e.getType() == EntityType.ZOMBIE))
						/*  785 */e.remove();
			}
	}

	public void deletePlayerIsland(String player) {
		/*  793 */if (!getActivePlayers().containsKey(player)) {
			/*  795 */PlayerInfo pi = readPlayerFile(player);
			/*  796 */if ((Settings.island_protectWithWorldGuard) && (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
				/*  798 */if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island"))
					/*  799 */WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
			}
			/*  801 */this.orphaned.push(pi.getIslandLocation());
			/*  802 */removeIsland(pi.getIslandLocation());
			/*  803 */pi = new PlayerInfo(player);
			/*  804 */saveOrphans();
			/*  805 */updateOrphans();
			/*  806 */writePlayerFile(player, pi);
		} else {
			/*  809 */if ((Settings.island_protectWithWorldGuard) && (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
				/*  811 */if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island"))
					/*  812 */WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
			}
			/*  814 */this.orphaned.push(((PlayerInfo) getActivePlayers().get(player)).getIslandLocation());
			/*  815 */removeIsland(((PlayerInfo) getActivePlayers().get(player)).getIslandLocation());
			/*  816 */PlayerInfo pi = new PlayerInfo(player);
			/*  817 */removeActivePlayer(player);
			/*  818 */addActivePlayer(player, pi);
			/*  819 */saveOrphans();
			/*  820 */updateOrphans();
		}
	}

	public void devDeletePlayerIsland(String player) {
		/*  827 */if (!getActivePlayers().containsKey(player)) {
			/*  829 */PlayerInfo pi = readPlayerFile(player);
			/*  830 */if ((Settings.island_protectWithWorldGuard) && (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
				/*  832 */if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island")) {
					/*  833 */WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
				}
			}
			/*  836 */pi = new PlayerInfo(player);
			/*  837 */writePlayerFile(player, pi);
		} else {
			/*  840 */if ((Settings.island_protectWithWorldGuard) && (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
				/*  842 */if (WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).hasRegion(player + "Island"))
					/*  843 */WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld()).removeRegion(player + "Island");
			}
			/*  845 */PlayerInfo pi = new PlayerInfo(player);
			/*  846 */removeActivePlayer(player);
			/*  847 */addActivePlayer(player, pi);
		}
	}

	public boolean devSetPlayerIsland(CommandSender sender, Location l, String player) {
		/*  853 */if (!getActivePlayers().containsKey(player)) {
			/*  855 */PlayerInfo pi = readPlayerFile(player);
			/*  856 */int px = l.getBlockX();
			/*  857 */int py = l.getBlockY();
			/*  858 */int pz = l.getBlockZ();
			/*  859 */for (int x = -10; x <= 10; x++) {
				/*  860 */for (int y = -10; y <= 10; y++)
					/*  861 */for (int z = -10; z <= 10; z++) {
						/*  862 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						/*  863 */if (b.getTypeId() == 7) {
							/*  865 */pi.setHomeLocation(new Location(l.getWorld(), px + x, py + y + 3, pz + z));
							/*  866 */pi.setHasIsland(true);
							/*  867 */pi.setIslandLocation(b.getLocation());
							/*  868 */writePlayerFile(player, pi);
							/*  869 */if ((Settings.island_protectWithWorldGuard)
									&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")))
								/*  870 */WorldGuardHandler.protectIsland(sender, player);
							/*  871 */return true;
						}
					}
			}
		} else {
			/*  878 */int px = l.getBlockX();
			/*  879 */int py = l.getBlockY();
			/*  880 */int pz = l.getBlockZ();
			/*  881 */for (int x = -10; x <= 10; x++) {
				/*  882 */for (int y = -10; y <= 10; y++) {
					/*  883 */for (int z = -10; z <= 10; z++) {
						/*  884 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						/*  885 */if (b.getTypeId() == 7) {
							/*  887 */((PlayerInfo) getActivePlayers().get(player)).setHomeLocation(new Location(l.getWorld(), px + x, py
									+ y + 3, pz + z));
							/*  888 */((PlayerInfo) getActivePlayers().get(player)).setHasIsland(true);
							/*  889 */((PlayerInfo) getActivePlayers().get(player)).setIslandLocation(b.getLocation());
							/*  890 */PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player);
							/*  891 */removeActivePlayer(player);
							/*  892 */addActivePlayer(player, pi);
							/*  893 */if ((Settings.island_protectWithWorldGuard)
									&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")))
								/*  894 */WorldGuardHandler.protectIsland(sender, player);
							/*  895 */return true;
						}
					}
				}
			}
		}
		/*  901 */return false;
	}

	public int orphanCount() {
		/*  906 */return this.orphaned.size();
	}

	public void removeIsland(Location loc) {
		/*  915 */if (loc != null) {
			/*  916 */Location l = loc;
			/*  917 */int px = l.getBlockX();
			/*  918 */int py = l.getBlockY();
			/*  919 */int pz = l.getBlockZ();
			/*  920 */for (int x = Settings.island_protectionRange / 2 * -1; x <= Settings.island_protectionRange / 2; x++)
				/*  921 */for (int y = 0; y <= 255; y++)
					/*  922 */for (int z = Settings.island_protectionRange / 2 * -1; z <= Settings.island_protectionRange / 2; z++) {
						/*  923 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						/*  924 */if (!b.getType().equals(Material.AIR)) {
							/*  925 */if (b.getType().equals(Material.CHEST)) {
								/*  926 */Chest c = (Chest) b.getState();
								/*  927 */ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
								/*  928 */c.getInventory().setContents(items);
								/*  929 */} else if (b.getType().equals(Material.FURNACE)) {
								/*  930 */Furnace f = (Furnace) b.getState();
								/*  931 */ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
								/*  932 */f.getInventory().setContents(items);
								/*  933 */} else if (b.getType().equals(Material.DISPENSER)) {
								/*  934 */Dispenser d = (Dispenser) b.getState();
								/*  935 */ItemStack[] items = new ItemStack[d.getInventory().getContents().length];
								/*  936 */d.getInventory().setContents(items);
							}
							/*  938 */b.setType(Material.AIR);
						}
					}
		}
	}

	public void removeIslandBlocks(Location loc) {
		/*  947 */if (loc != null) {
			/*  948 */System.out.print("Removing blocks from an abandoned island.");
			/*  949 */Location l = loc;
			/*  950 */int px = l.getBlockX();
			/*  951 */int py = l.getBlockY();
			/*  952 */int pz = l.getBlockZ();
			/*  953 */for (int x = -20; x <= 20; x++)
				/*  954 */for (int y = -20; y <= 20; y++)
					/*  955 */for (int z = -20; z <= 20; z++) {
						/*  956 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						/*  957 */if (!b.getType().equals(Material.AIR)) {
							/*  958 */if (b.getType().equals(Material.CHEST)) {
								/*  959 */Chest c = (Chest) b.getState();
								/*  960 */ItemStack[] items = new ItemStack[c.getInventory().getContents().length];
								/*  961 */c.getInventory().setContents(items);
								/*  962 */} else if (b.getType().equals(Material.FURNACE)) {
								/*  963 */Furnace f = (Furnace) b.getState();
								/*  964 */ItemStack[] items = new ItemStack[f.getInventory().getContents().length];
								/*  965 */f.getInventory().setContents(items);
								/*  966 */} else if (b.getType().equals(Material.DISPENSER)) {
								/*  967 */Dispenser d = (Dispenser) b.getState();
								/*  968 */ItemStack[] items = new ItemStack[d.getInventory().getContents().length];
								/*  969 */d.getInventory().setContents(items);
							}
							/*  971 */b.setType(Material.AIR);
						}
					}
		}
	}

	public boolean hasParty(String playername) {
		/*  995 */if (getActivePlayers().containsKey(playername)) {
			/*  997 */return ((PlayerInfo) getActivePlayers().get(playername)).getHasParty();
		}

		/* 1000 */PlayerInfo pi = getInstance().readPlayerFile(playername);
		/* 1001 */if (pi == null)
			/* 1002 */return false;
		/* 1003 */return pi.getHasParty();
	}

	public Location getLastIsland() {
		/* 1019 */if (this.lastIsland.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			/* 1020 */return this.lastIsland;
		}

		/* 1023 */setLastIsland(new Location(getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D));
		/* 1024 */return new Location(getSkyBlockWorld(), 0.0D, Settings.island_height, 0.0D);
	}

	public void setLastIsland(Location island) {
		/* 1030 */getConfig();
		FileConfiguration.createPath(getConfig().getConfigurationSection("options.general"), "lastIslandX");
		/* 1031 */getConfig();
		FileConfiguration.createPath(getConfig().getConfigurationSection("options.general"), "lastIslandZ");
		/* 1032 */getConfig().set("options.general.lastIslandX", Integer.valueOf(island.getBlockX()));
		/* 1033 */getConfig().set("options.general.lastIslandZ", Integer.valueOf(island.getBlockZ()));
		/* 1034 */saveConfig();
		/* 1035 */this.lastIsland = island;
	}

	public boolean hasOrphanedIsland() {
		/* 1044 */return !this.orphaned.empty();
	}

	public Location checkOrphan() {
		/* 1049 */return (Location) this.orphaned.peek();
	}

	public Location getOrphanedIsland() {
		/* 1053 */if (hasOrphanedIsland()) {
			/* 1054 */return (Location) this.orphaned.pop();
		}

		/* 1057 */return null;
	}

	public void addOrphan(Location island) {
		/* 1062 */this.orphaned.push(island);
	}

	public void removeNextOrphan() {
		/* 1067 */this.orphaned.pop();
	}

	public void saveOrphans() {
		try {
			/* 1073 */File f = new File(getDataFolder(), "orphanedIslands.bin");
			/* 1074 */SLAPI.save(changeStackToFile(this.orphaned), f);
		} catch (Exception e) {
			/* 1076 */System.out.print("Error saving orphan file!");
		}
	}

	public void updateOrphans() {
		try {
			/* 1083 */File f = new File(getDataFolder(), "orphanedIslands.bin");

			/* 1085 */Stack<SerializableLocation> load = (Stack<SerializableLocation>) SLAPI.load(f);
			/* 1086 */if (load != null)
				/* 1087 */this.orphaned = changestackfromfile(load);
		} catch (Exception e) {
			/* 1089 */System.out.print("Error saving orphan file!");
		}
	}

	public boolean homeTeleport(Player player) {
		/* 1094 */Location homeSweetHome = null;
		/* 1095 */if (getActivePlayers().containsKey(player.getName())) {
			/* 1097 */homeSweetHome = getInstance().getSafeHomeLocation((PlayerInfo) getActivePlayers().get(player.getName()));
		}

		/* 1101 */if (homeSweetHome == null) {
			/* 1102 */player.performCommand("spawn");
			/* 1103 */player.sendMessage(ChatColor.RED + "You are not part of an island. Returning you the spawn area!");
			/* 1104 */return true;
		}

		/* 1107 */getInstance().removeCreatures(homeSweetHome);
		/* 1108 */player.teleport(homeSweetHome);
		/* 1109 */player.sendMessage(ChatColor.GREEN + "Teleporting you to your island. (/island help for more info)");
		/* 1110 */return true;
	}

	public boolean homeSet(Player player) {
		/* 1120 */if (!player.getWorld().getName().equalsIgnoreCase(getSkyBlockWorld().getName())) {
			/* 1121 */player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
			/* 1122 */return true;
		}
		/* 1124 */if (playerIsOnIsland(player)) {
			/* 1126 */if (getActivePlayers().containsKey(player.getName())) {
				/* 1128 */((PlayerInfo) getActivePlayers().get(player.getName())).setHomeLocation(player.getLocation());
			}

			/* 1131 */player.sendMessage(ChatColor.GREEN + "Your skyblock home has been set to your current location.");
			/* 1132 */return true;
		}
		/* 1134 */player.sendMessage(ChatColor.RED + "You must be closer to your island to set your skyblock home!");
		/* 1135 */return true;
	}

	public boolean homeSet(String player, Location loc) {
		/* 1140 */if (getActivePlayers().containsKey(player)) {
			/* 1142 */((PlayerInfo) getActivePlayers().get(player)).setHomeLocation(loc);
		} else {
			/* 1145 */PlayerInfo pi = getInstance().readPlayerFile(player);
			/* 1146 */pi.setHomeLocation(loc);
			/* 1147 */getInstance().writePlayerFile(player, pi);
		}

		/* 1150 */return true;
	}

	public boolean playerIsOnIsland(Player player) {
		/* 1155 */if (getActivePlayers().containsKey(player.getName())) {
			/* 1157 */if (((PlayerInfo) getActivePlayers().get(player.getName())).getHasIsland()) {
				/* 1159 */this.islandTestLocation = ((PlayerInfo) getActivePlayers().get(player.getName())).getIslandLocation();
				/* 1160 */} else if (((PlayerInfo) getActivePlayers().get(player.getName())).getHasParty()) {
				/* 1162 */this.islandTestLocation = ((PlayerInfo) getActivePlayers().get(player.getName())).getPartyIslandLocation();
			}
			/* 1164 */if (this.islandTestLocation == null)
				/* 1165 */return false;
			/* 1166 */if ((player.getLocation().getX() > this.islandTestLocation.getX() - Settings.island_protectionRange / 2)
					&& (player.getLocation().getX() < this.islandTestLocation.getX() + Settings.island_protectionRange / 2) &&
					/* 1167 */(player.getLocation().getZ() > this.islandTestLocation.getZ() - Settings.island_protectionRange / 2)
					&& (player.getLocation().getZ() < this.islandTestLocation.getZ() + Settings.island_protectionRange / 2))
				/* 1168 */return true;
		}
		/* 1170 */return false;
	}

	public boolean locationIsOnIsland(Player player, Location loc) {
		/* 1175 */if (getActivePlayers().containsKey(player.getName())) {
			/* 1177 */if (((PlayerInfo) getActivePlayers().get(player.getName())).getHasIsland()) {
				/* 1179 */this.islandTestLocation = ((PlayerInfo) getActivePlayers().get(player.getName())).getIslandLocation();
				/* 1180 */} else if (((PlayerInfo) getActivePlayers().get(player.getName())).getHasParty()) {
				/* 1182 */this.islandTestLocation = ((PlayerInfo) getActivePlayers().get(player.getName())).getPartyIslandLocation();
			}
			/* 1184 */if (this.islandTestLocation == null)
				/* 1185 */return false;
			/* 1186 */if ((loc.getX() > this.islandTestLocation.getX() - Settings.island_protectionRange / 2)
					&& (loc.getX() < this.islandTestLocation.getX() + Settings.island_protectionRange / 2) &&
					/* 1187 */(loc.getZ() > this.islandTestLocation.getZ() - Settings.island_protectionRange / 2)
					&& (loc.getZ() < this.islandTestLocation.getZ() + Settings.island_protectionRange / 2))
				/* 1188 */return true;
		}
		/* 1190 */return false;
	}

	public boolean playerIsInSpawn(Player player) {
		/* 1195 */if ((player.getLocation().getX() > -55.0D) && (player.getLocation().getX() < 55.0D)
				&& (player.getLocation().getZ() > -55.0D) && (player.getLocation().getZ() < 55.0D))
			/* 1196 */return true;
		/* 1197 */return false;
	}

	public boolean hasIsland(String playername) {
		/* 1202 */if (getActivePlayers().containsKey(playername)) {
			/* 1204 */return ((PlayerInfo) getActivePlayers().get(playername)).getHasIsland();
		}

		/* 1207 */PlayerInfo pi = getInstance().readPlayerFile(playername);
		/* 1208 */if (pi == null)
			/* 1209 */return false;
		/* 1210 */return pi.getHasIsland();
	}

	public Location getPlayerIsland(String playername) {
		/* 1216 */if (getActivePlayers().containsKey(playername)) {
			/* 1218 */return ((PlayerInfo) getActivePlayers().get(playername)).getIslandLocation();
		}

		/* 1221 */PlayerInfo pi = getInstance().readPlayerFile(playername);
		/* 1222 */if (pi == null)
			/* 1223 */return null;
		/* 1224 */return pi.getIslandLocation();
	}

	public boolean transferIsland(String playerfrom, String playerto) {
		/* 1230 */if ((!getActivePlayers().containsKey(playerfrom)) || (!getActivePlayers().containsKey(playerto))) {
			/* 1232 */return false;
		}
		/* 1234 */if (((PlayerInfo) getActivePlayers().get(playerfrom)).getHasIsland()) {
			/* 1236 */((PlayerInfo) getActivePlayers().get(playerto)).setHasIsland(true);
			/* 1237 */((PlayerInfo) getActivePlayers().get(playerto)).setIslandLocation(((PlayerInfo) getActivePlayers().get(playerfrom))
					.getIslandLocation());
			/* 1238 */((PlayerInfo) getActivePlayers().get(playerto)).setIslandLevel(((PlayerInfo) getActivePlayers().get(playerfrom))
					.getIslandLevel());
			/* 1239 */((PlayerInfo) getActivePlayers().get(playerto)).setPartyIslandLocation(null);
			/* 1240 */((PlayerInfo) getActivePlayers().get(playerfrom)).setHasIsland(false);
			/* 1241 */((PlayerInfo) getActivePlayers().get(playerfrom)).setIslandLocation(null);
			/* 1242 */((PlayerInfo) getActivePlayers().get(playerfrom)).setIslandLevel(0);
			/* 1243 */((PlayerInfo) getActivePlayers().get(playerfrom)).setPartyIslandLocation(((PlayerInfo) getActivePlayers().get(
					playerto)).getIslandLocation());
			/* 1244 */return true;
		}
		/* 1246 */return false;
	}

	public boolean islandAtLocation(Location loc) {
		/* 1253 */if (loc == null) {
			/* 1255 */return true;
		}
		/* 1257 */int px = loc.getBlockX();
		/* 1258 */int py = loc.getBlockY();
		/* 1259 */int pz = loc.getBlockZ();
		/* 1260 */for (int x = -2; x <= 2; x++) {
			/* 1261 */for (int y = -2; y <= 2; y++) {
				/* 1262 */for (int z = -2; z <= 2; z++) {
					/* 1263 */Block b = new Location(loc.getWorld(), px + x, py + y, pz + z).getBlock();
					/* 1264 */if (b.getTypeId() != 0)
						/* 1265 */return true;
				}
			}
		}
		/* 1269 */return false;
	}

	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		/* 1273 */return new SkyBlockChunkGenerator();
	}

	public Stack<SerializableLocation> changeStackToFile(Stack<Location> stack) {
		/* 1278 */Stack<SerializableLocation> finishStack = new Stack<SerializableLocation>();
		/* 1279 */Stack<Location> tempStack = new Stack<Location>();
		/* 1280 */while (!stack.isEmpty())
			/* 1281 */tempStack.push((Location) stack.pop());
		/* 1282 */while (!tempStack.isEmpty()) {
			/* 1284 */if (tempStack.peek() != null) {
				/* 1286 */finishStack.push(new SerializableLocation((Location) tempStack.pop()));
			}
			/* 1288 */else
				tempStack.pop();
		}
		/* 1290 */return finishStack;
	}

	public Stack<Location> changestackfromfile(Stack<SerializableLocation> stack) {
		/* 1294 */Stack<SerializableLocation> tempStack = new Stack<SerializableLocation>();
		/* 1295 */Stack<Location> finishStack = new Stack<Location>();
		/* 1296 */while (!stack.isEmpty())
			/* 1297 */tempStack.push((SerializableLocation) stack.pop());
		/* 1298 */while (!tempStack.isEmpty()) {
			/* 1300 */if (tempStack.peek() != null)
				/* 1301 */finishStack.push(((SerializableLocation) tempStack.pop()).getLocation());
			else
				/* 1303 */tempStack.pop();
		}
		/* 1305 */return finishStack;
	}

	public boolean largeIsland(Location l) {
		/* 1310 */int blockcount = 0;
		/* 1311 */int px = l.getBlockX();
		/* 1312 */int py = l.getBlockY();
		/* 1313 */int pz = l.getBlockZ();
		/* 1314 */for (int x = -30; x <= 30; x++) {
			/* 1315 */for (int y = -30; y <= 30; y++) {
				/* 1316 */for (int z = -30; z <= 30; z++) {
					/* 1317 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
					/* 1318 */if ((b.getTypeId() != 0) && (b.getTypeId() != 8) && (b.getTypeId() != 10)) {
						/* 1320 */if (blockcount > 200) {
							/* 1322 */return true;
						}
					}
				}
			}
		}
		/* 1328 */if (blockcount > 200) {
			/* 1330 */return true;
		}
		/* 1332 */return false;
	}

	public boolean clearAbandoned() {
		/* 1339 */int numOffline = 0;
		/* 1340 */OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
		/* 1341 */System.out.print("Attemping to add more orphans");
		/* 1342 */for (int i = 0; i < oplayers.length; i++) {
			/* 1344 */long offlineTime = oplayers[i].getLastPlayed();
			/* 1345 */offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
			/* 1346 */if ((offlineTime > 250L) && (getInstance().hasIsland(oplayers[i].getName())) && (offlineTime < 50000L)) {
				/* 1348 */PlayerInfo pi = getInstance().readPlayerFile(oplayers[i].getName());
				/* 1349 */Location l = pi.getIslandLocation();
				/* 1350 */int blockcount = 0;
				/* 1351 */int px = l.getBlockX();
				/* 1352 */int py = l.getBlockY();
				/* 1353 */int pz = l.getBlockZ();
				/* 1354 */for (int x = -30; x <= 30; x++) {
					/* 1355 */for (int y = -30; y <= 30; y++) {
						/* 1356 */for (int z = -30; z <= 30; z++) {
							/* 1357 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
							/* 1358 */if ((b.getTypeId() != 0) && (b.getTypeId() != 8) && (b.getTypeId() != 10)) {
								/* 1360 */blockcount++;
							}
						}
					}
				}
				/* 1365 */if (blockcount < 200) {
					/* 1368 */numOffline++;
					/* 1369 */WorldGuardHandler.getWorldGuard().getRegionManager(getSkyBlockWorld())
							.removeRegion(oplayers[i].getName() + "Island");
					/* 1370 */this.orphaned.push(pi.getIslandLocation());

					/* 1372 */pi.setHomeLocation(null);
					/* 1373 */pi.setHasIsland(false);
					/* 1374 */pi.setIslandLocation(null);
					/* 1375 */writePlayerFile(oplayers[i].getName(), pi);
				}
			}
		}

		/* 1380 */if (numOffline > 0) {
			/* 1382 */System.out.print("Added " + numOffline + " new orphans.");
			/* 1383 */saveOrphans();
			/* 1384 */updateOrphans();
			/* 1385 */return true;
		}
		/* 1387 */System.out.print("No new orphans to add!");
		/* 1388 */return false;
	}

	public LinkedHashMap<String, Double> generateTopTen() {
		/* 1393 */HashMap<String, Double> tempMap = new LinkedHashMap<String, Double>();
		/* 1394 */File folder = this.directoryPlayers;
		/* 1395 */File[] listOfFiles = folder.listFiles();

		/* 1398 */for (int i = 0; i < listOfFiles.length; i++) {
			PlayerInfo pi;
			/* 1400 */if ((pi = getInstance().readPlayerFile(listOfFiles[i].getName())) != null) {
				/* 1402 */if ((pi.getIslandLevel() > 0)
						&& ((!pi.getHasParty()) || (pi.getPartyLeader().equalsIgnoreCase(pi.getPlayerName())))) {
					/* 1404 */tempMap.put(listOfFiles[i].getName(), Double.valueOf(pi.getIslandLevel()));
				}
			}
		}
		/* 1408 */LinkedHashMap<String, Double> sortedMap = sortHashMapByValuesD(tempMap);
		/* 1409 */return sortedMap;
	}

	public LinkedHashMap<String, Double> sortHashMapByValuesD(HashMap<String, Double> passedMap) {
		/* 1414 */List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		/* 1415 */List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		/* 1416 */Collections.sort(mapValues);
		/* 1417 */Collections.reverse(mapValues);
		/* 1418 */Collections.sort(mapKeys);
		/* 1419 */Collections.reverse(mapKeys);

		/* 1421 */LinkedHashMap<String, Double> sortedMap =
		/* 1422 */new LinkedHashMap<String, Double>();

		/* 1424 */Iterator<Double> valueIt = mapValues.iterator();
		/* 1425 */while (valueIt.hasNext()) {
			/* 1426 */Double val = (Double) valueIt.next();
			/* 1427 */Iterator<String> keyIt = mapKeys.iterator();

			/* 1429 */while (keyIt.hasNext()) {
				/* 1430 */String key = (String) keyIt.next();
				/* 1431 */String comp1 = ((Double) passedMap.get(key)).toString();
				/* 1432 */String comp2 = val.toString();

				/* 1434 */if (comp1.equals(comp2)) {
					/* 1435 */passedMap.remove(key);
					/* 1436 */mapKeys.remove(key);
					/* 1437 */sortedMap.put(key, val);
					/* 1438 */break;
				}
			}

		}

		/* 1444 */return sortedMap;
	}

	public boolean onInfoCooldown(Player player) {
		/* 1449 */if (this.infoCooldown.containsKey(player.getName())) {
			/* 1451 */if (((Long) this.infoCooldown.get(player.getName())).longValue() > Calendar.getInstance().getTimeInMillis()) {
				/* 1452 */return true;
			}

			/* 1455 */return false;
		}

		/* 1458 */return false;
	}

	public boolean onRestartCooldown(Player player) {
		/* 1463 */if (this.restartCooldown.containsKey(player.getName())) {
			/* 1465 */if (((Long) this.restartCooldown.get(player.getName())).longValue() > Calendar.getInstance().getTimeInMillis()) {
				/* 1466 */return true;
			}

			/* 1469 */return false;
		}

		/* 1472 */return false;
	}

	public long getInfoCooldownTime(Player player) {
		/* 1477 */if (this.infoCooldown.containsKey(player.getName())) {
			/* 1479 */if (((Long) this.infoCooldown.get(player.getName())).longValue() > Calendar.getInstance().getTimeInMillis()) {
				/* 1480 */return ((Long) this.infoCooldown.get(player.getName())).longValue() - Calendar.getInstance().getTimeInMillis();
			}

			/* 1483 */return 0L;
		}

		/* 1486 */return 0L;
	}

	public long getRestartCooldownTime(Player player) {
		/* 1491 */if (this.restartCooldown.containsKey(player.getName())) {
			/* 1493 */if (((Long) this.restartCooldown.get(player.getName())).longValue() > Calendar.getInstance().getTimeInMillis()) {
				/* 1494 */return ((Long) this.restartCooldown.get(player.getName())).longValue()
						- Calendar.getInstance().getTimeInMillis();
			}

			/* 1497 */return 0L;
		}

		/* 1500 */return 0L;
	}

	public void setInfoCooldown(Player player) {
		/* 1505 */this.infoCooldown.put(player.getName(),
				Long.valueOf(Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownInfo * 1000));
	}

	public void setRestartCooldown(Player player) {
		/* 1510 */this.restartCooldown.put(player.getName(),
				Long.valueOf(Calendar.getInstance().getTimeInMillis() + Settings.general_cooldownRestart * 1000));
	}

	public File[] getSchemFile() {
		/* 1515 */return this.schemFile;
	}

	public boolean testForObsidian(Block block) {
		/* 1521 */for (int x = -3; x <= 3; x++)
			/* 1522 */for (int y = -3; y <= 3; y++)
				/* 1523 */for (int z = -3; z <= 3; z++) {
					/* 1525 */Block testBlock = getSkyBlockWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
					/* 1526 */if (((x != 0) || (y != 0) || (z != 0)) && (testBlock.getType() == Material.OBSIDIAN)) {
						/* 1528 */return true;
					}
				}
		/* 1531 */return false;
	}

	public void removeInactive(List<String> removePlayerList) {
		/* 1536 */getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(getInstance(), new Runnable() {
			public void run() {
				/* 1539 */if ((uSkyBlock.getInstance().getRemoveList().size() > 0) && (!uSkyBlock.getInstance().isPurgeActive())) {
					/* 1541 */uSkyBlock.getInstance().deletePlayerIsland((String) uSkyBlock.getInstance().getRemoveList().get(0));
					/* 1542 */System.out.print("[uSkyBlock] Purge: Removing " + (String) uSkyBlock.getInstance().getRemoveList().get(0)
							+ "'s island");
					/* 1543 */uSkyBlock.getInstance().deleteFromRemoveList();
				}
			}
		}, 0L, 200L);
	}

	public List<String> getRemoveList() {
		/* 1551 */return this.removeList;
	}

	public void addToRemoveList(String string) {
		/* 1556 */this.removeList.add(string);
	}

	public void deleteFromRemoveList() {
		/* 1561 */this.removeList.remove(0);
	}

	public boolean isPurgeActive() {
		/* 1566 */return this.purgeActive;
	}

	public void activatePurge() {
		/* 1571 */this.purgeActive = true;
	}

	public void deactivatePurge() {
		/* 1576 */this.purgeActive = false;
	}

	public HashMap<String, PlayerInfo> getActivePlayers() {
		/* 1581 */return this.activePlayers;
	}

	public void addActivePlayer(String player, PlayerInfo pi) {
		/* 1586 */this.activePlayers.put(player, pi);
	}

	public void removeActivePlayer(String player) {
		/* 1591 */if (this.activePlayers.containsKey(player)) {
			/* 1593 */writePlayerFile(player, (PlayerInfo) this.activePlayers.get(player));

			/* 1595 */this.activePlayers.remove(player);
			/* 1596 */System.out.print("Removing player from memory: " + player);
		}
	}

	public void populateChallengeList() {
		/* 1602 */List<String> templist = new ArrayList<String>();
		/* 1603 */for (int i = 0; i < Settings.challenges_ranks.length; i++) {
			/* 1605 */this.challenges.put(Settings.challenges_ranks[i], templist);
			/* 1606 */templist = new ArrayList<String>();
		}
		/* 1608 */Iterator<?> itr = Settings.challenges_challengeList.iterator();
		/* 1609 */while (itr.hasNext()) {
			/* 1611 */String tempString = (String) itr.next();
			/* 1612 */if (this.challenges.containsKey(getConfig().getString(
					"options.challenges.challengeList." + tempString + ".rankLevel"))) {
				/* 1614 */((List<String>) this.challenges.get(getConfig().getString(
						"options.challenges.challengeList." + tempString + ".rankLevel"))).add(tempString);
			}
		}
	}

	public String getChallengesFromRank(Player player, String rank) {
		/* 1623 */this.rankDisplay = ((List<String>) this.challenges.get(rank));
		/* 1624 */String fullString = "";
		/* 1625 */PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());
		/* 1626 */Iterator<String> itr = this.rankDisplay.iterator();
		/* 1627 */while (itr.hasNext()) {
			/* 1629 */String tempString = (String) itr.next();
			/* 1630 */if (pi.checkChallenge(tempString)) {
				/* 1632 */if (getConfig().getBoolean("options.challenges.challengeList." + tempString + ".repeatable")) {
					/* 1634 */fullString = fullString + Settings.challenges_repeatableColor.replace('&', '§') + tempString
							+ ChatColor.DARK_GRAY + " - ";
				}
				/* 1636 */else
					fullString = fullString + Settings.challenges_finishedColor.replace('&', '§') + tempString + ChatColor.DARK_GRAY
							+ " - ";
			} else {
				/* 1639 */fullString = fullString + Settings.challenges_challengeColor.replace('&', '§') + tempString
						+ ChatColor.DARK_GRAY + " - ";
			}
		}
		/* 1642 */if (fullString.length() > 3)
			/* 1643 */fullString = fullString.substring(0, fullString.length() - 2);
		/* 1644 */return fullString;
	}

	public int checkRankCompletion(Player player, String rank) {
		/* 1649 */if (!Settings.challenges_requirePreviousRank)
			/* 1650 */return 0;
		/* 1651 */this.rankDisplay = ((List<String>) this.challenges.get(rank));
		/* 1652 */int ranksCompleted = 0;
		/* 1653 */PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());
		/* 1654 */Iterator<String> itr = this.rankDisplay.iterator();
		/* 1655 */while (itr.hasNext()) {
			/* 1657 */String tempString = (String) itr.next();
			/* 1658 */if (pi.checkChallenge(tempString)) {
				/* 1660 */ranksCompleted++;
			}

		}

		/* 1666 */return this.rankDisplay.size() - Settings.challenges_rankLeeway - ranksCompleted;
	}

	public boolean isRankAvailable(Player player, String rank) {
		/* 1671 */if (this.challenges.size() < 2) {
			/* 1673 */return true;
		}

		/* 1677 */for (int i = 0; i < Settings.challenges_ranks.length; i++) {
			/* 1679 */if (Settings.challenges_ranks[i].equalsIgnoreCase(rank)) {
				/* 1681 */if (i == 0) {
					/* 1682 */return true;
				}

				/* 1685 */if (checkRankCompletion(player, Settings.challenges_ranks[(i - 1)]) <= 0) {
					/* 1686 */return true;
				}
			}

		}

		/* 1692 */return false;
	}

	public boolean checkIfCanCompleteChallenge(Player player, String challenge) {
		/* 1697 */PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());

		/* 1701 */if (!isRankAvailable(player, getConfig().getString("options.challenges.challengeList." + challenge + ".rankLevel"))) {
			/* 1703 */player.sendMessage(ChatColor.RED + "You have not unlocked this challenge yet!");
			/* 1704 */return false;
		}
		/* 1706 */if (!pi.challengeExists(challenge)) {
			/* 1708 */player.sendMessage(ChatColor.RED + "Unknown challenge name (check spelling)!");
			/* 1709 */return false;
		}
		/* 1711 */if ((pi.checkChallenge(challenge))
				&& (!getConfig().getBoolean("options.challenges.challengeList." + challenge + ".repeatable"))) {
			/* 1713 */player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
			/* 1714 */return false;
		}
		/* 1716 */if ((pi.checkChallenge(challenge))
				&& ((getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")) || (getConfig()
						.getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")))) {
			/* 1718 */player.sendMessage(ChatColor.RED + "This challenge is not repeatable!");
			/* 1719 */return false;
		}
		/* 1721 */if (getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onPlayer")) {
			/* 1723 */if (!hasRequired(player, challenge, "onPlayer")) {
				/* 1725 */player
						.sendMessage(ChatColor.RED
								+ getConfig().getString(
										new StringBuilder("options.challenges.challengeList.").append(challenge).append(".description")
												.toString()));
				/* 1726 */player.sendMessage(ChatColor.RED + "You don't have enough of the required item(s)!");
				/* 1727 */return false;
			}
			/* 1729 */return true;
			/* 1730 */}
		if (getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("onIsland")) {
			/* 1732 */if (!playerIsOnIsland(player)) {
				/* 1734 */player.sendMessage(ChatColor.RED + "You must be on your island to do that!");
			}
			/* 1736 */if (!hasRequired(player, challenge, "onIsland")) {
				/* 1738 */player
						.sendMessage(ChatColor.RED
								+ getConfig().getString(
										new StringBuilder("options.challenges.challengeList.").append(challenge).append(".description")
												.toString()));

				/* 1740 */player.sendMessage(ChatColor.RED + "You must be standing within 10 blocks of all required items.");
				/* 1741 */return false;
			}
			/* 1743 */return true;
			/* 1744 */}
		if (getConfig().getString("options.challenges.challengeList." + challenge + ".type").equalsIgnoreCase("islandLevel")) {
			/* 1746 */if (pi.getIslandLevel() >= getConfig().getInt("options.challenges.challengeList." + challenge + ".requiredItems")) {
				/* 1748 */return true;
			}

			/* 1751 */player.sendMessage(ChatColor.RED
					+ "Your island must be level "
					+ getConfig().getInt(
							new StringBuilder("options.challenges.challengeList.").append(challenge).append(".requiredItems").toString())
					+ " to complete this challenge!");
			/* 1752 */return false;
		}

		/* 1755 */return false;
	}

	public boolean takeRequired(Player player, String challenge, String type) {
		/* 1760 */if (type.equalsIgnoreCase("onPlayer")) {
			/* 1762 */String[] reqList = getConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(
					" ");

			/* 1764 */int reqItem = 0;
			/* 1765 */int reqAmount = 0;
			/* 1766 */int reqMod = -1;
			/* 1767 */for (String s : reqList) {
				/* 1769 */String[] sPart = s.split(":");
				/* 1770 */if (sPart.length == 2) {
					/* 1772 */reqItem = Integer.parseInt(sPart[0]);
					/* 1773 */reqAmount = Integer.parseInt(sPart[1]);
					/* 1774 */if (!player.getInventory().contains(reqItem, reqAmount)) {
						/* 1775 */return false;
					}

					/* 1778 */player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount) });
				}
				/* 1780 */else if (sPart.length == 3) {
					/* 1782 */reqItem = Integer.parseInt(sPart[0]);
					/* 1783 */reqAmount = Integer.parseInt(sPart[2]);
					/* 1784 */reqMod = Integer.parseInt(sPart[1]);
					/* 1785 */if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount)) {
						/* 1786 */return false;
					}
					/* 1788 */player.getInventory().removeItem(new ItemStack[] { new ItemStack(reqItem, reqAmount, (short) reqMod) });
				}
			}
			/* 1791 */return true;
			/* 1792 */}
		if (type.equalsIgnoreCase("onIsland")) {
			/* 1794 */return true;
			/* 1795 */}
		if (type.equalsIgnoreCase("islandLevel")) {
			/* 1797 */return true;
		}
		/* 1799 */return false;
	}

	public boolean hasRequired(Player player, String challenge, String type) {
		/* 1804 */String[] reqList = getConfig().getString("options.challenges.challengeList." + challenge + ".requiredItems").split(" ");

		/* 1806 */if (type.equalsIgnoreCase("onPlayer")) {
			/* 1808 */int reqItem = 0;
			/* 1809 */int reqAmount = 0;
			/* 1810 */int reqMod = -1;
			/* 1811 */for (String s : reqList) {
				/* 1813 */String[] sPart = s.split(":");
				/* 1814 */if (sPart.length == 2) {
					/* 1816 */reqItem = Integer.parseInt(sPart[0]);
					/* 1817 */reqAmount = Integer.parseInt(sPart[1]);
					/* 1818 */if (!player.getInventory().contains(reqItem, reqAmount))
						/* 1819 */return false;
					/* 1820 */} else if (sPart.length == 3) {
					/* 1822 */reqItem = Integer.parseInt(sPart[0]);
					/* 1823 */reqAmount = Integer.parseInt(sPart[2]);
					/* 1824 */reqMod = Integer.parseInt(sPart[1]);
					/* 1825 */if (!player.getInventory().containsAtLeast(new ItemStack(reqItem, reqAmount, (short) reqMod), reqAmount))
						/* 1826 */return false;
				}
			}
			/* 1829 */if (getConfig().getBoolean("options.challenges.challengeList." + challenge + ".takeItems"))
				/* 1830 */takeRequired(player, challenge, type);
			/* 1831 */return true;
			/* 1832 */}
		if (type.equalsIgnoreCase("onIsland")) {
			/* 1834 */int[][] neededItem = new int[reqList.length][2];
			/* 1835 */for (int i = 0; i < reqList.length; i++) {
				/* 1837 */String[] sPart = reqList[i].split(":");
				/* 1838 */neededItem[i][0] = Integer.parseInt(sPart[0]);
				/* 1839 */neededItem[i][1] = Integer.parseInt(sPart[1]);
			}
			/* 1841 */Location l = player.getLocation();
			/* 1842 */int px = l.getBlockX();
			/* 1843 */int py = l.getBlockY();
			/* 1844 */int pz = l.getBlockZ();
			/* 1845 */for (int x = -10; x <= 10; x++) {
				/* 1846 */for (int y = -3; y <= 10; y++) {
					/* 1847 */for (int z = -10; z <= 10; z++) {
						/* 1848 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
						/* 1849 */for (int i = 0; i < neededItem.length; i++) {
							/* 1852 */if (b.getTypeId() == neededItem[i][0]) {
								/* 1855 */neededItem[i][1] -= 1;
							}
						}
					}
				}
			}
			/* 1861 */for (int i = 0; i < neededItem.length; i++) {
				/* 1863 */if (neededItem[i][1] > 0) {
					/* 1865 */return false;
				}
			}
			/* 1868 */return true;
		}

		/* 1873 */return true;
	}

	public boolean giveReward(Player player, String challenge) {
		/* 1879 */String[] permList = getConfig().getString(
				"options.challenges.challengeList." + challenge.toLowerCase() + ".permissionReward").split(" ");
		/* 1880 */int rewCurrency = 0;
		/* 1881 */player.sendMessage(ChatColor.GREEN + "You have completed the " + challenge + " challenge!");
		String[] rewList;
		/* 1882 */if (!((PlayerInfo) getInstance().getActivePlayers().get(player.getName())).checkChallenge(challenge)) {
			/* 1884 */rewList = getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".itemReward")
					.split(" ");
			/* 1885 */if ((Settings.challenges_enableEconomyPlugin) && (VaultHandler.econ != null))
				/* 1886 */rewCurrency = getConfig().getInt(
						"options.challenges.challengeList." + challenge.toLowerCase() + ".currencyReward");
		} else {
			/* 1890 */rewList = getConfig().getString("options.challenges.challengeList." + challenge.toLowerCase() + ".repeatItemReward")
					.split(" ");
			/* 1891 */if ((Settings.challenges_enableEconomyPlugin) && (VaultHandler.econ != null)) {
				/* 1892 */rewCurrency = getConfig().getInt(
						"options.challenges.challengeList." + challenge.toLowerCase() + ".repeatCurrencyReward");
			}
		}
		/* 1895 */int rewItem = 0;
		/* 1896 */int rewAmount = 0;
		/* 1897 */int rewMod = -1;
		/* 1898 */if ((Settings.challenges_enableEconomyPlugin) && (VaultHandler.econ != null)) {
			/* 1900 */VaultHandler.econ.depositPlayer(player.getName(), rewCurrency);
			/* 1901 */if (((PlayerInfo) getInstance().getActivePlayers().get(player.getName())).checkChallenge(challenge)) {
				/* 1903 */player.giveExp(getInstance().getConfig().getInt(
						"options.challenges.challengeList." + challenge + ".repeatXpReward"));
				/* 1904 */player.sendMessage(ChatColor.YELLOW
						+ "Repeat reward(s): "
						+ ChatColor.WHITE
						+ getInstance()
								.getConfig()
								.getString(
										new StringBuilder("options.challenges.challengeList.").append(challenge)
												.append(".repeatRewardText").toString()).replace('&', '§'));
				/* 1905 */player.sendMessage(ChatColor.YELLOW
						+ "Repeat exp reward: "
						+ ChatColor.WHITE
						+ getInstance().getConfig().getInt(
								new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatXpReward")
										.toString()));
				/* 1906 */player.sendMessage(ChatColor.YELLOW
						+ "Repeat currency reward: "
						+ ChatColor.WHITE
						+ getInstance().getConfig().getInt(
								new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatCurrencyReward")
										.toString()) + " " + VaultHandler.econ.currencyNamePlural());
			} else {
				/* 1909 */if (Settings.challenges_broadcastCompletion)
					/* 1910 */Bukkit.getServer().broadcastMessage(
							Settings.challenges_broadcastText.replace('&', '§') + player.getName() + " has completed the " + challenge
									+ " challenge!");
				/* 1911 */player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
				/* 1912 */player.sendMessage(ChatColor.YELLOW
						+ "Reward(s): "
						+ ChatColor.WHITE
						+ getInstance()
								.getConfig()
								.getString(
										new StringBuilder("options.challenges.challengeList.").append(challenge).append(".rewardText")
												.toString()).replace('&', '§'));
				/* 1913 */player.sendMessage(ChatColor.YELLOW
						+ "Exp reward: "
						+ ChatColor.WHITE
						+ getInstance().getConfig().getInt(
								new StringBuilder("options.challenges.challengeList.").append(challenge).append(".xpReward").toString()));
				/* 1914 */player.sendMessage(ChatColor.YELLOW
						+ "Currency reward: "
						+ ChatColor.WHITE
						+ getInstance().getConfig().getInt(
								new StringBuilder("options.challenges.challengeList.").append(challenge).append(".currencyReward")
										.toString()) + " " + VaultHandler.econ.currencyNamePlural());
			}

		}
		/* 1918 */else if (((PlayerInfo) getInstance().getActivePlayers().get(player.getName())).checkChallenge(challenge)) {
			/* 1920 */player
					.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".repeatXpReward"));
			/* 1921 */player.sendMessage(ChatColor.YELLOW
					+ "Repeat reward(s): "
					+ ChatColor.WHITE
					+ getInstance()
							.getConfig()
							.getString(
									new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatRewardText")
											.toString()).replace('&', '§'));
			/* 1922 */player.sendMessage(ChatColor.YELLOW
					+ "Repeat exp reward: "
					+ ChatColor.WHITE
					+ getInstance().getConfig().getInt(
							new StringBuilder("options.challenges.challengeList.").append(challenge).append(".repeatXpReward").toString()));
		} else {
			/* 1925 */if (Settings.challenges_broadcastCompletion)
				/* 1926 */Bukkit.getServer().broadcastMessage(
						Settings.challenges_broadcastText.replace('&', '§') + player.getName() + " has completed the " + challenge
								+ " challenge!");
			/* 1927 */player.giveExp(getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".xpReward"));
			/* 1928 */player.sendMessage(ChatColor.YELLOW
					+ "Reward(s): "
					+ ChatColor.WHITE
					+ getInstance()
							.getConfig()
							.getString(
									new StringBuilder("options.challenges.challengeList.").append(challenge).append(".rewardText")
											.toString()).replace('&', '§'));
			/* 1929 */player.sendMessage(ChatColor.YELLOW
					+ "Exp reward: "
					+ ChatColor.WHITE
					+ getInstance().getConfig().getInt(
							new StringBuilder("options.challenges.challengeList.").append(challenge).append(".xpReward").toString()));
		}

		/* 1932 */for (String s : permList) {
			/* 1934 */if (!s.equalsIgnoreCase("none")) {
				/* 1936 */if (!VaultHandler.checkPerk(player.getName(), s, player.getWorld())) {
					/* 1938 */VaultHandler.addPerk(player, s);
				}
			}
		}
		/* 1942 */for (String s : rewList) {
			/* 1944 */String[] sPart = s.split(":");
			/* 1945 */if (sPart.length == 2) {
				/* 1947 */rewItem = Integer.parseInt(sPart[0]);
				/* 1948 */rewAmount = Integer.parseInt(sPart[1]);
				/* 1949 */player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount) });
				/* 1950 */} else if (sPart.length == 3) {
				/* 1952 */rewItem = Integer.parseInt(sPart[0]);
				/* 1953 */rewAmount = Integer.parseInt(sPart[2]);
				/* 1954 */rewMod = Integer.parseInt(sPart[1]);
				/* 1955 */player.getInventory().addItem(new ItemStack[] { new ItemStack(rewItem, rewAmount, (short) rewMod) });
			}
		}
		/* 1958 */if (!((PlayerInfo) getInstance().getActivePlayers().get(player.getName())).checkChallenge(challenge)) {
			/* 1960 */((PlayerInfo) getInstance().getActivePlayers().get(player.getName())).completeChallenge(challenge);
			/* 1961 */getInstance().writePlayerFile(player.getName(), (PlayerInfo) getInstance().getActivePlayers().get(player.getName()));
		}

		/* 1965 */return true;
	}

	public void reloadData() {
		/* 1969 */if (this.skyblockDataFile == null) {
			/* 1970 */this.skyblockDataFile = new File(getDataFolder(), "skyblockData.yml");
		}
		/* 1972 */this.skyblockData = YamlConfiguration.loadConfiguration(this.skyblockDataFile);

		/* 1975 */InputStream defConfigStream = getResource("skyblockData.yml");
		/* 1976 */if (defConfigStream != null) {
			/* 1977 */YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			/* 1978 */this.skyblockData.setDefaults(defConfig);
		}
	}

	public FileConfiguration getData() {
		/* 1983 */if (this.skyblockData == null) {
			/* 1984 */reloadData();
		}
		/* 1986 */return this.skyblockData;
	}
}