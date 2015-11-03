package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.admin.*;
import us.talabrek.ultimateskyblock.command.common.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.ChallengeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;
import us.talabrek.ultimateskyblock.handler.ConfirmHandler;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;

/**
 * The new admin command, alias /usb
 */
public class AdminCommand extends AbstractCommandExecutor {
    public AdminCommand(final uSkyBlock plugin, ConfirmHandler confirmHandler) {
        super("usb", "usb.admin", I18nUtil.tr("Ultimate SkyBlock Admin"));
        OnlinePlayerTabCompleter playerCompleter = new OnlinePlayerTabCompleter();
        TabCompleter challengeCompleter = new ChallengeTabCompleter();
        TabCompleter allPlayerCompleter = new AllPlayerTabCompleter(playerCompleter);
        TabCompleter biomeCompleter = new BiomeTabCompleter();
        addTab("oplayer", playerCompleter);
        addTab("player", allPlayerCompleter);
        addTab("island", allPlayerCompleter);
        addTab("leader", allPlayerCompleter);
        addTab("challenge", challengeCompleter);
        addTab("biome", biomeCompleter);
        add(new ReloadCommand());
        add(new ImportCommand());
        add(new GenTopTenCommand(plugin));
        //add(new RegisterIslandToPlayerCommand());
        add(new AdminChallengeCommand(plugin, challengeCompleter));
        add(new OrphanCommand(plugin));
        add(new AdminIslandCommand(plugin, confirmHandler));
        add(new PurgeCommand(plugin));
        add(new GotoIslandCommand(plugin));
        add(new AbstractPlayerInfoCommand("info", "usb.admin.info", I18nUtil.tr("show player-information")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo) {
                sender.sendMessage(playerInfo.toString());
            }
        });
        add(new FlatlandFixCommand(plugin));
        add(new DebugCommand(plugin));
        add(new WGCommand(plugin));
        add(new VersionCommand(plugin));
            add(new CooldownCommand(plugin));
        add(new PerkCommand(plugin));
        add(new LanguageCommand(plugin));
        add(new FlushCommand(plugin));
        add(new JobsCommand(plugin));
        add(new ConfigGUICommand(plugin));
        add(new DocumentCommand(plugin));
    }
}
