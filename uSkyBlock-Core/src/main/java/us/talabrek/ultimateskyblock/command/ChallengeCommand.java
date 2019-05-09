package us.talabrek.ultimateskyblock.command;

import dk.lockfuglsang.minecraft.command.BaseCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import us.talabrek.ultimateskyblock.command.challenge.ChallengeCompleteCommand;
import us.talabrek.ultimateskyblock.command.challenge.ChallengeInfoCommand;
import us.talabrek.ultimateskyblock.command.completion.AvailableChallengeTabCompleter;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Primary challenges command
 */
public class ChallengeCommand extends BaseCommandExecutor {
    private final uSkyBlock plugin;

    public ChallengeCommand(uSkyBlock plugin) {
        super("challenges|c", "usb.island.challenges", marktr("complete and list challenges"));
        this.plugin = plugin;
        addTab("challenge", new AvailableChallengeTabCompleter());
        add(new ChallengeCompleteCommand(plugin));
        add(new ChallengeInfoCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.isRequirementsMet(sender, this, args)) {
            return true;
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
            Map.Entry<Inventory, String> inv = plugin.getMenu().displayChallengeGUI(player, 1, null);
            plugin.getMenu().getInventoryManager().createInventory(player, inv.getKey(), inv.getValue());
            return true;
        } else {
            return super.onCommand(sender, command, alias, args);
        }
    }
}
