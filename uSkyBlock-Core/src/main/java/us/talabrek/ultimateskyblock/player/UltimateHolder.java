package us.talabrek.ultimateskyblock.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UltimateHolder implements InventoryHolder {

	private Player player;
	private String title;
	private MenuType menuType;

	public UltimateHolder(@Nullable Player player, @NotNull String title, @NotNull MenuType menuType) {
		this.player = player;
		this.title = title;
		this.menuType = menuType;
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return player.getInventory();
	}

	@Nullable
	public Player getPlayer() {
		return player;
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	@NotNull
	public MenuType getMenuType() {
		return menuType;
	}

	public enum MenuType {
		CONFIG,
		DEFAULT
	}
}
