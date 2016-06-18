package us.talabrek.ultimateskyblock.command.completion;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ParticleTabCompleter extends AbstractTabCompleter {
    private final List<String> particles = new ArrayList<>();
    public ParticleTabCompleter() {
        for (Particle p : Particle.values()) {
            particles.add(p.name().toLowerCase());
        }
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        return particles;
    }
}
