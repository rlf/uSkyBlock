package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import dk.lockfuglsang.minecraft.util.FormatUtil;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.api.event.MemberJoinedEvent;
import us.talabrek.ultimateskyblock.block.BlockCollection;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;

/**
 * The home of challenge business logic.
 */
public class ChallengeLogic implements Listener {
    public static final long MS_MIN = 60 * 1000;
    public static final long MS_HOUR = 60 * MS_MIN;
    public static final long MS_DAY = 24 * MS_HOUR;
    public static final int COLS_PER_ROW = 9;
    public static final int ROWS_OF_RANKS = 5;
    public static final int CHALLENGE_PAGESIZE = ROWS_OF_RANKS * COLS_PER_ROW;

    private final FileConfiguration config;
    private final uSkyBlock plugin;
    private final Map<String, Rank> ranks;

    public final ChallengeDefaults defaults;
    public final ChallengeCompletionLogic completionLogic;
    private final ItemStack lockedItem;
    private final Map<Challenge.Type, ItemStack> lockedItemMap = new HashMap<>();

    public ChallengeLogic(FileConfiguration config, uSkyBlock plugin) {
        this.config = config;
        this.plugin = plugin;
        this.defaults = ChallengeFactory.createDefaults(config.getRoot());
        ranks = ChallengeFactory.createRankMap(config.getConfigurationSection("ranks"), defaults);
        completionLogic = new ChallengeCompletionLogic(plugin, config);
        String displayItemForLocked = config.getString("lockedDisplayItem", null);
        if (displayItemForLocked != null) {
            lockedItem = ItemStackUtil.createItemStack(displayItemForLocked);
        } else {
            lockedItem = null;
        }
        for (Challenge.Type type : Challenge.Type.values()) {
            String itemName = config.getString(type.name() + ".lockedDisplayItem", null);
            if (itemName != null) {
                lockedItemMap.put(type, ItemStackUtil.createItemStack(itemName));
            } else {
                lockedItemMap.put(type, lockedItem);
            }
        }
        if (completionLogic.isIslandSharing()) {
            Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    public boolean isEnabled() {
        return config.getBoolean("allowChallenges", true);
    }

    public List<Rank> getRanks() {
        return Collections.unmodifiableList(new ArrayList<>(ranks.values()));
    }

    public List<String> getAvailableChallengeNames(PlayerInfo playerInfo) {
        List<String> list = new ArrayList<>();
        if (playerInfo == null || !playerInfo.getHasIsland()) {
            return list;
        }
        for (Rank rank : ranks.values()) {
            if (rank.isAvailable(playerInfo)) {
                for (Challenge challenge : rank.getChallenges()) {
                    if (challenge.getMissingRequirements(playerInfo).isEmpty()) {
                        list.add(challenge.getName());
                    }
                }
            } else {
                break;
            }
        }
        return list;
    }

    public List<String> getAllChallengeNames() {
        List<String> list = new ArrayList<>();
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                list.add(challenge.getName());
            }
        }
        return list;
    }

    public List<Challenge> getChallengesForRank(String rank) {
        return ranks.containsKey(rank) ? ranks.get(rank).getChallenges() : Collections.<Challenge>emptyList();
    }

    public void completeChallenge(final Player player, String challengeName) {
        final PlayerInfo pi = plugin.getPlayerInfo(player);
        Challenge challenge = getChallenge(challengeName);
        if (challenge == null) {
            player.sendMessage(tr("\u00a74No challenge named {0} found", challengeName));
            return;
        }
        if (!plugin.playerIsOnOwnIsland(player)) {
            player.sendMessage(tr("\u00a74You must be on your island to do that!"));
            return;
        }
        if (!challenge.getRank().isAvailable(pi)) {
            player.sendMessage(tr("\u00a74The {0} challenge is not available yet!", challenge.getDisplayName()));
            return;
        }
        challengeName = challenge.getName();
        ChallengeCompletion completion = pi.getChallenge(challengeName);
        if (completion.getTimesCompleted() > 0 && (!challenge.isRepeatable() || challenge.getType() == Challenge.Type.ISLAND)) {
            player.sendMessage(tr("\u00a74The {0} challenge is not repeatable!", challenge.getDisplayName()));
            return;
        }
        if (completion.isOnCooldown() && completion.getTimesCompletedInCooldown() >= challenge.getRepeatLimit() && challenge.getRepeatLimit() > 0) {
            player.sendMessage(tr("\u00a74You cannot complete the {0} challenge again yet!", challenge.getDisplayName()));
            return;
        }
        player.sendMessage(tr("\u00a7eTrying to complete challenge \u00a7a{0}", challenge.getDisplayName()));
        if (challenge.getType() == Challenge.Type.PLAYER) {
            tryComplete(player, challengeName, "onPlayer");
        } else if (challenge.getType() == Challenge.Type.ISLAND) {
            if (!tryComplete(player, challengeName, "onIsland")) {
                player.sendMessage(tr("\u00a74{0}", challenge.getDescription()));
                player.sendMessage(tr("\u00a74You must be standing within {0} blocks of all required items.", challenge.getRadius()));
            }
        } else if (challenge.getType() == Challenge.Type.ISLAND_LEVEL) {
            if (!tryCompleteIslandLevel(player, challenge)) {
                player.sendMessage(tr("\u00a74Your island must be level {0} to complete this challenge!", challenge.getRequiredLevel()));
            }
        }
    }

    public Challenge getChallenge(String challengeName) {
        List<Challenge> partialMatch = new ArrayList<>();
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                if (challenge.getName().equalsIgnoreCase(challengeName)) {
                    return challenge;
                } else if (stripFormatting(challenge.getDisplayName()).equalsIgnoreCase(challengeName)) {
                    return challenge;
                } else if (challenge.getName().startsWith(challengeName)) {
                    partialMatch.add(challenge);
                }
            }
        }
        if (partialMatch.size() == 1) {
            return partialMatch.get(0);
        }
        return null;
    }

    public static int calcAmount(int amount, char op, int inc, int timesCompleted) {
        switch (op) {
            case '+':
                return amount + inc * timesCompleted;
            case '-':
                return amount - inc * timesCompleted; // Why?
            case '*':
            case '^':
                return amount * (int) Math.pow(inc, timesCompleted); // Oh, my god! Just do the time m8!
            case '/':
                return amount / (int) Math.pow(inc, timesCompleted); // Yay! Free stuff!!!
        }
        return amount;
    }

    public boolean tryComplete(final Player player, final String challenge, final String type) {
        if (type.equalsIgnoreCase("onPlayer")) {
            return tryCompleteOnPlayer(player, challenge);
        } else if (type.equalsIgnoreCase("onIsland")) {
            return tryCompleteOnIsland(player, challenge);
        } else {
            player.sendMessage(tr("\u00a74Unknown type of challenge: {0}", type));
        }
        return false;
    }

    private boolean tryCompleteIslandLevel(Player player, Challenge challenge) {
        if (plugin.getIslandInfo(player).getLevel() >= challenge.getRequiredLevel()) {
            giveReward(player, challenge.getName());
            return true;
        }
        return false;
    }

    private boolean islandContains(Player player, List<ItemStack> itemStacks, int radius) {
        final Location l = player.getLocation();
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        World world = l.getWorld();
        BlockCollection blockCollection = new BlockCollection();
        for (int x = px - radius; x <= px + radius; x++) {
            for (int y = py - radius; y <= py + radius; y++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blockCollection.add(block);
                }
            }
        }
        String diff = blockCollection.diff(itemStacks);
        if (diff != null) {
            player.sendMessage(diff);
            return false;
        }
        return true;
    }

    private boolean tryCompleteOnIsland(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        List<ItemStack> requiredItems = challenge.getRequiredItems(0);
        int radius = challenge.getRadius();
        if (islandContains(player, requiredItems, radius) && hasEntitiesNear(player, challenge.getRequiredEntities(), radius)) {
            giveReward(player, challengeName);
            return true;
        }
        return false;
    }

    private boolean hasEntitiesNear(Player player, List<EntityMatch> requiredEntities, int radius) {
        Map<EntityMatch, Integer> countMap = new LinkedHashMap<>();
        Map<EntityType, Set<EntityMatch>> matchMap = new HashMap<>();
        for (EntityMatch match : requiredEntities) {
            countMap.put(match, match.getCount());
            Set<EntityMatch> set = matchMap.get(match.getType());
            if (set == null) {
                set = new HashSet<>();
            }
            set.add(match);
            matchMap.put(match.getType(), set);
        }
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (matchMap.containsKey(entity.getType())) {
                for (Iterator<EntityMatch> it = matchMap.get(entity.getType()).iterator(); it.hasNext(); ) {
                    EntityMatch match = it.next();
                    if (match.matches(entity)) {
                        int newCount = countMap.get(match) - 1;
                        if (newCount <= 0) {
                            countMap.remove(match);
                            it.remove();
                        } else {
                            countMap.put(match, newCount);
                        }
                    }
                }
            }
        }
        if (!countMap.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<EntityMatch, Integer> entry : countMap.entrySet()) {
                sb.append("\u00a7e - ");
                sb.append(" \u00a74" + entry.getValue() + " \u00a7ex");
                sb.append(" \u00a7b" + entry.getKey().getDisplayName() + "\n");
            }
            player.sendMessage(tr("\u00a7eStill the following entities short:\n{0}", sb.toString()).split("\n"));
        }
        return countMap.isEmpty();
    }

    private boolean tryCompleteOnPlayer(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        if (challenge != null && completion != null) {
            StringBuilder sb = new StringBuilder();
            boolean hasAll = true;
            List<ItemStack> requiredItems = challenge.getRequiredItems(completion.getTimesCompletedInCooldown());
            for (ItemStack required : requiredItems) {
                String name = VaultHandler.getItemName(required);
                if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                    sb.append(tr(" \u00a74{0} \u00a7b{1}", (required.getAmount() - getCountOf(player.getInventory(), required)), name));
                    hasAll = false;
                }
            }
            if (hasAll) {
                if (challenge.isTakeItems()) {
                    player.getInventory().removeItem(requiredItems.toArray(new ItemStack[requiredItems.size()]));
                }
                giveReward(player, challenge);
                return true;
            } else {
                player.sendMessage(tr("\u00a7eYou are the following items short:{0}", sb.toString()));
            }
        }
        return false;
    }

    public int getCountOf(Inventory inventory, ItemStack required) {
        int count = 0;
        for (ItemStack invItem : inventory.all(required.getType()).values()) {
            if (invItem.getDurability() == required.getDurability() && NBTUtil.getNBTTag(invItem).equals(NBTUtil.getNBTTag(required))) {
                count += invItem.getAmount();
            }
        }
        return count;
    }

    public boolean giveReward(final Player player, final String challengeName) {
        return giveReward(player, getChallenge(challengeName));
    }

    private boolean giveReward(Player player, Challenge challenge) {
        String challengeName = challenge.getName();
        player.sendMessage(tr("\u00a7aYou have completed the {0} challenge!", challenge.getDisplayName()));
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        Reward reward;
        boolean isFirstCompletion = playerInfo.checkChallenge(challengeName) == 0;
        if (isFirstCompletion) {
            reward = challenge.getReward();
        } else {
            reward = challenge.getRepeatReward();
        }
        float rewBonus = 1;
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            Perk perk = plugin.getPerkLogic().getPerk(player);
            rewBonus += perk.getRewBonus();
            VaultHandler.depositPlayer(player, reward.getCurrencyReward() * rewBonus);
        }
        player.giveExp(reward.getXpReward());
        boolean wasBroadcast = false;
        if (defaults.broadcastCompletion && isFirstCompletion) {
            Bukkit.getServer().broadcastMessage(FormatUtil.normalize(config.getString("broadcastText")) + tr("\u00a79{0}\u00a7f has completed the \u00a79{1}\u00a7f challenge!", player.getName(), challenge.getDisplayName()));
            wasBroadcast = true;
        }
        player.sendMessage(tr("\u00a7eItem reward(s): \u00a7f{0}", reward.getRewardText()));
        player.sendMessage(tr("\u00a7eExp reward: \u00a7f{0,number,#.#}", reward.getXpReward()));
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            player.sendMessage(tr("\u00a7eCurrency reward: \u00a7f{0,number,###.##} {1} \u00a7a ({2,number,##.##})%", reward.getCurrencyReward() * rewBonus, VaultHandler.getEcon().currencyNamePlural(), (rewBonus - 1.0) * 100.0));
        }
        if (reward.getPermissionReward() != null) {
            List<String> perms = Arrays.asList(reward.getPermissionReward().trim().split(" "));
            if (isIslandSharing()) {
                // Give all members
                IslandInfo islandInfo = playerInfo.getIslandInfo();
                for (UUID memberUUID : islandInfo.getMemberUUIDs()) {
                    if (memberUUID == null) {
                        continue;
                    }
                    PlayerInfo pi = plugin.getPlayerInfo(memberUUID);
                    if (pi != null) {
                        pi.addPermissions(perms);
                    }
                }
            } else {
                // Give only player
                playerInfo.addPermissions(perms);
            }
        }
        HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(reward.getItemReward().toArray(new ItemStack[0]));
        for (ItemStack item : leftOvers.values()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }
        if (!leftOvers.isEmpty()) {
            player.sendMessage(tr("\u00a7eYour inventory is \u00a74full\u00a7e. Items dropped on the ground."));
        }
        for (String cmd : reward.getCommands()) {
            String command = cmd.replaceAll("\\{challenge\\}", Matcher.quoteReplacement(challenge.getName()));
            command = command.replaceAll("\\{challengeName\\}", Matcher.quoteReplacement(challenge.getDisplayName()));
            plugin.execCommand(player, command, true);
        }
        playerInfo.completeChallenge(challengeName, wasBroadcast);
        return true;
    }

    public long getResetInMillis(String challenge) {
        return getChallenge(challenge).getResetInHours() * MS_HOUR;
    }

    public ItemStack getItemStack(PlayerInfo playerInfo, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        ItemStack currentChallengeItem = challenge.getDisplayItem(completion, defaults.enableEconomyPlugin);
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = meta.getLore();
        if (challenge.isRepeatable() || completion.getTimesCompleted() == 0) {
            lores.add(tr("\u00a7e\u00a7lClick to complete this challenge."));
        } else {
            lores.add(tr("\u00a74\u00a7lYou can't repeat this challenge."));
        }
        if (completion.getTimesCompleted() > 0) {
            meta.addEnchant(Enchantment.LOYALTY, 0, true);
        }
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public void populateChallenges(Map<String, ChallengeCompletion> challengeMap) {
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                String key = challenge.getName().toLowerCase();
                if (!challengeMap.containsKey(key)) {
                    challengeMap.put(key, new ChallengeCompletion(key, 0L, 0, 0));
                }
            }
        }
    }

    public void populateChallengeRank(Inventory menu, PlayerInfo pi, int page, boolean isAdminAccess) {
        List<Rank> ranksOnPage = new ArrayList<>(ranks.values());
        // page 1 = 0-4, 2 = 5-8, ...
        if (page > 0) {
            ranksOnPage = getRanksForPage(page, ranksOnPage);
        }
        int location = 0;
        for (Rank rank : ranksOnPage) {
            location = populateChallengeRank(menu, rank, location, pi, isAdminAccess);
            if ((location % 9) != 0) {
                location += (9 - (location % 9)); // Skip the rest of that line
            }
            if (location >= CHALLENGE_PAGESIZE) {
                break;
            }
        }
    }

    private List<Rank> getRanksForPage(int page, List<Rank> ranksOnPage) {
        int rowsToSkip = (page - 1) * ROWS_OF_RANKS;
        for (Iterator<Rank> it = ranksOnPage.iterator(); it.hasNext(); ) {
            Rank rank = it.next();
            int rowsInRank = getRows(rank);
            if (rowsToSkip <= 0 || ((rowsToSkip - rowsInRank) < 0)) {
                return ranksOnPage;
            }
            rowsToSkip -= rowsInRank;
            it.remove();
        }
        return ranksOnPage;
    }

    private int calculateRows(List<Rank> ranksOnPage) {
        int row = 0;
        for (Rank rank : ranksOnPage) {
            row += getRows(rank);
        }
        return row;
    }

    private int getRows(Rank rank) {
        int rankSize = 0;
        for (Challenge challenge : rank.getChallenges()) {
            rankSize += challenge.getOffset() + 1;
        }
        return (int) Math.ceil(rankSize / 8f);
    }

    public int populateChallengeRank(Inventory menu, final Rank rank, int location, final PlayerInfo playerInfo, boolean isAdminAccess) {
        List<String> lores = new ArrayList<>();
        ItemStack currentChallengeItem = rank.getDisplayItem();
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        meta4.setDisplayName("\u00a7e\u00a7l" + tr("Rank: {0}", rank.getName()));
        lores.add(tr("\u00a7fComplete most challenges in"));
        lores.add(tr("\u00a7fthis rank to unlock the next rank."));
        if (location < (CHALLENGE_PAGESIZE / 2)) {
            lores.add(tr("\u00a7eClick here to show previous page"));
        } else {
            lores.add(tr("\u00a7eClick here to show next page"));
        }
        meta4.setLore(lores);
        currentChallengeItem.setItemMeta(meta4);
        menu.setItem(location, currentChallengeItem);
        List<String> missingRankRequirements = rank.getMissingRequirements(playerInfo);
        for (Challenge challenge : rank.getChallenges()) {
            if (challenge.getOffset() == -1 && !currentChallengeItem.getItemMeta().hasEnchants()) {
                continue; // skip
            }
            location += challenge.getOffset() + 1;
            if ((location % 9) == 0) {
                location++; // Skip rank-row
            }
            if (location >= CHALLENGE_PAGESIZE) {
                break;
            }
            lores.clear();
            String challengeName = challenge.getName();
            try {
                currentChallengeItem = getItemStack(playerInfo, challengeName);
                List<String> missingReqs = challenge.getMissingRequirements(playerInfo);
                if (!missingRankRequirements.isEmpty() || !missingReqs.isEmpty()) {
                    if (!isAdminAccess) {
                        ItemStack locked = challenge.getLockedDisplayItem();
                        if (locked == null) {
                            locked = lockedItemMap.get(challenge.getType());
                        }
                        if (locked != null) {
                            currentChallengeItem.setType(locked.getType());
                            currentChallengeItem.setDurability(locked.getDurability());
                        } else if (lockedItem != null) {
                            currentChallengeItem.setType(lockedItem.getType());
                            currentChallengeItem.setDurability(lockedItem.getDurability());
                        }
                    } else {
                        lores = currentChallengeItem.getItemMeta().getLore();
                    }
                    meta4 = currentChallengeItem.getItemMeta();
                    if (defaults.showLockedChallengeName) {
                        lores.add(meta4.getDisplayName());
                    }
                    meta4.setDisplayName(tr("\u00a74\u00a7lLocked Challenge"));
                    lores.addAll(missingReqs);
                    lores.addAll(missingRankRequirements);
                    meta4.setLore(lores);
                    currentChallengeItem.setItemMeta(meta4);
                }
                menu.setItem(location, currentChallengeItem);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Invalid challenge " + challenge, e);
            }
        }
        return location;
    }

    public boolean isResetOnCreate() {
        return config.getBoolean("resetChallengesOnCreate", true);
    }

    public int getTotalPages() {
        int totalRows = calculateRows(getRanks());
        return (int) Math.ceil(1f * totalRows / ROWS_OF_RANKS);
    }

    public Collection<ChallengeCompletion> getChallenges(PlayerInfo playerInfo) {
        return completionLogic.getChallenges(playerInfo).values();
    }

    public void completeChallenge(PlayerInfo playerInfo, String challengeName) {
        completionLogic.completeChallenge(playerInfo, challengeName);
    }

    public void resetChallenge(PlayerInfo playerInfo, String challenge) {
        completionLogic.resetChallenge(playerInfo, challenge);
    }

    public int checkChallenge(PlayerInfo playerInfo, String challenge) {
        return completionLogic.checkChallenge(playerInfo, challenge);
    }

    public ChallengeCompletion getChallenge(PlayerInfo playerInfo, String challenge) {
        return completionLogic.getChallenge(playerInfo, challenge);
    }

    public ChallengeCompletion getIslandCompletion(String islandName, String challengeName) {
        Map<String, ChallengeCompletion> challenges = completionLogic.getIslandChallenges(islandName);
        if (challenges.containsKey(challengeName)) {
            return challenges.get(challengeName);
        } else {
            ChallengeCompletion chal = null;
            for (String k : challenges.keySet()) {
                if (k.startsWith(challengeName)) {
                    if (chal != null) {
                        return null;
                    }
                    chal = challenges.get(k);
                }
            }
            return chal;
        }
    }

    public void resetAllChallenges(PlayerInfo playerInfo) {
        completionLogic.resetAllChallenges(playerInfo);
    }

    public void shutdown() {
        completionLogic.shutdown();
    }

    public long flushCache() {
        return completionLogic.flushCache();
    }

    public boolean isIslandSharing() {
        return completionLogic.isIslandSharing();
    }

    @EventHandler
    public void onMemberJoinedEvent(MemberJoinedEvent e) {
        if (!completionLogic.isIslandSharing() || !(e.getPlayerInfo() instanceof PlayerInfo)) {
            return;
        }
        PlayerInfo playerInfo = (PlayerInfo) e.getPlayerInfo();
        Map<String, ChallengeCompletion> completions = completionLogic.getIslandChallenges(e.getIslandInfo().getName());
        List<String> permissions = new ArrayList<>();
        for (Map.Entry<String, ChallengeCompletion> entry : completions.entrySet()) {
            if (entry.getValue().getTimesCompleted() > 0) {
                Challenge challenge = getChallenge(entry.getKey());
                if (challenge.getReward().getPermissionReward() != null) {
                    permissions.addAll(Arrays.asList(challenge.getReward().getPermissionReward().split(" ")));
                }
            }
        }
        if (!permissions.isEmpty()) {
            playerInfo.addPermissions(permissions);
        }
    }
}
