package us.talabrek.ultimateskyblock.menu;

import org.bukkit.entity.Player;

/**
 * Supports injection of ParameterEvaluators.
 */
public interface EvaluatorProvider {
    ParameterEvaluator getEvaluator(Player player);
}
