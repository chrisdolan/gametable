/*
 * GametableMap.java: GameTable is in the Public Domain.
 */
package com.galactanet.gametable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * This class stores the data related to a gametable map. This includes line, pogs, and underlays
 * 
 * @author sephalon
 */
public class GametableMap
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

	/******************* LINES MANAGEMENT**********************/
	public int getNumLines()
	{
		return m_lines.size();
	}
	
	public LineSegment getLineAt(int idx)
	{
		return (LineSegment)m_lines.get(idx);
	}
	
	public void addLine(LineSegment ls)
	{
		m_lines.add(ls);
	}
	
	public void removeLine(LineSegment ls)
	{
		m_lines.remove(ls);
	}
	
	public void clearLines()
	{
		m_lines = new ArrayList();
	}

	/******************* POGS MANAGEMENT**********************/
	public int getNumPogs()
	{
		return m_pogs.size();
	}
	
	public Pog getPogAt(int idx)
	{
		return (Pog)m_pogs.get(idx);
	}

	public void addPog(Pog pog)
	{
		m_pogs.add(pog);
	}

	public void removePog(Pog pog)
	{
		m_pogs.remove(pog);
	}
	
	public void clearPogs()
	{
		m_pogs = new ArrayList();
	}
	
    public Pog getPogAt(Point modelPosition)
    {
        Pog pogHit = null;
        Pog underlayHit = null;

        for (int i = 0; i < getNumPogs(); i++)
        {
            Pog pog = getPogAt(i);

            if (pog.modelPtInBounds(modelPosition.x, modelPosition.y))
            {
                // they clicked this pog
                if (pog.isUnderlay())
                {
                    underlayHit = pog;
                }
                else
                {
                    pogHit = pog;
                }
            }
        }

        // pogs take priority over underlays
        if (pogHit != null)
        {
            return pogHit;
        }

        return underlayHit;
    }	

	/******************* SCROLL MANAGEMENT**********************/
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