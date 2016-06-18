package us.talabrek.ultimateskyblock.command.admin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.command.completion.ParticleTabCompleter;
import us.talabrek.ultimateskyblock.handler.ParticleHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command to show the regions of interest.
 */
public class RegionCommand extends CompositeCommand {
    private final uSkyBlock plugin;

    private final Map<UUID, Set<Animation>> animations = new ConcurrentHashMap<>();
    private AnimationTask animationTask;

    private int animCount = 10;
    private Particle particle = Particle.REDSTONE;
    private int animTick = 20;
    private int dash = 1;

    public RegionCommand(uSkyBlock plugin) {
        super("region|rg", "usb.admin.region", tr("region manipulations"));
        this.plugin = plugin;
        addTab("particle", new ParticleTabCompleter());
        add(new AbstractCommand("show", tr("shows the borders of the current island")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(player.getLocation());
                    if (region != null) {
                        World world = player.getWorld();
                        int y = player.getLocation().getBlockY();
                        BlockVector minP = region.getMinimumPoint();
                        BlockVector maxP = region.getMaximumPoint();
                        List<Location> points = new ArrayList<>();
                        for (int x = minP.getBlockX(); x <= maxP.getBlockX(); x+=dash) {
                            points.add(new Location(world, x+0.5d, y, minP.getBlockZ()+0.5d));
                            points.add(new Location(world, x+0.5d, y, maxP.getBlockZ()+0.5d));
                        }
                        for (int z = minP.getBlockZ(); z <= maxP.getBlockZ(); z+=dash) {
                            points.add(new Location(world, minP.getBlockX()+0.5d, y, z+0.5d));
                            points.add(new Location(world, maxP.getBlockX()+0.5d, y, z+0.5d));
                        }
                        if (animations.get(player.getUniqueId()) == null) {
                            animations.put(player.getUniqueId(), new HashSet<Animation>());
                        }
                        animations.get(player.getUniqueId()).add(new Animation(player, particle, points));
                        startAnimations();
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
        add(new AbstractCommand("hide", tr("hides the regions again")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (animations.containsKey(player.getUniqueId())) {
                        animations.remove(player.getUniqueId());
                        if (animations.isEmpty()) {
                            stopAnimations();
                        }
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
        add(new AbstractCommand("particle", null, "particle", "set the particle used in animations") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                try {
                    if (args.length == 1 && Particle.valueOf(args[0].toUpperCase()) != null) {
                        particle = Particle.valueOf(args[0].toUpperCase());
                        sender.sendMessage(tr("\u00a7eParticle changed to {0}.", particle.name()));
                        return true;
                    } else if (args.length == 1) {
                        sender.sendMessage(tr("\u00a7eParticle must be a valid Bukkit Particle."));
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(tr("\u00a7eParticle must be a valid Bukkit Particle."));
                }
                return false;
            }
        });
        add(new AbstractCommand("count", null, "integer", "set the count of particles") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1 && args[0].matches("[0-9]+")) {
                    animCount = Integer.parseInt(args[0]);
                    sender.sendMessage(tr("\u00a7eParticle-count changed to {0}.", animCount));
                    return true;
                } else if (args.length == 1) {
                    sender.sendMessage(tr("\u00a7eParticle-count must be a valid integer."));
                }
                return false;
            }
        });
        add(new AbstractCommand("dash", null, "integer", "set the dash-size of animation") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1 && args[0].matches("[0-9]+")) {
                    dash = Integer.parseInt(args[0]);
                    sender.sendMessage(tr("\u00a7eAnimation-dash changed to {0}.", dash));
                    return true;
                } else if (args.length == 1) {
                    sender.sendMessage(tr("\u00a7eAnimation-dash must be a valid integer."));
                }
                return false;
            }
        });
        add(new AbstractCommand("tick", null, "integer", "set the ticks between animations") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1 && args[0].matches("[0-9]+")) {
                    animTick = Integer.parseInt(args[0]);
                    sender.sendMessage(tr("\u00a7eAnimation-tick changed to {0}.", animTick));
                    stopAnimations();
                    startAnimations();
                    return true;
                } else if (args.length == 1) {
                    sender.sendMessage(tr("\u00a7eAnimation-tick must be a valid integer."));
                }
                return false;
            }
        });
        animCount = plugin.getConfig().getInt("animations.count", 1);
        particle = Particle.valueOf(plugin.getConfig().getString("animations.particle", "DRIP_LAVA"));
        animTick = plugin.getConfig().getInt("animations.tick", 20);
        dash = plugin.getConfig().getInt("animations.dash", 3);
    }

    private void startAnimations() {
        if (animationTask == null && !animations.isEmpty()) {
            animationTask = new AnimationTask();
            animationTask.runTaskTimerAsynchronously(plugin, 0, animTick);
        }
    }

    private void stopAnimations() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    private class AnimationTask extends BukkitRunnable {
        @Override
        public void run() {
            Collection<Set<Animation>> anims = animations.values();
            for (Set<Animation> animSet : anims) {
                for (Animation animation : animSet) {
                    if (!animation.show()) {
                        UUID uuid = animation.getPlayer().getUniqueId();
                        animations.get(uuid).remove(animation);
                        if (animations.get(uuid).isEmpty()) {
                            animations.remove(uuid);
                        }
                    }
                }
            }
        }
    }

    private class Animation {
        private final Player player;
        private final Particle particle;
        private final List<Location> points;

        Animation(Player player, Particle particle, List<Location> points) {
            this.player = player;
            this.particle = particle;
            this.points = points;
        }

        boolean show() {
            if (!player.isOnline()) {
                return false;
            }
            for (Location loc : points) {
                if (!ParticleHandler.spawnParticle(player, particle, loc, animCount)) {
                    return false;
                }
            }
            return true;
        }

        public Player getPlayer() {
            return player;
        }
    }
}
