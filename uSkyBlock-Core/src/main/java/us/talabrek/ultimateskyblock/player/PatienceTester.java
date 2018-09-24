package us.talabrek.ultimateskyblock.player;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class PatienceTester {
    private static final long maxRunning = 30000;

    public static boolean isRunning(Player player, String key) {
        if (player.hasMetadata(key)) {
            List<MetadataValue> metadata = player.getMetadata(key);
            MetadataValue metadataValue = metadata.size() > 0 ? metadata.get(0) : null;
            if (metadataValue == null || metadataValue.asLong() < System.currentTimeMillis()) {
                player.removeMetadata(key, uSkyBlock.getInstance());
                return false;
            }
            player.sendMessage(getMessage());
            return true;
        }
        return false;
    }

    private static String getMessage() {
        String[] messages = new String[] {
                tr("\u00a79Hold your horses! You have to be patient..."),
                tr("\u00a79Not really patient, are you?"),
                tr("\u00a79Be patient, young padawan"),
                tr("\u00a79Patience you MUST have, young padawan"),
                tr("\u00a79The two most powerful warriors are patience and time."),
        };
        return messages[(int)Math.floor(Math.random() * messages.length)];
    }

    public static void startRunning(Player player, String key) {
        player.setMetadata(key, new FixedMetadataValue(uSkyBlock.getInstance(), System.currentTimeMillis() + maxRunning));
    }

    public static void stopRunning(Player player, String key) {
        player.removeMetadata(key, uSkyBlock.getInstance());
    }
}
