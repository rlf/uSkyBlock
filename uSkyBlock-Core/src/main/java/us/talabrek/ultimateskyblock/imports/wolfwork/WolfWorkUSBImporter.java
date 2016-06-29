package us.talabrek.ultimateskyblock.imports.wolfwork;

import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * An importer for the wolfwork branch.
 */
public class WolfWorkUSBImporter implements USBImporter {

    private uSkyBlock plugin;

    @Override
    public String getName() {
        return "wolfwork";
    }

    @Override
    public void init(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    public Boolean importFile(File file) {
        try {
            if (file != null && file.getName().equals("orphanedIslands.bin")) {
                if (file.exists()) {
                    importOrphanedIslands(plugin, file);
                    return true;
                }
                return false;
            }
            PlayerInfo playerInfo = readPlayerInfo(file);
            if (playerInfo == null) {
                return false;
            }
            us.talabrek.ultimateskyblock.player.PlayerInfo pi = importPlayerInfo(plugin, playerInfo);
            importIsland(plugin, playerInfo, pi);
            plugin.getPlayerLogic().removeActivePlayer(pi);
            if (!file.delete()) {
                file.deleteOnExit();
            }
            return true;
        } catch (Exception e) {
            log(Level.WARNING, "Unable to import " + file, e);
            return false;
        }
    }

    private int importOrphanedIslands(uSkyBlock plugin, File orphanFile) {
        try (ObjectInputStream in = new WolfWorkObjectInputStream(new FileInputStream(orphanFile))) {
            Object stackObj = in.readObject();
            if (stackObj instanceof Stack) {
                int countOrphan = 0;
                Stack<SerializableLocation> stack = (Stack) stackObj;
                while (!stack.isEmpty()) {
                    SerializableLocation remove = stack.remove(0);
                    plugin.getOrphanLogic().addOrphan(remove.getLocation());
                    countOrphan++;
                }
                if (!orphanFile.delete()) {
                    orphanFile.deleteOnExit();
                }
                return countOrphan;
            }
        } catch (IOException | ClassNotFoundException e) {
            log(Level.WARNING, "Unable to read the orphanedIslands.bin file", e);
        }
        return 0;
    }

    private us.talabrek.ultimateskyblock.player.PlayerInfo importPlayerInfo(uSkyBlock plugin, PlayerInfo playerInfo) {
        // Copy PlayerInfo
        us.talabrek.ultimateskyblock.player.PlayerInfo pi = plugin.getPlayerInfo(playerInfo.getPlayerName());
        if (playerInfo.getIslandLocation() != null) {
            pi.setIslandLocation(playerInfo.getIslandLocation());
        } else if (playerInfo.getPartyIslandLocation() != null) {
            pi.setIslandLocation(playerInfo.getPartyIslandLocation());
        }
        pi.setHomeLocation(playerInfo.getHomeLocation());
        // Challenges
        long now = System.currentTimeMillis();
        for (us.talabrek.ultimateskyblock.api.ChallengeCompletion challengeApi : pi.getChallenges()) {
            if (challengeApi instanceof ChallengeCompletion) {
                ChallengeCompletion challenge = (ChallengeCompletion) challengeApi;
                if (playerInfo.checkChallenge(challenge.getName())) {
                    challenge.setCooldownUntil(now);
                    challenge.setTimesCompleted(1);
                } else {
                    challenge.setTimesCompleted(0);
                }
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
            } else {
                islandInfo.setupPartyLeader(playerInfo.getPlayerName());
            }
            for (String member : playerInfo.getMembers()) {
                islandInfo.setupPartyMember(uSkyBlock.getInstance().getPlayerInfo(member));
            }
            islandInfo.setWarp(playerInfo.isWarpActive());
            islandInfo.setWarpLocation(playerInfo.getWarpLocation());
            for (String banned : playerInfo.getBanned()) {
                islandInfo.banPlayer(plugin.getPlayerDB().getUUIDFromName(banned));
            }
            // Not really that important - since it's most likely different!
            islandInfo.setLevel(playerInfo.getIslandLevel());
            islandInfo.setBiome("OCEAN");
            islandInfo.save();
            WorldGuardHandler.updateRegion(islandInfo);
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

    @Override
    public File[] getFiles() {
        ArrayList<File> fileList = new ArrayList<>();
        fileList.add(new File(plugin.getDataFolder(), "orphanedIslands.bin"));
        fileList.addAll(Arrays.asList(plugin.directoryPlayers.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && !name.endsWith(".yml");
            }
        })));
        return fileList.toArray(new File[fileList.size()]);
    }

    @Override
    public void completed(int success, int failed, int skipped) {
        // Do nothing
    }
}
