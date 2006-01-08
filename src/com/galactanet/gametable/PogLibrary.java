/*
 * PogLibrary.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.File;
import java.io.IOException;
import java.util.*;



/**
 * A recursively-defined representation of a set of pogs.
 * 
 * @author iffy
 */
public class PogLibrary
{
    // --- Constants -------------------------------------------------------------------------------------------------

    public static final int LIBRARY_TYPE_POG      = 0;
    public static final int LIBRARY_TYPE_UNDERLAY = 1;

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * The short name of this library. Unique within the parent library.
     */
    private String          name                  = null;

    /**
     * The filesystem path to this set of pogs.
     */
    private File            location              = null;

    /**
     * Whether this is a pog or underlay library.
     */
    private int             libraryType           = LIBRARY_TYPE_POG;

    /**
     * A list of child libraries, sorted by name.
     */
    private List            children              = new ArrayList();

    /**
     * The list of pogs in this library.
     */
    private List            pogs                  = new ArrayList();

    /**
     * Set of acquired pog names.
     */
    private Set             acquiredPogs          = new HashSet();

    /**
     * Set of acquired libraries.
     */
    private Set             acquiredLibraries     = new HashSet();

    /**
     * The parent library.
     */
    private PogLibrary      parent                = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Root PogLibrary Constructor.
     * 
     * @throws IOException
     */
    public PogLibrary() throws IOException
    {
        location = new File(".").getCanonicalFile();
        name = getNameFromDirectory(location);
        addLibrary("pogs", LIBRARY_TYPE_POG);
        addLibrary("underlays", LIBRARY_TYPE_UNDERLAY);
    }

    /**
     * Child PogLibrary Constructor.
     */
    public PogLibrary(PogLibrary mommy, String directory, int type) throws IOException
    {
        parent = mommy;
        libraryType = type;
        location = new File(directory).getCanonicalFile();
        if (!location.canRead() || !location.isDirectory())
        {
            throw new IOException("cannot read from " + directory);
        }
        name = getNameFromDirectory(location);

        acquirePogs();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return Returns the parent library.
     */
    public PogLibrary getParent()
    {
        return parent;
    }

    /**
     * @return Returns the root library.
     */
    public PogLibrary getRoot()
    {
        if (parent == null)
        {
            return this;
        }

        return parent.getRoot();
    }

    /**
     * @return Returns the child libraries of this library.
     */
    public List getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * @return Returns the location of this library.
     */
    public File getLocation()
    {
        return location;
    }

    /**
     * @return Gets the canonical path of this library.
     */
    public String getPath()
    {
        try
        {
            return location.getCanonicalPath();
        }
        catch (IOException ioe)
        {
            return location.getAbsolutePath();
        }
    }

    /**
     * @return Returns the name of this library.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the pogs in this library.
     */
    public List getPogs()
    {
        return Collections.unmodifiableList(pogs);
    }

    /**
     * @return Returns the pogs in this library.
     */
    public List getAllPogs()
    {
        int size = children.size();
        if (size < 1)
        {
            return getPogs();
        }

        List accum = new ArrayList(pogs);
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)children.get(i);
            accum.addAll(child.getAllPogs());
        }

        return Collections.unmodifiableList(accum);
    }

    /**
     * Gets the child library of the given name.
     * 
     * @param libraryName Name of library to get.
     * @return Library found or null.
     */
    public PogLibrary getChild(String libraryName)
    {
        PogLibrary child = getChildExact(libraryName);
        if (child != null)
        {
            return child;
        }

        File childPath = UtilityFunctions.getCanonicalFile(new File(location.getPath() + UtilityFunctions.LOCAL_SEPARATOR + libraryName));

        return getChildExact(childPath.getPath());
    }

    /**
     * Gets the pog with the given name.
     * 
     * @param pogName name of pog to fetch
     * @return Pog found or null.
     */
    public PogType getPog(String pogName)
    {
        int size = pogs.size();
        for (int i = 0; i < size; ++i)
        {
            PogType pogType = (PogType)pogs.get(i);
            if (pogName.equals(pogType.getFilename()))
            {
                return pogType;
            }
        }

        size = children.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)children.get(i);
            PogType pog = child.getPog(pogName);
            if (pog != null)
            {
                return pog;
            }
        }

        return null;
    }

    /**
     * Creates a placeholder PogType for a pog that hasn't been received yet. Delegates to children if appropriate.
     * 
     * @param filename Filename of pog to create placeholder for.
     * @param face What we think the face size of the pog is at this point.
     * @return Placeholder PogType
     */
    public PogType createPlaceholder(String filename, int face)
    {
//        Log.log(Log.SYS, this + ".createPlaceholder(" + filename + ", " + face + ")");
        File f = new File(filename);
        File p = f.getParentFile();
        PogLibrary lib = findDeepestChild(p);
        if (lib == null)
        {
            Log.log(Log.SYS, "unable to create library");
            return null;
        }

        if (lib != this)
        {
            return lib.createPlaceholder(filename, face);
        }

        File absParent = p.getAbsoluteFile();
        p.mkdirs();
        if (!absParent.equals(getLocation()))
        {
            PogLibrary child = getChild(p.getPath());
            if (child != null)
            {
                return child.createPlaceholder(filename, face);
            }

            File next = absParent;
            while (!next.getParentFile().equals(getLocation()))
            {
                next = next.getParentFile();
            }
            child = addLibrary(next.getAbsolutePath(), libraryType);
            if (child == null)
            {
                return null;
            }
            return child.createPlaceholder(filename, face);
        }

        return addPog(filename, face, libraryType);
    }

    /**
     * Ensures that this panel has all the available pogs loaded.
     */
    public boolean acquirePogs()
    {
        if (!location.exists())
        {
            return false;
        }

        boolean retVal = false;

        // We don't want to scour the root library for pogs
        if (getParent() != null)
        {
            String[] files = location.list();

            File rootLocation = getRoot().getLocation();
            String path = UtilityFunctions.getRelativePath(rootLocation, location) + UtilityFunctions.LOCAL_SEPARATOR;

            for (int i = 0, size = files.length; i < size; ++i)
            {
                String filename = path + files[i];
                File file = new File(filename);

                if (file.isFile() && file.canRead())
                {
                    if (addPog(filename, 1, libraryType, true) != null)
                    {
                        retVal = true;
                    }
                }
                else if (file.isDirectory() && file.canRead())
                {
                    if (addLibrary(filename, libraryType) != null)
                    {
                        retVal = true;
                    }
                }
            }
        }

        int numPogs = pogs.size();
        for (int i = 0; i < numPogs; ++i)
        {
            PogType pog = (PogType)pogs.get(i);
            if (pog.isUnknown())
            {
                pog.load();
            }
        }

        int size = children.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)children.get(i);
            if (child.acquirePogs())
            {
                retVal = true;
            }
        }

        return retVal;
    }

    // --- Object Implementation ---

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[PogLib " + getLocation() + " (" + pogs.size() + ")]";
    }

    // --- Private Methods ---

    private PogLibrary findDeepestChild(File path)
    {
        path = path.getAbsoluteFile();
        
        // trivial accept
        if (path.equals(getLocation()))
        {
            return this;
        }

        int size = children.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)children.get(i);
            PogLibrary lib = child.findDeepestChild(path);
            if (lib != null)
            {
                return lib;
            }
        }

        if (!UtilityFunctions.isAncestorFile(getLocation(), path))
        {
            return null;
        }

        return this;
    }

    /**
     * Gets a child by the exact name.
     * 
     * @param libraryName Exact name of child to find.
     * @return Child library with the given name, or null if not found.
     */
    private PogLibrary getChildExact(String libraryName)
    {
        File file = UtilityFunctions.getCanonicalFile(new File(libraryName));
        int size = children.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)children.get(i);
            if (file.equals(child.getLocation()))
            {
                return child;
            }
        }

        return null;
    }

    /**
     * Calculated the library name from a directory file.
     * 
     * @param file Directory to use to calculate name.
     * @return Name of this library node.
     */
    private static String getNameFromDirectory(File file)
    {
        String temp = file.getAbsolutePath();
        int start = temp.lastIndexOf(UtilityFunctions.LOCAL_SEPARATOR) + 1;
        if (start == temp.length())
        {
            start = temp.lastIndexOf(UtilityFunctions.LOCAL_SEPARATOR, start - 1) + 1;
        }
        
        int end = temp.indexOf(UtilityFunctions.LOCAL_SEPARATOR, start);
        if (end < 0)
        {
            end = temp.length();
        }
        return new String(temp.substring(start, end));
    }


    /**
     * In-place sorts the list of pogs by height.
     * 
     * @param toSort List of Pogs to sort.
     */
    private static void sortPogsByLabel(List toSort)
    {
        Collections.sort(toSort, new Comparator()
        {
            /*
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare(Object a, Object b)
            {
                PogType pa = (PogType)a;
                PogType pb = (PogType)b;
                return pa.getLabel().compareTo(pb.getLabel());
            }
        });
    }

    
    private PogType addPog(String pogName, int facing, int type)
    {
        return addPog(pogName, facing, type, false);
    }

    private PogType addPog(String pogName, int facing, int type, boolean ignoreOnFail)
    {
        try
        {
            File f = new File(pogName).getAbsoluteFile();
            if (acquiredPogs.contains(f))
            {
                return null;
            }

            PogType pog = new PogType(pogName, facing, (type == LIBRARY_TYPE_UNDERLAY));
            if (!ignoreOnFail || !pog.isUnknown())
            {
                //Log.log(Log.SYS, new Exception(this + " added: " + pog));
                pogs.add(pog);
                sortPogsByLabel(pogs);
            }
            acquiredPogs.add(f);
            return pog;
        }
        catch (Exception ex)
        {
            // any exceptions thrown in this process cancel
            // the addition of that one pog.
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    /**
     * Adds library to this library, ensuring it doesn't already exist.
     * 
     * @param libName
     * @param type
     * @return
     */
    private PogLibrary addLibrary(String libName, int type)
    {
        try
        {
            File libDir = new File(libName).getAbsoluteFile();
            if (acquiredLibraries.contains(libDir))
            {
                return null;
            }

            PogLibrary child = new PogLibrary(this, libDir.getPath(), type);
            children.add(child);
            //Log.log(Log.SYS, new Exception(this + " added: " + child));
            acquiredLibraries.add(libDir);
            return child;
        }
        catch (Exception ex)
        {
            // any exceptions thrown in this process cancel
            // the addition of that one directory.
            Log.log(Log.SYS, ex);
            return null;
        }
    }

}
