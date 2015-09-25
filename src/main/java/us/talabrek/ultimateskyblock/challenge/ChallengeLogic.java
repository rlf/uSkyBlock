package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The home of challenge business logic.
 */
public class ChallengeLogic {
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(?<id>[0-9]+)(:(?<data>[0-9]+))?:(?<amount>[0-9]+)");
    public static final int MS_MIN = 60*1000;
    public static final int MS_HOUR = 60*MS_MIN;
    public static final long MS_DAY = 24*MS_HOUR;
    public static final int ROWS_OF_RANKS = 6;
    public static final int CHALLENGE_PAGESIZE = ROWS_OF_RANKS * 9;

    private final FileConfiguration config;
    private final uSkyBlock plugin;
    private final Map<String, Rank> ranks;

    public final ChallengeDefaults defaults;

    public ChallengeLogic(FileConfiguration config, uSkyBlock plugin) {
        this.config = config;
        this.plugin = plugin;
        this.defaults = ChallengeFactory.createDefaults(config.getRoot());
        load();
        ranks = ChallengeFactory.createRankMap(config.getConfigurationSection("ranks"), defaults);
    }

    public boolean isEnabled() {
        return config.getBoolean("allowChallenges", true);
    }
    private void load() {
        Arrays.asList(config.getString("ranks", "").split(" "));
    }

    public List<Rank> getRanks() {
        return Collections.unmodifiableList(new ArrayList<>(ranks.values()));
    }

    public List<String> getAvailableChallengeNames(PlayerInfo playerInfo) {
        List<String> list = new ArrayList<>();
        for (Rank rank : ranks.values()) {
            if (rank.isAvailable(playerInfo)) {
                for (Challenge challenge : rank.getChallenges()) {
                    list.add(challenge.getName());
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
        return ranks.get(rank).getChallenges();
    }

    public boolean completeChallenge(final Player player, final String challengeName) {
        final PlayerInfo pi = plugin.getPlayerInfo(player);
        Challenge challenge = getChallenge(challengeName);
        if (challenge == null) {
            player.sendMessage(tr("\u00a74No challenge named {0} found", challengeName));
            return false;
        }
        ChallengeCompletion completion = pi.getChallenge(challengeName);
        if (!challenge.getRank().isAvailable(pi) || !pi.challengeExists(challengeName) || completion.getTimesCompleted() > 0 && (!challenge.isRepeatable() || challenge.getType() == Challenge.Type.ISLAND)) {
            player.sendMessage(tr("\u00a74The {0} challenge is not repeatable!", challengeName));
            return false;
        }
        if (challenge.getType() == Challenge.Type.PLAYER) {
            if (!tryComplete(player, challengeName, "onPlayer")) {
                player.sendMessage(tr("\u00a74{0}", challenge.getDescription()));
                player.sendMessage(tr("\u00a74You don't have enough of the required item(s)!"));
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND) {
            if (!plugin.playerIsOnIsland(player)) {
                player.sendMessage(tr("\u00a74You must be on your island to do that!"));
                return false;
            }
            if (!tryComplete(player, challengeName, "onIsland")) {
                player.sendMessage(tr("\u00a74{0}", challenge.getDescription()));
                player.sendMessage(tr("\u00a74You must be standing within {0} blocks of all required items.", challenge.getRadius()));
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND_LEVEL) {
            if (!tryCompleteIslandLevel(player, challenge)) {
                player.sendMessage(tr("\u00a74Your island must be level {0} to complete this challenge!", challenge.getRequiredLevel()));
            }
            return true;
        }
        return false;
    }

    public Challenge getChallenge(String challengeName) {
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                if (challenge.getName().equalsIgnoreCase(challengeName)) {
                    return challenge;
                }
            }
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
                return amount * inc * timesCompleted; // Oh, my god! Just do the time m8!
            case '/':
                return amount / (inc * timesCompleted); // Yay! Free stuff!!!
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
        return true;
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
        int[] blockCount = new int[0xffffff];
        int[] baseBlocks = new int[0xffff];
        for (int x = px - radius; x <= px + radius; x++) {
            for (int y = py - radius; y <= py + radius; y++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blockCount[(block.getTypeId() << 8) + (block.getData() & 0xff)]++;
                    baseBlocks[block.getTypeId()]++;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean hasAll = true;
        for (ItemStack item : itemStacks) {
            int diffSpecific = item.getAmount() - blockCount[(item.getTypeId() << 8) + (item.getDurability() & 0xff)];
            int diffGeneral = item.getAmount() - baseBlocks[item.getTypeId()];
            if (item.getDurability() != 0 && diffSpecific > 0) {
                sb.append(" \u00a74" + diffSpecific
                        + " \u00a7b" + VaultHandler.getItemName(item));
                hasAll = false;
            } if (diffGeneral > 0) {
                sb.append(" \u00a74" + diffGeneral
                        + " \u00a7b" + VaultHandler.getItemName(item));
                hasAll = false;
            }
        }
        if (!hasAll) {
            player.sendMessage(tr("\u00a7eStill the following blocks short: {0}", sb.toString()));
        }
        return hasAll;
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
                for (Iterator<EntityMatch> it = matchMap.get(entity.getType()).iterator(); it.hasNext();) {
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
            for (Map.Entry<EntityMatch,Integer> entry : countMap.entrySet()) {
                sb.append("\u00a7e - ");
                sb.append(" \u00a74" + entry.getValue() + " \u00a7ex");
                sb.append(" \u00a7b" + entry.getKey() + "\n");
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
            List<ItemStack> requiredItems = challenge.getRequiredItems(completion.getTimesCompletedSinceTimer());
            for (ItemStack required : requiredItems) {
                required.setItemMeta(null);
                if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                    sb.append(" \u00a74" + (required.getAmount() - getCountOf(player.getInventory(), required))
                            + " \u00a7b" + VaultHandler.getItemName(required));
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
        return true;
    }

    private int getCountOf(PlayerInventory inventory, ItemStack required) {
        int count = 0;
        for (ItemStack invItem : inventory.all(required.getType()).values()) {
            if (invItem.getDurability() == required.getDurability()) {
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
        World skyWorld = plugin.getWorld();
        player.sendMessage(tr("\u00a7aYou have completed the {0} challenge!", challengeName));
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
            VaultHandler.depositPlayer(player.getName(), reward.getCurrencyReward() * rewBonus);
        }
        player.giveExp(reward.getXpReward());
        if (defaults.broadcastCompletion && isFirstCompletion) {
            Bukkit.getServer().broadcastMessage(FormatUtil.normalize(config.getString("broadcastText")) + tr("{0} has completed the {1} challenge!", player.getName() , challengeName));
        }
        player.sendMessage(tr("\u00a7eItem reward(s): \u00a7f{0}", reward.getRewardText()));
        player.sendMessage(tr("\u00a7eExp reward: \u00a7f{0,number,#.#}", reward.getXpReward()));
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            player.sendMessage(tr("\u00a7eCurrency reward: \u00a7f{0,number,###.##} {1} \u00a7a ({2,number,##.##})%", reward.getCurrencyReward() * rewBonus, VaultHandler.getEcon().currencyNamePlural(), (rewBonus - 1.0) * 100.0));
        }
        if (reward.getPermissionReward() != null) {
            for (String perm : reward.getPermissionReward().split(" ")) {
                if (!VaultHandler.checkPerm(player, perm, player.getWorld())) {
                    VaultHandler.addPerk(player, perm);
                }
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
            String command = cmd.replaceAll("\\{challenge\\}", challengeName);
            plugin.execCommand(player, command, true);
        }
        playerInfo.completeChallenge(challengeName);
        return true;
    }

    public long getResetInMillis(String challenge) {
        return getChallenge(challenge).getResetInHours() * MS_HOUR;
    }

    public ItemStack getItemStack(PlayerInfo playerInfo, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        ItemStack currentChallengeItem = challenge.getDisplayItem(completion, defaults.enableEconomyPlugin);
        /*
        if (completion.getTimesCompleted() == 0) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 4);
        } else if (!challenge.isRepeatable()) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 13);
        }
        */
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = meta.getLore();
        if (challenge.isRepeatable() || completion.getTimesCompleted() == 0) {
            lores.add(tr("\u00a7e\u00a7lClick to complete this challenge."));
        } else {
            lores.add(tr("\u00a74\u00a7lYou can't repeat this challenge."));
        }
        if (completion.getTimesCompleted() > 0) {
            meta.addEnchant(new EnchantmentWrapper(0), 0, false);
        }
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public static List<String> wordWrap(String s, int firstSegment, int lineSize) {
        List<String> words = new ArrayList<>();
        int ix = firstSegment;
        int jx = 0;
        while (ix < s.length()) {
            ix = s.indexOf(' ', ix);
            if (ix != -1) {
                String subString = s.substring(jx, ix).trim();
                if (!subString.isEmpty()) {
                    words.add(subString);
                }
            } else {
                break;
            }
            jx = ix + 1;
            ix += lineSize;
        }
        words.add(s.substring(jx));
        return words;
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

    public void populateChallengeRank(Inventory menu, Player player, PlayerInfo pi, int page) {
        List<Rank> ranksOnPage = new ArrayList<>(ranks.values());
        // page 1 = 0-4, 2 = 5-8, ...
        if (page > 0) {
            ranksOnPage = getRanksForPage(page, ranksOnPage);
        }
        int location = 0;
        for (Rank rank : ranksOnPage) {
            location = populateChallengeRank(menu, player, rank, location, pi);
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
            int rowsInRank = (int) Math.ceil(rank.getChallenges().size() / 8f);
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
            row += Math.ceil(rank.getChallenges().size() / 8f);
        }
        return row;
    }

    public int populateChallengeRank(Inventory menu, final Player player, final Rank rank, int location, final PlayerInfo playerInfo) {
        List<String> lores = new ArrayList<>();
        ItemStack currentChallengeItem = rank.getDisplayItem();
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        meta4.setDisplayName("\u00a7e\u00a7l" + tr("Rank: {0}",  rank.getName()));
        lores.add(tr("\u00a7fComplete most challenges in"));
        lores.add(tr("\u00a7fthis rank to unlock the next rank."));
        if (location < (CHALLENGE_PAGESIZE/2)) {
            lores.add(tr("\u00a7eClick here to show previous page"));
        } else {
            lores.add(tr("\u00a7eClick here to show next page"));
        }
        meta4.setLore(lores);
        currentChallengeItem.setItemMeta(meta4);
        menu.setItem(location++, currentChallengeItem);
        List<String> missingRequirements = rank.getMissingRequirements(playerInfo);
        for (Challenge challenge : rank.getChallenges()) {
            if ((location % 9) == 0) {
                location++; // Skip rank-row
            }
            lores.clear();
            String challengeName = challenge.getName();
            try {
                currentChallengeItem = getItemStack(playerInfo, challengeName);
                if (!missingRequirements.isEmpty()) {
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName(tr("\u00a74\u00a7lLocked Challenge"));
                    lores.addAll(missingRequirements);
                    meta4.setLore(lores);
                    currentChallengeItem.setItemMeta(meta4);
                }
                menu.setItem(location++, currentChallengeItem);
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
}
