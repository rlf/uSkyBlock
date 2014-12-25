package us.talabrek.ultimateskyblock.imports.wolfwork;

import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.*;
import java.util.Stack;
import java.util.logging.Level;

/**
 * An importer for the wolfwork branch.
 */
public class WolfWorkUSBImporter implements USBImporter {
    @Override
    public boolean importPlayer(uSkyBlock plugin, File playerFile) {
        try {
            PlayerInfo playerInfo = readPlayerInfo(playerFile);
            if (playerInfo == null) {
                return false;
            }
            us.talabrek.ultimateskyblock.player.PlayerInfo pi = importPlayerInfo(plugin, playerInfo);
            importIsland(plugin, playerInfo, pi);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int importOrphans(uSkyBlock plugin, File configFolder) {
        int orphanCount = 0;
        File orphans = new File(configFolder, "orphanedIslands.bin");
        if (orphans.exists()) {
            orphanCount += importOrphanedIslands(plugin, orphans);
        }
        return orphanCount;
    }

    private int importOrphanedIslands(uSkyBlock plugin, File orphanFile) {
        try (ObjectInputStream in = new WolfWorkObjectInputStream(new FileInputStream(orphanFile))) {
            Object stackObj = in.readObject();
            if (stackObj instanceof Stack) {
                int countOrphan = 0;
                Stack<SerializableLocation> stack = (Stack) stackObj;
                while (!stack.isEmpty()) {
                    SerializableLocation remove = stack.remove(0);
                    plugin.addOrphan(remove.getLocation());
                    countOrphan++;
                }
                if (!orphanFile.delete()) {
                    orphanFile.deleteOnExit();
                }
                plugin.saveOrphans();
                return countOrphan;
            }
        } catch (IOException | ClassNotFoundException e) {
            plugin.log(Level.WARNING, "Unable to read the orphanedIslands.bin file", e);
        }
        return 0;
    }

    private us.talabrek.ultimateskyblock.player.PlayerInfo importPlayerInfo(uSkyBlock plugin, PlayerInfo playerInfo) {
        // Copy PlayerInfo
        us.talabrek.ultimateskyblock.player.PlayerInfo pi = plugin.getPlayerInfo(playerInfo.getPlayerName());
        pi.setHasIsland(playerInfo.getHasIsland());
        pi.setIslandLocation(playerInfo.getIslandLocation());
        pi.setHomeLocation(playerInfo.getHomeLocation());
        // Challenges
        long now = System.currentTimeMillis();
        for (ChallengeCompletion challenge : pi.getChallenges()) {
            if (playerInfo.checkChallenge(challenge.getName())) {
                challenge.setFirstCompleted(now);
                challenge.setTimesCompleted(1);
            } else {
                challenge.setTimesCompleted(0);
            }
        }
        pi.save();
        return pi;
    }

    private void importIsland(uSkyBlock plugin, PlayerInfo playerInfo, us.talabrek.ultimateskyblock.player.PlayerInfo pi) {
        // Copy IslandInfo
        IslandInfo islandInfo = plugin.getIslandInfo(pi);
        if (islandInfo != null) {
            if (playerInfo.getPartyLeader() != null) {
                islandInfo.setupPartyLeader(playerInfo.getPartyLeader());
            }
            for (String member : playerInfo.getMembers()) {
                islandInfo.setupPartyMember(member);
            }
            islandInfo.setWarpActive(playerInfo.isWarpActive());
            islandInfo.setWarpLocation(playerInfo.getWarpLocation());
            for (String banned : playerInfo.getBanned()) {
                islandInfo.banPlayer(banned);
            }
            // Not really that important - since it's most likely different!
            islandInfo.setLevel(playerInfo.getIslandLevel());
            islandInfo.setMaxPartySize(4);
            islandInfo.setBiome("OCEAN");
            islandInfo.save();
        }
    }

    private PlayerInfo readPlayerInfo(File playerFile) {
        try (ObjectInputStream in = new WolfWorkObjectInputStream(new FileInputStream(playerFile))) {
            Object o = in.readObject();
            if (o instanceof PlayerInfo) {
                return (PlayerInfo) o;
            }
        } catch (ClassNotFoundException|IOException e) {
            // Ignore...
        }
        return null;
    }
}
