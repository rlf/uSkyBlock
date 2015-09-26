package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import us.talabrek.ultimateskyblock.command.admin.DebugCommand;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

/**
 * uSkyBlock Actor for WE operations
 */
public class USBActor implements Actor {
    public static final UUID uniqueId = UUID.randomUUID();
    @Override
    public String getName() {
        return "uSkyBlock";
    }

    @Override
    public void printRaw(String s) {
        System.out.println(s);
    }

    @Override
    public void printDebug(String s) {
        DebugCommand.log.log(Level.FINE, s);
    }

    @Override
    public void print(String s) {
        System.out.println(s);
    }

    @Override
    public void printError(String s) {
        System.err.println(s);
    }

    @Override
    public boolean canDestroyBedrock() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public File openFileOpenDialog(String[] strings) {
        return null;
    }

    @Override
    public File openFileSaveDialog(String[] strings) {
        return null;
    }

    @Override
    public void dispatchCUIEvent(CUIEvent cuiEvent) {

    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public SessionKey getSessionKey() {
        return null;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public void checkPermission(String s) throws AuthorizationException {

    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }
}
