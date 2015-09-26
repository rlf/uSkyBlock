package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.command.island.RequirePlayerCommand;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Some hooks into the WG Handler
 */
public class WGCommand extends CompositeUSBCommand {
    private final uSkyBlock plugin;

    public WGCommand(uSkyBlock plugin) {
        super("wg", "usb.admin.wg", tr("various WorldGuard utilities"));
        this.plugin = plugin;
        add(new RequirePlayerCommand("refresh", null, tr("refreshes the chunks around the player")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                WorldEditHandler.refreshRegion(player.getLocation());
                player.sendMessage(tr("\u00a7eResending chunks to the client"));
                return true;
            }
        });
        add(new RequirePlayerCommand("load", null, tr("load the region chunks")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                WorldEditHandler.loadRegion(player.getLocation());
                player.sendMessage(tr("\u00a7eLoading chunks at {0}", LocationUtil.asString(player.getLocation())));
                return true;
            }
        });
        add(new RequirePlayerCommand("unload", null, tr("load the region chunks")) {
            @Override
            protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
                LocationUtil.loadChunkAt(player.getLocation());
                player.sendMessage(tr("\u00a7eUnloading chunks at {0}", LocationUtil.asString(player.getLocation())));
                return true;
            }
        });
    }
}
