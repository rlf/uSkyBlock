package us.talabrek.ultimateskyblock.async;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import us.talabrek.ultimateskyblock.model.Settings;
import us.talabrek.ultimateskyblock.model.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Recalculates the island-level, and stores it back on the player object.
 */
public class IslandCalculator implements Runnable {

    private final UUIDPlayerInfo player;
    private final Runnable callback;

    public IslandCalculator(UUIDPlayerInfo player, Runnable callback) {
        this.player = player;
        this.callback = callback;
    }
    @Override
    public void run() {
        try {
            Location loc;
            if (player.getHasParty())
                loc = player.getPartyIslandLocation();
            else
                loc = player.getIslandLocation();

            if (loc != null) {
                int blockCount = 0;
                int cobbleCount = 0;
                int endCount = 0;
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int radius = (Settings.island_distance / 2);
                for (int x = -radius; x <= radius; ++x) {
                    for (int y = 0; y <= 255; ++y) {
                        for (int z = -radius; z <= radius; ++z) {
                            Block b = loc.getWorld().getBlockAt(px + x, py + y, pz + z);
                            switch (b.getType()) {
                                case DIAMOND_BLOCK:
                                case EMERALD_BLOCK:
                                case BEACON:
                                case DRAGON_EGG:
                                    blockCount += 300;
                                    break;
                                case GOLD_BLOCK:
                                case ENCHANTMENT_TABLE:
                                    blockCount += 150;
                                    break;
                                case OBSIDIAN:
                                case IRON_BLOCK:
                                case REDSTONE_BLOCK:
                                    blockCount += 10;
                                    break;
                                case BOOKSHELF:
                                case JUKEBOX:
                                case HARD_CLAY:
                                case STAINED_CLAY:
                                    blockCount += 5;
                                    break;
                                case ICE:
                                case CLAY:
                                case NETHER_BRICK:
                                case GRASS:
                                case MYCEL:
                                case GLOWSTONE:
                                case NETHER_BRICK_STAIRS:
                                case QUARTZ_BLOCK:
                                case QUARTZ_STAIRS:
                                    blockCount += 3;
                                    break;
                                case SMOOTH_BRICK:
                                case BRICK:
                                case WOOL:
                                case SANDSTONE:
                                case BRICK_STAIRS:
                                case SMOOTH_STAIRS:
                                case DOUBLE_STEP:
                                case GLASS:
                                    blockCount += 2;
                                    break;
                                case COBBLESTONE:
                                    if (cobbleCount < 10000) {
                                        ++cobbleCount;
                                        ++blockCount;
                                    }
                                    break;
                                case ENDER_STONE:
                                    if (endCount < 10000) {
                                        ++endCount;
                                        ++blockCount;
                                    }
                                    break;

                                // 0 pointers
                                case WATER:
                                case STATIONARY_WATER:
                                case LAVA:
                                case STATIONARY_LAVA:
                                case AIR:
                                    break;

                                default:
                                    ++blockCount;
                                    break;
                            }
                        }
                    }
                }
                player.setIslandLevel(blockCount / 100);
            }
        } catch (Exception e) {
            uSkyBlock.getLog().severe("Error while calculating island level");
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(uSkyBlock.getInstance(), new Runnable() {
            public void run() {
                uSkyBlock.getInstance().updateTopIsland(player);
            }
        }, 0L);

        if (callback != null)
            Bukkit.getScheduler().runTask(uSkyBlock.getInstance(), callback);

    }
}
