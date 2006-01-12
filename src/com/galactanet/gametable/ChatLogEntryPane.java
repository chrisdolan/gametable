/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;


import javax.swing.JTextPane;
import java.awt.event.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ChatLogEntryPane extends JTextPane 
{
	public ChatLogEntryPane()
	{
		super();
        setEditable(true);
        setFocusable(true);
		clear();
	}
	
	public void clear()
	{
		setText("");
	}
	
	public String getUserText()
	{
		return getText();
	}
	
	// use this function to insert an html tag like <b> or <i> or whatever.
	public void addHtmlTag(String tag)
	{
		/*
		String userText = getUserText();
		setText(ChatLogPane.PRE_TEXT_STRING + userText + tag + ChatLogPane.POST_TEXT_STRING);
		System.out.println("New text="+getText());
		*/
	}
	
	public void bold()
	{
		// get a style context
		StyleContext sc = StyleContext.getDefaultStyleContext();
		
		// get the attributes of the current character
		AttributeSet current = getCharacterAttributes();
		
		// sometimes that might return null. If it does, take the empty set 
		if ( current == null )
		{
			current = SimpleAttributeSet.EMPTY;
		}
		
		// if the carat is in a non-bold location, make it bold. 
		// if it's bold, make it not bold
		boolean bBold = !StyleConstants.isBold(current);

		// apply the bold/non-bold to the attribute set
	    AttributeSet aset = sc.addAttribute(current, StyleConstants.Bold, new Boolean(bBold));
	    
	    // then apply the attributes to the current selection
	    setCharacterAttributes(aset, false);		
	}
	
	protected void processKeyEvent(KeyEvent e)
	{
		if ( e.getKeyCode() == KeyEvent.VK_ENTER )
		{
			// for some reason beyond my comprehension, JEditorPane sends
			// the enter key event twice every time the key is hit. 
			// we only want to have that happen once, thanks.
			if ( m_bIgnoreNextEnter ) 
			{
				m_bIgnoreNextEnter = false;
				return;
			}
			
			GametableFrame.getGametableFrame().textEntryEnterKey();
			m_bIgnoreNextEnter = true;
			return;
		}
		super.processKeyEvent(e);
	}
	
	private boolean m_bIgnoreNextEnter;
}
