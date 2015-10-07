package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.Map;

public class RestartCommand extends RequireIslandCommand {
    public RestartCommand(uSkyBlock plugin) {
        super(plugin, "restart|reset", I18nUtil.tr("delete your island and start a new one."));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.getPartySize() > 1) {
            if (!island.isLeader(player)) {
                player.sendMessage(I18nUtil.tr("\u00a74Only the owner may restart this island. Leave this island in order to start your own (/island leave)."));
            } else {
                player.sendMessage(I18nUtil.tr("\u00a7eYou must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party."));
            }
            return true;
        }
        int cooldown = plugin.getCooldownHandler().getCooldown(player, "restart");
        if (cooldown > 0) {
            player.sendMessage(I18nUtil.tr("\u00a7cYou can restart your island in {0} seconds.", cooldown));
            return true;
        } else {
            if (pi.isIslandGenerating()) {
                player.sendMessage(I18nUtil.tr("\u00a7cYour island is in the process of generating, you cannot restart now."));
                return true;
            }

            if (plugin.getConfirmHandler().checkCommand(player, "/is restart")) {
                plugin.getCooldownHandler().resetCooldown(player, "restart", Settings.general_cooldownRestart);
                return plugin.restartPlayerIsland(player, pi.getIslandLocation());
            } else {
                player.sendMessage(I18nUtil.tr("\u00a7eNOTE: Your entire island and all your belongings will be RESET!"));
                return true;
            }
        }
    }
}
