package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.util.BukkitServerMock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ChallengeFactoryTest {

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        BukkitServerMock.setupServerMock();
    }

    @Test
    public void createChallenge() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("challengefactory/requiredEntities.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceAsStream));
        ChallengeDefaults defaults = ChallengeFactory.createDefaults(config.getRoot());
        ConfigurationSection rankSection = config.getConfigurationSection("ranks.Tier1");
        Rank rank = new Rank(rankSection, null, defaults);
        Challenge challenge = ChallengeFactory.createChallenge(rank, rankSection.getConfigurationSection("challenges.villageguard"), defaults);

        assertThat(challenge, notNullValue());
        assertThat(challenge.getRequiredEntities().size(), is(2));
        assertThat(challenge.getRequiredEntities().get(0).getType(), is(EntityType.VILLAGER));
        assertThat(challenge.getRequiredEntities().get(1).getType(), is(EntityType.IRON_GOLEM));
    }
}