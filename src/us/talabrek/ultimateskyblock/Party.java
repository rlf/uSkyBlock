package us.talabrek.ultimateskyblock;

public class Party
{
    private String name;
    private int firstCompleted;
    private int timesCompleted;
    
    public Party(final String name, final int firstCompleted, final int timesCompleted) {
        super();
        this.name = name;
        this.firstCompleted = firstCompleted;
        this.timesCompleted = timesCompleted;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getFirstCompleted() {
        return this.firstCompleted;
    }
    
    public int getTimesCompleted() {
        return this.timesCompleted;
    }
    
    public void setFirstCompleted(final int newCompleted) {
        this.firstCompleted = newCompleted;
    }
    
    public void setTimesCompleted(final int newCompleted) {
        this.timesCompleted = newCompleted;
    }
    
    public void addTimesCompleted() {
        ++this.timesCompleted;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
}
