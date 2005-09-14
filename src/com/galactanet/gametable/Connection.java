/*
 * Connection.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.*;
import java.net.Socket;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class Connection extends Thread
{
    private InputStream    m_inputStream;
    private OutputStream   m_outputStream;
    private Socket         m_socket;
    private GametableFrame m_gametableFrame;

    // quarantined means we only want to receive a player packet. nothing else
    // is allowed, and will be ignored.
    private boolean        m_bQuarantined = true;



    public Connection()
    {
    }

    public void run()
    {
        boolean bContinue = true;

        DataInputStream in = new DataInputStream(inStream());

        while (bContinue)
        {
            try
            {
                // all we do is sit and listen for data packets.
                // we don't even process them. We just get'em and send them on their way

                // top of the order - get the packet size
                int size = in.readInt();

                // now get the packet
                byte[] packetData = new byte[size];
                in.readFully(packetData);

                boolean bPushPacket = false;
                if (isQuarantined())
                {
                    // if quarantined, check to see if this is a player packet
                    DataInputStream confirm = new DataInputStream(new ByteArrayInputStream(packetData));
                    if (confirm.readInt() == PacketManager.PACKET_PLAYER)
                    {
                        bPushPacket = true;
                    }
                }
                else
                {
                    bPushPacket = true;
                }

                if (bPushPacket)
                {
                    PacketHolder.push(packetData, this, PacketHolder.OPERATION_PACKET);
                }
            }
            catch (IOException ex)
            {
                PacketHolder.push(null, this, PacketHolder.OPERATION_DROP);
                Log.log(Log.SYS, ex);
                return;
            }
        }
    }

    public void sendPacket(byte[] packet)
    {
        if (packet == null)
        {
            return;
        }

        DataOutputStream out = new DataOutputStream(outStream());
        try
        {
            out.writeInt(packet.length);
            out.write(packet);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }

        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            Log.log(Log.NET, "Sent " + PacketManager.getPacketName(dis.readInt()));
        }
        catch (IOException e)
        {
            Log.log(Log.SYS, e);
        }
    }

    public boolean init(Socket connectionSocket)
    {
        setSocket(connectionSocket);

        try
        {
            setInputStream(getSocket().getInputStream());
            setOutputStream(getSocket().getOutputStream());
        }
        catch (IOException e)
        {
            Log.log(Log.SYS, e);
            setInputStream(null);
            setOutputStream(null);
            return false;
        }

        return true;
    }

    public OutputStream outStream()
    {
        return getOutputStream();
    }

    public InputStream inStream()
    {
        return getInputStream();
    }

    public void terminate()
    {
        try
        {
            getSocket().close();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    /**
     * @param m_inputStream The m_inputStream to set.
     */
    public void setInputStream(InputStream inputStream)
    {
        this.m_inputStream = inputStream;
    }

    /**
     * @return Returns the m_inputStream.
     */
    public InputStream getInputStream()
    {
        return m_inputStream;
    }

    /**
     * @param outputStream The m_outputStream to set.
     */
    public void setOutputStream(OutputStream outputStream)
    {
        this.m_outputStream = outputStream;
    }

    /**
     * @return Returns the m_outputStream.
     */
    public OutputStream getOutputStream()
    {
        return m_outputStream;
    }

    /**
     * @param socket The m_socket to set.
     */
    public void setSocket(Socket socket)
    {
        this.m_socket = socket;
    }

    /**
     * @return Returns the m_socket.
     */
    public Socket getSocket()
    {
        return m_socket;
    }

    /**
     * @param m_bQuarantined The m_bQuarantined to set.
     */
    public void setQuarantined(boolean bQuarantined)
    {
        this.m_bQuarantined = bQuarantined;
    }

    /**
     * @return Returns the m_bQuarantined.
     */
    public boolean isQuarantined()
    {
        return m_bQuarantined;
    }

    /**
     * @param m_gametableFrame The m_gametableFrame to set.
     */
    public void setGametableFrame(GametableFrame gametableFrame)
    {
        this.m_gametableFrame = gametableFrame;
    }

    /**
     * @return Returns the m_gametableFrame.
     */
    public GametableFrame getGametableFrame()
    {
        return m_gametableFrame;
    }
}
