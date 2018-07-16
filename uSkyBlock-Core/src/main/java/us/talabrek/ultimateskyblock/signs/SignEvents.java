package us.talabrek.ultimateskyblock.signs;

import dk.lockfuglsang.minecraft.util.FormatUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.Logger;

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
                || e.getPlayer() == null
                || (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
                || e.getClickedBlock() == null
                || e.getClickedBlock().getType() != Material.WALL_SIGN
                || !(e.getClickedBlock().getState() instanceof Sign)
                || !e.getPlayer().hasPermission("usb.island.signs.use")
                || !plugin.isSkyAssociatedWorld(e.getPlayer().getWorld())
                || !(plugin.playerIsOnOwnIsland(e.getPlayer()))
                ) {
            return;
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            logic.updateSign(e.getClickedBlock().getLocation());
        } else {
            logic.signClicked(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChanged(SignChangeEvent e) {
        if (e.isCancelled() || e.getPlayer() == null
                || !plugin.isSkyAssociatedWorld(e.getPlayer().getWorld())
                || !e.getLines()[0].equalsIgnoreCase("[usb]")
                || e.getLines()[1].trim().isEmpty()
                || !e.getPlayer().hasPermission("usb.island.signs.place")
                || !(e.getBlock().getType() == Material.WALL_SIGN)
                || !(e.getBlock().getState() instanceof Sign)
                ) {
            return;
        }
        Sign sign = (Sign) e.getBlock().getState();
        org.bukkit.material.Sign data = (org.bukkit.material.Sign) sign.getData();
        Block wallBlock = sign.getBlock().getRelative(data.getAttachedFace());
        if (isChest(wallBlock)) {
            logic.addSign(sign, e.getLines(), (Chest) wallBlock.getState());
        }
    }

    private boolean isChest(Block wallBlock) {
        return wallBlock != null
                && (wallBlock.getType() == Material.CHEST || wallBlock.getType() == Material.TRAPPED_CHEST)
                && wallBlock.getState() instanceof Chest;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryMovedEvent(InventoryMoveItemEvent e) {
        if (e.getDestination() == null
                || e.getDestination().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getDestination().getLocation().getWorld())) {
            return;
        }
        if (e.getDestination().getType() == InventoryType.CHEST) {
            Location loc = e.getDestination().getLocation();
            if (loc != null) {
                logic.updateSignsOnContainer(loc);
            }
        }
        if (e.getSource().getType() == InventoryType.CHEST) {
            Location loc = e.getSource().getLocation();
            if (loc != null) {
                logic.updateSignsOnContainer(loc);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChestClosed(InventoryCloseEvent e) {
        if (e.getPlayer() == null
                || e.getPlayer().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getPlayer().getLocation().getWorld())
                || e.getInventory().getType() != InventoryType.CHEST
                )
        {
            return;
        }
        Location loc = e.getInventory().getLocation();
        if (loc != null) {
            logic.updateSignsOnContainer(loc);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignOrChestBreak(BlockBreakEvent e) {
        if (e.isCancelled()
                || e.getBlock() == null
                || (e.getBlock().getType() != Material.WALL_SIGN && !(e.getBlock().getType() == Material.CHEST || e.getBlock().getType() == Material.TRAPPED_CHEST))
                || e.getBlock().getLocation() == null
                || !plugin.isSkyAssociatedWorld(e.getBlock().getLocation().getWorld())
                ) {
            return;
        }
        if (e.getBlock().getType() == Material.WALL_SIGN) {
            logic.removeSign(e.getBlock().getLocation());
        } else {
            logic.removeChest(e.getBlock().getLocation());
        }
    }

    private boolean isSign(Material material) {
        return material == Material.WALL_SIGN || material == Material.SIGN_POST;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockHit(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled()
                || e.getPlayer() == null
                || e.getClickedBlock() == null
                || e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getPlayer().getGameMode() != GameMode.SURVIVAL
                || !isSign(e.getClickedBlock().getType())
                || !player.hasPermission("usb.island.signs.use")
                || !plugin.isSkyAssociatedWorld(player.getWorld())) {
            return;
        }

        if (e.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            String firstLine = FormatUtil.stripFormatting(sign.getLine(0)).trim();
            if (firstLine.startsWith("/")) {
                e.setCancelled(true);
                player.performCommand(firstLine.substring(1));
            }
        }
    }
}
