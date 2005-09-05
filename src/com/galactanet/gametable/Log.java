/*
 * Log.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Static class providing an interface for logging out to a file or console.
 * 
 * @author iffy
 */
public class Log
{
    // --- Logging Contexts ---

    /**
     * The logging context for general system output.
     */
    public static final int         SYS               = 0;

    /**
     * The logging context for network output.
     */
    public static final int         NET               = 1;

    // --- Constants ---

    private static final String     DIVIDER           = " *************************";
    private static final String     DIVIDER_PREFIX    = "**** ";
    private static final DateFormat DATE_FORMAT       = new SimpleDateFormat("yyyy-MM-dd\' \'HH:mm:ss.SSS");
    private static final Object     G_LOCK            = new Object();

    private static Map              g_loggingContexts = new HashMap();

    /**
     * Static initializer.
     */
    static
    {
        initializeLog(SYS, System.err);
    }



    public static final void initializeLog(int context, String s)
    {
        synchronized (G_LOCK)
        {
            Log oldLog = (Log)g_loggingContexts.get(new Integer(context));
            if (oldLog != null)
            {
                oldLog.close();
            }

            Log log = new Log(context);
            g_loggingContexts.put(new Integer(context), log);
            log.setLogTarget(s);
        }
    }

    public static final void initializeLog(int context, PrintStream ps)
    {
        synchronized (G_LOCK)
        {
            Log oldLog = (Log)g_loggingContexts.get(new Integer(context));
            if (oldLog != null)
            {
                oldLog.close();
            }

            Log log = new Log(context);
            g_loggingContexts.put(new Integer(context), log);
            log.setLogTarget(ps);
        }
    }

    public static final void log(int context, String s)
    {
        synchronized (G_LOCK)
        {
            Log l = (Log)g_loggingContexts.get(new Integer(context));
            if (l != null)
            {
                l.log(s);
            }
        }
    }

    public static final void log(int context, Throwable t)
    {
        synchronized (G_LOCK)
        {
            Log l = (Log)g_loggingContexts.get(new Integer(context));
            if (l != null)
            {
                l.log(t);
            }
        }
    }

    private static final String getLogName(int context)
    {
        switch (context)
        {
            case SYS:
                return "SYS";
            case NET:
                return "NET";
            default:
                return "UNKNOWN";
        }
    }



    private final Object     LOCK = new Object();
    private int              context;
    private PrintStream      out  = null;
    private FileOutputStream fos  = null;



    /**
     * Static class, so don't allow instantiation.
     */
    private Log(int ctxt)
    {
        context = ctxt;
    }

    public void setLogTarget(PrintStream ps)
    {
        synchronized (LOCK)
        {
            out = ps;
            fos = null;
        }
    }

    public void setLogTarget(String filename)
    {
        synchronized (LOCK)
        {
            try
            {
                fos = new FileOutputStream(filename, true);
                out = new PrintStream(fos, true);
            }
            catch (FileNotFoundException ffne)
            {
                setLogTarget(System.err);
                log(ffne);
            }

            out.println(" ");
            out.print(DIVIDER_PREFIX);
            out.print(getLogName(context));
            out.print(' ');
            out.print(DATE_FORMAT.format(new GregorianCalendar().getTime()));
            out.println(DIVIDER);
        }
    }

    public void log(String s)
    {
        synchronized (LOCK)
        {
            out.print(DATE_FORMAT.format(new GregorianCalendar().getTime()));
            out.print(" [");
            out.print(getLogName(context));
            out.print("] ");
            out.println(s);
        }
    }

    public void log(Throwable t)
    {
        synchronized (LOCK)
        {
            out.print(DATE_FORMAT.format(new GregorianCalendar().getTime()));
            out.print(" [");
            out.print(getLogName(context));
            out.print("] ");
            out.println("");
        }
    }

    public void close()
    {
        if (fos != null)
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                log(SYS, e);
            }
        }

        fos = null;
        out = null;
    }
}
