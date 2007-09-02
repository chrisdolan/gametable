/*
 * Player.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Point;

import com.galactanet.gametable.net.Connection;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class Player
{
    private boolean      m_bIsHostPlayer = false;
    // pointing
    private boolean      m_bIsPointing;
    private final String m_characterName;

    // for use by the host.
    private Connection   m_connection;

    private int          m_id;
    private final String m_playerName;
    private Point        m_point;

    private Point        m_prevPoint;

    public Player(final String playerName, final String characterName, final int id)
    {
        m_playerName = playerName;
        m_characterName = characterName;
        m_id = id;
    }

    public String getCharacterName()
    {
        return m_characterName;
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
        return m_connection;
    }

    /**
     * @return Returns the m_Id.
     */
    public int getId()
    {
        return m_id;
    }

    public String getPlayerName()
    {
        return m_playerName;
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public Point getPoint()
    {
        return m_point;
    }

    /**
     * @return the previous pointed to point.
     */
    public Point getPrevPoint()
    {
        return m_prevPoint;
    }

    // returns trus if the player name or character name matches
    // the sent in name in a case-insensitive comparison
    public boolean hasName(final String name)
    {
        final String playerName = getPlayerName().toLowerCase();
        final String characterName = getCharacterName().toLowerCase();
        final String comp = name.toLowerCase();
        if (comp.equals(playerName))
        {
            return true;
        }
        if (comp.equals(characterName))
        {
            return true;
        }
        return false;
    }

    /**
     * @return Returns the hosting status of this player.
     */
    public boolean isHostPlayer()
    {
        return m_bIsHostPlayer;
    }

    /**
     * @return Returns the m_bIsPointing.
     */
    public boolean isPointing()
    {
        return m_bIsPointing;
    }

    /**
     * @param c The connection to set.
     */
    public void setConnection(final Connection c)
    {
        m_connection = c;
    }

    /**
     * @param b The new hosting status.
     */
    public void setHostPlayer(final boolean b)
    {
        m_bIsHostPlayer = b;
    }

    /**
     * @param id The m_Id to set.
     */
    public void setId(final int id)
    {
        m_id = id;
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public void setPoint(final int x, final int y)
    {
        setPoint(new Point(x, y));
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public void setPoint(final Point p)
    {
        setPrevPoint(getPoint());
        m_point = p;
    }

    /**
     * @param m_bIsPointing The m_bIsPointing to set.
     */
    public void setPointing(final boolean bIsPointing)
    {
        m_bIsPointing = bIsPointing;
    }

    /**
     * @param m_prevPointX The m_prevPointX to set.
     */
    public void setPrevPoint(final int x, final int y)
    {
        setPrevPoint(new Point(x, y));
    }

    /**
     * @param m_prevPointX The m_prevPointX to set.
     */
    public void setPrevPoint(final Point p)
    {
        m_prevPoint = p;
    }

    public String toString()
    {
        final String charName = getCharacterName();
        final String playerName = getPlayerName();
        if ((charName == null) || (charName.length() == 0))
        {
            return playerName;
        }

        if ((playerName == null) || (playerName.length() == 0))
        {
            return charName;
        }
        return charName + " (" + playerName + ")";
    }

}
