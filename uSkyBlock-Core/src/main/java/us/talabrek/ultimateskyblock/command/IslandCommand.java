package us.talabrek.ultimateskyblock.command;

import dk.lockfuglsang.minecraft.command.BaseCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.command.completion.AllPlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.BiomeTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.MemberTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.OnlinePlayerTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.PermissionTabCompleter;
import us.talabrek.ultimateskyblock.command.completion.SchematicTabCompleter;
import us.talabrek.ultimateskyblock.command.island.AcceptRejectCommand;
import us.talabrek.ultimateskyblock.command.island.AutoCommand;
import us.talabrek.ultimateskyblock.command.island.BanCommand;
import us.talabrek.ultimateskyblock.command.island.BiomeCommand;
import us.talabrek.ultimateskyblock.command.island.CreateCommand;
import us.talabrek.ultimateskyblock.command.island.HomeCommand;
import us.talabrek.ultimateskyblock.command.island.InfoCommand;
import us.talabrek.ultimateskyblock.command.island.InviteCommand;
import us.talabrek.ultimateskyblock.command.island.KickCommand;
import us.talabrek.ultimateskyblock.command.island.LeaveCommand;
import us.talabrek.ultimateskyblock.command.island.LevelCommand;
import us.talabrek.ultimateskyblock.command.island.LockUnlockCommand;
import us.talabrek.ultimateskyblock.command.island.LogCommand;
import us.talabrek.ultimateskyblock.command.island.MakeLeaderCommand;
import us.talabrek.ultimateskyblock.command.island.MapCommand;
import us.talabrek.ultimateskyblock.command.island.MobLimitCommand;
import us.talabrek.ultimateskyblock.command.island.PartyCommand;
import us.talabrek.ultimateskyblock.command.island.PermCommand;
import us.talabrek.ultimateskyblock.command.island.RestartCommand;
import us.talabrek.ultimateskyblock.command.island.SetHomeCommand;
import us.talabrek.ultimateskyblock.command.island.SetWarpCommand;
import us.talabrek.ultimateskyblock.command.island.SpawnCommand;
import us.talabrek.ultimateskyblock.command.island.ToggleWarp;
import us.talabrek.ultimateskyblock.command.island.TopCommand;
import us.talabrek.ultimateskyblock.command.island.TrustCommand;
import us.talabrek.ultimateskyblock.command.island.WarpCommand;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * The main /island command
 */
public class IslandCommand extends BaseCommandExecutor {
    private final uSkyBlock plugin;
    private final SkyBlockMenu menu;

    public IslandCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super("island|is", "usb.island.create", marktr("general island command"));
        this.plugin = plugin;
        this.menu = menu;
        addFeaturePermission("usb.mod.bypasscooldowns", tr("allows user to bypass cooldowns"));
        addFeaturePermission("usb.mod.bypassprotection", tr("allows user to bypass visitor-protections"));
        addFeaturePermission("usb.mod.bypassteleport", tr("allows user to bypass teleport-delay"));
        addFeaturePermission("usb.island.signs.use", tr("allows user to use [usb] signs"));
        addFeaturePermission("usb.island.signs.place", tr("allows user to place [usb] signs"));

        InviteHandler inviteHandler = new InviteHandler(plugin);
        plugin.getServer().getPluginManager().registerEvents(inviteHandler, plugin);
        OnlinePlayerTabCompleter onlinePlayerTabCompleter = new OnlinePlayerTabCompleter();
        AllPlayerTabCompleter playerTabCompleter = new AllPlayerTabCompleter(onlinePlayerTabCompleter);
        addTab("island", playerTabCompleter);
        addTab("player", playerTabCompleter);
        addTab("oplayer", onlinePlayerTabCompleter);
        addTab("biome", new BiomeTabCompleter());
        addTab("member", new MemberTabCompleter(plugin));
        addTab("schematic", new SchematicTabCompleter(plugin));
        addTab("perm", new PermissionTabCompleter(plugin));
        if (Settings.general_enableSkyMap) {
        	addTab("map", playerTabCompleter);
        	add(new MapCommand(plugin));        	
        }
        add(new RestartCommand(plugin));
        add(new LogCommand(plugin, menu));
        CreateCommand createCommand = new CreateCommand(plugin);
        add(createCommand);
        add(new SetHomeCommand(plugin));
        HomeCommand homeCommand = new HomeCommand(plugin);
        add(homeCommand);
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
        add(new AcceptRejectCommand(plugin));
        add(new LeaveCommand(plugin));
        add(new KickCommand(plugin));
        add(new PartyCommand(plugin, menu, inviteHandler));
        add(new MakeLeaderCommand(plugin));
        add(new SpawnCommand(plugin));
        add(new TrustCommand(plugin));
        add(new MobLimitCommand(plugin));
        add(new AutoCommand(plugin, createCommand, homeCommand));
        add(new PermCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.isRequirementsMet(sender, this, args)) {
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                player.openInventory(menu.displayIslandGUI(player));
                return true;
            }
        }
        return super.onCommand(sender, command, alias, args);
    }
}
