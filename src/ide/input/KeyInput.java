package ide.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import ide.main.Main;

public final class KeyInput extends KeyAdapter {
	
	/*public static Queue<Integer> keyCodes = new LinkedList<>();
	public static Queue<Character> chars = new LinkedList<>();*/
    
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
        //return !chars.isEmpty() ? chars.remove() : charPressed;
		return charPressed;
    }

    public static boolean isKeyPressed() {
        return keyPressed;
    }

    public static int getKeyCodePressed() {
        //return !keyCodes.isEmpty() ? keyCodes.remove() : keyCodePressed;
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
        
        Main.hasInput = true;
        
        /*if (Character.isLetterOrDigit(charPressed)) {
	        keyCodes.add(keyCodePressed);
	        chars.add(charPressed);
        }*/
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed = false;

        charPressed = e.getKeyChar();
        keyCodePressed = -1;
        
        controlDown = e.isControlDown();
        shiftDown = e.isShiftDown();
        altDown = e.isAltDown();
        altGrDown = e.isAltGraphDown();
        
        Main.hasInput = true;
    }
}
