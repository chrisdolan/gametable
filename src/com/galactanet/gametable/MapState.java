/*
 * MapState.java: GameTable is in the Public Domain.
 * 
 * This class keeps track of all the lines on the map. It is a simple
 * container class that for now contains just an collection of LineSegments.
 * It's used to track previous map states for undo purposes. 
 * In the future, there will probably be more than just LineSegments as
 * undoable map elements, hence this class.  
 */


package com.galactanet.gametable;

import java.util.ArrayList;
import java.util.List;



/**
 * 
 * @author sephalon
 */
public class MapState
{
    public List m_lineSegments; // the line segments

    public int  m_playerID;    // the ID of the player who put it into this state

    public int  m_stateID;     // the id of this state

    public MapState()
    {
    }

    public void setLines(final List lines)
    {
        m_lineSegments = new ArrayList();
        // we're going to copy every line segment
        for (int i = 0; i < lines.size(); i++)
        {
            final LineSegment ls = (LineSegment)lines.get(i);
            final LineSegment newLS = new LineSegment(ls);
            m_lineSegments.add(newLS);
        }
    }
}
