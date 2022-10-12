package ide.input;

import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ide.main.Main;
import ide.screen.Screen;
import ide.topcomponents.MaximizeWindow;
import ide.topcomponents.TopComponent;

public final class MouseInput extends MouseInputAdapter {
    
    private static int mouseX, mouseY;

    private static boolean leftPressed, rightPressed;
    
    private static MouseWheelRoll roll;
    
    private static boolean mousePressed;
    private static boolean mouseDragged;
    private static boolean mouseClicked;
    private static boolean mouseMoved;
    
    private static boolean leftDragged;
    private static boolean rightDragged;
    
    private static boolean mouseRolled;
    
    private static int pX = 0, pY = 0;
    private Rectangle screenBounds;
    
    public static final int DRAG_BOUNDS = 5;
    
    public static Map<Boolean, String> mousePoint;
    public static String direction;
    
    public static boolean hovered(int x, int y, int w, int h) {
    	Rectangle bs = new Rectangle(x, y, w, h);
    	
    	return bs.intersects(getMouseBounds());
    }
    
    public static Rectangle getMouseBounds() {
    	return new Rectangle(mouseX, mouseY, 1, 1);
    }

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static boolean isMousePressed() {
        return mousePressed;
    }
    
    public static void updateMouse() {
    	mousePressed = false;
    	leftPressed = false;
    	rightPressed = false;
    	
    	mouseRolled = false;
    	//mouseMoved = false; // olha o comentario
    }
    
    public static void updateMouseRoll() {
    	mouseRolled = false;
    }

    public static boolean isMouseDragged() {
        return mouseDragged;
    }
    
    public static boolean isMouseClicked() {
		return mouseClicked;
	}

    public static boolean isLeftPressed() {
		return leftPressed;
	}

	public static boolean isRightPressed() {
		return rightPressed;
	}
	
	public static MouseWheelRoll getWheelRoll() {
		return roll;
	}
	
	public static boolean mouseMoved() {
		return mouseMoved;
	}
	
	public static boolean wheelUp() {
		return roll == MouseWheelRoll.UP && mouseRolled;
	}
	
	public static boolean wheelDown() {
		return roll == MouseWheelRoll.DOWN && mouseRolled;
	}
	
	public static boolean isMouseRolling() {
		return mouseRolled;
	}
	
	public static boolean leftDragged() {
		return leftDragged;
	}
	
	public static boolean rightDragged() {
		return rightDragged;
	}
	
	public static Point getMouseLocation() {
		int x = MouseInfo.getPointerInfo().getLocation().x - Main.screen.frame.getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y - Main.screen.frame.getLocation().y;
		
		return new Point(x, y);
	}

	@Override
    public void mouseDragged(MouseEvent e) {
        //mousePressed = true;
        mouseDragged = true;
        
        if (SwingUtilities.isLeftMouseButton(e))
    		leftDragged = true;
    	
    	if (SwingUtilities.isRightMouseButton(e))
    		rightDragged = true;
        
        mouseMoved = true;
        
        Point p = getMouseLocation();
        
        mouseX = e.getX();
        mouseY = e.getY();
        
        if ((mouseY < Screen.DECORATION_HEIGHT || ComponentInput.windowMoved()) && !TopComponent.anyTopComponentHovered()) {
        	Main.screen.frame.setLocation(Main.screen.frame.getLocation().x + p.x - pX, Main.screen.frame.getLocation().y + p.y - pY);
        	Main.screen.frame.setExtendedState(JFrame.NORMAL);
        }
        
        if (Main.main != null)
	        Main.main.mainLogic();
        
        if (!Main.screen.frame.getCursor().equals(Cursor.getDefaultCursor())) {

            switch (direction) {
            case "N":
                if (e.getYOnScreen() > Main.screen.frame.getY()) {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() - (e.getYOnScreen() - Main.screen.frame.getY()));
                } else {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() + (Main.screen.frame.getY() - e.getYOnScreen()));
                }
                break;
            case "E":
                Main.screen.frame.setBounds(Main.screen.frame.getX(), Main.screen.frame.getY(), e.getX(), Main.screen.frame.getHeight());
                break;
            case "S":
                Main.screen.frame.setBounds(Main.screen.frame.getX(), Main.screen.frame.getY(), Main.screen.frame.getWidth(), e.getY());
                break;
            case "W":
            	Main.screen.frame.setBounds(e.getXOnScreen(), Main.screen.frame.getY(), (int) (screenBounds.getWidth() - Math.abs(e.getXOnScreen() - pX)), Main.screen.frame.getHeight());
                break;
            case "NE":
                Main.screen.frame.setBounds(Main.screen.frame.getX(), Main.screen.frame.getY(), e.getX(), Main.screen.frame.getHeight());

                if (e.getYOnScreen() > Main.screen.frame.getY()) {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() - (e.getYOnScreen() - Main.screen.frame.getY()));
                } else {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() + (Main.screen.frame.getY() - e.getYOnScreen()));
                }
                break;
            case "SE":
                Main.screen.frame.setBounds(Main.screen.frame.getX(), Main.screen.frame.getY(), e.getX(), e.getY());
                break;
            case "SW":
                Main.screen.frame.setBounds(Main.screen.frame.getX(), Main.screen.frame.getY(), Main.screen.frame.getWidth(), e.getY());
                if (e.getXOnScreen() > Main.screen.frame.getX()) {
                    Main.screen.frame.setBounds(e.getXOnScreen(), Main.screen.frame.getY(), Main.screen.frame.getWidth() - (e.getXOnScreen() - Main.screen.frame.getX()), Main.screen.frame.getHeight());
                } else {
                    Main.screen.frame.setBounds(e.getXOnScreen(), Main.screen.frame.getY(), Main.screen.frame.getWidth() + (Main.screen.frame.getX() - e.getXOnScreen()), Main.screen.frame.getHeight());
                }
                break;
            case "NW":
                if (e.getYOnScreen() > Main.screen.frame.getY()) {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() - (e.getYOnScreen() - Main.screen.frame.getY()));
                } else {
                    Main.screen.frame.setBounds(Main.screen.frame.getX(), e.getYOnScreen(), Main.screen.frame.getWidth(), Main.screen.frame.getHeight() + (Main.screen.frame.getY() - e.getYOnScreen()));
                }
                if (e.getXOnScreen() > Main.screen.frame.getX()) {
                    Main.screen.frame.setBounds(e.getXOnScreen(), Main.screen.frame.getY(), Main.screen.frame.getWidth() - (e.getXOnScreen() - Main.screen.frame.getX()), Main.screen.frame.getHeight());
                } else {
                    Main.screen.frame.setBounds(e.getXOnScreen(), Main.screen.frame.getY(), Main.screen.frame.getWidth() + (Main.screen.frame.getX() - e.getXOnScreen()), Main.screen.frame.getHeight());
                }
                break;
            }
        }
    }
	
	@Override
	public void mouseExited(MouseEvent e) {
		mouseMoved = false;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		//mouseMoved = true;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        mousePressed = false;
        mouseClicked = false;
        
        mouseMoved = true;
        
        if (Main.main != null)
	        Main.main.mainLogic();
        
        mousePoint = new HashMap<Boolean, String>();
        mousePoint.put(e.getY() < DRAG_BOUNDS, "N");
        mousePoint.put(e.getX() > (Main.screen.frame.getWidth() - DRAG_BOUNDS), "E");
        mousePoint.put(e.getY() > (Main.screen.frame.getHeight() - DRAG_BOUNDS), "S");
        mousePoint.put(e.getX() < DRAG_BOUNDS, "W");
        mousePoint.put(e.getY() < DRAG_BOUNDS && e.getX() > (Main.screen.frame.getWidth() - DRAG_BOUNDS), "NE");
        mousePoint.put(e.getY() > (Main.screen.frame.getHeight() - DRAG_BOUNDS) && e.getX() > (Main.screen.frame.getWidth() - DRAG_BOUNDS), "SE");
        mousePoint.put(e.getY() > (Main.screen.frame.getHeight() - DRAG_BOUNDS) && e.getX() <= DRAG_BOUNDS, "SW");
        mousePoint.put(e.getY() < DRAG_BOUNDS && e.getX() < DRAG_BOUNDS, "NW");

        for (Entry<Boolean, String> item : mousePoint.entrySet()) {
            if (item.getKey()) {
                direction = item.getValue();
                switch (item.getValue()) {
                case "N":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    break;
                case "E":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    break;
                case "S":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    break;
                case "W":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    break;
                case "NE":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    break;
                case "SE":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    break;
                case "SW":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    break;
                case "NW":
                	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    break;
                }
            } else {
            	Main.screen.frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        mouseClicked = true;

        mouseX = e.getX();
        mouseY = e.getY();
        
        if (Main.main != null)
	        Main.main.mainLogic();
        
        if (e.getClickCount() == 2 && mouseY < Screen.DECORATION_HEIGHT) {
        	MaximizeWindow.maximize();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    	if (SwingUtilities.isLeftMouseButton(e))
    		leftPressed = true;
    	
    	if (SwingUtilities.isRightMouseButton(e))
    		rightPressed = true;
    	
        mousePressed = true;

        mouseX = e.getX();
        mouseY = e.getY();
        
        pX = e.getX();
		pY = e.getY();
		
		screenBounds = Main.screen.frame.getBounds();
		
		if (Main.maximizeWindow.leftClicked())
			MaximizeWindow.maximize();
		
        if (Main.main != null)
	        Main.main.mainLogic();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
        mouseClicked = false;
        mouseDragged = false;
        
        if (SwingUtilities.isLeftMouseButton(e))
    		leftPressed = false;
    	
    	if (SwingUtilities.isRightMouseButton(e))
    		rightPressed = false;
    	
    	leftDragged = false;
    	rightDragged = false;
    	
    	if (Main.main != null)
	        Main.main.mainLogic();
    	
    	if (MouseInfo.getPointerInfo().getLocation().y == 0)
    		MaximizeWindow.maximize();
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    	mouseRolled = true;
    	
    	int notches = e.getWheelRotation();
    	
    	if (notches < 0) roll = MouseWheelRoll.UP;
    	else roll = MouseWheelRoll.DOWN;
    	
    	Main.editor.scroll();
		Main.editor.scrollTabs();
		
		if (Main.main != null)
	        Main.main.mainLogic();
    }
}
