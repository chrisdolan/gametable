

package com.galactanet.gametable;

import java.util.Vector;

import javax.swing.SwingUtilities;


public class PacketHolder
{
    public final static int OPERATION_PACKET = 0;
    public final static int OPERATION_JOIN   = 1;
    public final static int OPERATION_DROP   = 2;



    public static void push(byte[] packet, Connection conn, int operation)
    {
        synchronized (SYNCH)
        {
            m_packets.add(packet);
            m_connections.add(conn);
            m_operations.add(new Integer(operation));
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

            Integer op = (Integer)m_operations.elementAt(0);
            byte[] packet = (byte[])m_packets.elementAt(0);
            Connection conn = (Connection)m_connections.elementAt(0);

            switch (op.intValue())
            {
                case OPERATION_JOIN:
                {
                    GametableFrame.getGametableFrame().newConnection(conn);
                }
                    break;

                case OPERATION_PACKET:
                {
                    GametableFrame.getGametableFrame().packetReceived(conn, packet);
                }
                    break;

                case OPERATION_DROP:
                {
                    GametableFrame.getGametableFrame().connectionDropped(conn);
                }
                    break;
            }

            m_packets.remove(0);
            m_connections.remove(0);
            m_operations.remove(0);
        }
    }

    public static void clear()
    {
        synchronized (SYNCH)
        {
            m_packets = new Vector();
            m_connections = new Vector();
            m_operations = new Vector();
        }
    }

    public static boolean hasPackets()
    {
        if (m_packets.size() > 0)
        {
            return true;
        }
        return false;
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
                        pop();
                    }
                });
            }
        }
    }



    public static final Object SYNCH         = new Object();

    private static Vector      m_packets     = new Vector();
    private static Vector      m_connections = new Vector();
    private static Vector      m_operations  = new Vector();
}
