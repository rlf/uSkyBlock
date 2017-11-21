package us.talabrek.ultimateskyblock.command.completion;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.menu.PartyPermissionMenuItem;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

public class PermissionTabCompleter extends AbstractTabCompleter {
    private uSkyBlock plugin;

    public PermissionTabCompleter(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        List<String> list = new ArrayList<>();
        for (PartyPermissionMenuItem item : plugin.getMenu().getPermissionMenuItems()) {
            list.add(item.getPerm());
        }
        return list;
    }

}
