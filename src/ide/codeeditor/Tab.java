package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ide.components.CloseTabButton;
import ide.components.IDEComponent;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

/**
 * Representa uma aba da IDE.
 * 
 * @author Juninho
 *
 */
public class Tab extends IDEComponent {

	public static final int MIN_X = 77;
	
	public static final int Y = 3;
	public static final int WIDTH = 200;
	public static final int HEIGHT = 30;
	
	private boolean isSaved = true;
	
	private CloseTabButton button;
	
	private ListableFile regent;
	
	private BufferedImage closeSpr = Main.spritesheet.getSprite(16, 0, 5, 5);
	private BufferedImage notSavedSpr = Main.spritesheet.getSprite(16, 5, 5, 5);
	
	public Tab(int x, ListableFile regent) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.spritesheet.getSprite(16, 0, 5, 5), this);
		
		CodeEditor.tabScr = 0;
	}
	
	@Override
    public boolean hovered() {
		int x = this.x + CodeEditor.tabScr;
		
        Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
        Rectangle comp = new Rectangle(x, y, width, height);

        return mouse.intersects(comp);
    }

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public void setSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}

	public ListableFile getRegent() {
		return regent;
	}

	public void setRegent(ListableFile regent) {
		this.regent = regent;
	}
	
	public void close() {
		CodeEditor.toRemove.add(this);
		CodeEditor.lines.clear();
		
		if (CodeEditor.tabs.size() < 2) {
			CodeEditor.editing = null;
			
			return;
		}
		
		Tab next = CodeEditor.tabs.indexOf(this) == 0 ? CodeEditor.tabs.get(1) : CodeEditor.tabs.get(0);
		
		CodeEditor.editing = next;
		
		try {
			CodeEditor.lines = CodeEditor.readFile(next.getRegent().getRegent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Salvar Arquivo
	 */
	public void save() {
		try {
			BufferedWriter w = Files.newBufferedWriter(regent.getRegent().toPath(), StandardCharsets.UTF_8); // precisa escrever em utf-8 tbm!!
			
			for (IDELine i : CodeEditor.lines) {
				StringBuilder sb = new StringBuilder();
				
				for (char c : i.getChars())
					sb.append(c);
				
				String s = sb.toString();
				
				if (s == null) break;
				
				w.write(s + "\n");
			}

			w.close();
			
			setSaved(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void execute(String arg) {
		switch (arg) {
		case "this":
			this.close();
			break;
			
		case "all":
			CodeEditor.tabs.clear();
			break;
			
		case "save":
			save();
			break;
		}
	}
	
	public void tick() {
		int x = this.x + CodeEditor.tabScr;
		
		if (CodeEditor.tabs.indexOf(this) - 1 > -1)
			x = CodeEditor.tabs.get(CodeEditor.tabs.indexOf(this) - 1).getX() + WIDTH + 3;
		else
			x = Tab.MIN_X + WIDTH + 3;
		
		button.setX(((this.x + WIDTH) - 20) + CodeEditor.tabScr);
		button.tick();
		
		if (leftClicked() && !button.leftClicked()) {
			CodeEditor.editing = this;
			
			try {
				CodeEditor.lines = CodeEditor.readFile(regent.getRegent());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			CodeEditor.cursorX = 0;
			CodeEditor.cursorY = 1;
			
			CodeEditor.scrX = 0;
			CodeEditor.scrY = 0;
		}
		
		if (rightClicked()) {
			MouseInput.updateMouse();
			
			IDEComponent.addRightClickOption(x, y + height + 3, 320, "Fechar Aba", (s) -> execute(s), "this");
			IDEComponent.addRightClickOption(x, y + height + 3 + 30, 320, "Fechar todas as abas", (s) -> execute(s), "all");
			IDEComponent.addRightClickOption(x, y + height + 3 + 60, 320, "Salvar", (s) -> execute(s), "save");
		}
		
		if (isSaved)
			button.setSprite(closeSpr);
		else
			button.setSprite(notSavedSpr);
		
		this.x = x;
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		int x = this.x + CodeEditor.tabScr;
		
		if (x < Main.editor.getX()) return;
		
		Color c = CodeEditor.editing == this ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() ? Colors.explorerLight : Colors.explorer;
		
		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x, Y, WIDTH, HEIGHT);
		
		g.setColor(c);
		g.drawRect(x, Y, WIDTH, HEIGHT);
		
		String extension = ListableFile.getFileExtension(regent.getRegent());
		
		Fonts.drawString(regent.getRegent().getName(), x + 35, Y + 5, new IDEFont(Fonts.lighterGrayNormal, 16), (x + WIDTH) - 15, g);
	
		button.render(g);
		
		for (FileType f : ListableFile.types)
			if (f.getExtension().equals(extension)) {
				g.drawImage(f.getIcon(), x + 3, Y + 1, HEIGHT - 3, HEIGHT - 3, null);
				
				return;
			}
		g.drawImage(Main.spritesheet.getSprite(0, 64, 16, 16), x + 3, Y + 1, HEIGHT, HEIGHT, null);
	}
}
