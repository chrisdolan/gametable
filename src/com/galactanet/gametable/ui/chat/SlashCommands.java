/*
 * SlashCommands.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.ui.chat;

import java.util.List;

import com.galactanet.gametable.DiceMacro;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.GametableMap;
import com.galactanet.gametable.Player;
import com.galactanet.gametable.Pog;
import com.galactanet.gametable.util.*;


public class SlashCommands
{
    /** *************************************************************************************************************
     * Parses /roll, /proll
     * @param words
     * @param text
     */
    public static void slash_roll(final String[] words, final String text)
    {
        // req. 1 param
        if (words.length < 2)        {
            logSystemMessage("" + words[0] + " usage: " + words[0] + " &lt;Dice Roll in standard format&gt;");
            logSystemMessage("or: " + words[0]
                + " &lt;Macro Name&gt; [&lt;+/-&gt; &lt;Macro Name or Dice Roll&gt;]...");
            logSystemMessage("Examples:");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " 2d6 + 3d4 + 8");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " My Damage + d4");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " d20 + My Damage + My Damage Bonus");
            logSystemMessage("The /roll & /proll commands will not combine multi-dice Macros by adding or subracting.");
            logSystemMessage("By doing so you will only add the last dice of the macro, to the first die of the next.");
            logSystemMessage("They will make a mulit-dice command by doing /roll mydice,mydice2 and such.");
            return;
        }

        final String remaining = text.substring((words[0] + " ").length());
        final StringBuffer roll = new StringBuffer();
        int ci = 0;
        char c;
        boolean isLast = false;
        String term;
        for(int i = 0;i < remaining.length();++i) {
            c = remaining.charAt(i);
            isLast = (i == (remaining.length() - 1));
            if ((c == '+') || (c == '-') || (c == ',') || isLast) {
                if(isLast) term = remaining.substring(ci);
                else term = remaining.substring(ci,i);
                if(term.length() > 0) {
                    final DiceMacro macro = GametableFrame.getGametableFrame().findMacro(term);
                    if(macro != null) roll.append(macro.getMacro());  
                    else roll.append(term);  // No Macro assume its a normal die term. And let the dicemacro figure it out.
                }
                if(!isLast) {
                    roll.append(c);
                }
                ci = i + 1;
            }
        }

        final DiceMacro rmacro = new DiceMacro(roll.toString(),remaining, null); 
        if(rmacro.isInitialized()) {
            if (words[0].equals("/r") || words[0].equals("/roll"))            
                rmacro.doMacro(false);
            else    
                rmacro.doMacro(true);
        } else {
            logMechanics("<b><font color=\"#880000\">Error in Macro String.</font></b>");
        }
    }
    
    /** *************************************************************************************************************
     * Parses /macro
     * @param words
     * @param text
     */
    public static void slash_macro(final String words[], final String text) {
              // macro command. this requires at least 2 parameters
            if (words.length < 3)
            {
                // tell them the usage and bail
                logSystemMessage("/macro usage: /macro &lt;macroName&gt; &lt;dice roll in standard format&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/macro Attack d20+8");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/macro SneakDmg d4 + 2 + 4d6");
                logSystemMessage("Note: Macros will replace existing macros with the same name.");
                return;
            }

            // the second word is the name
            final String name = words[1];

            // all subsequent "words" are the die roll macro
            GametableFrame.getGametableFrame().addMacro(name, text.substring("/macro ".length() + name.length() + 1), null);  
    }
    /** *************************************************************************************************************
     * Parses /macrodelete
     * @param words
     */
    public static void slash_macrodelete(final String words[])  {
        // req. 1 param
        if (words.length < 2)
        {
            logSystemMessage(words[0] + " usage: " + words[0] + " &lt;macroName&gt;");
            return;
        }

        // find and kill this macro
        GametableFrame.getGametableFrame().removeMacro(words[1]);
    } 
    
    /** *************************************************************************************************************
     * Parses /who
     */
    public static void slash_who()  {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("<b><u>Who's connected</u></b><br>");
        for (int i = 0, size = GametableFrame.getGametableFrame().getPlayers().size(); i < size; ++i)
        {
            final Player player = (Player)GametableFrame.getGametableFrame().getPlayers().get(i);
            buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            buffer.append(UtilityFunctions.emitUserLink(player.getCharacterName(), player.toString()));
            buffer.append("<br>");
        }
        buffer.append("<b>");
        buffer.append(GametableFrame.getGametableFrame().getPlayers().size());
        buffer.append(" player");
        buffer.append((GametableFrame.getGametableFrame().getPlayers().size() > 1 ? "s" : ""));
        buffer.append("</b>");
        logSystemMessage(buffer.toString());
    }
    
    /** *************************************************************************************************************
     * Parses /poglist
     * @param words
     */
    public static  void slash_poglist(final String words[]) {
        // macro command. this requires at least 2 parameters
        if (words.length < 2)
        {
            // tell them the usage and bail
            logSystemMessage("/poglist usage: /poglist &lt;attribute name&gt;");
            logSystemMessage("Examples:");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/poglist HP");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/poglist Initiative");
            logSystemMessage("Note: attribute names are case, whitespace, and punctuation-insensitive.");
            return;
        }

        final String name = UtilityFunctions.stitchTogetherWords(words, 1);
        final GametableMap map = GametableFrame.getGametableFrame().getGametableCanvas().getActiveMap();
        final List pogs = map.getPogs();
        final StringBuffer buffer = new StringBuffer();
        buffer.append("<b><u>Pogs with \'" + name + "\' attribute</u></b><br>");
        int tally = 0;
        for (int i = 0, size = pogs.size(); i < size; ++i)
        {
            final Pog pog = (Pog)pogs.get(i);
            final String value = pog.getAttribute(name);
            if ((value != null) && (value.length() > 0))
            {
                String pogText = pog.getText();
                if ((pogText == null) || (pogText.length() == 0))
                {
                    pogText = "&lt;unknown&gt;";
                }

                buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>");
                buffer.append(pogText);
                buffer.append(":</b> ");
                buffer.append(value);
                buffer.append("<br>");
                ++tally;
            }
        }
        buffer.append("<b>" + tally + " pog" + (tally != 1 ? "s" : "") + " found.</b>");
        logSystemMessage(buffer.toString());
    }
    
    /** *************************************************************************************************************
     * Parses /tell, /send
     * @param words
     * @param text
     */
    public static  void slash_tell(final String words[], final String text) 
    {
        // send a private message to another player
        if (words.length < 3)
        {
            // tell them the usage and bail
            logSystemMessage(words[0] + " usage: " + words[0] + " &lt;player name&gt; &lt;message&gt;");
            logSystemMessage("Examples:");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0]
                                                                      + " Dave I am the most awesome programmer on Gametable!");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " Andy No you're not, you suck!");
            return;
        }

        // they have a legitimate /tell or /send
        final String toName = words[1];

        // see if there is a player or character with that name
        // and note the "proper" name for them (which is their player name)
        Player toPlayer = null;
        for (int i = 0; i < GametableFrame.getGametableFrame().getPlayers().size(); i++)
        {
            final Player player = (Player)GametableFrame.getGametableFrame().getPlayers().get(i);
            if (player.hasName(toName))
            {
                toPlayer = player;
                break;
            }
        }

        if (toPlayer == null)
        {
            // nobody by that name is in the session
            logAlertMessage("There is no player or character named \"" + toName + "\" in the session.");
            return;
        }

        // now get the message portion
        // we have to do this with the original text, cause the words[] array
        // will have stripped a lot of whitespace if they had multiple spaces, etc.
        // indexOf(toName) will get us to the start of the player name it's being sent to
        // we then add the length of the name to get past that
        final int start = text.indexOf(toName) + toName.length();
        final String toSend = text.substring(start).trim();

        GametableFrame.getGametableFrame().tell(toPlayer, toSend);
    }
    
    /** *************************************************************************************************************
     * Parses /emote, /me, /em
     * @param words
     * @param text
     */
    public final static String    EMOTE_MESSAGE_FONT       = "<font color=\"#004477\">";
    public final static String    END_EMOTE_MESSAGE_FONT   = "</font>";
    
    public static void slash_emote(final String words[], final String text)
    {
        if (words.length < 2)
        {
            // tell them the usage and bail
            logSystemMessage("/emote usage: /emote &lt;action&gt;");
            logSystemMessage("Examples:");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/emote gets a beer.");
            return;
        }

        // get the portion of the text after the emote command
        final int start = text.indexOf(words[0]) + words[0].length();
        final String emote = text.substring(start).trim();

        // simply post text that's an emote instead of a character action
        final String toPost = EMOTE_MESSAGE_FONT + 
            UtilityFunctions.emitUserLink(GametableFrame.getGametableFrame().getMyPlayer().getCharacterName())
            + " " + emote + END_EMOTE_MESSAGE_FONT;
        postMessage(toPost);
    }
    
    /** *************************************************************************************************************
     * Parses /as
     * @param words
     * @param text
     */
   
    public static void slash_as(final String words[], final String text, final boolean emoteas) {
        if (words.length < 3)
        {
            // tell them the usage and bail
            logSystemMessage("/as usage: /as &lt;name&gt; &lt;text&gt;");
            logSystemMessage("Examples:");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/as Balthazar Prepare to meet your doom!.");
            logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/as Lord_Doom Prepare to meet um... me!");
            logSystemMessage("Note: Underscore characters in the name you specify will be turned into spaces");
            return;
        }
        final StringBuffer speakerName = new StringBuffer(words[1]);

        for (int i = 0; i < speakerName.length(); i++)
        {
            if (speakerName.charAt(i) == '_')
            {
                speakerName.setCharAt(i, ' ');
            }
        }

        // get the portion of the text after the emote command
        final int start = text.indexOf(words[1]) + words[1].length();
        final String toSay = text.substring(start).trim();

        // simply post text that's an emote instead of a character action
        final StringBuffer toPost = new StringBuffer();
        toPost.append(EMOTE_MESSAGE_FONT + speakerName);
        if(!emoteas) toPost.append(":");
        toPost.append(" " + END_EMOTE_MESSAGE_FONT + toSay);
        postMessage(toPost.toString());
    }
  
    /** *************************************************************************************************************
     * PArses /goto
     * @param words
     */
    public static void slash_goto(final String words[])
    {
        if (words.length < 2)
        {
            logSystemMessage(words[0] + " usage: " + words[0] + " &lt;pog name&gt;");
            return;
        }

        final String name = UtilityFunctions.stitchTogetherWords(words, 1);
        final Pog pog = GametableFrame.getGametableFrame().getGametableCanvas().getActiveMap().getPogNamed(name);
        if (pog == null)
        {
            logAlertMessage("Unable to find pog named \"" + name + "\".");
            return;
        }
        GametableFrame.getGametableFrame().getGametableCanvas().scrollToPog(pog);
    }  
    
    
    /** *************************************************************************************************************
     * Parses / commands
     * @param text
     */
    public static void parseSlashCommand(final String text)
    {
        // get the command
        final String[] words = UtilityFunctions.breakIntoWords(text);
        if (words == null)
            return;
        
        if (words[0].equals("/macro"))     
            slash_macro(words,text);
        else if (words[0].equals("/macrodelete") || words[0].equals("/del"))
            slash_macrodelete(words);
        else if (words[0].equals("/who"))
            slash_who();
        else if (words[0].equals("/r") || words[0].equals("/pr") || words[0].equals("/rp") || words[0].equals("/roll") || words[0].equals("/proll"))
            slash_roll(words,text);
        else if (words[0].equals("/poglist")) 
            slash_poglist(words);            
        else if (words[0].equals("/tell") || words[0].equals("/send") || words[0].equals("/t"))
            slash_tell(words,text);
        else if (words[0].equals("/em") || words[0].equals("/me") || words[0].equals("/emote"))
            slash_emote(words,text);
        else if (words[0].equals("/as"))
            slash_as(words,text,false);
        else if (words[0].equals("/emas") || words[0].equals("/ea") || words[0].equals("/emoteas") || words[0].equals("/eas"))
            slash_as(words,text,true);
        else if (words[0].equals("/goto"))
            slash_goto(words);
        else if (words[0].equals("/cl") || words[0].equals("/clearlog"))
            GametableFrame.getGametableFrame().getChatPanel().clearText();
        else if (words[0].equals("/deck"))
            GametableFrame.getGametableFrame().deckCommand(words); // deck commands. there are many 
        else if (words[0].equals("/?") || words[0].equals("//") || words[0].equals("/help")) {
            // list macro commands
            logSystemMessage("<b><u>Slash Commands</u></b><br>"
                + "<b>/as:</b> Display a narrative of a character saying something<br>"
                + "<b>/deck:</b> Various deck actions. type /deck for more details<br>"
                + "<b>/emote:</b> Display an emote<br>" + "<b>/goto:</b> Centers a pog in the map view.<br>"
                + "<b>/help:</b> list all slash commands<br>" + "<b>/macro:</b> macro a die roll<br>"
                + "<b>/macrodelete:</b> deletes an unwanted macro<br>" + "<b>/poglist:</b> lists pogs by attribute<br>"
                + "<b>/proll:</b> roll dice privately<br>" + "<b>/roll:</b> roll dice<br>"
                + "<b>/tell:</b> send a private message to another player<br>"
                + "<b>/who:</b> lists connected players<br>" + "<b>//:</b> list all slash commands");
        }
    }

    private static void logSystemMessage(final String msg) {
        GametableFrame.getGametableFrame().getChatPanel().logSystemMessage(msg);
    }
    private static void logAlertMessage(final String msg) {
        GametableFrame.getGametableFrame().getChatPanel().logAlertMessage(msg);
    }
    private  static void postMessage(final String msg) {
        GametableFrame.getGametableFrame().postMessage(msg);
    }
    private  static void logMechanics(final String msg) {
        GametableFrame.getGametableFrame().getChatPanel().logMechanics(msg);
    }
}
