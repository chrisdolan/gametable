/*
 * PreferenceDescriptor.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.prefs;

/**
 * PreferenceDescriptor is a simple object that describes a preference field.
 * 
 * @author iffy
 */
public class PreferenceDescriptor
{
    public static final int TYPE_CHOICE     = 1;
    public static final int TYPE_FLAG       = 2;
    public static final int TYPE_TEXT_ENTRY = 0;

    private final Object    defaultValue;
    private final String    displayName;
    private final String    name;
    private Object          parameter;
    private final int       type;

    /**
     * Simple Constructor.
     * 
     * @param prefName The unique name of this preference.
     * @param prefType The type of this preference.
     */
    public PreferenceDescriptor(final String prefName, final int prefType)
    {
        this(prefName, prefName, prefType, null, null);
    }

    /**
     * Constructor without flex parameter.
     * 
     * @param prefName The unique name of this preference.
     * @param dispName The display name of this preference.
     * @param prefType The type of preference.
     * @param defaultVal An object representing the default value of this preference.
     * @param param A flex parameter, generally a list of possible values for choice fields.
     */
    public PreferenceDescriptor(final String prefName, final String dispName, final int prefType,
        final Object defaultVal)
    {
        this(prefName, dispName, prefType, defaultVal, null);
    }

    /**
     * Fully qualified constructor.
     * 
     * @param prefName The unique name of this preference.
     * @param dispName The display name of this preference.
     * @param prefType The type of preference.
     * @param defaultVal An object representing the default value of this preference.
     * @param param A flex parameter, generally a list of possible values for choice fields.
     */
    public PreferenceDescriptor(final String prefName, final String dispName, final int prefType,
        final Object defaultVal, final Object param)
    {
        name = prefName;
        displayName = dispName;
        type = prefType;
        defaultValue = defaultVal;
    }

    /**
     * @return Returns the defaultValue.
     */
    public Object getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the parameter.
     */
    public Object getParameter()
    {
        return parameter;
    }

    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return type;
    }

}
