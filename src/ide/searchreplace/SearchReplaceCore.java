package ide.searchreplace;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.main.Main;
import ide.util.Texts;

public final class SearchReplaceCore {

	private SearchReplaceCore() {}
	
	public static synchronized void init() {
		IDEComponent.toAdd.add(new BackButton(20, 20, 24, 24, Main.back));
		IDEComponent.toAdd.add(new InputBox(20, 100, Main.explorer.getWidth() - 40, 20));
		IDEComponent.toAdd.add(new InputBox(20, 170, Main.explorer.getWidth() - 40, 20));
		
		IDEComponent.toAdd.add(new ToggleButton(20, 210, 32, 32, Main.caseSensitive, false, Texts.caseSensitive, 220, 430));
	}
	
	public static synchronized void dispose() {
		Explorer.selected = null;
		
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof BackButton || i instanceof InputBox || i instanceof ToggleButton)
				IDEComponent.toRemove.add(i);
		}
	}
}
