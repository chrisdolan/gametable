/*
 * This is a deck, managing the cards and distributing them as needed.
 * For the functioinality that manages loading a deck from disk, see DeckData
 */


package com.galactanet.gametable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



class Deck
{
    // a static Random number generator
    public final static Random g_random   = new Random();

    // these Lists are filled with Integers. Those Integers
    // refer to indices that can be sent in to DeckData.getCard.
    // Cards in the deck:
    List                       m_deck     = new ArrayList();

    /** ************************** DATA ************************* */
    private DeckData           m_deckData;

    // Cards in the discards:
    List                       m_discards = new ArrayList();

    // our id
    int                        m_id;

    String                     m_name;

    public Deck()
    {
    }

    // returns the number of cards that are neither in the
    // deck nor in the discards
    public int cardsOutstanding()
    {
        return m_deckData.getNumCards() - m_deck.size() - m_discards.size();
    }

    // returns the number of cards remaining in the deck
    public int cardsRemaining()
    {
        return m_deck.size();
    }

    public void discard(final DeckData.Card card)
    {
        if (!card.m_deckName.equals(m_name))
        {
            // this is not our card. ignore it
            return;
        }

        // note the id of the discarded card
        final Integer cardNum = new Integer(card.m_cardId);

        // sanity check: make sure a given card only gets added if
        // it's not already somewhere else.
        for (int i = 0; i < m_discards.size(); i++)
        {
            final int checkCardId = ((Integer)m_discards.get(i)).intValue();
            if (checkCardId == card.m_cardId)
            {
                // we already have this in the discards
                // don't panic, just mention it
                System.out.println("discarded card already in discards.");
                return;
            }
        }

        for (int i = 0; i < m_deck.size(); i++)
        {
            final int checkCardId = ((Integer)m_deck.get(i)).intValue();
            if (checkCardId == card.m_cardId)
            {
                // we already have this in the discards
                // don't panic, just mention it
                System.out.println("discarded card already in the deck.");
                return;
            }
        }

        // add it to the discards
        m_discards.add(cardNum);
    }

    public DeckData.Card drawCard()
    {
        // get the next card in the deck
        final Integer cardNum = (Integer)m_deck.get(0);
        m_deck.remove(0);
        // System.out.println(""+cardNum.intValue());
        final DeckData.Card card = m_deckData.getCard(cardNum.intValue());

        // note the id. We'll need that later when it gets discarded
        card.m_cardId = cardNum.intValue();
        card.m_deckName = m_name;
        return card;
    }

    public void init(final DeckData deckData, final int id, final String name)
    {
        m_id = id;
        m_name = name;
        m_deckData = deckData;

        // generate a number for each of the cards in the deck
        for (int i = 0; i < m_deckData.getNumCards(); i++)
        {
            final Integer toAdd = new Integer(i);
            m_deck.add(toAdd);
        }
        shuffle();
    }

    public void initPlaceholderDeck(final String name)
    {
        // this inits the deck as a placeholder.
        m_name = name;
        m_id = -1;
        m_deckData = null; // this being null ensures that functional calls will throw exceptions
    }

    // helper function
    private int rand()
    {
        int ret = g_random.nextInt();
        if (ret < 0)
        {
            ret = -ret;
        }
        return ret;
    }

    // shuffles the cards and discards together
    public void shuffle()
    {
        // put all the discards into the deck
        for (int i = 0; i < m_discards.size(); i++)
        {
            final Integer cardNum = (Integer)m_discards.get(i);
            m_deck.add(cardNum);
        }
        m_discards.clear();

        // if the deck has 0 or 1 cards, there's no point in going on
        if (m_deck.size() < 2)
        {
            return;
        }

        // shuffle the deck by randomly pulling cards within
        // it and putting them at the back
        // we do a total of 2*(deck size) rearrangements. This way, each card
        // will have been moved an average of 2 times. That's a fair
        // bit of shuffling
        for (int i = 0; i < m_deckData.getNumCards() * 2; i++)
        {
            // yes we could end up swappign a card with itself. That's ok
            final int idx = rand() % m_deck.size();
            final Integer cardNum = (Integer)m_deck.get(idx);

            // pull it out of the deck
            m_deck.remove(idx);

            // move it to the end
            m_deck.add(cardNum);
        }
    }

    // reshuffle ALL cards in the deck, even thouse not in the discards
    public void shuffleAll()
    {
        // clear out the deck and discards lists
        m_deck.clear();
        m_discards.clear();

        // fill up the deck array again
        for (int i = 0; i < m_deckData.getNumCards(); i++)
        {
            final Integer toAdd = new Integer(i);
            m_deck.add(toAdd);
        }

        // shuffle
        shuffle();
    }
}
