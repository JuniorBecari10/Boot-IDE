package ide.searchreplace;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.main.Main;
import ide.util.Texts;

public final class SearchReplaceCore {

	private SearchReplaceCore() {}
	
	public static synchronized void init() {
		
		if (Explorer.search == null)
			Explorer.search = new InputBox(20, 100, Main.explorer.getWidth() - 40, 20);
		
		if (Explorer.replace == null)
			Explorer.replace = new InputBox(20, 170, Main.explorer.getWidth() - 40, 20);
		
		IDEComponent.toAdd.add(new BackButton(20, 20, 24, 24, Main.back));
		IDEComponent.toAdd.add(Explorer.search);
		IDEComponent.toAdd.add(Explorer.replace);
		
		IDEComponent.toAdd.add(new ExecuteButton(20, 260, Main.explorer.getWidth() - 40, 20, Texts.searchNext));
		IDEComponent.toAdd.add(new ExecuteButton(20, 300, Main.explorer.getWidth() - 40, 20, Texts.replaceAll));
		
		IDEComponent.toAdd.add(new ToggleButton(20, 210, 32, 32, Main.caseSensitive, false, Texts.caseSensitive, 220, 430)); // fica por último
		
		Explorer.selected = Explorer.search;
	}
	
	public static synchronized void dispose() {
		Explorer.selected = null;
		
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof BackButton || i instanceof InputBox || i instanceof ToggleButton || i instanceof ExecuteButton)
				IDEComponent.toRemove.add(i);
		}
	}
}
