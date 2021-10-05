package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JOptionPane;

import ide.components.CloseTabButton;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RightClickOption;
import ide.explorer.Explorer;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

/**
 * Representa uma aba da IDE.
 * 
 * @author Juninho
 *
 */
public class Tab extends IDEComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	public static transient int MIN_X = 77; // TODO colocar o min x pra frente, funcionando
	
	public static transient final int Y = 3;
	public static transient final int WIDTH = 200;
	public static transient final int HEIGHT = 30;
	
	private int drawW = WIDTH;
	
	public int scrX = 0, scrY = 0;
	
	public boolean closing = false;
	private boolean isSaved = true;
	
	public CloseTabButton button;
	
	private ListableFile regent;
	
	public boolean isReadOnly;
	
	public Tab(int x, ListableFile regent) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.spritesheet.getSprite(16, 0, 5, 5), this);
		
		String ext = ListableFile.getFileExtension(regent.getRegent());
		
		if (CodeEditor.isBinary(ext)) {
			isReadOnly = true;
			
			Main.editor.isReadOnly = true;
		}
		
		new Thread() {
			public void run() {
				drawW = 0;
				
				while (drawW < WIDTH) {
					drawW += 2;
					
					button.setX((x + Main.editor.tabScr + drawW) - 20);
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	@Override
    public boolean hovered() {
		int x = this.x + Main.editor.tabScr;
		
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
		closing = true;
		
		if (Main.editor.editing != null) { // não for nulo
			if (!Main.editor.editing.isSaved()) { // não estiver salvo
				String[] options = { Texts.save, Texts.dont + " " + Texts.save, Texts.cancel };
				
				CodeEditor.setSystemLook();
				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				
				if (selectedOption == 0) save();
				else if (selectedOption == 2) {
					WindowInput.update();
					closing = false;
					
					return;
				}
			}
		}
		
		Tab t = this;
		
		new Thread() {
			public void run() {
				while (drawW > 0) {
					drawW -= 2;
					
					button.setX((x + Main.editor.tabScr + drawW) - 20);
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				Main.editor.isMultilineCommenting = false; // TODO closeother reseta o cursor
				Main.editor.isAnotherIteration = false;
				Main.editor.foundExt = false;
				
				Main.editor.toRemove.add(t);
				
				Main.editor.selecting = false;
				
				if (Main.editor.tabs.size() == 1) {
					Main.editor.editing = null;
					
					return;
				}
				
				if (!Main.editor.tabs.isEmpty()) {
					Main.editor.tabScr = (Main.editor.tabs.get(Main.editor.tabs.size() > 0 ? Main.editor.tabs.size() - 1 : 0).getX() + Main.editor.tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280) ? Main.editor.tabScr : Main.editor.tabScr + 203;
				
					Tab next = Main.editor.tabs.indexOf(t) == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(Main.editor.tabs.indexOf(t) - 1);
					
					if (!Main.editor.toRemove.get(0).equals(t))
						next = t;
					
					if (Main.editor.editing == t) {
						Main.editor.cursorX = 0;
						Main.editor.cursorY = 1;
					}
					
					Main.editor.editing = next;
					
					
					Main.editor.scrX = next.scrX;
					Main.editor.scrY = next.scrY;
					
					Main.editor.lines.clear();
				
					try {
						Main.editor.lines = Main.editor.readFile(next.getRegent().getRegent());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
//		Main.editor.isMultilineCommenting = false; // TODO closeother reseta o cursor
//		Main.editor.isAnotherIteration = false;
//		Main.editor.foundExt = false;
//		
//		Main.editor.lines.clear();
//		
//		Main.editor.selecting = false;
//		
//		if (Main.editor.tabs.size() == 1) {
//			Main.editor.editing = null;
//			
//			return;
//		}
//		
//		Main.editor.tabScr = (Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX() + Main.editor.tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280) ? Main.editor.tabScr : Main.editor.tabScr + 203;
//		
//		Tab next = Main.editor.tabs.indexOf(this) == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(Main.editor.tabs.indexOf(this) - 1);
//		
//		if (!Main.editor.toRemove.get(0).equals(this))
//			next = this;
//		
//		Main.editor.editing = next;
//		
//		Main.editor.cursorX = 0;
//		Main.editor.cursorY = 1;
//		
//		Main.editor.scrX = next.scrX;
//		Main.editor.scrY = next.scrY;
//		
//		try {
//			Main.editor.lines = Main.editor.readFile(next.getRegent().getRegent());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		closing = false;
	}
	
	/**
	 * Salvar Arquivo
	 */
	public void save() {
		if (isReadOnly || Main.editor.lines.isEmpty() || Main.editor.lines == null) return;
		
		try {
			Charset ch = Main.editor.codeType.equals("UTF-8") ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
			
			BufferedWriter w = Files.newBufferedWriter(regent.getRegent().toPath(), ch); // precisa escrever em utf-8 tbm!!
			
			for (IDELine i : Main.editor.lines) {
				if (i == null) continue;
				
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
	
	public void execute(String arg) {
		switch (arg) {
		case "this":
			this.close();
			break;
			
		case "all":
			Main.editor.tabs.forEach((e) -> e.close());
			
			Main.editor.editing = null;
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
			for (Tab t : Main.editor.tabs)
				if (t != this) t.close();
			
			Main.editor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para você!
			
			Main.editor.editing = this;
			
			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			Main.editor.foundExt = false;
			
			Main.editor.tabScr = 0;
			
			try {
				Main.editor.lines = Main.editor.readFile(regent.getRegent());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Main.editor.cursorX = 0;
			Main.editor.cursorY = 1;
			
			Main.editor.scrX = scrX;
			Main.editor.scrY = scrY;
			
			break;
			
		case "alternate":
			Main.editor.alternateTabsMode = true;
			Main.editor.exchanging = this;
			
			break;
		}
	}
	
	public synchronized void refreshRegent() {
		regent = ListableFile.search(regent.getRegent());
	}
	
	public void tick() {
		if (regent == null || !regent.getRegent().exists()) {
			close();
			
			return;
		}
		
		MIN_X = CommandTerminal.expOff ? -WIDTH : Main.editor.getX() - 203;	// -WIDTH é um macete kkk - 77
		
		if (x < Main.editor.getX()) x = Main.editor.getX();
		int x = this.x + Main.editor.tabScr;
		
		if (Main.editor.tabs.indexOf(this) - 1 > -1)
			x = Main.editor.tabs.get(Main.editor.tabs.indexOf(this) - 1).getX() + WIDTH + 3;
		else
			x = Tab.MIN_X + WIDTH + 3;
		
		button.setX(((this.x + WIDTH) - 20) + Main.editor.tabScr);
		
		//if (Main.editor.editing == this)
			button.tick();
		
		if (Main.editor.editing == this) {
			scrX = Main.editor.scrX; // TODO
			scrY = Main.editor.scrY;
		}
		
		if (!regent.getRegent().exists()) close();
		
		if (leftClicked() && !(Main.editor.editing == this ? button.leftClicked() : false /*pq inverte*/) && Main.editor.alternateTabsMode) {
			Main.editor.exchanged = this;
			
			CommandTerminal.runCommand("ordertab " + Main.editor.tabs.indexOf(Main.editor.exchanging) + " " + Main.editor.tabs.indexOf(Main.editor.exchanged));
			
			Main.editor.alternateTabsMode = false;
			
			Main.editor.exchanging = null;
			Main.editor.exchanged = null;
			
			return;
		}
		
		if (leftClicked() && !button.leftClicked() && !Main.editor.alternateTabsMode) {
			if (Main.editor.editing != null && !isSaved())
				Main.editor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para você!
			
			Main.editor.wordSinceSpace = "";
			RightClickOption.removeAllRightClickOptions();
			
			Main.editor.editing = this;
			
			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			Main.editor.foundExt = false;
			
			if (Main.editor.searchWindow != null) {
				Main.editor.searchWindow.setVisible(false);
				Main.editor.alreadyAddedFrame = false;
				SearchReplaceWindow.active = false;
			}
			
			try {
				Main.editor.lines = Main.editor.readFile(regent.getRegent());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Main.editor.cursorX = 0;
			Main.editor.cursorY = 1;
			
			Main.editor.scrX = scrX;
			Main.editor.scrY = scrY;
			
			if (!isSaved())
				save();
		}
		
		if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered())) && !Main.editor.alternateTabsMode) {
			MouseInput.updateMouse();
			
			int width = Main.lang == Language.PORT ? 305 : 260;
			
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2, width, Texts.closeTab, (s) -> execute(s), "this");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 30, width, Texts.closeAllTabs, (s) -> execute(s), "all");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 60, width, Texts.closeOtherTabs, (s) -> execute(s), "closeother");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 90, width, Texts.save, (s) -> execute(s), "save");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 120, width, Texts.openBootExplorer, (s) -> execute(s), "showexp");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 150, width, Texts.orderTabs, (s) -> execute(s), "alternate");
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if ((ListableFile.getFileExtension(regent.getRegent()).equals(".bat") || ListableFile.getFileExtension(regent.getRegent()).equals(".cmd") || ListableFile.getFileExtension(regent.getRegent()).equals(".com") || ListableFile.getFileExtension(regent.getRegent()).equals(".ps1")) && isWindows)
				IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Texts.execute, (s) -> execute(s), "run");
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".sh") && !isWindows)
				IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Texts.execute, (s) -> execute(s), "runbash");
		}
		
		if (isSaved) {
			if (Main.editor.editing == this)
				button.setSprite(Main.closeTab);
			else
				button.setSprite(Main.notSelectedCloseTab);
		}
		else {
			if (Main.editor.editing == this)
				button.setSprite(Main.notSavedTab);
			else
				button.setSprite(Main.notSelectedNotSavedTab);
		}
		
		this.x = x;
	}
	
	public void render(Graphics g) {
		if (regent == null || !regent.getRegent().exists()) {
			close();
			
			return;
		}
		
		Graphics2D g2 = (Graphics2D) g;
		
		int x = this.x + Main.editor.tabScr;
		
		Color c = Main.editor.editing == this ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() ? Colors.explorerLight : Colors.explorer;
		
		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x, Y, drawW, HEIGHT);
		
		g.setColor(c);
		g.drawRect(x, Y, drawW, HEIGHT);
		
		String extension = ListableFile.getFileExtension(regent.getRegent());
		
		IDEFont font = new IDEFont(Fonts.lighterGrayNormal, 16);
		
		int limit = (x + drawW) - 15;
		
		/*if (Main.editor.editing == this && isReadOnly) limit = (x + drawW) - 30;
		else if (Main.editor.editing != this && isReadOnly) limit = (x + drawW) - 15;
		else if (Main.editor.editing != this && !isReadOnly) limit = x + drawW;*/
		
		if (isReadOnly) limit = (x + drawW) - 30;
		
		Fonts.drawString(regent.getRegent().getName(), x + 35, Y + 5, font, limit, g);
	
		if (isReadOnly)
			g.drawImage(Main.lock, /*Main.editor.editing == this ? */(x + drawW) - 40 /*: (x + drawW) - 20*/, y + 7, 15, 15, null);
		
		//if (Main.editor.editing == this)
			button.render(g);
		
		for (FileType f : ListableFile.types) {
			if (f.getExtension().equalsIgnoreCase(extension)) {
				g.drawImage(f.getIcon(), x + 3, Y + 2, HEIGHT - 3, HEIGHT - 3, null);
				
				return;
			}
			
			else if (f.getExtension().equalsIgnoreCase(regent.getRegent().getName())) {
				g.drawImage(f.getIcon(), x + 3, Y + 2, HEIGHT - 3, HEIGHT - 3, null);
				
				return;
			}
		}
		
		g.drawImage(Main.spritesheet.getSprite(0, 64, 16, 16), x + 3, Y + 1, HEIGHT, HEIGHT, null);
		
		/*if (Main.editor.alternateTabsMode && Main.editor.exchanging == this) {
			int xdr = (MouseInput.getMouseX() - drawW) - 10;
			int ydr = MouseInput.getMouseY();
			
			g.setColor(Colors.explorer);
			g.fillRect(xdr, ydr, drawW, HEIGHT);
			
			g.setColor(Colors.explorerLight);
			g2.setStroke(new BasicStroke(3f));
			g.drawRect(xdr, ydr, drawW, HEIGHT);
		}*/
	}
}