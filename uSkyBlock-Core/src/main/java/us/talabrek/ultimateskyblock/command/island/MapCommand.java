package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Does either CreateCommand or HomeCommand depending on state.
 */
public class MapCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public MapCommand(uSkyBlock plugin) {
        super("map", "usb.island.create", tr("opens the map GUI"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (sender instanceof Player) {
        	Player player = (Player) sender;
        	player.openInventory(plugin.getMenu().createLocationPickerMenu(player, null, false, 0, 0));
            return true;
        }
        return false;
    }
}
