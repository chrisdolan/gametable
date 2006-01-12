/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: comment
 *
 * @author sephalon
 */
public class ChatLogPane extends JEditorPane // implements Scrollable
{
	public final static String PRE_TEXT_STRING = "<html><head></head><body style=\"font-family:Arial,Helvetica,sans;font-size:12pt;\">";
	public final static String POST_TEXT_STRING = "</body></html>";
	
	public ChatLogPane()  
	{
		super("text/html", PRE_TEXT_STRING+POST_TEXT_STRING);        
        setEditable(false);
        setFocusable(true);
	}
	
	// returns the pane to add to UIs
	public Component getComponentToAdd()
	{
		if ( m_scrollPane == null )
		{
			m_scrollPane = new JScrollPane(this, 
	        		        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		
        return m_scrollPane;
	}
	
	public void addText(String toAdd)
	{
		System.out.println("Adding: "+toAdd);
		
        String rawText = getText();
        int end = rawText.lastIndexOf("</body>");
        int start = rawText.lastIndexOf('>', end) + 1;
        String cutText = rawText.substring(start, end).trim();

        entries.add(toAdd);

        StringBuffer text = new StringBuffer();
        text.append(PRE_TEXT_STRING);
        for (int i = 0, size = entries.size(); i < size; ++i)
        {
            String entry = (String)entries.get(i);
            text.append(entry);
            text.append("<br>\n");
        }
        text.append(POST_TEXT_STRING);
        setText(text.toString());
        // System.out.println("htmlPane:\n" + getText());	
	}

    // Data
    // List m_fontRuns = new ArrayList();
    JScrollPane m_scrollPane;
    private List entries = new ArrayList();
}
