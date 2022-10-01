package ide.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import ide.codeeditor.CodeEditor;
import ide.components.CommandTerminal;
import ide.components.MessageBox;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.SearchReplaceCore;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
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
        
        Main.editor.detectGlobalShortcuts();
        Main.editor.typeLogic();
        
        if (Explorer.explorerMode == ExplorerMode.SEARCHREPLACE) {
	    	if (Explorer.search == null || Explorer.replace == null || Explorer.caseSensitive == null || Explorer.regex == null || Explorer.entireDocument == null || Explorer.selectedLines == null || Explorer.searchNext == null || Explorer.replaceAll == null)
	    		SearchReplaceCore.initComponents();
	    	
	    	if (Explorer.entireDocument.getState() == false && Explorer.selectedLines.getState() == false)
	    		Explorer.entireDocument.setState(true);
	    	
	    	if (KeyInput.isKeyPressed() && KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB && !CommandTerminal.active && !MessageBox.active && !SetBranchName.added && !SetCommitName.added) {
	    		//KeyInput.updateKeys();
	    		
	    		if (Explorer.selected == Explorer.search) Explorer.selected = Explorer.replace;
	    		else Explorer.selected = Explorer.search;
	    	}
	    	
	    	if (Explorer.selected != null && KeyInput.isKeyPressed()) {
		    	if (KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
		    		//KeyInput.updateKeys();
		    		
		    		SearchReplaceCore.replaceAll(Explorer.search.getText(), Explorer.replace.getText(), Explorer.caseSensitive.getState(), Explorer.regex.getState(), Explorer.entireDocument.getState());
		    	}
		    	
		    	if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
		    		//KeyInput.updateKeys();
		    		
		    		SearchReplaceCore.searchNext(Explorer.search.getText(), Explorer.caseSensitive.getState(), Explorer.regex.getState(), Explorer.entireDocument.getState());
		    	}
		    	
		    	if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_S) { // Ctrl + S (Case Sensitive)
					//KeyInput.updateKeys();
					
					Explorer.caseSensitive.invertState();
					
					return;
				}
		    	
		    	if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_R) { // Ctrl + R (Regex)
					//KeyInput.updateKeys();
					
					Explorer.regex.invertState();
					
					return;
				}
		    	
		    	if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_E) { // Ctrl + E (Entire Document)
					//KeyInput.updateKeys();
					
					if (Main.editor.selecting) {
						if (Explorer.entireDocument.getState()) {
							Explorer.entireDocument.setState(false);
							Explorer.selectedLines.setState(true);
						}
						else {
							Explorer.entireDocument.setState(true);
							Explorer.selectedLines.setState(false);
						}
					}
					
					return;
				}
		    	
		    	if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Y) { // Ctrl + Shift + Y (Desselecionar a caixa Search)
					//KeyInput.updateKeys();
					
					Explorer.selected = null;
					
					return;
				}
	    	}
	    }
        
        if (Explorer.setFileName != null)
        	Explorer.setFileName.type();
        
        if (Explorer.renameFile != null)
        	Explorer.renameFile.type();
        
        if (CodeEditor.terminal != null)
        	CodeEditor.terminal.type();
        
        if (Explorer.selected != null)
        	Explorer.selected.type();
        
        if (Explorer.setBranchName != null)
        	Explorer.setBranchName.type();
        
        if (Explorer.setCommitName != null)
        	Explorer.setCommitName.type();
        
        /*if (Character.isLetterOrDigit(charPressed)) {
	        keyCodes.add(keyCodePressed);
	        chars.add(charPressed);
        }*/
        
        if (Main.main != null)
	        Main.main.mainLogic();
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
        
        if (Main.main != null)
	        Main.main.mainLogic();
    }
}
