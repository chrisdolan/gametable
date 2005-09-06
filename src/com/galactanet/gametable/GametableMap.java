/*
 * GametableMap.java: GameTable is in the Public Domain.
 */
package com.galactanet.gametable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores the data related to a gametable map. This includes line, pogs, and underlays
 * 
 * @author sephalon
 */
class GametableMap
{
	private GametableMap()
	{
		// no default construction allowed
	}
	
	// bIsSharedMap is true if this is the shared gametable map.
	// it is false if it's a private map (private layer).
	public GametableMap(boolean bIsSharedMap)
	{
		m_bIsSharedMap = bIsSharedMap;
	}
	
	public List getLines()
	{
		return m_lines;
	}

	public void setLines(List lines)
	{
		m_lines = lines;
	}

	public List getPogs()
	{
		return m_pogs;
	}

	public void setPogs(List pogs)
	{
		m_pogs = pogs;
	}
	
	public void setScroll(int x, int y)
	{
		m_scrollX = x;
		m_scrollY = y;
	}

	public int getScrollX()
	{
		return m_scrollX;
	}

	public int getScrollY()
	{
		return m_scrollY;
	}

	/**************************** CLASS DATA *******************************/
    // lines on the map
    protected List             m_lines               = new ArrayList();

    // pogs on the map
    protected List             m_pogs                = new ArrayList();
    
    protected boolean 		   m_bIsSharedMap;

    // add to origin to get actual coordinates.
    // (Negative if inside image)
    private int              m_scrollX;

    // add to origin to get actual coordinates.
    // (Negative if inside image)
    private int              m_scrollY;

}