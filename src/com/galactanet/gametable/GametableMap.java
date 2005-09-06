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

	/**************************** CLASS DATA *******************************/
    // lines on the map
    protected List             m_lines               = new ArrayList();

    // pogs on the map
    protected List             m_pogs                = new ArrayList();
    
    protected boolean 		   m_bIsSharedMap;
}