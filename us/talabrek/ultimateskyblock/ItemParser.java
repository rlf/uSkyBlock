/*    */ package us.talabrek.ultimateskyblock;
/*    */ 
/*    */ import java.util.Map;
/*    */ import java.util.logging.Logger;
/*    */ import org.bukkit.enchantments.Enchantment;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.material.MaterialData;
/*    */ 
/*    */ public class ItemParser
/*    */ {
/*    */   public static String parseItemStackToString(ItemStack item)
/*    */   {
/*  9 */     if (item == null) {
/* 10 */       return "";
/*    */     }
/*    */ 
/* 14 */     String s = "";
/* 15 */     s = s + "id:" + item.getTypeId() + ";";
/* 16 */     s = s + "amount:" + item.getAmount() + ";";
/* 17 */     s = s + "durab:" + item.getDurability() + ";";
/* 18 */     s = s + "data:" + item.getData().getData() + ";";
/*    */ 
/* 20 */     if (item.getEnchantments().size() > 0) {
/* 21 */       s = s + "ench:";
/* 22 */       for (Enchantment e : item.getEnchantments().keySet()) {
/* 23 */         s = s + "eid#" + e.getId() + " ";
/* 24 */         s = s + "elevel#" + item.getEnchantments().get(e) + " ";
/*    */       }
/*    */     }
/* 27 */     return s.trim();
/*    */   }
/*    */ 
/*    */   public static ItemStack getItemStackfromString(String s) {
/* 31 */     if (s.equalsIgnoreCase("")) {
/* 32 */       return null;
/*    */     }
/* 34 */     ItemStack x = new ItemStack(1);
/*    */ 
/* 36 */     for (String thing : s.split(";")) {
/* 37 */       String[] sp = thing.split(":");
/* 38 */       if (sp.length != 2)
/* 39 */         uSkyBlock.getInstance().log.warning("error, wrong type size");
/* 40 */       String name = sp[0];
/*    */ 
/* 42 */       if (name.equals("id")) {
/* 43 */         x.setTypeId(Integer.parseInt(sp[1]));
/* 44 */       } else if (name.equals("amount")) {
/* 45 */         x.setAmount(Integer.parseInt(sp[1]));
/* 46 */       } else if (name.equals("durab")) {
/* 47 */         x.setDurability((short)Integer.parseInt(sp[1]));
/* 48 */       } else if (name.equals("data")) {
/* 49 */         x.getData().setData((byte)Integer.parseInt(sp[1]));
/* 50 */       } else if (name.equals("ench")) {
/* 51 */         int enchId = 0;
/* 52 */         int level = 0;
/* 53 */         for (String enchantment : sp[1].split(" ")) {
/* 54 */           String[] prop = enchantment.split("#");
/* 55 */           if (prop.length != 2)
/* 56 */             uSkyBlock.getInstance().log.warning("error, wrong enchantmenttype length");
/* 57 */           if (prop[0].equals("eid")) {
/* 58 */             enchId = Integer.parseInt(prop[1]);
/* 59 */           } else if (prop[0].equals("elevel")) {
/* 60 */             level = Integer.parseInt(prop[1]);
/* 61 */             x.addUnsafeEnchantment(Enchantment.getById(enchId), level);
/*    */           }
/*    */         }
/*    */       }
/*    */       else {
/* 66 */         uSkyBlock.getInstance().log.warning("error, unknown itemvalue");
/*    */       }
/*    */     }
/* 69 */     return x;
/*    */   }
/*    */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.ItemParser
 * JD-Core Version:    0.6.2
 */