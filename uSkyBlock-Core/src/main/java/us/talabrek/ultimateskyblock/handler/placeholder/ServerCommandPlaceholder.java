package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Replaces placeholders in server-commands
 */
public class ServerCommandPlaceholder extends TextPlaceholder implements Listener {
    @Override
    public boolean registerPlaceholder(uSkyBlock plugin, PlaceholderReplacer replacer) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return super.registerPlaceholder(plugin, replacer);
    }

    @EventHandler
    public void onCmd(ServerCommandEvent e) {
        String cmd = e.getCommand();
        String replacement = replacePlaceholders(e.getSender() instanceof Player ? (Player) e.getSender() : null, cmd);
        if (replacement != null && !cmd.equals(replacement)) {
            e.setCommand(cmd);
        }
    }
}
