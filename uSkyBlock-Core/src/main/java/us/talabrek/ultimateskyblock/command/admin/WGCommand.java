package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.island.RequirePlayerCommand;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Some hooks into the WG Handler
 */
public class WGCommand extends CompositeCommand {
    private final uSkyBlock plugin;

    public WGCommand(final uSkyBlock plugin) {
        super("wg", "usb.admin.wg", marktr("various WorldGuard utilities"));
        this.plugin = plugin;
        add(new RequirePlayerCommand("refresh", null, marktr("refreshes the chunks around the player")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                WorldEditHandler.refreshRegion(player.getLocation());
                player.sendMessage(I18nUtil.tr("\u00a7eResending chunks to the client"));
                return true;
            }
        });
        add(new RequirePlayerCommand("load", null, marktr("load the region chunks")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                WorldEditHandler.loadRegion(player.getLocation());
                player.sendMessage(tr("\u00a7eLoading chunks at {0}", LocationUtil.asString(player.getLocation())));
                return true;
            }
        });
        add(new RequirePlayerCommand("unload", null, marktr("load the region chunks")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                LocationUtil.loadChunkAt(player.getLocation());
                player.sendMessage(tr("\u00a7eUnloading chunks at {0}", LocationUtil.asString(player.getLocation())));
                return true;
            }
        });
        add(new RequirePlayerCommand("update", null, marktr("update the WG regions")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                String island = WorldGuardHandler.getIslandNameAt(player.getLocation());
                if (island != null) {
                    IslandInfo islandInfo = plugin.getIslandInfo(island);
                    if (islandInfo != null) {
                        WorldGuardHandler.updateRegion(islandInfo);
                        player.sendMessage(tr("\u00a7eIsland world-guard regions updated for {0}", island));
                    } else {
                        player.sendMessage(tr("\u00a7eNo island found at your location!"));
                    }
                }
                return true;
            }
        });
        add(new RequirePlayerCommand("chunk", null, marktr("refreshes the chunk around the player")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                World world = player.getWorld();
                world.refreshChunk(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
                player.sendMessage(I18nUtil.tr("\u00a7eResending chunks to the client"));
                return true;
            }
        });
    }
}
