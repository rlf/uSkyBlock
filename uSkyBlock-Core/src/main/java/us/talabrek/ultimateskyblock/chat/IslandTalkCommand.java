package us.talabrek.ultimateskyblock.chat;

import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Island Talk
 */
public class IslandTalkCommand extends IslandChatCommand {

    public IslandTalkCommand(uSkyBlock plugin, ChatLogic chatLogic) {
        super(plugin, chatLogic, "islandtalk|istalk|it", "usb.island.talk", tr("talk to players on your island"));
    }
}
