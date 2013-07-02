/*     */ package us.talabrek.ultimateskyblock;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Party
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 7L;
/*     */   private String pLeader;
/*     */   private SerializableLocation pIsland;
/*     */   private int pSize;
/*     */   private List<String> members;
/*     */ 
/*     */   public Party(String leader, String member2, SerializableLocation island)
/*     */   {
/*  16 */     this.pLeader = leader;
/*  17 */     this.pSize = 2;
/*  18 */     this.pIsland = island;
/*  19 */     this.members = new ArrayList();
/*  20 */     this.members.add(leader);
/*  21 */     this.members.add(member2);
/*     */   }
/*     */ 
/*     */   public String getLeader() {
/*  25 */     return this.pLeader;
/*     */   }
/*     */ 
/*     */   public SerializableLocation getIsland() {
/*  29 */     return this.pIsland;
/*     */   }
/*     */ 
/*     */   public int getSize() {
/*  33 */     return this.pSize;
/*     */   }
/*     */ 
/*     */   public boolean hasMember(String player)
/*     */   {
/*  39 */     if (this.members.contains(player.toLowerCase()))
/*  40 */       return true;
/*  41 */     if (this.members.contains(player))
/*  42 */       return true;
/*  43 */     if (this.pLeader.equalsIgnoreCase(player))
/*  44 */       return true;
/*  45 */     return false;
/*     */   }
/*     */ 
/*     */   public List<String> getMembers()
/*     */   {
/*  50 */     List onlyMembers = this.members;
/*  51 */     onlyMembers.remove(this.pLeader);
/*  52 */     return onlyMembers;
/*     */   }
/*     */ 
/*     */   public boolean changeLeader(String oLeader, String nLeader)
/*     */   {
/*  57 */     if (oLeader.equalsIgnoreCase(this.pLeader))
/*     */     {
/*  59 */       if ((this.members.contains(nLeader)) && (!oLeader.equalsIgnoreCase(nLeader)))
/*     */       {
/*  61 */         this.pLeader = nLeader;
/*  62 */         this.members.remove(oLeader);
/*  63 */         this.members.add(oLeader);
/*  64 */         return true;
/*     */       }
/*     */     }
/*  67 */     return false;
/*     */   }
/*     */ 
/*     */   public int getMax()
/*     */   {
/*  72 */     if (VaultHandler.checkPerk(this.pLeader, "usb.extra.partysize", uSkyBlock.getSkyBlockWorld()))
/*     */     {
/*  74 */       return Settings.general_maxPartySize * 2;
/*     */     }
/*  76 */     return Settings.general_maxPartySize;
/*     */   }
/*     */ 
/*     */   public boolean addMember(String nMember)
/*     */   {
/*  81 */     if (VaultHandler.checkPerk(this.pLeader, "usb.extra.partysize", uSkyBlock.getSkyBlockWorld()))
/*     */     {
/*  83 */       if ((!this.members.contains(nMember)) && (getSize() < Settings.general_maxPartySize * 2))
/*     */       {
/*  85 */         this.members.add(nMember);
/*  86 */         this.pSize += 1;
/*  87 */         return true;
/*     */       }
/*  89 */       return false;
/*     */     }
/*     */ 
/*  92 */     if ((!this.members.contains(nMember)) && (getSize() < Settings.general_maxPartySize))
/*     */     {
/*  94 */       this.members.add(nMember);
/*  95 */       this.pSize += 1;
/*  96 */       return true;
/*     */     }
/*  98 */     return false;
/*     */   }
/*     */ 
/*     */   public int removeMember(String oMember)
/*     */   {
/* 104 */     if (oMember.equalsIgnoreCase(this.pLeader))
/*     */     {
/* 106 */       return 0;
/*     */     }
/* 108 */     if (this.members.contains(oMember))
/*     */     {
/* 110 */       this.pSize -= 1;
/* 111 */       this.members.remove(oMember);
/* 112 */       return 2;
/*     */     }
/* 114 */     return 1;
/*     */   }
/*     */ }

/* Location:           C:\Users\Alex M\Desktop\uSkyBlock.jar
 * Qualified Name:     us.talabrek.ultimateskyblock.Party
 * JD-Core Version:    0.6.2
 */