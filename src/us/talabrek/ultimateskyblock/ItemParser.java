package us.talabrek.ultimateskyblock;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemParser {
	@SuppressWarnings("deprecation")
	public static ItemStack getItemStackfromString(final String s) {
		if (s.equalsIgnoreCase("")) { return null; }
		final ItemStack x = new ItemStack(1);

		for (final String thing : s.split(";")) {
			final String[] sp = thing.split(":");
			if (sp.length != 2) {
				uSkyBlock.getInstance().log.warning("error, wrong type size");
			}
			final String name = sp[0];

			if (name.equals("id")) {
				x.setTypeId(Integer.parseInt(sp[1]));
			} else if (name.equals("amount")) {
				x.setAmount(Integer.parseInt(sp[1]));
			} else if (name.equals("durab")) {
				x.setDurability((short) Integer.parseInt(sp[1]));
			} else if (name.equals("data")) {
				x.getData().setData((byte) Integer.parseInt(sp[1]));
			} else if (name.equals("ench")) {
				int enchId = 0;
				int level = 0;
				for (final String enchantment : sp[1].split(" ")) {
					final String[] prop = enchantment.split("#");
					if (prop.length != 2) {
						uSkyBlock.getInstance().log.warning("error, wrong enchantmenttype length");
					}
					if (prop[0].equals("eid")) {
						enchId = Integer.parseInt(prop[1]);
					} else if (prop[0].equals("elevel")) {
						level = Integer.parseInt(prop[1]);
						x.addUnsafeEnchantment(Enchantment.getById(enchId), level);
					}
				}
			} else {
				uSkyBlock.getInstance().log.warning("error, unknown itemvalue");
			}
		}
		return x;
	}

	@SuppressWarnings("deprecation")
	public static String parseItemStackToString(final ItemStack item) {
		if (item == null) { return ""; }

		String s = "";
		s = s + "id:" + item.getTypeId() + ";";
		s = s + "amount:" + item.getAmount() + ";";
		s = s + "durab:" + item.getDurability() + ";";
		s = s + "data:" + item.getData().getData() + ";";

		if (item.getEnchantments().size() > 0) {
			s = s + "ench:";
			for (final Enchantment e : item.getEnchantments().keySet()) {
				s = s + "eid#" + e.getId() + " ";
				s = s + "elevel#" + item.getEnchantments().get(e) + " ";
			}
		}
		return s.trim();
	}
}