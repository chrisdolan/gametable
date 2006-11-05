/*
 * PointerTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.Pog;
import com.galactanet.gametable.SetPogAttributeDialog;
import com.galactanet.gametable.prefs.PreferenceDescriptor;



/**
 * The basic pog interaction tool.
 * 
 * @author iffy
 */
public class PointerTool extends NullTool
{
    private class DeletePogAttributeActionListener implements ActionListener
    {
        private String key;

        DeletePogAttributeActionListener(String name)
        {
            key = name;
        }

        public void actionPerformed(ActionEvent e)
        {
            Set toDelete = new HashSet();
            toDelete.add(key);
            m_canvas.setPogData(m_menuPog.getId(), null, null, toDelete);
        }
    }

    private class EditPogAttributeActionListener implements ActionListener
    {
        private String key;

        EditPogAttributeActionListener(String name)
        {
            key = name;
        }

        public void actionPerformed(ActionEvent e)
        {
            SetPogAttributeDialog dialog = new SetPogAttributeDialog();
            dialog.loadValues(key, m_menuPog.getAttribute(key));
            dialog.setLocationRelativeTo(m_canvas);
            dialog.setVisible(true);
            if (!dialog.isConfirmed())
            {
                return;
            }
            
            String name = dialog.getName();
            String value = dialog.getValue();
            Set toDelete = new HashSet();
            toDelete.add(key);
            Map toAdd = new HashMap();
            if (name != null && name.length() > 0)
            {
                toAdd.put(name, value);
            }
            m_canvas.setPogData(m_menuPog.getId(), null, toAdd, toDelete);
        }
    }

    private static final String PREF_DRAG = "com.galactanet.gametable.tools.PointerTool.drag";

    /**
     * @return The static, unmodifiable list of preferences for this tool.
     */
    private static final List createPreferenceList()
    {
        List retVal = new ArrayList();
        retVal.add(new PreferenceDescriptor(PREF_DRAG, "Drag map when not over Pog", PreferenceDescriptor.TYPE_FLAG,
            Boolean.TRUE));
        return Collections.unmodifiableList(retVal);
    }

    private static final List PREFERENCES = createPreferenceList();

    private GametableCanvas   m_canvas;
    private Pog               m_grabbedPog;
    private Pog               m_ghostPog;
    private boolean           m_snapping;
    private Point             m_grabOffset;
    private Point             m_mousePosition;
    private boolean           m_clicked   = true;
    private Point             m_startScroll;
    private Point             m_startMouse;

    private Pog               m_lastPogMousedOver;

    private Pog               m_menuPog   = null;

    /**
     * Constructor
     */
    public PointerTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_grabbedPog = null;
        m_ghostPog = null;
        m_grabOffset = null;
        m_mousePosition = null;
        m_startScroll = null;
        m_startMouse = null;
    }

    /*
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        return (m_grabbedPog != null) || (m_startScroll != null);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y, int modifierMask)
    {
        m_clicked = true;
        m_mousePosition = new Point(x, y);
        m_grabbedPog = m_canvas.getActiveMap().getPogAt(m_mousePosition);
        if (m_grabbedPog != null)
        {
            m_ghostPog = new Pog(m_grabbedPog);
            m_grabOffset = new Point(m_grabbedPog.getX() - m_mousePosition.x, m_grabbedPog.getY() - m_mousePosition.y);
            setSnapping(modifierMask);
        }
        else if (GametableFrame.getGametableFrame().getPreferences().getBooleanValue(PREF_DRAG))
        {
            m_startScroll = m_canvas.drawToModel(m_canvas.getPublicMap().getScrollX(), m_canvas.getPublicMap()
                .getScrollY());
            m_startMouse = m_canvas.modelToView(x, y);
            m_canvas.setToolCursor(2);
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
        setSnapping(modifierMask);
        m_mousePosition = new Point(x, y);
        if (m_grabbedPog != null)
        {
            m_clicked = false;
            if (m_snapping)
            {
                Point adjustment = m_ghostPog.getSnapDragAdjustment();
                m_ghostPog.setPosition(m_mousePosition.x + m_grabOffset.x + adjustment.x, m_mousePosition.y
                    + m_grabOffset.y + adjustment.y);
                m_canvas.snapPogToGrid(m_ghostPog);
            }
            else
            {
                m_ghostPog.setPosition(m_mousePosition.x + m_grabOffset.x, m_mousePosition.y + m_grabOffset.y);
            }
            m_canvas.repaint();
        }
        else if (m_startScroll != null)
        {
            Point mousePosition = m_canvas.modelToView(x, y);
            Point viewDelta = new Point(m_startMouse.x - mousePosition.x, m_startMouse.y - mousePosition.y);
            Point modelDelta = m_canvas.drawToModel(viewDelta);
            m_canvas.scrollMapTo(m_startScroll.x + modelDelta.x, m_startScroll.y + modelDelta.y);
        }
        else
        {
            hoverCursorCheck();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        if (m_grabbedPog != null)
        {
            if (m_clicked)
            {
                popupContextMenu(x, y);
            }
            else
            {
                m_grabbedPog.setPosition(m_ghostPog.getPosition());
                if (!m_canvas.isPointVisible(m_mousePosition))
                {
                    // they removed this pog
                    m_canvas.removePog(m_grabbedPog.getId());
                }
                else
                {
                    m_canvas.movePog(m_grabbedPog.getId(), m_ghostPog.getX(), m_ghostPog.getY());
                }
            }
        }
        endAction();
    }

    /**
     * Pops up a pog context menu.
     * 
     * @param x X location of mouse.
     * @param y Y location of mouse.
     */
    private void popupContextMenu(int x, int y)
    {
        m_menuPog = m_grabbedPog;
        JPopupMenu menu = new JPopupMenu("Pog");
        menu.add(new JMenuItem("Cancel"));
        JMenuItem item = new JMenuItem("Set Name...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String s = (String)JOptionPane.showInputDialog(GametableFrame.getGametableFrame(),
                    "Enter new name for this Pog:", "Set Pog Name", JOptionPane.PLAIN_MESSAGE, null, null, m_menuPog
                        .getText());

                if (s != null)
                {
                    m_canvas.setPogData(m_menuPog.getId(), s, null, null);
                }

            }
        });
        menu.add(item);
        item = new JMenuItem("Set Attribute...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                SetPogAttributeDialog dialog = new SetPogAttributeDialog();
                dialog.setLocationRelativeTo(m_canvas);
                dialog.setVisible(true);
                String name = dialog.getName();
                String value = dialog.getValue();
                if (name == null || name.length() == 0)
                {
                    return;
                }
                Map toAdd = new HashMap();
                toAdd.put(name, value);
                m_canvas.setPogData(m_menuPog.getId(), null, toAdd, null);
            }
        });
        menu.add(item);
        if (m_menuPog.getAttributeNames().size() > 0)
        {
            JMenu editMenu = new JMenu("Edit Attribute");

            JMenu removeMenu = new JMenu("Remove Attribute");
            Set nameSet = m_grabbedPog.getAttributeNames();
            for (Iterator iterator = nameSet.iterator(); iterator.hasNext();)
            {
                String key = (String)iterator.next();
                item = new JMenuItem(key);
                item.addActionListener(new DeletePogAttributeActionListener(key));
                removeMenu.add(item);

                item = new JMenuItem(key);
                item.addActionListener(new EditPogAttributeActionListener(key));
                editMenu.add(item);
            }
            menu.add(editMenu);
            menu.add(removeMenu);
        }

        JMenu sizeMenu = new JMenu("Face Size");
        item = new JMenuItem("Reset");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), -1);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("0.5 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 0.5f);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("1 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 1);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("2 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 2);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("3 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 3);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("4 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 4);
            }
        });
        sizeMenu.add(item);

        item = new JMenuItem("6 squares");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.setPogSize(m_menuPog.getId(), 6);
            }
        });
        sizeMenu.add(item);

        menu.add(sizeMenu);

        JMenu rotateMenu = new JMenu("Rotation");
        item = new JMenuItem("0");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 0);
            }
        });
        rotateMenu.add(item);

        item = new JMenuItem("60");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 60);
            }
        });
        rotateMenu.add(item);

        item = new JMenuItem("90");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 90);
            }
        });
        
        rotateMenu.add(item);
        item = new JMenuItem("120");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 120);
            }
        });
        rotateMenu.add(item);

        item = new JMenuItem("180");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 180);
            }
        });
        rotateMenu.add(item);

        item = new JMenuItem("240");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 240);
            }
        });
        rotateMenu.add(item);

        item = new JMenuItem("270");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 270);
            }
        });
        rotateMenu.add(item);
        
        item = new JMenuItem("300");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_canvas.rotatePog(m_menuPog.getId(), 300);
            }
        });
        rotateMenu.add(item);

        menu.add(rotateMenu);

        Point mousePosition = m_canvas.modelToView(x, y);
        menu.show(m_canvas, mousePosition.x, mousePosition.y);
    }

    public void endAction()
    {
        m_grabbedPog = null;
        m_ghostPog = null;
        m_grabOffset = null;
        m_startScroll = null;
        m_startMouse = null;
        hoverCursorCheck();
        m_canvas.repaint();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        if (m_ghostPog != null && m_canvas.isPointVisible(m_mousePosition))
        {
            m_ghostPog.drawGhostlyToCanvas(g);
        }
    }

    /*
     * @see com.galactanet.gametable.Tool#getPreferences()
     */
    public List getPreferences()
    {
        return PREFERENCES;
    }

    /**
     * Sets the snapping status based on the specified modifiers.
     * 
     * @param modifierMask the set of modifiers passed into the event.
     */
    private void setSnapping(int modifierMask)
    {
        if ((modifierMask & MODIFIER_CTRL) > 0)
        {
            m_snapping = false;
        }
        else
        {
            m_snapping = true;
        }
    }

    private void hoverCursorCheck()
    {
        if (GametableFrame.getGametableFrame().getPreferences().getBooleanValue(PREF_DRAG))
        {
            Pog pog = m_canvas.getActiveMap().getPogAt(m_mousePosition);
            if (pog != null)
            {
                m_canvas.setToolCursor(0);
            }
            else
            {
                m_canvas.setToolCursor(1);
            }

            if (m_lastPogMousedOver != pog)
            {
                m_canvas.repaint();
            }
            m_lastPogMousedOver = pog;
        }
    }
}
