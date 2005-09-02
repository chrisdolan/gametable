

package com.galactanet.gametable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;


public class HostListenThread extends Thread
{
    public HostListenThread()
    {
    }

    public void run()
    {
        try
        {
            m_serverSocket = new ServerSocket(GametableFrame.getGametableFrame().m_defaultPort);
        }
        catch (IOException e)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    GametableFrame.getGametableFrame().hostThreadFailed();
                }
            });
            return;
        }

        while (!bEnd)
        {
            Socket client = null;
            try
            {
                client = m_serverSocket.accept();
            }
            catch (Exception e)
            {
                return;
            }

            Connection newConnection = new Connection();
            boolean res = newConnection.init(client);
            if (!res)
            {
                System.out.println("Connection init failure");
            }

            PacketHolder.push(null, newConnection, PacketHolder.OPERATION_JOIN);
        }

    }

    public void terminate()
    {
        bEnd = true;
        try
        {
            m_serverSocket.close();
        }
        catch (IOException ex)
        {
        }
    }



    ServerSocket m_serverSocket;

    boolean      bEnd;
}
