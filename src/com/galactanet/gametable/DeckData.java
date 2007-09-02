/*
 * This class is used by the Deck class to manage a random
 * deck of cards. 
 */


package com.galactanet.gametable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



class DeckData
{
    /** ************************* INNER CLASS *********************** */
    // this is a type of card. There could be 50 of a given card
    // in a deck, there will only be one Card for it in this class
    public class Card
    {
        String m_cardDesc;      // "This is the Jack of Clubs!"

        String m_cardFile;      // "j_club.png"

        int    m_cardId;        // used by the Deck class

        String m_cardName;      // "Jack of Clubs"

        String m_deckName;      // used by the Deck class

        int    m_quantityInDeck; // the number of this card that there are in a deck.

        Card()
        {
            m_cardName = "";
            m_cardFile = "";
            m_cardDesc = "";
            m_quantityInDeck = 0;
            m_cardId = -1;
            m_deckName = "";
        }

        void copy(final Card in)
        {
            m_cardName = "" + in.m_cardName;
            m_cardFile = "" + in.m_cardFile;
            m_cardDesc = "" + in.m_cardDesc;
            m_quantityInDeck = in.m_quantityInDeck;
            m_cardId = in.m_cardId;
            m_deckName = in.m_deckName;
        }

        public boolean equals(final Card in)
        {
            // we are "equal" to another card if we came from the
            // same deck and have the same id
            if (!in.m_cardName.equals(m_cardName))
            {
                return false;
            }
            if (in.m_cardId != m_cardId)
            {
                return false;
            }
            return true;
        }

        public Card makeCopy()
        {
            final Card ret = new Card();
            ret.copy(this);
            return ret;
        }

        void read(final DataInputStream dis) throws IOException
        {
            m_cardName = dis.readUTF();
            m_cardFile = dis.readUTF();
            m_cardDesc = dis.readUTF();
            m_deckName = dis.readUTF();
            m_quantityInDeck = dis.readInt();
            m_cardId = dis.readInt();
        }

        void write(final DataOutputStream dos) throws IOException
        {
            dos.writeUTF(m_cardName);
            dos.writeUTF(m_cardFile);
            dos.writeUTF(m_cardDesc);
            dos.writeUTF(m_deckName);
            dos.writeInt(m_quantityInDeck);
            dos.writeInt(m_cardId);
        }
    }

    public static Card createBlankCard()
    {
        final DeckData dd = new DeckData();
        return dd.doCreateBlankCard();
    }

    private final List m_cardTypes = new ArrayList();

    /** ************************* DATA *********************** */
    private int        m_numCards;

    public DeckData()
    {
    }

    // stuff for the SAX parser
    public void addCardType(final Card newCardType)
    {
        m_cardTypes.add(newCardType);
    }

    // called when we're done adding CardTypes.
    // calculates deck information (like how many cards there are)
    public void deckComplete()
    {
        // work out how many cards are in this deck
        m_numCards = 0;
        for (int i = 0; i < m_cardTypes.size(); i++)
        {
            final Card cardType = (Card)m_cardTypes.get(i);
            m_numCards += cardType.m_quantityInDeck;
        }
    }

    private Card doCreateBlankCard()
    {
        return new Card();
    }

    // this function expects a number from 0 to getNumCards()-1.
    // Imagine that the deck is organized by card type, in the order
    // that the card types are in in m_cardTypes. This will return the
    // cardNum'th card in that order. A given number will always get
    // the same result. This allows the Deck class to track cards merely
    // by number.
    //
    // Note -- returns a COPY of the cardData.
    public Card getCard(final int cardNum)
    {
        int cardIndex = cardNum;
        for (int i = 0; i < m_cardTypes.size(); i++)
        {
            final Card cardType = (Card)m_cardTypes.get(i);
            cardIndex -= cardType.m_quantityInDeck;

            if (cardIndex < 0)
            {
                // the type we have here is the type that this card is
                final Card ret = new Card();
                ret.copy(cardType);
                return ret;
            }
        }

        // it should be impossible to get here. If we're here they sent
        // in a card that's out of range.
        throw new IllegalArgumentException("DeckData.getCardType():" + cardNum);
    }

    public int getNumCards()
    {
        return m_numCards;
    }

    public boolean init(final File file)
    {
        try
        {
            // make a SAX parser and use our deckdata handler to
            // parse out the deck info
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            final DeckSaxHandler handler = new DeckSaxHandler();
            handler.init(this);
            parser.parse(file, handler);

            deckComplete();
            return true;
        }
        catch (final Exception e)
        {
            // error case
            e.printStackTrace();
            return false;
        }
    }
}
