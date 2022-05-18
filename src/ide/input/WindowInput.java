package ide.input;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import ide.main.Main;

public class WindowInput implements WindowListener {
	
	private static boolean activated = true;
	private static boolean closed;
	private static boolean closing;
	private static boolean deactivated;
	private static boolean maximized;
	private static boolean minimized;
	private static boolean opened;
	
	public static void update() {
		activated = false;
		closed = false;
		closing = false;
		deactivated = false;
		maximized = false;
		minimized = false;
		opened = false;
	}

	public static boolean isActivated() {
		return activated;
	}

	public static boolean isClosed() {
		return closed;
	}

	public static boolean isClosing() {
		return closing;
	}

	public static boolean isDeactivated() {
		return deactivated;
	}

	public static boolean isMaximized() {
		return maximized;
	}

	public static boolean isMinimized() {
		return minimized;
	}

	public static boolean isOpened() {
		return opened;
	}

	@Override
	public void windowActivated(WindowEvent e) {
		activated = true;
		deactivated = false;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		closed = true;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		closing = true;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		deactivated = true;
		activated = false;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		maximized = true;
		minimized = false;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		minimized = true;
		maximized = false;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		opened = true;
		
		if (Main.main != null)
	        Main.main.mainLogic();
	}
}
