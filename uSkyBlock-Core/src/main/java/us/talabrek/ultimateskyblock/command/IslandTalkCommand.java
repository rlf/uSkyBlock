package us.talabrek.ultimateskyblock.command;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Collections;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Island Talk
 */
public class IslandTalkCommand extends IslandChatCommand {
    private final uSkyBlock plugin;

    public IslandTalkCommand(uSkyBlock plugin) {
        super(plugin, "islandtalk|istalk|it", "usb.island.talk", tr("talk to players on your island"));
        this.plugin = plugin;
    }
    @Override
    protected List<Player> getRecipients(Player player, us.talabrek.ultimateskyblock.api.IslandInfo islandInfo) {
        if (plugin.isSkyWorld(player.getWorld())) {
            return WorldGuardHandler.getPlayersInRegion(plugin.getWorld(), WorldGuardHandler.getIslandRegionAt(player.getLocation()));
        }
        return Collections.emptyList();
    }

    @Override
    protected String getFormat() {
        return plugin.getConfig().getString("options.island.chat-format", "&9SKY &r{DISPLAYNAME} &f>&b {MESSAGE}");
    }
}
