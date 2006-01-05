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
	/********************* CONSTANTS *************************/
	public final static int MAX_UNDO_LEVELS = 20;
	
	/********************* CONSTRUCTION *************************/
	private GametableMap()
	{
		// no default construction allowed
	}
	
	// bIsSharedMap is true if this is the shared gametable map.
	// it is false if it's a private map (private layer).
	public GametableMap(boolean bIsSharedMap)
	{
		m_bIsSharedMap = bIsSharedMap;
		
		// seed the undo stack with the "blank state"
		beginUndoableAction();
		endUndoableAction(-1, -1);
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
        if (modelPosition == null)
        {
            return null;
        }
        
        Pog pogHit = null;
        Pog underlayHit = null;

        for (int i = 0; i < getNumPogs(); i++)
        {
            Pog pog = getPogAt(i);

            if (pog.testHit(modelPosition))
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
	
	/******************* UNDO MANAGEMENT**********************/
	public void clearUndos()
	{
		m_undoLevels = new ArrayList();
		m_redoIndex = -1;

		// seed it
		beginUndoableAction();
		endUndoableAction(-1, -1);
	}
	
	// call before making changes you want to be undoable
	public void beginUndoableAction()
	{
		// nothing needed here yet
	}

	// call after making the changes you want undoable
	public void endUndoableAction(int playerID, int stateID)
	{
		// adding an action means removing any actions from the undo tree beyond the current state
		if ( m_redoIndex >= 0 )
		{
			killStates(m_redoIndex);
			m_redoIndex = -1;
		}
		
		MapState state = new MapState();
		state.setLines(m_lines);
		state.m_playerID = playerID;
		state.m_stateID = stateID;
		
		// add it to the undo stack
		m_undoLevels.add(state);
		
		// trim the stack if necessary
		if ( m_undoLevels.size() > MAX_UNDO_LEVELS )
		{
			// dump the earliest one
			m_undoLevels.remove(0);
		}
		
		// System.out.println("Added undoable - state:"+stateID+", plr:"+playerID);
	}
	
	public boolean canUndo()
	{
		// if there's nothing to undo, then no, you can't undo it
		if ( m_undoLevels.size() == 0 )
		{
			return false;
		}
		
		if ( !m_bIsSharedMap )
		{
			// we're the private layer. We can undo anything
			return true;
		}
		
		// the most recent action has to be yours for it to be undoable
		int myID = GametableFrame.g_gameTableFrame.getMeID();
		
		MapState lastUndoable = (MapState)m_undoLevels.get(m_undoLevels.size()-1);
		if ( lastUndoable.m_playerID == myID )
		{
			// you did the last undoable action. You can undo
			return true;
		}
		return false;
	}

	public boolean canRedo()
	{
		// you can't redo if there's no action to redo
		if ( m_redoIndex < 0 )
		{
			return false;
		}
		
		// you can redo if it's the private layer
		if ( !m_bIsSharedMap )
		{
			return true;
		}
		
		// you can't redo if it's not an action you did
		int myID = GametableFrame.g_gameTableFrame.getMeID();
		
		MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
		if ( nextRedoable.m_playerID == myID )
		{
			// you did the last redoable action. You can redo it
			return true;
		}
		return false;
	}	
	
	public void redoNextRecent()
	{
		MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
		GametableFrame frame = GametableFrame.g_gameTableFrame;
		GametableCanvas canvas = frame.m_gametableCanvas;
		if ( m_bIsSharedMap )
		{
	    	if ( canvas.isPublicMap() )
	    	{
	    		frame.send(PacketManager.makeRedoPacket(nextRedoable.m_stateID));
		
		        if (frame.m_netStatus != GametableFrame.NETSTATE_JOINED)
		        {
					redo(nextRedoable.m_stateID);
		        }
	    	}
	    	else
	    	{
				redo(nextRedoable.m_stateID);
	    	}
		}
		else
		{
			// redoing on the private map doesn't work through IDs,
			// and causes no network activity
			redo();
		}	
	}
	
	
	public void redo()
	{
		if ( !canRedo() )
		{
			return;
		}
		
		// fairly simple, actually. just adopt the state and advance the redo
		MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
		adoptState(nextRedoable);
		m_redoIndex++;
		
		if ( m_redoIndex >= m_undoLevels.size() )
		{
			// we've redone up to the end of the undo stack.
			m_redoIndex = -1;
		}
	}
	
	public void redo(int stateID)
	{
		// this function has no meaning on the private map
		if ( !m_bIsSharedMap )
		{
			return;
		}
		
		// for redo, we don't care if it's you who did the undoable action or not.
		// it could have been sent in from another player. We just redo it.
		
		// first, find the action.
		int stateIdx = -1; 
		for ( int i=0 ; i<m_undoLevels.size() ; i++ )
		{
			MapState state = (MapState)m_undoLevels.get(i);
			if ( state.m_stateID == stateID )
			{
				// this is the action that we can redo.
				stateIdx = i;
			}
		}
		
		if ( stateIdx < 0 )
		{
			// Houston... we have a problem.
			// if we're here, it means someone managed to send an redo
			// command and we don't have the state to revert to. This
			// means we'll probably desynch with the rest of the players.
			// This shouldn't happen. But on the offchance that it does for
			// some unknown reason, we should defensively return, rather than
			// crash. Desynched is better than crashed.
			return;
		}
		
		
		// get the state
		MapState redoTo = (MapState)m_undoLevels.get(stateIdx);
		
		// adopt the state
		adoptState(redoTo);
		
		// now we have to trash all states beyond this undo state
		m_redoIndex = stateIdx + 1;	
		
		if ( m_redoIndex >= m_undoLevels.size() )
		{
			// no worlds left to conquer
			m_redoIndex = -1;
		}
	}
	
	private int getUseableStackSize()
	{
		// youcan't look at undoables past the redoIndex
		int useableStackSize = m_undoLevels.size();
		if ( m_redoIndex >=0 )
		{
			useableStackSize  = m_redoIndex;
		}

		return useableStackSize;
	}
	
	public void undoMostRecent()
	{
		// safety check
		if ( !canUndo() )
		{
			return;
		}
		
		int useableStackSize = getUseableStackSize();
		
		if ( m_bIsSharedMap )
		{
			MapState lastUndoable = (MapState)m_undoLevels.get(useableStackSize-1);
			GametableFrame frame = GametableFrame.g_gameTableFrame;
			GametableCanvas canvas = frame.m_gametableCanvas;
	    	if ( canvas.isPublicMap() )
	    	{
	    		frame.send(PacketManager.makeUndoPacket(lastUndoable.m_stateID));
		
		        if (frame.m_netStatus != GametableFrame.NETSTATE_JOINED)
		        {
					undo(lastUndoable.m_stateID);
		        }
	    	}
	    	else
	    	{
				undo(lastUndoable.m_stateID);
	    	}
		}
		else
		{
			// undoing on the private map doesn't work through IDs,
			// and causes no network activity
			int undoToIdx = useableStackSize-2;
			if ( undoToIdx < 0 )
			{
				// nothing to undo
				return;
			}
			MapState undoTo = (MapState)m_undoLevels.get(undoToIdx);
			adoptState(undoTo);
			m_redoIndex = useableStackSize-1;
		}
	}
	
	public void undo(int stateID)
	{
		// this function has no meaning on the private map
		if ( !m_bIsSharedMap )
		{
			return;
		}
		
		// for undo, we don't care if it's you who did the undoable action or not.
		// it could have been sent in from another player. We just undo it.
		
		// first, find the action.
		int useableStackSize = getUseableStackSize();
		int stateIdx = -1; 
		for ( int i=0 ; i<useableStackSize ; i++ )
		{
			MapState state = (MapState)m_undoLevels.get(i);
			if ( state.m_stateID == stateID )
			{
				// this is the action that we can undo.
				// so the state we need to set it to is the action
				// BEFORE this one.
				stateIdx = i-1;
			}
		}
		
		if ( stateIdx < 0 )
		{
			// Houston... we have a problem.
			// if we're here, it means someone managed to send an undo
			// command and we don't have the state to revert to. This
			// means we'll probably desynch with the rest of the players.
			// This shouldn't happen. But on the offchance that it does for
			// some unknown reason, we should defensively return, rather than
			// crash. Desynched is better than crashed.
			return;
		}
		
		
		// get the state
		MapState undoTo = (MapState)m_undoLevels.get(stateIdx);
		// System.out.println("Undoing to ID:" + undoTo.m_stateID);
		

		// adopt the state
		adoptState(undoTo);
		
		// now we have to trash all states beyond this undo state
		m_redoIndex = stateIdx + 1;
	}
	
	private void killStates(int startIdx)
	{
		while ( m_undoLevels.size() > startIdx )
		{
			m_undoLevels.remove(startIdx);
		}
	}
	
	private void adoptState(MapState state)
	{
		// adopt the lines from the MapState
		m_lines = new ArrayList();
		for ( int i=0 ; i<state.m_lineSegments.size() ; i++ )
		{
			LineSegment ls = (LineSegment)state.m_lineSegments.get(i);
			LineSegment toAdd = new LineSegment(ls);
			m_lines.add(toAdd);
		}
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
    
    private List m_undoLevels = new ArrayList();
    private int m_redoIndex = -1;
}