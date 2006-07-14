/*
 * This is a deck, managing the cards and distributing them as needed.
 * For the functioinality that manages loading a deck from disk, see DeckData
 */

package com.galactanet.gametable;

import java.util.*;

class Deck
{
	// a static Random number generator
	public final static Random g_random = new Random(); 
	
	public Deck()
	{
	}
	
	public void init(DeckData deckData, int id, String name)
	{
		m_id = id;
		m_name = name;
		m_deckData = deckData;
		
		// generate a number for each of the cards in the deck
		for ( int i=0 ; i<m_deckData.getNumCards() ; i++ )
		{
			Integer toAdd = new Integer(i);
			m_deck.add(toAdd);
		}
		shuffle();
	}
	
	// returns the number of cards remaining in the deck
	public int cardsRemaining()
	{
		return m_deck.size();
	}
	
	// returns the number of cards that are neither in the 
	// deck nor in the discards
	public int cardsOutstanding()
	{
		return m_deckData.getNumCards() - m_deck.size() - m_discards.size();
	}
	
	public DeckData.Card drawCard()
	{
		// get the next card in the deck
		Integer cardNum = (Integer)m_deck.get(0);
		m_deck.remove(0);
		System.out.println(""+cardNum.intValue());
		DeckData.Card card = m_deckData.getCard(cardNum.intValue());
		
		// note the id. We'll need that later when it gets discarded
		card.m_cardId = cardNum.intValue();
		card.m_deckId = m_id;
		return card;
	}
	
	public void discard(DeckData.Card card)
	{
		if ( card.m_deckId != m_id )
		{
			// this is not our card. ignore it
			return;
		}
		
		// note the id of the discarded card
		Integer cardNum = new Integer(card.m_cardId);
		
		// add it to the discards
		m_discards.add(cardNum);
	}
	
	// shuffles the cards and discards together
	public void shuffle()
	{
		// put all the discards into the deck
		for ( int i=0 ; i<m_discards.size() ; i++ )
		{
			Integer cardNum = (Integer)m_discards.get(i);
			m_deck.add(cardNum);
		}
		m_discards.clear();
		
		// if the deck has 0 or 1 cards, there's no point in going on
		if ( m_deck.size() < 2 ) return;
		
		// shuffle the deck by randomly pulling cards within
		// it and putting them at the back
		// we do a total of 2*(deck size) rearrangements. This way, each card
		// will have been moved an average of 2 times. That's a fair
		// bit of shuffling
		for ( int i=0 ; i<m_deckData.getNumCards()*2 ; i++ )
		{
			// yes we could end up swappign a card with itself. That's ok
			int idx = rand()%m_deck.size();
			Integer cardNum = (Integer)m_deck.get(idx);
			
			// pull it out of the deck
			m_deck.remove(idx);
			
			// move it to the end
			m_deck.add(cardNum);
		}
	}
	
	// helper function
	private int rand()
	{
		int ret = g_random.nextInt();
		if ( ret < 0 ) ret = -ret;
		return ret;
	}

	/**************************** DATA **************************/
	private DeckData m_deckData;
	
	// our id
	int m_id;
	String m_name;
	
	// these Lists are filled with Integers. Those Integers
	// refer to indices that can be sent in to DeckData.getCard.
	// Cards in the deck:
	List m_deck = new ArrayList();
	
	// Cards in the discards:
	List m_discards = new ArrayList();
}