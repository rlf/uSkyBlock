/*    */ package us.talabrek.ultimateskyblock;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import org.bukkit.ChatColor;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
/*    */ import org.bukkit.event.entity.FoodLevelChangeEvent;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
/*    */ import org.bukkit.event.player.PlayerJoinEvent;
/*    */ import org.bukkit.event.player.PlayerQuitEvent;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.inventory.PlayerInventory;
/*    */ 
/*    */ public class PlayerJoin
/*    */   implements Listener
/*    */ {
/* 18 */   private Player hungerman = null;
/*    */ 
/*    */   @EventHandler(priority=EventPriority.NORMAL)
/*    */   public void onPlayerJoin(PlayerJoinEvent event)
/*    */   {
/* 24 */     PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(event.getPlayer().getName());
/* 25 */     if (pi == null)
/*    */     {
/* 27 */       System.out.print("Creating a new skyblock file for " + event.getPlayer().getName());
/* 28 */       pi = new PlayerInfo(event.getPlayer().getName());
/* 29 */       uSkyBlock.getInstance().writePlayerFile(event.getPlayer().getName(), pi);
/*    */     }
/* 31 */     if ((pi.getHasParty()) && (pi.getPartyIslandLocation() == null))
/*    */     {
/* 33 */       PlayerInfo pi2 = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
/* 34 */       pi.setPartyIslandLocation(pi2.getIslandLocation());
/* 35 */       uSkyBlock.getInstance().writePlayerFile(event.getPlayer().getName(), pi);
/*    */     }
/*    */ 
/* 46 */     pi.buildChallengeList();
/* 47 */     uSkyBlock.getInstance().addActivePlayer(event.getPlayer().getName(), pi);
/* 48 */     System.out.print("Loaded player file for " + event.getPlayer().getName());
/*    */   }
/*    */ 
/*    */   @EventHandler(priority=EventPriority.NORMAL)
/*    */   public void onPlayerQuit(PlayerQuitEvent event)
/*    */   {
/* 56 */     uSkyBlock.getInstance().removeActivePlayer(event.getPlayer().getName());
/*    */   }
/*    */ 
/*    */   @EventHandler(priority=EventPriority.NORMAL)
/*    */   public void onPlayerFoodChange(FoodLevelChangeEvent event)
/*    */   {
/* 62 */     if ((event.getEntity() instanceof Player))
/*    */     {
/* 64 */       this.hungerman = ((Player)event.getEntity());
/* 65 */       if (this.hungerman.getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
/*    */       {
/* 67 */         if (this.hungerman.getFoodLevel() > event.getFoodLevel())
/*    */         {
/* 69 */           if (uSkyBlock.getInstance().playerIsOnIsland(this.hungerman))
/*    */           {
/* 71 */             if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger", this.hungerman.getWorld()))
/*    */             {
/* 73 */               event.setCancelled(true);
/*    */             }
/*    */           }
/*    */         }
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   @EventHandler(priority=EventPriority.NORMAL)
/*    */   public void onPlayerInteract(PlayerInteractEvent event) {
/* 83 */     if ((Settings.extras_obsidianToLava) && (uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())))
/*    */     {
/* 85 */       if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (event.getPlayer().getItemInHand().getTypeId() == 325) && 
/* 86 */         (event.getClickedBlock().getType() == Material.OBSIDIAN))
/*    */       {
/* 88 */         if (!uSkyBlock.getInstance().testForObsidian(event.getClickedBlock()))
/*    */         {
/* 90 */           event.getPlayer().sendMessage(ChatColor.YELLOW + "Changing your obsidian back into lava. Be careful!");
/* 91 */           event.getClickedBlock().setType(Material.AIR);
/* 92 */           event.getPlayer().getInventory().removeItem(new ItemStack[] { new ItemStack(325, 1) });
/* 93 */           event.getPlayer().getInventory().addItem(new ItemStack[] { new ItemStack(327, 1) });
/*    */         }
/*    */       }
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.PlayerJoin
 * JD-Core Version:    0.6.2
 */