package us.talabrek.ultimateskyblock.event;

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
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Events triggering the tool-menu
 */
public class ToolMenuEvents implements Listener {

    private final uSkyBlock plugin;
    private final ItemStack tool;
    private final Map<Material,String> commandMap = new HashMap<>();

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
                    commandMap.put(item.getType(), section.getString(block));
                }
            }
        }
    }

    private void registerChallenges() {
        for (String challengeName : plugin.getChallengeLogic().getAllChallengeNames()) {
            Challenge challenge = plugin.getChallengeLogic().getChallenge(challengeName);
            ItemStack displayItem = challenge != null ? challenge.getDisplayItem() : null;
            if (displayItem != null && displayItem.getType() != null && displayItem.getType().isBlock()) {
                commandMap.put(displayItem.getType(), "challenges complete " + challengeName);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockHit(PlayerInteractEvent e) {
        if (e == null || e.isCancelled() || e.getPlayer() == null || e.getClickedBlock() == null
                || e.getAction() != Action.LEFT_CLICK_BLOCK || e.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Player player = e.getPlayer();
        if (!plugin.isSkyAssociatedWorld(player.getWorld()) || !isTool(e.getItem())) {
            return;
        }
        // We are in a skyworld, a block has been hit, with the tool
        Material block = e.getClickedBlock().getType();
        if (commandMap.containsKey(block)) {
            plugin.execCommand(player, commandMap.get(block), false);
        }
    }

    private boolean isTool(ItemStack item) {
        return item != null && tool != null && item.getType() == tool.getType() && item.getDurability() == tool.getDurability();
    }
}
