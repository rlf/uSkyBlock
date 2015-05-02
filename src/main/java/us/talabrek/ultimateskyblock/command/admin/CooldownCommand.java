package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Manages player cooldowns
 */
public class CooldownCommand extends CompositeUSBCommand {
    public CooldownCommand(final uSkyBlock plugin) {
        super("cooldown|cd", "usb.admin.cooldown", "player", tr("Controls player-cooldowns"));
        add(new AbstractUSBCommand("clear|c", null, "command", tr("clears the cooldown on a command (* = all)")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (!data.containsKey("p") || !(data.get("p") instanceof Player)) {
                    sender.sendMessage(tr("The player is not currently online"));
                    return false;
                }
                Player p = (Player)data.get("p");
                if (args.length > 0 && "restart|biome".contains(args[0])) {
                    if (plugin.getCooldownHandler().clearCooldown(p, args[0])) {
                        sender.sendMessage(tr("Cleared cooldown on {0} for {1}", args[0], p.getDisplayName()));
                    } else {
                        sender.sendMessage(tr("No active cooldown on {0} for {1} detected!", args[0], p.getDisplayName()));
                    }
                    return true;
                } else {
                    sender.sendMessage(tr("Invalid command supplied, only restart and biome supported!"));
                    return false;
                }
            }
        });
        add(new AbstractUSBCommand("reset|r", null, "command", tr("resets the cooldown on the command")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (!data.containsKey("p") || !(data.get("p") instanceof Player)) {
                    sender.sendMessage(tr("\u00a7eThe player is not currently online"));
                    return false;
                }
                Player p = (Player)data.get("p");
                if (args.length > 0 && "restart|biome".contains(args[0])) {
                    int cooldown = getCooldown(args[0]);
                    plugin.getCooldownHandler().resetCooldown(p, args[0], cooldown);
                    sender.sendMessage(tr("\u00a7eReset cooldown on {0} for {1}\u00a7e to {2} seconds", args[0], p.getDisplayName(), cooldown));
                    return true;
                } else {
                    sender.sendMessage(tr("Invalid command supplied, only restart and biome supported!"));
                    return false;
                }
            }
        });
        add(new AbstractUSBCommand("list|l", tr("lists all the active cooldowns")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (data.containsKey("uuid") && data.get("uuid") instanceof UUID) {
                    Map<String, Long> map = plugin.getCooldownHandler().getCooldowns((UUID) data.get("uuid"));
                    StringBuilder sb = new StringBuilder();
                    if (map != null && !map.isEmpty()) {
                        long now = System.currentTimeMillis();
                        sb.append(tr("\u00a7eCmd Cooldown") + "\n");
                        for (String cmd : map.keySet()) {
                            sb.append(tr("\u00a7a{0} \u00a7c{1}", cmd, TimeUtil.millisAsString(map.get(cmd) - now)) + "\n");
                        }
                    } else {
                        sb.append(tr("\u00a7eNo active cooldowns for \u00a79{0}\u00a7e found.", data.get("playerName")));
                    }
                    sender.sendMessage(sb.toString().split("\n"));
                }
                return true;
            }
        });
        addTab("command", new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String term) {
                return Arrays.asList("restart", "biome");
            }
        });
    }

    private int getCooldown(String cmd) {
        switch (cmd) {
            case "restart": return Settings.general_cooldownRestart;
            case "biome": return Settings.general_biomeChange;
        }
        return 0;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length > 0) {
            String playerName = args[0];
            data.put("playerName", playerName);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
                data.put("uuid", offlinePlayer.getUniqueId());
                if (offlinePlayer.isOnline()) {
                    data.put("p", offlinePlayer.getPlayer());
                }
            }
        }
        return super.execute(sender, alias, data, args);
    }
}
