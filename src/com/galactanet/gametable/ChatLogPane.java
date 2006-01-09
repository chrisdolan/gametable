/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import javax.swing.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;


/**
 * TODO: comment
 *
 * @author sephalon
 */
public class ChatLogPane extends JPanel implements Scrollable
{
	
	public ChatLogPane()
	{
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
	
	// inner class
	public class FontRun
	{
		public static final int TEXT_SEPERATION = 2; // pixels between lines of text
		
		public FontRun()
		{
			m_fontSize = 14;
			m_fontColor = Color.BLACK;
		}
		
		// startX and startY are the top left of the text run. This will
		// figure out the start and end locations of the font run.
		public void calcLocations(Rectangle displayArea, int startX, int startY)
		{
			// well the start locations are easy enough
			m_startX = startX;
			m_startY = startY;
			
			// ignore line wrapping for now. 
			Font font = getFontForRun();
			FontRenderContext context = new FontRenderContext(new AffineTransform(), false, false);
			TextLayout layout = new TextLayout(m_text, font, context);
	        Rectangle2D stringBounds = layout.getBounds();
	        
	        // m_endX = m_startX + (int)stringBounds.getWidth(); // HACKED OUT for testing
	        m_endY = m_startY + (int)stringBounds.getHeight();
	        m_endY += TEXT_SEPERATION;
		}
		
		public void draw(Graphics g)
		{
			Font oldFont = g.getFont();
			g.setFont(getFontForRun());
			g.setColor(m_fontColor);
			g.drawString(m_text, m_startX, m_startY);
		}
		
		public Font getFontForRun()
		{
			if ( m_font == null )
			{
				int params = 0;
				
				if ( m_bold ) params |= Font.BOLD;
				if ( m_italic ) params |= Font.ITALIC;
				m_font = new Font("Arial", params, m_fontSize);
			}
			return m_font;
		}
		
		public void setText(String text)
		{
			m_text = text;
		}
		
		public String m_text;
		public boolean m_bold; 
		public boolean m_italic; 
		public int m_fontSize;
		public Color m_fontColor;
	    public Font m_font;
		
		// the starting location
		public int m_startX;
		public int m_startY;
		
		// the end location, which is the start
		// location of the next font run
		public int m_endX;
		public int m_endY;
	};
	
	public void addText(String toAdd)
	{
		// for now, we just make a new FontRun
		FontRun newFontRun = new FontRun();
		newFontRun.setText(toAdd);
		
		addFontRun(newFontRun);
	}
	
	public void addFontRun(FontRun newFontRun)
	{
		int startX = 0;
		int startY = 0;
		
		if ( m_fontRuns.size() != 0 )
		{
			// there are font runs
			// take the last one's end location to determine where this new one starts
			FontRun last = (FontRun)m_fontRuns.get(m_fontRuns.size()-1);
			startX = last.m_endX;
			startY = last.m_endY;
		}
		newFontRun.calcLocations(this.getBounds(), startX, startY);
		m_fontRuns.add(newFontRun);
		revalidate();
		
		// make a rect that includes the end position of the newly added fontrun
		Rectangle r = new Rectangle();
		r.x = newFontRun.m_endX;
		r.y = newFontRun.m_endY;
		r.width = 1;
		r.height = 1;
		scrollRectToVisible(r);
		
		repaint();
	}
	
    public void paint(Graphics g)
    {
        super.paint(g);
        
        for ( int i=0 ; i<m_fontRuns.size() ; i++ )
        {
			FontRun fontRun = (FontRun)m_fontRuns.get(i);
			fontRun.draw(g);
        }
    }
    
    /*
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        int maxX = 100;
        int maxY = 100;

        // if there are font runs, note the actual desired size
		if ( m_fontRuns.size() != 0 )
		{
			// there are font runs
			// take the last one's end location to find the bottom
			FontRun last = (FontRun)m_fontRuns.get(m_fontRuns.size()-1);
			maxX = last.m_endX;
			maxY = last.m_endY;
		}

        return new Dimension(maxX + 2, maxY + 2);
    }

    // --- Scrollable Implementation ---
    
    /*
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        // TODO: calculate size based on content
        return getPreferredSize();
    }

    /*
     * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return visibleRect.height * 3 / 4;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return visibleRect.height / 15;
    }

    // Data
    List m_fontRuns = new ArrayList();
    JScrollPane m_scrollPane;
}
