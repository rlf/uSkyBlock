package us.talabrek.ultimateskyblock.signs;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import us.talabrek.ultimateskyblock.uSkyBlock;


import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;

/**
 * Handles USB Signs
 */
public class SignEvents implements Listener {
    private static final Logger log = Logger.getLogger(SignEvents.class.getName());
    private final uSkyBlock plugin;
    private final SignLogic logic;

    public SignEvents(uSkyBlock plugin, SignLogic logic) {
        this.plugin = plugin;
        this.logic = logic;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHitSign(PlayerInteractEvent e) {
        if (e.isCancelled()
                || (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK)
                || e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.WALL_SIGN
                || !(e.getClickedBlock().getState() instanceof Sign)
                || !hasPermission(e.getPlayer(), "usb.island.signs.use")
                || e.getPlayer() == null || !plugin.isSkyAssociatedWorld(e.getPlayer().getWorld())
                || !(plugin.playerIsOnOwnIsland(e.getPlayer()))
                ) {
            return;
        }
        logic.signClicked(e.getPlayer(), e.getClickedBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChanged(SignChangeEvent e) {
        if (e.isCancelled() || e.getPlayer() == null
                || !plugin.isSkyAssociatedWorld(e.getPlayer().getWorld())
                || !e.getLines()[0].equalsIgnoreCase("[usb]")
                || e.getLines()[1].trim().isEmpty()
                || !hasPermission(e.getPlayer(), "usb.island.signs.place")
                || !(e.getBlock().getType() == Material.WALL_SIGN)
                || !(e.getBlock().getState() instanceof Sign)
                ) {
            return;
        }
        Sign sign = (Sign) e.getBlock().getState();
        org.bukkit.material.Sign data = (org.bukkit.material.Sign) sign.getData();
        Block wallBlock = sign.getBlock().getRelative(data.getAttachedFace());
        if (wallBlock != null && wallBlock.getType() == Material.CHEST && wallBlock.getState() instanceof Chest) {
            logic.addSign(sign, e.getLines(), (Chest) wallBlock.getState());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChestChanged(InventoryMoveItemEvent e) {
        if (e.isCancelled()
                || e.getSource() == null || e.getSource().getLocation() == null
                || e.getDestination() == null || e.getDestination().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getSource().getLocation().getWorld())
                || !plugin.isSkyAssociatedWorld(e.getDestination().getLocation().getWorld())
                ) {
            return;
        }
        logic.updateSigns(e.getSource().getLocation(), e.getDestination().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChestClosed(InventoryCloseEvent e) {
        if (e.getPlayer() == null || e.getPlayer().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getPlayer().getLocation().getWorld())
                ) {
            return;
        }
        logic.updateSigns(e.getInventory().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignBreak(BlockBreakEvent e) {
        if (e.isCancelled()
                || e.getBlock() == null
                || (e.getBlock().getType() != Material.WALL_SIGN && e.getBlock().getType() != Material.CHEST)
                || e.getBlock().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getBlock().getLocation().getWorld())
                ) {
            return;
        }
        if (e.getBlock().getType() == Material.CHEST) {
            logic.removeChest(e.getBlock().getLocation());
        } else {
            logic.removeSign(e.getBlock().getLocation());
        }
    }
}
