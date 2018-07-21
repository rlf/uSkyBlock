package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.util.BukkitServerMock;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ChallengeFactoryTest {

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        BukkitServerMock.setupServerMock();
    }

    @Test
    public void createChallenge_IronGolem() {
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

    @Test
    public void createChallenge_ManyItems() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("challengefactory/manyRequiredItems.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceAsStream));
        ChallengeDefaults defaults = ChallengeFactory.createDefaults(config.getRoot());
        ConfigurationSection rankSection = config.getConfigurationSection("ranks.Tier1");
        Rank rank = new Rank(rankSection, null, defaults);
        Challenge challenge = ChallengeFactory.createChallenge(rank, rankSection.getConfigurationSection("challenges.villageguard"), defaults);

        assertThat(challenge, notNullValue());
        List<ItemStack> requiredItems = challenge.getRequiredItems(0);
        assertThat(requiredItems.size(), is(1));
        assertThat(ItemStackUtil.asString(requiredItems.get(0)), is(ItemStackUtil.asString(new ItemStack(Material.COBBLESTONE, 257))));
    }

}