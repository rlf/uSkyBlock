package us.talabrek.ultimateskyblock;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class DevCommand
  implements CommandExecutor
{
  public DevCommand() {}
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
  {
    if (!(sender instanceof Player)) {
      return false;
    }
    Player player = (Player)sender;
    if (split.length == 0)
    {
      if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || 
        (VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || 
        (VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || 
        (VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp()))
      {
        player.sendMessage("[dev usage]");
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE + " add island protection to unprotected islands.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE + " save the list of old (empty) island locations.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE + " set a player's island to your location");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as complete");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as incomplete");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <challengename>:" + ChatColor.WHITE + " resets all of the player's challenges");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev purge [TimeInDays]:" + ChatColor.WHITE + " delete inactive islands older than [TimeInDays].");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
        }
        if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) || (player.isOp())) {
          player.sendMessage(ChatColor.YELLOW + "/dev info <player>:" + ChatColor.WHITE + " check the party information for the given player.");
        }
      }
      else
      {
        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
      }
    }
    else if (split.length == 1)
    {
      if ((split[0].equals("clearorphan")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
        uSkyBlock.getInstance().clearOrphanedIsland();
      }
      else if ((split[0].equals("protectall")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + "This command is only available using WorldGuard.");
        if (Settings.island_protectWithWorldGuard) {
          player.sendMessage(ChatColor.YELLOW + "This command has been disabled.");
        } else {
          player.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
        }
      }
      else if ((split[0].equals("buildislandlist")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + "Building island list..");
        uSkyBlock.getInstance().buildIslandList();
        player.sendMessage(ChatColor.YELLOW + "Finished building island list..");
      }
      else if ((split[0].equals("orphancount")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + uSkyBlock.getInstance().orphanCount() + " old island locations will be used before new ones.");
      }
      else if ((split[0].equals("reload")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld())) || (player.isOp())))
      {
        uSkyBlock.getInstance().reloadConfig();
        uSkyBlock.getInstance().loadPluginConfig();
        uSkyBlock.getInstance().reloadLevelConfig();
        uSkyBlock.getInstance().loadLevelConfig();
        player.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
      }
      else if ((split[0].equals("saveorphan")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
        uSkyBlock.getInstance().saveOrphans();
      }
      else if ((split[0].equals("topten")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (player.isOp())))
      {
        player.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
        uSkyBlock.getInstance().updateTopTen(uSkyBlock.getInstance().generateTopTen());
        player.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
      }
      else if ((split[0].equals("purge")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp())))
      {
        if (uSkyBlock.getInstance().isPurgeActive())
        {
          player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
          return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Usage: /dev purge [TimeInDays]");
        return true;
      }
    }
    else if (split.length == 2)
    {
      if ((split[0].equals("purge")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp())))
      {
        if (uSkyBlock.getInstance().isPurgeActive())
        {
          player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
          return true;
        }
        uSkyBlock.getInstance().activatePurge();
        int time = Integer.parseInt(split[1]) * 24;
        player.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + split[1] + " days.");
        uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new DevCommand.1(this, time));
      }
      else if ((split[0].equals("goto")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.goto", player.getWorld())) || (player.isOp())))
      {
        PlayerInfo pi = new PlayerInfo(split[1]);
        if (!pi.getHasIsland())
        {
          player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
        }
        else
        {
          if (pi.getHomeLocation() != null)
          {
            player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
            player.teleport(pi.getHomeLocation());
            return true;
          }
          if (pi.getIslandLocation() != null)
          {
            player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
            player.teleport(pi.getIslandLocation());
            return true;
          }
          player.sendMessage("Error: That player does not have an island!");
        }
      }
      else if ((split[0].equals("remove")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || (player.isOp())))
      {
        PlayerInfo pi = new PlayerInfo(split[1]);
        if (!pi.getHasIsland())
        {
          player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
        }
        else
        {
          if (pi.getIslandLocation() != null)
          {
            player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
            uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
            return true;
          }
          player.sendMessage("Error: That player does not have an island!");
        }
      }
      else if ((split[0].equals("delete")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (player.isOp())))
      {
        PlayerInfo pi = new PlayerInfo(split[1]);
        if (!pi.getHasIsland())
        {
          player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
        }
        else
        {
          if (pi.getIslandLocation() != null)
          {
            player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
            uSkyBlock.getInstance().deletePlayerIsland(split[1]);
            return true;
          }
          player.sendMessage("Error: That player does not have an island!");
        }
      }
      else if ((split[0].equals("register")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp())))
      {
        PlayerInfo pi = new PlayerInfo(split[1]);
        if (pi.getHasIsland()) {
          uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
        }
        if (uSkyBlock.getInstance().devSetPlayerIsland(player, player.getLocation(), split[1])) {
          player.sendMessage(ChatColor.GREEN + "Set " + split[1] + "'s island to the bedrock nearest you.");
        } else {
          player.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!");
        }
      }
      else if ((!split[0].equals("info")) || ((!VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) && (!player.isOp())))
      {
        if ((split[0].equals("resetallchallenges")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
        {
          if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[1]))
          {
            PlayerInfo pi = new PlayerInfo(split[1]);
            if (!pi.getHasIsland())
            {
              player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
              return true;
            }
            pi.resetAllChallenges();
            pi.savePlayerConfig(split[1]);
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
          }
          else
          {
            ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[1])).resetAllChallenges();
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
          }
        }
        else if ((split[0].equals("setbiome")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.setbiome", player.getWorld())) || (player.isOp()))) {
          if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[1]))
          {
            PlayerInfo pi = new PlayerInfo(split[1]);
            if (!pi.getHasIsland())
            {
              player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
              return true;
            }
            uSkyBlock.getInstance().setBiome(pi.getIslandLocation(), "OCEAN");
            pi.savePlayerConfig(split[1]);
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
          }
          else
          {
            uSkyBlock.getInstance().setBiome(((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[1])).getIslandLocation(), "OCEAN");
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
          }
        }
      }
    }
    else if (split.length == 3) {
      if ((split[0].equals("completechallenge")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
      {
        if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2]))
        {
          PlayerInfo pi = new PlayerInfo(split[2]);
          if (!pi.getHasIsland())
          {
            player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
            return true;
          }
          if ((pi.checkChallenge(split[1].toLowerCase()) > 0) || (!pi.challengeExists(split[1].toLowerCase())))
          {
            player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
            return true;
          }
          pi.completeChallenge(split[1].toLowerCase());
          pi.savePlayerConfig(split[2]);
          player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
        }
        else
        {
          if ((((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).checkChallenge(split[1].toLowerCase()) > 0) || (!((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).challengeExists(split[1].toLowerCase())))
          {
            player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
            return true;
          }
          ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).completeChallenge(split[1].toLowerCase());
          player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
        }
      }
      else if ((split[0].equals("resetchallenge")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
      {
        if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2]))
        {
          PlayerInfo pi = new PlayerInfo(split[2]);
          if (!pi.getHasIsland())
          {
            player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
            return true;
          }
          if ((pi.checkChallenge(split[1].toLowerCase()) == 0) || (!pi.challengeExists(split[1].toLowerCase())))
          {
            player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
            return true;
          }
          pi.resetChallenge(split[1].toLowerCase());
          pi.savePlayerConfig(split[2]);
          player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been reset for " + split[2]);
        }
        else
        {
          if ((((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).checkChallenge(split[1].toLowerCase()) == 0) || (!((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).challengeExists(split[1].toLowerCase())))
          {
            player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
            return true;
          }
          ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).resetChallenge(split[1].toLowerCase());
          player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
        }
      }
      else if ((split[0].equals("setbiome")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.setbiome", player.getWorld())) || (player.isOp()))) {
        if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[1]))
        {
          PlayerInfo pi = new PlayerInfo(split[1]);
          if (!pi.getHasIsland())
          {
            player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
            return true;
          }
          if (uSkyBlock.getInstance().setBiome(pi.getIslandLocation(), split[2])) {
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to " + split[2].toUpperCase() + ".");
          } else {
            player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
          }
          pi.savePlayerConfig(split[1]);
        }
        else if (uSkyBlock.getInstance().setBiome(((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[1])).getIslandLocation(), split[2]))
        {
          player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to " + split[2].toUpperCase() + ".");
        }
        else
        {
          player.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
        }
      }
    }
    return true;
  }
}
