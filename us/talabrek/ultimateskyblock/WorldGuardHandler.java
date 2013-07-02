/*     */ package us.talabrek.ultimateskyblock;
/*     */ 
/*     */ import com.sk89q.worldedit.BlockVector;
/*     */ import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
/*     */ import com.sk89q.worldguard.domains.DefaultDomain;
/*     */ import com.sk89q.worldguard.protection.ApplicableRegionSet;
/*     */ import com.sk89q.worldguard.protection.flags.DefaultFlag;
/*     */ import com.sk89q.worldguard.protection.flags.StateFlag;
/*     */ import com.sk89q.worldguard.protection.flags.StringFlag;
/*     */ import com.sk89q.worldguard.protection.managers.RegionManager;
/*     */ import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
/*     */ import com.sk89q.worldguard.protection.regions.ProtectedRegion;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.ChatColor;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.OfflinePlayer;
/*     */ import org.bukkit.Server;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.PluginManager;
/*     */ 
/*     */ public class WorldGuardHandler
/*     */ {
/*     */   public static WorldGuardPlugin getWorldGuard()
/*     */   {
/*  25 */     Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
/*     */ 
/*  28 */     if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin))) {
/*  29 */       return null;
/*     */     }
/*     */ 
/*  32 */     return (WorldGuardPlugin)plugin;
/*     */   }
/*     */ 
/*     */   public static void protectIsland(CommandSender sender, String player)
/*     */   {
/*     */     try
/*     */     {
/*  39 */       if (Settings.island_protectWithWorldGuard)
/*     */       {
/*  41 */         if ((uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null) && (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")))
/*     */         {
/*  43 */           ProtectedRegion region = null;
/*  44 */           DefaultDomain owners = new DefaultDomain();
/*  45 */           region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()));
/*  46 */           owners.addPlayer(player);
/*  47 */           region.setOwners(owners);
/*  48 */           region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
/*  49 */           region.setPriority(100);
/*  50 */           region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" + player + ")"));
/*  51 */           region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" + player + ")"));
/*  52 */           region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
/*  53 */           region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
/*  54 */           region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
/*  55 */           ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getApplicableRegions(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
/*  56 */           if (set.size() > 0)
/*     */           {
/*  58 */             for (ProtectedRegion regions : set) {
/*  59 */               if (!regions.getId().equalsIgnoreCase("__global__"))
/*  60 */                 getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
/*     */             }
/*     */           }
/*  63 */           getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
/*  64 */           System.out.print("New protected region created for " + player + "'s Island by " + sender.getName());
/*  65 */           getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
/*     */         }
/*     */         else {
/*  68 */           sender.sendMessage("Player doesn't have an island or it's already protected!");
/*     */         }
/*     */       }
/*     */     } catch (Exception ex) {
/*  72 */       System.out.print("ERROR: Failed to protect " + player + "'s Island (" + sender.getName() + ")");
/*  73 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void protectAllIslands(CommandSender sender)
/*     */   {
/*  79 */     String player = "";
/*  80 */     int checkislands = 0;
/*     */     try
/*     */     {
/*  85 */       if (Settings.island_protectWithWorldGuard)
/*     */       {
/*  87 */         Player[] players = Bukkit.getServer().getOnlinePlayers();
/*     */         ProtectedRegion region;
/*  88 */         for (Player playerx : players)
/*     */         {
/*  90 */           player = playerx.getName();
/*  91 */           if ((uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null) && (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")))
/*     */           {
/*  93 */             region = null;
/*  94 */             DefaultDomain owners = new DefaultDomain();
/*  95 */             region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()));
/*  96 */             owners.addPlayer(player);
/*  97 */             if (uSkyBlock.getInstance().hasParty(player))
/*     */             {
/*  99 */               PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(player);
/* 100 */               List members = pi.getMembers();
/* 101 */               if (!members.isEmpty())
/*     */               {
/* 103 */                 Iterator memlist = members.iterator();
/* 104 */                 while (memlist.hasNext())
/*     */                 {
/* 106 */                   owners.addPlayer((String)memlist.next());
/*     */                 }
/*     */               }
/*     */             }
/* 110 */             region.setOwners(owners);
/* 111 */             region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
/* 112 */             region.setPriority(100);
/* 113 */             region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" + player + ")"));
/* 114 */             region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" + player + ")"));
/* 115 */             region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
/* 116 */             region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
/* 117 */             region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
/* 118 */             ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getApplicableRegions(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
/* 119 */             if (set.size() > 0)
/*     */             {
/* 121 */               for (ProtectedRegion regions : set) {
/* 122 */                 if (!regions.getId().equalsIgnoreCase("__global__"))
/* 123 */                   getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
/*     */               }
/*     */             }
/* 126 */             getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
/* 127 */             System.out.print("New protected region created for " + player + "'s Island by " + sender.getName());
/* 128 */             checkislands++;
/*     */           }
/*     */         }
/* 131 */         OfflinePlayer[] players2 = Bukkit.getServer().getOfflinePlayers();
/* 132 */         for (OfflinePlayer playerx : players2)
/*     */         {
/* 134 */           player = playerx.getName();
/* 135 */           if (uSkyBlock.getInstance().readPlayerFile(player) == null)
/*     */           {
/* 137 */             System.out.print(player + " does not have an island file!");
/*     */           }
/* 140 */           else if ((uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null) && (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")))
/*     */           {
/* 142 */             ProtectedRegion region = null;
/* 143 */             DefaultDomain owners = new DefaultDomain();
/* 144 */             region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation()));
/* 145 */             owners.addPlayer(player);
/* 146 */             if (uSkyBlock.getInstance().hasParty(player))
/*     */             {
/* 148 */               PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(player);
/* 149 */               List members = pi.getMembers();
/* 150 */               if (!members.isEmpty())
/*     */               {
/* 152 */                 Iterator memlist = members.iterator();
/* 153 */                 while (memlist.hasNext())
/*     */                 {
/* 155 */                   owners.addPlayer((String)memlist.next());
/*     */                 }
/*     */               }
/*     */             }
/* 159 */             region.setOwners(owners);
/* 160 */             region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
/* 161 */             region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" + player + ")"));
/* 162 */             region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" + player + ")"));
/* 163 */             region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
/* 164 */             region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
/* 165 */             region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
/* 166 */             ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getApplicableRegions(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
/* 167 */             if (set.size() > 0)
/*     */             {
/* 169 */               for (ProtectedRegion regions : set) {
/* 170 */                 if (!regions.getId().equalsIgnoreCase("__global__"))
/* 171 */                   getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
/*     */               }
/*     */             }
/* 174 */             getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
/* 175 */             System.out.print("New protected region created for " + player + "'s Island by " + sender.getName());
/* 176 */             checkislands++;
/*     */           }
/*     */         }
/* 179 */         System.out.print("Protected " + checkislands + " islands.");
/* 180 */         getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
/*     */       }
/*     */     } catch (Exception ex) {
/* 183 */       System.out.print("ERROR: Failed to protect " + player + "'s Island (" + sender.getName() + ")");
/* 184 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void islandLock(CommandSender sender, String player)
/*     */   {
/*     */     try
/*     */     {
/* 192 */       if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island"))
/*     */       {
/* 194 */         getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "deny"));
/* 195 */         sender.sendMessage(ChatColor.YELLOW + "Your island is now locked. Only your party members may enter.");
/* 196 */         getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
/*     */       }
/*     */       else {
/* 199 */         sender.sendMessage(ChatColor.RED + "You must be the party leader to lock your island!");
/*     */       }
/*     */     } catch (Exception ex) {
/* 202 */       System.out.print("ERROR: Failed to lock " + player + "'s Island (" + sender.getName() + ")");
/* 203 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void islandUnlock(CommandSender sender, String player)
/*     */   {
/*     */     try
/*     */     {
/* 211 */       if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island"))
/*     */       {
/* 213 */         getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "allow"));
/* 214 */         sender.sendMessage(ChatColor.YELLOW + "Your island is unlocked and anyone may enter, however only you and your party members may build or remove blocks.");
/* 215 */         getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
/*     */       }
/*     */       else {
/* 218 */         sender.sendMessage(ChatColor.RED + "You must be the party leader to unlock your island!");
/*     */       }
/*     */     } catch (Exception ex) {
/* 221 */       System.out.print("ERROR: Failed to unlock " + player + "'s Island (" + sender.getName() + ")");
/* 222 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static BlockVector getProtectionVectorLeft(Location island)
/*     */   {
/* 228 */     return new BlockVector(island.getX() + Settings.island_protectionRange / 2, 255.0D, island.getZ() + Settings.island_protectionRange / 2);
/*     */   }
/*     */ 
/*     */   public static BlockVector getProtectionVectorRight(Location island)
/*     */   {
/* 233 */     return new BlockVector(island.getX() - Settings.island_protectionRange / 2, 0.0D, island.getZ() - Settings.island_protectionRange / 2);
/*     */   }
/*     */ 
/*     */   public static void removePlayerFromRegion(String owner, String player)
/*     */   {
/* 238 */     if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island"))
/*     */     {
/* 240 */       DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners();
/* 241 */       owners.removePlayer(player);
/* 242 */       getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addPlayerToOldRegion(String owner, String player)
/*     */   {
/* 248 */     if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island"))
/*     */     {
/* 250 */       DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners();
/* 251 */       owners.addPlayer(player);
/* 252 */       getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void resetPlayerRegion(String owner)
/*     */   {
/* 258 */     if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island"))
/*     */     {
/* 260 */       DefaultDomain owners = new DefaultDomain();
/* 261 */       owners.addPlayer(owner);
/*     */ 
/* 263 */       getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void transferRegion(String owner, String player, CommandSender sender)
/*     */   {
/*     */     try
/*     */     {
/* 271 */       ProtectedRegion region2 = null;
/* 272 */       region2 = new ProtectedCuboidRegion(player + "Island", getWorldGuard().getRegionManager(Bukkit.getWorld("skyworld")).getRegion(owner + "Island").getMinimumPoint(), getWorldGuard().getRegionManager(Bukkit.getWorld(Settings.general_worldName)).getRegion(owner + "Island").getMaximumPoint());
/* 273 */       region2.setOwners(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners());
/* 274 */       region2.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
/* 275 */       region2.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" + player + ")"));
/* 276 */       region2.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" + player + ")"));
/* 277 */       region2.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
/* 278 */       getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(owner + "Island");
/* 279 */       getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region2);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 283 */       System.out.println("Error transferring WorldGuard Protected Region from (" + owner + ") to (" + player + ")");
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.WorldGuardHandler
 * JD-Core Version:    0.6.2
 */