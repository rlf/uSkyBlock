package us.talabrek.ultimateskyblock.handler.placeholder;

import dk.lockfuglsang.minecraft.file.FileUtil;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class PlaceholderHandler {
    private static final String[] ADAPTORS = {
            ChatPlaceholder.class.getName(),
            ServerCommandPlaceholder.class.getName(),
            "us.talabrek.ultimateskyblock.handler.placeholder.MVdWPlaceholder"
    };

    public static void register(uSkyBlock plugin) {
        PlaceholderReplacerImpl placeholderReplacer = new PlaceholderReplacerImpl(plugin);
        for (String className : ADAPTORS) {
            String baseName = FileUtil.getExtension(className);
            if (plugin.getConfig().getBoolean("placeholder." + baseName.toLowerCase(), false)) {
                try {
                    Class<?> aClass = Class.forName(className);
                    Object o = aClass.newInstance();
                    if (o instanceof PlaceholderAPI) {
                        if (((PlaceholderAPI) o).registerPlaceholder(plugin, placeholderReplacer)) {
                            plugin.getLogger().info("uSkyBlock hooked into " + baseName);
                        } else {
                            plugin.getLogger().info("uSkyBlock failed to hook into " + baseName);
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    // Ignore
                    plugin.getLogger().info("uSkyBlock failed to hook into " + baseName);
                }
            }
        }
    }
}
