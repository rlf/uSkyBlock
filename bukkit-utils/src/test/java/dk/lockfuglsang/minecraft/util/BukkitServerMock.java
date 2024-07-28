package dk.lockfuglsang.minecraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BukkitServerMock {
    public static boolean useMetaData;
    /**
     * Stubbing data, allows for advanced stubbing behaviour for item-meta
     */
    protected static Map<ItemMeta, Map<String,String>> itemMetaMap = new HashMap<>();

    public static Server setupServerMock() throws NoSuchFieldException, IllegalAccessException {
        Field server = Bukkit.class.getDeclaredField("server");
        server.setAccessible(true);
        Server serverMock = createServerMock();
        server.set(null, serverMock);
        server.setAccessible(false);
        return serverMock;
    }

    public static Server createServerMock() {
        Server serverMock = mock(Server.class);
        ItemFactory itemFactoryMock = mock(ItemFactory.class);

        when(itemFactoryMock.isApplicable(any(ItemMeta.class), any(Material.class)))
                .thenReturn(true);

        when(itemFactoryMock.equals(any(), any()))
                .thenAnswer((Answer<Boolean>) invocationOnMock -> {
                    // Better equals for mocks?
                    return Objects.equals("" + invocationOnMock.getArguments()[0],
                            "" + invocationOnMock.getArguments()[1]);
                });

        when(itemFactoryMock.getItemMeta(any(Material.class)))
                .thenAnswer((Answer<ItemMeta>) invocationOnMock -> createItemMetaStub());

        when(itemFactoryMock.asMetaFor(any(ItemMeta.class), any(Material.class)))
                .thenAnswer((Answer<ItemMeta>) invocationOnMock -> invocationOnMock.getArguments()[0] != null
                        ? (ItemMeta) invocationOnMock.getArguments()[0]
                        : null);

        when(itemFactoryMock.createItemStack(any()))
                .thenAnswer((Answer<ItemStack>) invocationOnMock -> {
                    String specification = (String) invocationOnMock.getArguments()[0];
                    var componentSplitIndex = specification.indexOf('[');
                    String typeSpecification;
                    String components;
                    if (componentSplitIndex > 0) {
                        typeSpecification = specification.substring(0, componentSplitIndex);
                        components = specification.substring(componentSplitIndex);
                    } else {
                        typeSpecification = specification;
                        components = "";
                    }
                    var type = Material.matchMaterial(typeSpecification);
                    var itemStack = new ItemStack(type);
                    var mockMeta = mock(ItemMeta.class, withSettings().extraInterfaces(Damageable.class));
                    when(((Damageable)mockMeta).getDamage()).thenReturn(0);
                    when(mockMeta.toString()).thenReturn(components);
                    when(mockMeta.getAsComponentString()).thenReturn(components.isEmpty() ? "[]" : components);
                    when(itemStack.getItemMeta()).thenReturn(mockMeta);
                    return itemStack;
                });

        when(serverMock.getItemFactory()).thenReturn(itemFactoryMock);

        UnsafeValues unsafeMock = mock(UnsafeValues.class);
        when(unsafeMock.fromLegacy(any(Material.class))).thenAnswer(a -> (Material) a.getArguments()[0]);
        when(serverMock.getUnsafe()).thenReturn(unsafeMock);
        return serverMock;
    }

    public static ItemMeta createItemMetaStub() {
        if (!useMetaData) {
            return null;
        }
        ItemMeta meta = mock(ItemMeta.class, withSettings().extraInterfaces(Damageable.class));
        when(((Damageable)meta).getDamage()).thenReturn(0);
        // Note: This is a HACKY way of stubbing, using mock and toString()
        final Map<String, String> metaData = new TreeMap<>();
        itemMetaMap.put(meta, metaData);
        doAnswer((Answer<Void>) invocationOnMock -> {
            String displayName = "" + invocationOnMock.getArguments()[0];
            if (displayName.isEmpty()) {
                metaData.remove("displayName");
            } else {
                metaData.put("displayName", "" + displayName);
            }
            return null;
        }).when(meta).setDisplayName(any(String.class));
        doAnswer((Answer<Void>) invocationOnMock -> {
            List<String> lore = (List<String>) invocationOnMock.getArguments()[0];
            if (lore != null && !lore.isEmpty()) {
                metaData.put("lore", "" + lore);
            } else {
                metaData.remove("lore");
            }
            return null;
        }).when(meta).setLore(any(List.class));
        when(meta.toString()).thenAnswer((Answer<String>) invocationOnMock -> "" + metaData);
        when(meta.clone()).thenReturn(meta); // Don't clone it - we need to verify it
        return meta;
    }
}
