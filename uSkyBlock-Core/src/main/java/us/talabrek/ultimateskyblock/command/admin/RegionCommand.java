package us.talabrek.ultimateskyblock.command.admin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dk.lockfuglsang.minecraft.animation.Animation;
import dk.lockfuglsang.minecraft.animation.AnimationHandler;
import dk.lockfuglsang.minecraft.animation.BlockAnimation;
import dk.lockfuglsang.minecraft.animation.ParticleAnimation;
import us.talabrek.ultimateskyblock.command.completion.ParticleTabCompleter;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command to show the regions of interest.
 */
public class RegionCommand extends CompositeCommand {

    private final AnimationHandler animationHandler;
    private int dash;
    private int animCount;
    private String animType;
    private Particle particle;
    private Material material;
    private byte material_data;

    public RegionCommand(uSkyBlock plugin, final AnimationHandler animationHandler) {
        super("region|rg", "usb.admin.region", tr("region manipulations"));
        this.animationHandler = animationHandler;
        addTab("particle", new ParticleTabCompleter());
        add(new AbstractCommand("show", tr("shows the borders of the current island")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        setAnimType("block");
                        dash = 3;
                        setMaterial(Material.BRICK, (byte) 0);
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
        add(new AbstractCommand("chunk", tr("shows the borders of the current chunk")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Chunk chunk = player.getLocation().getChunk();
                    Vector2D chunkCoords = new Vector2D(chunk.getX(), chunk.getZ());
                    setAnimType("block");
                    dash = 4;
                    setMaterial(Material.STAINED_GLASS, (byte) 4);
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("inner", tr("shows the borders of the inner-chunks")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<Vector2D> borderRegions = WorldEditHandler.getInnerChunks(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        setAnimType("block");
                        dash = 3;
                        setMaterial(Material.STAINED_GLASS, (byte) 11);
                        for (Vector2D v : borderRegions) {
                            showChunk(player, v);
                        }
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    Chunk chunk = player.getLocation().getChunk();
                    Vector2D chunkCoords = new Vector2D(chunk.getX(), chunk.getZ());
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("border", tr("shows the non-chunk-aligned borders")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        setAnimType("block");
                        dash = 3;
                        setMaterial(Material.STAINED_GLASS, (byte) 3);
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
        add(new AbstractCommand("outer", tr("shows the borders of the outer-chunks")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        Set<Vector2D> borderRegions = WorldEditHandler.getOuterChunks(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint()));
                        setAnimType("block");
                        dash = 4;
                        setMaterial(Material.STAINED_GLASS, (byte) 15);
                        for (Vector2D v : borderRegions) {
                            showChunk(player, v);
                        }
                    } else {
                        sender.sendMessage(tr("\u00a7eNo island found at your current location"));
                    }
                    Chunk chunk = player.getLocation().getChunk();
                    Vector2D chunkCoords = new Vector2D(chunk.getX(), chunk.getZ());
                    showChunk(player, chunkCoords);
                    return true;
                } else {
                    sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                }
                return false;
            }
        });
        add(new AbstractCommand("hide", tr("hides the regions again")) {
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
        add(new AbstractCommand("tick", null, "integer", "set the ticks between animations") {
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
        add(new AbstractCommand("refresh", "refreshes the existing animations") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                animationHandler.stop();
                animationHandler.start();
                return true;
            }
        });
    }

    private void showChunk(Player player, Vector2D chunk) {
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
        Vector minP = region.getMinimumPoint();
        Vector maxP = region.getMaximumPoint();
        showRegion(player, y, minP, maxP);
    }

    private void showRegion(Player player, int y, Vector minP, Vector maxP) {
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
        BlockVector minP = region.getMinimumPoint();
        BlockVector maxP = region.getMaximumPoint();
        showRegion(player, y, minP, maxP);
    }

    public void setAnimType(String animType) {
        this.animType = animType;
    }
    
    public void setAnimCount(int animCount) {
        this.animCount = animCount;
    }

    public void setParticle(Particle particle) {
        if (particle == null) {
            throw new IllegalArgumentException("particle cannot be null");
        }
        this.particle = particle;
    }

    public void setMaterial(Material material, byte data) {
        if (material == null) {
            throw new IllegalArgumentException("material cannot be null");
        }
        this.material = material;
        this.material_data = data;
    }

    public synchronized void addAnimation(Player player, List<Location> points) {
        Animation animation = null;
        if (animType.equalsIgnoreCase("block")) {
            animation = new BlockAnimation(player, points, material, material_data);
        } else {
            animation = new ParticleAnimation(player, points, particle, animCount);
        }
        animationHandler.addAnimation(animation);
    }
}
