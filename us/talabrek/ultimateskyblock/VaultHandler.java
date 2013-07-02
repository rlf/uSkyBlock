/*    */ package us.talabrek.ultimateskyblock;
/*    */ 
/*    */ import net.milkbowl.vault.economy.Economy;
/*    */ import net.milkbowl.vault.permission.Permission;
/*    */ import org.bukkit.Server;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.plugin.PluginManager;
/*    */ import org.bukkit.plugin.RegisteredServiceProvider;
/*    */ import org.bukkit.plugin.ServicesManager;
/*    */ 
/*    */ public class VaultHandler
/*    */ {
/* 12 */   public static Permission perms = null;
/* 13 */   public static Economy econ = null;
/*    */ 
/*    */   public static void addPerk(Player player, String perk)
/*    */   {
/* 17 */     perms.playerAdd(null, player.getName(), perk);
/*    */   }
/*    */ 
/*    */   public static void removePerk(Player player, String perk)
/*    */   {
/* 22 */     perms.playerRemove(null, player.getName(), perk);
/*    */   }
/*    */ 
/*    */   public static void addGroup(Player player, String perk)
/*    */   {
/* 27 */     perms.playerAddGroup(null, player.getName(), perk);
/*    */   }
/*    */ 
/*    */   public static boolean checkPerk(String player, String perk, World world)
/*    */   {
/* 32 */     if (perms.has(null, player, perk))
/* 33 */       return true;
/* 34 */     if (perms.has(world, player, perk))
/* 35 */       return true;
/* 36 */     return false;
/*    */   }
/*    */ 
/*    */   public static boolean setupPermissions() {
/* 40 */     RegisteredServiceProvider rsp = uSkyBlock.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
/* 41 */     if (rsp.getProvider() != null)
/* 42 */       perms = (Permission)rsp.getProvider();
/* 43 */     return perms != null;
/*    */   }
/*    */ 
/*    */   public static boolean setupEconomy() {
/* 47 */     if (uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
/* 48 */       return false;
/*    */     }
/* 50 */     RegisteredServiceProvider rsp = uSkyBlock.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
/* 51 */     if (rsp == null) {
/* 52 */       return false;
/*    */     }
/* 54 */     econ = (Economy)rsp.getProvider();
/* 55 */     return econ != null;
/*    */   }
/*    */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.VaultHandler
 * JD-Core Version:    0.6.2
 */