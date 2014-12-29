package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
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
    private final Map<String, Rank> ranks;

    public final ChallengeDefaults defaults;

    public ChallengeLogic(FileConfiguration config, uSkyBlock skyBlock) {
        this.config = config;
        this.skyBlock = skyBlock;
        this.defaults = ChallengeFactory.createDefaults(config.getRoot());
        load();
        ranks = ChallengeFactory.createRankMap(config.getConfigurationSection("ranks"), defaults);
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
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = pi.getChallenge(challengeName);
        if (!challenge.getRank().isAvailable(pi)) {
            player.sendMessage("\u00a74You have not unlocked this challenge yet!");
            return false;
        }
        if (!pi.challengeExists(challengeName)) {
            player.sendMessage("\u00a74Unknown challenge name (check spelling)!");
            return false;
        }
        if (completion.getTimesCompleted() > 0 && (!challenge.isRepeatable() || challenge.getType() == Challenge.Type.ISLAND)) {
            player.sendMessage("\u00a74The " + challengeName + " challenge is not repeatable!");
            return false;
        }
        if (challenge.getType() == Challenge.Type.PLAYER) {
            if (!tryComplete(player, challengeName, "onPlayer")) {
                player.sendMessage("\u00a74" + challenge.getDescription());
                player.sendMessage("\u00a74You don't have enough of the required item(s)!");
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND) {
            if (!skyBlock.playerIsOnIsland(player)) {
                player.sendMessage("\u00a74You must be on your island to do that!");
                return false;
            }
            if (!tryComplete(player, challengeName, "onIsland")) {
                player.sendMessage("\u00a74" + challenge.getDescription());
                player.sendMessage("\u00a74You must be standing within " + challenge.getRadius() + " blocks of all required items.");
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND_LEVEL) {
            if (!tryCompleteIslandLevel(player, challenge)) {
                player.sendMessage("\u00a74Your island must be level " + challenge.getRequiredLevel() + " to complete this challenge!");
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
            player.sendMessage("\u00a74Unknown type of challenge: " + type);
        }
        return true;
    }

    private boolean tryCompleteIslandLevel(Player player, Challenge challenge) {
        if (skyBlock.getIslandInfo(player).getLevel() >= challenge.getRequiredLevel()) {
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
            player.sendMessage("\u00a7eStill the following blocks short:" + sb.toString());
        }
        return hasAll;
    }

    private boolean tryCompleteOnIsland(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        List<ItemStack> requiredItems = challenge.getRequiredItems(0);
        int radius = challenge.getRadius();
        if (islandContains(player, requiredItems, radius)) {
            giveReward(player, challengeName);
            return true;
        }
        return false;
    }

    private boolean tryCompleteOnPlayer(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
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
                player.sendMessage("\u00a7eYou are the following items short:" + sb.toString());
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
        World skyWorld = skyBlock.getWorld();
        player.sendMessage(ChatColor.GREEN + "You have completed the " + challengeName + " challenge!");
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
        Reward reward;
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
            Bukkit.getServer().broadcastMessage(config.getString("broadcastText") + player.getName() + " has completed the " + challengeName + " challenge!");
        }
        player.sendMessage("\u00a7eItem reward(s): " + ChatColor.WHITE + reward.getRewardText());
        player.sendMessage("\u00a7eExp reward: " + ChatColor.WHITE + reward.getXpReward());
        player.sendMessage("\u00a7eCurrency reward: " + ChatColor.WHITE + this.DECIMAL_FORMAT.format(reward.getCurrencyReward() * rewBonus) + " " + VaultHandler.getEcon().currencyNamePlural() + "\u00a7a (+" + this.DECIMAL_FORMAT.format((rewBonus - 1.0) * 100.0) + "%)");
        if (reward.getPermissionReward() != null) {
            for (String perm : reward.getPermissionReward().split(" ")) {
                if (!VaultHandler.checkPerm(player, perm, player.getWorld())) {
                    VaultHandler.addPerk(player, perm);
                }
            }
        }
        for (String cmd : reward.getCommands()) {
            String command = cmd.replaceAll("\\{challenge\\}", challengeName);
            skyBlock.execCommand(player, command);
        }
        player.getInventory().addItem(reward.getItemReward().toArray(new ItemStack[0]));
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
            ranksOnPage = ranksOnPage.subList(((page-1)*4), Math.min(page*4, ranksOnPage.size()));
        }
        int location = 0;
        for (Rank rank : ranksOnPage) {
            populateChallengeRank(menu, player, rank, location, pi);
            location += 9;
        }
    }

    public void populateChallengeRank(Inventory menu, final Player player, final Rank rank, int location, final PlayerInfo playerInfo) {
        List<String> lores = new ArrayList<>();
        ItemStack currentChallengeItem = rank.getDisplayItem();
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        meta4.setDisplayName("\u00a7e\u00a7lRank: " + rank.getName());
        lores.add("\u00a7fComplete most challenges in");
        lores.add("\u00a7fthis rank to unlock the next rank.");
        meta4.setLore(lores);
        currentChallengeItem.setItemMeta(meta4);
        menu.setItem(location, currentChallengeItem);
        List<String> missingRequirements = rank.getMissingRequirements(playerInfo);
        for (Challenge challenge : rank.getChallenges()) {
            lores.clear();
            String challengeName = challenge.getName();
            try {
                if (!missingRequirements.isEmpty()) {
                    currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName("\u00a74\u00a7lLocked Challenge");
                    lores.addAll(missingRequirements);
                    meta4.setLore(lores);
                    currentChallengeItem.setItemMeta(meta4);
                    menu.setItem(++location, currentChallengeItem);
                } else {
                    currentChallengeItem = getItemStack(playerInfo, challengeName);
                    menu.setItem(++location, currentChallengeItem);
                }
            } catch (Exception e) {
                skyBlock.getLogger().log(Level.SEVERE, "Invalid challenge " + challenge, e);
            }
        }
    }
}
