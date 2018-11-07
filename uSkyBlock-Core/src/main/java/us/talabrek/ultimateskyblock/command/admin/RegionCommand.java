package us.talabrek.ultimateskyblock.command.admin;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.animation.AnimationHandler;
import dk.lockfuglsang.minecraft.animation.BlockAnimation;
import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command to show the regions of interest.
 */
public class RegionCommand extends CompositeCommand {

    private final AnimationHandler animationHandler;
    private int dash;
    private Material material;

    public RegionCommand(uSkyBlock plugin, final AnimationHandler animationHandler) {
        super("region|rg", "usb.admin.region", marktr("region manipulations"));
        this.animationHandler = animationHandler;
        add(new AbstractCommand("show", marktr("shows the borders of the current island")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        dash = 3;
                        setMaterial(Material.BRICKS);
                        showRegion(player, region);
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("chunk", marktr("shows the borders of the current chunk")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Chunk chunk = player.getLocation().getChunk();
                    BlockVector2 chunkCoords = BlockVector2.at(chunk.getX(), chunk.getZ());
                    dash = 4;
                    setMaterial(Material.YELLOW_STAINED_GLASS);
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("inner", marktr("shows the borders of the inner-chunks")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<BlockVector2> borderRegions = WorldEditHandler.getInnerChunks(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        dash = 3;
                        setMaterial(Material.BLUE_STAINED_GLASS);
                        for (BlockVector2 v : borderRegions) {
                            showChunk(player, v);
                        }
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    Chunk chunk = player.getLocation().getChunk();
                    BlockVector2 chunkCoords = BlockVector2.at(chunk.getX(), chunk.getZ());
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("border", marktr("shows the non-chunk-aligned borders")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        dash = 3;
                        setMaterial(Material.LIGHT_BLUE_STAINED_GLASS);
                        for (Region rg : borderRegions) {
                            showRegion(player, rg);
                        }
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("outer", marktr("shows the borders of the outer-chunks")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<BlockVector2> borderRegions = WorldEditHandler.getOuterChunks(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        dash = 4;
                        setMaterial(Material.BLACK_STAINED_GLASS);
                        for (BlockVector2 v : borderRegions) {
                            showChunk(player, v);
                        }
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    Chunk chunk = player.getLocation().getChunk();
                    BlockVector2 chunkCoords = BlockVector2.at(chunk.getX(), chunk.getZ());
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("hide", marktr("hides the regions again")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (RegionCommand.this.animationHandler.removeAnimations(player)) {
                        sender.sendMessage(tr("\u00a7eStopped displaying regions"));
                    } else {
                        sender.sendMessage(tr("\u00a7eNo currently shown regions for this player"));
                    }
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("tick", null, "integer", marktr("set the ticks between animations")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1 && args[0].matches("[0-9]+")) {
                    int animTick = Integer.parseInt(args[0]);
                    RegionCommand.this.animationHandler.setAnimTick(animTick);
                    sender.sendMessage(tr("\u00a7eAnimation-tick changed to {0}.", animTick));
                    RegionCommand.this.animationHandler.stop();
                    RegionCommand.this.animationHandler.start();
                    return true;
                } else if (args.length == 1) {
                    sender.sendMessage(tr("\u00a7eAnimation-tick must be a valid integer."));
                }
                return false;
            }
        });
        add(new AbstractCommand("refresh", marktr("refreshes the existing animations")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                animationHandler.stop();
                animationHandler.start();
                return true;
            }
        });
    }

    private void showChunk(Player player, BlockVector2 chunk) {
        World world = player.getWorld();
        int y = player.getLocation().getBlockY();
        List<Location> points = new ArrayList<>();
        int px = chunk.getBlockX() << 4;
        int pz = chunk.getBlockZ() << 4;
        for (int x = 0; x <= 15; x+=dash) {
            points.add(new Location(world, px+x+0.5d, y, pz+0.5d));
        }
        for (int z = 0; z <= 15; z+=dash) {
            points.add(new Location(world, px+15+0.5d, y, pz+z+0.5d));
        }
        for (int x = 15; x >= 0; x-=dash) {
            points.add(new Location(world, px+x+0.5d, y, pz+15+0.5d));
        }
        for (int z = 15; z >= 0; z-=dash) {
            points.add(new Location(world, px+0.5d, y, pz+z+0.5d));
        }
        addAnimation(player, points);
    }

    private void showRegion(Player player, Region region) {
        int y = player.getLocation().getBlockY();
        BlockVector3 minP = region.getMinimumPoint();
        BlockVector3 maxP = region.getMaximumPoint();
        showRegion(player, y, minP, maxP);
    }

    private void showRegion(Player player, int y, BlockVector3 minP, BlockVector3 maxP) {
        World world = player.getWorld();
        List<Location> points = new ArrayList<>();
        for (int x = minP.getBlockX(); x <= maxP.getBlockX(); x+=dash) {
            points.add(new Location(world, x+0.5d, y, minP.getBlockZ()+0.5d));
        }
        for (int z = minP.getBlockZ(); z <= maxP.getBlockZ(); z+=dash) {
            points.add(new Location(world, maxP.getBlockX()+0.5d, y, z+0.5d));
        }
        for (int x = maxP.getBlockX(); x >= minP.getBlockX(); x-=dash) {
            points.add(new Location(world, x+0.5d, y, maxP.getBlockZ()+0.5d));
        }
        for (int z = maxP.getBlockZ(); z >= minP.getBlockZ(); z-=dash) {
            points.add(new Location(world, minP.getBlockX()+0.5d, y, z+0.5d));
        }
        addAnimation(player, points);
    }

    private void showRegion(Player player, ProtectedRegion region) {
        int y = player.getLocation().getBlockY();
        BlockVector3 minP = region.getMinimumPoint();
        BlockVector3 maxP = region.getMaximumPoint();
        showRegion(player, y, minP, maxP);
    }

    public void setMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("material cannot be null");
        }
        this.material = material;
    }

    public synchronized void addAnimation(Player player, List<Location> points) {
        animationHandler.addAnimation(new BlockAnimation(player, points, material, (byte) 0));
    }
}
