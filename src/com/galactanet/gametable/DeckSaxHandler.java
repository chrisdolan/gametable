/*
 * DiceMacroSaxHandler.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

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
public class DeckSaxHandler extends DefaultHandler
{
    // --- Constants -------------------------------------------------------------------------------------------------

    public static final String ELEMENT_CARDS  = "deck";
    public static final String ELEMENT_CARD   = "card";

    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_FILE = "file";
    public static final String ATTRIBUTE_DESC = "desc";
    public static final String ATTRIBUTE_QTY  = "qty";

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
     * The DeckData object we're filling out
     */
    private DeckData           m_deckData;

    // --- Constructor -----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public DeckSaxHandler()
    {
    }

    /**
     * Initter.
     */
    public void init(DeckData dd)
    {
        m_deckData = dd;
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

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
        // deckData requires no initalization
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
            // it is not allowed for the name to not exist
            fatalError(new SAXParseException("name field not specified in card field", locator));
        }

        if (name.equals(ELEMENT_CARDS))
        {
        }
        else if (name.equals(ELEMENT_CARD))
        {
            // make a card. This will receive all the data
            DeckData.Card card = m_deckData.createBlankCard();

            String cardName = attributes.getValue(ATTRIBUTE_NAME);
            if (cardName == null)
            {
                card.m_cardName = "";
            }
            else
            {
                card.m_cardName = cardName;
            }

            String cardFile = attributes.getValue(ATTRIBUTE_FILE);
            if (cardFile == null)
            {
                card.m_cardFile = "";
            }
            else
            {
                card.m_cardFile = cardFile;
            }

            String cardDesc = attributes.getValue(ATTRIBUTE_DESC);
            if (cardDesc == null)
            {
                card.m_cardDesc = "";
            }
            else
            {
                card.m_cardDesc = cardDesc;
            }

            String qtyStr = attributes.getValue(ATTRIBUTE_QTY);

            int qty = 0;
            try
            {
                qty = Integer.parseInt(qtyStr);
            }
            catch (Exception e)
            {
                // there was a problem parsing the int. this is not allowed
                error(new SAXParseException("invalid or missing " + ATTRIBUTE_QTY + " field", locator));
            }

            card.m_quantityInDeck = qty;

            // only add this card type if there is at least 1 of them in the deck
            if (qty < 0)
            {
                // a quantity of 0 or less is not allowed
                error(new SAXParseException("invalid or missing " + ATTRIBUTE_QTY + " field", locator));
            }
            m_deckData.addCardType(card);
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
