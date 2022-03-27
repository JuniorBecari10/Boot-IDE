package ide.searchreplace;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.IDELine;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.main.Main;
import ide.util.Texts;

public final class SearchReplaceCore {

	private SearchReplaceCore() {}
	
	public static synchronized void init() {
		initComponents();
		
		IDEComponent.toAdd.add(new BackButton(20, 20, 24, 24, Main.back));
		IDEComponent.toAdd.add(Explorer.search);
		IDEComponent.toAdd.add(Explorer.replace);
		
		IDEComponent.toAdd.add(Explorer.searchNext);
		IDEComponent.toAdd.add(Explorer.replaceAll);
		
		IDEComponent.toAdd.add(Explorer.entireDocument);
		IDEComponent.toAdd.add(Explorer.selectedLines);
		
		IDEComponent.toAdd.add(Explorer.caseSensitive); // fica por último
		
		Explorer.selected = Explorer.search;
	}
	
	public static synchronized void initComponents() {
		if (Explorer.search == null)
			Explorer.search = new InputBox(20, 100, Main.explorer.getWidth() - 40, 20);
		
		if (Explorer.replace == null)
			Explorer.replace = new InputBox(20, 170, Main.explorer.getWidth() - 40, 20);
		
		if (Explorer.entireDocument == null)
			Explorer.entireDocument = new RadioButton(Main.explorer.getWidth() - 100, 210, 32, 32, Main.entireDocument, true, Texts.entireDocument, 240, 270, true);
		
		if (Explorer.selectedLines == null)
			Explorer.selectedLines = new RadioButton(Main.explorer.getWidth() - 62, 210, 32, 32, Main.selectedLines, false, Texts.selectedLines, 225, 300, false);
		
		if (Explorer.caseSensitive == null)
			Explorer.caseSensitive = new ToggleButton(20, 210, 32, 32, Main.caseSensitive, false, Texts.caseSensitive, 220, 430);
		
		if (Explorer.searchNext == null)
			Explorer.searchNext = new ExecuteButton(20, 260, Main.explorer.getWidth() - 40, 20, Texts.searchNext, () -> searchNext(Explorer.search.getText(), Explorer.caseSensitive.getState(), Explorer.entireDocument.getState()));
		
		if (Explorer.replaceAll == null)
			Explorer.replaceAll = new ExecuteButton(20, 300, Main.explorer.getWidth() - 40, 20, Texts.replaceAll, () -> replaceAll(Explorer.search.getText(), Explorer.replace.getText(), Explorer.caseSensitive.getState(), Explorer.entireDocument.getState()));
	}
	
	public static synchronized void dispose() {
		Explorer.selected = null;
		
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof BackButton || i instanceof InputBox || i instanceof ToggleButton || i instanceof ExecuteButton || i instanceof RadioButton)
				IDEComponent.toRemove.add(i);
		}
	}
	
	public static void searchNext(String searchText, boolean caseSensitive, boolean isEntireDocument) {
		int occurnum = 0;
		if (searchText.equals("")) return;
		
		List<Integer> linesfound = new ArrayList<>();
		
		if (isEntireDocument) { // se não é selectedlines...
			for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
				IDELine l = Main.editor.lines.get(i);
				String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
				
				String text = caseSensitive ? searchText : searchText.toLowerCase();
				
				if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
			}
		}
		else {
			for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
				IDELine l = Main.editor.lines.get(i);
				String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
				
				String text = caseSensitive ? searchText : searchText.toLowerCase();
				
				if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
			}
		}
		
		if (linesfound.size() == 0) {
			CodeEditor.setSystemLook();
			JOptionPane.showMessageDialog(null, Texts.cannotFindWord, Texts.nothingFound, JOptionPane.WARNING_MESSAGE);
			
			return;
		}
		
		try {
			Main.editor.cursorY = (linesfound.get(occurnum) - 1) + 2;
		} catch (IndexOutOfBoundsException f) {
			occurnum = 0;
			
			Main.editor.cursorY = (linesfound.get(occurnum) - 1) + 2;
			CommandTerminal.runCommand("gotocursor");
			
			CodeEditor.setSystemLook();
			JOptionPane.showMessageDialog(null, Texts.didNotFindAfterThat, Texts.itsTheEnd, JOptionPane.INFORMATION_MESSAGE);
			
		}
		
		occurnum++;
		
		CommandTerminal.runCommand("gotocursor");
		Explorer.selected = null;
	}
	
	public static void replaceAll(String searchText, String replaceText, boolean caseSensitive, boolean isEntireDocument) {
		if (Main.editor.isReadOnly) return;
		if (searchText.equals("")) return;
		
		List<Integer> linesfound = new ArrayList<>();
		
		String text = caseSensitive ? searchText : searchText.toLowerCase();
		String replText = replaceText;
		
		if (isEntireDocument) {
			for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
				IDELine l = Main.editor.lines.get(i);
				String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
				
				if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
			}
		}
		else {
			for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
				IDELine l = Main.editor.lines.get(i);
				String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
				
				if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
			}
		}
		
		if (linesfound.size() == 0) {
			CodeEditor.setSystemLook();
			JOptionPane.showMessageDialog(null, Texts.cannotFindWord, Texts.nothingFound, JOptionPane.WARNING_MESSAGE);
			
			return;
		}
		
		int count = 0;
		
		for (Integer i : linesfound) {
			String s = new String(CodeEditor.toCharArray(Main.editor.lines.get(i).getChars()));
			
			s = s.replaceAll(text, replText);
			
			Main.editor.register(new StringBuilder(s), i);
			
			count++;
		}
		
		/*int i = 0;
		for (IDELine l : Main.editor.lines) {
			String s = new String(CodeEditor.toCharArray(l.getChars()));
			String replText = txbReplace.getText();
			
			String text = chkCaseSensitive.isSelected() ? searchText : searchText.toLowerCase();
			
			s = s.replaceAll(text, replText);
			
			Main.editor.register(new StringBuilder(s), i++);
		}*/
		
		Main.editor.editing.setSaved(false);
		Main.editor.undo.push(new ArrayList<>(Main.editor.getLines()));
		
		CommandTerminal.runCommand("gotocursor");
		CommandTerminal.runCommand("deselect");
		
		Main.screen.requestFocus();
		
		Main.editor.callAutomaticColor();
		Explorer.selected = null;
		
		CodeEditor.setSystemLook();
		JOptionPane.showMessageDialog(null, Texts.replaced + " " + Texts.occurences + " " + Texts.in +  " " + count + " " + Texts.lines + ".", Texts.success + "!", JOptionPane.INFORMATION_MESSAGE);
	}
}
