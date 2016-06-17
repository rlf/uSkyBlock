package us.talabrek.ultimateskyblock.api;

/**
 * Represents a challenge-completion.
 * @since 2.7.0
 */
public interface ChallengeCompletion {
    /**
     * The name of the challenge.
     * @return name of the challenge.
     * @since 2.7.0
     */
    String getName();

    /**
     * The timestamp at which the cooldown runs out.
     * @return The timestamp at which the cooldown runs out.
     * @since 2.7.0
     */
    long getCooldownUntil();

    /**
     * Whether or not the challenge is currently on cooldown.
     * @return Whether or not the challenge is currently on cooldown.
     * @since 2.7.0
     */
    boolean isOnCooldown();

    /**
     * How many milliseconds of the cooldown is left
     * @return How many milliseconds of the cooldown is left
     * @since 2.7.0
     */
    long getCooldownInMillis();

    /**
     * Total number of times the challenge has been completed
     * @return Total number of times the challenge has been completed
     * @since 2.7.0
     */
    int getTimesCompleted();

    /**
     * Number of times the challenge has been completed within this cooldown.
     * @return Number of times the challenge has been completed within this cooldown.
     * @since 2.7.0
     */
    int getTimesCompletedInCooldown();
}
