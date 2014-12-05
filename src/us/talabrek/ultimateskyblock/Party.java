package us.talabrek.ultimateskyblock;

public class Party
{
  private String name;
  private int firstCompleted;
  private int timesCompleted;
  
  public Party(String name, int firstCompleted, int timesCompleted)
  {
    this.name = name;
    this.firstCompleted = firstCompleted;
    this.timesCompleted = timesCompleted;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public int getFirstCompleted()
  {
    return this.firstCompleted;
  }
  
  public int getTimesCompleted()
  {
    return this.timesCompleted;
  }
  
  public void setFirstCompleted(int newCompleted)
  {
    this.firstCompleted = newCompleted;
  }
  
  public void setTimesCompleted(int newCompleted)
  {
    this.timesCompleted = newCompleted;
  }
  
  public void addTimesCompleted()
  {
    this.timesCompleted += 1;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
}
