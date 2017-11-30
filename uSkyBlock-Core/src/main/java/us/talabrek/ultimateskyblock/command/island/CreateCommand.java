package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.event.CreateIslandEvent;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class CreateCommand extends RequirePlayerCommand {
    private final uSkyBlock plugin;

    public CreateCommand(uSkyBlock plugin) {
        super("create|c", "usb.island.create", "?schematic", tr("create an island"));
        this.plugin = plugin;
        addFeaturePermission("usb.exempt.cooldown.create", tr("exempt player from create-cooldown"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        PlayerInfo pi = plugin.getPlayerInfo(player);
        int cooldown = plugin.getCooldownHandler().getCooldown(player, "restart");
        if (!pi.getHasIsland() && cooldown == 0) {
        	
            String cSchem = args != null && args.length > 2 ? args[0] : Settings.island_schematicName;
            String x = args != null && args.length > 2 ? args[1] : "0";
            String z = args != null && args.length > 2 ? args[2] : "0";
            	try{
            		if (args.length == 2){
            			//possible someone is typing this manually without schematic
	            		Integer.parseInt(args[0]);
	            		Integer.parseInt(args[1]);
	            		x = args[0];
	            		z = args[1];
            		}
            	} catch (NumberFormatException e){
            		x = "0";
            		z = "0";
            	}
            
            plugin.getServer().getPluginManager().callEvent(new CreateIslandEvent(player, cSchem, Integer.parseInt(x), Integer.parseInt(z)));
        } else if (pi.getHasIsland()) {
            us.talabrek.ultimateskyblock.api.IslandInfo island = plugin.getIslandInfo(pi);
            if (island.isLeader(player)) {
                player.sendMessage(tr("\u00a74Island found!" +
                        "\u00a7e You already have an island. If you want a fresh island, type" +
                        "\u00a7b /is restart\u00a7e to get one"));
            } else {
                player.sendMessage(tr("\u00a74Island found!" +
                        "\u00a7e You are already a member of an island. To start your own, first" +
                        "\u00a7b /is leave"));
            }
        } else {
            player.sendMessage(tr("\u00a7eYou can create a new island in {0,number,#} seconds.", cooldown));
        }
        return true;
    }
}
