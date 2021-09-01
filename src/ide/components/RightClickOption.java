package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class RightClickOption extends IDEComponent {
	
	private String text;
	public String clickArg;
	public ExecuteCommand command;
	
	private int textSize;
	
	public boolean isAutoComplete;
	
	private static final int HEIGHT = 30;

	public RightClickOption(int x, int y, int width, String text, ExecuteCommand command, String clickArg) {
		super(x, y, width, HEIGHT, null);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = 20;
		
		isAutoComplete = false;
	}
	
	public RightClickOption(int x, int y, int width, int height, int textSize, String text, ExecuteCommand command, String clickArg) {
		super(x, y, width, height, null);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = textSize;
		
		isAutoComplete = true;
	}
	
	public RightClickOption(int x, int y, int width, int height, int textSize, String text, BufferedImage icon, ExecuteCommand command, String clickArg) {
		super(x, y, width, height, icon);

		this.text = text;
		this.command = command;
		this.clickArg = clickArg;
		
		this.textSize = textSize;
		
		isAutoComplete = true;
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
	
	public static boolean isAutoCompleteActive() {
		for (RightClickOption r : Main.editor.autocompletes)
			if (r.isAutoComplete) return true;
		
		return false;
	}
	
	public static boolean anyRightClickOptionHovered() {
		for (IDEComponent i : IDEComponent.components)
			if (i instanceof RightClickOption)
				if (i.hovered()) return true;
		
		
		for (RightClickOption r : Main.editor.autocompletes)
			if (r.isAutoComplete)
				if (r.hovered()) return true;
		
		return false;
	}
	
	public static synchronized void removeAllRightClickOptions() {
		for (IDEComponent i : IDEComponent.components)
			if (i instanceof RightClickOption)
				IDEComponent.toRemove.add(i);
		
		for (RightClickOption r : Main.editor.autocompletes)
			if (r.isAutoComplete)
				Main.editor.toRemoveAutoCompletes.add(r);
		
		Main.editor.autocompletescroll = 0;
	}
	
	public static int getRightClickIndex(List<RightClickOption> list, RightClickOption obj) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(obj)) return i;
		}
		
		return -1;
	}
	
	public void tick() {
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE || (MouseInput.isMousePressed() && !(leftClicked() || rightClicked()))) // obs: o bug não é aqui
			IDEComponent.toRemove.add(this);
		
		if (isAutoComplete)
			x = Main.editor.drawcx - Main.editor.scrX;
		
		//if (rightClicked()) removeAllRightClickOptions();
		
		if (leftClicked() || rightClicked()) {
			MouseInput.updateMouse(); // resolver o bug de clicar com o botão direito e abrir e fechar as options
			
			command.execute(clickArg);
			
			for (IDEComponent i : IDEComponent.components)
				if (i instanceof RightClickOption)
					IDEComponent.toRemove.add(i);
			
			if (isAutoComplete) {
				for (RightClickOption r : Main.editor.autocompletes)
					Main.editor.toRemoveAutoCompletes.add(r);
			}
		}
		
		/*if (Main.editor.autocompletes.indexOf(this) == Main.editor.autocompleteindex && KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
			command.execute(clickArg);
			
			for (IDEComponent i : IDEComponent.components)
				if (i instanceof RightClickOption)
					IDEComponent.toRemove.add(i);
			
			if (isAutoComplete) {
				for (RightClickOption r : Main.editor.autocompletes)
					Main.editor.toRemoveAutoCompletes.add(r);
			}
		}*/
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Color c = hovered() ? Colors.explorerLight : Colors.background2;
		Color d = c;
		
		//System.out.println(listRightClicks(true).indexOf(this)); // terminar
		
		if (isAutoComplete)
			c = Main.editor.autocompletes.indexOf(this) == Main.editor.autocompleteindex ? Colors.explorerLight : d;
		
		g.setColor(c);
		g.fillRect(x, y, width, HEIGHT);
		
		Fonts.drawString(isAutoComplete ? (text.length() > 25 ? text.substring(0, 22) + "..." : text) : text, x + 2, y + 2, new IDEFont(Fonts.lightGrayNormal, textSize), x + width, g);
		
		if (isAutoComplete) {
			g.drawImage(sprite, (x + width) - 20, y + 4, 16, 16, null);
			
			g.setColor(Colors.explorer);
			g2.setStroke(new BasicStroke(1f));
			g2.drawLine(x, y, x + width, y);
		}
	}
}
