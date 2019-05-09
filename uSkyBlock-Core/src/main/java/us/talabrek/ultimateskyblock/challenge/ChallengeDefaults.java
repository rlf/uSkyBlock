package us.talabrek.ultimateskyblock.challenge;

/**
 *
 */
public class ChallengeDefaults {
    public final int resetInHours;
    public final String displayItem = "160:5";
    public final boolean requiresPreviousRank;
    public final String repeatableColor;
    public final String finishedColor;
    public final String challengeColor;
    public final int rankLeeway;
    public final boolean enableEconomyPlugin;
    public final boolean broadcastCompletion;
    public final int radius;
    public final boolean showLockedChallengeName;
    public final int repeatLimit;

    ChallengeDefaults(int resetInHours, boolean requiresPreviousRank, String repeatableColor, String finishedColor,
                      String challengeColor, int rankLeeway, boolean enableEconomyPlugin, boolean broadcastCompletion,
                      int radius, boolean showLockedChallengeName, int repeatLimit) {
        this.resetInHours = resetInHours;
        this.requiresPreviousRank = requiresPreviousRank;
        this.repeatableColor = repeatableColor;
        this.finishedColor = finishedColor;
        this.challengeColor = challengeColor;
        this.rankLeeway = rankLeeway;
        this.enableEconomyPlugin = enableEconomyPlugin;
        this.broadcastCompletion = broadcastCompletion;
        this.radius = radius;
        this.showLockedChallengeName = showLockedChallengeName;
        this.repeatLimit = repeatLimit;
    }
}
