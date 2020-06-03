package us.talabrek.ultimateskyblock.chat;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.handler.placeholder.PlaceholderHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import dk.lockfuglsang.minecraft.util.FormatUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import static us.talabrek.ultimateskyblock.api.event.IslandChatEvent.*;

/**
 * The primary logic of uSkyBlocks chat-handling
 */
public class ChatLogic {
    private static final List<String> ALONE_MESSAGES = Arrays.asList(
        I18nUtil.tr("But you are ALLLLLLL ALOOOOONE!"),
        I18nUtil.tr("But you are Yelling in the wind!"),
        I18nUtil.tr("But your fantasy friends are gone!"),
        I18nUtil.tr("But you are Talking to your self!")
    );
    private final uSkyBlock plugin;
    private final Map<Type, String> formats = new EnumMap<>(Type.class);
    private final Map<UUID, Type> toggled = new HashMap<>();

    public ChatLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        formats.put(Type.PARTY,
            plugin.getConfig().getString("options.party.chat-format", "&9PARTY &r{DISPLAYNAME} &f>&d {MESSAGE}"));
        formats.put(Type.ISLAND,
            plugin.getConfig().getString("options.island.chat-format", "&9SKY &r{DISPLAYNAME} &f>&b {MESSAGE}"));
    }

    /**
     * Gets a {@link List} containing {@link Player}'s with all the recipients that should receive the given message
     * {@link Type} from the sending {@link Player}. Returns an empty list when there are no recipients.
     * @param sender   Player sending the message.
     * @param chatType Message type that the player is sending.
     * @return List of all recipients, or an empty list if there are none.
     */
    public @NotNull List<Player> getRecipients(Player sender, Type chatType) {
        if (chatType == Type.PARTY) {
            IslandInfo islandInfo = plugin.getIslandInfo(sender);
            return islandInfo != null ? islandInfo.getOnlineMembers() : Collections.singletonList(sender);
        } else if (chatType == Type.ISLAND) {
            if (plugin.getWorldManager().isSkyWorld(sender.getWorld())) {
                return WorldGuardHandler.getPlayersInRegion(plugin.getWorldManager().getWorld(),
                    WorldGuardHandler.getIslandRegionAt(sender.getLocation()));
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    /**
     * Sends the given message to all online partymembers or island visitors on the given {@link Player}'s island,
     * depending on the given {@link Type}.
     * @param sender  Player sending the message.
     * @param type    Message type to send.
     * @param message Message to send.
     */
    public void sendMessage(Player sender, Type type, String message) {
        String format = getFormat(type);
        format = FormatUtil.normalize(format);
        format = format.replaceAll("\\{DISPLAYNAME}", Matcher.quoteReplacement(sender.getDisplayName()));
        String msg = format.replaceAll("\\{MESSAGE}", Matcher.quoteReplacement(message));
        msg = PlaceholderHandler.replacePlaceholders(sender, msg);
        List<Player> onlineMembers = getRecipients(sender, type);
        if (onlineMembers.size() <= 1) {
            sender.sendMessage(I18nUtil.tr("\u00a7cSorry! {0}", "\u00a79" +
                ALONE_MESSAGES.get(((int) Math.round(Math.random() * ALONE_MESSAGES.size())) % ALONE_MESSAGES.size())));
        } else {
            for (Player member : onlineMembers) {
                member.sendMessage(msg);
            }
        }
    }

    /**
     * Gets the message format for the given {@link Type}.
     * @param type Island chat type to lookup.
     * @return Message format.
     */
    public @NotNull String getFormat(Type type) {
        return formats.get(type);
    }

    /**
     * Toggle the {@link Type} on or off for the given {@link Player}, returns true if it is toggled on.
     * @param player Player to toggle the chat type for.
     * @param type   Chat type to toggle.
     * @return True if it is toggled on, false otherwise.
     */
    public synchronized boolean toggle(Player player, Type type) {
        Type oldType = toggled.get(player.getUniqueId());
        if (oldType == type) {
            toggled.remove(player.getUniqueId());
            return false;
        } else {
            toggled.put(player.getUniqueId(), type);
        }
        return true;
    }

    /**
     * Gets the current {@link Type} toggle for the given {@link Player}, or null if none exists.
     * @param player Player to lookup.
     * @return The current Type toggle, or null if none exists.
     */
    public synchronized @Nullable Type getToggle(Player player) {
        return toggled.get(player.getUniqueId());
    }
}
