package us.talabrek.ultimateskyblock.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class UltimateHolder implements InventoryHolder {

	private Player player;
	private String title;

	public UltimateHolder(Player player, String title) {
		this.player = player;
		this.title = title;
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return player.getInventory();
	}

	public Player getPlayer() {
		return player;
	}

	public String getTitle() {
		return title;
	}

}
