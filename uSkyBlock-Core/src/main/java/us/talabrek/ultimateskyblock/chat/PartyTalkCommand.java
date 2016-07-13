package us.talabrek.ultimateskyblock.chat;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Talk to your party
 */
public class PartyTalkCommand extends IslandChatCommand {

    public PartyTalkCommand(uSkyBlock plugin, ChatLogic chatLogic) {
        super(plugin, chatLogic, "partytalk|ptalk|ptk", "usb.party.talk", I18nUtil.tr("talk to your island party"));
    }
}
