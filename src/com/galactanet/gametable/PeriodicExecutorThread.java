/*
 * PeriodicExecutorThread.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import javax.swing.SwingUtilities;



/**
 * Simple thread to periodically place a task on the AWT event queue. This wouldn't be necessary if the task could call
 * invokeLater() directly, but that starves out the rest of the events for some reason.
 * 
 * @author iffy
 */
public class PeriodicExecutorThread extends Thread
{
    private static final int INTERVAL = 25;

    private final Runnable   task;

    /**
     * Constructor;
     */
    public PeriodicExecutorThread(final Runnable r)
    {
        super("PeriodicExecutorThread");
        setPriority(NORM_PRIORITY + 1);
        task = r;
    }

    public void run()
    {
        try
        {
            while (true)
            {
                sleep(INTERVAL);

                try
                {
                    SwingUtilities.invokeLater(task);
                }
                catch (final Throwable t)
                {
                    Log.log(Log.SYS, t);
                }
            }
        }
        catch (final InterruptedException ie)
        {
            Log.log(Log.SYS, ie);
        }
    }
}
