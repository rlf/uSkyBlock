package us.talabrek.ultimateskyblock;

public class Challenge
{
    private String name;
    private long firstCompleted;
    private int timesCompleted;
    private int timesCompletedSinceTimer;
    
    public Challenge(final String name) {
        super();
        this.name = name;
        this.firstCompleted = 0L;
        this.timesCompleted = 0;
    }
    
    public Challenge(final String name, final long firstCompleted, final int timesCompleted, final int timesCompletedSinceTimer) {
        super();
        this.name = name;
        this.firstCompleted = firstCompleted;
        this.timesCompleted = timesCompleted;
        this.timesCompletedSinceTimer = timesCompletedSinceTimer;
    }
    
    public String getName() {
        return this.name;
    }
    
    public long getFirstCompleted() {
        return this.firstCompleted;
    }
    
    public int getTimesCompleted() {
        return this.timesCompleted;
    }
    
    public int getTimesCompletedSinceTimer() {
        return this.timesCompletedSinceTimer;
    }
    
    public void setFirstCompleted(final long newCompleted) {
        this.firstCompleted = newCompleted;
        this.timesCompletedSinceTimer = 0;
    }
    
    public void setTimesCompleted(final int newCompleted) {
        this.timesCompleted = newCompleted;
        this.timesCompletedSinceTimer = newCompleted;
    }
    
    public void addTimesCompleted() {
        ++this.timesCompleted;
        ++this.timesCompletedSinceTimer;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
}
