

package com.galactanet.gametable;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class PlayerListCellRenderer extends JLabel implements ListCellRenderer
{
    public ImageIcon[] m_icons;



    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(JList list, Object value, // value to display
        int index, // cell index
        boolean isSelected, // is the cell selected
        boolean cellHasFocus) // the list and the cell have the focus
    {

        // lazy init
        if (m_icons == null)
        {
            m_icons = new ImageIcon[GametableCanvas.NUM_POINT_CURSORS];
            m_icons[0] = new ImageIcon(UtilityFunctions.getImage("assets/whiteHand_small.png"));
            m_icons[1] = new ImageIcon(UtilityFunctions.getImage("assets/brownHand_small.png"));
            m_icons[2] = new ImageIcon(UtilityFunctions.getImage("assets/purpleHand_small.png"));
            m_icons[3] = new ImageIcon(UtilityFunctions.getImage("assets/blueHand_small.png"));
            m_icons[4] = new ImageIcon(UtilityFunctions.getImage("assets/redHand_small.png"));
            m_icons[5] = new ImageIcon(UtilityFunctions.getImage("assets/greenHand_small.png"));
            m_icons[6] = new ImageIcon(UtilityFunctions.getImage("assets/greyHand_small.png"));
            m_icons[7] = new ImageIcon(UtilityFunctions.getImage("assets/yellowHand_small.png"));
        }
        String s = value.toString();
        setText(s);
        setIcon(m_icons[index]);
        return this;
    }
}
