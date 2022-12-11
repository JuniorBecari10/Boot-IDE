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

import javax.swing.JFileChooser;

import ide.components.CloseTabButton;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.explorercomponents.Execute;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
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
	
	public int drawW = WIDTH;
	
	public int scrX = 0, scrY = 0;
	public int cx = 0, cy = 1;
	
	public boolean closing = false;
	private boolean isSaved = true;
	
	public CloseTabButton button;
	
	public ListableFile regent;
	
	private boolean save = true;
	public static Tab dragging;
	
	public boolean isTemporary;
	
	private static final int animSpeed = 2;
	
	public boolean isReadOnly;
	
	public FileReadMode readMode = FileReadMode.NORMAL;
	
	public Tab(int x, ListableFile regent) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		this.isTemporary = false;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.closeTab, this);
		
		String ext = ListableFile.getFileExtension(regent.getRegent());
		
		if (CodeEditor.isBinary(ext) || readMode != FileReadMode.NORMAL) {
			isReadOnly = true;
			
			Main.editor.isReadOnly = true;
		}
		
		if (!Explorer.allowAnimations) return;
		
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
	
	public Tab(int x, ListableFile regent, boolean isTemporary) {
		super(x, Y, WIDTH, HEIGHT, null);
		
		this.regent = regent;
		this.isTemporary = isTemporary;
		
		button = new CloseTabButton((x + WIDTH) - 20, Y + 8, 13, 13, Main.spritesheet.getSprite(16, 0, 5, 5), this);
		
		String ext = ListableFile.getFileExtension(regent.getRegent());
		
		if (CodeEditor.isBinary(ext) || readMode != FileReadMode.NORMAL) {
			isReadOnly = true;
			
			Main.editor.isReadOnly = true;
		}
		
		if (!Explorer.allowAnimations) return;
		
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
		if (SetBranchName.added || SetFileName.added || CommandTerminal.active || MessageBox.active || SetBranchName.added || SetCommitName.added) return false;
		if (Main.anyMoreOptionsButtonHovered()) return false;
		
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

		if (Main.editor.editing != null && save) { // nao for nulo
			if (!Main.editor.editing.isSaved()) { // nao estiver salvo
				String[] options = { Texts.save, Texts.dont + " " + Texts.save, Texts.cancel };
				
				new Thread() {
					public void run() {
		    			MessageBox.showDialog(Texts.confirmSave, new String[] { Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.doYouWantToSave }, options, new Execute[] {
		    					() -> {
		    						save();
		    					},
		    					() -> {
		    						WindowInput.update();
		    						closing = false;
		    					}, () -> { } });
					}
				}.start();
				
				return;
			}
		}
		
		CommandTerminal.runCommand("resetundoredo");

		// por causa da thread
		Tab t = this;

		Main.editor.isMultilineCommenting = false; // TODO closeother reseta o cursor
		Main.editor.isAnotherIteration = false;
		Main.editor.foundExt = false;
		Main.editor.wordSinceSpace = "";

		Main.editor.toRemove.add(t);

		Main.editor.selecting = false;

		if (Main.editor.tabs.size() == 1) {
			Main.editor.editing = null;

			return;
		}

		if (!Main.editor.tabs.isEmpty()) {
			Main.editor.tabScr = (Main.editor.tabs.get(Main.editor.tabs.size() > 0 ? Main.editor.tabs.size() - 1 : 0).getX() + Main.editor.tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280) ? Main.editor.tabScr : Main.editor.tabScr + 203;

			Tab next = Main.editor.tabs.indexOf(t) == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(Main.editor.tabs.indexOf(t) - 1);

			if (Main.editor.toRemove != null && Main.editor.toRemove.get(0) != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab | a o get(0) que a null
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
		
		if (Main.editor.editing != null && save) { // nao for nulo
			if (!Main.editor.editing.isSaved()) { // nao estiver salvo
				String[] options = { Texts.save, Texts.dont + " " + Texts.save, Texts.cancel };
				
				new Thread() {
					public void run() {
		    			MessageBox.showDialog(Texts.confirmSave, new String[] { Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.doYouWantToSave }, options, new Execute[] {
		    					() -> {
		    						save();
		    						close();
		    					},
		    					() -> {
		    						WindowInput.update();
		    						
		    						save = false;
		    						close();
		    					}, () -> { } });
					}
				}.start();
				
				return;
			}
		}
		
		CommandTerminal.runCommand("resetundoredo");
		
		// por causa da thread
		Tab t = this;
		
		if (!Explorer.allowAnimations) {
			Main.editor.isMultilineCommenting = false; // TODO closeother reseta o cursor
			Main.editor.isAnotherIteration = false;
			Main.editor.foundExt = false;
			Main.editor.wordSinceSpace = "";
			
			Main.editor.toRemove.add(t);
			
			Main.editor.selecting = false;
			
			if (Main.editor.tabs.size() == 1) {
				Main.editor.editing = null;
				
				return;
			}
			
			if (!Main.editor.tabs.isEmpty()) {
				Main.editor.tabScr = (Main.editor.tabs.get(Main.editor.tabs.size() > 0 ? Main.editor.tabs.size() - 1 : 0).getX() + Main.editor.tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280) ? Main.editor.tabScr : Main.editor.tabScr + 203;
				
				if (Main.editor.editing.closing) {
					// aqui rola uma exception TODO
					Tab next = Main.editor.tabs.indexOf(t) == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(Main.editor.tabs.indexOf(t) - 1);
					
					if (Main.editor.toRemove != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab
						next = t;
					
					if (Main.editor.editing.getRegent().getRegent().getAbsolutePath().equals(t.getRegent().getRegent().getAbsolutePath())) {
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
					
					// T = Tab
					int indexOfT = Main.editor.tabs.indexOf(t);
					if (indexOfT < 0) {
						indexOfT = 0;
					}
					
					Tab next = indexOfT == 0 ? Main.editor.tabs.get(1) : Main.editor.tabs.get(indexOfT - 1);
					
					if (Main.editor.toRemove != null && Main.editor.toRemove.get(0) != null && !Main.editor.toRemove.get(0).equals(t)) // aqui rola um nullpointerexception quando fecha uma tab | a o get(0) que a null
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
		
		closing = false; // TODO talvez fazer uma variavel boolean e quando apertar o botao fechar, ativa ela, e se nao fechar, verifica se ela a true e fecha, removendo da lista
	}
	
	public void saveForced() {
		if (isReadOnly || Main.editor.lines.isEmpty() || Main.editor.lines == null || readMode != FileReadMode.NORMAL) return;
		
		Main.editor.addToUndo();
		
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
			
			//Main.editor.addToUndo();
			
			setSaved(true);
			save = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Salvar Arquivo
	 */
	public int save() {
		if (!isTemporary) {
			if (isReadOnly || Main.editor.lines.isEmpty() || Main.editor.lines == null || readMode != FileReadMode.NORMAL) return 1;
			
			Main.editor.addToUndo();
			
			try {
				Charset ch = Main.editor.codeType.equalsIgnoreCase("UTF-8") ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
				
				BufferedWriter w = Files.newBufferedWriter(regent.getRegent().toPath(), ch); // precisa escrever em utf-8 tbm!!
				
				int count = 0;
				for (IDELine i : Main.editor.lines) {
					if (i == null) continue;
					
					StringBuilder sb = new StringBuilder();
					
					for (char c : i.getChars())
						sb.append(c);
					
					String s = sb.toString();
					
					if (s == null) break;
					
					w.write(s + (count < Main.editor.lines.size() - 1 ? CodeEditor.lineEnding.getCh() : ""));
					count++;
				}
	
				w.close();
				
				//Main.editor.addToUndo();
				
				setSaved(true);
				save = true;
				
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JFileChooser chooser = new JFileChooser(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent());
			int option = chooser.showSaveDialog(Main.screen.frame);
			
			File oldFile = regent.getRegent();
			
			if (option == JFileChooser.APPROVE_OPTION) {
				regent = ListableFile.newListableFile(chooser.getSelectedFile());
				
				oldFile.delete();
				isTemporary = false;
				save();
				
				return 0;
			}
			else {
				return 1;
			}
		}
		
		return 0;
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
				
				Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(Main.baseFolder));
				
				break;
			}
			
			Explorer.scope = regent.getParent();
			ListableFile.files = ListableFile.loadFolder((!regent.getParent().getRegent().equals(Main.baseFolder) ? regent.getParent() : null));
			
			break;
			
		case "closeother":
			for (Tab t : Main.editor.tabs)
				if (t != this) t.close();
			
			Main.editor.editing.save(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para voca!
			
			Main.editor.editing = this;
			
			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			Main.editor.foundExt = false;
			
			Main.editor.tabScr = 0;
			
			Main.editor.tabs.get(0).select();
			
			break;
			
		case "closeapply":
			Main.load(regent.getRegent().getAbsolutePath());
			
			ListableFile.generateLocalConfigFile(Main.defaultConfigFile);
			
			break;
			
		case "copyrel":
			CodeEditor.copyText(regent.getRegent().getAbsolutePath().contains(File.separator + Main.baseFolder.getName() + File.separator) ? regent.getRegent().getAbsolutePath().substring(regent.getRegent().getAbsolutePath().indexOf(Main.baseFolder.getName())) : regent.getRegent().getAbsolutePath());
			break;
			
		case "copyabs":
			CodeEditor.copyText(regent.getRegent().getAbsolutePath());
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
			Main.editor.editing.saveForced(); // agr n tem mais problema em abrir outra tab sem salvar essa pq a Boot IDE salva para você!
		
		CommandTerminal.runCommand("resetundoredo");
		
		Main.editor.wordSinceSpace = "";
		RightClickOption.removeAllRightClickOptions();
		
		String oldEditingPath = Main.editor.editing.getRegent().getRegent().getAbsolutePath();
		Main.editor.editing = this;
		
		Main.editor.isMultilineCommenting = false;
		Main.editor.isAnotherIteration = false;
		Main.editor.foundExt = false;
		
		// tática pra verificar se o arquivo é o mesmo, strings são imutáveis ksksk
		if (Main.editor.editing.getRegent().getRegent().getAbsolutePath().equals(oldEditingPath))
			CommandTerminal.runCommand("resetundoredo");
		
		Main.editor.refreshText();
		
		Main.editor.cursorX = cx;
		Main.editor.cursorY = cy;
		
		Main.editor.scrX = scrX;
		Main.editor.scrY = scrY;
		
		Main.editor.setExtType(ListableFile.getFileExtension(regent.getRegent()));
		
		/*if (!isSaved())
			save();*/
		Main.editor.setCursorWithinBounds();
	}
	
	public void tick() {
		if (regent == null || !regent.getRegent().exists()) {
			close();
			
			return;
		}
		
		if (width < 10) closeWithoutAnimation();
		
		if (isTemporary)
			if (Main.editor.lines.size() == 1 && Main.editor.lines.get(0).getChars().isEmpty())
				isSaved = true;
		
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
					return Integer.compare(t1.getX() - (WIDTH / 2) + Main.editor.tabScr, t2.getX() + Main.editor.tabScr);
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
			
			int width = Main.lang == Language.PORT ? 385 : 300;
			
			List<RightClickOption> list = new ArrayList<>();
			
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2, width, Texts.closeTab, (s) -> execute(s), "this", true));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 30, width, Texts.closeAllTabs, (s) -> execute(s), "all"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 60, width, Texts.closeWithoutSave, (s) -> execute(s), "nosave"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 90, width, Main.editor.tabs.size() > 1, Texts.closeOtherTabs, (s) -> execute(s), "closeother"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 120, width, Texts.save, (s) -> execute(s), "save"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 150, width, Main.baseFolder != null, Texts.openBootExplorer, (s) -> execute(s), "showexp"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Texts.openExplorer, (s) -> execute(s), "sysexp"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Main.baseFolder != null ? regent.getRegent().getPath().contains(File.separator + Main.baseFolder.getName()) : false, Texts.copyRelativePath, (s) -> execute(s), "copyrel"));
			list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 180, width, Texts.copyAbsolutePath, (s) -> execute(s), "copyabs"));
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if ((ListableFile.getFileExtension(regent.getRegent()).equals(".bat") || ListableFile.getFileExtension(regent.getRegent()).equals(".cmd") || ListableFile.getFileExtension(regent.getRegent()).equals(".com") || ListableFile.getFileExtension(regent.getRegent()).equals(".ps1")) && isWindows)
				list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.execute, (s) -> execute(s), "run"));
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".sh") && !isWindows)
				list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.execute, (s) -> execute(s), "runbash"));
			
			if (ListableFile.getFileExtension(regent.getRegent()).equals(".conf"))
				list.add(new RightClickOption(x + Main.editor.tabScr, y + height + 2 + 210, width, Texts.apply, (s) -> execute(s), "closeapply"));
			
			IDEComponent.addRightClickOptions(x + Main.editor.tabScr, y + height + 2, list.toArray(new RightClickOption[list.size()]));
			
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
		
		/*if (isTemporary) {
			if (Main.editor.lines.size() == 1 && Main.editor.lines.get(0).getChars().isEmpty()) {
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
		}*/
		
		if (!closing && dragging != this)
			button.setX(((x + WIDTH) - 20) + Main.editor.tabScr);
		
		x = dragging == null ? this.x + Main.editor.tabScr : this.x;
		
		// não está arrastando
		if (!isTabDragged() || dragging == null) {
			MIN_X = CommandTerminal.expOff ? -WIDTH : Main.editor.getX() - (WIDTH + 4);	// -WIDTH é um macete kkk - 77
			if (Main.editor.tabs.indexOf(this) > 0)
				x = Main.editor.tabs.get(Main.editor.tabs.indexOf(this) - 1).getX() + WIDTH + 2;
			else
				x = Tab.MIN_X + WIDTH + 3;
		}
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
		g2.fillRect(x, Y - 1, drawW, HEIGHT);
		
		g.setColor(c);
		
		if (Main.editor.editing == this)
			g.drawRect(x, Y, drawW, HEIGHT);
		else if (Main.editor.tabs.indexOf(Main.editor.editing) - 1 != Main.editor.tabs.indexOf(this))
			g.drawLine(x + drawW, Y/* + 2*/, x + drawW, Screen.DECORATION_HEIGHT + HEIGHT/* - 2*/);
		
		String extension = ListableFile.getFileExtension(regent.getRegent());
		
		IDEFont font = new IDEFont(Fonts.lighterGrayNormal, 16);
		
		int limit = (x + drawW) - 15;
		
		if (isReadOnly) limit = (x + drawW) - 30;
		
		Fonts.drawString(
				ListableFile.getFileExtension(regent.getRegent()).equalsIgnoreCase(Main.CONFIG_FILE_EXTENSION)
				&& regent.getRegent().getParent().equalsIgnoreCase(Main.userDir)
					? Texts.settings
					: regent.getRegent().getName(), x + 35, Y + 5, font, limit, g);
	
		if (isReadOnly) {
			if (readMode == FileReadMode.NORMAL)
				g.drawImage(Main.lock, /*Main.editor.editing == this ? */(x + drawW) - 40 /*: (x + drawW) - 20*/, y + 7, 15, 15, null);
			else {
				if (readMode == FileReadMode.HEX)
					g.drawImage(Main.hexView, (x + drawW) - 40, y + 7, 16, 16, null);
				else if (readMode == FileReadMode.BIN || readMode == FileReadMode.BINARY)
					g.drawImage(Main.binView, (x + drawW) - 40, y + 7, 16, 16, null);
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
