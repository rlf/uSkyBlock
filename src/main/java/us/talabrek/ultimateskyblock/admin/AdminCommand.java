package us.talabrek.ultimateskyblock.admin;

import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.admin.island.AdminIslandCommand;
import us.talabrek.ultimateskyblock.command.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.ChallengeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;

/**
 * The new admin command, alias /usb
 */
public class AdminCommand extends AbstractCommandExecutor {
    public AdminCommand() {
        super("usb", "usb.admin", "Ultimate SkyBlock Admin");
        TabCompleter playerCompleter = new OnlinePlayerTabCompleter();
        TabCompleter challengeCompleter = new ChallengeTabCompleter();
        TabCompleter allPlayerCompleter = new AllPlayerTabCompleter();
        TabCompleter biomeCompleter = new BiomeTabCompleter();
        addTab("player", playerCompleter);
        addTab("aplayer", allPlayerCompleter);
        addTab("challenge", challengeCompleter);
        addTab("biome", biomeCompleter);
        add(new ReloadCommand());
        add(new ImportCommand());
        add(new GenTopTenCommand());
        add(new RegisterIslandToPlayerCommand());
        add(new AdminChallengeCommand());
        add(new OrphanCommand());
        add(new AdminIslandCommand());
    }
}
