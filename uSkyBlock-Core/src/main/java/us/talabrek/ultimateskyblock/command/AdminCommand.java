package us.talabrek.ultimateskyblock.command;

import dk.lockfuglsang.minecraft.command.AbstractCommandExecutor;
import dk.lockfuglsang.minecraft.command.DocumentCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.admin.AbstractPlayerInfoCommand;
import us.talabrek.ultimateskyblock.command.admin.AdminChallengeCommand;
import us.talabrek.ultimateskyblock.command.admin.AdminIslandCommand;
import us.talabrek.ultimateskyblock.command.admin.ConfigGUICommand;
import us.talabrek.ultimateskyblock.command.admin.CooldownCommand;
import us.talabrek.ultimateskyblock.command.admin.DebugCommand;
import us.talabrek.ultimateskyblock.command.admin.FlatlandFixCommand;
import us.talabrek.ultimateskyblock.command.admin.FlushCommand;
import us.talabrek.ultimateskyblock.command.admin.GenTopTenCommand;
import us.talabrek.ultimateskyblock.command.admin.GotoIslandCommand;
import us.talabrek.ultimateskyblock.command.admin.ImportCommand;
import us.talabrek.ultimateskyblock.command.admin.JobsCommand;
import us.talabrek.ultimateskyblock.command.admin.LanguageCommand;
import us.talabrek.ultimateskyblock.command.admin.OrphanCommand;
import us.talabrek.ultimateskyblock.command.admin.PerkCommand;
import us.talabrek.ultimateskyblock.command.admin.PurgeCommand;
import us.talabrek.ultimateskyblock.command.admin.ReloadCommand;
import us.talabrek.ultimateskyblock.command.admin.VersionCommand;
import us.talabrek.ultimateskyblock.command.admin.WGCommand;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.ChallengeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.RankTabCompleter;
import us.talabrek.ultimateskyblock.handler.ConfirmHandler;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

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
        addTab("rank", new RankTabCompleter(plugin));
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
        add(new DocumentCommand(plugin, "doc", "usb.admin.doc"));
    }
}
