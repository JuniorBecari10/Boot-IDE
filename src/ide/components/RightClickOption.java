package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.util.Colors;

public class RightClickOption extends IDEComponent {
	
	private String text;
	private String clickArg;
	private ExecuteCommand command;
	
	private static final int HEIGHT = 30;

	public RightClickOption(int x, int y, int width, String text, ExecuteCommand command, String clickArg) {
		super(x, y, width, HEIGHT, null);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
	}
	
	/**
	 * Detecta se tem alguma opção de RightClick aberta.
	 * 
	 * @return <tt>true</tt> se sim, <tt>false</tt> se não.
	 */
	public static boolean isRightClickActive() {
		for (IDEComponent i : IDEComponent.components)
			if (i instanceof RightClickOption)
				return true;
		
		return false;
	}
	
	public static boolean anyRightClickOptionHovered() {
		for (IDEComponent i : IDEComponent.components)
			if (i instanceof RightClickOption)
				if (i.hovered()) return true;
		
		return false;
	}
	
	public static synchronized void removeAllRightClickOptions() {
		for (IDEComponent i : IDEComponent.components)
			if (i instanceof RightClickOption)
				IDEComponent.toRemove.add(i);
	}
	
	public void tick() {		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE || (MouseInput.isMousePressed() && !(leftClicked() || rightClicked()))) // obs: o bug não é aqui
			IDEComponent.toRemove.add(this);
		
		if (leftClicked()) {
			MouseInput.updateMouse(); // resolver o bug de clicar com o botão direito e abrir e fechar as options
			
			command.execute(clickArg);
			
			for (IDEComponent i : IDEComponent.components)
				if (i instanceof RightClickOption)
					IDEComponent.toRemove.add(i);
		}
	}
	
	public void render(Graphics g) {
		Color c = hovered() ? Colors.explorerLight : Color.decode("#29394a");
		
		g.setColor(c);
		g.fillRect(x, y, width, HEIGHT);
		
		Fonts.drawString(text, x + 2, y + 2, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
