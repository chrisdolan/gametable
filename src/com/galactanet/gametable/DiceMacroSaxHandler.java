/*
 * DiceMacroSaxHandler.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * Sax handler for parsing dice macro files.
 * 
 * @author iffy
 */
public class DiceMacroSaxHandler extends DefaultHandler
{
    // --- Constants -------------------------------------------------------------------------------------------------

    public static final String ATTRIBUTE_DEFINITION = "definition";
    public static final String ATTRIBUTE_NAME       = "name";

    public static final String ELEMENT_DICE_MACRO   = "diceMacro";
    public static final String ELEMENT_DICE_MACROS  = "diceMacros";

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * Character accumulator.
     */
    private StringBuffer       accum;

    /**
     * The SAX locator for pinning errors.
     */
    private Locator            locator;

    /**
     * The collected list of macros.
     */
    private final List         macros               = new ArrayList();

    // --- Constructor -----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public DiceMacroSaxHandler()
    {
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(final char[] ch, final int start, final int length) throws SAXException
    {
        if (accum != null)
        {
            accum.append(ch, start, length);
        }
    }

    // --- ContentHandler implementation ---

    /*
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
    }

    /*
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(final SAXParseException exception) throws SAXException
    {
        Log.log(Log.SYS, "ERROR: Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber());
        Log.log(Log.SYS, exception);
    }

    /*
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(final SAXParseException exception) throws SAXException
    {
        throw exception;
    }

    /**
     * @return The macros from the latest parse.
     */
    public List getMacros()
    {
        return Collections.unmodifiableList(macros);
    }

    /*
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(final Locator l)
    {
        locator = l;
    }

    // --- ErrorHandler Implementation ---

    /*
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    public void startDocument() throws SAXException
    {
        macros.clear();
    }

    /*
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
        throws SAXException
    {
        String name = qName;
        if ((name == null) || (name.length() == 0))
        {
            name = localName;
        }

        if (name.equals(ELEMENT_DICE_MACROS))
        {
        }
        else if (name.equals(ELEMENT_DICE_MACRO))
        {
            final String macroName = attributes.getValue(ATTRIBUTE_NAME);

            if ((macroName == null) || (macroName.length() == 0))
            {
                error(new SAXParseException("Macro tag without " + ATTRIBUTE_NAME + " attribute, skipping...", locator));
                return;
            }

            final String macroDefinition = attributes.getValue(ATTRIBUTE_DEFINITION);
            if ((macroDefinition == null) || (macroDefinition.length() == 0))
            {
                error(new SAXParseException("Macro tag without " + ATTRIBUTE_DEFINITION + " attribute, skipping...",
                    locator));
                return;
            }

            final DiceMacro macro = new DiceMacro(macroDefinition, macroName, null);
            if (!macro.isInitialized())
            {
                error(new SAXParseException("Unable to parse macro " + macroName + ": " + macroDefinition, locator));
                return;
            }

            macros.add(macro);
        }
    }

    /*
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(final SAXParseException exception) throws SAXException
    {
        Log.log(Log.SYS, "WARNING: Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber());
        Log.log(Log.SYS, exception);
    }
}
