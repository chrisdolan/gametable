

package com.galactanet.gametable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DiceMacro
{
    List   m_dieTypes = new ArrayList(); // full of DiceMacro_RollType instances
    String m_name;
    String m_macro;



    public DiceMacro()
    {
    }

    public String toString()
    {
        return m_name;
    }

    public String doMacro()
    {
        Player me = GametableFrame.getGametableFrame().getMePlayer();
        String name = me.getCharacterName();

        String ret = name + " rolls " + getRollString() + ": " + roll();
        return ret;
    }

    public String roll()
    {
        String ret = "[";
        boolean bFirstAdd = true;
        int total = 0;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            // get the die type
            DiceMacro_RollType dieType = (DiceMacro_RollType)m_dieTypes.get(i);

            // special case: straight bonus
            if (dieType.m_die < 2)
            {
                if (dieType.m_qty > 0)
                {
                    ret += "+" + dieType.m_qty;
                }
                else if (dieType.m_qty < 0)
                {
                    ret += "" + dieType.m_qty;
                }
                bFirstAdd = false;
                total += dieType.m_qty;
            }
            else
            {
                // normal case: die rollin!
                for (int j = 0; j < dieType.m_qty; j++)
                {
                    int value = rollDie(dieType.m_die);
                    if (bFirstAdd)
                    {
                        ret += "" + value;
                    }
                    else
                    {
                        ret += "+" + value;
                    }
                    bFirstAdd = false;
                    total += value;
                }
            }
        }

        ret += "] = ";
        ret += "" + total;
        return ret;
    }

    public int rollDie(int sides)
    {
        double random = Math.random();
        double result = random * sides;
        int ret = (int)result;
        ret++;
        return ret;
    }

    public String getRollString()
    {
        String ret = getMacroString();

        if (!ret.equals(m_name))
        {
            return m_name + " (" + ret + ")";
        }
        return ret;
    }

    public String getMacroString()
    {
        String ret = "";
        boolean bIsFirst = true;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            // get the die type
            DiceMacro_RollType dieType = (DiceMacro_RollType)m_dieTypes.get(i);

            if (!bIsFirst)
            {
                // special case: Don't put a "+" there if we're on the
                // 1 siders (bonuses) and they're negative
                if (dieType.m_die > 1 || dieType.m_qty >= 0)
                {
                    ret += "+";
                }
            }
            if (dieType.m_qty == 1 && dieType.m_die > 1)
            {
                ret += dieType.getDieName();
            }
            else
            {
                ret += "" + dieType.m_qty + dieType.getDieName();
            }
            bIsFirst = false;
        }

        return ret;
    }

    public boolean init(String macro, String name)
    {
        try
        {
            m_macro = macro;
            m_name = name;

            // the string will be something like "3d6 + 4" or "2d4 + 3d6 + 8"
            boolean bDone = false;
            int segmentStart = 0;
            int segmentEnd = 0;
            boolean bIsMinus = false;
            boolean bNextIsMinus = false;
            while (!bDone)
            {
                // look for the next "+" character
                int nextPlus = macro.indexOf("+", segmentStart);
                int nextMinus = macro.indexOf("-", segmentStart);
                bIsMinus = bNextIsMinus;
                bNextIsMinus = false;

                if (nextPlus == -1 && nextMinus == -1)
                {
                    segmentEnd = -1;
                }
                else if (nextPlus == -1 || nextMinus == -1)
                {
                    // we found a plus or a minus, not both.
                    if (nextPlus == -1)
                    {
                        segmentEnd = nextMinus;
                        bNextIsMinus = true;
                    }
                    if (nextMinus == -1)
                    {
                        segmentEnd = nextPlus;
                    }
                }
                else
                {
                    // we found both
                    if (nextPlus < nextMinus)
                    {
                        segmentEnd = nextPlus;
                    }
                    else
                    {
                        segmentEnd = nextMinus;
                        bNextIsMinus = true;
                    }
                }

                String segment;
                if (segmentEnd == -1)
                {
                    bDone = true;
                    segment = macro.substring(segmentStart);
                }
                else
                {
                    segment = macro.substring(segmentStart, segmentEnd);
                    segmentStart = segmentEnd + 1;
                }

                // clear spaces
                segment = validate(segment);

                if (segment == null)
                {
                    // invalid character
                    return false;
                }
                
                if (segment.length() == 0)
                {
                    continue;
                }

                // now look for a die instruction.
                // valid values are:
                // -all digits
                // -d followed by all digits
                // -digits followed by d followed by digits

                // is there a "d"?
                int dIdx = segment.indexOf("d");

                if (dIdx == -1)
                {
                    // no d. It's all numbers
                    // so it's just an adder
                    // (or subtracter)
                    DiceMacro_RollType dieType = new DiceMacro_RollType();
                    dieType.m_die = 1;
                    int value = Integer.parseInt(segment);
                    if (bIsMinus)
                    {
                        dieType.m_qty = -value;
                    }
                    else
                    {
                        dieType.m_qty = value;
                    }

                    m_dieTypes.add(dieType);
                }
                else
                {
                    // there's a d
                    // if there are 2 d's it's invalid
                    if (segment.indexOf("d", dIdx + 1) != -1)
                    {
                        return false;
                    }

                    // just the one d, then.
                    String left = segment.substring(0, dIdx);
                    String right = segment.substring(dIdx + 1);

                    int num = 0;
                    int type = 0;

                    // it's ok to have nothing for the left string
                    if (left.length() == 0)
                    {
                        // presumed 1 die
                        num = 1;
                    }
                    else
                    {
                        num = Integer.parseInt(left);
                    }

                    // it's not ok to have nothing after the d
                    if (right.length() == 0)
                    {
                        return false;
                    }

                    type = Integer.parseInt(right);

                    DiceMacro_RollType dieType = new DiceMacro_RollType();
                    dieType.m_die = type;
                    dieType.m_qty = num;

                    m_dieTypes.add(dieType);
                }
            }

            if (m_name == null || m_name.length() == 0)
            {
                m_name = getMacroString();
            }

            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    protected String validate(String in)
    {
        String ret = "";
        for (int i = 0; i < in.length(); i++)
        {
            char ch = in.charAt(i);
            if (ch >= '0' && ch <= '9')
            {
                ret += ch;
            }
            else if (ch == 'd')
            {
                ret += ch;
            }
            else if (ch == ' ')
            {
                // this is ok, too
            }
            else
            {
                // invalid character
                return null;
            }
        }
        return ret;
    }

    public void writeToStream(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(m_name);
        dos.writeUTF(m_macro);
    }

    public void initFromStream(DataInputStream dis) throws IOException
    {
        String name = dis.readUTF();
        String macro = dis.readUTF();
        init(macro, name);
    }
}


class DiceMacro_RollType
{
    // note, a "1" sided die is a bonus
    // and qty can be negative
    int m_die;
    int m_qty;



    public DiceMacro_RollType()
    {
    }

    public boolean equals(DiceMacro_RollType comp)
    {
        if (m_die != comp.m_die)
            return false;
        if (m_qty != comp.m_qty)
            return false;
        return true;
    }

    public String getDieName()
    {
        if (m_die < 2)
        {
            return "";
        }
        String name = "d" + m_die;
        return name;
    }

    public String toString()
    {
        if (m_die == 1)
        {
            return String.valueOf(m_qty);
        }

        return m_qty + "d" + m_die;
    }
}
