/*
 * PointerTool.java: GameTable is in the Public Domain.
 */

package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.GametableMap;
import com.galactanet.gametable.GridMode;
import com.galactanet.gametable.Pog;
import com.galactanet.gametable.SetPogAttributeDialog;
import com.galactanet.gametable.util.UtilityFunctions;
import com.galactanet.gametable.prefs.PreferenceDescriptor;

/**
 * The basic pog interaction tool.
 * 
 * @author iffy
 */
public class PointerTool extends NullTool
{
    GridMode                   m_gridMode;
    
    private class DeletePogAttributeActionListener implements ActionListener
    {
        private final String key;

        DeletePogAttributeActionListener(final String name)
        {
            key = name;
        }

        public void actionPerformed(final ActionEvent e)
        {
            final Set toDelete = new HashSet();
            toDelete.add(key);
            m_canvas.setPogData(m_menuPog.getId(), null, null, toDelete);
        }
    }

    private class EditPogAttributeActionListener implements ActionListener
    {
        private final String key;

        EditPogAttributeActionListener(final String name)
        {
            key = name;
        }

        public void actionPerformed(final ActionEvent e)
        {
            final SetPogAttributeDialog dialog = new SetPogAttributeDialog(true);
            dialog.loadValues(key, m_menuPog.getAttribute(key));
            dialog.setLocationRelativeTo(m_canvas);
            dialog.setVisible(true);
            if (!dialog.isConfirmed())
            {
                return;
            }

            final String name = dialog.getName();
            final String value = dialog.getValue();
            final Set toDelete = new HashSet();
            toDelete.add(key);
            final Map toAdd = new HashMap();
            if ((name != null) && (name.length() > 0))
            {
                toAdd.put(name, value);
            }
            m_canvas.setPogData(m_menuPog.getId(), null, toAdd, toDelete);
        }
    }

    private static final String PREF_DRAG   = "com.galactanet.gametable.tools.PointerTool.drag";

    private static final List   PREFERENCES = createPreferenceList();

    /**
     * @return The static, unmodifiable list of preferences for this tool.
     */
    private static final List createPreferenceList()
    {
        final List retVal = new ArrayList();
        retVal.add(new PreferenceDescriptor(PREF_DRAG, "Drag map when not over Pog", PreferenceDescriptor.TYPE_FLAG,
            Boolean.TRUE));
        return Collections.unmodifiableList(retVal);
    }

    private GametableCanvas m_canvas;
    private GametableMap    m_from;
    private GametableMap    m_to;
    private boolean         m_clicked = true;
    private Pog             m_ghostPog;
    private Pog             m_grabbedPog;
    private Point           m_grabOffset;
    private Pog             m_lastPogMousedOver;
    private Pog             m_menuPog = null;
    private Point           m_mousePosition;
    private boolean         m_snapping;

    private Point           m_startMouse;

    private Point           m_startScroll;

    /**
     * Constructor
     */
    public PointerTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(final GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_grabbedPog = null;
        m_ghostPog = null;
        m_grabOffset = null;
        m_mousePosition = null;
        m_startScroll = null;
        m_startMouse = null;
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
     * @see com.galactanet.gametable.Tool#getPreferences()
     */
    public List getPreferences()
    {
        return PREFERENCES;
    }

    private void hoverCursorCheck()
    {
        if (GametableFrame.getGametableFrame().getPreferences().getBooleanValue(PREF_DRAG))
        {
            final Pog pog = m_canvas.getActiveMap().getPogAt(m_mousePosition);
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
    public void mouseButtonPressed(final int x, final int y, final int modifierMask)
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
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        if (m_grabbedPog != null)
        {
            if (m_clicked)
            {
                popupContextMenu(x, y, modifierMask);
            }
            else
            {
                if (!m_grabbedPog.isLocked())
                {
                    m_grabbedPog.setPosition(m_ghostPog.getPosition());
                    if (!m_canvas.isPointVisible(m_mousePosition))
                    {
                        // they removed this pog
                            //If pog not locked, do remove
                            m_canvas.removePog(m_grabbedPog.getId());
                    }
                    else
                    {
                        m_canvas.movePog(m_grabbedPog.getId(), m_ghostPog.getX(), m_ghostPog.getY());
                    }
                }
            }
        }
        endAction();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
        setSnapping(modifierMask);
        m_mousePosition = new Point(x, y);
        if ((m_grabbedPog != null) && !m_grabbedPog.isLocked())
        {
            m_clicked = false;
            if (m_snapping)
            {
                final Point adjustment = m_ghostPog.getSnapDragAdjustment();
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
            final Point mousePosition = m_canvas.modelToView(x, y);
            final Point viewDelta = new Point(m_startMouse.x - mousePosition.x, m_startMouse.y - mousePosition.y);
            final Point modelDelta = m_canvas.drawToModel(viewDelta);
            m_canvas.scrollMapTo(m_startScroll.x + modelDelta.x, m_startScroll.y + modelDelta.y);
        }
        else if ((m_grabbedPog != null) && m_grabbedPog.isLocked())
        {
            m_clicked = false;
        }
        else
        {
            hoverCursorCheck();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
        if ((m_ghostPog != null) && m_canvas.isPointVisible(m_mousePosition))
        {
            m_ghostPog.drawGhostlyToCanvas(g);
        }
    }

    /**
     * Pops up a pog context menu.
     * 
     * @param x X location of mouse.
     * @param y Y location of mouse.
     */
    private void popupContextMenu(final int x, final int y, final int modifierMask)
    {
        m_menuPog = m_grabbedPog;
        final JPopupMenu menu = new JPopupMenu("Pog");
        if ((modifierMask & MODIFIER_SHIFT) > 0) // holding shift
        {   
            final int xLocation;
            final int yLocation;
            final int pogSize = m_menuPog.getFaceSize();
            final int tempSize = pogSize;
            final int m_gridModeId = m_canvas.getGridModeId();

            if (m_gridModeId == 1) //square mode
            {
                xLocation =  (m_menuPog.getX() / 64) + ( ((tempSize % 2 == 0) ? pogSize - 1 : pogSize) / 2);
                yLocation = ((m_menuPog.getY() / 64) + ( ((tempSize % 2 == 0) ? pogSize - 1 : pogSize) / 2)) * -1;
            }
            else if (m_gridModeId == 2) //hex mode - needs work to get it to display appropriate numbers
            {
                xLocation = m_menuPog.getX();
                yLocation = m_menuPog.getY() * -1;
            }
            else //no grid
            {
                xLocation = m_menuPog.getX();
                yLocation = m_menuPog.getY() * -1;
            }
            
            menu.add(new JMenuItem("X: " + xLocation));
            menu.add(new JMenuItem("Y: " + yLocation));
        }
        else
        {
            menu.add(new JMenuItem("Cancel"));
            JMenuItem item = new JMenuItem(m_menuPog.isLocked() ? "Unlock" : "Lock");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    m_canvas.lockPog(m_menuPog.getId(), !m_menuPog.isLocked());
                    //System.out.println(m_menuPog.isLocked());
                }
            });
            menu.add(item);
            item = new JMenuItem(m_canvas.isPublicMap() ? "Unpublish" : "Publish");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    final Pog pog = m_menuPog;
                    if (m_canvas.isPublicMap())
                    {
                        m_from = m_canvas.getPublicMap();
                        m_to = m_canvas.getPrivateMap();
                    }
                    else
                    {
                        m_from = m_canvas.getPrivateMap();
                        m_to = m_canvas.getPublicMap();
                    }

                    // this pog gets copied
                    final Pog newPog = new Pog(pog);
                    newPog.assignUniqueId();
                    m_canvas.setActiveMap(m_to);
                    m_canvas.addPog(newPog);
                    m_canvas.lockPog(newPog.getId(), pog.isLocked());
                    m_canvas.setActiveMap(m_from);

                    if ((modifierMask & MODIFIER_CTRL) == 0) // not holding control
                    {
                        // remove the pogs that we moved
                        m_canvas.removePog(pog.getId(), false);
                    }
                }
            });
            menu.add(item);
            item = new JMenuItem("Set Name...");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    final String s = (String)JOptionPane.showInputDialog(GametableFrame.getGametableFrame(),
                        "Enter new name for this Pog:", "Set Pog Name", JOptionPane.PLAIN_MESSAGE, null, null, m_menuPog.getText());

                    if (s != null)
                    {
                        m_canvas.setPogData(m_menuPog.getId(), s, null, null);
                    }

                }
            });
            menu.add(item);
            item = new JMenuItem("Set Attribute");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    final SetPogAttributeDialog dialog = new SetPogAttributeDialog(false);
                    dialog.setLocationRelativeTo(m_canvas);
                    dialog.setVisible(true);               
                   
                    if(dialog.isConfirmed()) {
                        final Map toAdd = dialog.getAttribs();
                        m_canvas.setPogData(m_menuPog.getId(), null, toAdd, null);
                    }
                }
            });
            menu.add(item);
            if (m_menuPog.getAttributeNames().size() > 0)
            {
                final JMenu editMenu = new JMenu("Edit Attribute");

                final JMenu removeMenu = new JMenu("Remove Attribute");
                final Set nameSet = m_grabbedPog.getAttributeNames();
                for (final Iterator iterator = nameSet.iterator(); iterator.hasNext();)
                {
                    final String key = (String)iterator.next();
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

            // -------------------------------------
            // Copy Pog
                menu.addSeparator();
                item = new JMenuItem("Copy Pog...");
                //item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
                item.addActionListener(new ActionListener()
                {
                    public void actionPerformed(final ActionEvent e)
                    {
                        final Pog pog = m_menuPog;
                            GametableFrame.getGametableFrame().copyPog(pog);
                      
                    }
                });

          
                menu.add(item);           
            // -------------------------------------
            // Save Pog
           
                item = new JMenuItem("Save Pog...");
                //item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
                item.addActionListener(new ActionListener()
                {
                    public void actionPerformed(final ActionEvent e)
                    {
                        final Pog pog = m_menuPog;
                        final File spaf = UtilityFunctions.doFileSaveDialog("Save As", "pog", true);
                        if (spaf != null)
                        {
                            GametableFrame.getGametableFrame().savePog(spaf, pog);
                        }                       
                    }
                });
                menu.add(item);
                
                // -------------------------------------
                // Pog Layers
                int layer = m_menuPog.getLayer();
                JMenu m_item = new JMenu("Change Layer");
                item = new JMenuItem("Underlay");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        m_canvas.setPogLayer(m_menuPog.getId(), Pog.LAYER_UNDERLAY);
                    }
                });
                if(layer == Pog.LAYER_UNDERLAY) item.setEnabled(false);
                
                m_item.add(item);
                item = new JMenuItem("Overlay");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        m_canvas.setPogLayer(m_menuPog.getId(), Pog.LAYER_OVERLAY);
                    }
                });
                if(layer == Pog.LAYER_OVERLAY) item.setEnabled(false);
                m_item.add(item);
                item = new JMenuItem("Environment");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        m_canvas.setPogLayer(m_menuPog.getId(), Pog.LAYER_ENV);
                    }
                });
                if(layer == Pog.LAYER_ENV) item.setEnabled(false);
                m_item.add(item);
                item = new JMenuItem("Pog");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        m_canvas.setPogLayer(m_menuPog.getId(), Pog.LAYER_POG);
                    }
                });
                if(layer == Pog.LAYER_POG) item.setEnabled(false);
                m_item.add(item);
                menu.add(m_item);                    
                
                menu.addSeparator();
            // -------------------------------------
                
            final JMenu sizeMenu = new JMenu("Face Size");
            item = new JMenuItem("Reset");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    m_canvas.setPogSize(m_menuPog.getId(), -1);
                }
            });
            sizeMenu.add(item);

            item = new JMenuItem("0.5 squares");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    m_canvas.setPogSize(m_menuPog.getId(), 0.5f);
                }
            });
            sizeMenu.add(item);

            item = new JMenuItem("1 squares");
            item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(final ActionEvent e)
                 {
                     m_canvas.setPogSize(m_menuPog.getId(), 1);
                 }
             });
             sizeMenu.add(item);

             item = new JMenuItem("2 squares");
             item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(final ActionEvent e)
                 {
                     m_canvas.setPogSize(m_menuPog.getId(), 2);
                 }
             });
             sizeMenu.add(item);

             item = new JMenuItem("3 squares");
             item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(final ActionEvent e)
                 {
                     m_canvas.setPogSize(m_menuPog.getId(), 3);
                 }
             });
             sizeMenu.add(item);

             item = new JMenuItem("4 squares");
             item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(final ActionEvent e)
                 {
                     m_canvas.setPogSize(m_menuPog.getId(), 4);
                 }
             });
             sizeMenu.add(item);

             item = new JMenuItem("6 squares");
             item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(final ActionEvent e)
                 {
                     m_canvas.setPogSize(m_menuPog.getId(), 6);
                 }
             });
             sizeMenu.add(item);

             menu.add(sizeMenu);

              final JMenu rotateMenu = new JMenu("Rotation");

              JCheckBoxMenuItem item2 = new JCheckBoxMenuItem("Force grid snap");
              if (!m_menuPog.getForceGridSnap()) {
                  item2.setState(false);
              } else {
                  item2.setState(true);
              }
              
              item2.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      if (!m_menuPog.getForceGridSnap()) {
                          m_canvas.forceGridSnapPog(m_menuPog.getId(), true);
                      } else {
                          m_canvas.forceGridSnapPog(m_menuPog.getId(), false);
                      }
                  }
              });
              rotateMenu.add(item2);
              rotateMenu.addSeparator();
              
              item = new JMenuItem("0");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 0);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("60");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 60);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("90");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 90);
                  }
              });

              rotateMenu.add(item);
              item = new JMenuItem("120");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 120);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("180");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 180);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("240");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 240);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("270");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 270);
                  }
              });
              rotateMenu.add(item);

              item = new JMenuItem("300");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.rotatePog(m_menuPog.getId(), 300);
                  }
              });
              rotateMenu.add(item);

              menu.add(rotateMenu);
              
              final JMenu flipMenu = new JMenu("Flip");
              item = new JMenuItem("Reset");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.flipPog(m_menuPog.getId(), 0, 0);
                  }
              });
              flipMenu.add(item);

              item = new JMenuItem("Vertical");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.flipPog(m_menuPog.getId(), (m_menuPog.getFlipH() == 0 ? 1 : m_menuPog.getFlipH()), (m_menuPog.getFlipV() == 0 ? -1 : -(m_menuPog.getFlipV())));
                  }
              });
              flipMenu.add(item);

              item = new JMenuItem("Horizontal");
              item.addActionListener(new ActionListener()
              {
                  public void actionPerformed(final ActionEvent e)
                  {
                      m_canvas.flipPog(m_menuPog.getId(), (m_menuPog.getFlipH() == 0 ? -1 : -(m_menuPog.getFlipH())), (m_menuPog.getFlipV() == 0 ? 1 : m_menuPog.getFlipV()));
                  }
              });
              flipMenu.add(item);

              menu.add(flipMenu);
              
              // --------------------------------
              if(layer == Pog.LAYER_UNDERLAY) {
                  menu.addSeparator();
                  item = new JMenuItem("Set as Background");
                  item.addActionListener(new ActionListener()
                  {
                      public void actionPerformed(final ActionEvent e)
                      {
                          final int result = UtilityFunctions.yesNoDialog(GametableFrame.getGametableFrame(),
                              "Are you sure you wish to change the background to this pog's Image?", "Change Background?");
                          if (result == UtilityFunctions.YES)
                              m_canvas.changeBackgroundCP(m_menuPog.getId(), true);                        
                      }
                  });
                  menu.add(item);
              }
        }
        final Point mousePosition = m_canvas.modelToView(x, y);
        menu.show(m_canvas, mousePosition.x, mousePosition.y);
    }

    /**
     * Sets the snapping status based on the specified modifiers.
     * 
     * @param modifierMask the set of modifiers passed into the event.
     */
    private void setSnapping(final int modifierMask)
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
}
