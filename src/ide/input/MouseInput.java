package ide.input;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ide.main.Main;
import screen.Screen;
import topcomponents.MaximizeWindow;

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
    
    public static boolean hovered(int x, int y, int w, int h) {
    	Rectangle bs = new Rectangle(x, y, w, h);
    	Rectangle ms = new Rectangle(mouseX, mouseY, 1, 1);
    	
    	return bs.intersects(ms);
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
    	//mouseMoved = false; // olha o coment�rio
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
        
        if (mouseY < Screen.DECORATION_HEIGHT || ComponentInput.windowMoved()) {
        	Main.screen.frame.setLocation(Main.screen.frame.getLocation().x + p.x - pX, Main.screen.frame.getLocation().y + p.y - pY);
        	Main.screen.frame.setExtendedState(JFrame.NORMAL);
        }
        
        if (Main.main != null)
	        Main.main.mainLogic();
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
