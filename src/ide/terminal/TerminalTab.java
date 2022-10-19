package ide.terminal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorercomponents.Execute;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;
import ide.util.Texts;

public class TerminalTab extends IDEComponent {
	
	public static final int Y_EXPLORER = Screen.DECORATION_HEIGHT + 170;
	public static final int WIDTH = 136;
	public static final int HEIGHT = 30;
	
	private static final int animSpeed = 2;
	
	public String name;
	private File log;
	
	private File scope;
	
	private String[] lines;
	public Thread reader;
	
	private boolean alive = true;
	
	public CloseTerminalTabButton button;
	
	public boolean commandRunning = false;
	
	public TerminalTab(int x, int y, int widthh, String name) {
		super(x, y, widthh, HEIGHT, Main.term12Px);
		
		button = new CloseTerminalTabButton((x + WIDTH) - 20, y + 8, 13, 13, Main.closeTab, this);
		
		this.scope = new File(Explorer.getScopePath());
		
		this.width = 0;
		
		new Thread() {
			public void run() {
				while (width < WIDTH) {
					width += animSpeed;
					Main.canRunLoop = true;
					
					button.setX((x + Main.editor.tabScr + width) - 20);
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		this.name = name;
		this.log = new File(Main.userDir + File.separator + name);
		
		try {
			this.log.createNewFile();
			this.log.deleteOnExit();
			
			// Write header
			
			BufferedWriter w = Files.newBufferedWriter(log.toPath(), StandardCharsets.UTF_8);
			
			w.write(Main.PROGRAM_NAME + " Terminal\n\n");
			w.write(TerminalCore.prompt + " ");
			
			w.close();
			
			// read at least once
			lines = Files.readAllLines(log.toPath()).toArray(new String[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		reader = new Thread() {
			public void run() {
				while (alive) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					if (!commandRunning) continue;
					
					read();
				}
			}
		};
		
		reader.start();
		
		if (lines != null)
			Explorer.textArea.cursorX = lines[lines.length - 1].length();
		else
			Explorer.textArea.cursorX = 2;
	}
	
	public File getScope() {
		return scope;
	}
	
	public void setScope(File scope) {
		this.scope = scope;
	}

	public String[] getLines() {
		return lines;
	}
	
	public void setLines(String[] lines) {
		this.lines = lines;
	}
	
	public File getLog() {
		return log;
	}
	
	public void write() {
		try {
			BufferedWriter wr = Files.newBufferedWriter(log.toPath(), StandardCharsets.UTF_8);
			
			for (String s : lines)
				wr.write(s + "\n");
			
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void read() {
		try {
			lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8).toArray(new String[0]);
			
			Explorer.textArea.cursorX = lines[lines.length - 1].length();
		} catch (Exception e) {
			try {
				lines = Files.readAllLines(log.toPath(), StandardCharsets.ISO_8859_1).toArray(new String[0]);
				
				if (lines.length > 0)
					Explorer.textArea.cursorX = lines[lines.length - 1].length();
				else
					Explorer.textArea.cursorX = 2;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void close() {
		if (commandRunning) {
			MessageBox.showDialog(Texts.aCommandIsRunning, new String[] { "Do you want to stop the command execution anyway?" }, new String[] { Texts.yes, Texts.no }, new Execute[] {() -> { commandRunning = false; close(); }, () -> {}});
			return;
		}
		
		alive = false;
		TerminalTab t = this;

		new Thread() {
			public void run() {
				while (width > 0) {
					width -= animSpeed;
					Main.canRunLoop = true;

					button.setX((x + Main.editor.tabScr + width) - 20);

					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					for (int i = TerminalCore.tabs.indexOf(t); i < TerminalCore.tabs.size(); i++) {
						TerminalCore.tabs.get(i).setX(TerminalCore.tabs.get(i).getX() - TerminalCore.tabs.get(i).getWidth() - 3);
					}
					
					TerminalCore.tabs.remove(t);
					
					if (TerminalCore.tabs.isEmpty())
						TerminalCore.selected = null;
					else
						TerminalCore.selected = TerminalCore.tabs.get(TerminalCore.tabs.size() - 1);
					
					reader.join();
					
					try {
						log.delete();
					} catch (Exception ee) {
						
					}
					
					Explorer.textArea.lines = new String[0];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void select() {
		TerminalCore.selected = this;
		
		Explorer.textArea.cursorX = lines[lines.length - 1].length();
	}
	
	public void tick() {
		if (leftClicked())
			select();
		
		button.setX((x + Main.editor.tabScr + width) - 20);
		
		// close thread if tab closed
		
		if (commandRunning)
			button.setSprite(Main.notSavedTab);
		else
			button.setSprite(Main.closeTab);
		
		button.tick();
		
		if (TerminalCore.tabs.indexOf(this) > 0)
			x = TerminalCore.tabs.get(TerminalCore.tabs.indexOf(this) - 1).getX() + TerminalCore.tabs.get(TerminalCore.tabs.indexOf(this) - 1).getWidth() + 3;
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Color c = TerminalCore.selected == this ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() && !(SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added) ? Colors.explorerLight : Colors.explorer;

		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x - 1, y - 1, width + 2, HEIGHT + 2);

		g.setColor(c);
		
		if (TerminalCore.selected == this)
			g.drawRect(x, y, width, HEIGHT);
		
		final int imageSize = 24;
		g.drawImage(sprite, x + 10, y + ((HEIGHT / 2) - (imageSize / 2)) + 1, imageSize, imageSize, null);
		
		Fonts.drawString(name, x + 37, y + (HEIGHT / 2) - 9 /*16 / 2*/, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
		
		button.render(g);
	}
}
