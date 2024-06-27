package dk.lockfuglsang.minecraft.util;

import dk.lockfuglsang.minecraft.nbt.NBTItemStackTagger;
import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static dk.lockfuglsang.minecraft.util.ItemStackMatcher.itemStack;
import static dk.lockfuglsang.minecraft.util.ItemStackMatcher.itemStacks;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the utility methods of ItemStackUtil
 */
public class ItemStackUtilTest extends BukkitServerMock {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Server server = setupServerMock();
    }

    @Before
    public void setUp() {
        useMetaData = false;
        itemMetaMap.clear();
    }

    @Test(expected = NullPointerException.class)
    public void createItemsWithProbabiltyNull() throws Exception {
        ItemStackUtil.createItemsWithProbabilty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createItemsWithProbabiltyInvalid() throws Exception {
        ItemStackUtil.createItemsWithProbabilty(Arrays.asList("{p:0.9}10:1"));
    }

    @Test
    public void createItemsWithProbabilty1() throws Exception {
        List<ItemStackUtil.ItemProbability> actual = ItemStackUtil.createItemsWithProbabilty(Arrays.asList("{p=0.9}LAVA_BUCKET:1"));
        List<ItemStackUtil.ItemProbability> expected = Arrays.asList(
                new ItemStackUtil.ItemProbability(0.9, new ItemStack(Material.LAVA_BUCKET, 1))
        );
        assertThat(actual, notNullValue());
        assertThat(actual, is(expected));
    }

    @Test
    public void createItemsWithProbabiltyN() throws Exception {
        List<ItemStackUtil.ItemProbability> actual = ItemStackUtil.createItemsWithProbabilty(Arrays.asList(
                "{p=0.9}LAVA_BUCKET:1",
                "{p=0.2}STONE:2:3",
                "{p=0.3}NETHER_BRICK_FENCE:2"
        ));
        List<ItemStackUtil.ItemProbability> expected = Arrays.asList(
                new ItemStackUtil.ItemProbability(0.9, new ItemStack(Material.LAVA_BUCKET, 1)),
                new ItemStackUtil.ItemProbability(0.2, new ItemStack(Material.STONE, 3, (short) 2)),
                new ItemStackUtil.ItemProbability(0.3, new ItemStack(Material.NETHER_BRICK_FENCE, 2))
        );
        assertThat(actual, notNullValue());
        assertThat(actual, is(expected));
    }

    @Test
    public void createItemsWithProbabiltyWithNBTTag() throws Exception {
        useMetaData = true;
        List<ItemStackUtil.ItemProbability> actual = ItemStackUtil.createItemsWithProbabilty(Arrays.asList(
                "{p=0.9}LAVA_BUCKET:1{Potion:Death}",
                "{p=0.2}STONE:2:3 {MyLittle:\"Pony\"}",
                "{p=0.3}NETHER_BRICK_FENCE:2\t {meta:{nested:{data:[{},{}]}}}"
        ));
        List<ItemStackUtil.ItemProbability> expected = Arrays.asList(
                new ItemStackUtil.ItemProbability(0.9, NBTUtil.setNBTTag(
                        new ItemStack(Material.LAVA_BUCKET, 1),
                        "{Potion:Death}")),
                new ItemStackUtil.ItemProbability(0.2, NBTUtil.setNBTTag(
                        new ItemStack(Material.STONE, 3, (short) 2),
                        "{MyLittle:\"Pony\"}")),
                new ItemStackUtil.ItemProbability(0.3, NBTUtil.setNBTTag(
                        new ItemStack(Material.NETHER_BRICK_FENCE, 2),
                        "{meta:{nested:{data:[{},{}]}}}"))
        );
        assertThat(actual, notNullValue());
        assertThat(actual, is(expected));
        assertThat(NBTUtil.getNBTTag(actual.get(2).getItem()), is("{meta:{nested:{data:[{},{}]}}}"));
    }

    @Test(expected = NullPointerException.class)
    public void createItemListNull() throws Exception {
        ItemStackUtil.createItemList((List) null);
    }

    @Test
    public void createItemListInvalid() throws Exception {
        try {
            ItemStackUtil.createItemList(Arrays.asList("DART"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unknown item: 'DART'"));
        }
    }

    @Test
    public void createItemList() throws Exception {
        List<ItemStack> actual = ItemStackUtil.createItemList(Arrays.asList(
                "LAVA_BUCKET:1",
                "STONE:2:3",
                "NETHER_BRICK_FENCE:2"
        ));
        List<ItemStack> expected = Arrays.asList(
                new ItemStack(Material.LAVA_BUCKET, 1),
                new ItemStack(Material.STONE, 3, (short) 2),
                new ItemStack(Material.NETHER_BRICK_FENCE, 2)
        );
        assertThat(actual, itemStacks(expected));
    }

    @Test
    public void createItemListStringAndListWithNBTTags() throws Exception {
        useMetaData = true;
        List<ItemStack> actual = ItemStackUtil.createItemList(Arrays.asList("NETHER_BRICK_FENCE:2{meta2}", "JUNGLE_WOOD:256 {meta3}"));
        List<ItemStack> expected = Arrays.asList(
                NBTUtil.setNBTTag(new ItemStack(Material.NETHER_BRICK_FENCE, 2), "{meta2}"),
                NBTUtil.setNBTTag(new ItemStack(Material.JUNGLE_WOOD, 256), "{meta3}") // Jungle Wood Planks
        );
        assertThat(actual, is(expected));
        assertThat(actual.get(1).getAmount(), is(256));
        assertThat(NBTUtil.getNBTTag(actual.get(0)), is("{meta2}"));
    }

    @Test
    public void createItemArrayNull() throws Exception {
        ItemStack[] actual = ItemStackUtil.createItemArray(null);
        assertThat(actual, is(new ItemStack[0]));
    }

    @Test
    public void createItemArrayEmpty() throws Exception {
        ItemStack[] actual = ItemStackUtil.createItemArray(Collections.<ItemStack>emptyList());
        assertThat(actual, is(new ItemStack[0]));
    }

    @Test
    public void createItemArray() throws Exception {
        List<ItemStack> expected = Arrays.asList(
                new ItemStack(Material.LAVA_BUCKET, 1),
                new ItemStack(Material.STONE, 3, (short) 2),
                new ItemStack(Material.NETHER_BRICK_FENCE, 2),
                new ItemStack(Material.JUNGLE_WOOD, 256) // Jungle Wood Planks
        );
        ItemStack[] actual = ItemStackUtil.createItemArray(expected);
        assertThat(actual, is(expected.toArray()));
    }

    @Test
    @Ignore("Bukkit.getUnsafe() is not available in test-runner")
    public void createItemStack_LegacyColor() throws Exception {
        ItemStack actual = ItemStackUtil.createItemStack("STAINED_GLASS_PANE:14");
        assertThat(actual.getType(), is(Material.RED_STAINED_GLASS_PANE));
        assertThat(actual.getDurability(), is(0));
    }

    @Test
    public void createItemStackName() throws Exception {
        ItemStack actual = ItemStackUtil.createItemStack("DIRT");
        ItemStack expected = new ItemStack(Material.DIRT, 1);
        assertThat(actual, itemStack(expected));

        actual = ItemStackUtil.createItemStack("STONE:2"); // Diorite
        expected = new ItemStack(Material.STONE, 1, (short) 2);
        assertThat(actual, itemStack(expected));
    }

    @Test
    public void createItemStackWithMeta() throws Exception {
        useMetaData = true;
        ItemStack actual = ItemStackUtil.createItemStack("STONE:2", "&lMy Title", "Hello &4World");
        ItemStack expected = new ItemStack(Material.STONE, 1, (short) 2);
        ItemMeta itemMeta = expected.getItemMeta();
        itemMeta.setDisplayName("\u00a7lMy Title");
        itemMeta.setLore(Arrays.asList("Hello \u00a74World"));
        expected.setItemMeta(itemMeta);

        assertThat(actual.getItemMeta(), notNullValue());
        verify(actual.getItemMeta()).setDisplayName("\u00a7lMy Title");
        assertThat(actual, is(expected));
    }

    @Test
    public void createItemStackWithMetaNBTTag() throws Exception {
        useMetaData = true;
        ItemStack actual = ItemStackUtil.createItemStack("STONE {display:{Name:\"Hi mom\"}}", "&lMy Title", "Hello &4World");
        ItemStack expected = new ItemStack(Material.STONE, 1);
        ItemMeta itemMeta = expected.getItemMeta();
        itemMeta.setDisplayName("\u00a7lMy Title");
        itemMeta.setLore(Arrays.asList("Hello \u00a74World"));
        expected.setItemMeta(itemMeta);
        expected = NBTUtil.setNBTTag(expected, "{display:{Name:\"Hi mom\"}}");

        assertThat(actual.getItemMeta(), notNullValue());
        verify(actual.getItemMeta()).setDisplayName("\u00a7lMy Title");
        assertThat(actual, is(expected));
        // Also verify the string, just to be extra sure
        assertThat(actual.toString(), is("ItemStack{STONE x 1, {displayName=\u00a7lMy Title, lore=[Hello \u00a74World], nbt={display:{Name:\"Hi mom\"}}}}"));
    }

    @Test
    public void testCloneNull() throws Exception {
        List<ItemStack> clone = ItemStackUtil.clone(null);
        assertThat(clone, nullValue());
    }

    @Test
    public void testClone() throws Exception {
        List<ItemStack> orig = new ArrayList<>(Arrays.asList(
                new ItemStack(Material.LAVA, 1),
                new ItemStack(Material.DIRT, 2),
                new ItemStack(Material.STONE, 3)
        ));
        List<ItemStack> clone = ItemStackUtil.clone(orig);
        assertThat(clone, itemStacks(orig));
        orig.get(0).setAmount(10);
        orig.get(1).setAmount(20);
        orig.remove(2);
        assertThat(clone, not(orig));
        assertThat(clone.size(), is(3));
        assertThat(clone.get(1).getAmount(), is(2));
    }

}