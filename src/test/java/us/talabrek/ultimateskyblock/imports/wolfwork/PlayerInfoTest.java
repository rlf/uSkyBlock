package us.talabrek.ultimateskyblock.imports.wolfwork;

import org.junit.Test;

import java.io.ObjectInputStream;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class PlayerInfoTest {

    @Test
    public void testDeserialization() throws Exception {
        for (String playerName : new String[]{"Block_Busta_7", "Moctezuma309", "solukkajr",
                "xFreakyPVPx", "matt9959", "xXMasterOfPvpXx", "pi4music"}) {
            try (ObjectInputStream in = new WolfWorkObjectInputStream(getClass().getClassLoader().getResourceAsStream(playerName))) {
                final PlayerInfo p = (PlayerInfo) in.readObject();
                assertThat(p, notNullValue());
                System.out.println(p);
            }
        }
    }
}