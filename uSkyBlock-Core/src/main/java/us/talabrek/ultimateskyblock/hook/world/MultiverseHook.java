package us.talabrek.ultimateskyblock.hook.world;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.hook.PluginHook;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Optional;

public class MultiverseHook extends PluginHook {
    private MultiverseCore mvCore;

    private static final String GENERATOR_NAME = "uSkyBlock";

    public MultiverseHook(@NotNull uSkyBlock plugin) {
        super(plugin, "Multiverse", "Multiverse");
        setupCore().ifPresent(mvPlugin -> this.mvCore = mvPlugin);
    }

    private Optional<MultiverseCore> setupCore() {
        Plugin mvPlugin = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin instanceof MultiverseCore) {
            return Optional.of((MultiverseCore) mvPlugin);
        }

        return Optional.empty();
    }

    /**
     * Registers the given {@link World} to {@link MultiverseCore} as the skyblock overworld (skyworld).
     * @param world World to register.
     */
    public void registerOverworld(@NotNull World world) {
        if (!mvCore.getMVWorldManager().isMVWorld(world)) {
            mvCore.getMVWorldManager().addWorld(world.getName(), World.Environment.NORMAL,
                "0", WorldType.NORMAL, false, GENERATOR_NAME, false);
        }

        MultiverseWorld mvWorld = mvCore.getMVWorldManager().getMVWorld(world);
        mvWorld.setEnvironment(World.Environment.NORMAL);
        mvWorld.setScaling(1.0);
        mvWorld.setGenerator(GENERATOR_NAME);

        if (Settings.general_spawnSize > 0 && LocationUtil.isEmptyLocation(mvWorld.getSpawnLocation())) {
            Location spawn = LocationUtil.centerOnBlock(
                new Location(world, 0.5, Settings.island_height + 0.1, 0.5));
            mvWorld.setAdjustSpawn(false);
            mvWorld.setSpawnLocation(spawn);
            world.setSpawnLocation(spawn);
        }

        if (!Settings.extras_sendToSpawn) {
            mvWorld.setRespawnToWorld(mvWorld.getName());
        }
    }

    /**
     * Registers the given {@link World} to {@link MultiverseCore} as the skyblock nether world (skyworld_nether).
     * @param world World to register.
     */
    public void registerNetherworld(@NotNull World world) {
        if (!mvCore.getMVWorldManager().isMVWorld(world)) {
            mvCore.getMVWorldManager().addWorld(world.getName(), World.Environment.NETHER,
                "0", WorldType.NORMAL, false, GENERATOR_NAME, false);
        }

        MultiverseWorld mvWorld = mvCore.getMVWorldManager().getMVWorld(world);
        mvWorld.setEnvironment(World.Environment.NETHER);
        mvWorld.setScaling(1.0);
        mvWorld.setGenerator(GENERATOR_NAME);
        if (Settings.general_spawnSize > 0 && LocationUtil.isEmptyLocation(mvWorld.getSpawnLocation())) {
            Location spawn = LocationUtil.centerOnBlock(
                new Location(world, 0.5, Settings.island_height/2.0 + 0.1, 0.5));
            mvWorld.setAdjustSpawn(false);
            mvWorld.setSpawnLocation(spawn);
            world.setSpawnLocation(spawn);
        }

        if (!Settings.extras_sendToSpawn) {
            mvWorld.setRespawnToWorld(plugin.getWorldManager().getWorld().getName());
        }
    }
}
