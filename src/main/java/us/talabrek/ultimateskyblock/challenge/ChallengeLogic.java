package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The home of challenge business logic.
 */
public class ChallengeLogic {
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(?<id>[0-9]+)(:(?<data>[0-9]+))?:(?<amount>[0-9]+)");
    // TODO: 09/12/2014 - R4zorax: This is not thread-safe...
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".#");
    public static final int MS_MIN = 60*1000;
    public static final int MS_HOUR = 60*MS_MIN;
    public static final long MS_DAY = 24*MS_HOUR;

    private final FileConfiguration config;
    private final uSkyBlock skyBlock;
    private final Map<String, Challenge> challengeData;
    private final List<String> ranks;

    public final ChallengeDefaults defaults;

    public ChallengeLogic(FileConfiguration config, uSkyBlock skyBlock) {
        this.config = config;
        this.skyBlock = skyBlock;
        this.defaults = ChallengeFactory.createDefaults(config.getRoot());
        ranks = Arrays.asList(config.getString("ranks", "").split(" "));
        this.challengeData = ChallengeFactory.createChallengeMap(config.getConfigurationSection("challengeList"), defaults);
    }

    public List<String> getRanks() {
        return Collections.unmodifiableList(ranks);
    }

    public List<String> getAvailableChallengeNames(Player player) {
        List<String> list = new ArrayList<>();
        for (String rank : ranks) {
            if (isRankAvailable(player, rank)) {
                for (Challenge challenge : getChallengesForRank(rank)) {
                    list.add(challenge.getName());
                }
            } else {
                break;
            }
        }
        return list;
    }
    public List<Challenge> getChallengesForRank(String rank) {
        List<Challenge> challenges = new ArrayList<>();
        for (Challenge challenge : challengeData.values()) {
            if (challenge.getRank().equalsIgnoreCase(rank)) {
                challenges.add(challenge);
            }
        }
        return challenges;
    }

    public int checkRankCompletion(final Player player, final String rank) {
        if (!defaults.requiresPreviousRank) {
            return 0;
        }
        int completedInRank = 0;
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        List<Challenge> challengesInRank = getChallengesForRank(rank);
        for (Challenge challenge : challengesInRank) {
            if (pi.checkChallenge(challenge.getName()) > 0) {
                ++completedInRank;
            }
        }
        return challengesInRank.size() - defaults.rankLeeway - completedInRank;
    }

    public boolean isRankAvailable(final Player player, final String rank) {
        if (ranks.size() < 2) {
            return true;
        }
        for (int i = 0; i < ranks.size(); i++) {
            if (ranks.get(i).equalsIgnoreCase(rank)) {
                if (i == 0) {
                    return true;
                }
                if (checkRankCompletion(player, ranks.get(i-1)) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean completeChallenge(final Player player, final String challengeName) {
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = pi.getChallenge(challengeName);
        if (!this.isRankAvailable(player, challenge.getRank())) {
            player.sendMessage(ChatColor.RED + "You have not unlocked this challenge yet!");
            return false;
        }
        if (!pi.challengeExists(challengeName)) {
            player.sendMessage(ChatColor.RED + "Unknown challenge name (check spelling)!");
            return false;
        }
        if (completion.getTimesCompleted() > 0 && (!challenge.isRepeatable() || challenge.getType() == Challenge.Type.ISLAND)) {
            player.sendMessage(ChatColor.RED + "The " + challengeName + " challenge is not repeatable!");
            return false;
        }
        if (challenge.getType() == Challenge.Type.PLAYER) {
            if (!tryComplete(player, challengeName, "onPlayer")) {
                player.sendMessage(ChatColor.RED + challenge.getDescription());
                player.sendMessage(ChatColor.RED + "You don't have enough of the required item(s)!");
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND) {
            if (!skyBlock.playerIsOnIsland(player)) {
                player.sendMessage(ChatColor.RED + "You must be on your island to do that!");
                return false;
            }
            if (!tryComplete(player, challengeName, "onIsland")) {
                player.sendMessage(ChatColor.RED + challenge.getDescription());
                player.sendMessage(ChatColor.RED + "You must be standing within " + challenge.getRadius() + " blocks of all required items.");
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND_LEVEL) {
            if (skyBlock.getIslandConfig(player).getInt("general.level") >= challenge.getRequiredLevel()) {
                return true;
            }
            player.sendMessage(ChatColor.RED + "Your island must be level " + challenge.getRequiredLevel() + " to complete this challenge!");
            return false;
        }
        return false;
    }

    public Challenge getChallenge(String challenge) {
        return challengeData.get(challenge.toLowerCase());
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
        }
        if (type.equalsIgnoreCase("onIsland")) {
            return tryCompleteOnIsland(player, challenge);
        }
        return true;
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
                    blockCount[block.getTypeId() << 8 + block.getData()]++;
                    baseBlocks[block.getTypeId()]++;
                }
            }
        }
        for (ItemStack item : itemStacks) {
            if (item.getDurability() != 0 && blockCount[item.getTypeId() << 8 + item.getDurability()] < item.getAmount()) {
                return false;
            } else if (baseBlocks[item.getTypeId()] < item.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private boolean tryCompleteOnIsland(Player player, String challenge) {
        String reqItems = config.getString("challengeList." + challenge + ".requiredItems");
        if (reqItems == null) {
            return false;
        }
        List<ItemStack> items = new ArrayList<>();
        for (String reqItem : reqItems.split(" ")) {
            Matcher m = ITEM_AMOUNT_PATTERN.matcher(reqItem);
            if (m.matches()) {
                int id = Integer.parseInt(m.group("id"));
                int data = m.group("data") != null ? Integer.parseInt(m.group("data")) : 0;
                int amount = Integer.parseInt(m.group("amount"));
                items.add(new ItemStack(id, amount, (short) data));
            } else {
                uSkyBlock.log(Level.WARNING, "Invalid item found for challenge " + challenge + ", " + reqItem);
            }
        }
        int radius = config.getInt("challengeList." + challenge + ".radius", 10);
        if (islandContains(player, items, radius)) {
            giveReward(player, challenge);
            return true;
        }
        return false;
    }

    private boolean tryCompleteOnPlayer(Player player, String challenge) {
        String reqItems = config.getString("challengeList." + challenge + ".requiredItems");
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
        boolean takeItems = config.getBoolean("challengeList." + challenge + ".takeItems");
        int timesCompleted = playerInfo.checkChallengeSinceTimer(challenge);
        if (reqItems != null) {
            List<ItemStack> items = new ArrayList<>();
            Pattern reqPattern = Pattern.compile("(?<type>[0-9]+)(:(?<subtype>[0-9]+))?:(?<amount>[0-9]+)(;(?<op>[+\\-*])(?<inc>[0-9]+))?");
            for (String item : reqItems.split(" ")) {
                Matcher m = reqPattern.matcher(item);
                if (m.matches()) {
                    int reqItem = Integer.parseInt(m.group("type"));
                    int subType = m.group("subtype") != null ? Integer.parseInt(m.group("subtype")) : 0;
                    int amount = Integer.parseInt(m.group("amount"));
                    char op = m.group("op") != null ? m.group("op").charAt(0) : 0;
                    int inc = m.group("inc") != null ? Integer.parseInt(m.group("inc")) : 0;
                    amount = calcAmount(amount, op, inc, timesCompleted);
                    ItemStack mat = new ItemStack(reqItem, amount, (short) subType);
                    items.add(mat);
                    if (!player.getInventory().containsAtLeast(mat, amount)) {
                        return false;
                    }
                }
            }
            if (takeItems) {
                player.getInventory().removeItem(items.toArray(new ItemStack[items.size()]));
            }
            giveReward(player, challenge);
        }
        return true;
    }

    public boolean giveReward(final Player player, final String challengeName) {
        World skyWorld = skyBlock.getWorld();
        Challenge challenge = getChallenge(challengeName);
        player.sendMessage(ChatColor.GREEN + "You have completed the " + challengeName + " challengeName!");
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
        Reward reward = null;
        boolean isFirstCompletion = playerInfo.checkChallenge(challengeName) == 0;
        if (isFirstCompletion) {
            reward = challenge.getReward();
        } else {
            reward = challenge.getRepeatReward();
        }
        float rewBonus = 1;
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            // TODO: 10/12/2014 - R4zorax: Move this to some config file
            if (VaultHandler.checkPerk(player.getName(), "group.memberplus", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.all", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.25", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.50", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.75", skyWorld)) {
                rewBonus += 0.1;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.100", skyWorld)) {
                rewBonus += 0.2;
            }
            VaultHandler.depositPlayer(player.getName(), reward.getCurrencyReward() * rewBonus);
        }
        player.giveExp(reward.getXpReward());
        if (defaults.broadcastCompletion && isFirstCompletion) {
            Bukkit.getServer().broadcastMessage(config.getString("broadcastText") + player.getName() + " has completed the " + challengeName + " challengeName!");
        }
        player.sendMessage(ChatColor.YELLOW + "Item reward(s): " + ChatColor.WHITE + reward.getRewardText());
        player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + reward.getXpReward());
        player.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + this.DECIMAL_FORMAT.format(reward.getCurrencyReward()*rewBonus) + " " + VaultHandler.getEcon().currencyNamePlural() + "\u00a7a (+" + this.DECIMAL_FORMAT.format((rewBonus - 1.0) * 100.0) + "%)");
        if (isFirstCompletion && reward.getPermissionReward() != null) {
            for (String perm : reward.getPermissionReward().split(" ")) {
                if (!VaultHandler.checkPerm(player, perm, player.getWorld())) {
                    VaultHandler.addPerk(player, perm);
                }
            }
        }
        player.getInventory().addItem(reward.getItemReward().toArray(new ItemStack[0]));
        playerInfo.completeChallenge(challengeName);
        return true;
    }

    public long getResetInMillis(String challenge) {
        return getChallenge(challenge).getResetInHours() * MS_HOUR;
    }

    public List<String> getItemLore(PlayerInfo player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = player.getChallenge(challengeName);
        List<String> lores = new ArrayList<>();
        lores.add("\u00a77" + challenge.getDescription());
        lores.add("\u00a7eThis challenge requires the following:");
        int timesCompleted = completion.getTimesCompletedSinceTimer();
        for (ItemStack item : challenge.getRequiredItems(timesCompleted)) {
            lores.add(item.getItemMeta().getDisplayName());
        }
        return lores;
    }

    public ItemStack getItemStack(PlayerInfo playerInfo, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        ItemStack currentChallengeItem = challenge.getDisplayItem(completion, defaults.enableEconomyPlugin);
        if (completion.getTimesCompleted() == 0) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 4);
        } else if (!challenge.isRepeatable()) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 13);
        }
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = meta.getLore();
        if (challenge.isRepeatable() || completion.getTimesCompleted() == 0) {
            lores.add("\u00a7e\u00a7lClick to complete this challenge.");
        } else {
            lores.add("\u00a74\u00a7lYou can't repeat this challenge.");
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
    public void populateChallenges(HashMap<String, ChallengeCompletion> challengeMap) {
        for (Challenge challenge : challengeData.values()) {
            String key = challenge.getName().toLowerCase();
            if (!challengeMap.containsKey(key)) {
                challengeMap.put(key, new ChallengeCompletion(key, 0L, 0, 0));
            }
        }
    }

    public void populateChallengeRank(Inventory menu, final Player player, final int rankIndex, final Material mat, int location, final PlayerInfo playerInfo) {
        List<String> lores = new ArrayList<>();
        int rankComplete = 0;
        ItemStack currentChallengeItem = new ItemStack(mat, 1);
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        String currentRank = ranks.get(rankIndex);
        meta4.setDisplayName("\u00a7e\u00a7lRank: " + currentRank);
        lores.add("\u00a7fComplete most challenges in");
        lores.add("\u00a7fthis rank to unlock the next rank.");
        meta4.setLore(lores);
        currentChallengeItem.setItemMeta(meta4);
        menu.setItem(location, currentChallengeItem);
        lores.clear();
        for (Challenge challenge : getChallengesForRank(currentRank)) {
            String challengeName = challenge.getName();
            try {
                if (rankIndex > 0) {
                    String previousRank = ranks.get(rankIndex - 1);
                    rankComplete = checkRankCompletion(player, previousRank);
                    if (rankComplete > 0) {
                        currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                        meta4 = currentChallengeItem.getItemMeta();
                        meta4.setDisplayName("\u00a74\u00a7lLocked Challenge");
                        lores.add("\u00a77Complete " + rankComplete + " more " + previousRank + " challenges");
                        lores.add("\u00a77to unlock this rank.");
                        meta4.setLore(lores);
                        currentChallengeItem.setItemMeta(meta4);
                        menu.setItem(++location, currentChallengeItem);
                        lores.clear();
                        continue;
                    }
                }
                currentChallengeItem = getItemStack(playerInfo, challengeName);
                menu.setItem(++location, currentChallengeItem);
                lores.clear();
            } catch (Exception e) {
                skyBlock.getLogger().log(Level.SEVERE, "Invalid challenge " + challenge, e);
            }
        }
    }
}
