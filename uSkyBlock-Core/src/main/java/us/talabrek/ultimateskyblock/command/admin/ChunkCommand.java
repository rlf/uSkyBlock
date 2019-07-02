package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class ChunkCommand extends CompositeCommand {
    public ChunkCommand() {
        super("chunk", "usb.admin.chunk", marktr("various chunk commands"));
        add(new RequireChunkCommand("regen", marktr("regenerate current chunk")) {
            @Override
            void doChunkCommand(Player player, Chunk chunk) {
                if (chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ())) {
                    player.sendMessage(tr("successfully regenerated chunk at {0},{1}", chunk.getX(), chunk.getZ()));
                } else {
                    player.sendMessage(tr("\u00a74FAILED!\u00a7e could not regenerate chunk at {0},{1}", chunk.getX(), chunk.getZ()));
                }
            }
        });
        add(new RequireChunkCommand("unload", marktr("unload current chunk")) {
            @Override
            void doChunkCommand(Player player, Chunk chunk) {
                if (chunk.getWorld().unloadChunk(chunk.getX(), chunk.getZ(), false)) {
                    player.sendMessage(tr("successfully unloaded chunk at {0},{1}", chunk.getX(), chunk.getZ()));
                } else {
                    player.sendMessage(tr("\u00a74FAILED!\u00a7e could not unload chunk at {0},{1}", chunk.getX(), chunk.getZ()));
                }
            }
        });
        add(new RequireChunkCommand("load", marktr("load current chunk")) {
            @Override
            void doChunkCommand(Player player, Chunk chunk) {
                chunk.getWorld().loadChunk(chunk.getX(), chunk.getZ(), true);
                player.sendMessage(tr("loaded chunk at {0},{1}", chunk.getX(), chunk.getZ()));
            }
        });
    }

    public abstract class RequireChunkCommand extends AbstractCommand {
        public RequireChunkCommand(String name, String description) {
            super(name, null, "?x ?z ?r", description);
        }

        @Override
        public boolean execute(CommandSender commandSender, String alias, Map<String, Object> map, String... args) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(tr("only available for players"));
                return false;
            }
            Player player = (Player) commandSender;
            World world = player.getLocation().getWorld();
            int x = 0;
            int z = 0;
            int r = 0;
            if (args.length == 0) {
                Chunk chunk = player.getLocation().getChunk();
                x = chunk.getX();
                z = chunk.getZ();
            }
            if (args.length > 0 && args[0].matches("-?[0-9]+")) {
                x = Integer.parseInt(args[0], 10);
            }
            if (args.length > 1 && args[1].matches("-?[0-9]+")) {
                z = Integer.parseInt(args[1], 10);
            }
            if (args.length > 2 && args[2].matches("[0-9]+")) {
                r = Integer.parseInt(args[2], 10);
            }
            try {
                for (int cx = x - r; cx <= x + r; cx++) {
                    for (int cz = z - r; cz <= z + r; cz++) {
                        Chunk chunk = world.getChunkAt(cx, cz);
                        doChunkCommand(player, chunk);
                    }
                }
            } catch (Exception e) {
                player.sendMessage(tr("\u00a74ERROR:\u00a7e {0}", e.getMessage()));
            }
            return true;
        }

        abstract void doChunkCommand(Player player, Chunk chunk);
    }
}
