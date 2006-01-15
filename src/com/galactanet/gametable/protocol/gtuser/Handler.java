/*
 * Handler.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.protocol.gtuser;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Placeholder to enable the gtuser protocol.
 *
 * @author iffy
 */
public class Handler extends URLStreamHandler
{
    /**
     * Constructor 
     */
    public Handler()
    {
    }

    /*
     * @see java.net.URLStreamHandler#openConnection(java.net.URL)
     */
    protected URLConnection openConnection(URL u) throws IOException
    {
        return null;
    }
}
