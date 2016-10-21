package us.talabrek.ultimateskyblock.challenge;

public class ChallengeCompletion implements us.talabrek.ultimateskyblock.api.ChallengeCompletion {
    private String name;
    private long cooldownUntil;
    private int timesCompleted;
    private int timesCompletedInCooldown;

    public ChallengeCompletion(final String name) {
        super();
        this.name = name;
        this.cooldownUntil = 0L;
        this.timesCompleted = 0;
    }

    public ChallengeCompletion(final String name, final long cooldownUntil, final int timesCompleted, final int timesCompletedInCooldown) {
        super();
        this.name = name;
        this.cooldownUntil = cooldownUntil;
        this.timesCompleted = timesCompleted;
        this.timesCompletedInCooldown = timesCompletedInCooldown;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public long getCooldownUntil() {
        return this.cooldownUntil;
    }

    @Override
    public boolean isOnCooldown() {
        return cooldownUntil < 0 || cooldownUntil > System.currentTimeMillis();
    }

    @Override
    public long getCooldownInMillis() {
        if (cooldownUntil < 0) {
            return -1;
        }
        long now = System.currentTimeMillis();
        return cooldownUntil > now ? cooldownUntil - now : 0;
    }

    @Override
    public int getTimesCompleted() {
        return this.timesCompleted;
    }

    public int getTimesCompletedInCooldown() {
        return isOnCooldown() ? this.timesCompletedInCooldown : timesCompleted > 0 ? 1 : 0;
    }

    public void setCooldownUntil(final long newCompleted) {
        this.cooldownUntil = newCompleted;
        this.timesCompletedInCooldown = 0;
    }

    public void setTimesCompleted(final int newCompleted) {
        this.timesCompleted = newCompleted;
        this.timesCompletedInCooldown = newCompleted;
    }

    public void addTimesCompleted() {
        ++this.timesCompleted;
        ++this.timesCompletedInCooldown;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
