package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

public class LogCommand extends RequireIslandCommand {
    private final SkyBlockMenu menu;

    public LogCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super(plugin, "log|l", "usb.island.log", marktr("display log"));
        this.menu = menu;
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        player.openInventory(menu.displayLogGUI(player));
        return true;
    }
}
