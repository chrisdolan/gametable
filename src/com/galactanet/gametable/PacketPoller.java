/*
 * PacketPoller.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

/**
 * TODO: comment
 *
 * @author sephalon
 */
public class PacketPoller extends Thread
{
    public void run()
    {
        while (true)
        {
            synchronized (PacketHolder.SYNCH)
            {
                if (!PacketHolder.hasPackets())
                {
                    try
                    {
                        PacketHolder.SYNCH.wait();
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
            }

            if (m_bDoPolling)
            {
                // check the packet queue
                PacketHolder.poll();
            }
            else
            {
                // clear out the event queue
                PacketHolder.clear();
            }
        }
    }

    public void activate(boolean active)
    {
        m_bDoPolling = active;
    }



    private boolean m_bDoPolling = true;
}
