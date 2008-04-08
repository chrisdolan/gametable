/*
 * PacketManager.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Rectangle;
import java.io.*;
import java.util.*;

import com.galactanet.gametable.net.Connection;



/**
 * TODO: comment
 * 
 * 
 * @author sephalon
 */
public class PacketManager
{
    // --- Constants -------------------------------------------------------------------------------------------------

    /**
     * Set of files already asked for. TODO: Add some kind of timed retry feature.
     */
    private static Set      g_requestedFiles          = new HashSet();

    /**
     * A Map of sets of pending incoming requests that could not be fulfilled.
     */
    private static Map      g_unfulfilledRequests     = new HashMap();

    // Pog added
    public static final int PACKET_ADDPOG             = 5;

    // Packet sent by the host telling all the players in the game
    public static final int PACKET_CAST               = 1;

    // informs players that a deck is pulling all its cards
    // home. Either because the deck is being destroyed, or
    // because it's having a complete shuffling
    public static final int PACKET_DECK_CLEAR_DECK    = 25;

    // sent by players to tell everyone that they've discarded
    // one or more cards
    public static final int PACKET_DECK_DISCARD_CARDS = 26;

    // informs you of which decks exist
    public static final int PACKET_DECK_LIST          = 22;

    // sent TO players, giving them cards they requested
    // (in response to a PACKED_TECK_REQUEST_CARDS)
    public static final int PACKET_DECK_RECEIVE_CARDS = 24;

    // sent by players who are trying to draw cards
    public static final int PACKET_DECK_REQUEST_CARDS = 23;

    // Eraser used
    public static final int PACKET_ERASE              = 4;

    // png data transfer
    public static final int PACKET_FILE               = 12;

    // notification of a hex mode / grid mode change
    public static final int PACKET_HEX_MODE           = 14;

    // Lines being added
    public static final int PACKET_LINES              = 3;

    // Pog lock state changed
    public static final int PACKET_LOCKPOG            = 28;

    // notification that the host is done sending you the inital packets
    // you get when you log in
    public static final int PACKET_LOGIN_COMPLETE     = 15;

    // Pog moved
    public static final int PACKET_MOVEPOG            = 7;

    // host sends PING, client sends back PING
    public static final int PACKET_PING               = 16;

    // Packet sent by a new joiner as soon as he joins
    public static final int PACKET_PLAYER             = 0;

    // request for a png
    public static final int PACKET_PNGREQUEST         = 13;

    // pog reorder packet
    public static final int PACKET_POG_REORDER        = 21;

    // a pog size packet
    public static final int PACKET_POG_SIZE           = 19;

    // pog data change
    public static final int PACKET_POGDATA            = 9;

    // point state change
    public static final int PACKET_POINT              = 8;

    // private text packet
    public static final int PACKET_PRIVATE_TEXT       = 20;

    // recentering packet
    public static final int PACKET_RECENTER           = 10;

    // a redo packet
    public static final int PACKET_REDO               = 18;

    // join rejected
    public static final int PACKET_REJECT             = 11;

    // Pog removed
    public static final int PACKET_REMOVEPOGS         = 6;

    // Pog rotated
    public static final int PACKET_ROTATEPOG          = 27;

    // Pog flipped
    public static final int PACKET_FLIPPOG            = 30;

    // Packet with text to go to the text log
    public static final int PACKET_TEXT               = 2;

    // --- Static Members --------------------------------------------------------------------------------------------

    // Player is typing
    public static final int PACKET_TYPING             = 29;

    // an undo packet
    public static final int PACKET_UNDO               = 17;

    // --- Static Methods --------------------------------------------------------------------------------------------

    private static void addUnfulfilledRequest(final String filename, final Connection connection)
    {
        Set set = (Set)g_unfulfilledRequests.get(filename);
        if (set == null)
        {
            set = new HashSet();
            g_unfulfilledRequests.put(filename, set);
        }

        set.add(connection);
    }

    public static String getPacketName(final byte[] packet)
    {
        try
        {
            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            return getPacketName(dis.readInt());
        }
        catch (final IOException ioe)
        {
            return "ERROR";
        }
    }

    public static String getPacketName(final int type)
    {
        switch (type)
        {
            case PACKET_PLAYER:
                return "PACKET_PLAYER";
            case PACKET_REJECT:
                return "PACKET_REJECT";
            case PACKET_CAST:
                return "PACKET_CAST";
            case PACKET_TEXT:
                return "PACKET_TEXT";
            case PACKET_TYPING:
                return "PACKET_TYPING";
            case PACKET_LINES:
                return "PACKET_LINES";
            case PACKET_ERASE:
                return "PACKET_ERASE";
            case PACKET_ADDPOG:
                return "PACKET_ADDPOG";
            case PACKET_REMOVEPOGS:
                return "PACKET_REMOVEPOGS";
            case PACKET_MOVEPOG:
                return "PACKET_MOVEPOG";
            case PACKET_LOCKPOG:
                return "PACKET_LOCKPOG";
            case PACKET_POINT:
                return "PACKET_POINT";
            case PACKET_POGDATA:
                return "PACKET_POGDATA";
            case PACKET_RECENTER:
                return "PACKET_RECENTER";
            case PACKET_FILE:
                return "PACKET_FILE";
            case PACKET_PNGREQUEST:
                return "PACKET_PNGREQUEST";
            case PACKET_HEX_MODE:
                return "PACKET_HEX_MODE";
            case PACKET_LOGIN_COMPLETE:
                return "PACKET_LOGIN_COMPLETE";
            case PACKET_PING:
                return "PACKET_PING";
            case PACKET_UNDO:
                return "PACKET_UNDO";
            case PACKET_REDO:
                return "PACKET_REDO";
            case PACKET_POG_SIZE:
                return "PACKET_POG_SIZE";
            case PACKET_PRIVATE_TEXT:
                return "PACKET_PRIVATE_TEXT";
            case PACKET_POG_REORDER:
                return "PACKET_POG_REORDER";
            case PACKET_DECK_LIST:
                return "PACKET_DECK_LIST";
            case PACKET_DECK_REQUEST_CARDS:
                return "PACKET_DECK_REQUEST_CARDS";
            case PACKET_DECK_RECEIVE_CARDS:
                return "PACKET_DECK_RECEIVE_CARDS";
            case PACKET_DECK_CLEAR_DECK:
                return "PACKET_DECK_CLEAR_DECK";
            case PACKET_DECK_DISCARD_CARDS:
                return "PACKET_DECK_DISCARD_CARDS";
            default:
                return "PACKET_UNKNOWN";
        }
    }

    /* *********************** ADDPOG PACKET *********************************** */
    // calls for the pog to be added to the public layer
    public static byte[] makeAddPogPacket(final Pog pog)
    {
        return makeAddPogPacket(pog, true);
    }

    /* *********************** CAST PACKET *********************************** */

    public static byte[] makeAddPogPacket(final Pog pog, final boolean bPublicLayerPog)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_ADDPOG); // type
            dos.writeBoolean(bPublicLayerPog); // layer
            pog.writeToPacket(dos);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeCastPacket(final Player recipient)
    {
        try
        {
            // create a packet with all the players in it
            final GametableFrame frame = GametableFrame.getGametableFrame();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_CAST);

            final List players = frame.getPlayers();
            dos.writeInt(players.size());
            for (int i = 0; i < players.size(); i++)
            {
                final Player player = (Player)players.get(i);
                dos.writeUTF(player.getCharacterName());
                dos.writeUTF(player.getPlayerName());
                dos.writeInt(player.getId());
                dos.writeBoolean(player.isHostPlayer());
            }

            // finally, tell the recipient which player he is
            final int whichPlayer = frame.getPlayerIndex(recipient);
            dos.writeInt(whichPlayer);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** PLAYER PACKET *********************************** */

    /* ********************* CLEAR DECK PACKET *********************************** */
    public static byte[] makeClearDeckPacket(final String deckName)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_DECK_CLEAR_DECK); // packet type
            dos.writeUTF(deckName); // the deck in question

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** DECK LIST PACKET *********************************** */
    public static byte[] makeDeckListPacket(final List decks)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_DECK_LIST); // packet type
            dos.writeInt(decks.size()); // number of decks
            for (int i = 0; i < decks.size(); i++)
            {
                final Deck d = (Deck)decks.get(i);
                dos.writeUTF(d.m_name); // the name of this deck
            }
            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** TEXT PACKET *********************************** */

    /* ********************* DISCARD CARDS PACKET *********************************** */
    public static byte[] makeDiscardCardsPacket(final String playerName, final DeckData.Card cards[])
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_DECK_DISCARD_CARDS); // packet type
            dos.writeUTF(playerName); // the player doing the discarding

            dos.writeInt(cards.length); // how many cards
            // and now the cards
            for (int i = 0; i < cards.length; i++)
            {
                cards[i].write(dos);
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeErasePacket(final Rectangle r, final boolean bColorSpecific, final int color,
        final int authorPlayerID, final int stateID)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_ERASE); // type
            dos.writeInt(authorPlayerID);
            dos.writeInt(stateID);
            dos.writeInt(r.x);
            dos.writeInt(r.y);
            dos.writeInt(r.width);
            dos.writeInt(r.height);
            dos.writeBoolean(bColorSpecific);
            dos.writeInt(color);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** TYPING PACKET *********************************** */

    public static byte[] makeGridModePacket(final int hexMode)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_HEX_MODE); // type
            dos.writeInt(hexMode); // type

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeGrmPacket(final byte[] grmData)
    {
        // grmData will be the contents of the file
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            // write the packet type
            dos.writeInt(PACKET_FILE);

            // write the mime type
            dos.writeUTF("application/x-gametable-grm");

            // now write the data length
            dos.writeInt(grmData.length);

            // and finally, the data itself
            dos.write(grmData);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** PRIVATE TEXT PACKET *************************** */

    public static byte[] makeLinesPacket(final LineSegment[] lines, final int authorPlayerID, final int stateID)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_LINES); // type
            dos.writeInt(authorPlayerID);
            dos.writeInt(stateID);
            dos.writeInt(lines.length);
            for (int i = 0; i < lines.length; i++)
            {
                lines[i].writeToPacket(dos);
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* ************************ LOCKPOG PACKET ********************************* */
    public static byte[] makeLockPogPacket(final int id, final boolean newLocked)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_LOCKPOG); // type
            dos.writeInt(id);
            dos.writeBoolean(newLocked);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** LINES PACKET *********************************** */

    public static byte[] makeLoginCompletePacket()
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_LOGIN_COMPLETE); // type
            // there's actually no additional data. Just the info that the login is complete

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeMovePogPacket(final int id, final int newX, final int newY)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_MOVEPOG); // type
            dos.writeInt(id);
            dos.writeInt(newX);
            dos.writeInt(newY);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** ERASE PACKET *********************************** */

    public static byte[] makePingPacket()
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PING); // type
            // there's actually no additional data. Just the info that the login is complete

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePlayerPacket(final Player plr, final String password)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PLAYER);
            dos.writeInt(GametableFrame.COMM_VERSION);
            dos.writeUTF(password);
            dos.writeUTF(plr.getCharacterName());
            dos.writeUTF(plr.getPlayerName());
            dos.writeBoolean(plr.isHostPlayer());

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePngPacket(final String filename)
    {
        // load the entire png file
        final byte[] pngFileData = UtilityFunctions.loadFileToArray(filename);

        if (pngFileData == null)
        {
            return null;
        }

        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            // write the packet type
            dos.writeInt(PACKET_FILE);

            // write the mime type
            dos.writeUTF("image/png");

            // write the filename
            dos.writeUTF(UtilityFunctions.getUniversalPath(filename));

            // now write the data length
            dos.writeInt(pngFileData.length);

            // and finally, the data itself
            dos.write(pngFileData);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePngRequestPacket(final String filename)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PNGREQUEST); // type
            dos.writeUTF(UtilityFunctions.getUniversalPath(filename));

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePogDataPacket(final int id, final String s)
    {
        return makePogDataPacket(id, s, null, null);
    }

    public static byte[] makePogDataPacket(final int id, final String s, final Map toAdd, final Set toDelete)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POGDATA);
            dos.writeInt(id);
            if (s != null)
            {
                dos.writeBoolean(true);
                dos.writeUTF(s);
            }
            else
            {
                dos.writeBoolean(false);
            }

            // removing
            if (toDelete == null)
            {
                dos.writeInt(0);
            }
            else
            {
                dos.writeInt(toDelete.size());
                for (final Iterator iterator = toDelete.iterator(); iterator.hasNext();)
                {
                    final String key = (String)iterator.next();
                    dos.writeUTF(key);
                }
            }

            // adding
            if (toAdd == null)
            {
                dos.writeInt(0);
            }
            else
            {
                dos.writeInt(toAdd.size());
                for (final Iterator iterator = toAdd.entrySet().iterator(); iterator.hasNext();)
                {
                    final Map.Entry entry = (Map.Entry)iterator.next();
                    dos.writeUTF((String)entry.getKey());
                    dos.writeUTF((String)entry.getValue());
                }
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** REMOVEPOG PACKET *********************************** */

    public static byte[] makePogReorderPacket(final Map changes)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POG_REORDER);
            dos.writeInt(changes.size());
            for (final Iterator iterator = changes.entrySet().iterator(); iterator.hasNext();)
            {
                final Map.Entry entry = (Map.Entry)iterator.next();
                final Integer id = (Integer)entry.getKey();
                final Long order = (Long)entry.getValue();
                dos.writeInt(id.intValue());
                dos.writeLong(order.longValue());
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePogSizePacket(final int id, final float size)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POG_SIZE);
            dos.writeInt(id);
            dos.writeFloat(size);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** MOVEPOG PACKET *********************************** */

    public static byte[] makePointPacket(final int plrIdx, final int x, final int y, final boolean bPointing)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POINT); // type
            dos.writeInt(plrIdx);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeBoolean(bPointing);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makePrivateTextPacket(final String fromName, final String toName, final String text)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PRIVATE_TEXT); // type
            dos.writeUTF(fromName);
            dos.writeUTF(toName);
            dos.writeUTF(text);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* ********************* RERECEIVE CARDS PACKET *********************************** */
    public static byte[] makeReceiveCardsPacket(final DeckData.Card cards[])
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_DECK_RECEIVE_CARDS); // packet type
            dos.writeInt(cards.length); // how many cards

            // and now the cards
            for (int i = 0; i < cards.length; i++)
            {
                cards[i].write(dos);
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeRecenterPacket(final int x, final int y, final int zoom)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_RECENTER); // type
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(zoom);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeRedoPacket(final int stateID)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_REDO); // type
            dos.writeInt(stateID); // state ID

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeRejectPacket(final int reason)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_REJECT); // type
            dos.writeInt(reason); // type

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** POINT PACKET *********************************** */

    public static byte[] makeRemovePogsPacket(final int ids[])
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_REMOVEPOGS); // type

            // the number of pogs to be removed is first
            dos.writeInt(ids.length);

            // then the IDs of the pogs.
            for (int i = 0; i < ids.length; i++)
            {
                dos.writeInt(ids[i]);
            }

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* ********************* REQUEST CARDS PACKET *********************************** */
    public static byte[] makeRequestCardsPacket(final String deckName, final int numCards)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_DECK_REQUEST_CARDS); // packet type
            dos.writeUTF(deckName); // the deck
            dos.writeInt(numCards); // how many cards

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** POGDATA PACKET *********************************** */

    /* *********************** ROTATEPOG PACKET ********************************* */
    public static byte[] makeRotatePogPacket(final int id, final double newAngle)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_ROTATEPOG); // type
            dos.writeInt(id);
            dos.writeDouble(newAngle);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** FLIPPOG PACKET ********************************* */
    public static byte[] makeFlipPogPacket(final int id, final int left, final int right)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_FLIPPOG); // type
            dos.writeInt(id);
            dos.writeInt(left);
            dos.writeInt(right);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeTextPacket(final String text)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_TEXT); // type
            dos.writeUTF(text);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static byte[] makeTypingPacket(final String playerName, final boolean typing)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_TYPING); // type
            dos.writeUTF(playerName);
            dos.writeBoolean(typing);

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /* *********************** RECENTER PACKET *********************************** */

    public static byte[] makeUndoPacket(final int stateID)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_UNDO); // type
            dos.writeInt(stateID); // state ID

            return baos.toByteArray();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readAddPogPacket(final Connection conn, final DataInputStream dis)
    {
        try
        {
            final boolean bPublicLayerPog = dis.readBoolean(); // layer. true = public. false = private
            //System.out.println(bPublicLayerPog);
            
            final Pog pog = new Pog(dis);
            if (pog.m_bStillborn)
            {
                // for one reason or another, this pog is corrupt and should
                // be ignored
                return;
            }

            if (pog.isUnknown())
            {
                // we need this image
                requestPogImage(conn, pog);
            }

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.addPogPacketReceived(pog, bPublicLayerPog);
        }
        catch (final IOException ex)
        {
            Log.log(Log.NET, ex);
        }
    }

    /* *********************** UNDO PACKET *********************************** */

    public static void readCastPacket(final DataInputStream dis)
    {
        try
        {
            final int numPlayers = dis.readInt();
            final Player[] players = new Player[numPlayers];
            for (int i = 0; i < numPlayers; i++)
            {
                final String charName = dis.readUTF();
                final String playerName = dis.readUTF();
                final int playerID = dis.readInt();
                players[i] = new Player(playerName, charName, playerID);
                players[i].setHostPlayer(dis.readBoolean());
            }

            // get which index we are
            final int ourIdx = dis.readInt();

            // this is only ever received by players
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.updateCast(players, ourIdx);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readClearDeckPacket(final DataInputStream dis)
    {
        try
        {
            // which deck?
            final String deckName = dis.readUTF();

            // tell the model
            GametableFrame.getGametableFrame().clearDeck(deckName);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** REDO PACKET *********************************** */

    public static void readDeckListPacket(final DataInputStream dis)
    {
        try
        {
            final int numDecks = dis.readInt();
            final String[] deckNames = new String[numDecks];

            for (int i = 0; i < deckNames.length; i++)
            {
                deckNames[i] = dis.readUTF();
            }

            // tell the model
            GametableFrame.getGametableFrame().deckListPacketReceived(deckNames);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readDiscardCardsPacket(final DataInputStream dis)
    {
        try
        {
            // who is discarding?
            final String playerName = dis.readUTF();

            // how many cards are there?
            final int numCards = dis.readInt();

            // make the array
            final DeckData.Card cards[] = new DeckData.Card[numCards];

            // read in all the cards
            for (int i = 0; i < cards.length; i++)
            {
                cards[i] = DeckData.createBlankCard();
                cards[i].read(dis);
            }

            // tell the model
            GametableFrame.getGametableFrame().doDiscardCards(playerName, cards);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** REJECT PACKET *********************************** */

    public static void readErasePacket(final DataInputStream dis)
    {

        try
        {
            final int authorID = dis.readInt();
            final int stateID = dis.readInt();

            final Rectangle r = new Rectangle();
            r.x = dis.readInt();
            r.y = dis.readInt();
            r.width = dis.readInt();
            r.height = dis.readInt();

            final boolean bColorSpecific = dis.readBoolean();
            final int color = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.erasePacketReceived(r, bColorSpecific, color, authorID, stateID);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readFilePacket(final DataInputStream dis)
    {
        // get the mime type of the file
        try
        {
            // get the mime type
            final String mimeType = dis.readUTF();

            if (mimeType.equals("image/png"))
            {
                // this is a png file
                readPngPacket(dis);
            }
            else if (mimeType.equals("application/x-gametable-grm"))
            {
                // this is a png file
                readGrmPacket(dis);
            }
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** HEX MODE PACKET *********************************** */

    public static void readGridModePacket(final DataInputStream dis)
    {

        try
        {
            final int gridMode = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.gridModePacketReceived(gridMode);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readGrmPacket(final DataInputStream dis)
    {
        try
        {
            // read the length of the png file data
            final int len = dis.readInt();

            // the file itself
            final byte[] grmFile = new byte[len];
            dis.read(grmFile);

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.grmPacketReceived(grmFile);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** FILE PACKET *********************************** */

    public static void readLinesPacket(final DataInputStream dis)
    {
        try
        {
            final int authorID = dis.readInt();
            final int stateID = dis.readInt();
            final int numLines = dis.readInt();
            final LineSegment[] lines = new LineSegment[numLines];
            for (int i = 0; i < numLines; i++)
            {
                lines[i] = new LineSegment(dis);
            }

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.linesPacketReceived(lines, authorID, stateID);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** PNG PACKET *********************************** */

    public static void readLockPogPacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            final boolean newLocked = dis.readBoolean();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.lockPogPacketReceived(id, newLocked);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readLoginCompletePacket(final DataInputStream dis)
    {
        // there's no data in a login_complete packet.

        // tell the model
        final GametableFrame gtFrame = GametableFrame.getGametableFrame();
        gtFrame.loginCompletePacketReceived();
    }

    /* *********************** GRM PACKET *********************************** */

    public static void readMovePogPacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            final int newX = dis.readInt();
            final int newY = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.movePogPacketReceived(id, newX, newY);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readPacket(final Connection conn, final byte[] packet)
    {
        try
        {
            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            final int type = dis.readInt();

            Log.log(Log.NET, "Received: " + getPacketName(type) + ", length = " + packet.length);
            // find the player responsible for this

            PacketSourceState.beginNetPacketProcessing();
            switch (type)
            {
                case PACKET_PLAYER:
                {
                    readPlayerPacket(conn, dis);
                }
                break;

                case PACKET_REJECT:
                {
                    readRejectPacket(dis);
                }
                break;

                case PACKET_CAST:
                {
                    readCastPacket(dis);
                }
                break;

                case PACKET_TEXT:
                {
                    readTextPacket(dis);
                }
                break;

                case PACKET_TYPING:
                {
                    readTypingPacket(dis);
                }
                break;

                case PACKET_LINES:
                {
                    readLinesPacket(dis);
                }
                break;

                case PACKET_ERASE:
                {
                    readErasePacket(dis);
                }
                break;

                case PACKET_ADDPOG:
                {
                    readAddPogPacket(conn, dis);
                }
                break;

                case PACKET_REMOVEPOGS:
                {
                    readRemovePogsPacket(dis);
                }
                break;

                case PACKET_MOVEPOG:
                {
                    readMovePogPacket(dis);
                }
                break;

                case PACKET_ROTATEPOG:
                {
                    readRotatePogPacket(dis);
                }
                break;

                case PACKET_FLIPPOG:
                {
                    readFlipPogPacket(dis);
                }
                break;

                case PACKET_LOCKPOG:
                {
                    readLockPogPacket(dis);
                }
                break;

                case PACKET_POINT:
                {
                    readPointPacket(dis);
                }
                break;

                case PACKET_POGDATA:
                {
                    readPogDataPacket(dis);
                }
                break;

                case PACKET_RECENTER:
                {
                    readRecenterPacket(dis);
                }
                break;

                case PACKET_FILE:
                {
                    readFilePacket(dis);
                }
                break;

                case PACKET_PNGREQUEST:
                {
                    readPngRequestPacket(conn, dis);
                }
                break;

                case PACKET_HEX_MODE:
                {
                    readGridModePacket(dis);
                }
                break;

                case PACKET_LOGIN_COMPLETE:
                {
                    readLoginCompletePacket(dis);
                }
                break;

                case PACKET_PING:
                {
                    readPingPacket(dis);
                }
                break;

                case PACKET_UNDO:
                {
                    readUndoPacket(dis);
                }
                break;

                case PACKET_REDO:
                {
                    readRedoPacket(dis);
                }
                break;

                case PACKET_POG_SIZE:
                {
                    readPogSizePacket(dis);
                }
                break;

                case PACKET_PRIVATE_TEXT:
                {
                    readPrivateTextPacket(dis);
                }
                break;

                case PACKET_POG_REORDER:
                {
                    readPogReorderPacket(dis);
                }
                break;

                case PACKET_DECK_LIST:
                {
                    readDeckListPacket(dis);
                }
                break;

                case PACKET_DECK_REQUEST_CARDS:
                {
                    readRequestCardsPacket(conn, dis);
                }
                break;

                case PACKET_DECK_RECEIVE_CARDS:
                {
                    readReceiveCardsPacket(dis);
                }
                break;

                case PACKET_DECK_DISCARD_CARDS:
                {
                    readDiscardCardsPacket(dis);
                }
                break;

                case PACKET_DECK_CLEAR_DECK:
                {
                    readClearDeckPacket(dis);
                }
                break;

                default:
                {
                    throw new IllegalArgumentException("Unknown packet");
                }
            }
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }

        PacketSourceState.endNetPacketProcessing();
    }

    /* *********************** PNG REQUEST PACKET *********************************** */

    public static void readPingPacket(final DataInputStream dis)
    {
        // there's no data in a login_complete packet.

        // tell the model
        final GametableFrame gtFrame = GametableFrame.getGametableFrame();
        gtFrame.pingPacketReceived();
    }

    public static void readPlayerPacket(final Connection conn, final DataInputStream dis)
    {

        try
        {
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();

            final int commVersion = dis.readInt();
            if (commVersion != GametableFrame.COMM_VERSION)
            {
                // cut them off right there.
                gtFrame.kick(conn, GametableFrame.REJECT_VERSION_MISMATCH);
                return;
            }

            final String password = dis.readUTF();
            final String characterName = dis.readUTF();
            final String playerName = dis.readUTF();
            final Player newPlayer = new Player(playerName, characterName, -1);
            newPlayer.setHostPlayer(dis.readBoolean());

            // this is only ever received by the host
            gtFrame.playerJoined(conn, newPlayer, password);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** TEXT PACKET *********************************** */

    public static void readPngPacket(final DataInputStream dis)
    {
        try
        {
            // the file name
            final String filename = UtilityFunctions.getLocalPath(dis.readUTF());

            // read the length of the png file data
            final int len = dis.readInt();

            // the file itself
            final byte[] pngFile = new byte[len];
            dis.read(pngFile);

            // validate PNG file
            if (!UtilityFunctions.isPngData(pngFile))
            {
                GametableFrame.getGametableFrame().logAlertMessage(
                    "Illegal pog data: \"" + filename + "\", aborting transfer.");
                return;
            }

            // validate file location
            final File here = new File("").getAbsoluteFile();
            File target = new File(filename).getAbsoluteFile();
            if (!UtilityFunctions.isAncestorFile(here, target))
            {
                GametableFrame.getGametableFrame().logAlertMessage("Malicious pog path? \"" + filename + "\"");
                final String temp = filename.toLowerCase();
                if (temp.contains("underlay"))
                {
                    target = new File("underlays" + UtilityFunctions.LOCAL_SEPARATOR + target.getName());
                }
                else if (temp.contains("pog"))
                {
                    target = new File("pogs" + UtilityFunctions.LOCAL_SEPARATOR + target.getName());
                }
                else
                {
                    GametableFrame.getGametableFrame().logAlertMessage(
                        "Illegal pog path: \"" + filename + "\", aborting transfer.");
                    return;
                }
            }

            final File parentDir = target.getParentFile();
            if (!parentDir.exists())
            {
                parentDir.mkdirs();
            }

            // now save out the png file
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(target));
            os.write(pngFile);
            os.flush();
            os.close();

            final PogType pogType = GametableFrame.getGametableFrame().getPogLibrary().getPog(filename);
            pogType.load();

            // tell the pog panels to check for the new image
            GametableFrame.getGametableFrame().refreshPogList();

            // Ok, now send the file out to any previously unfulfilled requests.
            final File providedFile = new File(filename).getCanonicalFile();
            final Iterator iterator = g_unfulfilledRequests.keySet().iterator();
            byte[] packet = null;
            while (iterator.hasNext())
            {
                final String requestedFilename = (String)iterator.next();
                final Set connections = (Set)g_unfulfilledRequests.get(requestedFilename);
                if (connections.isEmpty())
                {
                    iterator.remove();
                    continue;
                }

                final File requestedFile = new File(requestedFilename).getCanonicalFile();
                if (requestedFile.equals(providedFile))
                {
                    if (packet == null)
                    {
                        packet = makePngPacket(filename);
                        if (packet == null)
                        {
                            // Still can't make packet
                            // TODO: echo failure message to peoples?
                            break;
                        }
                    }

                    // send to everyone asking for this file
                    final Iterator connectionIterator = connections.iterator();
                    while (connectionIterator.hasNext())
                    {
                        final Connection connection = (Connection)connectionIterator.next();
                        connection.sendPacket(packet);
                    }
                }
            }
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readPngRequestPacket(final Connection conn, final DataInputStream dis)
    {
        try
        {
            // someone wants a png file from us.
            final String filename = UtilityFunctions.getLocalPath(dis.readUTF());

            // make a png packet and send it back
            final byte[] packet = makePngPacket(filename);
            if (packet != null)
            {
                conn.sendPacket(packet);
            }
            else
            {
                addUnfulfilledRequest(filename, conn);
            }
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** PING PACKET *********************************** */

    public static void readPogDataPacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            String name = null;
            if (dis.readBoolean())
            {
                name = dis.readUTF();
            }

            final Set toDelete = new HashSet();
            final int numToDelete = dis.readInt();
            for (int i = 0; i < numToDelete; ++i)
            {
                toDelete.add(dis.readUTF());
            }
            final Map toAdd = new HashMap();
            final int numToAdd = dis.readInt();
            for (int i = 0; i < numToAdd; ++i)
            {
                final String key = dis.readUTF();
                final String value = dis.readUTF();
                toAdd.put(key, value);
            }

            // tell the model
            GametableFrame.getGametableFrame().pogDataPacketReceived(id, name, toAdd, toDelete);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readPogReorderPacket(final DataInputStream dis)
    {
        try
        {
            final int numChanges = dis.readInt();
            final Map changes = new HashMap();
            for (int i = 0; i < numChanges; ++i)
            {
                final int id = dis.readInt();
                final long order = dis.readLong();
                changes.put(new Integer(id), new Long(order));
            }

            // tell the model
            GametableFrame.getGametableFrame().pogReorderPacketReceived(changes);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** POG_SIZE PACKET *********************************** */

    public static void readPogSizePacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            final float size = dis.readFloat();

            // tell the model
            GametableFrame.getGametableFrame().pogSizePacketReceived(id, size);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readPointPacket(final DataInputStream dis)
    {

        try
        {
            final int plrIdx = dis.readInt();
            final int x = dis.readInt();
            final int y = dis.readInt();
            final boolean bPointing = dis.readBoolean();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.pointPacketReceived(plrIdx, x, y, bPointing);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readPrivateTextPacket(final DataInputStream dis)
    {
        try
        {
            final String fromName = dis.readUTF();
            final String toName = dis.readUTF();
            final String text = dis.readUTF();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.privateTextPacketReceived(fromName, toName, text);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readReceiveCardsPacket(final DataInputStream dis)
    {
        try
        {
            // how many cards are there?
            final int numCards = dis.readInt();

            // make the array
            final DeckData.Card cards[] = new DeckData.Card[numCards];

            // read in all the cards
            for (int i = 0; i < cards.length; i++)
            {
                cards[i] = DeckData.createBlankCard();
                cards[i].read(dis);
            }

            // tell the model
            GametableFrame.getGametableFrame().receiveCards(cards);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRecenterPacket(final DataInputStream dis)
    {
        try
        {
            final int x = dis.readInt();
            final int y = dis.readInt();
            final int zoom = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.recenterPacketReceived(x, y, zoom);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRedoPacket(final DataInputStream dis)
    {
        try
        {
            final int stateID = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.redoPacketReceived(stateID);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRejectPacket(final DataInputStream dis)
    {

        try
        {
            final int reason = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.rejectPacketReceived(reason);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRemovePogsPacket(final DataInputStream dis)
    {
        try
        {
            // the number of pogs to be removed is first
            final int ids[] = new int[dis.readInt()];

            // then the IDs of the pogs.
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = dis.readInt();
            }

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.removePogsPacketReceived(ids);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRequestCardsPacket(final Connection conn, final DataInputStream dis)
    {
        try
        {
            // note the deck we're after
            final String deckName = dis.readUTF();

            // note how many cards have been requested
            final int numCards = dis.readInt();

            // tell the model
            GametableFrame.getGametableFrame().requestCardsPacketReceived(conn, deckName, numCards);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readRotatePogPacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            final double newAngle = dis.readDouble();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.rotatePogPacketReceived(id, newAngle);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readFlipPogPacket(final DataInputStream dis)
    {
        try
        {
            final int id = dis.readInt();
            final int flipH = dis.readInt();
            final int flipV = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.flipPogPacketReceived(id, flipH, flipV);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readTextPacket(final DataInputStream dis)
    {

        try
        {
            final String text = dis.readUTF();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.textPacketReceived(text);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void readTypingPacket(final DataInputStream dis)
    {

        try
        {
            final String playerName = dis.readUTF();
            final boolean typing = dis.readBoolean();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.typingPacketReceived(playerName, typing);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** POG_SIZE PACKET *********************************** */

    public static void readUndoPacket(final DataInputStream dis)
    {
        try
        {
            final int stateID = dis.readInt();

            // tell the model
            final GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.undoPacketReceived(stateID);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public static void requestPogImage(final Connection conn, final Pog pog)
    {
        final String desiredFile = pog.getFilename();

        if (g_requestedFiles.contains(desiredFile))
        {
            return;
        }

        // add it to the list of pogs that need art
        g_requestedFiles.add(desiredFile);

        // there are no pending requests for this file. Send one
        // if this somehow came from a null connection, return
        if (conn == null)
        {
            return;
        }

        conn.sendPacket(makePngRequestPacket(desiredFile));
    }

    // --- Constructors ----------------------------------------------------------------------------------------------

    // prevent instantiation
    private PacketManager()
    {
        throw new RuntimeException("PacketManager should not be instantiated!");
    }

}
