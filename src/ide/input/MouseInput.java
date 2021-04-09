package ide.input;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public final class MouseInput extends MouseInputAdapter {
    
    private static int mouseX, mouseY;

    private static boolean leftPressed, rightPressed;
    
    private static MouseWheelRoll roll;
    
    private static boolean mousePressed;
    private static boolean mouseDragged;
    private static boolean mouseClicked;
    
    private static boolean mouseRolled;
    
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
    
    /**
     * Use esse método sempre depois de usar o clicked() ou dragged().
     */
    public static void updateMouse() {
    	mousePressed = false;
    	leftPressed = false;
    	rightPressed = false;
    	
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
	
	public static boolean wheelUp() {
		return roll == MouseWheelRoll.UP;
	}
	
	public static boolean wheelDown() {
		return roll == MouseWheelRoll.DOWN;
	}
	
	public static boolean isMouseRolling() {
		return mouseRolled;
	}

	@Override
    public void mouseDragged(MouseEvent e) {
        mousePressed = true;
        mouseDragged = true;

        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        mousePressed = false;
        mouseClicked = false;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        mouseClicked = true;

        mouseX = e.getX();
        mouseY = e.getY();
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
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    	mouseRolled = true;
    	
    	int notches = e.getWheelRotation();
    	
    	if (notches < 0) roll = MouseWheelRoll.UP;
    	else roll = MouseWheelRoll.DOWN;
    }
}
