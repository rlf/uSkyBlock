package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;

import java.util.UUID;

/**
 * Dummy for allowing IJobs to AWE
 */
public class ConsolePlayer extends BukkitPlayer {
    private ConsolePlayer() throws CommandException {
        super(WorldEditHandler.getWorldEdit(), WorldGuardHandler.getWorldGuard().getWorldEdit().getServerInterface(), null);
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    public static Player getInstance() {
        try {
            return new ConsolePlayer();
        } catch (CommandException e) {
            return null;
        }
    }
}
