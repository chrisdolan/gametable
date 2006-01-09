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
 * @author sephalon
 */
public class PacketManager
{
    // --- Constants -------------------------------------------------------------------------------------------------

    // packet sent by a new joiner as soon as he joins
    public static final int PACKET_PLAYER         = 0;

    // packet sent by the host telling all the players in the game
    public static final int PACKET_CAST           = 1;

    // packet with text to go to the text log
    public static final int PACKET_TEXT           = 2;

    // lines being added
    public static final int PACKET_LINES          = 3;

    // Eraser used
    public static final int PACKET_ERASE          = 4;

    // Pog added
    public static final int PACKET_ADDPOG         = 5;

    // Pog removed
    public static final int PACKET_REMOVEPOGS     = 6;

    // Pog moved
    public static final int PACKET_MOVEPOG        = 7;

    // point state change
    public static final int PACKET_POINT          = 8;

    // pog data change
    public static final int PACKET_POGDATA        = 9;

    // recentering packet
    public static final int PACKET_RECENTER       = 10;

    // join rejected
    public static final int PACKET_REJECT         = 11;

    // png data transfer
    public static final int PACKET_FILE           = 12;

    // request for a png
    public static final int PACKET_PNGREQUEST     = 13;

    // notification of a hex mode / grid mode change
    public static final int PACKET_HEX_MODE       = 14;

    // notification that the host is done sending you the inital packets
    // you get when you log in
    public static final int PACKET_LOGIN_COMPLETE = 15;

    // host sends PING, client sends back PING
    public static final int PACKET_PING           = 16;

    // an undo packet
    public static final int PACKET_UNDO           = 17;

    // a redo packet
    public static final int PACKET_REDO           = 18;


    // --- Static Members --------------------------------------------------------------------------------------------

    /**
     * Set of files already asked for. TODO: Add some kind of timed retry feature.
     */
    private static Set      g_requestedFiles      = new HashSet();

    /**
     * A Map of sets of pending incoming requests that could not be fulfilled.
     */
    private static Map      g_unfulfilledRequests = new HashMap();

    // --- Static Methods --------------------------------------------------------------------------------------------

    private static void addUnfulfilledRequest(String filename, Connection connection)
    {
        Set set = (Set)g_unfulfilledRequests.get(filename);
        if (set == null)
        {
            set = new HashSet();
            g_unfulfilledRequests.put(filename, set);
        }

        set.add(connection);
    }

    public static void readPacket(Connection conn, byte[] packet)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            int type = dis.readInt();

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

                default:
                {
                    throw new IllegalArgumentException("Unknown packet");
                }
            }
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }

        PacketSourceState.endNetPacketProcessing();
    }

    public static String getPacketName(byte[] packet)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            return getPacketName(dis.readInt());
        }
        catch (IOException ioe)
        {
            return "ERROR";
        }
    }

    public static String getPacketName(int type)
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
            default:
                return "PACKET_UNKNOWN";
        }
    }

    /* *********************** CAST PACKET *********************************** */

    public static byte[] makeCastPacket(Player recipient)
    {
        try
        {
            // create a packet with all the players in it
            GametableFrame frame = GametableFrame.getGametableFrame();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_CAST);

            List players = frame.getPlayers();
            dos.writeInt(players.size());
            for (int i = 0; i < players.size(); i++)
            {
                Player player = (Player)players.get(i);
                dos.writeUTF(player.getCharacterName());
                dos.writeUTF(player.getPlayerName());
                dos.writeInt(player.getId());
                dos.writeBoolean(player.isHostPlayer());
            }

            // finally, tell the recipient which player he is
            int whichPlayer = frame.getPlayerIndex(recipient);
            dos.writeInt(whichPlayer);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readCastPacket(DataInputStream dis)
    {
        try
        {
            int numPlayers = dis.readInt();
            Player[] players = new Player[numPlayers];
            for (int i = 0; i < numPlayers; i++)
            {
                String charName = dis.readUTF();
                String playerName = dis.readUTF();
                int playerID = dis.readInt();
                players[i] = new Player(playerName, charName, playerID);
                players[i].setHostPlayer(dis.readBoolean());
            }

            // get which index we are
            int ourIdx = dis.readInt();

            // this is only ever received by players
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.updateCast(players, ourIdx);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** PLAYER PACKET *********************************** */

    public static byte[] makePlayerPacket(Player plr, String password)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PLAYER);
            dos.writeInt(GametableFrame.COMM_VERSION);
            dos.writeUTF(password);
            dos.writeUTF(plr.getCharacterName());
            dos.writeUTF(plr.getPlayerName());
            dos.writeBoolean(plr.isHostPlayer());

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPlayerPacket(Connection conn, DataInputStream dis)
    {

        try
        {
            GametableFrame gtFrame = GametableFrame.getGametableFrame();

            int commVersion = dis.readInt();
            if (commVersion != GametableFrame.COMM_VERSION)
            {
                // cut them off right there.
                gtFrame.kick(conn, GametableFrame.REJECT_VERSION_MISMATCH);
                return;
            }

            String password = dis.readUTF();
            String characterName = dis.readUTF();
            String playerName = dis.readUTF();
            Player newPlayer = new Player(playerName, characterName, -1);
            newPlayer.setHostPlayer(dis.readBoolean());

            // this is only ever received by the host
            gtFrame.playerJoined(conn, newPlayer, password);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** TEXT PACKET *********************************** */

    public static byte[] makeTextPacket(String text)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_TEXT); // type
            dos.writeUTF(text);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readTextPacket(DataInputStream dis)
    {

        try
        {
            String text = dis.readUTF();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.textPacketReceived(text);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** LINES PACKET *********************************** */

    public static byte[] makeLinesPacket(LineSegment[] lines, int authorPlayerID, int stateID)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readLinesPacket(DataInputStream dis)
    {
        try
        {
            int authorID = dis.readInt();
            int stateID = dis.readInt();
            int numLines = dis.readInt();
            LineSegment[] lines = new LineSegment[numLines];
            for (int i = 0; i < numLines; i++)
            {
                lines[i] = new LineSegment(dis);
            }

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.linesPacketReceived(lines, authorID, stateID);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** ERASE PACKET *********************************** */

    public static byte[] makeErasePacket(Rectangle r, boolean bColorSpecific, int color, int authorPlayerID, int stateID)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readErasePacket(DataInputStream dis)
    {

        try
        {
            int authorID = dis.readInt();
            int stateID = dis.readInt();

            Rectangle r = new Rectangle();
            r.x = dis.readInt();
            r.y = dis.readInt();
            r.width = dis.readInt();
            r.height = dis.readInt();

            boolean bColorSpecific = dis.readBoolean();
            int color = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.erasePacketReceived(r, bColorSpecific, color, authorID, stateID);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** ADDPOG PACKET *********************************** */

    public static byte[] makeAddPogPacket(Pog pog)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_ADDPOG); // type
            pog.writeToPacket(dos);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readAddPogPacket(Connection conn, DataInputStream dis)
    {
        try
        {
            Pog pog = new Pog(dis);
            if (pog.isUnknown())
            {
                // we need this image
                requestPogImage(conn, pog);
            }

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.addPogPacketReceived(pog);
        }
        catch (IOException ex)
        {
            Log.log(Log.NET, ex);
        }
    }

    public static void requestPogImage(Connection conn, Pog pog)
    {
        String desiredFile = pog.getFilename();

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

    /* *********************** REMOVEPOG PACKET *********************************** */

    public static byte[] makeRemovePogsPacket(int ids[])
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readRemovePogsPacket(DataInputStream dis)
    {
        try
        {
            // the number of pogs to be removed is first
            int ids[] = new int[dis.readInt()];

            // then the IDs of the pogs.
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = dis.readInt();
            }

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.removePogsPacketReceived(ids);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** MOVEPOG PACKET *********************************** */

    public static byte[] makeMovePogPacket(int id, int newX, int newY)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_MOVEPOG); // type
            dos.writeInt(id);
            dos.writeInt(newX);
            dos.writeInt(newY);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readMovePogPacket(DataInputStream dis)
    {
        try
        {
            int id = dis.readInt();
            int newX = dis.readInt();
            int newY = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.movePogPacketReceived(id, newX, newY);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** POINT PACKET *********************************** */

    public static byte[] makePointPacket(int plrIdx, int x, int y, boolean bPointing)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POINT); // type
            dos.writeInt(plrIdx);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeBoolean(bPointing);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPointPacket(DataInputStream dis)
    {

        try
        {
            int plrIdx = dis.readInt();
            int x = dis.readInt();
            int y = dis.readInt();
            boolean bPointing = dis.readBoolean();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.pointPacketReceived(plrIdx, x, y, bPointing);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** POGDATA PACKET *********************************** */

    public static byte[] makePogDataPacket(int id, String s)
    {
        return makePogDataPacket(id, s, null, null);
    }

    public static byte[] makePogDataPacket(int id, String s, Map toAdd, Set toDelete)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
                for (Iterator iterator = toDelete.iterator(); iterator.hasNext();)
                {
                    String key = (String)iterator.next();
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
                for (Iterator iterator = toAdd.entrySet().iterator(); iterator.hasNext();)
                {
                    Map.Entry entry = (Map.Entry)iterator.next();
                    dos.writeUTF((String)entry.getKey());
                    dos.writeUTF((String)entry.getValue());
                }
            }

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPogDataPacket(DataInputStream dis)
    {
        try
        {
            int id = dis.readInt();
            String name = null;
            if (dis.readBoolean())
            {
                name = dis.readUTF();
            }

            Set toDelete = new HashSet();
            int numToDelete = dis.readInt();
            for (int i = 0; i < numToDelete; ++i)
            {
                toDelete.add(dis.readUTF());
            }
            Map toAdd = new HashMap();
            int numToAdd = dis.readInt();
            for (int i = 0; i < numToAdd; ++i)
            {
                String key = dis.readUTF();
                String value = dis.readUTF();
                toAdd.put(key, value);
            }

            // tell the model
            GametableFrame.getGametableFrame().pogDataPacketReceived(id, name, toAdd, toDelete);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }
    

    /* *********************** RECENTER PACKET *********************************** */

    public static byte[] makeRecenterPacket(int x, int y, int zoom)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_RECENTER); // type
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(zoom);

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readRecenterPacket(DataInputStream dis)
    {
        try
        {
            int x = dis.readInt();
            int y = dis.readInt();
            int zoom = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.recenterPacketReceived(x, y, zoom);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** UNDO PACKET *********************************** */

    public static byte[] makeUndoPacket(int stateID)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_UNDO); // type
            dos.writeInt(stateID); // state ID

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readUndoPacket(DataInputStream dis)
    {
        try
        {
            int stateID = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.undoPacketReceived(stateID);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** REDO PACKET *********************************** */

    public static byte[] makeRedoPacket(int stateID)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_REDO); // type
            dos.writeInt(stateID); // state ID

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readRedoPacket(DataInputStream dis)
    {
        try
        {
            int stateID = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.redoPacketReceived(stateID);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** REJECT PACKET *********************************** */

    public static byte[] makeRejectPacket(int reason)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_REJECT); // type
            dos.writeInt(reason); // type

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readRejectPacket(DataInputStream dis)
    {

        try
        {
            int reason = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.rejectPacketReceived(reason);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** HEX MODE PACKET *********************************** */

    public static byte[] makeGridModePacket(int hexMode)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_HEX_MODE); // type
            dos.writeInt(hexMode); // type

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readGridModePacket(DataInputStream dis)
    {

        try
        {
            int gridMode = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.gridModePacketReceived(gridMode);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** FILE PACKET *********************************** */

    public static void readFilePacket(DataInputStream dis)
    {
        // get the mime type of the file
        try
        {
            // get the mime type
            String mimeType = dis.readUTF();

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** PNG PACKET *********************************** */

    public static byte[] makePngPacket(String filename)
    {
        // load the entire png file
        byte[] pngFileData = UtilityFunctions.loadFileToArray(filename);

        if (pngFileData == null)
        {
            return null;
        }

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPngPacket(DataInputStream dis)
    {
        try
        {
            // fire up the spinner
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.m_progressSpinner.activate(gtFrame);

            // the file name
            String filename = UtilityFunctions.getLocalPath(dis.readUTF());

            // read the length of the png file data
            int len = dis.readInt();

            // the file itself
            byte[] pngFile = new byte[len];
            dis.read(pngFile);

            // validate PNG file
            if (!UtilityFunctions.isPngData(pngFile))
            {
                GametableFrame.getGametableFrame().logAlertMessage(
                    "Illegal pog data: \"" + filename + "\", aborting transfer.");
                return;
            }

            // validate file location
            File here = new File("").getAbsoluteFile();
            File target = new File(filename).getAbsoluteFile();
            if (!UtilityFunctions.isAncestorFile(here, target))
            {
                GametableFrame.getGametableFrame().logAlertMessage("Malicious pog path? \"" + filename + "\"");
                String temp = filename.toLowerCase();
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

            File parentDir = target.getParentFile();
            if (!parentDir.exists())
            {
                parentDir.mkdirs();
            }

            // now save out the png file
            OutputStream os = new BufferedOutputStream(new FileOutputStream(target));
            os.write(pngFile);
            os.flush();
            os.close();

            PogType pogType = GametableFrame.getGametableFrame().getPogLibrary().getPog(filename);
            pogType.load();

            // if we're done with imageless pogs, shut off the progress spinner
            gtFrame.m_progressSpinner.deactivate();

            // tell the pog panels to check for the new image
            GametableFrame.getGametableFrame().refreshPogList();

            // Ok, now send the file out to any previously unfulfilled requests.
            File providedFile = new File(filename).getCanonicalFile();
            Iterator iterator = g_unfulfilledRequests.keySet().iterator();
            byte[] packet = null;
            while (iterator.hasNext())
            {
                String requestedFilename = (String)iterator.next();
                Set connections = (Set)g_unfulfilledRequests.get(requestedFilename);
                if (connections.isEmpty())
                {
                    iterator.remove();
                    continue;
                }

                File requestedFile = new File(requestedFilename).getCanonicalFile();
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
                    Iterator connectionIterator = connections.iterator();
                    while (connectionIterator.hasNext())
                    {
                        Connection connection = (Connection)connectionIterator.next();
                        connection.sendPacket(packet);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** GRM PACKET *********************************** */

    public static byte[] makeGrmPacket(byte[] grmData)
    {
        // grmData will be the contents of the file
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

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
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readGrmPacket(DataInputStream dis)
    {
        try
        {
            // read the length of the png file data
            int len = dis.readInt();

            // the file itself
            byte[] grmFile = new byte[len];
            dis.read(grmFile);

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.grmPacketReceived(grmFile);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** PNG REQUEST PACKET *********************************** */

    public static byte[] makePngRequestPacket(String filename)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PNGREQUEST); // type
            dos.writeUTF(UtilityFunctions.getUniversalPath(filename));

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPngRequestPacket(Connection conn, DataInputStream dis)
    {
        try
        {
            // someone wants a png file from us.
            String filename = UtilityFunctions.getLocalPath(dis.readUTF());

            // make a png packet and send it back
            byte[] packet = makePngPacket(filename);
            if (packet != null)
            {
                conn.sendPacket(packet);
            }
            else
            {
                addUnfulfilledRequest(filename, conn);
            }
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /* *********************** TEXT PACKET *********************************** */

    public static byte[] makeLoginCompletePacket()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_LOGIN_COMPLETE); // type
            // there's actually no additional data. Just the info that the login is complete

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readLoginCompletePacket(DataInputStream dis)
    {
        // there's no data in a login_complete packet.

        // tell the model
        GametableFrame gtFrame = GametableFrame.getGametableFrame();
        gtFrame.loginCompletePacketReceived();
    }

    /* *********************** PING PACKET *********************************** */

    public static byte[] makePingPacket()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PING); // type
            // there's actually no additional data. Just the info that the login is complete

            return baos.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static void readPingPacket(DataInputStream dis)
    {
        // there's no data in a login_complete packet.

        // tell the model
        GametableFrame gtFrame = GametableFrame.getGametableFrame();
        gtFrame.pingPacketReceived();
    }

    // --- Constructors ----------------------------------------------------------------------------------------------

    // prevent instantiation
    private PacketManager()
    {
        throw new RuntimeException("PacketManager should not be instantiated!");
    }

}
