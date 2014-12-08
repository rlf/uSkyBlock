package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.menu.Menu;
import us.talabrek.ultimateskyblock.menu.MenuBuilder;
import us.talabrek.ultimateskyblock.menu.MenuItem;
import us.talabrek.ultimateskyblock.menu.MenuItemBuilder;
import us.talabrek.ultimateskyblock.provider.MenuProvider;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The menu-interactions that are handled by the plugin, instead of the menu-structure
 */
public class uSkyBlockMenuProvider implements MenuProvider {
    private final uSkyBlock skyBlock;
    private MenuProvider parentProvider;

    public uSkyBlockMenuProvider(uSkyBlock skyBlock) {
        this.skyBlock = skyBlock;
    }

    @Override
    public Menu getMenu(Player player, String menuName) {
        // TODO: 07/12/2014 - R4zorax: Support multiple pages
        if ("\u00a79Challenge Menu".equals(menuName)) {
            return generateChallengeMenu(player);
        } else if ("\u00a79Island Group Members".equals(menuName)) {
            return generateGroupMenu(player);
        } else if (menuName.endsWith(" \u00a7r<Permissions>")) {
            return generatePermissionMenu(player, menuName);
        }
        return null;
    }

    private Menu generatePermissionMenu(Player player, String menuName) {
        String playerName = menuName.substring(0, menuName.length()-16);
        playerName = uSkyBlock.stripFormatting(playerName);
        MenuBuilder menuBuilder = new MenuBuilder();
        menuBuilder.title("\u00a79" + playerName + " \u00a7r<Permissions>");
        menuBuilder.size(9);
        MenuItemBuilder mib = new MenuItemBuilder();
        mib.icon("397:3");
        mib.title("\u00a7hPlayer Permissions");
        mib.lore("\u00a7eClick here to return to");
        mib.lore("\u00a7eyour island group's info.");
        mib.subMenu("\u00a79Island Group Members");
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("SIGN");
        mib.title(playerName + "'s Permissions");
        mib.lore("\u00a7eHover over an icon to view");
        mib.lore("\u00a7ea permission. Change the");
        mib.lore("\u00a7epermission by clicking it.");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("6:3");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canChangeBiome")) {
            mib.title("\u00a7aChange Biome");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f change the");
            mib.lore("\u00a7fisland's biome. Click here");
            mib.lore("\u00a7fto remove this permission.");
        } else {
            mib.title("\u00a7cChange Biome");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f change the");
            mib.lore("\u00a7fisland's biome. Click here");
            mib.lore("\u00a7fto grant this permission.");
        }
        mib.command("island perm " + playerName + " canChangeBiome");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("101");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canToggleLock")) {
            mib.title("\u00a7aToggle Island Lock");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f toggle the");
            mib.lore("\u00a7fisland's lock, which prevents");
            mib.lore("\u00a7fnon-group members from entering.");
            mib.lore("\u00a7fClick here to remove this permission.");
        } else {
            mib.title("\u00a7cToggle Island Lock");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f toggle the");
            mib.lore("\u00a7fisland's lock, which prevents");
            mib.lore("\u00a7fnon-group members from entering.");
            mib.lore("\u00a7fClick here to add this permission");
        }
        mib.command("island perm " + playerName + " canToggleLock");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("90");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canChangeWarp")) {
            mib.title("\u00a7aSet Island Warp");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f set the");
            mib.lore("\u00a7fisland's warp, which allows");
            mib.lore("\u00a7fnon-group members to teleport");
            mib.lore("\u00a7fto the island. Click here to");
            mib.lore("\u00a7fremove this permission.");
        } else {
            mib.title("\u00a7cSet Island Warp");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f set the");
            mib.lore("\u00a7fisland's warp, which allows");
            mib.lore("\u00a7fnon-group members to teleport");
            mib.lore("\u00a7fto the island. Click here to");
            mib.lore("\u00a7fadd this permission.");
        }
        mib.command("island perm " + playerName + " canChangeWarp");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("69");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canToggleWarp")) {
            mib.title("\u00a7aToggle Island Warp");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f toggle the");
            mib.lore("\u00a7fisland's warp, allowing them");
            mib.lore("\u00a7fto turn it on or off at anytime.");
            mib.lore("\u00a7fbut not set the location. Click");
            mib.lore("\u00a7fhere to remove this permission.");
        } else {
            mib.title("\u00a7cToggle Island Warp");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f toggle the");
            mib.lore("\u00a7fisland's warp, allowing them");
            mib.lore("\u00a7fto turn it on or off at anytime,");
            mib.lore("\u00a7fbut not set the location. Click");
            mib.lore("\u00a7fhere to add this permission.");
        }
        mib.command("island perm " + playerName + " canToggleWarp");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("398");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canInviteOthers")) {
            mib.title("\u00a7aInvite Players");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f invite");
            mib.lore("\u00a7fother players to the island if");
            mib.lore("\u00a7fthere is enough room for more");
            mib.lore("\u00a7fmembers. Click here to remove");
            mib.lore("\u00a7fthis permission.");
        } else {
            mib.title("\u00a7cInvite Players");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f invite");
            mib.lore("\u00a7fother players to the island.");
            mib.lore("\u00a7fClick here to add this permission.");
        }
        mib.command("island perm " + playerName + " canInviteOthers");
        mib.subMenu(menuName);
        menuBuilder.item(mib.build());

        mib = new MenuItemBuilder();
        mib.icon("301");
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + playerName + ".canKickOthers")) {
            mib.title("\u00a7aKick Players");
            mib.lore("\u00a7fThis player \u00a7acan\u00a7f kick");
            mib.lore("\u00a7fother players from the island,");
            mib.lore("\u00a7fbut they are unable to kick");
            mib.lore("\u00a7fthe island leader. Click here");
            mib.lore("\u00a7fto remove this permission.");
        } else {
            mib.title("\u00a7cKick Players");
            mib.lore("\u00a7fThis player \u00a7ccannot\u00a7f kick");
            mib.lore("\u00a7fother players from the island.");
            mib.lore("\u00a7fClick here to add this permission.");
        }
        mib.command("island perm " + playerName + " canKickOthers");
        mib.subMenu(menuName);

        menuBuilder.item(mib.build());
        return menuBuilder.build();
    }

    private Menu generateGroupMenu(Player player) {
        MenuBuilder menuBuilder = new MenuBuilder();
        menuBuilder.size(18);
        menuBuilder.title("\u00a79Island Group Members");
        final Set<String> memberList = skyBlock.getIslandConfig(player).getConfigurationSection("party.members").getKeys(false);
        MenuItemBuilder mib = new MenuItemBuilder();
        mib.title("\u00a7aGroup Info")
                .icon("SIGN")
                .lore("Group Members: \u00a72$party.currentSize\u00a77/\u00a7e$party.maxSize");
        if (skyBlock.getIslandConfig(player).getInt("party.currentSize") < skyBlock.getIslandConfig(player).getInt("party.maxSize")) {
            mib.lore("\u00a7aMore players can be invited to this island.");
        } else {
            mib.lore("\u00a7cThis island is full.");
        }
        mib.lore("\u00a7eHover over a player's icon to");
        mib.lore("\u00a7eview their permissions. The");
        mib.lore("\u00a7eleader can change permissions");
        mib.lore("\u00a7eby clicking a player's icon.");
        menuBuilder.item(mib.build());
        for (String temp : memberList) {
            mib = new MenuItemBuilder();
            if (temp.equalsIgnoreCase(skyBlock.getIslandConfig(player).getString("party.leader"))) {
                mib.title("\u00a7f" + temp);
                mib.lore("\u00a7a\u00a7lLeader");
                mib.lore("\u00a7aCan \u00a7fchange the island's biome.");
                mib.lore("\u00a7aCan \u00a7flock/unlock the island.");
                mib.lore("\u00a7aCan \u00a7fset the island's warp.");
                mib.lore("\u00a7aCan \u00a7ftoggle the island's warp.");
                mib.lore("\u00a7aCan \u00a7finvite others to the island.");
                mib.lore("\u00a7aCan \u00a7fkick others from the island.");
            } else {
                mib.title("\u00a7f" + temp);
                mib.lore("\u00a7e\u00a7lMember");
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canChangeBiome")) {
                    mib.lore("\u00a7aCan \u00a7fchange the island's biome.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7fchange the island's biome.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canToggleLock")) {
                    mib.lore("\u00a7aCan \u00a7flock/unlock the island.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7flock/unlock the island.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canChangeWarp")) {
                    mib.lore("\u00a7aCan \u00a7fset the island's warp.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7fset the island's warp.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canToggleWarp")) {
                    mib.lore("\u00a7aCan \u00a7ftoggle the island's warp.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7ftoggle the island's warp.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canInviteOthers")) {
                    mib.lore("\u00a7aCan \u00a7finvite others to the island.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7finvite others to the island.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canKickOthers")) {
                    mib.lore("\u00a7aCan \u00a7fkick others from the island.");
                } else {
                    mib.lore("\u00a7cCannot \u00a7fkick others from the island.");
                }
                if (player.getName().equalsIgnoreCase(skyBlock.getIslandConfig(player).getString("party.leader"))) {
                    mib.lore("\u00a7e<Click to change this player's permissions>");
                }
            }
            mib.lore("\u00a75" + temp);
            mib.subMenu("\u00a79" + temp + " \u00a7r<Permissions>");
            menuBuilder.item(mib.build());
        }
        return menuBuilder.build();
    }

    @Override
    public boolean onClick(Player player, Menu menu, MenuItem clickedItem) {
        return false;
    }

    @Override
    public void setParent(MenuProvider parentProvider) {
        this.parentProvider = parentProvider;
    }

    private Menu generateChallengeMenu(Player player) {
        MenuBuilder mb = new MenuBuilder();
        mb.size(36).title("\u00a79Challenge Menu");
        // TODO: 07/12/2014 - R4zorax: This should really be organized better
        // TODO: 07/12/2014 - R4zorax: Support pagination etc. here
        Menu rankMenu = parentProvider.getMenu(player, "__RANKS__");
        String previousRank = null;
        for (String rank : Settings.challenges_ranks) {
            MenuItem item = rankMenu.findItem(null, rank);
            mb.item(item);
            for (MenuItem menuItem : getMenuItemsForRank(player, rank, previousRank, item.getIndex())) {
                mb.item(menuItem);
            }
            previousRank = rank;
        }
        return mb.build();
    }

    // TODO: 07/12/2014 - R4zorax: Much of this business logic should be moved to a Challenge package/model
    private List<MenuItem> getMenuItemsForRank(Player player, String currentRank, String previousRank, int index) {
        List<MenuItem> items = new ArrayList<>();
        final String[] challengeList = skyBlock.getChallengesFromRank(player, currentRank).split(" - ");
        FileConfiguration config = skyBlock.getConfig();
        for (int i = 0; i < challengeList.length; ++i) {
            String challenge = skyBlock.correctFormatting(challengeList[i]);
            String challengeName = skyBlock.stripFormatting(challenge).toLowerCase();
            MenuItemBuilder mib = new MenuItemBuilder();
            mib.index(index + 1 + i);
            try {
                if (previousRank != null) {
                    int missingChallenges = skyBlock.checkRankCompletion(player, previousRank);
                    if (missingChallenges > 0) {
                        // TODO: 07/12/2014 - R4zorax: Get these from the config
                        mib.icon("STAINED_GLASS_PANE:14");
                        mib.title("\u00a74\u00a7lLocked Challenge");
                        mib.lore("\u00a77Complete " + missingChallenges + " more " + previousRank + " challenges");
                        mib.lore("\u00a77to unlock this rank.");
                        items.add(mib.build());
                        continue;
                    }
                }
                if (isRepeatableChallenge(challenge)) {
                    if (!config.contains("options.challenges.challengeList." + challengeName + ".displayItem")) {
                        mib.icon("STAINED_GLASS_PANE:5");
                    } else {
                        mib.icon(config.getString("options.challenges.challengeList." + challengeName + ".displayItem"));
                    }
                } else if (isCompletedChallenge(challenge)) {
                    mib.icon("STAINED_GLASS_PANE:13");
                } else {
                    mib.icon("STAINED_GLASS_PANE:4");
                }
                mib.title(challenge);
                mib.lore("\u00a77" + config.getString("options.challenges.challengeList." + challengeName + ".description"));
                mib.lore("\u00a7eThis challenge requires the following:");
                mib.lore(getChallengeDescription(player, challengeName, config));
                PlayerInfo pi = skyBlock.getPlayerInfo(player);
                if (pi.checkChallenge(challengeName) > 0 && config.getBoolean("options.challenges.challengeList." + challengeName + ".repeatable")) {
                    if (pi.onChallengeCooldown(challengeName)) {
                        if (pi.getChallengeCooldownTime(challengeName) / 86400000L >= 1L) {
                            final int days = (int) pi.getChallengeCooldownTime(challengeName) / 86400000;
                            mib.lore("\u00a74Requirements will reset in " + days + " days.");
                        } else {
                            final int hours = (int) pi.getChallengeCooldownTime(challengeName) / 3600000;
                            mib.lore("\u00a74Requirements will reset in " + hours + " hours.");
                        }
                    }
                    mib.lore("\u00a76Item Reward: \u00a7a" + config.getString("options.challenges.challengeList." + challengeName + ".repeatRewardText"));
                    mib.lore("\u00a76Currency Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".repeatCurrencyReward"));
                    mib.lore("\u00a76Exp Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".repeatXpReward"));
                    mib.lore("\u00a7dTotal times completed: \u00a7f" + pi.getChallenge(challengeName).getTimesCompleted());
                    mib.lore("\u00a7e\u00a7lClick to complete this challenge.");
                } else {
                    mib.lore("\u00a76Item Reward: \u00a7a" + config.getString("options.challenges.challengeList." + challengeName + ".rewardText"));
                    mib.lore("\u00a76Currency Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".currencyReward"));
                    mib.lore("\u00a76Exp Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".xpReward"));
                    if (config.getBoolean("options.challenges.challengeList." + challengeName + ".repeatable")) {
                        mib.lore("\u00a7e\u00a7lClick to complete this challenge.");
                    } else {
                        mib.lore("\u00a74\u00a7lYou can't repeat this challenge.");
                    }
                }
                mib.command("c c " + challengeName);
                mib.subMenu("§9Challenge Menu");
                items.add(mib.build());
            } catch (NullPointerException e) {
                skyBlock.getLogger().log(Level.SEVERE, "Mis-configured challenge " + challenge, e);
            }
        }
        return items;
    }

    private int calcAmount(int amount, String op, int inc, int times) {
        switch (op) {
            case "+":
                return amount + inc * times;
            case "*":
                return amount * inc * times; // This would perhaps make sense, if a float was used...
            case "-":
                return amount - inc * times; // This doesn't seem to make sense at all!
            case "/":
                return amount / (inc * times); // Neither does this for that matter...
        }
        return amount;
    }

    private List<String> getChallengeDescription(Player player, String challengeName, FileConfiguration config) {
        List<String> challengeLines = new ArrayList<>();
        final String[] reqList = config.getString("options.challenges.challengeList." + challengeName + ".requiredItems").split(" ");
        //                                    item[:mod]:amount[;(+|-|*|/)increment]
        Pattern reqPattern = Pattern.compile("(?<mat>[0-9]+(:([0-9]+))?):(?<amount>[0-9]+)(;(?<op>[+*\\-/])(?<inc>[0-9]+))?");
        for (String s : reqList) {
            Matcher m = reqPattern.matcher(s);
            if (m.matches()) {
                Material mat = Material.matchMaterial(m.group("mat"));
                int amount = Integer.parseInt(m.group("amount"));
                String op = m.group("op");
                String inc = m.group("inc");
                if (op != null && inc != null) {
                    int numChallenges = skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName);
                    amount = calcAmount(amount, op, Integer.parseInt(inc), numChallenges);
                }
                challengeLines.add("\u00a7f" + amount + " " + mat);
            } else {
                challengeLines.add("\u00a7f" + s + " levels");
            }
        }
        return challengeLines;
    }

    private boolean isCompletedChallenge(String challengeName) {
        return challengeName.charAt(1) == '2';
    }

    private boolean isRepeatableChallenge(String challengeName) {
        return challengeName.charAt(1) == 'a';
    }

    private boolean isNormalChallenge(String challengeName) {
        return challengeName.charAt(1) == 'e';
    }

}
