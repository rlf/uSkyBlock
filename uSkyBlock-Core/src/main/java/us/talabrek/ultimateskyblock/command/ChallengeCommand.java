package us.talabrek.ultimateskyblock.command;

import dk.lockfuglsang.minecraft.command.AbstractCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.challenge.ChallengeCompleteCommand;
import us.talabrek.ultimateskyblock.command.challenge.ChallengeInfoCommand;
import us.talabrek.ultimateskyblock.command.completion.AvailableChallengeTabCompleter;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Primary challenges command
 */
public class ChallengeCommand extends AbstractCommandExecutor {
    private final uSkyBlock plugin;

    public ChallengeCommand(uSkyBlock plugin) {
        super("challenges|c", "usb.island.challenges", tr("complete and list challenges"));
        this.plugin = plugin;
        addTab("challenge", new AvailableChallengeTabCompleter());
        add(new ChallengeCompleteCommand(plugin));
        add(new ChallengeInfoCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.isRequirementsMet(sender, null)) {
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("\u00a7cCommand only available for players."));
            return false;
        }
        if (!plugin.getChallengeLogic().isEnabled()) {
            sender.sendMessage(tr("\u00a7eChallenges has been disabled. Contact an administrator."));
            return false;
        }
        Player player = (Player) sender;
        if (!plugin.isSkyAssociatedWorld(player.getWorld())) {
            player.sendMessage(tr("\u00a74You can only submit challenges in the skyblock world!"));
            return true;
        }
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (!playerInfo.getHasIsland()) {
            player.sendMessage(tr("\u00a74You can only submit challenges when you have an island!"));
            return true;
        }
        if (args.length == 0) {
            player.openInventory(plugin.getMenu().displayChallengeGUI(player, 1));
            return true;
        } else {
            return super.onCommand(sender, command, alias, args);
        }
    }
}
