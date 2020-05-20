package us.talabrek.ultimateskyblock.hook.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.hook.PluginHook;
import us.talabrek.ultimateskyblock.uSkyBlock;

public abstract class EconomyHook extends PluginHook {
    public EconomyHook(@NotNull uSkyBlock plugin, @NotNull String implementing) {
        super(plugin, "Economy", implementing);
    }

    /**
     * Gets balance of an {@link OfflinePlayer}
     * @param player of the player
     * @return Amount currently held in players account
     */
    public abstract double getBalance(@NotNull OfflinePlayer player);

    /**
     * Deposit an amount to an {@link OfflinePlayer} - DO NOT USE NEGATIVE AMOUNTS
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    public abstract boolean depositPlayer(@NotNull OfflinePlayer player, double amount);

    /**
     * Withdraw an amount from an {@link OfflinePlayer} - DO NOT USE NEGATIVE AMOUNTS
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    public abstract boolean withdrawPlayer(@NotNull OfflinePlayer player, double amount);

    /**
     * Returns the icon or name of the currency in plural form. Defaults to $.
     * @return icon or of the currency
     */
    public @NotNull String getCurrenyName() {
        return "$";
    }
}
