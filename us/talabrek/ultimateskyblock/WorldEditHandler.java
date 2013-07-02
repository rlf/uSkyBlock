/*    */ package us.talabrek.ultimateskyblock;
/*    */ 
/*    */ import com.sk89q.worldedit.CuboidClipboard;
/*    */ import com.sk89q.worldedit.EditSession;
/*    */ import com.sk89q.worldedit.MaxChangedBlocksException;
/*    */ import com.sk89q.worldedit.Vector;
/*    */ import com.sk89q.worldedit.bukkit.BukkitWorld;
/*    */ import com.sk89q.worldedit.bukkit.WorldEditPlugin;
/*    */ import com.sk89q.worldedit.data.DataException;
/*    */ import com.sk89q.worldedit.schematic.SchematicFormat;
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Server;
/*    */ import org.bukkit.World;
/*    */ import org.bukkit.plugin.Plugin;
/*    */ import org.bukkit.plugin.PluginManager;
/*    */ 
/*    */ public class WorldEditHandler
/*    */ {
/*    */   public static WorldEditPlugin getWorldEdit()
/*    */   {
/* 22 */     Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
/*    */ 
/* 25 */     if ((plugin == null) || (!(plugin instanceof WorldEditPlugin))) {
/* 26 */       return null;
/*    */     }
/*    */ 
/* 29 */     return (WorldEditPlugin)plugin;
/*    */   }
/*    */ 
/*    */   public static boolean loadIslandSchematic(World world, File file, Location origin) throws DataException, IOException, MaxChangedBlocksException {
/* 33 */     Vector v = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
/* 34 */     SchematicFormat format = SchematicFormat.getFormat(file);
/* 35 */     if (format == null)
/*    */     {
/* 37 */       return false;
/*    */     }
/* 39 */     EditSession es = new EditSession(new BukkitWorld(world), 999999999);
/* 40 */     CuboidClipboard cc = format.load(file);
/* 41 */     cc.paste(es, v, false);
/* 42 */     return true;
/*    */   }
/*    */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.WorldEditHandler
 * JD-Core Version:    0.6.2
 */