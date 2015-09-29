package us.talabrek.ultimateskyblock.imports.impl;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

/**
 * Tab support for the registered importers.
 */
public class ImportTabCompleter extends AbstractTabCompleter {
    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        return uSkyBlock.getInstance().getPlayerImporter().getImporterNames();
    }
}
