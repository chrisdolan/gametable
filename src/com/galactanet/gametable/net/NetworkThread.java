/*
 * NetworkThread.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;

import com.galactanet.gametable.Log;



/**
 * Multiplexed network thread for all the network stuff.
 * 
 * @author iffy
 */
public class NetworkThread extends Thread
{
    /**
     * Private command class to set the interest ops between selections.
     * 
     * @author iffy
     */
    private class RegisterConnection implements Runnable
    {
        private Connection connection;
        private int        interestOps;

        public RegisterConnection(Connection c, int ops)
        {
            connection = c;
            interestOps = ops;
        }

        public void run()
        {
            try
            {
                connection.register(NetworkThread.this, interestOps);
            }
            catch (Throwable t)
            {
                Log.log(Log.NET, t);
            }
        }
    }

    /**
     * Private command class to set the interest ops between selections.
     * 
     * @author iffy
     */
    private class MarkForWriting implements Runnable
    {
        private Connection connection;

        public MarkForWriting(Connection c)
        {
            connection = c;
        }

        public void run()
        {
            try
            {
                connection.getKey().interestOps(connection.getKey().interestOps() | SelectionKey.OP_WRITE);
            }
            catch (Throwable t)
            {
                Log.log(Log.NET, t);
            }
        }
    }

    private Selector            selector;
    private int                 serverPort;
    private ServerSocketChannel serverSocketChannel;

    private Set                 connections     = new HashSet();
    private Set                 lostConnections = new HashSet();
    private boolean             startServer     = false;
    private List                pendingCommands = new LinkedList();

    /**
     * Client Constructor.
     */
    public NetworkThread()
    {
        super(NetworkThread.class.getName());
        setPriority(NORM_PRIORITY - 1);
        serverPort = -1;
        startServer = false;
    }

    /**
     * Server Constructor.
     */
    public NetworkThread(int port)
    {
        super(NetworkThread.class.getName());
        setPriority(NORM_PRIORITY - 1);
        serverPort = port;
        startServer = true;
    }

    public void markForWriting(Connection c)
    {
        synchronized (pendingCommands)
        {
            pendingCommands.add(new MarkForWriting(c));
        }

        selector.wakeup();
    }

    /**
     * @return Returns this thread's selector.
     */
    public Selector getSelector()
    {
        return selector;
    }

    /*
     * @see java.lang.Thread#run()
     */
    public void run()
    {
        try
        {
            selector = Selector.open();

            while (true)
            {
                sleep(1);

                if (startServer)
                {
                    serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.socket().bind(new InetSocketAddress(serverPort));
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this);
                    startServer = false;
                }

                synchronized (pendingCommands)
                {
                    while (pendingCommands.size() > 0)
                    {
                        Runnable r = (Runnable)pendingCommands.remove(0);

                        try
                        {
                            r.run();
                        }
                        catch (Exception e)
                        {
                            Log.log(Log.NET, e);
                        }
                    }
                }
                
                if (selector.selectNow() == 0)
                {
                    continue;
                }

                sleep(1);

                Set keys = selector.selectedKeys();
                Iterator keyIterator = keys.iterator();
                while (keyIterator.hasNext())
                {
                    try
                    {
                        SelectionKey key = (SelectionKey)keyIterator.next();

                        if (key.isAcceptable())
                        {
                            ServerSocketChannel keyChannel = (ServerSocketChannel)key.channel();
                            SocketChannel newChannel = keyChannel.accept();
                            Connection connection = new Connection(newChannel);
                            add(connection, SelectionKey.OP_READ);
                        }

                        if (key.isConnectable())
                        {
                            SocketChannel keyChannel = (SocketChannel)key.channel();
                            try
                            {
                                while (!keyChannel.finishConnect())
                                {
                                    // keep going
                                }
                                key.interestOps(SelectionKey.OP_READ);
                                Connection connection = (Connection)key.attachment();
                                connection.markConnected();
                                connection.markLoggedIn();
                            }
                            catch (IOException ioe)
                            {
                                Log.log(Log.NET, ioe);
                                keyChannel.close();
                            }
                        }

                        if (key.isReadable())
                        {
                            Connection connection = (Connection)key.attachment();
                            try
                            {
                                connection.readFromNet();
                            }
                            catch (IOException ioe)
                            {
                                Log.log(Log.NET, ioe);
                                connection.close();
                            }
                        }

                        if (key.isWritable())
                        {
                            Connection connection = (Connection)key.attachment();
                            try
                            {
                                connection.writeToNet();
                            }
                            catch (IOException ioe)
                            {
                                Log.log(Log.NET, ioe);
                                connection.close();
                            }
                        }
                    }
                    catch (CancelledKeyException cke)
                    {
                        Log.log(Log.NET, cke);
                    }
                    finally
                    {
                        keyIterator.remove();
                    }
                }
            }
        }
        catch (InterruptedException ie)
        {
            Log.log(Log.SYS, ie);
        }
        catch (Throwable t)
        {
            Log.log(Log.SYS, t);
        }
        finally
        {
            closeAllConnections();
        }
    }

    public Set getConnections()
    {
        Set retVal = new HashSet();
        synchronized (connections)
        {
            retVal.addAll(connections);
            connections.clear();
        }

        return retVal;
    }

    void add(Connection connection, int ops)
    {
        synchronized (connections)
        {
            connections.add(connection);
            synchronized (pendingCommands)
            {
                pendingCommands.add(new RegisterConnection(connection, ops));
            }
        }

        if (selector != null)
        {
            selector.wakeup();
        }
    }

    public void add(Connection connection)
    {
        boolean connected = false;
        try
        {
            connected = connection.getChannel().finishConnect();
        }
        catch (IOException ioe)
        {
            Log.log(Log.NET, ioe);
        }
        
        if (connected)
        {
            connection.markConnected();
            connection.markLoggedIn();
            add(connection, SelectionKey.OP_READ);
        }
        else
        {
            add(connection, SelectionKey.OP_CONNECT);
        }
    }

    public void remove(Connection connection)
    {
        connection.close();
        synchronized (connections)
        {
            connections.remove(connection);
            synchronized (lostConnections)
            {
                lostConnections.add(connection);
            }
        }
    }

    public void send(byte[] packet)
    {
        synchronized (connections)
        {
            Iterator iter = connections.iterator();
            while (iter.hasNext())
            {
                Connection connection = (Connection)iter.next();
                if (connection.isLoggedIn())
                {
                    connection.sendPacket(packet);
                }
            }
        }
    }

    public void send(byte[] packet, Connection connection)
    {
        connection.sendPacket(packet);
    }

    public List getPackets()
    {
        List retVal = new ArrayList();
        synchronized (connections)
        {
            Iterator iter = connections.iterator();
            while (iter.hasNext())
            {
                Connection connection = (Connection)iter.next();
                while (connection.hasPackets())
                {
                    byte[] data = connection.receivePacket();
                    if (data == null)
                    {
                        break;
                    }
                    retVal.add(new Packet(data, connection));
                }
            }
        }
        return retVal;
    }

    public Set getLostConnections()
    {
        cullLostConnections();

        Set retVal = null;
        synchronized (lostConnections)
        {
            retVal = new HashSet(lostConnections);
            lostConnections.clear();
            return retVal;
        }
    }

    private void cullLostConnections()
    {
        Set lost = new HashSet();
        synchronized (connections)
        {
            Iterator iterator = connections.iterator();
            while (iterator.hasNext())
            {
                Connection connection = (Connection)iterator.next();
                if (connection.isDead())
                {
                    lost.add(connection);
                }
            }
        }

        Iterator iterator = lost.iterator();
        while (iterator.hasNext())
        {
            remove((Connection)iterator.next());
        }
    }

    public void closeAllConnections()
    {
        try
        {
            synchronized (connections)
            {
                Iterator iter = connections.iterator();
                while (iter.hasNext())
                {
                    Connection connection = (Connection)iter.next();
                    connection.close();
                }
                connections.clear();
            }

            if (selector != null)
            {
                selector.close();
                selector = null;
            }
            
            if (serverSocketChannel != null)
            {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        }
        catch (Throwable t)
        {
            Log.log(Log.NET, t);
        }
    }
}
