package us.talabrek.ultimateskyblock.hook.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Optional;

public class VaultEconomy extends EconomyHook {
    private Economy economy;

    public VaultEconomy(@NotNull uSkyBlock plugin) {
        super(plugin, "Vault");
        setupEconomy().ifPresent((economy) -> this.economy = economy);
    }

    private Optional<Economy> setupEconomy() {
        RegisteredServiceProvider<Economy> rsp =
            plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            return Optional.of(economy);
        }

        return Optional.empty();
    }

    @Override
    public @NotNull String getCurrenyName() {
        return economy.currencyNamePlural();
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0;
    }

    @Override
    public boolean depositPlayer(@NotNull OfflinePlayer player, double amount) {
        if (economy != null) {
            return economy.depositPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    @Override
    public boolean withdrawPlayer(@NotNull OfflinePlayer player, double amount) {
        if (economy != null) {
            return economy.depositPlayer(player, amount).transactionSuccess();
        }
        return false;
    }
}
