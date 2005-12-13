/*
 * Created on Dec 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.galactanet.gametable;

import java.awt.*;

/**
 * @author Andy
 *
 * Manages a spinning progress indicator on its own inside the component 
 * given
 */
public class ProgressSpinner implements Runnable
{
	public final static int SPIN_STATES = 8;
	
	public ProgressSpinner()
	{
		m_bActive = false;
		m_bCanActivate = true;
		m_spinState = 0;
	}
	
	void init()
	{
        m_images[0] = UtilityFunctions.getImage("assets/spinner0.png");
        m_images[1] = UtilityFunctions.getImage("assets/spinner1.png");
        m_images[2] = UtilityFunctions.getImage("assets/spinner2.png");
        m_images[3] = UtilityFunctions.getImage("assets/spinner3.png");
        m_images[4] = UtilityFunctions.getImage("assets/spinner4.png");
        m_images[5] = UtilityFunctions.getImage("assets/spinner5.png");
        m_images[6] = UtilityFunctions.getImage("assets/spinner6.png");
        m_images[7] = UtilityFunctions.getImage("assets/spinner7.png");
	}
	
	public boolean isActive()
	{
		return m_bActive;
	}
	
	public void activate(Component parent)
	{
		if ( !m_bCanActivate )
		{
			return;
		}
		
		// lazy init
		if ( m_images[0] == null )
		{
			init();
		}
		
		if ( parent == null )
		{
			throw new IllegalArgumentException("ProgressSpinner parent can not be null");
		}

		// start 'er up
		m_parent = parent;
		m_bActive = true;
		m_bCanActivate = false;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void deactivate()
	{
		m_bActive = false;
	}
	
	public void run()
	{
		while ( m_bActive )
		{
			// wait some time
			try
			{
				Thread.sleep(250);
			}
			catch ( Exception e )
			{
				
			}
			
			// display stuff
			String strLoading = "STREAMING IMAGES";
			Graphics g = m_parent.getGraphics();
	        FontMetrics metrics = g.getFontMetrics();
	        Rectangle stringBounds = metrics.getStringBounds(strLoading, g).getBounds();
			int boxWidth = (int)stringBounds.getWidth();
			int boxHeight = (int)stringBounds.getHeight();
			
			Image spinImage = m_images[m_spinState];
			
			int spinnerHeight = spinImage.getHeight(m_parent);
			int xOutset = 20;
			int yOutset = 10 + spinnerHeight;
			boxWidth += xOutset;
			boxHeight += yOutset;
			
			// draw a box
			int x = (m_parent.getWidth() - boxWidth)/2;
			int y = (m_parent.getHeight() - boxHeight)/2;
			g.setColor(Color.GRAY);
			g.fillRect(x, y, boxWidth, boxHeight);
			g.setColor(Color.BLACK);
			g.drawRect(x, y, boxWidth, boxHeight);

			// draw the loading string
	        int stringX = x + xOutset/2;
	        int stringY = y + 4 + metrics.getAscent();
	        g.drawString(strLoading, stringX, stringY);
	        
	        // draw the spinner
	        int spinnerX = x + (boxWidth - spinImage.getWidth(m_parent))/2;
	        int spinnerY = y + boxHeight - spinImage.getHeight(m_parent) - 4;
	        g.drawImage(spinImage, spinnerX, spinnerY, m_parent);
			
			m_spinState++;
			if ( m_spinState >= SPIN_STATES )
			{
				m_spinState = 0;
			}
		}
		
		m_parent.repaint();
		m_bCanActivate = true;
	}
	
	protected Component m_parent;
	protected boolean m_bActive;
	protected boolean m_bCanActivate;
	protected int m_spinState;
	
	// the images
	Image m_images[] = new Image[SPIN_STATES];
}
