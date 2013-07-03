 package us.talabrek.ultimateskyblock;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 
 public class ProtectionEvents
   implements Listener
 {
/*  21 */   private Player attacker = null;
/*  22 */   private Player breaker = null;
 
   @EventHandler(priority=EventPriority.HIGH)
   public void onPlayerBlockBreak(BlockBreakEvent event) {
/*  26 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  28 */       if ((!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation())) && 
/*  29 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  31 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.HIGH)
   public void onPlayerBlockPlace(BlockPlaceEvent event) {
/*  38 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  40 */       if ((!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation())) && 
/*  41 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  43 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerInteract(PlayerInteractEvent event) {
/*  50 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  52 */       if ((!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())) && (!uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer())) && 
/*  53 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  55 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
/*  62 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  64 */       if ((!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())) && 
/*  65 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  67 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerShearEntity(PlayerShearEntityEvent event) {
/*  74 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  76 */       if ((!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())) && 
/*  77 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  79 */         event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
/*  80 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerBucketFill(PlayerBucketFillEvent event) {
/*  87 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/*  89 */       if ((!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation())) && 
/*  90 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/*  92 */         event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
/*  93 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
/* 100 */     if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
     {
/* 102 */       if ((!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation())) && 
/* 103 */         (!VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())) && (!event.getPlayer().isOp()))
       {
/* 105 */         event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
/* 106 */         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
/* 113 */     if ((event.getRemover() instanceof Player))
     {
/* 115 */       this.breaker = ((Player)event.getRemover());
/* 116 */       if (this.breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
       {
/* 118 */         if ((!uSkyBlock.getInstance().locationIsOnIsland(this.breaker, event.getEntity().getLocation())) && 
/* 119 */           (!VaultHandler.checkPerk(this.breaker.getName(), "usb.mod.bypassprotection", this.breaker.getWorld())) && (!this.breaker.isOp()))
         {
/* 121 */           event.setCancelled(true);
         }
       }
     }
   }
 
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerVehicleDamage(VehicleDamageEvent event) {
/* 129 */     if ((event.getAttacker() instanceof Player))
     {
/* 131 */       this.breaker = ((Player)event.getAttacker());
/* 132 */       if (this.breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
       {
/* 134 */         if ((!uSkyBlock.getInstance().locationIsOnIsland(this.breaker, event.getVehicle().getLocation())) && 
/* 135 */           (!VaultHandler.checkPerk(this.breaker.getName(), "usb.mod.bypassprotection", this.breaker.getWorld())) && (!this.breaker.isOp()))
         {
/* 137 */           event.setCancelled(true);
         }
       }
     }
   }
 
   @EventHandler(priority=EventPriority.HIGH)
   public void onPlayerAttack(EntityDamageByEntityEvent event)
   {
/* 146 */     if ((event.getDamager() instanceof Player)) {
/* 147 */       this.attacker = ((Player)event.getDamager());
/* 148 */       if (this.attacker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
       {
/* 150 */         if (!(event.getEntity() instanceof Player))
         {
/* 155 */           if ((!uSkyBlock.getInstance().playerIsOnIsland(this.attacker)) && 
/* 156 */             (!VaultHandler.checkPerk(this.attacker.getName(), "usb.mod.bypassprotection", this.attacker.getWorld())) && (!this.attacker.isOp()))
           {
/* 158 */             event.setCancelled(true);
           }
         }
       }
     }
   }
 }