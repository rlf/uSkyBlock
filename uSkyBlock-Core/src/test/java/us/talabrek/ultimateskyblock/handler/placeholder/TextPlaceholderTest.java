package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by R4zorax on 26/04/2016.
 */
public class TextPlaceholderTest {
    @Test
    public void replacePlaceholders() throws Exception {
        TextPlaceholder placeholder = new TextPlaceholder();
        placeholder.registerPlaceholder(null, new PlaceholderAPI.PlaceholderReplacer() {
            @Override
            public Set<String> getPlaceholders() {
                return new HashSet<>(Arrays.asList("usb_replaceme"));
            }

            @Override
            public String replace(OfflinePlayer offlinePlayer, Player player, String placeholder) {
                return "replaced string";
            }
        });
        assertThat(placeholder.replacePlaceholders(null, null), is(nullValue()));
        assertThat(placeholder.replacePlaceholders(null, "Hi {uskyblock_island_level}"), is("Hi {uskyblock_island_level}"));
        assertThat(placeholder.replacePlaceholders(null, "Hi {usb_island_level}"), is("Hi {usb_island_level}"));
        assertThat(placeholder.replacePlaceholders(null, "Hi {usb_replaceme} please"), is("Hi replaced string please"));
    }


}