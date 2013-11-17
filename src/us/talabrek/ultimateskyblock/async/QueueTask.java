package us.talabrek.ultimateskyblock.async;

import org.bukkit.Bukkit;

import us.talabrek.ultimateskyblock.uSkyBlock;

public abstract class QueueTask implements Runnable
{
	private Runnable mNext;
	private long mDelay;
	private long mInterval;
	
	public void then(Runnable next)
	{
		then(next, 0L);
	}
	
	public void then(Runnable next, long delay)
	{
		thenRepeat(next, delay, 0L);
	}
	
	public void thenRepeat(Runnable next, long delay, long interval)
	{
		mNext = next;
		mDelay = delay;
		mInterval = interval;
	}
	
	protected void doNext()
	{
		if(mNext == null)
			return;
		
		if(mInterval == 0)
			Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), mNext, mDelay);
		else
			Bukkit.getScheduler().runTaskTimer(uSkyBlock.getInstance(), mNext, mDelay, mInterval);
	}
}
