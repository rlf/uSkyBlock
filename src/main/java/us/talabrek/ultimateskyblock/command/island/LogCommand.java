package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.SkyBlockMenu;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class LogCommand extends RequireIslandCommand {
    private final SkyBlockMenu menu;

    public LogCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super(plugin, "log|l", "usb.island.create", "display log");
        this.menu = menu;
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        player.openInventory(menu.displayLogGUI(player));
        return true;
    }
}
