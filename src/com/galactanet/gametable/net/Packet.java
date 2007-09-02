/*
 * Packet.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.net;

/**
 * Encapsulation for a packet as it moves through the system.
 * 
 * @author iffy
 */
public class Packet
{
    private final byte[]     data;
    private final Connection source;

    /**
     * Constructor
     */
    public Packet(final byte[] dat, final Connection src)
    {
        data = dat;
        source = src;
    }

    /**
     * @return Returns the data.
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * @return Returns the source.
     */
    public Connection getSource()
    {
        return source;
    }

}
