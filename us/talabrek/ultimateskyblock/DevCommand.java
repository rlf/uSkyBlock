/*     */ package us.talabrek.ultimateskyblock;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.ChatColor;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.OfflinePlayer;
/*     */ import org.bukkit.Server;
/*     */ import org.bukkit.command.Command;
/*     */ import org.bukkit.command.CommandExecutor;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.scheduler.BukkitScheduler;
/*     */ 
/*     */ public class DevCommand
/*     */   implements CommandExecutor
/*     */ {
/*     */   public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
/*     */   {
/*  24 */     if (!(sender instanceof Player)) {
/*  25 */       return false;
/*     */     }
/*  27 */     Player player = (Player)sender;
/*  28 */     if (split.length == 0) {
/*  29 */       if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || 
/*  30 */         (VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || 
/*  31 */         (VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || 
/*  32 */         (VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp()))
/*     */       {
/*  34 */         player.sendMessage("[dev usage]");
/*  35 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld())) || (player.isOp()))
/*  36 */           player.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
/*  37 */         if ((VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld())) || (player.isOp()))
/*  38 */           player.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
/*  39 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp()))
/*  40 */           player.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE + " add island protection to unprotected islands.");
/*  41 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (player.isOp()))
/*  42 */           player.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
/*  43 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp()))
/*  44 */           player.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
/*  45 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp()))
/*  46 */           player.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
/*  47 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp()))
/*  48 */           player.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE + " save the list of old (empty) island locations.");
/*  49 */         if ((VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (player.isOp()))
/*  50 */           player.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
/*  51 */         if ((VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || (player.isOp()))
/*  52 */           player.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
/*  53 */         if ((VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp()))
/*  54 */           player.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE + " set a player's island to your location");
/*  55 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp()))
/*  56 */           player.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as complete");
/*  57 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp()))
/*  58 */           player.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as incomplete");
/*  59 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp()))
/*  60 */           player.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <challengename>:" + ChatColor.WHITE + " resets all of the player's challenges");
/*  61 */         if ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp()))
/*  62 */           player.sendMessage(ChatColor.YELLOW + "/dev purge [TimeInDays]:" + ChatColor.WHITE + " delete inactive islands older than [TimeInDays].");
/*  63 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) || (player.isOp()))
/*  64 */           player.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
/*  65 */         if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) || (player.isOp()))
/*  66 */           player.sendMessage(ChatColor.YELLOW + "/dev info <player>:" + ChatColor.WHITE + " check the party information for the given player.");
/*     */       } else {
/*  68 */         player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
/*     */       } } else if (split.length == 1) {
/*  70 */       if ((split[0].equals("clearorphan")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
/*     */       {
/*  72 */         player.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
/*  73 */         uSkyBlock.getInstance().clearOrphanedIsland();
/*  74 */       } else if ((split[0].equals("protectall")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp())))
/*     */       {
/*  76 */         player.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
/*  77 */         if (Settings.island_protectWithWorldGuard)
/*     */         {
/*  79 */           player.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
/*  80 */           WorldGuardHandler.protectAllIslands(sender);
/*     */         } else {
/*  82 */           player.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
/*     */         } } else if ((split[0].equals("buildpartylist")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())) || (player.isOp())))
/*     */       {
/*  85 */         player.sendMessage(ChatColor.YELLOW + "Building party lists..");
/*  86 */         buildPartyList();
/*  87 */       } else if ((split[0].equals("orphancount")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
/*     */       {
/*  89 */         player.sendMessage(ChatColor.YELLOW + uSkyBlock.getInstance().orphanCount() + " old island locations will be used before new ones.");
/*  90 */       } else if ((split[0].equals("reload")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld())) || (player.isOp())))
/*     */       {
/*  92 */         uSkyBlock.getInstance().reloadConfig();
/*  93 */         uSkyBlock.getInstance().loadPluginConfig();
/*  94 */         player.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
/*  95 */       } else if ((split[0].equals("saveorphan")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())) || (player.isOp())))
/*     */       {
/*  97 */         player.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
/*  98 */         uSkyBlock.getInstance().saveOrphans();
/*  99 */       } else if ((split[0].equals("topten")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())) || (player.isOp())))
/*     */       {
/* 101 */         player.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
/* 102 */         uSkyBlock.getInstance().updateTopTen(uSkyBlock.getInstance().generateTopTen());
/* 103 */         player.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
/* 104 */       } else if ((split[0].equals("purge")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp())))
/*     */       {
/* 106 */         if (uSkyBlock.getInstance().isPurgeActive())
/*     */         {
/* 108 */           player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
/* 109 */           return true;
/*     */         }
/* 111 */         player.sendMessage(ChatColor.YELLOW + "Usage: /dev purge [TimeInDays]");
/* 112 */         return true;
/*     */       }
/* 114 */     } else if (split.length == 2) {
/* 115 */       if ((split[0].equals("purge")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld())) || (player.isOp())))
/*     */       {
/* 117 */         if (uSkyBlock.getInstance().isPurgeActive())
/*     */         {
/* 119 */           player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
/* 120 */           return true;
/*     */         }
/* 122 */         uSkyBlock.getInstance().activatePurge();
/* 123 */         final int time = Integer.parseInt(split[1]) * 24;
/* 124 */         player.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + split[1] + " days.");
/* 125 */         uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable()
/*     */         {
/*     */           public void run()
/*     */           {
/* 129 */             File directoryPlayers = new File(uSkyBlock.getInstance().getDataFolder() + File.separator + "players");
/*     */ 
/* 131 */             long offlineTime = 0L;
/*     */ 
/* 133 */             for (File child : directoryPlayers.listFiles())
/*     */             {
/* 135 */               if ((Bukkit.getOfflinePlayer(child.getName()) != null) && (Bukkit.getPlayer(child.getName()) == null))
/*     */               {
/* 137 */                 OfflinePlayer oplayer = Bukkit.getOfflinePlayer(child.getName());
/* 138 */                 offlineTime = oplayer.getLastPlayed();
/* 139 */                 offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
/* 140 */                 if ((offlineTime > time) && (uSkyBlock.getInstance().hasIsland(oplayer.getName())))
/*     */                 {
/* 142 */                   PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(oplayer.getName());
/* 143 */                   if (pi != null)
/*     */                   {
/* 145 */                     if (!pi.getHasParty())
/*     */                     {
/* 147 */                       if (pi.getIslandLevel() < 10)
/*     */                       {
/* 149 */                         if (child.getName() != null)
/* 150 */                           uSkyBlock.getInstance().addToRemoveList(child.getName()); 
/*     */                       }
/*     */                     }
/*     */                   }
/*     */                 }
/*     */               }
/*     */             }
/* 155 */             System.out.print("Removing " + uSkyBlock.getInstance().getRemoveList().size() + " inactive islands.");
/* 156 */             uSkyBlock.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(uSkyBlock.getInstance(), new Runnable()
/*     */             {
/*     */               public void run() {
/* 159 */                 if ((uSkyBlock.getInstance().getRemoveList().size() > 0) && (uSkyBlock.getInstance().isPurgeActive()))
/*     */                 {
/* 161 */                   uSkyBlock.getInstance().deletePlayerIsland((String)uSkyBlock.getInstance().getRemoveList().get(0));
/* 162 */                   System.out.print("[uSkyBlock] Purge: Removing " + (String)uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
/* 163 */                   uSkyBlock.getInstance().deleteFromRemoveList();
/*     */                 }
/*     */ 
/* 166 */                 if ((uSkyBlock.getInstance().getRemoveList().size() == 0) && (uSkyBlock.getInstance().isPurgeActive()))
/*     */                 {
/* 168 */                   uSkyBlock.getInstance().deactivatePurge();
/* 169 */                   System.out.print("[uSkyBlock] Finished purging marked inactive islands.");
/*     */                 }
/*     */               }
/*     */             }
/*     */             , 0L, 20L);
/*     */           } } );
/*     */       }
/* 176 */       else if ((split[0].equals("goto")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.goto", player.getWorld())) || (player.isOp())))
/*     */       {
/* 178 */         PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 179 */         if ((pi.getHasParty()) && (!pi.getPartyLeader().equalsIgnoreCase(split[1])))
/* 180 */           pi = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
/* 181 */         if (pi == null) {
/* 182 */           player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/*     */         }
/*     */         else {
/* 185 */           if (pi.getHomeLocation() != null)
/*     */           {
/* 187 */             player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
/* 188 */             player.teleport(pi.getIslandLocation());
/* 189 */             return true;
/*     */           }
/* 191 */           if (pi.getIslandLocation() != null)
/*     */           {
/* 193 */             player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
/* 194 */             player.teleport(pi.getIslandLocation());
/* 195 */             return true;
/*     */           }
/* 197 */           player.sendMessage("Error: That player does not have an island!");
/*     */         }
/* 199 */       } else if ((split[0].equals("remove")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())) || (player.isOp())))
/*     */       {
/* 201 */         PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 202 */         if (pi == null) {
/* 203 */           player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/*     */         }
/*     */         else {
/* 206 */           if (pi.getIslandLocation() != null)
/*     */           {
/* 208 */             player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
/* 209 */             uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
/* 210 */             return true;
/*     */           }
/* 212 */           player.sendMessage("Error: That player does not have an island!");
/*     */         }
/* 214 */       } else if ((split[0].equals("delete")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())) || (player.isOp())))
/*     */       {
/* 216 */         PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 217 */         if (pi == null) {
/* 218 */           player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/*     */         }
/*     */         else {
/* 221 */           if (pi.getIslandLocation() != null)
/*     */           {
/* 223 */             player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
/* 224 */             uSkyBlock.getInstance().deletePlayerIsland(split[1]);
/* 225 */             return true;
/*     */           }
/* 227 */           player.sendMessage("Error: That player does not have an island!");
/*     */         }
/* 229 */       } else if ((split[0].equals("register")) && ((VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld())) || (player.isOp())))
/*     */       {
/* 231 */         PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 232 */         if (pi == null) {
/* 233 */           player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/*     */         }
/*     */         else {
/* 236 */           if (pi.getHasIsland())
/* 237 */             uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
/* 238 */           if (uSkyBlock.getInstance().devSetPlayerIsland(sender, player.getLocation(), split[1]))
/*     */           {
/* 240 */             player.sendMessage(ChatColor.GREEN + "Set " + split[1] + "'s island to the bedrock nearest you.");
/*     */           }
/* 242 */           else player.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!"); 
/*     */         }
/*     */       }
/* 244 */       else if ((split[0].equals("info")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) || (player.isOp())))
/*     */       {
/* 246 */         PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 247 */         if (pi == null) {
/* 248 */           player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/*     */         }
/* 251 */         else if (pi.getHasParty())
/*     */         {
/* 253 */           PlayerInfo piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
/* 254 */           List pList = piL.getMembers();
/* 255 */           if (pList.contains(split[1]))
/*     */           {
/* 257 */             if (split[1].equalsIgnoreCase(pi.getPartyLeader())) {
/* 258 */               pList.remove(split[1]);
/*     */             }
/*     */             else {
/* 261 */               pList.remove(pi.getPartyLeader());
/*     */             }
/*     */           }
/* 264 */           player.sendMessage(ChatColor.GREEN + pi.getPartyLeader() + " " + ChatColor.WHITE + pList.toString());
/* 265 */           player.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " (" + pi.getPartyIslandLocation().getBlockX() + "," + pi.getPartyIslandLocation().getBlockY() + "," + pi.getPartyIslandLocation().getBlockZ() + ")");
/*     */         }
/*     */         else {
/* 268 */           player.sendMessage(ChatColor.YELLOW + "That player is not a member of an island party.");
/* 269 */           if (pi.getHasIsland())
/*     */           {
/* 271 */             player.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " (" + pi.getIslandLocation().getBlockX() + "," + pi.getIslandLocation().getBlockY() + "," + pi.getIslandLocation().getBlockZ() + ")");
/*     */           }
/* 273 */           if (pi.getPartyLeader() != null)
/*     */           {
/* 275 */             player.sendMessage(ChatColor.RED + "Party leader: " + pi.getPartyLeader() + " should be null!");
/*     */           }
/* 277 */           if (pi.getMembers() != null)
/*     */           {
/* 279 */             player.sendMessage(ChatColor.RED + "Player has party members, but shouldn't!");
/*     */           }
/*     */         }
/*     */       }
/* 283 */       else if ((split[0].equals("resetallchallenges")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
/*     */       {
/* 285 */         if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[1]))
/*     */         {
/* 287 */           PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
/* 288 */           if (pi == null) {
/* 289 */             player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/* 290 */             return true;
/*     */           }
/* 292 */           pi.resetAllChallenges();
/* 293 */           uSkyBlock.getInstance().writePlayerFile(split[1], pi);
/* 294 */           player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
/*     */         }
/*     */         else {
/* 297 */           ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[1])).resetAllChallenges();
/* 298 */           player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
/*     */         }
/*     */       }
/* 301 */     } else if (split.length == 3)
/*     */     {
/* 303 */       if ((split[0].equals("completechallenge")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
/*     */       {
/* 305 */         if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2]))
/*     */         {
/* 307 */           PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
/* 308 */           if (pi == null) {
/* 309 */             player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/* 310 */             return true;
/*     */           }
/* 312 */           if ((pi.checkChallenge(split[1].toLowerCase())) || (!pi.challengeExists(split[1].toLowerCase())))
/*     */           {
/* 314 */             player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
/* 315 */             return true;
/*     */           }
/* 317 */           pi.completeChallenge(split[1].toLowerCase());
/* 318 */           uSkyBlock.getInstance().writePlayerFile(split[2], pi);
/* 319 */           player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
/*     */         }
/*     */         else {
/* 322 */           if ((((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).checkChallenge(split[1].toLowerCase())) || (!((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).challengeExists(split[1].toLowerCase())))
/*     */           {
/* 324 */             player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
/* 325 */             return true;
/*     */           }
/* 327 */           ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).completeChallenge(split[1].toLowerCase());
/* 328 */           player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
/*     */         }
/* 330 */       } else if ((split[0].equals("resetchallenge")) && ((VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld())) || (player.isOp())))
/*     */       {
/* 332 */         if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2]))
/*     */         {
/* 334 */           PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
/* 335 */           if (pi == null) {
/* 336 */             player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
/* 337 */             return true;
/*     */           }
/* 339 */           if ((!pi.checkChallenge(split[1].toLowerCase())) || (!pi.challengeExists(split[1].toLowerCase())))
/*     */           {
/* 341 */             player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
/* 342 */             return true;
/*     */           }
/* 344 */           pi.resetChallenge(split[1].toLowerCase());
/* 345 */           uSkyBlock.getInstance().writePlayerFile(split[2], pi);
/* 346 */           player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been reset for " + split[2]);
/*     */         }
/*     */         else {
/* 349 */           if ((!((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).checkChallenge(split[1].toLowerCase())) || (!((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).challengeExists(split[1].toLowerCase())))
/*     */           {
/* 351 */             player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
/* 352 */             return true;
/*     */           }
/* 354 */           ((PlayerInfo)uSkyBlock.getInstance().getActivePlayers().get(split[2])).resetChallenge(split[1].toLowerCase());
/* 355 */           player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
/*     */         }
/*     */       }
/*     */     }
/* 359 */     return true;
/*     */   }
/*     */ 
/*     */   public void buildPartyList()
/*     */   {
/* 365 */     File folder = uSkyBlock.getInstance().directoryPlayers;
/* 366 */     File[] listOfFiles = folder.listFiles();
/*     */ 
/* 369 */     System.out.print(ChatColor.YELLOW + "[uSkyBlock] Building a new party list...");
/* 370 */     for (int i = 0; i < listOfFiles.length; i++)
/*     */     {
/*     */       PlayerInfo pi;
/* 372 */       if ((pi = uSkyBlock.getInstance().readPlayerFile(listOfFiles[i].getName())) != null)
/*     */       {
/* 374 */         if (pi.getHasParty())
/*     */         {
/*     */           PlayerInfo piL;
/*     */           PlayerInfo piL;
/* 376 */           if (!pi.getPartyLeader().equalsIgnoreCase(listOfFiles[i].getName()))
/* 377 */             piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
/*     */           else {
/* 379 */             piL = pi;
/*     */           }
/* 381 */           piL.getHasParty();
/*     */ 
/* 385 */           if (!piL.getMembers().contains(listOfFiles[i].getName()))
/*     */           {
/* 387 */             piL.addMember(listOfFiles[i].getName());
/*     */           }
/*     */ 
/* 390 */           uSkyBlock.getInstance().writePlayerFile(pi.getPartyLeader(), piL);
/*     */         }
/*     */       }
/*     */     }
/* 394 */     System.out.print(ChatColor.YELLOW + "[uSkyBlock] Party list completed.");
/*     */   }
/*     */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.DevCommand
 * JD-Core Version:    0.6.2
 */