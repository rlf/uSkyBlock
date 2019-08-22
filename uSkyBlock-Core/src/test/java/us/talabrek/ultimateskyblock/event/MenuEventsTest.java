package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.junit.Before;
import org.junit.Test;
import us.talabrek.ultimateskyblock.menu.ConfigMenu;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.UltimateHolder;
import us.talabrek.ultimateskyblock.player.UltimateHolder.MenuType;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class MenuEventsTest {
    private ConfigMenu fakeConfigMenu;
    private SkyBlockMenu fakeMenu;

    private MenuEvents menuEvents;

    @Before
    public void setUp() {
        uSkyBlock fakePlugin = mock(uSkyBlock.class);
        fakeConfigMenu = spy(mock(ConfigMenu.class));
        fakeMenu = spy(mock(SkyBlockMenu.class));

        doNothing().when(fakeConfigMenu).onClick(any(InventoryClickEvent.class));
        doNothing().when(fakeMenu).onClick(any(InventoryClickEvent.class));

        doReturn(fakeConfigMenu).when(fakePlugin).getConfigMenu();
        doReturn(fakeMenu).when(fakePlugin).getMenu();

        menuEvents = new MenuEvents(fakePlugin);
    }

    @Test
    public void testOnGuiClick_configMenu() {
        UltimateHolder holder = new UltimateHolder(getFakePlayer(), "Config: config.yml (1/2)", MenuType.CONFIG);
        InventoryClickEvent event = getEvent(holder);

        menuEvents.guiClick(event);
        verify(fakeConfigMenu).onClick(event);
        verify(fakeMenu, times(0)).onClick(any());
    }

    @Test
    public void testOnGuiClick_regularMenu() {
        UltimateHolder holder = new UltimateHolder(getFakePlayer(), "Island GUI menu", MenuType.DEFAULT);
        InventoryClickEvent event = getEvent(holder);

        menuEvents.guiClick(event);
        verify(fakeConfigMenu, times(0)).onClick(any());
        verify(fakeMenu).onClick(event);
    }

    @Test
    public void testOnGuiClick_noUltimateHolder() {
        InventoryHolder holder = mock(InventoryHolder.class);
        InventoryClickEvent event = getEvent(holder);

        menuEvents.guiClick(event);
        verify(fakeConfigMenu, times(0)).onClick(any());
        verify(fakeMenu, times(0)).onClick(event);
    }

    private Player getFakePlayer() {
        Player fakePlayer = mock(Player.class);
        when(fakePlayer.getUniqueId()).thenReturn(UUID.fromString("29292160-6d49-47a3-ae1c-7c800e14cca3"));
        when(fakePlayer.getName()).thenReturn("linksssofrechts");
        return fakePlayer;
    }

    private InventoryClickEvent getEvent(InventoryHolder holder) {
        // These values are not checked anywhere.
        InventoryView inventoryView = mock(InventoryView.class);
        InventoryType.SlotType slotType = InventoryType.SlotType.QUICKBAR;
        ClickType clickType = ClickType.LEFT;
        InventoryAction inventoryAction = InventoryAction.NOTHING;

        Inventory fakeInventory = mock(Inventory.class);
        doReturn(holder).when(fakeInventory).getHolder();
        doReturn(fakeInventory).when(inventoryView).getTopInventory();

        return new InventoryClickEvent(inventoryView, slotType, 1, clickType, inventoryAction);
    }
}
