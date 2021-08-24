package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.util.Colors;

public class RightClickOption extends IDEComponent {
	
	private String text;
	private String clickArg;
	private ExecuteCommand command;
	
	private int textSize;
	
	private static final int HEIGHT = 30;

	public RightClickOption(int x, int y, int width, String text, ExecuteCommand command, String clickArg) {
		super(x, y, width, HEIGHT, null);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = 20;
	}
	
	public RightClickOption(int x, int y, int width, int height, int textSize, String text, ExecuteCommand command, String clickArg) {
		super(x, y, width, height, null);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = textSize;
	}
	
	public RightClickOption(int x, int y, int width, int height, int textSize, String text, BufferedImage icon, ExecuteCommand command, String clickArg) {
		super(x, y, width, height, icon);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = textSize;
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
		Color c = hovered() ? Colors.explorerLight : Colors.background2;
		
		g.setColor(c);
		g.fillRect(x, y, width, HEIGHT);
		
		Fonts.drawString(text, x + 2, y + 2, new IDEFont(Fonts.lightGrayNormal, textSize), g);
		
		g.drawImage(sprite, (x + width) - 18, y + 2, 16, 16, null); // 18 pq se o sprite é 16, fica com 2 de espaço
	}
}
