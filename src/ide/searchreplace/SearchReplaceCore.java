package ide.searchreplace;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.main.Main;

public final class SearchReplaceCore {

	private SearchReplaceCore() {}
	
	public static synchronized void init() {
		IDEComponent.toAdd.add(new BackButton(20, 20, 24, 24, Main.spritesheet.getSprite(168, 0, 8, 8)));
		IDEComponent.toAdd.add(new InputBox(20, 100, Main.explorer.getWidth() - 40, 20));
	}
	
	public static synchronized void dispose() {
		Explorer.selected = null;
		
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof BackButton || i instanceof InputBox)
				IDEComponent.toRemove.add(i);
		}
	}
}
