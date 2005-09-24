/*
 * Preferences.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.prefs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Class that encapsulates all of the persistable preference state for GameTable.
 * 
 * @author iffy
 */
public class Preferences
{
    // --- Members ---

    /**
     * The map of custom preference descriptors.
     */
    private Map preferenceDescriptors = new HashMap();

    /**
     * Map of the custom preference values.
     */
    private Map preferenceValues      = new HashMap();



    // --- Constructors ---

    /**
     * Default Constructor.
     */
    public Preferences()
    {
    }

    // --- Public Functions ---

    public void addPreference(PreferenceDescriptor pref)
    {
        preferenceDescriptors.put(pref.getName(), pref);
        preferenceValues.put(pref.getName(), pref.getDefaultValue());
    }

    public PreferenceDescriptor getPreference(String name)
    {
        return (PreferenceDescriptor)preferenceDescriptors.get(name);
    }

    public Set getPreferenceNames()
    {
        return Collections.unmodifiableSet(preferenceDescriptors.entrySet());
    }

    public Object getValue(String name)
    {
        Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultValue(name);
        }

        return o;
    }

    public boolean getBooleanValue(String name)
    {
        Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultBooleanValue(name);
        }

        return ("true".equalsIgnoreCase(o.toString()));
    }

    public int getIntegerValue(String name)
    {
        Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultIntegerValue(name);
        }

        try
        {
            return Integer.parseInt(o.toString());
        }
        catch (NumberFormatException nfe)
        {
            return getDefaultIntegerValue(name);
        }
    }

    public String getStringValue(String name)
    {
        Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultStringValue(name);
        }

        return o.toString();
    }

    // --- Private Functions ---

    private Object getDefaultValue(String name)
    {
        PreferenceDescriptor pref = getPreference(name);
        if (pref == null)
        {
            return null;
        }

        return pref.getDefaultValue();
    }

    private boolean getDefaultBooleanValue(String name)
    {
        PreferenceDescriptor pref = getPreference(name);
        if (pref == null || pref.getDefaultValue() == null)
        {
            return false;
        }

        return ("true".equalsIgnoreCase(pref.getDefaultValue().toString()));
    }

    private int getDefaultIntegerValue(String name)
    {
        PreferenceDescriptor pref = getPreference(name);
        if (pref == null || pref.getDefaultValue() == null)
        {
            return Integer.MIN_VALUE;
        }

        try
        {
            return Integer.parseInt(pref.getDefaultValue().toString());
        }
        catch (NumberFormatException nfe)
        {
            return Integer.MIN_VALUE;
        }
    }

    private String getDefaultStringValue(String name)
    {
        PreferenceDescriptor pref = getPreference(name);
        if (pref == null || pref.getDefaultValue() == null)
        {
            return null;
        }

        return pref.getDefaultValue().toString();
    }
}
