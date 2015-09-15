package us.talabrek.ultimateskyblock.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Island Talk
 */
public class IslandTalkCommand extends IslandChatCommand {
    private final uSkyBlock plugin;

    public IslandTalkCommand(uSkyBlock plugin) {
        super(plugin, "islandtalk|istalk|it", "usb.island.talk", tr("talk to your island (party+trustees)"));
        this.plugin = plugin;
    }
    @Override
    protected List<Player> getRecipients(IslandInfo islandInfo) {
        List<Player> onlineMembers = islandInfo.getOnlineMembers();
        for (String trustee : islandInfo.getTrustees()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trustee);
            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                onlineMembers.add(offlinePlayer.getPlayer());
            }
        }
        return onlineMembers;
    }

    @Override
    protected String getFormat() {
        return plugin.getConfig().getString("options.island.chat-format", "&9SKY &r{DISPLAYNAME} &f>&b {MESSAGE}");
    }
}
