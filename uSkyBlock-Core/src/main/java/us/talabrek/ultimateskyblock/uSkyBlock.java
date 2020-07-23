package us.talabrek.ultimateskyblock;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.animation.AnimationHandler;
import dk.lockfuglsang.minecraft.command.Command;
import dk.lockfuglsang.minecraft.command.CommandManager;
import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import dk.lockfuglsang.minecraft.util.TimeUtil;
import dk.lockfuglsang.minecraft.util.VersionUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.api.IslandLevel;
import us.talabrek.ultimateskyblock.api.IslandRank;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.api.event.EventLogic;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockScoreChangedEvent;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.chat.ChatEvents;
import us.talabrek.ultimateskyblock.chat.ChatLogic;
import us.talabrek.ultimateskyblock.chat.IslandTalkCommand;
import us.talabrek.ultimateskyblock.chat.PartyTalkCommand;
import us.talabrek.ultimateskyblock.command.AdminCommand;
import us.talabrek.ultimateskyblock.command.ChallengeCommand;
import us.talabrek.ultimateskyblock.command.IslandCommand;
import us.talabrek.ultimateskyblock.command.admin.DebugCommand;
import us.talabrek.ultimateskyblock.command.admin.SetMaintenanceCommand;
import us.talabrek.ultimateskyblock.command.island.BiomeCommand;
import us.talabrek.ultimateskyblock.event.ExploitEvents;
import us.talabrek.ultimateskyblock.event.GriefEvents;
import us.talabrek.ultimateskyblock.event.InternalEvents;
import us.talabrek.ultimateskyblock.event.ItemDropEvents;
import us.talabrek.ultimateskyblock.event.MenuEvents;
import us.talabrek.ultimateskyblock.event.NetherTerraFormEvents;
import us.talabrek.ultimateskyblock.event.PlayerEvents;
import us.talabrek.ultimateskyblock.event.SpawnEvents;
import us.talabrek.ultimateskyblock.event.ToolMenuEvents;
import us.talabrek.ultimateskyblock.event.WorldGuardEvents;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.ConfirmHandler;
import us.talabrek.ultimateskyblock.handler.CooldownHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.handler.placeholder.PlaceholderHandler;
import us.talabrek.ultimateskyblock.hook.HookManager;
import us.talabrek.ultimateskyblock.imports.USBImporterExecutor;
import us.talabrek.ultimateskyblock.island.BlockLimitLogic;
import us.talabrek.ultimateskyblock.island.IslandGenerator;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandLocatorLogic;
import us.talabrek.ultimateskyblock.island.IslandLogic;
import us.talabrek.ultimateskyblock.island.LimitLogic;
import us.talabrek.ultimateskyblock.island.OrphanLogic;
import us.talabrek.ultimateskyblock.island.level.ChunkSnapshotLevelLogic;
import us.talabrek.ultimateskyblock.island.level.IslandScore;
import us.talabrek.ultimateskyblock.island.level.LevelLogic;
import us.talabrek.ultimateskyblock.island.task.CreateIslandTask;
import us.talabrek.ultimateskyblock.island.task.RecalculateRunnable;
import us.talabrek.ultimateskyblock.island.task.SetBiomeTask;
import us.talabrek.ultimateskyblock.menu.ConfigMenu;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.IslandPerk;
import us.talabrek.ultimateskyblock.player.PerkLogic;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.player.PlayerLogic;
import us.talabrek.ultimateskyblock.player.PlayerNotifier;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.player.TeleportLogic;
import us.talabrek.ultimateskyblock.signs.SignEvents;
import us.talabrek.ultimateskyblock.signs.SignLogic;
import us.talabrek.ultimateskyblock.util.IslandUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.PlayerUtil;
import us.talabrek.ultimateskyblock.util.ServerUtil;
import us.talabrek.ultimateskyblock.uuid.BukkitPlayerDB;
import us.talabrek.ultimateskyblock.uuid.FilePlayerDB;
import us.talabrek.ultimateskyblock.uuid.MemoryPlayerDB;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;
import us.talabrek.ultimateskyblock.world.WorldManager;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.po.I18nUtil.pre;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.Settings.island_height;
import static us.talabrek.ultimateskyblock.util.LocationUtil.isSafeLocation;
import static us.talabrek.ultimateskyblock.util.LogUtil.log;

public class uSkyBlock extends JavaPlugin implements uSkyBlockAPI, CommandManager.RequirementChecker {
    private static final String CN = uSkyBlock.class.getName();
    private static final String[][] depends = new String[][]{
            new String[]{"Vault", "1.7.0", "optional"},
            new String[]{"WorldEdit", "7.0", "optionalIf", "FastAsyncWorldEdit"},
            new String[]{"WorldGuard", "7.0"},
            new String[]{"FastAsyncWorldEdit", "1.13", "optional"},
            new String[]{"Multiverse-Core", "2.5", "optional"},
            new String[]{"Multiverse-Portals", "2.5", "optional"},
            new String[]{"Multiverse-NetherPortals", "2.5", "optional"},
    };
    private static String missingRequirements = null;
    private static final Random RND = new Random(System.currentTimeMillis());

    private SkyBlockMenu menu;
    private ConfigMenu configMenu;
    private ChallengeLogic challengeLogic;
    private EventLogic eventLogic;
    private LevelLogic levelLogic;
    private IslandLogic islandLogic;
    private OrphanLogic orphanLogic;
    private PerkLogic perkLogic;
    private TeleportLogic teleportLogic;
    private LimitLogic limitLogic;

    /* MANAGERS */
    private HookManager hookManager;
    private MetricsManager metricsManager;
    private WorldManager worldManager;

    private IslandGenerator islandGenerator;
    private PlayerNotifier notifier;

    private USBImporterExecutor importer;

    private static uSkyBlock instance;
    // TODO: 28/06/2016 - R4zorax: These two should probably be moved to the proper classes
    public File directoryPlayers;
    public File directoryIslands;

    private BukkitTask autoRecalculateTask;

    private IslandLocatorLogic islandLocatorLogic;
    private PlayerDB playerDB;
    private ConfirmHandler confirmHandler;
    private AnimationHandler animationHandler;

    private CooldownHandler cooldownHandler;
    private PlayerLogic playerLogic;
    private ChatLogic chatLogic;

    private volatile boolean maintenanceMode = false;
    private BlockLimitLogic blockLimitLogic;

    public uSkyBlock() {
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        try {
            WorldManager.skyBlockWorld = null; // Force a reload on config.
        } catch (Exception e) {
            log(Level.INFO, tr("Something went wrong saving the island and/or party data!"), e);
        }
        PlaceholderHandler.unregister(this);
        if (animationHandler != null) {
            animationHandler.stop();
        }
        challengeLogic.shutdown();
        eventLogic.shutdown();
        playerLogic.shutdown();
        islandLogic.shutdown();
        playerDB.shutdown(); // Must be before playerNameChangeManager!!
        AsyncWorldEditHandler.onDisable(this);
        DebugCommand.disableLogging(null);
    }

    @Override
    public FileConfiguration getConfig() {
        return FileUtil.getYmlConfiguration("config.yml");
    }

    @Override
    public void onEnable() {
        WorldManager.skyBlockWorld = null; // Force a re-import or what-ever...
        WorldManager.skyBlockNetherWorld = null;
        missingRequirements = null;
        instance = this;
        CommandManager.registerRequirements(this);
        FileUtil.setDataFolder(getDataFolder());
        FileUtil.setAllwaysOverwrite("levelConfig.yml");
        I18nUtil.setDataFolder(getDataFolder());

        reloadConfigs();

        getServer().getScheduler().runTaskLater(getInstance(), new Runnable() {
            @Override
            public void run() {
                ServerUtil.init(uSkyBlock.this);
                if (!isRequirementsMet(Bukkit.getConsoleSender(), null)) {
                    return;
                }
                uSkyBlock.this.getHookManager().setupMultiverse();
                uSkyBlock.this.getHookManager().setupEconomyHook();
                uSkyBlock.this.getHookManager().setupPermissionsHook();
                AsyncWorldEditHandler.onEnable(uSkyBlock.this);
                WorldGuardHandler.setupGlobal(getWorldManager().getWorld());
                if (getWorldManager().getNetherWorld() != null) {
                    WorldGuardHandler.setupGlobal(getWorldManager().getNetherWorld());
                }
                registerEventsAndCommands();
                if (!getConfig().getBoolean("importer.name2uuid.imported", false)) {
                    Bukkit.getConsoleSender().sendMessage(tr("Converting data to UUID, this make take a while!"));
                    getImporter().importUSB(Bukkit.getConsoleSender(), "name2uuid");
                }
                log(Level.INFO, getVersionInfo(false));
            }
        }, getConfig().getLong("init.initDelay", 50L));

        metricsManager = new MetricsManager(this);
        PaperLib.suggestPaper(this);
    }

    public synchronized boolean isRequirementsMet(CommandSender sender, Command command, String... args) {
        if (maintenanceMode && !(
                (command instanceof AdminCommand && args != null && args.length > 0 && args[0].equals("maintenance")) ||
                        command instanceof SetMaintenanceCommand)) {
            sender.sendMessage(tr("\u00a7cMAINTENANCE:\u00a7e uSkyBlock is currently in maintenance mode"));
            return false;
        }
        if (missingRequirements == null) {
            PluginManager pluginManager = getServer().getPluginManager();
            missingRequirements = "";
            for (String[] pluginReq : depends) {
                if (pluginReq.length > 2 && pluginReq[2].equals("optional")) {
                    // Do check the version if an optional requirement is present.
                    if (!pluginManager.isPluginEnabled(pluginReq[0])) {
                        continue;
                    }
                }
                if (pluginReq.length > 2 && pluginReq[2].equals("optionalIf")) {
                    if (pluginManager.isPluginEnabled(pluginReq[3])) {
                        continue;
                    }
                }
                if (pluginManager.isPluginEnabled(pluginReq[0])) {
                    PluginDescriptionFile desc = pluginManager.getPlugin(pluginReq[0]).getDescription();
                    if (VersionUtil.getVersion(desc.getVersion()).isLT(pluginReq[1])) {
                        missingRequirements += tr("\u00a7buSkyBlock\u00a7e depends on \u00a79{0}\u00a7e >= \u00a7av{1}\u00a7e but only \u00a7cv{2}\u00a7e was found!\n", pluginReq[0], pluginReq[1], desc.getVersion());
                    }
                } else {
                    missingRequirements += tr("\u00a7buSkyBlock\u00a7e depends on \u00a79{0}\u00a7e >= \u00a7av{1}", pluginReq[0], pluginReq[1]);
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

    public void registerEvents() {
        final PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new InternalEvents(this), this);
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
        if (getConfig().getBoolean("options.protection.visitors.block-banned-entry", true)) {
            manager.registerEvents(new WorldGuardEvents(this), this);
        }
        if (Settings.nether_enabled) {
            manager.registerEvents(new NetherTerraFormEvents(this), this);
        }
        if (getConfig().getBoolean("tool-menu.enabled", true)) {
            manager.registerEvents(new ToolMenuEvents(this), this);
        }
        if (getConfig().getBoolean("signs.enabled", true)) {
            manager.registerEvents(new SignEvents(this, new SignLogic(this)), this);
        }
        PlaceholderHandler.register(this);
        manager.registerEvents(new ChatEvents(chatLogic, this), this);
    }

    public Location getSafeHomeLocation(final PlayerInfo p) {
        Location home = LocationUtil.findNearestSafeLocation(p.getHomeLocation(), null);
        if (home == null) {
            home = LocationUtil.findNearestSafeLocation(p.getIslandLocation(), null);
        }
        return home;
    }

    public Location getSafeWarpLocation(final PlayerInfo p) {
        us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = getIslandInfo(p);
        if (islandInfo != null) {
            Location warp = LocationUtil.findNearestSafeLocation(islandInfo.getWarpLocation(), null);
            if (warp == null) {
                warp = LocationUtil.findNearestSafeLocation(islandInfo.getIslandLocation(), null);
            }
            return warp;
        }
        return null;
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

    public boolean restartPlayerIsland(final Player player, final Location next, final String cSchem) {
        if (!perkLogic.getSchemes(player).contains(cSchem)) {
            player.sendMessage(tr("\u00a7eYou do not have access to that island-schematic!"));
            return false;
        }
        final PlayerInfo playerInfo = getPlayerInfo(player);
        if (playerInfo != null) {
            playerInfo.setIslandGenerating(true);
        }
        if (getWorldManager().isSkyWorld(player.getWorld())) {
            // Clear first, since the player could log out and we NEED to make sure their inventory gets cleared.
            clearPlayerInventory(player);
        }
        islandLogic.clearIsland(next, new Runnable() {
            @Override
            public void run() {
                generateIsland(player, playerInfo, next, cSchem);
            }
        });
        return true;
    }

    public void clearPlayerInventory(Player player) {
        getLogger().entering(CN, "clearPlayerInventory", player);
        PlayerInfo playerInfo = getPlayerInfo(player);
        if (!getWorldManager().isSkyWorld(player.getWorld())) {
            getLogger().finer("not clearing, since player is not in skyworld, marking for clear on next entry");
            if (playerInfo != null) {
                playerInfo.setClearInventoryOnNextEntry(true);
            }
            return;
        }
        if (playerInfo != null) {
            playerInfo.setClearInventoryOnNextEntry(false);
        }
        if (getConfig().getBoolean("options.restart.clearInventory", true)) {
            player.getInventory().clear();
        }
        if (getConfig().getBoolean("options.restart.clearPerms", true)) {
            playerInfo.clearPerms(player);
        }
        if (getConfig().getBoolean("options.restart.clearArmor", true)) {
            ItemStack[] armor = player.getEquipment().getArmorContents();
            player.getEquipment().setArmorContents(new ItemStack[armor.length]);
        }
        if (getConfig().getBoolean("options.restart.clearEnderChest", true)) {
            player.getEnderChest().clear();
        }
        if (getConfig().getBoolean("options.restart.clearCurrency", false)) {
            getHookManager().getEconomyHook().ifPresent((hook) -> hook.withdrawPlayer(player, hook.getBalance(player)));
        }
        getLogger().exiting(CN, "clearPlayerInventory");
    }

    public synchronized boolean devSetPlayerIsland(final Player sender, final Location l, final String player) {
        final PlayerInfo pi = playerLogic.getPlayerInfo(player);

        String islandName = WorldGuardHandler.getIslandNameAt(l);
        Location islandLocation = IslandUtil.getIslandLocation(islandName);
        final Location newLoc = LocationUtil.alignToDistance(islandLocation, Settings.island_distance);
        if (newLoc == null) {
            return false;
        }

        boolean deleteOldIsland = false;
        if (pi.getHasIsland()) {
            Location oldLoc = pi.getIslandLocation();
            if (oldLoc != null
                    && !(newLoc.getBlockX() == oldLoc.getBlockX() && newLoc.getBlockZ() == oldLoc.getBlockZ())) {
                deleteOldIsland = true;
            }
        }

        if (newLoc.equals(pi.getIslandLocation())) {
            sender.sendMessage(tr("\u00a74Player is already assigned to this island!"));
            deleteOldIsland = false;
        }

        // Purge current islandinfo and partymembers if there's an active party at this location (issue #948)
        getIslandLogic().purge(islandName);

        Runnable resetIsland = () -> {
            pi.setHomeLocation(null);
            pi.setIslandLocation(newLoc);
            pi.setHomeLocation(getSafeHomeLocation(pi));
            IslandInfo island = islandLogic.createIslandInfo(pi.locationForParty(), player);
            WorldGuardHandler.updateRegion(island);
            pi.save();
        };
        if (deleteOldIsland) {
            deletePlayerIsland(pi.getPlayerName(), resetIsland);
        } else {
            resetIsland.run();
        }
        return true;
    }

    public boolean playerIsOnIsland(final Player player) {
        return playerIsOnOwnIsland(player)
                || playerIsTrusted(player);
    }

    public boolean playerIsOnOwnIsland(Player player) {
        return locationIsOnIsland(player, player.getLocation())
                || locationIsOnNetherIsland(player, player.getLocation());
    }

    private boolean playerIsTrusted(Player player) {
        String islandName = WorldGuardHandler.getIslandNameAt(player.getLocation());
        if (islandName != null) {
            us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = islandLogic.getIslandInfo(islandName);
            if (islandInfo != null && islandInfo.getTrustees().contains(player.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean locationIsOnNetherIsland(final Player player, final Location loc) {
        if (!getWorldManager().isSkyNether(loc.getWorld())) {
            return false;
        }
        PlayerInfo playerInfo = playerLogic.getPlayerInfo(player);
        if (playerInfo != null && playerInfo.getHasIsland()) {
            Location p = playerInfo.getIslandNetherLocation();
            if (p == null) {
                return false;
            }
            ProtectedRegion region = WorldGuardHandler.getNetherRegionAt(p);
            return region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return false;
    }

    public boolean locationIsOnIsland(final Player player, final Location loc) {
        if (!getWorldManager().isSkyWorld(loc.getWorld())) {
            return false;
        }
        PlayerInfo playerInfo = playerLogic.getPlayerInfo(player);
        if (playerInfo != null && playerInfo.getHasIsland()) {
            Location p = playerInfo.getIslandLocation();
            if (p == null) {
                return false;
            }
            ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(p);
            return region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return false;
    }

    public boolean hasIsland(final Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        return playerInfo != null && playerInfo.getHasIsland();
    }

    public boolean islandAtLocation(final Location loc) {
        return ((WorldGuardHandler.getIntersectingRegions(loc).size() > 0)
                || islandLogic.hasIsland(loc)
        );

    }

    public boolean islandInSpawn(final Location loc) {
        if (loc == null) {
            return true;
        }
        return WorldGuardHandler.isIslandIntersectingSpawn(loc);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return getWorldManager().getDefaultWorldGenerator(worldName, id);
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return playerLogic.getPlayerInfo(player);
    }

    public PlayerInfo getPlayerInfo(UUID uuid) {
        return playerLogic.getPlayerInfo(uuid);
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        return playerLogic.getPlayerInfo(playerName);
    }

    public boolean setBiome(final Location loc, final String bName) {
        Biome biome = getBiome(bName);
        if (biome == null) return false;
        setBiome(loc, biome);
        return true;
    }

    public Biome getBiome(String bName) {
        if (bName == null) return null;
        return BiomeCommand.BIOMES.get(bName.toLowerCase());
    }

    private void setBiome(Location loc, Biome biome) {
        new SetBiomeTask(this, loc, biome, null).runTask(this);
    }

    public void createIsland(final Player player, String cSchem) {
        PlayerInfo pi = getPlayerInfo(player);
        if (pi.isIslandGenerating()) {
            player.sendMessage(tr("\u00a7cYour island is in the process of generating, you cannot create now."));
            return;
        }
        if (!perkLogic.getSchemes(player).contains(cSchem)) {
            player.sendMessage(tr("\u00a7eYou do not have access to that island-schematic!"));
            return;
        }
        if (pi != null) {
            pi.setIslandGenerating(true);
        }
        try {
            Location next = getIslandLocatorLogic().getNextIslandLocation(player);
            if (getWorldManager().isSkyWorld(player.getWorld())) {
                getTeleportLogic().spawnTeleport(player, true);
            }
            generateIsland(player, pi, next, cSchem);
        } catch (Exception ex) {
            player.sendMessage(tr("Could not create your Island. Please contact a server moderator."));
            log(Level.SEVERE, "Error creating island", ex);
        }
        log(Level.INFO, "Finished creating player island.");
    }

    private void generateIsland(final Player player, final PlayerInfo pi, final Location next, final String cSchem) {
        if (!perkLogic.getSchemes(player).contains(cSchem)) {
            player.sendMessage(tr("\u00a7eYou do not have access to that island-schematic!"));
            orphanLogic.addOrphan(next);
            return;
        }
        final PlayerPerk playerPerk = new PlayerPerk(pi, perkLogic.getPerk(player));
        player.sendMessage(tr("\u00a7eGetting your island ready, please be patient, it can take a while."));
        BukkitRunnable createTask = new CreateIslandTask(this, player, playerPerk, next, cSchem);
        IslandInfo tempInfo = islandLogic.createIslandInfo(LocationUtil.getIslandName(next), pi.getPlayerName());
        WorldGuardHandler.protectIsland(this, player, tempInfo);
        islandLogic.clearIsland(next, createTask);
    }

    public IslandInfo setNewPlayerIsland(final PlayerInfo playerInfo, final Location loc) {
        playerInfo.startNewIsland(loc);

        Location chestLocation = LocationUtil.findChestLocation(loc);
        Optional<Location> chestSpawnLocation = LocationUtil.findNearestSpawnLocation(
            chestLocation != null ? chestLocation : loc);

        if (chestSpawnLocation.isPresent()) {
            playerInfo.setHomeLocation(chestSpawnLocation.get());
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
        return info;
    }

    public IslandInfo getIslandInfo(Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        return islandLogic.getIslandInfo(playerInfo);
    }

    @Override
    public IslandInfo getIslandInfo(Location location) {
        return getIslandInfo(WorldGuardHandler.getIslandNameAt(location));
    }

    @Override
    public boolean isGTE(String versionNumber) {
        return VersionUtil.getVersion(getDescription().getVersion()).isGTE(versionNumber);
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

    public ConfigMenu getConfigMenu() {
        return configMenu;
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

    public IslandLocatorLogic getIslandLocatorLogic() {
        return islandLocatorLogic;
    }

    @Override
    public void reloadConfig() {
        reloadConfigs();
        registerEventsAndCommands();
    }

    private void reloadConfigs() {
        createFolders();
        HandlerList.unregisterAll(this);
        hookManager = new HookManager(this);
        if (challengeLogic != null) {
            challengeLogic.shutdown();
        }
        if (playerLogic != null) {
            playerLogic.shutdown();
        }
        if (islandLogic != null) {
            islandLogic.shutdown();
        }
        PlaceholderHandler.unregister(this);
        if (Settings.loadPluginConfig(getConfig())) {
            saveConfig();
        }
        I18nUtil.clearCache();
        // Update all of the loaded configs.
        FileUtil.reload();

        String playerDbStorage = getConfig().getString("options.advanced.playerdb.storage", "yml");
        if (playerDbStorage.equalsIgnoreCase("yml")) {
            playerDB = new FilePlayerDB(this);
        } else if (playerDbStorage.equalsIgnoreCase("memory")) {
            playerDB = new MemoryPlayerDB(getConfig());
        } else {
            playerDB = new BukkitPlayerDB();
        }

        getServer().getPluginManager().registerEvents(playerDB, this);
        worldManager = new WorldManager(this);
        eventLogic = new EventLogic(this);
        teleportLogic = new TeleportLogic(this);
        PlayerUtil.loadConfig(playerDB, getConfig());
        islandGenerator = new IslandGenerator(getDataFolder(), getConfig());
        perkLogic = new PerkLogic(this, islandGenerator);
        challengeLogic = new ChallengeLogic(FileUtil.getYmlConfiguration("challenges.yml"), this);
        menu = new SkyBlockMenu(this, challengeLogic);
        configMenu = new ConfigMenu(this);
        YmlConfiguration levelConfig = FileUtil.getYmlConfiguration("levelConfig.yml");
        // Disabled until AWE/FAWE supports 1.13
        //levelLogic = AsyncWorldEditHandler.isAWE() ? new AweLevelLogic(this, levelConfig) : new ChunkSnapshotLevelLogic(this, levelConfig);
        levelLogic = new ChunkSnapshotLevelLogic(this, levelConfig);
        orphanLogic = new OrphanLogic(this);
        islandLocatorLogic = new IslandLocatorLogic(this);
        islandLogic = new IslandLogic(this, directoryIslands, orphanLogic);
        limitLogic = new LimitLogic(this);
        blockLimitLogic = new BlockLimitLogic(this);
        notifier = new PlayerNotifier(getConfig());
        playerLogic = new PlayerLogic(this);
        if (autoRecalculateTask != null) {
            autoRecalculateTask.cancel();
        }
        chatLogic = new ChatLogic(this);
    }

    public void registerEventsAndCommands() {
        if (!isRequirementsMet(Bukkit.getConsoleSender(), null)) {
            return;
        }
        registerEvents();
        int refreshEveryMinute = getConfig().getInt("options.island.autoRefreshScore", 0);
        if (refreshEveryMinute > 0) {
            int refreshTicks = refreshEveryMinute * 1200; // Ticks per minute
            autoRecalculateTask = new RecalculateRunnable(this).runTaskTimer(this, refreshTicks, refreshTicks);
        } else {
            autoRecalculateTask = null;
        }
        confirmHandler = new ConfirmHandler(this, getConfig().getInt("options.advanced.confirmTimeout", 10));
        cooldownHandler = new CooldownHandler(this);
        animationHandler = new AnimationHandler(this);
        getCommand("island").setExecutor(new IslandCommand(this, menu));
        getCommand("challenges").setExecutor(new ChallengeCommand(this));
        getCommand("usb").setExecutor(new AdminCommand(this, confirmHandler, animationHandler));
        getCommand("islandtalk").setExecutor(new IslandTalkCommand(this, chatLogic));
        getCommand("partytalk").setExecutor(new PartyTalkCommand(this, chatLogic));
    }

    public IslandLogic getIslandLogic() {
        return islandLogic;
    }

    public OrphanLogic getOrphanLogic() {
        return orphanLogic;
    }

    public BlockLimitLogic getBlockLimitLogic() {
        return blockLimitLogic;
    }

    /**
     * @param player    The player executing the command
     * @param command   The command to execute
     * @param onlyInSky Whether the command is restricted to a sky-associated world.
     */
    public void execCommand(Player player, String command, boolean onlyInSky) {
        if (command == null || player == null) {
            return;
        }
        if (onlyInSky && !getWorldManager().isSkyAssociatedWorld(player.getWorld())) {
            return;
        }
        command = command
                .replaceAll("\\{player\\}", Matcher.quoteReplacement(player.getName()))
                .replaceAll("\\{playerName\\}", Matcher.quoteReplacement(player.getDisplayName()))
                .replaceAll("\\{playername\\}", Matcher.quoteReplacement(player.getDisplayName()))
                .replaceAll("\\{position\\}", Matcher.quoteReplacement(LocationUtil.asString(player.getLocation()))); // Figure out what this should be
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
            IslandInfo islandInfo = getIslandInfo(playerInfo);
            for (String member : islandInfo.getMembers()) {
                doExecCommand(player, command.replaceAll("\\{party\\}", Matcher.quoteReplacement(member)), delay);
            }
        } else {
            doExecCommand(player, command, delay);
        }
    }

    private void doExecCommand(final Player player, final String command, int delay) {
        if (delay == 0) {
            sync(new Runnable() {
                @Override
                public void run() {
                    doExecCommand(player, command);
                }
            });
        } else if (delay > 0) {
            sync(new Runnable() {
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

    public USBImporterExecutor getImporter() {
        if (importer == null) {
            importer = new USBImporterExecutor(this);
        }
        return importer;
    }

    public boolean playerIsInSpawn(Player player) {
        Location pLoc = player.getLocation();
        if (!getWorldManager().isSkyWorld(pLoc.getWorld())) {
            return false;
        }
        Location spawnCenter = new Location(WorldManager.skyBlockWorld, 0, pLoc.getBlockY(), 0);
        return spawnCenter.distance(pLoc) <= Settings.general_spawnSize;
    }

    /**
     * Notify the player, but max. every X seconds.
     */
    public void notifyPlayer(Player player, String msg) {
        notifier.notifyPlayer(player, msg);
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
            us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = getIslandInfo(info);
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

    @Override
    public IslandRank getIslandRank(Location location) {
        String islandNameAt = WorldGuardHandler.getIslandNameAt(location);
        if (islandNameAt != null && islandLogic != null) {
            return islandLogic.getRank(islandNameAt);
        }
        return null;
    }

    public void fireChangeEvent(CommandSender sender, uSkyBlockEvent.Cause cause) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        final uSkyBlockEvent event = new uSkyBlockEvent(player, this, cause);
        fireAsyncEvent(event);
    }

    public void fireAsyncEvent(final Event event) {
        getServer().getScheduler().runTaskAsynchronously(this,
                () -> getServer().getPluginManager().callEvent(event)
        );
    }

    public String getVersionInfo(boolean checkEnabled) {
        PluginDescriptionFile description = getDescription();
        String msg = pre("\u00a77Name: \u00a7b{0}\n", description.getName());
        msg += pre("\u00a77Version: \u00a7b{0}\n", description.getVersion());
        msg += pre("\u00a77Description: \u00a7b{0}\n", description.getDescription());
        msg += pre("\u00a77Language: \u00a7b{0} ({1})\n", getConfig().get("language", "en"), I18nUtil.getI18n().getLocale());
        msg += pre("\u00a79  State: d={0}, r={1}, i={2}, p={3}, n={4}, awe={5}\n", Settings.island_distance, Settings.island_radius,
                islandLogic.getSize(), playerLogic.getSize(),
                Settings.nether_enabled, AsyncWorldEditHandler.isAWE());
        msg += pre("\u00a77Server: \u00a7e{0} {1}\n", getServer().getName(), getServer().getVersion());
        msg += pre("\u00a79  State: online={0}, bungee={1}\n", ServerUtil.isOnlineMode(),
                ServerUtil.isBungeeEnabled());
        msg += pre("\u00a77------------------------------\n");
        for (String[] dep : depends) {
            Plugin dependency = getServer().getPluginManager().getPlugin(dep[0]);
            if (dependency != null) {
                String status = pre("N/A");
                if (checkEnabled) {
                    if (dependency.isEnabled()) {
                        if (VersionUtil.getVersion(dependency.getDescription().getVersion()).isLT(dep[1])) {
                            status = pre("\u00a7eWRONG-VERSION");
                        } else {
                            status = pre("\u00a72ENABLED");
                        }
                    } else {
                        status = pre("\u00a74DISABLED");
                    }
                }
                msg += pre("\u00a77\u00a7d{0} \u00a7f{1} \u00a77({2}\u00a77)\n", dependency.getName(),
                        dependency.getDescription().getVersion(), status);
            }
        }
        msg += pre("\u00a77------------------------------\n");
        return msg;
    }

    public PlayerDB getPlayerDB() {
        return playerDB;
    }

    private IslandScore adjustScore(IslandScore score, IslandInfo islandInfo) {
        IslandPerk islandPerk = perkLogic.getIslandPerk(islandInfo.getSchematicName());
        double blockScore = score.getScore();
        blockScore = blockScore * islandPerk.getScoreMultiply() * islandInfo.getScoreMultiplier() + islandPerk.getScoreOffset() + islandInfo.getScoreOffset();
        return new IslandScore(blockScore, score.getTop());
    }

    public void calculateScoreAsync(final Player player, String islandName, final Callback<us.talabrek.ultimateskyblock.api.model.IslandScore> callback) {
        final IslandInfo islandInfo = getIslandInfo(islandName);
        getLevelLogic().calculateScoreAsync(islandInfo.getIslandLocation(), new Callback<IslandScore>() {
            @Override
            public void run() {
                IslandScore score = adjustScore(getState(), islandInfo);
                callback.setState(score);
                islandInfo.setLevel(score.getScore());
                getIslandLogic().updateRank(islandInfo, score);
                fireAsyncEvent(new uSkyBlockScoreChangedEvent(player, getInstance(), score, islandInfo.getIslandLocation()));
                callback.run();
            }
        });
    }

    public ConfirmHandler getConfirmHandler() {
        return confirmHandler;
    }

    public CooldownHandler getCooldownHandler() {
        return cooldownHandler;
    }

    public EventLogic getEventLogic() {
        return eventLogic;
    }

    public PlayerLogic getPlayerLogic() {
        return playerLogic;
    }

    public TeleportLogic getTeleportLogic() {
        return teleportLogic;
    }

    public LimitLogic getLimitLogic() {
        return limitLogic;
    }

    public IslandGenerator getIslandGenerator() {
        return islandGenerator;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    /**
     * CAUTION! If anyone calls this with true, they MUST ensure it is later called with false,
     * or the plugin will effectively be in a locked state.
     *
     * @param maintenanceMode whether or not to enable maintenance-mode.
     */
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
        if (maintenanceMode) {
            if (playerLogic != null) {
                playerLogic.flushCache();
            }
            if (islandLogic != null) {
                islandLogic.flushCache();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!isRequirementsMet(sender, null, args)) {
            sender.sendMessage(tr("\u00a7cCommand is currently disabled!"));
        }
        return true;
    }

    public BukkitTask async(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
    }

    public BukkitTask async(Runnable runnable, long delayMs) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable,
                TimeUtil.millisAsTicks(delayMs));
    }

    public BukkitTask async(Runnable runnable, long delay, long every) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable,
                TimeUtil.millisAsTicks(delay),
                TimeUtil.millisAsTicks(every));
    }

    public BukkitTask sync(Runnable runnable) {
        return Bukkit.getScheduler().runTask(this, runnable);
    }

    public BukkitTask sync(Runnable runnable, long delayMs) {
        return Bukkit.getScheduler().runTaskLater(this, runnable,
                TimeUtil.millisAsTicks(delayMs));
    }

    public BukkitTask sync(Runnable runnable, long delay, long every) {
        return Bukkit.getScheduler().runTaskTimer(this, runnable,
                TimeUtil.millisAsTicks(delay),
                TimeUtil.millisAsTicks(every));
    }

    public void execCommands(Player player, List<String> cmdList) {
        for (String cmd : cmdList) {
            execCommand(player, cmd, false);
        }
    }
}
