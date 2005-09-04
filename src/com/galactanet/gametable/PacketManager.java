

package com.galactanet.gametable;

import java.awt.Rectangle;
import java.io.*;
import java.util.Vector;


public class PacketManager
{
    // prevent instantiation
    private PacketManager()
    {
        throw new RuntimeException("PacketManager should not be instantiated!");
    }



    // packet sent by a new joiner as soon as he joins
    public static final int PACKET_PLAYER     = 0;

    // packet sent by the host telling all the players in the game
    public static final int PACKET_CAST       = 1;

    // packet with text to go to the text log
    public static final int PACKET_TEXT       = 2;

    // lines being added
    public static final int PACKET_LINES      = 3;

    // Eraser used
    public static final int PACKET_ERASE      = 4;

    // Pog added
    public static final int PACKET_ADDPOG     = 5;

    // Pog removed
    public static final int PACKET_REMOVEPOGS  = 6;

    // Pog moved
    public static final int PACKET_MOVEPOG    = 7;

    // point state change
    public static final int PACKET_POINT      = 8;

    // pog data change
    public static final int PACKET_POGDATA    = 9;

    // recentering packet
    public static final int PACKET_RECENTER   = 10;

    // join rejected
    public static final int PACKET_REJECT     = 11;

    // png data transfer
    public static final int PACKET_PNG        = 12;

    // request for a png
    public static final int PACKET_PNGREQUEST = 13;

    /**
     * Holding ground for POGs with no images yet.
     */
    public static Vector    g_imagelessPogs   = new Vector();



    public final static void readPacket(Connection conn, byte[] packet)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
            int type = dis.readInt();

            Log.log(Log.NET, "Received " + getPacketName(type));
            // find the player responsible for this
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

                case PACKET_PNG:
                {
                    readPngPacket(dis);
                }
                    break;

                case PACKET_PNGREQUEST:
                {
                    readPngRequestPacket(conn, dis);
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
            Log.log(Log.SYS, ex.toString());
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
            case PACKET_PNG:
                return "PACKET_PNG";
            case PACKET_PNGREQUEST:
                return "PACKET_PNGREQUEST";
            default:
                return "PACKET_UNKNOWN";
        }
    }

    /** *********************** CAST PACKET *********************************** */
    public static byte[] makeCastPacket(Player recipient)
    {
        try
        {
            // create a packet with all the players in it
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_CAST);
            dos.writeInt(gtFrame.m_players.size());
            for (int i = 0; i < gtFrame.m_players.size(); i++)
            {
                Player plr = (Player)gtFrame.m_players.elementAt(i);
                dos.writeUTF(plr.getCharacterName());
                dos.writeUTF(plr.getPlayerName());
                dos.writeBoolean(plr.isHostPlayer());
            }

            // finally, tell the recipient which player he is
            int whichPlayer = gtFrame.m_players.indexOf(recipient);
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
                players[i] = new Player(playerName, charName);
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

    /** *********************** PLAYER PACKET *********************************** */
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
            Player newPlayer = new Player(playerName, characterName);
            newPlayer.setHostPlayer(dis.readBoolean());

            // this is only ever received by the host
            gtFrame.playerJoined(conn, newPlayer, password);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** TEXT PACKET *********************************** */
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

    /** *********************** LINES PACKET *********************************** */
    public static byte[] makeLinesPacket(LineSegment[] lines)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_LINES); // type
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
            int numLines = dis.readInt();
            LineSegment[] lines = new LineSegment[numLines];
            for (int i = 0; i < numLines; i++)
            {
                lines[i] = new LineSegment();
            }
            for (int i = 0; i < numLines; i++)
            {
                lines[i].initFromPacket(dis);
            }
            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.linesPacketReceived(lines);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** ERASE PACKET *********************************** */
    public static byte[] makeErasePacket(Rectangle r, boolean bColorSpecific, int color)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_ERASE); // type
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
            Rectangle r = new Rectangle();

            r.x = dis.readInt();
            r.y = dis.readInt();
            r.width = dis.readInt();
            r.height = dis.readInt();

            boolean bColorSpecific = dis.readBoolean();
            int color = dis.readInt();

            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.erasePacketReceived(r, bColorSpecific, color);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** ADDPOG PACKET *********************************** */
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
            Pog pog = new Pog();
            pog.initFromPacket(dis);

            if (pog.m_bIsUnknownImage)
            {
                // we need this image
                requestPogImage(conn, pog);
            }
            else
            {
                // tell the model
                GametableFrame gtFrame = GametableFrame.getGametableFrame();
                gtFrame.addPogPacketReceived(pog);
            }
        }
        catch (IOException ex)
        {
            Log.log(Log.NET, ex);
        }
    }

    public static void requestPogImage(Connection conn, Pog pog)
    {
        // add it to the list of pogs that need art
        g_imagelessPogs.add(pog);

        String desiredFile = pog.m_fileName;

        // run through the list and see if there's alreay a pending request for that image
        // (We don't check the last entry cause we just added that one.
        // Hence the "g_imagelessPogs.size()-1" in the loop condition.)
        for (int i = 0; i < g_imagelessPogs.size() - 1; i++)
        {
            Pog aPog = (Pog)g_imagelessPogs.elementAt(i);
            if (desiredFile.equals(aPog.m_fileName))
            {
                // we already have a request pending for this file.
                // no need to send another.
                return;
            }
        }

        // there are no pending requests for this file. Send one
        // if this somehow came from a null connection, return
        if (conn == null)
        {
            return;
        }

        conn.sendPacket(makePngRequestPacket(desiredFile));
    }

    /** *********************** REMOVEPOG PACKET *********************************** */
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
            for ( int i=0 ; i<ids.length ; i++ )
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
            for ( int i=0 ; i<ids.length ; i++ )
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

    /** *********************** MOVEPOG PACKET *********************************** */
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

    /** *********************** POINT PACKET *********************************** */
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

    /** *********************** POGDATA PACKET *********************************** */
    public static byte[] makePogDataPacket(int id, String s)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_POGDATA);
            dos.writeInt(id);
            dos.writeUTF(s);

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
            String s = dis.readUTF();
            // tell the model
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            gtFrame.pogDataPacketReceived(id, s);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** RECENTER PACKET *********************************** */
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

    /** *********************** REJECT PACKET *********************************** */
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

    /** *********************** PNG PACKET *********************************** */
    public static byte[] makePngPacket(String filename)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // write the packet type
            dos.writeInt(PACKET_PNG);

            // write the filename
            dos.writeUTF(filename);

            // load the entire png file
            // java makes this a pain in the ass
            DataInputStream infile = new DataInputStream(new FileInputStream(filename));
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream pngFile = new ByteArrayOutputStream();
            while (true)
            {
                int bytesRead = infile.read(buffer);
                if (bytesRead > 0)
                {
                    pngFile.write(buffer, 0, bytesRead);
                }
                else
                {
                    break;
                }
            }
            byte[] pngFileData = pngFile.toByteArray();

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
            // the file name
            String filename = dis.readUTF();

            // read the length of the png file data
            int len = dis.readInt();

            // the file itself
            byte[] pngFile = new byte[len];
            dis.read(pngFile);

            // validate PNG file
            if (!UtilityFunctions.isPngData(pngFile))
            {
                GametableFrame.g_gameTableFrame.addToTextLog("!!! Illegal pog data: \"" + filename
                    + "\", aborting transfer.");
                return;
            }

            // validate file location
            File here = new File("").getAbsoluteFile();
            File target = new File(filename).getAbsoluteFile();
            Log.log(Log.NET, "here: " + here + ", target: " + target + ", ancestor?: "
                + UtilityFunctions.isAncestorFile(here, target));
            if (!UtilityFunctions.isAncestorFile(here, target))
            {
                GametableFrame.g_gameTableFrame.addToTextLog("!!! Malicious pog path? \"" + filename + "\"");
                String temp = filename.toLowerCase();
                if (temp.contains("underlay"))
                {
                    target = new File("underlays/" + target.getName());
                }
                else if (temp.contains("pog"))
                {
                    target = new File("pogs/" + target.getName());
                }
                else
                {
                    GametableFrame.g_gameTableFrame.addToTextLog("!!! Illegal pog path: \"" + filename
                        + "\", aborting transfer.");
                    return;
                }
            }

            // now save out the png file
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(pngFile);

            // finally, run through our pending pogs and see who needed that.
            GametableFrame gtFrame = GametableFrame.getGametableFrame();
            for (int i = 0; i < g_imagelessPogs.size(); i++)
            {
                Pog pog = (Pog)g_imagelessPogs.elementAt(i);
                if (pog.m_fileName.equals(filename))
                {
                    pog.reaquireImages();
                    gtFrame.addPogPacketReceived(pog);
                    g_imagelessPogs.remove(i);
                    i--; // keep from skipping over stuff
                }
            }

            // tell the pog panels to check for the new image
            GametableFrame.g_gameTableFrame.m_pogsArea.reaquirePogs();
            GametableFrame.g_gameTableFrame.m_underlaysArea.reaquirePogs();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** PNG REQUEST PACKET *********************************** */
    public static byte[] makePngRequestPacket(String filename)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(PACKET_PNGREQUEST); // type
            dos.writeUTF(filename);

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
            String filename = dis.readUTF();

            // make a png packet and send it back
            byte[] packet = makePngPacket(filename);
            if (packet != null)
            {
                conn.sendPacket(packet);
            }
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    /** *********************** BLANK PACKET *********************************** */
    /*
     * public static byte[] makeBlankPacket(Player plr) { try { ByteArrayOutputStream baos = new
     * ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos); dos.writeInt(-1); //
     * type return baos.toByteArray(); } catch (IOException ex) { return null; } } public static
     * void readBlankPacket(DataInputStream dis) { try { // tell the model GametableFrame gtFrame =
     * GametableFrame.getGametableFrame(); } catch (IOException ex) { } }
     */

}
