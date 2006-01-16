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

    public static final String ELEMENT_DICE_MACROS  = "diceMacros";
    public static final String ELEMENT_DICE_MACRO   = "diceMacro";

    public static final String ATTRIBUTE_NAME       = "name";
    public static final String ATTRIBUTE_DEFINITION = "definition";

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * The collected list of macros.
     */
    private List               macros               = new ArrayList();

    /**
     * Character accumulator.
     */
    private StringBuffer       accum;

    /**
     * The SAX locator for pinning errors.
     */
    private Locator            locator;

    // --- Constructor -----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public DiceMacroSaxHandler()
    {
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return The macros from the latest parse.
     */
    public List getMacros()
    {
        return Collections.unmodifiableList(macros);
    }

    // --- ContentHandler implementation ---

    /*
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator l)
    {
        locator = l;
    }

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
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        String name = qName;
        if (name == null || name.length() == 0)
        {
            name = localName;
        }

        if (name.equals(ELEMENT_DICE_MACROS))
        {
        }
        else if (name.equals(ELEMENT_DICE_MACRO))
        {
            String macroName = attributes.getValue(ATTRIBUTE_NAME);

            if (macroName == null || macroName.length() == 0)
            {
                error(new SAXParseException("Macro tag without " + ATTRIBUTE_NAME + " attribute, skipping...", locator));
                return;
            }

            String macroDefinition = attributes.getValue(ATTRIBUTE_DEFINITION);
            if (macroDefinition == null || macroDefinition.length() == 0)
            {
                error(new SAXParseException("Macro tag without " + ATTRIBUTE_DEFINITION + " attribute, skipping...",
                    locator));
                return;
            }

            DiceMacro macro = new DiceMacro(macroDefinition, macroName);
            if (!macro.isInitialized())
            {
                error(new SAXParseException("Unable to parse macro " + macroName + ": " + macroDefinition, locator));
                return;
            }

            macros.add(macro);
        }
    }

    /*
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
    }

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (accum != null)
        {
            accum.append(ch, start, length);
        }
    }

    // --- ErrorHandler Implementation ---

    /*
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        Log.log(Log.SYS, "WARNING: Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber());
        Log.log(Log.SYS, exception);
    }

    /*
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException
    {
        Log.log(Log.SYS, "ERROR: Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber());
        Log.log(Log.SYS, exception);
    }

    /*
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
        throw exception;
    }
}
