package ide.input;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import ide.main.Main;

public class ComponentInput extends ComponentAdapter {
	
	private static boolean moved;
	private static boolean resized;
	
	public static void update() {
		moved = false;
		resized = false;
	}
	
	public static boolean windowMoved() {
		return moved;
	}
	
	public static boolean windowResized() {
		return resized;
	}

    public void componentMoved(ComponentEvent evt) {
      moved = true;
      
      if (Main.main != null)
	        Main.main.mainLogic();
    }

    public void componentResized(ComponentEvent evt) {
      resized = true;
      
      if (Main.main != null)
	        Main.main.mainLogic();
    }
}
