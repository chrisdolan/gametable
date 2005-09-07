/*
 * PacketHolder.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class PacketHolder
{
    /**
     * An object to hold packet information in a queue.
     * 
     * @author iffy
     */
    private static class PacketEntry
    {
        public byte[]     m_packet;
        public Connection m_connection;
        public int        m_operation;



        /**
         * Constructor.
         */
        public PacketEntry(byte[] packet, Connection conn, int operation)
        {
            m_packet = packet;
            m_connection = conn;
            m_operation = operation;
        }

    }



    public final static int    OPERATION_PACKET = 0;
    public final static int    OPERATION_JOIN   = 1;
    public final static int    OPERATION_DROP   = 2;

    public static final Object SYNCH            = new Object();

    private static List        m_entries        = new LinkedList();



    public static void push(byte[] packet, Connection conn, int operation)
    {
        synchronized (SYNCH)
        {
            m_entries.add(new PacketEntry(packet, conn, operation));
            SYNCH.notifyAll();
        }
    }

    static void pop()
    {
        synchronized (SYNCH)
        {
            if (!hasPackets())
            {
                return;
            }

            PacketEntry entry = (PacketEntry)m_entries.get(0);
            m_entries.remove(0);

            switch (entry.m_operation)
            {
                case OPERATION_JOIN:
                {
                    GametableFrame.getGametableFrame().newConnection(entry.m_connection);
                }
                    break;

                case OPERATION_PACKET:
                {
                    GametableFrame.getGametableFrame().packetReceived(entry.m_connection, entry.m_packet);
                }
                    break;

                case OPERATION_DROP:
                {
                    GametableFrame.getGametableFrame().connectionDropped(entry.m_connection);
                }
                    break;
            }
        }
    }

    public static void clear()
    {
        synchronized (SYNCH)
        {
            m_entries.clear();
        }
    }

    public static boolean hasPackets()
    {
        return (m_entries.size() > 0);
    }

    public static void poll()
    {
        synchronized (SYNCH)
        {
            if (hasPackets())
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            pop();
                        }
                        catch (Throwable t)
                        {
                            Log.log(Log.SYS, t);
                        }
                    }
                });
            }
        }
    }
}
