package us.talabrek.ultimateskyblock;

public class Challenge
{
  private String name;
  private long firstCompleted;
  private int timesCompleted;
  private int timesCompletedSinceTimer;
  
  public Challenge(String name)
  {
    this.name = name;
    this.firstCompleted = 0L;
    this.timesCompleted = 0;
  }
  
  public Challenge(String name, long firstCompleted, int timesCompleted, int timesCompletedSinceTimer)
  {
    this.name = name;
    this.firstCompleted = firstCompleted;
    this.timesCompleted = timesCompleted;
    this.timesCompletedSinceTimer = timesCompletedSinceTimer;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public long getFirstCompleted()
  {
    return this.firstCompleted;
  }
  
  public int getTimesCompleted()
  {
    return this.timesCompleted;
  }
  
  public int getTimesCompletedSinceTimer()
  {
    return this.timesCompletedSinceTimer;
  }
  
  public void setFirstCompleted(long newCompleted)
  {
    this.firstCompleted = newCompleted;
    this.timesCompletedSinceTimer = 0;
  }
  
  public void setTimesCompleted(int newCompleted)
  {
    this.timesCompleted = newCompleted;
    this.timesCompletedSinceTimer = newCompleted;
  }
  
  public void addTimesCompleted()
  {
    this.timesCompleted += 1;
    this.timesCompletedSinceTimer += 1;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
}
