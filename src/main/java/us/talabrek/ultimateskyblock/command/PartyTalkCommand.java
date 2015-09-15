package us.talabrek.ultimateskyblock.command;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Talk to your party
 */
public class PartyTalkCommand extends IslandChatCommand {
    private final uSkyBlock plugin;

    public PartyTalkCommand(uSkyBlock plugin) {
        super(plugin, "partytalk|ptalk|pt", "usb.party.talk", tr("talk to your island party"));
        this.plugin = plugin;
    }

    @Override
    protected List<Player> getRecipients(IslandInfo islandInfo) {
        return islandInfo.getOnlineMembers();
    }

    @Override
    protected String getFormat() {
        return plugin.getConfig().getString("options.party.chat-format", "&9PARTY &r{DISPLAYNAME} &f>&d {MESSAGE}");
    }
}
