package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.admin.AbstractIslandInfoCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

public class MobLimitCommand extends AbstractIslandInfoCommand {
    protected MobLimitCommand(String name, String permission, String description) {
        super("limits", "usb.island.limit", "show the islands limits");
    }

    @Override
    protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {

    }
}
