package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.challenge.ChallengeLogic.CHALLENGE_PAGESIZE;
import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

// TODO: Move all the texts to resource-files (translatable).

/**
 * The UI menu of uSkyBlock (using the inventory UI).
 */
public class SkyBlockMenu {
    private final Pattern PERM_VALUE_PATTERN = Pattern.compile("(\\[(?<perm>(?<not>[!])?[^\\]]+)\\])?(?<value>.*)");
    private final Pattern CHALLENGE_PAGE_HEADER = Pattern.compile(tr("Challenge Menu") + ".*\\((?<p>[0-9]+)/(?<max>[0-9]+)\\)");
    private uSkyBlock skyBlock;
    private final ChallengeLogic challengeLogic;
    ItemStack pHead = new ItemStack(397, 1, (short) 3);
    ItemStack sign = new ItemStack(323, 1);
    ItemStack biome = new ItemStack(6, 1, (short) 3);
    ItemStack lock = new ItemStack(101, 1);
    ItemStack warpset = new ItemStack(120, 1);
    ItemStack warptoggle = new ItemStack(69, 1);
    ItemStack invite = new ItemStack(398, 1);
    ItemStack kick = new ItemStack(301, 1);
    private List<PartyPermissionMenuItem> permissionMenuItems = Arrays.asList(
            new PartyPermissionMenuItem(biome, "canChangeBiome", "Change Biome",
                    tr("change the islands's biome.")),
            new PartyPermissionMenuItem(lock, "canToggleLock", "Toggle Island Lock",
                    tr("toggle the island's lock.")),
            new PartyPermissionMenuItem(warpset, "canChangeWarp", "Set Island Warp",
                    tr("set the island's warp."),
                    tr("set the island's warp,\nwhich allows non-group\nmembers to teleport to\n the island")),
            new PartyPermissionMenuItem(warptoggle, "canToggleWarp", "Toggle Island Warp",
                    tr("toggle the island's warp."),
                    tr("toggle the\nisland's warp, allowing them\nto turn it on or off at anytime.\nbut not set the location.")),
            new PartyPermissionMenuItem(invite, "canInviteOthers", "Invite Players",
                    tr("invite others to the island."),
                    tr("invite\n" +
                    "other players to the island if\n" +
                    "there is enough room for more\n" +
                    "members")),
            new PartyPermissionMenuItem(kick, "canKickOthers", "Kick Players",
                    tr("kick others from the island."),
                    tr("kick\n" +
                    "other players from the island,\n" +
                    "but they are unable to kick\n" +
                    "the island leader."))
    );

    public SkyBlockMenu(uSkyBlock skyBlock, ChallengeLogic challengeLogic) {
        this.skyBlock = skyBlock;
        this.challengeLogic = challengeLogic;
    }

    public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, tr("{0} <{1}>", pname, tr("Permissions")));
        final ItemStack pHead = new ItemStack(397, 1, (short) 3);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName(tr("\u00a7hPlayer Permissions"));
        addLore(lores, tr("\u00a7eClick here to return to\n\u00a7eyour island group's info."));
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        meta3.setDisplayName(pname + "'s Permissions");
        addLore(lores, tr("\u00a7eHover over an icon to view\n\u00a7ea permission. Change the\n\u00a7epermission by clicking it."));
        meta3.setLore(lores);
        pHead.setItemMeta(meta3);
        menu.addItem(new ItemStack[]{pHead});
        lores.clear();
        IslandInfo islandInfo = skyBlock.getIslandInfo(player);
        boolean isLeader = islandInfo.isLeader(player);
        for (PartyPermissionMenuItem menuItem : permissionMenuItems) {
            ItemStack itemStack = menuItem.getIcon();
            meta2 = itemStack.getItemMeta();
            if (islandInfo.hasPerm(pname, menuItem.getPerm())) {
                meta2.setDisplayName("\u00a7a" + menuItem.getTitle());
                lores.add(tr("\u00a7fThis player \u00a7acan"));
                addLore(lores, "\u00a7f", menuItem.getDescription());
                if (isLeader) {
                    addLore(lores, "\u00a7f", tr("Click here to remove this permission."));
                }
            } else {
                meta2.setDisplayName("\u00a7c" + menuItem.getTitle());
                lores.add(tr("\u00a7fThis player \u00a7ccannot"));
                addLore(lores, "\u00a7f", menuItem.getDescription());
                if (isLeader) {
                    addLore(lores, "\u00a7f", tr("Click here to grant this permission."));
                }
            }
            meta2.setLore(lores);
            itemStack.setItemMeta(meta2);
            menu.addItem(new ItemStack[]{itemStack});
            lores.clear();
        }
        return menu;
    }

    private void addLore(List<String> lores, String format, String multiLine) {
        for (String line : multiLine.split("\n")) {
            lores.add(format + line);
        }
    }
    private void addLore(List<String> lores, String multiLine) {
        addLore(lores, "", multiLine);
    }

    public Inventory displayPartyGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79" + tr("Island Group Members"));
        IslandInfo islandInfo = skyBlock.getIslandInfo(player);
        final Set<String> memberList = islandInfo.getMembers();
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        final ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName("\u00a7a" + tr("Island Group Members"));
        lores.add(tr("Group Members: \u00a72{0}\u00a77/\u00a7e{1}", islandInfo.getPartySize(), islandInfo.getMaxPartySize()));
        if (islandInfo.getPartySize() < islandInfo.getMaxPartySize()) {
            addLore(lores, tr("\u00a7aMore players can be invited to this island."));
        } else {
            addLore(lores, tr("\u00a7cThis island is full."));
        }
        addLore(lores, tr("\u00a7eHover over a player's icon to\n\u00a7eview their permissions. The\n\u00a7eleader can change permissions\n\u00a7eby clicking a player's icon."));
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        for (String temp : memberList) {
            meta3.setDisplayName("\u00a7f" + temp);
            boolean isLeader = islandInfo.isLeader(temp);
            if (isLeader) {
                addLore(lores, "\u00a7a\u00a7l", tr("Leader"));
            } else {
                addLore(lores, "\u00a7e\u00a7l", tr("Member"));
            }
            for (PartyPermissionMenuItem perm : permissionMenuItems) {
                if (isLeader ||islandInfo.hasPerm(temp, perm.getPerm())) {
                    lores.add("\u00a7a" + tr("Can {0}", "\u00a7f" + perm.getShortDescription()));
                } else {
                    lores.add("\u00a7c" + tr("Cannot {0}", "\u00a7f" + perm.getShortDescription()));
                }
            }
            if (islandInfo.isLeader(player.getName())) {
                addLore(lores, tr("\u00a7e<Click to change this player's permissions>"));
            }
            meta3.setOwner(temp);
            meta3.setLore(lores);
            pHead.setItemMeta(meta3);
            menu.addItem(pHead.clone());
            lores.clear();
        }
        return menu;
    }

    public Inventory displayLogGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, "\u00a79" + tr("Island Log"));
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("\u00a79\u00a7l" + tr("Island Log"));
        addLore(lores, tr("\u00a7eClick here to return to\n\u00a7ethe main island screen."));
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        ItemStack menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7e\u00a7lIsland Log"));
        for (String log : skyBlock.getIslandInfo(player).getLog()) {
            lores.add(log);
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
        return menu;
    }

    public Inventory displayBiomeGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79" + tr("Island Biome"));
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("\u00a7h" + tr("Island Biome"));
        addLore(lores, tr("\u00a7eClick here to return to\n\u00a7ethe main island screen."));
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        List<BiomeMenuItem> biomeMenus = Arrays.asList(
                new BiomeMenuItem(new ItemStack(Material.RAW_FISH, 1, (short) 2),
                        "ocean", tr("Ocean"),
                        tr("The ocean biome is the basic\nstarting biome for all islands.\npassive mobs like animals will\nnot spawn. Hostile mobs will\nspawn normally.")
                ),
                new BiomeMenuItem(new ItemStack(Material.SAPLING, 1, (short) 1),
                        "forest", tr("Forest"),
                        tr("The forest biome will allow\nyour island to spawn passive.\nmobs like animals (including\nwolves). Hostile mobs will\nspawn normally.")
                ),
                new BiomeMenuItem(new ItemStack(Material.SAND, 1),
                        "desert", tr("Desert"),
                        tr("The desert biome makes it so\nthat there is no rain or snow\non your island. Passive mobs\nwon't spawn. Hostile mobs will\nspawn normally.")
                ),
                new BiomeMenuItem(new ItemStack(Material.SAPLING, 1, (short) 3),
                        "jungle", tr("Jungle"),
                        tr("The jungle biome is bright\nand colorful. Passive mobs\n(including ocelots) will\nspawn. Hostile mobs will\nspawn normally.")
                ),
                new BiomeMenuItem(new ItemStack(Material.WATER_LILY, 1),
                        "swampland", tr("Swampland"),
                        tr("The swamp biome is dark\nand dull. Passive mobs\nwill spawn normally and\nslimes have a small chance\nto spawn at night depending\non the moon phase.")
                ),
                new BiomeMenuItem(new ItemStack(Material.SNOW, 1),
                        "taiga", tr("Taiga"),
                        tr("The taiga biome has snow\ninstead of rain. Passive\nmobs will spawn normally\n(including wolves) and\nhostile mobs will spawn.")
                ),
                new BiomeMenuItem(new ItemStack(Material.RED_MUSHROOM, 1),
                        "mushroom", tr("Mushroom"),
                        tr("The mushroom biome is\nbright and colorful.\nMooshrooms are the only\nmobs that will spawn.\nNo other passive or\nhostile mobs will spawn.")
                ),
                new BiomeMenuItem(new ItemStack(Material.NETHER_BRICK, 1),
                        "hell", tr("Hell"),
                        tr("The hell biome looks\ndark and dead. Some\nmobs from the nether will\nspawn in this biome\n(excluding ghasts and\nblazes).")
                ),
                new BiomeMenuItem(new ItemStack(Material.EYE_OF_ENDER, 1),
                        "sky", tr("Sky"),
                        tr("The sky biome gives your\nisland a special dark sky.\nOnly endermen will spawn\nin this biome.")
                ),
                new BiomeMenuItem(new ItemStack(Material.LONG_GRASS, 1, (byte) 1),
                        "plains", tr("Plains"),
                        tr("The plains biome has rain\ninstead of snow. Passive\nmobs will spawn normally\n(including horses) and\nhostile mobs will spawn.")
                ),
                new BiomeMenuItem(new ItemStack(Material.EMERALD_ORE, 1),
                        "extreme_hills", tr("Extreme Hills"),
                        tr("The extreme hills biome.\nPassive mobs will spawn \nnormally and hostile\nmobs will spawn.")
                ),
                new BiomeMenuItem(new ItemStack(Material.RED_ROSE, 1, (short) 5),
                        "flower_forest", tr("Flower Forest"),
                        tr("The flower forest biome.\nPassive mobs will spawn \nnormally and hostile\nmobs will spawn.")
                ),
                new BiomeMenuItem(new ItemStack(Material.PRISMARINE_SHARD, 1),
                        "deep_ocean", tr("Deep Ocean"),
                        tr("The deep-ocean biome is an advanced\n" +
                                "biome. Passive mobs like animals will\n" +
                                "not spawn. Hostile mobs \n"+
                                "(including Guardians) will\n" +
                                "spawn normally.")
                ));
        String currentBiome = skyBlock.getCurrentBiome(player);
        for (BiomeMenuItem biomeMenu : biomeMenus) {
            ItemStack menuItem = biomeMenu.getIcon();
            meta4 = menuItem.getItemMeta();
            if (VaultHandler.checkPerk(player.getName(), "usb.biome." + biomeMenu.getId(), player.getWorld())) {
                meta4.setDisplayName("\u00a7a" + tr("Biome: {0}", biomeMenu.getTitle()));
                addLore(lores, "\u00a7f", biomeMenu.getDescription());
                if (biomeMenu.getId().equalsIgnoreCase(currentBiome)) {
                    addLore(lores, tr("\u00a72\u00a7lThis is your current biome."));
                } else {
                    addLore(lores, tr("\u00a7e\u00a7lClick to change to this biome."));
                }
            } else {
                meta4.setDisplayName("\u00a78" + tr("Biome: {0}", biomeMenu.getTitle()));
                lores.add("\u00a7c" + tr("You cannot use this biome."));
                addLore(lores, "\u00a77", biomeMenu.getDescription());
            }
            meta4.setLore(lores);
            menuItem.setItemMeta(meta4);
            menu.addItem(menuItem);
            lores.clear();
        }
        return menu;
    }

    private void addExtraMenus(Player player, Inventory menu) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null) {
            return;
        }
        World world = skyBlock.getSkyBlockWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                int index = Integer.parseInt(sIndex, 10);
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                List<String> lores = new ArrayList<>();
                for (String l : menuSection.getStringList("lore")) {
                    Matcher matcher = PERM_VALUE_PATTERN.matcher(l);
                    if (matcher.matches()) {
                        String perm = matcher.group("perm");
                        String lore = matcher.group("value");
                        boolean not = matcher.group("not") != null;
                        if (perm != null) {
                            boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                            if ((hasPerm && !not) || (!hasPerm && not)) {
                                lores.add(lore);
                            }
                        } else {
                            lores.add(lore);
                        }
                    }
                }
                // Only SIMPLE icons supported...
                ItemStack item = new ItemStack(Material.matchMaterial(icon), 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(title);
                meta.setLore(lores);
                item.setItemMeta(meta);
                menu.setItem(index, item);
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to add extra-menu " + sIndex + ": " + e);
            }
        }
    }

    private boolean isExtraMenuAction(Player player, ItemStack currentItem) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null || currentItem == null || currentItem.getItemMeta() == null) {
            return false;
        }
        Material itemType = currentItem.getType();
        String itemTitle = currentItem.getItemMeta().getDisplayName();
        World world = player.getWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                Material material = Material.matchMaterial(icon);
                if (title.equals(itemTitle) && material == itemType) {
                    for (String command : menuSection.getStringList("commands")) {
                        Matcher matcher = PERM_VALUE_PATTERN.matcher(command);
                        if (matcher.matches()) {
                            String perm = matcher.group("perm");
                            String cmd = matcher.group("value");
                            boolean not = matcher.group("not") != null;
                            if (perm != null) {
                                boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                                if ((hasPerm && !not) || (!hasPerm && not)) {
                                    skyBlock.execCommand(player, cmd, false);
                                }
                            } else {
                                skyBlock.execCommand(player, cmd, false);
                            }
                        } else {
                            uSkyBlock.log(Level.INFO, "\u00a7a[uSkyBlock] Malformed menu " + title + ", invalid command : " + command);
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to execute commands for extra-menu " + sIndex + ": " + e);
            }
        }
        return false;
    }

    public Inventory displayChallengeGUI(final Player player, int page) {
        Inventory menu = Bukkit.createInventory(null, CHALLENGE_PAGESIZE, "\u00a79" + tr("{0} ({1}/{2})", tr("Challenge Menu"), page, challengeLogic.getTotalPages()));
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        challengeLogic.populateChallengeRank(menu, player, pi, page);
        return menu;
    }

    public Inventory displayIslandGUI(final Player player) {
        Inventory menu = null;
        if (skyBlock.hasIsland(player)) {
            menu = Bukkit.createInventory(null, 18, "\u00a79" + tr("Island Menu"));
            addMainMenu(menu, player);
        } else {
            menu = Bukkit.createInventory(null, 9, "\u00a79" + tr("Island Create Menu"));
            addInitMenu(menu);
        }
        return menu;
    }

    private void addInitMenu(Inventory menu) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lStart an Island"));
        addLore(lores, "\u00a7f", tr("Start your skyblock journey\nby starting your own island.\nComplete challenges to earn\nitems and skybucks to help\nexpand your skyblock. You can\ninvite others to join in\nbuilding your island empire!\n\u00a7e\u00a7lClick here to start!"));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName(tr("\u00a7a\u00a7lJoin an Island"));
        addLore(lores, "\u00a7f", tr("Want to join another player's\nisland instead of starting\nyour own? If another player\ninvites you to their island\nyou can click here or use\n\u00a7e/island accept\u00a7f to join them.\n\u00a7e\u00a7lClick here to accept an invite!\n\u00a7e\u00a7l(You must be invited first)"));
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.setItem(4, menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SIGN, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Help"));
        addLore(lores, "\u00a7f", tr("Need help with skyblock\nconcepts or commands? View\ndetails about them here.\n\u00a7e\u00a7lClick here for help!"));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
    }

    private void addMainMenu(Inventory menu, Player player) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.WOOD_DOOR, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lReturn Home"));
        addLore(lores, "\u00a7f", tr("Return to your island's home\npoint. You can change your home\npoint to any location on your\nisland using \u00a7b/island sethome\n\u00a7e\u00a7lClick here to return home."));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        IslandInfo islandInfo = skyBlock.getIslandInfo(player);

        menuItem = new ItemStack(Material.DIAMOND_ORE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lChallenges"));
        addLore(lores, "\u00a7f", tr("View a list of \u00a79challenges that\nyou can complete on your island\nto earn skybucks, items, perks,\nand titles."));
        if (skyBlock.getChallengeLogic().isEnabled()) {
            addLore(lores, tr("\u00a7e\u00a7lClick here to view challenges."));
        } else {
            addLore(lores, tr("\u00a74\u00a7lChallenges disabled."));
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);

        lores.clear();
        menuItem = new ItemStack(Material.EXP_BOTTLE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Level"));
        addLore(lores, tr("\u00a7eCurrent Level: \u00a7a{0,number,##.#}", islandInfo.getLevel()));
        addLore(lores, "\u00a7f", tr("Gain island levels by expanding\nyour skyblock and completing\ncertain challenges. Rarer blocks\nwill add more to your level.\n\u00a7e\u00a7lClick here to refresh.\n\u00a7e\u00a7l(must be on island)"));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7l" + tr("Island Group"));
        lores.add("\u00a7eMembers: \u00a72" + islandInfo.getPartySize() + "/" + islandInfo.getMaxPartySize());
        addLore(lores, "\u00a7f", tr("View the members of your island\ngroup and their permissions. If\nyou are the island leader, you\ncan change the member permissions.\n\u00a7e\u00a7lClick here to view or change."));
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7l" + tr("Change Island Biome"));
        lores.add("\u00a7eCurrent Biome: \u00a7b" + islandInfo.getBiome());
        addLore(lores, "\u00a7f", tr("The island biome affects things\nlike grass color and spawning\nof both animals and monsters."));
        if (islandInfo.hasPerm(player, "canChangeBiome")) {
            addLore(lores, tr("\u00a7e\u00a7lClick here to change biomes."));
        } else {
            addLore(lores, tr("\u00a7c\u00a7lYou can't change the biome."));
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.IRON_FENCE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Lock"));
        if (skyBlock.getIslandInfo(player).isLocked()) {
            addLore(lores, tr("\u00a7eLock Status: \u00a7aActive\n\u00a7fYour island is currently \u00a7clocked.\n\u00a7fPlayers outside of your group\n\u00a7fare unable to enter your island."));
            if (islandInfo.hasPerm(player, "canToggleLock")) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to unlock your island."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the lock."));
            }
        } else {
            addLore(lores, tr("\u00a7eLock Status: \u00a78Inactive\n\u00a7fYour island is currently \u00a7aunlocked.\n\u00a7fAll players are able to enter your\n\u00a7fisland, but only you and your group\n\u00a7fmembers may build there."));
            if (islandInfo.hasPerm(player, "canToggleLock")) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to lock your island."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the lock."));
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        if (skyBlock.getIslandInfo(player).hasWarp()) {
            menuItem = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Warp"));
            addLore(lores, tr("\u00a7eWarp Status: \u00a7aActive\n\u00a7fOther players may warp to your\n\u00a7fisland at anytime to the point\n\u00a7fyou set using \u00a7d/island setwarp."));
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to deactivate."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the warp."));
            }
        } else {
            menuItem = new ItemStack(Material.ENDER_STONE, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Warp"));
            addLore(lores, tr("\u00a7eWarp Status: \u00a78Inactive\n\u00a7fOther players can't warp to your\n\u00a7fisland. Set a warp point using\n\u00a7d/island setwarp \u00a7fbefore activating."));
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to activate."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the warp."));
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Log"));
        addLore(lores, "\u00a7f", tr("View a log of events from\nyour island such as member,\nbiome, and warp changes.\n\u00a7e\u00a7lClick to view the log."));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem); // Last item, first line
        lores.clear();

        menuItem = new ItemStack(Material.BED, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lChange Home Location"));
        addLore(lores, "\u00a7f", tr("When you teleport to your\nisland you will be taken to\nthis location.\n\u00a7e\u00a7lClick here to change."));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(9, menuItem); // First item, 2nd line
        lores.clear();

        menuItem = new ItemStack(Material.HOPPER, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lChange Warp Location"));
        addLore(lores, "\u00a7f", tr("When your warp is activated,\nother players will be taken to\nthis point when they teleport\nto your island.\n\u00a7e\u00a7lClick here to change."));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(15, menuItem);
        lores.clear();
        addExtraMenus(player, menu);
    }

    public void onClick(InventoryClickEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null || event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return; // Bail out, nothing we can do anyway
        }
        Player p = (Player) event.getWhoClicked();
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        SkullMeta skull = meta instanceof SkullMeta ? (SkullMeta) meta : null;
        String inventoryName = stripFormatting(event.getInventory().getName());
        if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Group Members")))) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (meta == null || event.getCurrentItem().getType() == Material.SIGN) {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            } else if (skull != null && skyBlock.getIslandInfo(p).isLeader(p)) {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, skull.getOwner()));
            }
        } else if (inventoryName.contains(stripFormatting(tr("Permissions")))) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            String[] playerPerm = inventoryName.split(" ");
            IslandInfo islandInfo = skyBlock.getIslandInfo(p);
            if (!skyBlock.getIslandInfo(p).isLeader(p)) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            }
            if (event.getCurrentItem().getTypeId() == 6) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeBiome");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 101) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleLock");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 90) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 69) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 398) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canInviteOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 301) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canKickOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 323) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            }
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Biome")))) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome jungle");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 1) {
                p.closeInventory();
                p.performCommand("island biome forest");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAND) {
                p.closeInventory();
                p.performCommand("island biome desert");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SNOW) {
                p.closeInventory();
                p.performCommand("island biome taiga");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
                p.closeInventory();
                p.performCommand("island biome sky");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.WATER_LILY) {
                p.closeInventory();
                p.performCommand("island biome swampland");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.NETHER_BRICK) {
                p.closeInventory();
                p.performCommand("island biome hell");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RED_MUSHROOM) {
                p.closeInventory();
                p.performCommand("island biome mushroom");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.LONG_GRASS) {
                p.closeInventory();
                p.performCommand("island biome plains");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EMERALD_ORE) {
                p.closeInventory();
                p.performCommand("island biome extreme_hills");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RED_ROSE && event.getCurrentItem().getDurability() == 5) {
                p.closeInventory();
                p.performCommand("island biome flower_forest");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RAW_FISH) {
                p.closeInventory();
                p.performCommand("island biome ocean");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.PRISMARINE_SHARD) {
                p.closeInventory();
                p.performCommand("island biome deep_ocean");
                p.openInventory(displayIslandGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            }
        } else if (inventoryName.contains(stripFormatting(tr("Challenge Menu")))) {
            event.setCancelled(true);
            Matcher m = CHALLENGE_PAGE_HEADER.matcher(inventoryName);
            int page = 1;
            int max = challengeLogic.getTotalPages();
            if (m.find()) {
                page = Integer.parseInt(m.group("p"));
                max = Integer.parseInt(m.group("max"));
            }
            if (event.getSlot() < 0 || event.getSlot() > CHALLENGE_PAGESIZE) {
                return;
            }
            if ((event.getSlot() % 9) > 0) { // 0,9... are the rank-headers...
                p.closeInventory();
                if (event.getCurrentItem().getItemMeta() != null) {
                    String challenge = event.getCurrentItem().getItemMeta().getDisplayName();
                    String challengeName = stripFormatting(challenge);
                    p.performCommand("c c " + challengeName);
                }
                p.openInventory(displayChallengeGUI(p, page));
            } else {
                p.closeInventory();
                if (event.getSlot() < (CHALLENGE_PAGESIZE/2)) { // Upper half
                    if (page > 1) {
                        p.openInventory(displayChallengeGUI(p, page - 1));
                    } else {
                        p.openInventory(displayIslandGUI(p));
                    }
                } else if (page < max) {
                    p.openInventory(displayChallengeGUI(p, page + 1));
                } else {
                    p.openInventory(displayIslandGUI(p));
                }
            }
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Log")))) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            p.closeInventory();
            p.openInventory(displayIslandGUI(p));
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Menu")))) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            PlayerInfo playerInfo = skyBlock.getPlayerInfo(p);
            IslandInfo islandInfo = skyBlock.getIslandInfo(playerInfo);
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome");
            } else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                p.closeInventory();
                p.performCommand("island party");
            } else if (event.getCurrentItem().getType() == Material.BED) {
                p.closeInventory();
                p.performCommand("island sethome");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.HOPPER) {
                p.closeInventory();
                p.performCommand("island setwarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
                p.closeInventory();
                p.performCommand("island log");
            } else if (event.getCurrentItem().getType() == Material.WOOD_DOOR) {
                p.closeInventory();
                p.performCommand("island home");
            } else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE) {
                p.closeInventory();
                p.performCommand("island level");
            } else if (event.getCurrentItem().getType() == Material.DIAMOND_ORE) {
                p.closeInventory();
                p.performCommand("c");
            } else if (event.getCurrentItem().getType() == Material.ENDER_STONE || event.getCurrentItem().getType() == Material.ENDER_PORTAL_FRAME) {
                p.closeInventory();
                p.performCommand("island togglewarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island unlock");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && !islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island lock");
                p.openInventory(displayIslandGUI(p));
            } else {
                if (!isExtraMenuAction(p, event.getCurrentItem())) {
                    p.closeInventory();
                    p.openInventory(displayIslandGUI(p));
                }
            }
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Create Menu")))) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                p.closeInventory();
                p.performCommand("island create");
            } else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                p.closeInventory();
                p.performCommand("island accept");
            } else if (event.getCurrentItem().getType() == Material.SIGN) {
                p.closeInventory();
                p.performCommand("chestcommands open island_help");
            }
        } else if (inventoryName.startsWith(stripFormatting(tr("Config:"))) && event.getWhoClicked() instanceof Player) {
            skyBlock.getConfigMenu().onClick(event);
        }
    }
}
