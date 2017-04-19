package us.talabrek.ultimateskyblock.api.event; 
 
import org.bukkit.entity.Player; 
import org.bukkit.event.Event; 
import org.bukkit.event.HandlerList; 
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;
import us.talabrek.ultimateskyblock.api.IslandInfo; 

public class IslandDeleteEvent extends uSkyBlockEvent { 
   
    private static final HandlerList handlers = new HandlerList(); 
 
    private final IslandInfo island; 
 
    public IslandDeleteEvent(Player player, uSkyBlockAPI api, IslandInfo island) { 
        super(player, api, Cause.OTHER); 
        this.island = island; 
    } 

    public IslandInfo getIsland() {
        return island;
    }

    @Override 
    public HandlerList getHandlers() { 
        return handlers; 
    } 

 
     /** 
      * Returns the handlers listening to this event. 
      * Required for Bukkit-events. 
      * @return the handlers listening to this event. 
      */ 
    public static HandlerList getHandlerList() { 
        return handlers; 
    } 
} 

