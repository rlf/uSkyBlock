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
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.command.island.BiomeCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.IslandPerk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import dk.lockfuglsang.minecraft.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.pre;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.challenge.ChallengeLogic.CHALLENGE_PAGESIZE;
import static us.talabrek.ultimateskyblock.challenge.ChallengeLogic.COLS_PER_ROW;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;
import static us.talabrek.ultimateskyblock.util.LogUtil.log;

// TODO: Move all the texts to resource-files (translatable).

/**
 * The UI menu of uSkyBlock (using the inventory UI).
 */
public class SkyBlockMenu {
    private final Pattern PERM_VALUE_PATTERN = Pattern.compile("(\\[(?<perm>(?<not>[!])?[^\\]]+)\\])?(?<value>.*)");
    private final Pattern CHALLENGE_PAGE_HEADER = Pattern.compile(tr("Challenge Menu") + ".*\\((?<p>[0-9]+)/(?<max>[0-9]+)\\)");
    private uSkyBlock plugin;
    private final ChallengeLogic challengeLogic;
    ItemStack sign = new ItemStack(323, 1);
    ItemStack biome = new ItemStack(6, 1, (short) 3);
    ItemStack lock = new ItemStack(101, 1);
    ItemStack warpset = new ItemStack(120, 1);
    ItemStack warptoggle = new ItemStack(69, 1);
    ItemStack invite = new ItemStack(398, 1);
    ItemStack kick = new ItemStack(301, 1);
    private List<PartyPermissionMenuItem> permissionMenuItems = Arrays.asList(
            new PartyPermissionMenuItem(biome, "canChangeBiome", tr("Change Biome"),
                    tr("change the island's biome.")),
            new PartyPermissionMenuItem(lock, "canToggleLock", tr("Toggle Island Lock"),
                    tr("toggle the island's lock.")),
            new PartyPermissionMenuItem(warpset, "canChangeWarp", tr("Set Island Warp"),
                    tr("set the island's warp."),
                    tr("set the island's warp,\nwhich allows non-group\nmembers to teleport to\nthe island.")),
            new PartyPermissionMenuItem(warptoggle, "canToggleWarp", tr("Toggle Island Warp"),
                    tr("toggle the island's warp."),
                    tr("toggle the island's warp,\nallowing them to turn it\non or off at anytime, but\nnot set the location.")),
            new PartyPermissionMenuItem(invite, "canInviteOthers", tr("Invite Players"),
                    tr("invite others to the island."),
                    tr("invite\n" +
                    "other players to the island if\n" +
                    "there is enough room for more\n" +
                    "members")),
            new PartyPermissionMenuItem(kick, "canKickOthers", tr("Kick Players"),
                    tr("kick others from the island."),
                    tr("kick\n" +
                    "other players from the island,\n" +
                    "but they are unable to kick\n" +
                    "the island leader."))
    );
    private List<BiomeMenuItem> biomeMenus = Arrays.asList(
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
            ),
            new BiomeMenuItem(new ItemStack(Material.PACKED_ICE, 1),
                    "ice_plains", tr("Ice Plains"),
                    tr("The ice-plains biome is an advanced biome.\nMobs will spawn naturally.\nincluding polar-bears")
            )
    );

    public SkyBlockMenu(uSkyBlock plugin, ChallengeLogic challengeLogic) {
        this.plugin = plugin;
        this.challengeLogic = challengeLogic;
    }

    public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        List<String> lores = new ArrayList<>();
        String emptyTitle = tr("{0} <{1}>", "", tr("Permissions"));
        String title = tr("{0} <{1}>", pname.substring(0, Math.min(32-emptyTitle.length(), pname.length())), tr("Permissions"));
        Inventory menu = Bukkit.createInventory(null, 9, title);
        final ItemStack pHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName(tr("\u00a79Player Permissions"));
        addLore(lores, tr("\u00a7eClick here to return to\n\u00a7eyour island group's info."));
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(sign);
        lores.clear();
        meta3.setOwner(pname);
        meta3.setDisplayName(tr("\u00a7e{0}''\u00a79s Permissions", pname));
        addLore(lores, tr("\u00a7eHover over an icon to view\n\u00a7ea permission. Change the\n\u00a7epermission by clicking it."));
        meta3.setLore(lores);
        pHead.setItemMeta(meta3);
        menu.addItem(pHead);
        lores.clear();
        IslandInfo islandInfo = plugin.getIslandInfo(player);
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
            menu.addItem(itemStack);
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
        IslandInfo islandInfo = plugin.getIslandInfo(player);
        final Set<String> memberList = islandInfo.getMembers();
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
        menu.addItem(sign.clone());
        lores.clear();
        for (String temp : memberList) {
            ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            SkullMeta meta3 = (SkullMeta) headItem.getItemMeta();
            meta3.setDisplayName(tr("\u00a7e{0}''s\u00a79 Permissions", temp));
            meta3.setOwner(temp);
            boolean isLeader = islandInfo.isLeader(temp);
            if (isLeader) {
                addLore(lores, "\u00a7a\u00a7l", tr("Leader"));
            } else {
                addLore(lores, "\u00a7e\u00a7l", tr("Member"));
            }
            for (PartyPermissionMenuItem perm : permissionMenuItems) {
                if (isLeader || islandInfo.hasPerm(temp, perm.getPerm())) {
                    lores.add("\u00a7a" + tr("Can {0}", "\u00a7f" + perm.getShortDescription()));
                } else {
                    lores.add("\u00a7c" + tr("Cannot {0}", "\u00a7f" + perm.getShortDescription()));
                }
            }
            if (islandInfo.isLeader(player.getName())) {
                addLore(lores, tr("\u00a7e<Click to change this player's permissions>"));
            }
            meta3.setLore(lores);
            headItem.setItemMeta(meta3);
            menu.addItem(headItem);
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
        for (String log : plugin.getIslandInfo(player).getLog()) {
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
        String currentBiome = plugin.getCurrentBiome(player);
        for (BiomeMenuItem biomeMenu : biomeMenus) {
            if (!BiomeCommand.biomeExists(biomeMenu.getId())) {
                continue; // Skip it
            }
            ItemStack menuItem = biomeMenu.getIcon();
            meta4 = menuItem.getItemMeta();
            if (hasPermission(player, "usb.biome." + biomeMenu.getId())) {
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
        ConfigurationSection extras = plugin.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null) {
            return;
        }
        World world = plugin.getSkyBlockWorld();
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
                            boolean hasPerm = hasPermission(player, perm);
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
                log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to add extra-menu " + sIndex + ": " + e);
            }
        }
    }

    private boolean isExtraMenuAction(Player player, ItemStack currentItem) {
        ConfigurationSection extras = plugin.getConfig().getConfigurationSection("options.extra-menus");
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
                                boolean hasPerm = hasPermission(player, perm);
                                if ((hasPerm && !not) || (!hasPerm && not)) {
                                    plugin.execCommand(player, cmd, false);
                                }
                            } else {
                                plugin.execCommand(player, cmd, false);
                            }
                        } else {
                            log(Level.INFO, "\u00a7a[uSkyBlock] Malformed menu " + title + ", invalid command : " + command);
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to execute commands for extra-menu " + sIndex + ": " + e);
            }
        }
        return false;
    }

    public Inventory displayChallengeGUI(final Player player, int page) {
        int total = challengeLogic.getTotalPages();
        Inventory menu = Bukkit.createInventory(null, CHALLENGE_PAGESIZE+COLS_PER_ROW, "\u00a79" + pre("{0} ({1}/{2})", tr("Challenge Menu"), page, total));
        final PlayerInfo pi = plugin.getPlayerInfo(player);
        challengeLogic.populateChallengeRank(menu, pi, page);
        int pages[] = new int[9];
        pages[0] = 1;
        pages[8] = total;
        int startOffset = 2;
        if (page > 5) {
            startOffset = (int) ((Math.round(page/2d)) - 1);
            if (startOffset > total-7) {
                startOffset = total-7;
            }
        }
        for (int i = 0; i < 7; i++) {
            pages[i+1] = startOffset+i;
        }
        for (int i = 0; i < pages.length; i++) {
            int p = pages[i];
            if (p >= 1 && p <= total) {
                ItemStack pageItem = null;
                if (p == page) {
                    pageItem = ItemStackUtil.createItemStack("BOOK_AND_QUILL", tr("\u00a77Current page"), null);
                } else {
                    pageItem = ItemStackUtil.createItemStack("BOOK", tr("\u00a77Page {0}", p), null);
                }
                if (i == 0) {
                    pageItem = ItemStackUtil.builder(pageItem).displayName(tr("\u00a77First Page")).build();
                } else if (i == 8) {
                    pageItem = ItemStackUtil.builder(pageItem).displayName(tr("\u00a77Last Page")).build();
                }
                pageItem.setAmount(p);
                menu.setItem(i + CHALLENGE_PAGESIZE, pageItem);
            }
        }
        return menu;
    }

    public Inventory displayIslandGUI(final Player player) {
        if (plugin.hasIsland(player)) {
            return createMainMenu(player);
        } else {
            return createInitMenu(player);
        }
    }

    private Inventory createInitMenu(Player player) {
        List<String> schemeNames = plugin.getIslandGenerator().getSchemeNames();
        int menuSize = (int) Math.ceil(getMaxSchemeIndex(schemeNames) / 9d)*9;
        Inventory menu = Bukkit.createInventory(null, menuSize, "\u00a79" + tr("Island Create Menu"));
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta = menuItem.getItemMeta();
        meta.setDisplayName(tr("\u00a7a\u00a7lStart an Island"));
        addLore(lores, "\u00a7f", tr("Start your skyblock journey\nby starting your own island.\nComplete challenges to earn\nitems and skybucks to help\nexpand your skyblock. You can\ninvite others to join in\nbuilding your island empire!\n\u00a7e\u00a7lClick here to start!"));
        meta.setLore(lores);
        menuItem.setItemMeta(meta);
        menu.addItem(menuItem);
        lores.clear();

        if (plugin.getConfig().getBoolean("island-schemes-enabled", true)) {
            int index = 1;
            for (String schemeName : schemeNames) {
                IslandPerk islandPerk = plugin.getPerkLogic().getIslandPerk(schemeName);
                boolean enabled = plugin.getConfig().getBoolean("island-schemes." + islandPerk.getSchemeName() + ".enabled", true);
                if (!enabled) {
                    continue; // Skip
                }
                index = Math.max(plugin.getConfig().getInt("island-schemes." + islandPerk.getSchemeName() + ".index", index), 1);
                menuItem = islandPerk.getDisplayItem();
                meta = menuItem.getItemMeta();
                lores = meta.getLore();
                if (lores == null) {
                    lores = new ArrayList<>();
                }
                // TODO: 30/01/2016 - R4zorax: Add the extra items?
                if (player.hasPermission(islandPerk.getPermission())) {
                    addLore(lores, tr("\u00a7aClick to create!"));
                } else {
                    addLore(lores, tr("\u00a7cNo access!\n\u00a77({0})", islandPerk.getPermission()));
                }
                meta.setLore(lores);
                menuItem.setItemMeta(meta);
                menu.setItem(index++, menuItem);
            }
        }

        lores.clear();
        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName(tr("\u00a7a\u00a7lJoin an Island"));
        addLore(lores, "\u00a7f", tr("Want to join another player's\nisland instead of starting\nyour own? If another player\ninvites you to their island\nyou can click here or use\n\u00a7e/island accept\u00a7f to join them.\n\u00a7e\u00a7lClick here to accept an invite!\n\u00a7e\u00a7l(You must be invited first)"));
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.setItem(menuSize-1, menuItem);
        return menu;
    }

    private int getMaxSchemeIndex(List<String> schemeNames) {
        int index = 1;
        for (String schemeName : schemeNames) {
            int nextIndex = plugin.getConfig().getInt("island-schemes." + schemeName + ".index", index);
            if (nextIndex > index) {
                index = nextIndex;
            } else {
                index++;
            }
        }
        return index + 2;
    }

    private Inventory createMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79" + tr("Island Menu"));
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.WOOD_DOOR, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lReturn Home"));
        addLore(lores, "\u00a7f", tr("Return to your island's home\npoint. You can change your home\npoint to any location on your\nisland using \u00a7b/island sethome\n\u00a7e\u00a7lClick here to return home."));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        IslandInfo islandInfo = plugin.getIslandInfo(player);

        menuItem = new ItemStack(Material.DIAMOND_ORE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName(tr("\u00a7a\u00a7lChallenges"));
        addLore(lores, "\u00a7f", tr("View a list of \u00a79challenges that\nyou can complete on your island\nto earn skybucks, items, perks,\nand titles."));
        if (plugin.getChallengeLogic().isEnabled()) {
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
        addLore(lores, plugin.getLimitLogic().getSummary(islandInfo));
        addLore(lores, "\u00a7f", tr("Gain island levels by expanding\nyour skyblock and completing\ncertain challenges. Rarer blocks\nwill add more to your level.\n\u00a7e\u00a7lClick here to refresh.\n\u00a7e\u00a7l(must be on island)"));
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7l" + tr("Island Group"));
        lores.add(tr("\u00a7eMembers: \u00a72{0}/{1}", islandInfo.getPartySize(), islandInfo.getMaxPartySize()));
        addLore(lores, "\u00a7f", tr("View the members of your island\ngroup and their permissions. If\nyou are the island leader, you\ncan change the member permissions.\n\u00a7e\u00a7lClick here to view or change."));
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7l" + tr("Change Island Biome"));
        lores.add(tr("\u00a7eCurrent Biome: \u00a7b{0}", islandInfo.getBiome()));
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
        if (plugin.getIslandInfo(player).isLocked()) {
            addLore(lores, tr("\u00a7eLock Status: \u00a7aActive\n\u00a7fYour island is currently \u00a7clocked.\n\u00a7fPlayers outside of your group\n\u00a7fare unable to enter your island."));
            if (islandInfo.hasPerm(player, "canToggleLock") && hasPermission(player, "usb.island.lock")) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to unlock your island."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the lock."));
            }
        } else {
            addLore(lores, tr("\u00a7eLock Status: \u00a78Inactive\n\u00a7fYour island is currently \u00a7aunlocked.\n\u00a7fAll players are able to enter your\n\u00a7fisland, but only you and your group\n\u00a7fmembers may build there."));
            if (islandInfo.hasPerm(player, "canToggleLock") && hasPermission(player, "usb.island.lock")) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to lock your island."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the lock."));
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        if (plugin.getIslandInfo(player).hasWarp()) {
            menuItem = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Warp"));
            addLore(lores, tr("\u00a7eWarp Status: \u00a7aActive\n\u00a7fOther players may warp to your\n\u00a7fisland at anytime to the point\n\u00a7fyou set using \u00a7d/island setwarp."));
            if (islandInfo.hasPerm(player, "canToggleWarp") && hasPermission(player, "usb.island.togglewarp")) {
                addLore(lores, tr("\u00a7e\u00a7lClick here to deactivate."));
            } else {
                addLore(lores, tr("\u00a7c\u00a7lYou can't change the warp."));
            }
        } else {
            menuItem = new ItemStack(Material.ENDER_STONE, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName(tr("\u00a7a\u00a7lIsland Warp"));
            addLore(lores, tr("\u00a7eWarp Status: \u00a78Inactive\n\u00a7fOther players can't warp to your\n\u00a7fisland. Set a warp point using\n\u00a7d/island setwarp \u00a7fbefore activating."));
            if (islandInfo.hasPerm(player, "canToggleWarp") && hasPermission(player, "usb.island.togglewarp")) {
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
        addLore(lores, "\u00a7f", tr("When your warp is activated,\nother players will be taken to\nthis point when they teleport\nto your island."));
        if (islandInfo.hasPerm(player, "canChangeWarp") && hasPermission(player, "usb.island.setwarp")) {
            addLore(lores, tr("\u00a7e\u00a7lClick here to change."));
        } else {
            addLore(lores, tr("\u00a7c\u00a7lYou can't change the warp."));
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(15, menuItem);
        lores.clear();
        if (islandInfo.isLeader(player)) {
            if (plugin.getConfig().getBoolean("island-schemes-enabled", true)) {
                menuItem = new ItemStack(Material.DIRT, 1, (byte) 2);
                meta4 = menuItem.getItemMeta();
                meta4.setDisplayName(tr("\u00a7c\u00a7lRestart Island"));
                addLore(lores, "\u00a7f", tr("Restarts your island.\n\u00a74WARNING! \u00a7cwill remove your items and island!"));
                meta4.setLore(lores);
                menuItem.setItemMeta(meta4);
                menu.setItem(17, menuItem);
                lores.clear();
            }
        } else {
            menuItem = new ItemStack(Material.IRON_DOOR, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName(tr("\u00a7c\u00a7lLeave Island"));
            addLore(lores, "\u00a7f", tr("Leaves your island.\n\u00a74WARNING! \u00a7cwill remove all your items!"));
            addLore(lores, tr("\u00a7cClick to leave"));
            meta4.setLore(lores);
            menuItem.setItemMeta(meta4);
            menu.setItem(17, menuItem);
            lores.clear();
            long millisLeft = plugin.getConfirmHandler().millisLeft(player, "/is leave");
            if (millisLeft > 0) {
                updateLeaveMenuItemTimer(player, menu, menuItem);
            }
        }
        addExtraMenus(player, menu);
        return menu;
    }

    public void onClick(InventoryClickEvent event) {
        ItemStack currentItem = event != null ? event.getCurrentItem() : null;
        if (event == null || currentItem == null || event.getWhoClicked() == null || event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return; // Bail out, nothing we can do anyway
        }
        Player p = (Player) event.getWhoClicked();
        ItemMeta meta = currentItem.getItemMeta();
        SkullMeta skull = meta instanceof SkullMeta ? (SkullMeta) meta : null;
        String inventoryName = stripFormatting(event.getInventory().getName());
        int slotIndex = event.getSlot();
        int menuSize = event.getInventory().getSize();
        if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Group Members")))) {
            onClickPartyMenu(event, currentItem, p, meta, skull, slotIndex);
        } else if (inventoryName.contains(stripFormatting(tr("Permissions")))) {
            onClickPermissionMenu(event, currentItem, p, inventoryName, slotIndex);
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Biome")))) {
            onClickBiomeMenu(event, currentItem, p, slotIndex);
        } else if (inventoryName.contains(stripFormatting(tr("Challenge Menu")))) {
            onClickChallengeMenu(event, currentItem, p, inventoryName);
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Log")))) {
            onClickLogMenu(event, p, slotIndex);
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Menu")))) {
            onClickMainMenu(event, currentItem, p, slotIndex);
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Create Menu")))) {
            onClickCreateMenu(event, p, meta, slotIndex, menuSize);
        } else if (inventoryName.equalsIgnoreCase(stripFormatting(tr("Island Restart Menu")))) {
            onClickRestartMenu(event, p, meta, slotIndex, currentItem);
        } else if (inventoryName.startsWith(stripFormatting(tr("Config:"))) && event.getWhoClicked() instanceof Player) {
            plugin.getConfigMenu().onClick(event);
        }
    }

    private void onClickRestartMenu(final InventoryClickEvent event, final Player p, ItemMeta meta, int slotIndex, ItemStack currentItem) {
        event.setCancelled(true);
        if (slotIndex == 0) {
            p.closeInventory();
            p.openInventory(createMainMenu(p));
        } else if (currentItem != null && meta != null && meta.getDisplayName() != null) {
            String schemeName = stripFormatting(meta.getDisplayName());
            IslandPerk islandPerk = plugin.getPerkLogic().getIslandPerk(schemeName);
            if (plugin.getPerkLogic().getSchemes(p).contains(schemeName) && p.hasPermission(islandPerk.getPermission())) {
                if (plugin.getConfirmHandler().millisLeft(p, "/is restart") > 0) {
                    p.closeInventory();
                    p.performCommand("island restart " + schemeName);
                } else {
                    p.performCommand("island restart " + schemeName);
                    updateRestartMenuTimer(p, event.getInventory());
                }
            }
        }
    }

    private void updateRestartMenuTimer(final Player p, final Inventory inventory) {
        final BukkitTask[] hackySharing = new BukkitTask[1];
        hackySharing[0] = plugin.sync(new Runnable() {
            @Override
            public void run() {
                if (inventory.getViewers().contains(p)) {
                    updateRestartMenu(inventory, p, plugin.getIslandGenerator().getSchemeNames());
                }
                if (plugin.getConfirmHandler().millisLeft(p, "/is restart") <= 0 || !inventory.getViewers().contains(p)) {
                    if (hackySharing.length > 0 && hackySharing[0] != null) {
                        hackySharing[0].cancel();
                    }
                }
            }
        }, 0, 1000);
    }

    private void onClickCreateMenu(InventoryClickEvent event, Player p, ItemMeta meta, int slotIndex, int menuSize) {
        event.setCancelled(true);
        if (slotIndex == 0) {
            p.closeInventory();
            p.performCommand("island create");
        } else if (slotIndex == menuSize-1) {
            p.closeInventory();
            p.performCommand("island accept");
        } else if (meta != null && meta.getDisplayName() != null) {
            String schemeName = stripFormatting(meta.getDisplayName());
            if (plugin.getPerkLogic().getSchemes(p).contains(schemeName)) {
                p.closeInventory();
                p.performCommand("island create " + schemeName);
            } else {
                p.sendMessage(tr("\u00a7eYou do not have access to that island-schematic!"));
            }
        }
    }

    private void onClickMainMenu(InventoryClickEvent event, ItemStack currentItem, Player p, int slotIndex) {
        event.setCancelled(true);
        if (slotIndex < 0 || slotIndex > 35) {
            return;
        }
        PlayerInfo playerInfo = plugin.getPlayerInfo(p);
        IslandInfo islandInfo = plugin.getIslandInfo(playerInfo);
        if (currentItem.getType() == Material.SAPLING && currentItem.getDurability() == 3) {
            p.closeInventory();
            p.performCommand("island biome");
        } else if (currentItem.getType() == Material.SKULL_ITEM) {
            p.closeInventory();
            p.performCommand("island party");
        } else if (currentItem.getType() == Material.BED) {
            p.closeInventory();
            p.performCommand("island sethome");
            p.performCommand("island");
        } else if (currentItem.getType() == Material.HOPPER) {
            p.closeInventory();
            p.performCommand("island setwarp");
            p.performCommand("island");
        } else if (currentItem.getType() == Material.BOOK_AND_QUILL) {
            p.closeInventory();
            p.performCommand("island log");
        } else if (currentItem.getType() == Material.WOOD_DOOR) {
            p.closeInventory();
            p.performCommand("island home");
        } else if (currentItem.getType() == Material.EXP_BOTTLE) {
            p.closeInventory();
            p.performCommand("island level");
        } else if (currentItem.getType() == Material.DIAMOND_ORE) {
            p.closeInventory();
            p.performCommand("c");
        } else if (currentItem.getType() == Material.ENDER_STONE || currentItem.getType() == Material.ENDER_PORTAL_FRAME) {
            p.closeInventory();
            p.performCommand("island togglewarp");
            p.performCommand("island");
        } else if (currentItem.getType() == Material.IRON_FENCE && islandInfo.isLocked()) {
            p.closeInventory();
            p.performCommand("island unlock");
            p.performCommand("island");
        } else if (currentItem.getType() == Material.IRON_FENCE && !islandInfo.isLocked()) {
            p.closeInventory();
            p.performCommand("island lock");
            p.performCommand("island");
        } else if (slotIndex == 17) {
            if (islandInfo.isLeader(p) && plugin.getConfig().getBoolean("island-schemes-enabled", true)) {
                p.closeInventory();
                p.openInventory(createRestartGUI(p));
            } else {
                if (plugin.getConfirmHandler().millisLeft(p, "/is leave") > 0) {
                    p.closeInventory();
                    p.performCommand("island leave");
                } else {
                    p.performCommand("island leave");
                    updateLeaveMenuItemTimer(p, event.getInventory(), currentItem);
                }
            }
        } else {
            if (!isExtraMenuAction(p, currentItem)) {
                p.closeInventory();
                p.performCommand("island");
            }
        }
    }

    private void updateLeaveMenuItemTimer(final Player p, final Inventory inventory, final ItemStack currentItem) {
        final BukkitTask[] hackySharing = new BukkitTask[1];
        hackySharing[0] = plugin.sync(new Runnable() {
            @Override
            public void run() {
                long millisLeft = plugin.getConfirmHandler().millisLeft(p, "/is leave");
                if (inventory.getViewers().contains(p)) {
                    updateLeaveMenuItem(inventory, currentItem, millisLeft);
                }
                if (millisLeft <= 0 || !inventory.getViewers().contains(p)) {
                    if (hackySharing.length > 0 && hackySharing[0] != null) {
                        hackySharing[0].cancel();
                    }
                }
            }
        }, 0, 1000);
    }

    private void updateLeaveMenuItem(Inventory inventory, ItemStack currentItem, long millisLeft) {
        if (currentItem == null || currentItem.getItemMeta() == null ||currentItem.getItemMeta().getLore() == null) {
            return;
        }
        ItemMeta meta = currentItem.getItemMeta();
        List<String> lore = meta.getLore();
        if (millisLeft >= 0) {
            lore.set(lore.size()-1, tr("\u00a7cClick within \u00a79{0}\u00a7c to leave!", TimeUtil.millisAsString(millisLeft)));
        } else {
            lore.set(lore.size()-1, tr("\u00a7cClick to leave"));
        }
        meta.setLore(lore);
        currentItem.setItemMeta(meta);
        inventory.setItem(17, currentItem);
    }

    public Inventory createRestartGUI(Player player) {
        List<String> schemeNames = plugin.getIslandGenerator().getSchemeNames();
        int menuSize = (int) Math.ceil(getMaxSchemeIndex(schemeNames) / 9d)*9;
        Inventory menu = Bukkit.createInventory(null, menuSize, "\u00a79" + tr("Island Restart Menu"));
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.SIGN, 1);
        ItemMeta meta = menuItem.getItemMeta();
        meta.setDisplayName(tr("\u00a7a\u00a7lReturn to the main menu"));
        meta.setLore(lores);
        menuItem.setItemMeta(meta);
        menu.addItem(menuItem);
        lores.clear();

        updateRestartMenu(menu, player, schemeNames);
        if (plugin.getConfirmHandler().millisLeft(player, "/is restart") > 0) {
            updateRestartMenuTimer(player, menu);
        }
        return menu;
    }

    private void updateRestartMenu(Inventory menu, Player player, List<String> schemeNames) {
        ItemStack menuItem;
        ItemMeta meta;
        List<String> lores;
        int index = 1;
        for (String schemeName : schemeNames) {
            IslandPerk islandPerk = plugin.getPerkLogic().getIslandPerk(schemeName);
            boolean enabled = plugin.getConfig().getBoolean("island-schemes." + islandPerk.getSchemeName() + ".enabled", true);
            if (!enabled) {
                continue; // Skip
            }
            index = plugin.getConfig().getInt("island-schemes." + islandPerk.getSchemeName() + ".index", index);
            menuItem = islandPerk.getDisplayItem();
            meta = menuItem.getItemMeta();
            lores = meta.getLore();
            if (lores == null) {
                lores = new ArrayList<>();
            }
            if (hasPermission(player, islandPerk.getPermission())) {
                long millisLeft = plugin.getConfirmHandler().millisLeft(player, "/is restart");
                if (millisLeft > 0) {
                    addLore(lores, tr("\u00a7cClick within \u00a79{0}\u00a7c to restart!", TimeUtil.millisAsString(millisLeft)));
                } else {
                    addLore(lores, tr("\u00a7aClick to restart!"));
                }
            } else {
                addLore(lores, tr("\u00a7cNo access!\n\u00a77({0})", islandPerk.getPermission()));
            }
            meta.setLore(lores);
            menuItem.setItemMeta(meta);
            menu.setItem(index++, menuItem);
        }
        player.updateInventory();
    }

    private void onClickLogMenu(InventoryClickEvent event, Player p, int slotIndex) {
        event.setCancelled(true);
        if (slotIndex < 0 || slotIndex > 35) {
            return;
        }
        p.closeInventory();
        p.performCommand("island");
    }

    private void onClickChallengeMenu(InventoryClickEvent event, ItemStack currentItem, Player p, String inventoryName) {
        int slotIndex = event.getRawSlot();
        event.setCancelled(true);
        Matcher m = CHALLENGE_PAGE_HEADER.matcher(inventoryName);
        int page = 1;
        int max = challengeLogic.getTotalPages();
        if (m.find()) {
            page = Integer.parseInt(m.group("p"));
            max = Integer.parseInt(m.group("max"));
        }
        // Last row is pagination
        if (slotIndex >= CHALLENGE_PAGESIZE && slotIndex < CHALLENGE_PAGESIZE + COLS_PER_ROW
                && currentItem != null && currentItem.getType() != Material.AIR)
        {
            // Pagination
            p.closeInventory();
            p.openInventory(displayChallengeGUI(p, currentItem.getAmount()));
            return;
        }
        // If in action bar or anywhere else, just bail out
        if (slotIndex < 0 || slotIndex > CHALLENGE_PAGESIZE || isAirOrLocked(currentItem)) {
            return;
        }
        if ((slotIndex % 9) > 0) { // 0,9... are the rank-headers...
            p.closeInventory();
            if (currentItem.getItemMeta() != null) {
                String challenge = currentItem.getItemMeta().getDisplayName();
                String challengeName = stripFormatting(challenge);
                p.performCommand("c c " + challengeName);
            }
            p.openInventory(displayChallengeGUI(p, page));
        } else {
            p.closeInventory();
            if (slotIndex < (CHALLENGE_PAGESIZE/2)) { // Upper half
                if (page > 1) {
                    p.openInventory(displayChallengeGUI(p, page - 1));
                } else {
                    p.performCommand("island");
                }
            } else if (page < max) {
                p.openInventory(displayChallengeGUI(p, page + 1));
            } else {
                p.performCommand("island");
            }
        }
    }

    private boolean isAirOrLocked(ItemStack currentItem) {
        return currentItem != null && currentItem.getType() == Material.AIR ||
                currentItem != null && currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName().equals(tr("\u00a74\u00a7lLocked Challenge"));
    }

    private void onClickBiomeMenu(InventoryClickEvent event, ItemStack currentItem, Player p, int slotIndex) {
        event.setCancelled(true);
        if (slotIndex < 0 || slotIndex > 35) {
            return;
        }
        if (slotIndex == 0 && currentItem.getType() == Material.SIGN) {
            p.closeInventory();
            p.performCommand("island");
            return;
        }
        for (BiomeMenuItem biomeMenu : biomeMenus) {
            ItemStack menuIcon = biomeMenu.getIcon();
            if (currentItem.getType() == menuIcon.getType() && currentItem.getDurability() == menuIcon.getDurability()) {
                p.closeInventory();
                p.performCommand("island biome " + biomeMenu.getId());
                return;
            }
        }
    }

    private void onClickPermissionMenu(InventoryClickEvent event, ItemStack currentItem, Player p, String inventoryName, int slotIndex) {
        event.setCancelled(true);
        if (slotIndex < 0 || slotIndex > 35) {
            return;
        }
        IslandInfo islandInfo = plugin.getIslandInfo(p);
        if (!plugin.getIslandInfo(p).isLeader(p)) {
            p.closeInventory();
            p.openInventory(displayPartyGUI(p));
        }
        String[] playerPerm = inventoryName.split(" ");
        String pname = playerPerm[0];
        ItemStack skullItem = event.getInventory().getItem(1);
        if (skullItem != null && skullItem.getType().equals(Material.SKULL_ITEM)) {
            ItemMeta meta = skullItem.getItemMeta();
            if (meta instanceof SkullMeta) {
                pname = ((SkullMeta) meta).getOwner();
            }
        }
        for (PartyPermissionMenuItem item : permissionMenuItems) {
            if (currentItem.getType() == item.getIcon().getType()) {
                p.closeInventory();
                islandInfo.togglePerm(pname, item.getPerm());
                p.openInventory(displayPartyPlayerGUI(p, pname));
                return;
            }
        }
        if (currentItem.getType() == Material.SIGN) {
            p.closeInventory();
            p.openInventory(displayPartyGUI(p));
        } else {
            p.closeInventory();
            p.openInventory(displayPartyPlayerGUI(p, pname));
        }
    }

    private void onClickPartyMenu(InventoryClickEvent event, ItemStack currentItem, Player p, ItemMeta meta, SkullMeta skull, int slotIndex) {
        event.setCancelled(true);
        if (slotIndex < 0 || slotIndex > 35) {
            return;
        }
        if (meta == null || currentItem.getType() == Material.SIGN) {
            p.closeInventory();
            p.performCommand("island");
        } else if (skull != null && plugin.getIslandInfo(p).isLeader(p)) {
            p.closeInventory();
            p.openInventory(displayPartyPlayerGUI(p, skull.getOwner()));
        }
    }

    public List<PartyPermissionMenuItem> getPermissionMenuItems() {
        return permissionMenuItems;
    }
}
