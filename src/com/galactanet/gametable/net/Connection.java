/*
 * Connection.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import com.galactanet.gametable.Log;
import com.galactanet.gametable.PacketManager;



/**
 * Represents a two-way network connection. The paradigm for this network stuff is to make operations as efficient as
 * possible for the application, even if that means sacrificing efficiency on the network. For example, the send and
 * receive buffers will always be kept "flipped" such that nothing need be done for the application to read or write to
 * the appropriate buffers.
 * 
 * @author iffy
 */
public class Connection
{
    public interface State
    {
        public int CONNECTED          = 1;
        public int FLUSHING           = 3;
        public int LOGGED_IN          = 2;
        public int PENDING_CONNECTION = 0;
    }

    private static final int    DEFAULT_BUFFER_SIZE = 1024;

    private final SocketChannel channel;
    private SelectionKey        key;
    private final List          queue               = new LinkedList();
    private ByteBuffer          receiveBuffer;
    private ByteBuffer          sendBuffer;
    private int                 state               = State.PENDING_CONNECTION;
    private NetworkThread       thread;

    /**
     * Incoming Connection Constructor
     */
    public Connection(final SocketChannel chan) throws IOException
    {
        channel = chan;
        channel.configureBlocking(false);
        sendBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
        receiveBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Outgoing Connection Constructor
     */
    public Connection(final String addr, final int port) throws IOException
    {
        channel = SocketChannel.open(new InetSocketAddress(addr, port));
        if (channel.finishConnect())
        {
            markConnected();
        }
        channel.configureBlocking(false);
        sendBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
        receiveBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Closes this connection.
     */
    public void close()
    {
        boolean term = false;
        synchronized (sendBuffer)
        {
            sendBuffer.flip();
            if (sendBuffer.hasRemaining())
            {
                state = State.FLUSHING;
                sendBuffer.compact();
            }
            else
            {
                term = true;
            }
        }

        if (term)
        {
            terminate();
        }
    }

    /**
     * @return Returns the channel.
     */
    public SocketChannel getChannel()
    {
        return channel;
    }

    /**
     * @return Returns the key.
     */
    public SelectionKey getKey()
    {
        return key;
    }

    /**
     * @return
     */
    public boolean hasPackets()
    {
        synchronized (queue)
        {
            return queue.size() > 0;
        }
    }

    /**
     * @return True if this connection is connected.
     */
    public boolean isConnected()
    {
        // System.out.println(this + " connected: " + channel.socket().isConnected());
        return channel.socket().isConnected() && (thread != null);
    }

    public boolean isDead()
    {
        // System.out.println(this + " connected2: " + channel.socket().isConnected());
        return (!channel.socket().isConnected() && (state != State.PENDING_CONNECTION));
    }

    /**
     * @return True if this connection is logged in.
     */
    public boolean isLoggedIn()
    {
        return isConnected() && (state == State.LOGGED_IN);
    }

    void markConnected()
    {
        if (state == State.PENDING_CONNECTION)
        {
            state = State.CONNECTED;
        }
    }

    public void markLoggedIn()
    {
        if (state == State.CONNECTED)
        {
            state = State.LOGGED_IN;
        }
    }

    /**
     * Reads as much as it can from the net without blocking.
     * 
     * @throws IOException
     */
    void readFromNet() throws IOException
    {
        synchronized (receiveBuffer)
        {
            while (true)
            {
                if (receiveBuffer.limit() == receiveBuffer.position())
                {
                    final ByteBuffer newBuffer = ByteBuffer.allocateDirect(receiveBuffer.capacity() * 2);
                    receiveBuffer.flip();
                    newBuffer.put(receiveBuffer);
                    receiveBuffer = newBuffer;
                }

                final int count = channel.read(receiveBuffer);
                if (count < 1)
                {
                    break;
                }
            }
        }

        while (true)
        {
            final byte[] packet = readPacket();
            if (packet == null)
            {
                break;
            }
            Log.log(Log.NET, "Read: " + PacketManager.getPacketName(packet) + ", length = " + packet.length);

            queue.add(packet);
        }
    }

    /**
     * @return
     */
    private byte[] readPacket()
    {
        synchronized (receiveBuffer)
        {
            receiveBuffer.flip();
            if (receiveBuffer.remaining() < 4)
            {
                receiveBuffer.compact();
                return null;
            }

            final int size = receiveBuffer.getInt(0);
            if (receiveBuffer.remaining() < size + 4)
            {
                receiveBuffer.compact();
                return null;
            }

            final byte[] retVal = new byte[size];
            receiveBuffer.getInt();
            receiveBuffer.get(retVal);
            receiveBuffer.compact();

            return retVal;
        }
    }

    /**
     * @return
     */
    public byte[] receivePacket()
    {
        synchronized (queue)
        {
            if (!hasPackets())
            {
                return null;
            }

            return (byte[])queue.remove(0);
        }
    }

    void register(final NetworkThread t, final int ops) throws ClosedChannelException
    {
        thread = t;
        key = channel.register(thread.getSelector(), ops, this);
        if (isConnected())
        {
            markConnected();
            key.interestOps(ops & ~SelectionKey.OP_CONNECT);
        }
    }

    /**
     * @param packet Packet data to send.
     */
    public void sendPacket(final byte[] packet)
    {
        final int size = packet.length + 4;
        Log.log(Log.NET, "Sending : " + PacketManager.getPacketName(packet) + ", length = " + packet.length);
        synchronized (sendBuffer)
        {
            if (sendBuffer.limit() - sendBuffer.position() <= size)
            {
                int newCapacity = sendBuffer.capacity();
                while (newCapacity - sendBuffer.position() <= size)
                {
                    newCapacity *= 2;
                }
                final ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);
                sendBuffer.flip();
                newBuffer.put(sendBuffer);
                sendBuffer = newBuffer;
            }

            try
            {
                sendBuffer.putInt(packet.length);
                sendBuffer.put(packet);
            }
            catch (final BufferOverflowException boe)
            {
                Log.log(Log.NET, boe);
            }

            thread.markForWriting(this);
        }
    }

    private void terminate()
    {
        Log.log(Log.NET, "Connection.terminate();");
        try
        {
            key.cancel();
            channel.socket().close();
            channel.close();
        }
        catch (final IOException e)
        {
            Log.log(Log.NET, e);
        }
    }

    /**
     * Writes as much to the net as it can without blocking.
     * 
     * @throws IOException
     */
    void writeToNet() throws IOException
    {
        synchronized (sendBuffer)
        {
            sendBuffer.flip();
            while (sendBuffer.hasRemaining())
            {
                if (channel.write(sendBuffer) < 1)
                {
                    break;
                }
            }

            if (!sendBuffer.hasRemaining())
            {
                key.interestOps(SelectionKey.OP_READ);
                if (state == State.FLUSHING)
                {
                    terminate();
                }
            }

            sendBuffer.compact();
        }
    }
}
