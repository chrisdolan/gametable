/*
 * XmlSerializer.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 * A class for serializing xml to a string buffer.
 * 
 * @author iffy
 */
public class XmlSerializer
{
    // --- Members ---------------------------------------------------------------------------------------------------

    boolean bTagOpen = false;
    Writer  out;
    List    tagStack = new LinkedList();

    // --- Constructors ----------------------------------------------------------------------------------------------

    public XmlSerializer()
    {
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    public void addAttribute(final String name, final String value) throws IOException
    {
        out.write(' ');
        out.write(name);
        out.write("=\"");
        UtilityFunctions.xmlEncode(out, new StringReader(value));
        out.write('"');
    }

    public void addAttributes(final Map attributes) throws IOException
    {
        for (final Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry)iterator.next();
            addAttribute((String)entry.getKey(), (String)entry.getValue());
        }
    }

    public void addText(final String text) throws IOException
    {
        checkTagClose();
        UtilityFunctions.xmlEncode(out, new StringReader(text));
    }

    private void checkTagClose() throws IOException
    {
        if (bTagOpen)
        {
            out.write('>');
            bTagOpen = false;
        }
    }

    public void endDocument() throws IOException
    {
        out.flush();
    }

    public void endElement() throws IOException
    {
        if (bTagOpen)
        {
            out.write("/>");
            popTag();
        }
        else
        {
            out.write("</");
            out.write(popTag());
            out.write('>');
        }
        bTagOpen = false;
    }

    private String popTag()
    {
        return (String)tagStack.remove(0);
    }

    /* --- Private Methods ------------------------------------------------- */

    private void pushTag(final String name)
    {
        tagStack.add(0, name);
    }

    public void startDocument(final Writer w) throws IOException
    {
        out = w;
        out.write("<?xml version=\"1.0\"?>");
        tagStack.clear();
    }

    public void startElement(final String name) throws IOException
    {
        checkTagClose();
        out.write('<');
        out.write(name);
        pushTag(name);
        bTagOpen = true;
    }

}
