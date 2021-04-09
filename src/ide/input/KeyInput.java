package ide.input;

import java.awt.event.*;

public final class KeyInput extends KeyAdapter {
    
    private static char charPressed;
    private static boolean keyPressed;
    private static int keyCodePressed;
    
    private static boolean controlDown;
    private static boolean shiftDown;
    private static boolean altDown;
    private static boolean altGrDown;

    public static boolean isControlDown() {
		return controlDown;
	}
    
    public static boolean isShiftDown() {
    	return shiftDown;
    }

	public static boolean isAltDown() {
		return altDown;
	}

	public static boolean isAltGrDown() {
		return altGrDown;
	}

	public static char getCharPressed() {
        return charPressed;
    }

    public static boolean isKeyPressed() {
        return keyPressed;
    }

    public static int getKeyCodePressed() {
        return keyCodePressed;
    }
    
    public static void updateKeys() {
    	keyPressed = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed = true;

        charPressed = e.getKeyChar();
        keyCodePressed = e.getKeyCode();
        
        controlDown = e.isControlDown();
        shiftDown = e.isShiftDown();
        altDown = e.isAltDown();
        altGrDown = e.isAltGraphDown();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed = false;

        charPressed = e.getKeyChar();
        keyCodePressed = e.getKeyCode();
        
        controlDown = e.isControlDown();
        shiftDown = e.isShiftDown();
        altDown = e.isAltDown();
        altGrDown = e.isAltGraphDown();
    }
}
