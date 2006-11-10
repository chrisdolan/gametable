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
    private String     m_playerName;
    private String     m_characterName;
    private boolean    m_bIsHostPlayer = false;

    // for use by the host.
    private Connection m_connection;

    // pointing
    private boolean    m_bIsPointing;
    private Point      m_point;
    private Point      m_prevPoint;
    
    private int m_id;

    public Player(String playerName, String characterName, int id)
    {
        m_playerName = playerName;
        m_characterName = characterName;
        m_id = id;
    }
    
    // returns trus if the player name or character name matches
    // the sent in name in a case-insensitive comparison
    public boolean hasName(String name)
    {
    	String playerName = getPlayerName().toLowerCase();
    	String characterName = getCharacterName().toLowerCase();
    	String comp = name.toLowerCase();
    	if ( comp.equals(playerName) ) return true;
    	if ( comp.equals(characterName) ) return true;
    	return false;
    }

    public String toString()
    {
        String charName = getCharacterName();
        String playerName = getPlayerName();
        if (charName == null || charName.length() == 0)
        {
            return playerName;
        }

        if (playerName == null || playerName.length() == 0)
        {
            return charName;
        }
        return charName + " (" + playerName + ")";
    }

    public String getPlayerName()
    {
        return m_playerName;
    }

    public String getCharacterName()
    {
        return m_characterName;
    }

    /**
     * @param id The m_Id to set.
     */
    public void setId(int id)
    {
        this.m_id = id;
    }

    /**
     * @return Returns the m_Id.
     */
    public int getId()
    {
        return m_id;
    }

    /**
     * @param b The new hosting status.
     */
    public void setHostPlayer(boolean b)
    {
        m_bIsHostPlayer = b;
    }

    /**
     * @return Returns the hosting status of this player.
     */
    public boolean isHostPlayer()
    {
        return m_bIsHostPlayer;
    }

    /**
     * @param c The connection to set.
     */
    public void setConnection(Connection c)
    {
        m_connection = c;
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
        return m_connection;
    }

    /**
     * @param m_bIsPointing The m_bIsPointing to set.
     */
    public void setPointing(boolean bIsPointing)
    {
        this.m_bIsPointing = bIsPointing;
    }

    /**
     * @return Returns the m_bIsPointing.
     */
    public boolean isPointing()
    {
        return m_bIsPointing;
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public void setPoint(int x, int y)
    {
        setPoint(new Point(x, y));
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public void setPoint(Point p)
    {
        setPrevPoint(getPoint());
        m_point = p;
    }

    /**
     * @param m_pointX The m_pointX to set.
     */
    public Point getPoint()
    {
        return m_point;
    }

    /**
     * @param m_prevPointX The m_prevPointX to set.
     */
    public void setPrevPoint(int x, int y)
    {
        setPrevPoint(new Point(x, y));
    }

    /**
     * @param m_prevPointX The m_prevPointX to set.
     */
    public void setPrevPoint(Point p)
    {
        m_prevPoint = p;
    }

    /**
     * @return the previous pointed to point.
     */
    public Point getPrevPoint()
    {
        return m_prevPoint;
    }

}
