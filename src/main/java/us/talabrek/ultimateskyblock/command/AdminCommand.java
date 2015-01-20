package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.admin.*;
import us.talabrek.ultimateskyblock.command.common.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.ChallengeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * The new admin command, alias /usb
 */
public class AdminCommand extends AbstractCommandExecutor {
    public AdminCommand(uSkyBlock plugin) {
        super("usb", "usb.admin", "Ultimate SkyBlock Admin");
        TabCompleter playerCompleter = new OnlinePlayerTabCompleter();
        TabCompleter challengeCompleter = new ChallengeTabCompleter();
        TabCompleter allPlayerCompleter = new AllPlayerTabCompleter();
        TabCompleter biomeCompleter = new BiomeTabCompleter();
        addTab("oplayer", playerCompleter);
        addTab("player", allPlayerCompleter);
        addTab("challenge", challengeCompleter);
        addTab("biome", biomeCompleter);
        add(new ReloadCommand());
        add(new ImportCommand());
        add(new GenTopTenCommand());
        add(new RegisterIslandToPlayerCommand());
        add(new AdminChallengeCommand(plugin, challengeCompleter));
        add(new OrphanCommand());
        add(new AdminIslandCommand());
        add(new PurgeCommand(plugin));
        add(new GotoIslandCommand(plugin));
        add(new AbstractPlayerInfoCommand("info", null, "show player-information") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo) {
                sender.sendMessage(playerInfo.toString());
            }
        });
        add(new FlatlandFixCommand(plugin));
    }
}
