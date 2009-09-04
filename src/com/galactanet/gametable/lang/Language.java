/*
 * Language.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.lang;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import com.galactanet.gametable.Log;

/**
 * Class for selecting and implementing the chosen language.
 * 
 * @author: Rizban
 */
public class Language
{
    public String LANGUAGE;
    public String CONNECT_FAIL;
    public String CONNECTED;
    public String CONNECTION_LEFT;
    public String CONNECTION_LOST;
    public String CONNECTION_REJECTED;
    public String CONNECTION_NO_DISCONNECT;
    public String CONFIRM_HOST_FAIL;
    public String DISCONNECTED;
    public String HOST_ERROR_HOST;
    public String HOST_ERROR_JOIN;
    public String HOST_ERROR_FAIL;
    public String HOSTING;
    public String IP_CHECK;
    public String IP_CHECK2;
    public String JOIN_BAD_PASS;
    public String JOIN_BAD_VERSION;
    public String JOIN_ERROR_HOST;
    public String JOIN_ERROR_JOIN;
    public String JOINED;
    public String PLAYER;
    public String PLAYERS;
    public String PLAYER_JOINED;
    public String UNKNOWN_STATE;
    public String FILE;
    public String MAP_OPEN;
    public String MAP_OPEN_BAD_VERSION;
    public String MAP_OPEN_CONFIRM;
    public String MAP_OPEN_DONE;
    public String MAP_OPEN_WARN;
    public String MAP_SAVE;
    public String MAP_SAVE_AS;
    public String OPEN;
    public String POG_SCAN;
    public String QUIT;
    public String SAVE_AS;
    public String EDIT;
    public String REDO;
    public String UNDO;
    public String DISCONNECT;
    public String HOST;
    public String JOIN;
    public String LIST_PLAYERS;
    public String NETWORK;
    public String MAP;
    public String MAP_BG_CHANGE;
    public String MAP_CENTER;
    public String MAP_CENTER_DONE;
    public String MAP_CENTER_PLAYERS;
    public String MAP_CENTER_PLAYERS_WARN;
    public String MAP_EXPORT;
    public String MAP_GRID_MODE;
    public String MAP_LOCK_ALL;
    public String MAP_LOCK_ALL_DONE;
    public String MAP_LOCK_ALL_DONE2;
    public String MAP_UNLOCK_ALL;
    public String MAP_UNLOCK_ALL_DONE;
    public String MAP_UNLOCK_ALL_DONE2;
    public String MAP_PRIVATE_EDIT;
    public String MAP_SAVE_IMG;
    public String MAP_SAVE_EXISTS;
    public String MAP_SAVE_FILE_FAIL;
    public String MAP_SAVE_IMG_FAIL;
    public String MAP_SAVE_NO_ACCESS;
    public String MAP_SAVE_OVERWRITE;
    public String POG_LOAD;
    public String MAP_CLEAR;
    public String MAP_CLEAR_WARNING;
    public String MAP_GRID_CHANGE;
    public String MAP_GRID_HEX;
    public String MAP_GRID_NONE;
    public String MAP_GRID_SQUARE;
    public String DICE;
    public String MACRO_ADD;
    public String MACRO_DELETE;
    public String MACRO_DELETE_INFO;
    public String MACRO_LOAD;
    public String MACRO_LOAD_CONFIRM;
    public String MACRO_LOAD_DONE;
    public String MACRO_LOAD_WARN;
    public String MACRO_SAVE;
    public String MACRO_SAVE_AS;
    public String MACRO_SAVE_DONE;
    public String WINDOW;
    public String CHAT_WINDOW_DOCK;
    public String MECHANICS_WINDOW_USE;
    public String POG_WINDOW_DOCK;
    public String HELP;
    public String ABOUT;
    public String ABOUT2;
    public String ABOUT3;
    public String VERSION;
    public String SHOW_POG_NAMES;
    public String TOOLBAR_FAIL;
    public String DICE_MACROS;
    public String MACRO_ERROR;
    public String MACRO_EXISTS_1;
    public String MACRO_EXISTS_2;
    public String MACRO_EXISTS_3;
    public String MACRO_REPLACE;
    public String POG_LIBRARY;
    public String POG_ACTIVE;
    public String AND;
    public String TELL;
    public String TELL_SELF;
    public String IS_TYPING;
    public String ARE_TYPING;
    public String DECK;
    public String DECK_ALREADY_EXISTS;
    public String DECK_CARD_NONE;
    public String DECK_CARDS;
    public String DECK_CARDS_COLLECT_ALL_1;
    public String DECK_CARDS_COLLECT_ALL_2;
    public String DECK_CARDS_INVALID_NUMBER;
    public String DECK_CREATE_SUCCESS_1;
    public String DECK_CREATE_SUCCESS_2;
    public String DECK_DECKS;
    public String DECK_DESTROY;
    public String DECK_DISCARDS;
    public String DECK_DRAW_PLAYER;
    public String DECK_DRAWS;
    public String DECK_DRAWS2;
    public String DECK_DREW;
    public String DECK_ERROR_CREATE;
    public String DECK_ERROR_DODISCARD;
    public String DECK_ERROR_HOST_DECKLIST;
    public String DECK_HAND_EMPTY;
    public String DECK_HAS;
    public String DECK_NO_DECKS;
    public String DECK_NONE;
    public String DECK_NOT_CONNECTED;
    public String DECK_NOT_HOST_CREATE;
    public String DECK_NOT_HOST_DESTROY;
    public String DECK_NOT_HOST_SHUFFLE;
    public String DECK_OUT_OF_CARDS;
    public String DECK_SHUFFLE;
    public String DECK_SHUFFLE_INVALID;
    public String DECK_THERE_ARE;
    public String DECK_YOU_HAVE;

    /**
     * Constructor.
     */
    public Language()
    {
    }
    public Language(final String language)
    {
        setLanguage(language);
    }
    
    public void setLanguage(final String language)
    {
        //final File langFile = new File("./language/" + language + ".txt");
        try
        {
            //FileInputStream is = new FileInputStream(langFile);
            String path = getClass().getPackage().getName().replace(".", "/") + "/" + language + ".txt";
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            if (is == null)
                throw new FileNotFoundException(path);
            try {
                processLineByLine(new BufferedInputStream(is));
            } finally {
                is.close();
            }
        }
        catch (final Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    /** Template method that calls {@link #processLine(String)}.  */
    public final void processLineByLine(final InputStream fFile) throws FileNotFoundException {
        Scanner scanner = new Scanner(fFile);
        try {
            //first use a Scanner to get each line
            while ( scanner.hasNextLine() ){
                processLine( scanner.nextLine() );
            }
        }
        finally {
            //ensure the underlying stream is always closed
            scanner.close();
        }
    }
    
    /** 
    * Overridable method for processing lines in different ways.
    *  
    * <P>This simple default implementation expects simple name-value pairs, separated by an 
    * '=' sign. Examples of valid input : 
    * <tt>height = 167cm</tt>
    * <tt>mass =  65kg</tt>
    * <tt>disposition =  "grumpy"</tt>
    * <tt>this is the name = this is the value</tt>
    */
    protected void processLine(String aLine){
        //use a second Scanner to parse the content of each line 
        String name;
        String value;
        Scanner scanner = new Scanner(aLine);
        scanner.useDelimiter("=");
        if ( scanner.hasNext() ){
            try {
                name  = scanner.next().trim();
                value = scanner.next().trim();
                setConstant(name, value);
            }
            catch (final Exception e) {}
        }
        //(no need for finally here, since String is source)
        scanner.close();
    }

    private void setConstant(final String name, final String value)
    {
        if (name.equals("LANGUAGE")){
            LANGUAGE = value;
        } else if (name.equals("CONNECT_FAIL")){
            CONNECT_FAIL = value;
        } else if (name.equals("CONNECTED")){
            CONNECTED = value;
        } else if (name.equals("CONNECTION_LEFT")){
           CONNECTION_LEFT = value;
        } else if (name.equals("CONNECTION_LOST")){
            CONNECTION_LOST = value;
        } else if (name.equals("CONNECTION_REJECTED")){
            CONNECTION_REJECTED = value;
        } else if (name.equals("CONNECTION_NO_DISCONNECT")){
            CONNECTION_NO_DISCONNECT = value;
        } else if (name.equals("CONFIRM_HOST_FAIL")){
            CONFIRM_HOST_FAIL = value;
        } else if (name.equals("DISCONNECTED")){
            DISCONNECTED = value;
        } else if (name.equals("HOST_ERROR_HOST")){
            HOST_ERROR_HOST = value;
        } else if (name.equals("HOST_ERROR_JOIN")){
            HOST_ERROR_JOIN = value;
        } else if (name.equals("HOST_ERROR_FAIL")){
            HOST_ERROR_FAIL = value;
        } else if (name.equals("HOSTING")){
            HOSTING = value;
        } else if (name.equals("IP_CHECK")){
            IP_CHECK = value;
        } else if (name.equals("IP_CHECK2")){
            IP_CHECK2 = value;
        } else if (name.equals("JOIN_BAD_PASS")){
            JOIN_BAD_PASS = value;
        } else if (name.equals("JOIN_BAD_VERSION")){
            JOIN_BAD_VERSION = value;
        } else if (name.equals("JOIN_ERROR_HOST")){
            JOIN_ERROR_HOST = value;
        } else if (name.equals("JOIN_ERROR_JOIN")){
            JOIN_ERROR_JOIN = value;
        } else if (name.equals("JOINED")){
            JOINED = value;
        } else if (name.equals("PLAYER")){
            PLAYER = value;
        } else if (name.equals("PLAYERS")){
            PLAYERS = value;
        } else if (name.equals("PLAYER_JOINED")){
            PLAYER_JOINED = value;
        } else if (name.equals("UNKNOWN_STATE")){
            UNKNOWN_STATE = value;
        } else if (name.equals("FILE")){
            FILE = value;
        } else if (name.equals("MAP_OPEN")){
            MAP_OPEN = value;
        } else if (name.equals("MAP_OPEN_BAD_VERSION")){
            MAP_OPEN_BAD_VERSION = value;
        } else if (name.equals("MAP_OPEN_CONFIRM")){
            MAP_OPEN_CONFIRM = value;
        } else if (name.equals("MAP_OPEN_DONE")){
            MAP_OPEN_DONE = value;
        } else if (name.equals("MAP_OPEN_WARN")){
            MAP_OPEN_WARN = value;
        } else if (name.equals("MAP_SAVE")){
            MAP_SAVE = value;
        } else if (name.equals("MAP_SAVE_AS")){
            MAP_SAVE_AS = value;
        } else if (name.equals("OPEN")){
            OPEN = value;
        } else if (name.equals("POG_SCAN")){
            POG_SCAN = value;
        } else if (name.equals("QUIT")){
            QUIT = value;
        } else if (name.equals("SAVE_AS")){
            SAVE_AS = value;
        } else if (name.equals("EDIT")){
            EDIT = value;
        } else if (name.equals("REDO")){
            REDO = value;
        } else if (name.equals("UNDO")){
            UNDO = value;
        } else if (name.equals("DISCONNECT")){
            DISCONNECT = value;
        } else if (name.equals("HOST")){
            HOST = value;
        } else if (name.equals("JOIN")){
            JOIN = value;
        } else if (name.equals("LIST_PLAYERS")){
            LIST_PLAYERS = value;
        } else if (name.equals("NETWORK")){
            NETWORK = value;
        } else if (name.equals("MAP")){
            MAP = value;
        } else if (name.equals("MAP_BG_CHANGE")){
            MAP_BG_CHANGE = value;
        } else if (name.equals("MAP_CENTER")){
            MAP_CENTER = value;
        } else if (name.equals("MAP_CENTER_DONE")){
            MAP_CENTER_DONE = value;
        } else if (name.equals("MAP_CENTER_PLAYERS")){
            MAP_CENTER_PLAYERS = value;
        } else if (name.equals("MAP_CENTER_PLAYERS_WARN")){
            MAP_CENTER_PLAYERS_WARN = value;
        } else if (name.equals("MAP_EXPORT")){
            MAP_EXPORT = value;
        } else if (name.equals("MAP_GRID_MODE")){
            MAP_GRID_MODE = value;
        } else if (name.equals("MAP_LOCK_ALL")){
            MAP_LOCK_ALL = value;
        } else if (name.equals("MAP_LOCK_ALL_DONE")){
            MAP_LOCK_ALL_DONE = value;
        } else if (name.equals("MAP_LOCK_ALL_DONE2")){
            MAP_LOCK_ALL_DONE2 = value;
        } else if (name.equals("MAP_UNLOCK_ALL")){
            MAP_UNLOCK_ALL = value;
        } else if (name.equals("MAP_UNLOCK_ALL_DONE")){
            MAP_UNLOCK_ALL_DONE = value;
        } else if (name.equals("MAP_UNLOCK_ALL_DONE2")){
            MAP_UNLOCK_ALL_DONE2 = value;
        } else if (name.equals("MAP_PRIVATE_EDIT")){
            MAP_PRIVATE_EDIT = value;
        } else if (name.equals("MAP_SAVE")){
            MAP_SAVE = value;
        } else if (name.equals("MAP_SAVE_EXISTS")){
            MAP_SAVE_EXISTS = value;
        } else if (name.equals("MAP_SAVE_FILE_FAIL")){
            MAP_SAVE_FILE_FAIL = value;
        } else if (name.equals("MAP_SAVE_IMG_FAIL")){
            MAP_SAVE_IMG_FAIL = value;
        } else if (name.equals("MAP_SAVE_NO_ACCESS")){
            MAP_SAVE_NO_ACCESS = value;
        } else if (name.equals("MAP_SAVE_OVERWRITE")){
            MAP_SAVE_OVERWRITE = value;
        } else if (name.equals("POG_LOAD")){
            POG_LOAD = value;
        } else if (name.equals("MAP_CLEAR")){
            MAP_CLEAR = value;
        } else if (name.equals("MAP_CLEAR_WARNING")){
            MAP_CLEAR_WARNING = value;
        } else if (name.equals("MAP_GRID_CHANGE")){
            MAP_GRID_CHANGE = value;
        } else if (name.equals("MAP_GRID_HEX")){
            MAP_GRID_HEX = value;
        } else if (name.equals("MAP_GRID_NONE")){
            MAP_GRID_NONE = value;
        } else if (name.equals("MAP_GRID_SQUARE")){
            MAP_GRID_SQUARE = value;
        } else if (name.equals("DICE")){
            DICE = value;
        } else if (name.equals("MACRO_ADD")){
            MACRO_ADD = value;
        } else if (name.equals("MACRO_DELETE")){
            MACRO_DELETE = value;
        } else if (name.equals("MACRO_DELETE_INFO")){
            MACRO_DELETE_INFO = value;
        } else if (name.equals("MACRO_LOAD")){
            MACRO_LOAD = value;
        } else if (name.equals("MACRO_LOAD_CONFIRM")){
            MACRO_LOAD_CONFIRM = value;
        } else if (name.equals("MACRO_LOAD_DONE")){
            MACRO_LOAD_DONE = value;
        } else if (name.equals("MACRO_LOAD_WARN")){
            MACRO_LOAD_WARN = value;
        } else if (name.equals("MACRO_SAVE")){
            MACRO_SAVE = value;
        } else if (name.equals("MACRO_SAVE_AS")){
            MACRO_SAVE_AS = value;
        } else if (name.equals("MACRO_SAVE_DONE")){
            MACRO_SAVE_DONE = value;
        } else if (name.equals("WINDOW")){
            WINDOW = value;
        } else if (name.equals("CHAT_WINDOW_DOCK")){
            CHAT_WINDOW_DOCK = value;
        } else if (name.equals("MECHANICS_WINDOW_USE")){
            MECHANICS_WINDOW_USE = value;
        } else if (name.equals("POG_WINDOW_DOCK")){
            POG_WINDOW_DOCK = value;
        } else if (name.equals("HELP")){
            HELP = value;
        } else if (name.equals("ABOUT")){
            ABOUT = value;
        } else if (name.equals("ABOUT2")){
            ABOUT2 = value;
        } else if (name.equals("ABOUT3")){
            ABOUT3 = value;
        } else if (name.equals("VERSION")){
            VERSION = value;
        } else if (name.equals("SHOW_POG_NAMES")){
            SHOW_POG_NAMES = value;
        } else if (name.equals("TOOLBAR_FAIL")){
            TOOLBAR_FAIL = value;
        } else if (name.equals("DICE_MACROS")){
            DICE_MACROS = value;
        } else if (name.equals("MACRO_ERROR")){
            MACRO_ERROR = value;
        } else if (name.equals("MACRO_EXISTS_1")){
            MACRO_EXISTS_1 = value;
        } else if (name.equals("MACRO_EXISTS_2")){
            MACRO_EXISTS_2 = value;
        } else if (name.equals("MACRO_EXISTS_3")){
            MACRO_EXISTS_3 = value;
        } else if (name.equals("MACRO_REPLACE")){
            MACRO_REPLACE = value;
        } else if (name.equals("POG_LIBRARY")){
            POG_LIBRARY = value;
        } else if (name.equals("POG_ACTIVE")){
            POG_ACTIVE = value;
        } else if (name.equals("AND")){
            AND = value;
        } else if (name.equals("TELL")){
            TELL = value;
        } else if (name.equals("TELL_SELF")){
            TELL_SELF = value;
        } else if (name.equals("IS_TYPING")){
            IS_TYPING = value;
        } else if (name.equals("ARE_TYPING")){
            ARE_TYPING = value;
        } else if (name.equals("DECK")){
            DECK = value;
        } else if (name.equals("DECK_ALREADY_EXISTS")){
            DECK_ALREADY_EXISTS = value;
        } else if (name.equals("DECK_CARD_NONE")){
            DECK_CARD_NONE = value;
        } else if (name.equals("DECK_CARDS")){
            DECK_CARDS = value;
        } else if (name.equals("DECK_CARDS_COLLECT_ALL_1")){
            DECK_CARDS_COLLECT_ALL_1 = value;
        } else if (name.equals("DECK_CARDS_COLLECT_ALL_2")){
            DECK_CARDS_COLLECT_ALL_2 = value;
        } else if (name.equals("DECK_CARDS_INVALID_NUMBER")){
            DECK_CARDS_INVALID_NUMBER = value;
        } else if (name.equals("DECK_CREATE_SUCCESS_1")){
            DECK_CREATE_SUCCESS_1 = value;
        } else if (name.equals("DECK_CREATE_SUCCESS_2")){
            DECK_CREATE_SUCCESS_2 = value;
        } else if (name.equals("DECK_DECKS")){
            DECK_DECKS = value;
        } else if (name.equals("DECK_DESTROY")){
            DECK_DESTROY = value;
        } else if (name.equals("DECK_DISCARDS")){
            DECK_DISCARDS = value;
        } else if (name.equals("DECK_DRAW_PLAYER")){
            DECK_DRAW_PLAYER = value;
        } else if (name.equals("DECK_DRAWS")){
            DECK_DRAWS = value;
        } else if (name.equals("DECK_DRAWS2")){
            DECK_DRAWS2 = value;
        } else if (name.equals("DECK_DREW")){
            DECK_DREW = value;
        } else if (name.equals("DECK_ERROR_CREATE")){
            DECK_ERROR_CREATE = value;
        } else if (name.equals("DECK_ERROR_DODISCARD")){
            DECK_ERROR_DODISCARD = value;
        } else if (name.equals("DECK_ERROR_HOST_DECKLIST")){
            DECK_ERROR_HOST_DECKLIST = value;
        } else if (name.equals("DECK_HAND_EMPTY")){
            DECK_HAND_EMPTY = value;
        } else if (name.equals("DECK_HAS")){
            DECK_HAS = value;
        } else if (name.equals("DECK_NO_DECKS")){
            DECK_NO_DECKS = value;
        } else if (name.equals("DECK_NONE")){
            DECK_NONE = value;
        } else if (name.equals("DECK_NOT_CONNECTED")){
            DECK_NOT_CONNECTED = value;
        } else if (name.equals("DECK_NOT_HOST_CREATE")){
            DECK_NOT_HOST_CREATE = value;
        } else if (name.equals("DECK_NOT_HOST_DESTROY")){
            DECK_NOT_HOST_DESTROY = value;
        } else if (name.equals("DECK_NOT_HOST_SHUFFLE")){
            DECK_NOT_HOST_SHUFFLE = value;
        } else if (name.equals("DECK_OUT_OF_CARDS")){
            DECK_OUT_OF_CARDS = value;
        } else if (name.equals("DECK_SHUFFLE")){
            DECK_SHUFFLE = value;
        } else if (name.equals("DECK_SHUFFLE_INVALID")){
            DECK_SHUFFLE_INVALID = value;
        } else if (name.equals("DECK_THERE_ARE")){
            DECK_THERE_ARE = value;
        } else if (name.equals("DECK_YOU_HAVE")){
            DECK_YOU_HAVE = value;
        }
    }
}