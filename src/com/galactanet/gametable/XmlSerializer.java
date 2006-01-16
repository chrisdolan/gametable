/*
 * XmlSerializer.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

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

    Writer  out;
    boolean bTagOpen = false;
    List    tagStack = new LinkedList();

    // --- Constructors ----------------------------------------------------------------------------------------------

    public XmlSerializer()
    {
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    public void startDocument(Writer w) throws IOException
    {
        out = w;
        out.write("<?xml version=\"1.0\"?>");
        tagStack.clear();
    }

    public void startElement(String name) throws IOException
    {
        checkTagClose();
        out.write('<');
        out.write(name);
        pushTag(name);
        bTagOpen = true;
    }

    public void addAttribute(String name, String value) throws IOException
    {
        out.write(' ');
        out.write(name);
        out.write("=\"");
        UtilityFunctions.xmlEncode(out, new StringReader(value));
        out.write('"');
    }

    public void addAttributes(Map attributes) throws IOException
    {
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry)iterator.next();
            addAttribute((String)entry.getKey(), (String)entry.getValue());
        }
    }

    public void addText(String text) throws IOException
    {
        checkTagClose();
        UtilityFunctions.xmlEncode(out, new StringReader(text));
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

    public void endDocument() throws IOException
    {
        out.flush();
    }

    /* --- Private Methods ------------------------------------------------- */

    private void pushTag(String name)
    {
        tagStack.add(0, name);
    }

    private String popTag()
    {
        return (String)tagStack.remove(0);
    }

    private void checkTagClose() throws IOException
    {
        if (bTagOpen)
        {
            out.write('>');
            bTagOpen = false;
        }
    }

}
