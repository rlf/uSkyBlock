package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.SkyBlockMenu;
import us.talabrek.ultimateskyblock.command.common.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.MemberTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.island.*;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The main /island command
 */
public class IslandCommand extends AbstractCommandExecutor {
    private final uSkyBlock plugin;
    private final SkyBlockMenu menu;

    public IslandCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super("island|is", "usb.island.create", tr("general island command"));
        this.plugin = plugin;
        this.menu = menu;
        InviteHandler inviteHandler = new InviteHandler(plugin);
        AllPlayerTabCompleter playerTabCompleter = new AllPlayerTabCompleter();
        addTab("island", playerTabCompleter);
        addTab("player", playerTabCompleter);
        addTab("oplayer", new OnlinePlayerTabCompleter());
        addTab("biome", new BiomeTabCompleter());
        addTab("member", new MemberTabCompleter(plugin));
        add(new RestartCommand(plugin));
        add(new LogCommand(plugin, menu));
        add(new CreateCommand(plugin));
        add(new SetHomeCommand(plugin));
        add(new HomeCommand(plugin));
        add(new SetWarpCommand(plugin));
        add(new WarpCommand(plugin));
        add(new ToggleWarp(plugin));
        add(new BanCommand(plugin));
        add(new LockUnlockCommand(plugin));
        if (Settings.island_useTopTen) {
            add(new TopCommand(plugin));
        }
        add(new BiomeCommand(plugin, menu));
        add(new LevelCommand(plugin));
        add(new InfoCommand(plugin));
        add(new InviteCommand(plugin, inviteHandler));
        add(new AcceptRejectCommand(inviteHandler));
        add(new LeaveCommand(plugin));
        add(new KickCommand(plugin));
        add(new PartyCommand(plugin, menu, inviteHandler));
        add(new MakeLeaderCommand(plugin));
        add(new SpawnCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && plugin.getPlayerLogic().isLocked((Player) sender)) {
            sender.sendMessage(tr("\u00a74Your island data is being loaded - try again later"));
            return true;
        }
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            player.openInventory(menu.displayIslandGUI(player));
            return true;
        }
        return super.onCommand(sender, command, alias, args);
    }
}
