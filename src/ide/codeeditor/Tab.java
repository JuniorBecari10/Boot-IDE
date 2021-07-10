package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ide.components.CloseTabButton;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
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
public class Tab extends IDEComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	public static transient int MIN_X = 77;
	
	public static transient final int Y = 3;
	public static transient final int WIDTH = 200;
	public static transient final int HEIGHT = 30;
	
	public int scrX = 0, scrY = 0;
	
	private boolean isSaved = true;
	
	public CloseTabButton button;
	
	private ListableFile regent;
	
	public boolean isReadOnly;
	
	private transient BufferedImage closeSpr = Main.spritesheet.getSprite(16, 0, 5, 5);
	private transient BufferedImage notSavedSpr = Main.spritesheet.getSprite(16, 5, 5, 5);
	
	public Tab() {
		super(MIN_X, Y, WIDTH, HEIGHT, null);
		
		regent = null;
	}
	
	public Tab(int x, ListableFile regent) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.spritesheet.getSprite(16, 0, 5, 5), this);
		
		CodeEditor.tabScr = 0;
		
		String ext = ListableFile.getFileExtension(regent.getRegent());
		
		if (ext.equalsIgnoreCase(".pdf") || ext.equalsIgnoreCase(".jar") || ext.equalsIgnoreCase(".class") || ext.equalsIgnoreCase(".exe") || ext.equalsIgnoreCase(".urna") || ext.equalsIgnoreCase(".save") || ext.equalsIgnoreCase(".docx") || ext.equalsIgnoreCase(".pptx") || ext.equalsIgnoreCase(".one") || ext.equalsIgnoreCase(".psd") || ext.equalsIgnoreCase(".aed") || ext.equalsIgnoreCase(".ai") || ext.equalsIgnoreCase(".indd") || ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".dll") || ext.equalsIgnoreCase(".png") || ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".gif") || ext.equalsIgnoreCase(".bmp") || ext.equalsIgnoreCase(".ico") || ext.equalsIgnoreCase(".webp") || ext.equalsIgnoreCase(".mp4") || ext.equalsIgnoreCase(".wmv") || ext.equalsIgnoreCase(".avi") || ext.equalsIgnoreCase(".wav") || ext.equalsIgnoreCase(".mp3") || ext.equalsIgnoreCase(".ogg") || ext.equalsIgnoreCase(".otf") || ext.equalsIgnoreCase(".ttf") || ext.equalsIgnoreCase(".woff") || ext.equalsIgnoreCase(".woff2") || ext.equalsIgnoreCase(".zip") || ext.equalsIgnoreCase(".rar") || ext.equalsIgnoreCase(".7z") || ext.equalsIgnoreCase(".bin")) {
			isReadOnly = true;
			
			CodeEditor.isReadOnly = true;
		}
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
	
	/**
	 * Fecha essa Tab.
	 */
	public void close() {
		CodeEditor.toRemove.add(this);
		CodeEditor.lines.clear();
		
		CodeEditor.selecting = false;
		
		if (CodeEditor.tabs.size() == 1) {
			CodeEditor.editing = null;
			
			return;
		}
		
		CodeEditor.tabScr = (CodeEditor.tabs.get(CodeEditor.tabs.size() - 1).getX() + CodeEditor.tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280) ? CodeEditor.tabScr : CodeEditor.tabScr + 203;
		
		Tab next = CodeEditor.tabs.indexOf(this) == 0 ? CodeEditor.tabs.get(1) : CodeEditor.tabs.get(CodeEditor.tabs.indexOf(this) - 1);
		
		if (!CodeEditor.toRemove.get(0).equals(this))
			next = this;
		
		CodeEditor.editing = next;
		
		CodeEditor.cursorX = 0;
		CodeEditor.cursorY = 0;
		
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
		if (isReadOnly || CodeEditor.lines.isEmpty() || CodeEditor.lines == null) return;
		
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
			
			CodeEditor.editing = null;
			break;
			
		case "save":
			save();
			break;
			
		case "run":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", regent.getRegent().getName());
				File dir = regent.getRegent().getParentFile();
				
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "runbash":
			try {
				ProcessBuilder pb = new ProcessBuilder("sh", "-c", "start", regent.getRegent().getName());
				File dir = regent.getRegent().getParentFile();
				
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "showexp":
			Explorer.files.clear();
			ListableFile.files.clear();
			
			if (CommandTerminal.expOff)
				CommandTerminal.runCommand("toggleexplorer");
			
			if (regent.getParent() == null) {
				Explorer.scope = null;
				
				int index = 0;
				
				for (File f : Main.baseFolder.listFiles()) {
					Explorer.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
					
					index++;
				}
				
				break;
			}
			
			Explorer.scope = regent.getParent();
			
			ListableFile.files = ListableFile.loadFolder((!regent.getParent().getRegent().equals(Main.baseFolder) ? regent.getParent() : null));
			
			break;
			
		case "closeother":
			for (Tab t : CodeEditor.tabs)
				if (t != this) t.close();
			
			CodeEditor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para você!
			
			CodeEditor.editing = this;
			
			CodeEditor.isMultilineCommenting = false;
			CodeEditor.isAnotherIteration = false;
			CodeEditor.foundExt = false;
			
			try {
				CodeEditor.lines = CodeEditor.readFile(regent.getRegent());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			CodeEditor.cursorX = 0;
			CodeEditor.cursorY = 1;
			
			CodeEditor.scrX = scrX;
			CodeEditor.scrY = scrY;
			
			break;
		}
	}
	
	public void tick() {
		if (!regent.getRegent().exists())
			close();
		
		MIN_X = CommandTerminal.expOff ? -WIDTH : 77;	// -WIDTH é um macete kkk
		
		int x = this.x + CodeEditor.tabScr;
		
		if (CodeEditor.tabs.indexOf(this) - 1 > -1)
			x = CodeEditor.tabs.get(CodeEditor.tabs.indexOf(this) - 1).getX() + WIDTH + 3;
		else
			x = Tab.MIN_X + WIDTH + 3;
		
		button.setX(((this.x + WIDTH) - 20) + CodeEditor.tabScr);
		button.tick();
		
		if (CodeEditor.editing == this) {
			scrX = CodeEditor.scrX; // TODO
			scrY = CodeEditor.scrY;
		}
		
		if (leftClicked() && !button.leftClicked()) {
			CodeEditor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para você!
			
			CodeEditor.editing = this;
			
			CodeEditor.isMultilineCommenting = false;
			CodeEditor.isAnotherIteration = false;
			CodeEditor.foundExt = false;
			
			try {
				CodeEditor.lines = CodeEditor.readFile(regent.getRegent());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			CodeEditor.cursorX = 0;
			CodeEditor.cursorY = 1;
			
			CodeEditor.scrX = scrX;
			CodeEditor.scrY = scrY;
			
			//if (CodeEditor.scrY > CodeEditor.lines.size() * (CodeEditor.FONT_SIZE / 4))
				//CommandTerminal.runCommand("gotocursor");
			
			save();
		}
		
		if (rightClicked()) {
			MouseInput.updateMouse();
			
			IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3, 305, "Fechar Aba", (s) -> execute(s), "this");
			IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 30, 305, "Fechar todas as abas", (s) -> execute(s), "all");
			IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 60, 305, "Fechar outras abas", (s) -> execute(s), "closeother");
			IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 90, 305, "Salvar", (s) -> execute(s), "save");
			IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 120, 305, "Abrir no Explorador", (s) -> execute(s), "showexp");
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if ((ListableFile.getFileExtension(regent.getRegent()).equals(".bat") || ListableFile.getFileExtension(regent.getRegent()).equals(".cmd") || ListableFile.getFileExtension(regent.getRegent()).equals(".com") || ListableFile.getFileExtension(regent.getRegent()).equals(".ps1")) && isWindows)
				IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 150, 305, "Executar", (s) -> execute(s), "run");
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".sh") && !isWindows)
				IDEComponent.addRightClickOption(x + CodeEditor.tabScr, y + height + 3 + 150, 305, "Executar", (s) -> execute(s), "runbash");
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
		
		IDEFont font = new IDEFont(Fonts.lighterGrayNormal, 16);
		
		if (CodeEditor.linesWithErrors != null && CodeEditor.syntaxErrorsOn) {
			if (CodeEditor.linesWithErrors.size() > 0 && CodeEditor.editing == this)
				font = new IDEFont(Fonts.errorNormal, 16);
		}
		
		Fonts.drawString(regent.getRegent().getName(), x + 35, Y + 5, font, isReadOnly ? (x + WIDTH) - 35 : (x + WIDTH) - 15, g);
	
		if (isReadOnly)
			g.drawImage(Main.spritesheet.getSprite(27, 0, 5, 5), (x + WIDTH) - 40, y + 7, 15, 15, null);
		
		button.render(g);
		
		for (FileType f : ListableFile.types) {
			if (f.getExtension().equalsIgnoreCase(extension)) {
				g.drawImage(f.getIcon(), x + 3, Y + 1, HEIGHT - 3, HEIGHT - 3, null);
				
				return;
			}
			
			else if (f.getExtension().equalsIgnoreCase(regent.getRegent().getName())) {
				g.drawImage(f.getIcon(), x + 3, Y + 1, HEIGHT - 3, HEIGHT - 3, null);
				
				return;
			}
		}
		g.drawImage(Main.spritesheet.getSprite(0, 64, 16, 16), x + 3, Y + 1, HEIGHT, HEIGHT, null);
	}
}
