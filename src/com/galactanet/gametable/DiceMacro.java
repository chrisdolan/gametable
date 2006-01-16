/*
 * DiceMacro.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Encapsulates a dice macro.
 * 
 * @author sephalon
 * @author iffy
 */
public class DiceMacro
{
    // --- Types ---

    /**
     * A class to hold the results of a roll.
     * 
     * @author iffy
     */
    public static class Result
    {
        /**
         * The actual resulting value.
         */
        public int    value;

        /**
         * The macro string executed.
         */
        public String roll;

        /**
         * The string of rolls made.
         */
        public String result;

        /**
         * Convenience constructor.
         * 
         * @param val Actual result value.
         * @param rollText Macro string executed.
         * @param resultText Result string.
         */
        public Result(int val, String rollText, String resultText)
        {
            value = val;
            roll = rollText;
            result = resultText;
        }
    }

    /**
     * An encapsulation of one term of the DiceMacro.
     * 
     * @author sephalon
     */
    private static class Term
    {
        /**
         * The number of sides on this die. A "1" sided die is a bonus.
         */
        public int m_die;

        /**
         * Number of die rolls for this term. If negative, then the roll result is negated. If a bonus, than this is
         * just the bonus value.
         */
        public int m_qty;

        /**
         * The number of highest rolls to keep. If negative, keeps the lowest rolls. A value of 0 means to keep all
         * rolls.
         */
        public int m_keep;

        /**
         * @param qty
         * @param die
         * @param keep
         */
        public Term(int qty, int die, int keep)
        {
            m_qty = qty;
            m_die = die;
            m_keep = keep;
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            try
            {
                Term t = (Term)o;
                return m_die == t.m_die && m_qty == t.m_qty && m_keep == t.m_keep;
            }
            catch (ClassCastException cce)
            {
                return false;
            }
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            StringBuffer result = new StringBuffer();

            if (m_die > 1)
            {
                if (m_qty < -1 || m_qty > 1)
                {
                    result.append(m_qty);
                }
                else if (m_qty == -1)
                {
                    result.append('-');
                }

                result.append('d');
                result.append(m_die);
                if (m_keep < 0)
                {
                    result.append('l');
                    result.append(0 - m_keep);
                }
                else if (m_keep > 0)
                {
                    result.append('h');
                    result.append(m_keep);
                }
            }
            else
            {
                result.append(m_qty);
            }

            return result.toString();
        }
    }

    // --- Static Methods ---

    /**
     * Compares the normalized values of the two Strings.
     * 
     * @param a First String to compare.
     * @param b Second String to compare.
     * @return True if the two strings are the same after normalization.
     */
    private static boolean isSameMacroString(String a, String b)
    {
        if (a == b)
        {
            return true;
        }

        if (a == null || b == null)
        {
            return false;
        }

        return normalizeMacroString(a).equals(normalizeMacroString(b));
    }
    
    public static String generateOutputString(String rollerName, String rollName, String rollItemizedResults, String result)
    {
        String ret = UtilityFunctions.emitUserLink(rollerName) + " rolls " + rollName + ": [" + rollItemizedResults + "] = "+ GametableFrame.DIEROLL_MESSAGE_FONT + result + GametableFrame.END_DIEROLL_MESSAGE_FONT;
        return ret;
    }

    /**
     * Regularlizes a string before parsing.
     * 
     * @param in Macro string to normalize.
     * @return Normalized macro string.
     */
    private static String normalizeMacroString(String in)
    {
        // Remove spaces.
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < in.length(); index++)
        {
            if (!Character.isWhitespace(in.charAt(index)))
            {
                buffer.append(Character.toLowerCase(in.charAt(index)));
            }
        }

        return buffer.toString();
    }

    /**
     * Rolls a die with the given number of sides.
     * 
     * @param sides Sides of the die to roll.
     * @return The result of the die roll.
     */
    private static int rollDie(int sides)
    {
        return UtilityFunctions.getRandom(sides) + 1;
    }

    // --- Members ---

    /**
     * List of parsed terms in this macro.
     */
    private List  m_terms = new ArrayList();

    /**
     * The name of this macro. Null means an "anonymous" macro.
     */
    private String m_name  = null;

    /**
     * The parsed and then reserialized macro text that initialized this macro.
     */
    private String m_macro = "0";

    // --- Constructors ---

    /**
     * Uninitialized Constructor.
     */
    public DiceMacro()
    {
    }

    /**
     * Initialized constructor.
     */
    public DiceMacro(String macro, String name)
    {
        if (!init(macro, name))
        {
            reset();
        }
    }

    // --- Methods ---

    /**
     * @return True if this DiceMacro is initialized.
     */
    public boolean isInitialized()
    {
        return !("0".equals(getMacro()));
    }

    /**
     * Resets this dice macro to be uninitialized.
     */
    public void reset()
    {
        setName(null);
        setMacro("0");
        m_terms.clear();
    }

    /**
     * Initializes the Macro with the given data.
     * 
     * @param macro Macro string to parse.
     * @param name Name of this macro. Null means "anonymous". Empty string means use the parsed macro as the name.
     * @return True if the macro string was parseable.
     */
    public boolean init(String macro, String name)
    {
        reset();
        try
        {
            setName(name);
            setMacro(normalizeMacroString(macro));

            // Parse the macro string. It will be something like
            // "3d6 + 4" or "2d4 + 3d6h2 + 8"

            if (!isInitialized())
            {
                return true;
            }

            // Grab individual dice rolls.
            boolean isNegative = false;
            // #dice, type, #keep
            int[] numbers = {
                1, 1, 0
            };

            // Corresponds with index of numbers array
            int phase = 0;
            int startOfCurrentNumber = 0;
            int length = getMacro().length();
            for (int index = 0; index < length; ++index)
            {
                char c = getMacro().charAt(index);
                boolean isLast = (index == (length - 1));
                if (!Character.isDigit(c) || isLast)
                {
                    // End of this number.
                    if (startOfCurrentNumber != index || isLast)
                    {
                        // end is position after number.
                        int end = (isLast ? length : index);
                        String numberStr = getMacro().substring(startOfCurrentNumber, end);
                        int number = Integer.parseInt(numberStr);
                        numbers[phase] = isNegative ? (-number) : number;
                        isNegative = false;
                    }
                    startOfCurrentNumber = index + 1;

                    // Check for end of dice roll.
                    if (c == '+' || c == '-' || isLast)
                    {
                        if (index > 0 || isLast)
                        {
                            m_terms.add(new Term(numbers[0], numbers[1], numbers[2]));
                        }
                        isNegative = (c == '-');
                        numbers[0] = 1;
                        numbers[1] = 1;
                        numbers[2] = 0;
                        phase = 0;
                    }
                    else if (c == 'd')
                    {
                        numbers[0] = isNegative ? (-numbers[0]) : numbers[0];
                        isNegative = false;
                        phase = 1;
                    }
                    else if (c == 'h')
                    {
                        isNegative = false;
                        phase = 2;
                    }
                    else if (c == 'l')
                    {
                        isNegative = true;
                        phase = 2;
                    }
                    else
                    {
                        reset();
                        return false;
                    }
                }
            }

            setMacro(getMacroString());
            if (getName() != null && getName().length() < 1)
            {
                setName(getMacro());
            }

            return true;
        }
        catch (Throwable ex)
        {
            Log.log(Log.SYS, "parse error: \"" + getMacro() + "\" (\"" + macro + "\")");
            Log.log(Log.SYS, ex);
            reset();
            return false;
        }
    }

    /**
     * @param name The name to set.
     */
    private void setName(String name)
    {
        m_name = name;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * @param macro The macro to set.
     */
    private void setMacro(String macro)
    {
        m_macro = macro;
    }

    /**
     * @return Returns the macro.
     */
    public String getMacro()
    {
        return m_macro;
    }

    /**
     * Executes this macro and returns the formatted result.
     * 
     * @return The formatted result string.
     */
    public String doMacro()
    {
        if (!isInitialized())
        {
            return "";
        }

        Player me = GametableFrame.getGametableFrame().getMyPlayer();
        String name = me.getCharacterName();

        Result result = roll();

        String ret = generateOutputString(name, result.roll, result.result, ""+result.value);        
        return ret;
    }

    /**
     * Executes this macro, returning a data object with various information about the result.
     * 
     * @return DiceMacro.Result object.
     */
    public Result roll()
    {
        if (!isInitialized())
        {
            return null;
        }

        StringBuffer ret = new StringBuffer();
        int total = 0;

        for (int i = 0; i < m_terms.size(); i++)
        {
            // get the die type
            Term dieType = (Term)m_terms.get(i);
            boolean bFirstAdd = (i == 0);

            int subtotal = 0;
            // special case: straight bonus
            if (dieType.m_die < 2)
            {
                if (dieType.m_qty > 0)
                {
                    if (!bFirstAdd)
                    {
                        ret.append(" + ");
                    }
                    ret.append(dieType.m_qty);
                    subtotal += dieType.m_qty;
                }
                else if (dieType.m_qty < 0)
                {
                    if (!bFirstAdd)
                    {
                        ret.append(' ');
                    }
                    ret.append("- ");
                    ret.append(-dieType.m_qty);
                    subtotal += dieType.m_qty;
                }
            }
            else if (dieType.m_keep == 0)
            {
                // normal case: die rollin!
                // Straight addition
                int bound = Math.abs(dieType.m_qty);
                boolean negative = (dieType.m_qty < 0);
                if (bound > 0)
                {
                    // single quantity - no parentheticals
                    if (bound == 1)
                    {
                        int value = rollDie(dieType.m_die);
                        if (bFirstAdd)
                        {
                            if (negative)
                            {
                                ret.append("- ");
                            }
                        }
                        else
                        {
                            ret.append(negative ? " - " : " + ");
                        }

                        ret.append(value);
                        if (negative)
                        {
                            subtotal -= value;
                        }
                        else
                        {
                            subtotal += value;
                        }
                    }
                    else
                    {
                        // multiple quantity - parentheticals
                        if (bFirstAdd)
                        {
                            if (negative)
                            {
                                ret.append("- ");
                            }
                        }
                        else
                        {
                            ret.append(negative ? " - " : " + ");
                        }

                        if (m_terms.size() > 1)
                        {
                            ret.append('(');
                        }
                        for (int j = 0; j < bound; j++)
                        {
                            int value = rollDie(dieType.m_die);
                            if (j > 0)
                            {
                                ret.append(" + ");
                            }
                            ret.append(value);
                            if (negative)
                            {
                                subtotal -= value;
                            }
                            else
                            {
                                subtotal += value;
                            }
                        }
                        if (m_terms.size() > 1)
                        {
                            ret.append(')');
                        }
                    }
                }
            }
            else
            {
                // Keep some rolls.
                int number = Math.abs(dieType.m_qty);
                if (!bFirstAdd)
                {
                    ret.append((dieType.m_qty < 0) ? " - " : " + ");
                }
                ret.append('(');
                int[] rolls = new int[number];
                for (int index = 0; index < number; index++)
                {
                    rolls[index] = rollDie(dieType.m_die);
                    if (index < 1)
                    {
                        ret.append(rolls[index]);
                    }
                    else
                    {
                        ret.append(',');
                        ret.append(rolls[index]);
                    }
                }
                Arrays.sort(rolls);
                if (dieType.m_keep > 0)
                {
                    // Keep highest
                    for (int index = dieType.m_qty - dieType.m_keep; index < dieType.m_qty; index++)
                    {
                        if (dieType.m_qty < 0)
                        {
                            subtotal -= rolls[index];
                        }
                        else
                        {
                            subtotal += rolls[index];
                        }
                    }
                    ret.append(")h");
                    ret.append(dieType.m_keep);
                }
                else
                {
                    // Keep lowest
                    for (int index = 0; index < -dieType.m_keep; index++)
                    {
                        if (dieType.m_qty < 0)
                        {
                            subtotal -= rolls[index];
                        }
                        else
                        {
                            subtotal += rolls[index];
                        }
                    }
                    ret.append(")l");
                    ret.append(-dieType.m_keep);
                }

            }
            total += subtotal;
            // System.out.println(dieType + ": " + subtotal + " (" + total + ")");
        }

        return new Result(total, getRollString(), ret.toString());
    }

    /**
     * @return The string used to describe this macro when formatting a roll. 
     */
    private String getRollString()
    {
        if (!isInitialized())
        {
            return "";
        }

        return toFormattedString();
    }

    /**
     * Serializes the parsed Macro terms back into a source string that would generate the same macro.
     * 
     * @return The serialized macro string.
     */
    private String getMacroString()
    {
        if (!isInitialized())
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        boolean bIsFirst = true;

        for (int i = 0; i < m_terms.size(); i++)
        {
            String dice = m_terms.get(i).toString();
            if (!bIsFirst)
            {
                buffer.append(' ');
            }

            boolean negate = (dice.charAt(0) == '-');
            if (negate)
            {
                buffer.append("- ");
                buffer.append(dice.substring(1).trim());
            }
            else
            {
                if (!bIsFirst)
                {
                    buffer.append("+ ");
                }

                buffer.append(dice);
            }
            bIsFirst = false;
        }

        return buffer.toString();
    }

    public void writeToStream(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getName());
        dos.writeUTF(getMacro());
    }

    public void initFromStream(DataInputStream dis) throws IOException
    {
        String name = dis.readUTF();
        String macro = dis.readUTF();
        init(macro, name);
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (getName() == null || isSameMacroString(getName(), getMacro()))
        {
            return getMacro();
        }

        return getName() + " (" + getMacro() + ")";
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toFormattedString()
    {
        if (getName() == null || isSameMacroString(getName(), getMacro()))
        {
            return "<b><font color=\"#006600\">" + getMacro() + "</font></b>";
        }

        return "<b><font color=\"#006600\">" + getName() + "</font></b>" + " (" + getMacro() + ")";
    }

}
