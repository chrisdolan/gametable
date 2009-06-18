/*
 * GametableMap.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Point;
import java.util.*;

import com.galactanet.gametable.util.UtilityFunctions;



/**
 * This class stores the data related to a gametable map. This includes line, pogs, and underlays
 * 
 * @author sephalon
 */
public class GametableMap
{
    /* ******************* CONSTANTS ************************ */

    public final static int MAX_UNDO_LEVELS = 20;

    /* ******************* CONSTRUCTION ************************ */

    protected boolean       m_bIsSharedMap;

    /** ************************** CLASS DATA ****************************** */
    // lines on the map
    protected List          m_lines         = new ArrayList();

    /* ***************** LINES MANAGEMENT********************* */

    protected SortedSet     m_orderedPogs   = new TreeSet();

    // pogs on the map
    protected List          m_pogs          = new ArrayList();

    private int             m_redoIndex     = -1;

    // add to origin to get actual coordinates.
    // (Negative if inside image)
    private int             m_scrollX;

    // add to origin to get actual coordinates.
    // (Negative if inside image)
    private int             m_scrollY;

    /* ***************** POGS MANAGEMENT********************* */

    private List            m_undoLevels    = new ArrayList();

    // bIsSharedMap is true if this is the shared gametable map.
    // it is false if it's a private map (private layer).
    public GametableMap(final boolean bIsSharedMap)
    {
        m_bIsSharedMap = bIsSharedMap;

        // seed the undo stack with the "blank state"
        beginUndoableAction();
        endUndoableAction(-1, -1);
    }

    public void addLine(final LineSegment ls)
    {
        m_lines.add(ls);
    }

    public void addPog(final Pog pog)
    {
        m_pogs.add(pog);
        if (!pog.isUnderlay())
        {
            m_orderedPogs.add(pog);
        }
    }

    private void adoptState(final MapState state)
    {
        // adopt the lines from the MapState
        m_lines = new ArrayList();
        for (int i = 0; i < state.m_lineSegments.size(); i++)
        {
            final LineSegment ls = (LineSegment)state.m_lineSegments.get(i);
            final LineSegment toAdd = new LineSegment(ls);
            m_lines.add(toAdd);
        }
    }

    // call before making changes you want to be undoable
    public void beginUndoableAction()
    {
        // nothing needed here yet
    }

    public boolean canRedo()
    {
        // you can't redo if there's no action to redo
        if (m_redoIndex < 0)
        {
            return false;
        }

        // you can redo if it's the private layer
        if (!m_bIsSharedMap)
        {
            return true;
        }

        // you can't redo if it's not an action you did
        final int myID = GametableFrame.getGametableFrame().getMyPlayerId();

        final MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
        if (nextRedoable.m_playerID == myID)
        {
            // you did the last redoable action. You can redo it
            return true;
        }
        return false;
    }

    public boolean canUndo()
    {
        // if there's nothing to undo, then no, you can't undo it
        if (m_undoLevels.size() == 0)
        {
            return false;
        }

        if (!m_bIsSharedMap)
        {
            // we're the private layer. We can undo anything
            return true;
        }

        // the most recent action has to be yours for it to be undoable
        final int myID = GametableFrame.getGametableFrame().getMyPlayerId();

        final MapState lastUndoable = (MapState)m_undoLevels.get(m_undoLevels.size() - 1);
        if (lastUndoable.m_playerID == myID)
        {
            // you did the last undoable action. You can undo
            return true;
        }
        return false;
    }

    public void clearLines()
    {
        m_lines = new ArrayList();
    }

    public void clearPogs()
    {
        m_pogs.clear();
        m_orderedPogs.clear();
    }

    /** ***************** UNDO MANAGEMENT********************* */
    public void clearUndos()
    {
        m_undoLevels = new ArrayList();
        m_redoIndex = -1;

        // seed it
        beginUndoableAction();
        endUndoableAction(-1, -1);
    }

    // call after making the changes you want undoable
    public void endUndoableAction(final int playerID, final int stateID)
    {
        // adding an action means removing any actions from the undo tree beyond the current state
        if (m_redoIndex >= 0)
        {
            killStates(m_redoIndex);
            m_redoIndex = -1;
        }

        final MapState state = new MapState();
        state.setLines(m_lines);
        state.m_playerID = playerID;
        state.m_stateID = stateID;

        // add it to the undo stack
        m_undoLevels.add(state);

        // trim the stack if necessary
        if (m_undoLevels.size() > MAX_UNDO_LEVELS)
        {
            // dump the earliest one
            m_undoLevels.remove(0);
        }

        // System.out.println("Added undoable - state:"+stateID+", plr:"+playerID);
    }

    public LineSegment getLineAt(final int idx)
    {
        return (LineSegment)m_lines.get(idx);
    }

    /* ***************** SCROLL MANAGEMENT********************* */

    public int getNumLines()
    {
        return m_lines.size();
    }

    public int getNumPogs()
    {
        return m_pogs.size();
    }

    public SortedSet getOrderedPogs()
    {
        return Collections.unmodifiableSortedSet(m_orderedPogs);
    }

    public Pog getPog(final int idx)
    {
        return (Pog)m_pogs.get(idx);
    }

    public Pog getPogAt(final Point modelPosition)
    {
        if (modelPosition == null)
        {
            return null;
        }

        Pog pogHit = null;
        Pog underlayHit = null;

        for (int i = 0; i < getNumPogs(); i++)
        {
            final Pog pog = getPog(i);

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

    public Pog getPogByID(final int id)
    {
        for (int i = 0, size = getNumPogs(); i < size; ++i)
        {
            final Pog pog = getPog(i);
            if (pog.getId() == id)
            {
                return pog;
            }
        }

        return null;
    }

    public Pog getPogNamed(final String pogName)
    {
        final List pogs = getPogsNamed(pogName);
        if (pogs.isEmpty())
        {
            return null;
        }

        return (Pog)pogs.get(0);
    }

    public List getPogs()
    {
        return Collections.unmodifiableList(m_pogs);
    }

    public List getPogsNamed(final String pogName)
    {
        final String normalizedName = UtilityFunctions.normalizeName(pogName);
        final List retVal = new ArrayList();
        for (int i = 0, size = getNumPogs(); i < size; ++i)
        {
            final Pog pog = getPog(i);
            if (UtilityFunctions.normalizeName(pog.getText()).equals(normalizedName))
            {
                retVal.add(pog);
            }
        }

        return retVal;
    }

    public int getScrollX()
    {
        return m_scrollX;
    }

    public int getScrollY()
    {
        return m_scrollY;
    }

    private int getUseableStackSize()
    {
        // youcan't look at undoables past the redoIndex
        int useableStackSize = m_undoLevels.size();
        if (m_redoIndex >= 0)
        {
            useableStackSize = m_redoIndex;
        }

        return useableStackSize;
    }

    private void killStates(final int startIdx)
    {
        while (m_undoLevels.size() > startIdx)
        {
            m_undoLevels.remove(startIdx);
        }
    }

    public void redo()
    {
        if (!canRedo())
        {
            return;
        }

        // fairly simple, actually. just adopt the state and advance the redo
        final MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
        adoptState(nextRedoable);
        m_redoIndex++;

        if (m_redoIndex >= m_undoLevels.size())
        {
            // we've redone up to the end of the undo stack.
            m_redoIndex = -1;
        }
    }

    public void redo(final int stateID)
    {
        // this function has no meaning on the private map
        if (!m_bIsSharedMap)
        {
            return;
        }

        // for redo, we don't care if it's you who did the undoable action or not.
        // it could have been sent in from another player. We just redo it.

        // first, find the action.
        int stateIdx = -1;
        for (int i = 0; i < m_undoLevels.size(); i++)
        {
            final MapState state = (MapState)m_undoLevels.get(i);
            if (state.m_stateID == stateID)
            {
                // this is the action that we can redo.
                stateIdx = i;
            }
        }

        if (stateIdx < 0)
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
        final MapState redoTo = (MapState)m_undoLevels.get(stateIdx);

        // adopt the state
        adoptState(redoTo);

        // now we have to trash all states beyond this undo state
        m_redoIndex = stateIdx + 1;

        if (m_redoIndex >= m_undoLevels.size())
        {
            // no worlds left to conquer
            m_redoIndex = -1;
        }
    }

    public void redoNextRecent()
    {
        final MapState nextRedoable = (MapState)m_undoLevels.get(m_redoIndex);
        final GametableFrame frame = GametableFrame.getGametableFrame();
        final GametableCanvas canvas = frame.getGametableCanvas();
        if (m_bIsSharedMap)
        {
            if (canvas.isPublicMap())
            {
                frame.send(PacketManager.makeRedoPacket(nextRedoable.m_stateID));

                if (frame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
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

    public void removeCardPogsForCards(final DeckData.Card discards[])
    {
        final List removeList = new ArrayList();

        for (int i = 0; i < m_pogs.size(); i++)
        {
            final Pog pog = (Pog)m_pogs.get(i);
            if (pog.isCardPog())
            {
                final DeckData.Card pogCard = pog.getCard();
                // this is a card pog. Is it oue of the discards?
                for (int j = 0; j < discards.length; j++)
                {
                    if (pogCard.equals(discards[j]))
                    {
                        // it's the pog for this card
                        removeList.add(pog);
                    }
                }
            }
        }

        // remove any offending pogs
        if (removeList.size() > 0)
        {
            for (int i = 0; i < removeList.size(); i++)
            {
                removePog((Pog)removeList.get(i));
            }
        }
    }

    public void removeLine(final LineSegment ls)
    {
        m_lines.remove(ls);
    }

    public void removePog(final Pog pog)
    {
        m_pogs.remove(pog);
        m_orderedPogs.remove(pog);
    }

    public void reorderPogs(final Map changes)
    {
        if (changes == null)
        {
            return;
        }

        for (final Iterator iterator = changes.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry)iterator.next();
            final Integer id = (Integer)entry.getKey();
            final Long order = (Long)entry.getValue();

            setSortOrder(id.intValue(), order.longValue());
        }
    }

    public void setScroll(final int x, final int y)
    {
        m_scrollX = x;
        m_scrollY = y;
    }

    public void setSortOrder(final int id, final long order)
    {
        final Pog pog = getPogByID(id);
        if (pog == null)
        {
            return;
        }

        m_orderedPogs.remove(pog);
        pog.setSortOrder(order);
        m_orderedPogs.add(pog);
    }

    public void undo(final int stateID)
    {
        // this function has no meaning on the private map
        if (!m_bIsSharedMap)
        {
            return;
        }

        // for undo, we don't care if it's you who did the undoable action or not.
        // it could have been sent in from another player. We just undo it.

        // first, find the action.
        final int useableStackSize = getUseableStackSize();
        int stateIdx = -1;
        for (int i = 0; i < useableStackSize; i++)
        {
            final MapState state = (MapState)m_undoLevels.get(i);
            if (state.m_stateID == stateID)
            {
                // this is the action that we can undo.
                // so the state we need to set it to is the action
                // BEFORE this one.
                stateIdx = i - 1;
            }
        }

        if (stateIdx < 0)
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
        final MapState undoTo = (MapState)m_undoLevels.get(stateIdx);
        // System.out.println("Undoing to ID:" + undoTo.m_stateID);

        // adopt the state
        adoptState(undoTo);

        // now we have to trash all states beyond this undo state
        m_redoIndex = stateIdx + 1;
    }

    public void undoMostRecent()
    {
        // safety check
        if (!canUndo())
        {
            return;
        }

        final int useableStackSize = getUseableStackSize();

        if (m_bIsSharedMap)
        {
            final MapState lastUndoable = (MapState)m_undoLevels.get(useableStackSize - 1);
            final GametableFrame frame = GametableFrame.getGametableFrame();
            final GametableCanvas canvas = frame.getGametableCanvas();
            if (canvas.isPublicMap())
            {
                frame.send(PacketManager.makeUndoPacket(lastUndoable.m_stateID));

                if (frame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
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
            final int undoToIdx = useableStackSize - 2;
            if (undoToIdx < 0)
            {
                // nothing to undo
                return;
            }
            final MapState undoTo = (MapState)m_undoLevels.get(undoToIdx);
            adoptState(undoTo);
            m_redoIndex = useableStackSize - 1;
        }
    }
}
