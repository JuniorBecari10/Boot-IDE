package ide.searchreplace;

import ide.components.IDEComponent;
import ide.main.Main;

public final class SearchReplaceCore {

	private SearchReplaceCore() {}
	
	public static synchronized void init() {
		IDEComponent.toAdd.add(new BackButton(20, 20, 24, 24, Main.spritesheet.getSprite(168, 0, 8, 8)));
	}
	
	public static synchronized void dispose() {
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof BackButton)
				IDEComponent.toRemove.add(i);
		}
	}
}
