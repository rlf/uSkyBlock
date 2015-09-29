package us.talabrek.ultimateskyblock.player;

/**
 * Transport object of PlayerInfo and Perk
 */
public class PlayerPerk {
    private final PlayerInfo playerInfo;
    private final Perk perk;

    public PlayerPerk(PlayerInfo playerInfo, Perk perk) {
        this.playerInfo = playerInfo;
        this.perk = perk;
    }

    public PlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    public Perk getPerk() {
        return perk;
    }
}