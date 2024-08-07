package us.talabrek.ultimateskyblock.player;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Perk is an immutable object holding all the perks,
 */
public class Perk {
    private final int maxPartySize;
    private final int animals;
    private final int monsters;
    private final List<ItemStack> extraItems;
    private final double rewBonus;
    private final double hungerReduction;
    private final Set<String> schematics;
    private final Map<Material, Integer> blockLimits;
    private final int villagers;
    private final int golems;

    Perk(List<ItemStack> extraItems,
         int maxPartySize,
         int animals,
         int monsters,
         int villagers,
         int golems,
         double rewBonus,
         double hungerReduction,
         List<String> schematics,
         Map<Material, Integer> blockLimits) {
        this.maxPartySize = Math.max(maxPartySize, 0);
        this.animals = Math.max(animals, 0);
        this.monsters = Math.max(monsters, 0);
        this.villagers = Math.max(villagers, 0);
        this.golems = Math.max(golems, 0);
        this.extraItems = extraItems != null ? extraItems : Collections.emptyList();
        this.rewBonus = rewBonus >= 0 ? rewBonus : 0;
        this.hungerReduction = hungerReduction >= 0 && hungerReduction <= 1 ? hungerReduction : 0;
        this.schematics = schematics != null ? new HashSet<>(schematics) : Collections.emptySet();
        this.blockLimits = blockLimits != null ? new HashMap<>(blockLimits) : Collections.emptyMap();
    }

    public int getMaxPartySize() {
        return maxPartySize;
    }

    public int getAnimals() {
        return animals;
    }

    public int getMonsters() {
        return monsters;
    }

    public int getVillagers() {
        return villagers;
    }

    public int getGolems() {
        return golems;
    }

    public List<ItemStack> getExtraItems() {
        return ItemStackUtil.clone(extraItems);
    }

    public Map<Material, Integer> getBlockLimits() {
        return blockLimits;
    }

    public double getRewBonus() {
        return rewBonus;
    }

    public double getHungerReduction() {
        return hungerReduction;
    }

    public Set<String> getSchematics() {
        return Collections.unmodifiableSet(schematics);
    }

    public Perk combine(Perk other) {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(this.extraItems);
        items.addAll(other.getExtraItems());
        List<String> schems = new ArrayList<>();
        schems.addAll(this.schematics);
        schems.addAll(other.getSchematics());
        return new Perk(
            items,
            Math.max(maxPartySize, other.getMaxPartySize()),
            Math.max(animals, other.getAnimals()),
            Math.max(monsters, other.getMonsters()),
            Math.max(villagers, other.getVillagers()),
            Math.max(golems, other.getGolems()),
            Math.max(rewBonus, other.getRewBonus()),
            Math.max(hungerReduction, other.getHungerReduction()),
            schems, null);
    }

    @Override
    public String toString() {
        return (maxPartySize > 0 ? "maxPartySize:" + maxPartySize + "\n" : "") +
            (animals > 0 ? "animals:" + animals + "\n" : "") +
            (monsters > 0 ? "monsters:" + monsters + "\n" : "") +
            (villagers > 0 ? "villagers:" + villagers + "\n" : "") +
            (golems > 0 ? "golems:" + golems + "\n" : "") +
            (!extraItems.isEmpty() ? "extraItems:" + ItemStackUtil.asShortString(extraItems) + "\n" : "") +
            (rewBonus > 0 ? "rewBonus:" + rewBonus + "\n" : "") +
            (hungerReduction > 0 ? "hungerReduction:" + hungerReduction + "\n" : "") +
            (!schematics.isEmpty() ? "schematics:" + schematics + "\n" : "") +
            (!blockLimits.isEmpty() ? "blockLimits:" + blockLimits + "\n" : "");
    }
}
