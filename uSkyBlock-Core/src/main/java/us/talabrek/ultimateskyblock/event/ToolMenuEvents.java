package us.talabrek.ultimateskyblock.event;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.challenge.Challenge;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Events triggering the tool-menu
 */
public class ToolMenuEvents implements Listener {

    public static final String COMPLETE_CHALLENGE_CMD = "challenges complete ";
    private final uSkyBlock plugin;
    private final ItemStack tool;
    private final Map<String,String> commandMap = new HashMap<>();

    public ToolMenuEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        tool = ItemStackUtil.createItemStack(plugin.getConfig().getString("tool-menu.tool", "SAPLING"));
        registerChallenges();
        registerCommands();
    }

    private void registerCommands() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tool-menu.commands");
        if (section != null) {
            for (String block : section.getKeys(false)) {
                ItemStack item = ItemStackUtil.createItemStack(block);
                if (item.getType().isBlock() && section.isString(block)) {
                    commandMap.put(ItemStackUtil.asString(item), section.getString(block));
                }
            }
        }
    }

    private void registerChallenges() {
        for (String challengeName : plugin.getChallengeLogic().getAllChallengeNames()) {
            Challenge challenge = plugin.getChallengeLogic().getChallenge(challengeName);
            ItemStack displayItem = challenge != null ? challenge.getDisplayItem() : null;
            ItemStack toolItem = challenge != null && challenge.getTool() != null ? ItemStackUtil.createItemStack(challenge.getTool()) : null;
            if (toolItem != null) {
                commandMap.put(ItemStackUtil.asString(toolItem), COMPLETE_CHALLENGE_CMD + challengeName);
            } else if (displayItem != null && displayItem.getType().isBlock()) {
                commandMap.put(ItemStackUtil.asString(displayItem), COMPLETE_CHALLENGE_CMD + challengeName);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockHit(PlayerInteractEvent e) {
        if (e == null || e.getClickedBlock() == null
                || e.getAction() != Action.LEFT_CLICK_BLOCK || e.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Player player = e.getPlayer();
        if (!plugin.getWorldManager().isSkyAssociatedWorld(player.getWorld()) || !isTool(e.getItem())) {
            return;
        }

        // We are in a skyworld, a block has been hit, with the tool
        Material block = e.getClickedBlock().getType();
        short data = e.getClickedBlock().getData();
        String itemId = ItemStackUtil.asString(new ItemStack(block, 1, data));
        if (commandMap.containsKey(itemId)) {
            doCmd(e, player, itemId);
        }
        if (!e.isCancelled()) {
            itemId = ItemStackUtil.asString(new ItemStack(block, 1));
            if (commandMap.containsKey(itemId)) {
                doCmd(e, player, itemId);
            }
        }
    }

    private void doCmd(PlayerInteractEvent e, Player player, String itemId) {
        String command = commandMap.get(itemId);
        if (command.startsWith(COMPLETE_CHALLENGE_CMD)) {
            String challengeName = command.substring(COMPLETE_CHALLENGE_CMD.length());
            if (plugin.getChallengeLogic().getAvailableChallengeNames(plugin.getPlayerInfo(player)).contains(challengeName)) {
                e.setCancelled(true);
                plugin.execCommand(player, command, true);
            }
        } else {
            e.setCancelled(true);
            plugin.execCommand(player, command, false);
        }
    }

    private boolean isTool(ItemStack item) {
        return item != null && tool != null && item.getType() == tool.getType() && item.getDurability() == tool.getDurability();
    }
}
