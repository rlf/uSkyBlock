package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Task for generating player-info-data after island has been formed.
 */
public class GenerateTask extends BukkitRunnable {
    private final uSkyBlock plugin;
    private final Player player;
    private final PlayerInfo pi;
    private final Location next;
    private final PlayerPerk playerPerk;
    private final String schematicName;
    boolean hasRun = false;

    public GenerateTask(uSkyBlock plugin, final Player player, final PlayerInfo pi, final Location next, PlayerPerk playerPerk, String schematicName) {
        this.plugin = plugin;
        this.player = player;
        this.pi = pi;
        this.next = next;
        this.playerPerk = playerPerk;
        this.schematicName = schematicName;
    }
    @Override
    public void run() {
        if (hasRun) {
            return;
        }
        next.getChunk().load();
        Perk perk = plugin.getPerkLogic().getIslandPerk(schematicName).getPerk();
        perk = perk.combine(playerPerk.getPerk());
        plugin.getIslandGenerator().setChest(next, perk);
        IslandInfo islandInfo = plugin.setNewPlayerIsland(pi, next);
        islandInfo.setSchematicName(schematicName);
        WorldGuardHandler.updateRegion(islandInfo);
        plugin.getCooldownHandler().resetCooldown(player, "restart", Settings.general_cooldownRestart);

        Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (pi != null) {
                            pi.setIslandGenerating(false);
                        }
                        plugin.clearPlayerInventory(player);
                        if (player != null && player.isOnline()) {
                            if (plugin.getConfig().getBoolean("options.restart.teleportWhenReady", true)) {
                                player.sendMessage(tr("\u00a7aCongratulations! \u00a7eYour island has appeared."));
                                if (AsyncWorldEditHandler.isAWE()) {
                                    player.sendMessage(tr("\u00a7cNote:\u00a7e Construction might still be ongoing."));
                                }
                                plugin.homeTeleport(player, true);
                            } else {
                                player.sendMessage(new String[]{
                                        tr("\u00a7aCongratulations! \u00a7eYour island has appeared."),
                                        tr("Use \u00a79/is h\u00a7r or the \u00a79/is\u00a7r menu to go there."),
                                        tr("\u00a7cNote:\u00a7e Construction might still be ongoing.")});
                            }
                        }
                        for (String command : plugin.getConfig().getStringList("options.restart.extra-commands")) {
                            plugin.execCommand(player, command, true);
                        }
                    }
                }, plugin.getConfig().getInt("options.restart.teleportDelay", 40)
        );
    }
}

