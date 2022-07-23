package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import ide.screen.Screen;
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

	public static int MIN_X = 77; // TODO colocar o min x pra frente, funcionando
	
	public static final int Y = Screen.DECORATION_HEIGHT + 3;
	public static final int WIDTH = 200;
	public static final int HEIGHT = 30;
	
	public static boolean allowAnimation = true;
	
	public int drawW = WIDTH;
	
	public int scrX = 0, scrY = 0;
	public int cx = 0, cy = 1;
	
	public boolean closing = false;
	private boolean isSaved = true;
	
	//private Tab dragging = null;
	
	public CloseTabButton button;
	
	public ListableFile regent;
	
	private boolean save = true;
	public static Tab dragging;
	
	private final int animSpeed = 2;
	
	public boolean isReadOnly;
	
	public FileReadMode readMode = FileReadMode.NORMAL;
	
	public Tab(int x, ListableFile regent) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.spritesheet.getSprite(16, 0, 5, 5), this);
		
		String ext = ListableFile.getFileExtension(regent.getRegent());
		
		if (CodeEditor.isBinary(ext) || readMode != FileReadMode.NORMAL) {
			isReadOnly = true;
			
			Main.editor.isReadOnly = true;
		}
		
		if (!allowAnimation) return;
		
		new Thread() {
			public void run() {
				drawW = 0;
				
				while (drawW < WIDTH) {
					drawW += animSpeed;
					Main.canRunLoop = true;
					
					button.setX((x + Main.editor.tabScr + drawW) - 20);
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		Main.editor.setCursorWithinBounds();
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
	
	public String toString() {
		return "Tab: Regent: " + regent;
	}

	public void closeWithoutAnimation() {
		closing = true;

		if (Main.editor.editing != null && save) { // n�o for nulo
			if (!Main.editor.editing.isSaved()) { // n�o estiver salvo
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

		// por causa da thread
		Tab t = this;

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

			if (Main.editor.toRemove != null && Main.editor.toRemove.get(0) != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab | � o get(0) que � null
				next = t;

			if (Main.editor.toRemove.get(0) == null)
				next = Main.editor.tabs.get(0);

			if (Main.editor.editing == t) {
				Main.editor.cursorX = 0;
				Main.editor.cursorY = 1;
			}

			Main.editor.editing = next;

			Main.editor.cursorX = cx;
			Main.editor.cursorY = cy;

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

	/**
	 * Fecha essa Tab.
	 */
	public void close() {
		closing = true;
		
		if (Main.editor.editing != null && save) { // n�o for nulo
			if (!Main.editor.editing.isSaved()) { // n�o estiver salvo
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
		
		// por causa da thread
		Tab t = this;
		
		if (!allowAnimation) {
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
				
				// aqui rola uma exception TODO
				Tab next = Main.editor.tabs.indexOf(t) == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(Main.editor.tabs.indexOf(t) - 1);
				
				if (Main.editor.toRemove != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab
					next = t;
				
				if (Main.editor.editing == t) {
					Main.editor.cursorX = 0;
					Main.editor.cursorY = 1;
				}
				
				Main.editor.editing = next;
				
				Main.editor.cursorX = cx;
				Main.editor.cursorY = cy;
				
				Main.editor.scrX = next.scrX;
				Main.editor.scrY = next.scrY;
				
				Main.editor.lines.clear();
			
				try {
					Main.editor.lines = Main.editor.readFile(next.getRegent().getRegent());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return;
		}
		
		new Thread() {
			public void run() {
				while (drawW > 0) {
					drawW -= animSpeed;
					Main.canRunLoop = true;
					
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
					
					if (Main.editor.toRemove != null && Main.editor.toRemove.get(0) != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab | � o get(0) que � null
						next = t;
					
					if (Main.editor.toRemove.get(0) == null)
						next = Main.editor.tabs.get(0);
					
					if (Main.editor.editing == t) {
						Main.editor.cursorX = 0;
						Main.editor.cursorY = 1;
					}
					
					Main.editor.editing = next;
					
					Main.editor.cursorX = cx;
					Main.editor.cursorY = cy;
					
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
		
		closing = false; // TODO talvez fazer uma variavel boolean e quando apertar o bot�o fechar, ativa ela, e se n�o fechar, verifica se ela � true e fecha, removendo da lista
	}
	
	/**
	 * Salvar Arquivo
	 */
	public void save() {
		if (isReadOnly || Main.editor.lines.isEmpty() || Main.editor.lines == null || readMode != FileReadMode.NORMAL) return;
		
		try {
			Charset ch = Main.editor.codeType.equalsIgnoreCase("UTF-8") ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
			
			BufferedWriter w = Files.newBufferedWriter(regent.getRegent().toPath(), ch); // precisa escrever em utf-8 tbm!!
			
			for (IDELine i : Main.editor.lines) {
				if (i == null) continue;
				
				StringBuilder sb = new StringBuilder();
				
				for (char c : i.getChars())
					sb.append(c);
				
				String s = sb.toString();
				
				if (s == null) break;
				
				w.write(s + CodeEditor.lineEnding.getCh());
			}

			w.close();
			
			Main.editor.addToUndo();
			
			setSaved(true);
			save = true;
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
			Main.editor.editing = null;
			
			Main.editor.tabs.forEach((e) -> e.close());
			
			try {
				Main.editor.killAllTabs.start();
			} catch (Exception e) { return; }
			break;
			
		case "save":
			save();
			break;
			
		case "nosave":
			save = false;
			this.close();
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
				ProcessBuilder pb = new ProcessBuilder("./", regent.getRegent().getName());
				File dir = regent.getRegent().getParentFile();
				
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "runwithbash":
			try {
				ProcessBuilder pb = new ProcessBuilder("sh", regent.getRegent().getName());
				File dir = regent.getRegent().getParentFile();
				
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "sysexp":
			try {
				Main.desktop.open(regent.getRegent().getParentFile());
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
			
			Main.editor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para voc�!
			
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
			
			Main.editor.cursorX = cx;
			Main.editor.cursorY = cy;
			
			Main.editor.tabs.get(0).select();
			
			break;
			
		case "closeapply":
			if (!Main.editor.editing.isSaved())
				Main.editor.editing.save();
			
			String[] options = { Texts.yes, Texts.no, Texts.cancel };

			CodeEditor.setSystemLook();
			int selectedOption = JOptionPane.showOptionDialog(null,
					Texts.thisWillCloseProgram,
					Texts.programWillClose, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options,
					options[0]);
			
			if (selectedOption == 0) {
				Main.writeFiles = false;
				System.exit(0);
			}
			break;
		}
	}
	
	public synchronized void refreshRegent() {
		regent = ListableFile.search(regent.getRegent());
	}
	
	public static boolean isTabHovered() {
		for (Tab t : Main.editor.tabs)
			if (t.hovered()) return true;
		
		return false;
	}
	
	public static boolean isTabDragged() {
		for (Tab t : Main.editor.tabs)
			if (t.dragged() || dragging != null) return true;
		
		return false;
	}
	
	public void select() {
		if (Main.editor.editing != null && !Main.editor.editing.isSaved())
			Main.editor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para voc�!
		
		Main.editor.wordSinceSpace = "";
		RightClickOption.removeAllRightClickOptions();
		
		Main.editor.editing = this;
		
		Main.editor.isMultilineCommenting = false;
		Main.editor.isAnotherIteration = false;
		Main.editor.foundExt = false;
		
		try {
			Main.editor.lines = Main.editor.readFile(regent.getRegent());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Main.editor.cursorX = cx;
		Main.editor.cursorY = cy;
		
		Main.editor.scrX = scrX;
		Main.editor.scrY = scrY;
		
		Main.editor.setExtType(ListableFile.getFileExtension(regent.getRegent()));
		
		/*if (!isSaved())
			save();*/
		
		CommandTerminal.runCommand("resetundoredo");
		Main.editor.setCursorWithinBounds();
	}
	
	public void tick() {
		if (regent == null || !regent.getRegent().exists()) {
			close();
			
			return;
		}
		
		if (width < 10) closeWithoutAnimation();
		
		int x = dragging == null ? this.x + Main.editor.tabScr : this.x;
		
		if (!isTabDragged() || dragging == null) {
			MIN_X = CommandTerminal.expOff ? -WIDTH : Main.editor.getX() - 203;	// -WIDTH � um macete kkk - 77
			int targetX = Main.editor.getX();
			
			if (Main.editor.tabs.indexOf(this) - 1 > -1)
				targetX = Main.editor.tabs.get(Main.editor.tabs.indexOf(this) - 1).getX() + WIDTH + 3;
			else
				targetX = Tab.MIN_X + WIDTH + 3;
			
			/*if (x < targetX) x += 40;
			if (x > targetX) x -= 40;
			
			if (x - targetX < 40) x = targetX;*/
			
			x = targetX;
		}
		
		//System.out.println(dragging + ", " + MouseInput.isMouseDragged());
		
		if (!RightClickOption.isRightClickActive()) {
			if (hovered())
				Main.screen.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else {
			Main.screen.setCursor(Cursor.getDefaultCursor());
		}
		
		/*if (dragged() && !dragging) {
			dragging = true;
			
			x = MouseInput.getMouseX() - 20;
			y = MouseInput.getMouseY();
		}
		
		if (!MouseInput.isMouseDragged() && dragging) {
			dragging = false;
			
			List<Tab> ts = Main.editor.tabs;
			
			Collections.sort(ts, new Comparator<Tab>() {
				@Override
				public int compare(Tab t1, Tab t2) {
					return new Integer(t2.getX()).compareTo(t1.getX());
				}
			});
		}*/
		
		if (dragged() && dragging == null) {
			dragging = this;
		}
		
		if (dragging == this) {
			x = MouseInput.getMouseX() - (WIDTH / 2) - Main.editor.tabScr;
			button.setX(((this.x + WIDTH) - 20) + Main.editor.tabScr);
		}
		
		if (!MouseInput.isMouseDragged() && dragging != null) {
			dragging = null;
			
			List<Tab> ts = new ArrayList<>(Main.editor.tabs);
			
			Collections.sort(ts, new Comparator<Tab>() {
				@Override
				public int compare(Tab t1, Tab t2) {
					return new Integer(t1.getX() - (WIDTH / 2) + Main.editor.tabScr).compareTo(t2.getX() + Main.editor.tabScr);
				}
			});
			
			Main.editor.tabs = ts;
		}
		
		//if (Main.editor.editing == this)
		button.tick();
		
		if (Main.editor.editing == this) {
			scrX = Main.editor.scrX;
			scrY = Main.editor.scrY;
			
			cx = Main.editor.cursorX;
			cy = Main.editor.cursorY;
		}
		
		if (!regent.getRegent().exists()) close();
		
		if (leftClicked() && !button.leftClicked()) {
			select();
		}
		
		if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered()))) {
			MouseInput.updateMouse();
			
			int width = Main.lang == Language.PORT ? 485 : 330;
			
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2, width, Texts.closeTab, (s) -> execute(s), "this");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 30, width, Texts.closeAllTabs, (s) -> execute(s), "all");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 60, width, Texts.closeWithoutSave, (s) -> execute(s), "nosave");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 90, width, Main.editor.tabs.size() > 1, Texts.closeOtherTabs, (s) -> execute(s), "closeother");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 120, width, Texts.save, (s) -> execute(s), "save");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 150, width, Texts.openBootExplorer, (s) -> execute(s), "showexp");
			IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Texts.openExplorer, (s) -> execute(s), "sysexp");
			//IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.orderTabs, (s) -> execute(s), "alternate");
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if ((ListableFile.getFileExtension(regent.getRegent()).equals(".bat") || ListableFile.getFileExtension(regent.getRegent()).equals(".cmd") || ListableFile.getFileExtension(regent.getRegent()).equals(".com") || ListableFile.getFileExtension(regent.getRegent()).equals(".ps1")) && isWindows)
				IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.execute, (s) -> execute(s), "run");
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".sh") && !isWindows)
				IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.execute, (s) -> execute(s), "runbash");
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".conf"))
				IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.closeApply, (s) -> execute(s), "closeapply");
			
			//if (ListableFile.getFileExtension(regent.getRegent()).equals(".sh") && isWindows)
			//	IDEComponent.addRightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.executeBash, (s) -> execute(s), "runwithbash");
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
		
		if (!closing && dragging != this)
			button.setX(((this.x + WIDTH) - 20) + Main.editor.tabScr);
		
		this.x = x;
	}
	
	public synchronized void render(Graphics g) {
		if (regent == null || !regent.getRegent().exists()) {
			close();
			
			return;
		}
		
		Graphics2D g2 = (Graphics2D) g;
		
		int x = this.x + Main.editor.tabScr;
		
		Color c = Main.editor.editing == this ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() ? Colors.explorerLight : Colors.codeEditor;

		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x, Y, drawW, HEIGHT);

		g.setColor(c);
		g.drawRect(x, Y, drawW, HEIGHT);

		/*if (Main.editor.editing == this || (Main.editor.editing != this && hovered() && !Main.editor.editing.equals(Main.editor.tabs.get(Main.editor.tabs.indexOf(this) + 1)))) {
			g.setColor(bg);
			g2.setStroke(new BasicStroke(3f));
			g2.fillRect(x, Y, drawW, HEIGHT);
			
			g.setColor(c);
			g.drawRect(x, Y, drawW, HEIGHT);
		}
		
		if (Main.editor.editing != this && !hovered()) {
			g.setColor(Colors.textLight);
			g2.setStroke(new BasicStroke(2f));
			g2.drawLine(x + width, y + 5, x + width, y + height - 5);
		}*/
		
		String extension = ListableFile.getFileExtension(regent.getRegent());
		
		IDEFont font = new IDEFont(Fonts.lighterGrayNormal, 16);
		
		int limit = (x + drawW) - 15;
		
		/*if (Main.editor.editing == this && isReadOnly) limit = (x + drawW) - 30;
		else if (Main.editor.editing != this && isReadOnly) limit = (x + drawW) - 15;
		else if (Main.editor.editing != this && !isReadOnly) limit = x + drawW;*/
		
		if (isReadOnly) limit = (x + drawW) - 30;
		
		Fonts.drawString(regent.getRegent().getName(), x + 35, Y + 5, font, limit, g);
	
		if (isReadOnly) {
			if (readMode == FileReadMode.NORMAL)
				g.drawImage(Main.lock, /*Main.editor.editing == this ? */(x + drawW) - 40 /*: (x + drawW) - 20*/, y + 7, 15, 15, null);
			else {
				if (readMode == FileReadMode.HEX)
					g.drawImage(Main.hexView, (x + drawW) - 40, y + 7, 15, 15, null);
				else if (readMode == FileReadMode.BIN || readMode == FileReadMode.BINARY)
					g.drawImage(Main.binView, (x + drawW) - 40, y + 7, 15, 15, null);
			}
		}
		
		//if (Main.editor.editing == this)
		button.render(g);
		
		if (Main.editor.editing != this) {
			Color layer = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 70);
			
			g.setColor(layer);
			g2.setStroke(new BasicStroke(3f));
			g2.fillRect(x, Y, drawW, HEIGHT);
		}
		
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
		
		g.drawImage(Main.UNKNOWN_FILE_ICON, x + 3, Y + 1, HEIGHT, HEIGHT, null);
		
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
