package ide.terminal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;

public class TerminalTab extends IDEComponent {
	
	public static final int Y_EXPLORER = Screen.DECORATION_HEIGHT + 210;
	public static final int HEIGHT = 30;
	
	public String name;
	private File log;
	
	private String[] lines;
	public Thread reader;
	
	public TerminalTab(int x, int y, int width, String name) {
		super(x, y, width, HEIGHT, Main.term12Px);
		
		this.name = name;
		this.log = new File(Main.userDir + File.separator + name);
		
		try {
			this.log.createNewFile();
			this.log.deleteOnExit();
			
			// Write header
			
			BufferedWriter w = new BufferedWriter(new FileWriter(log));
			
			w.write(Main.PROGRAM_NAME + " Terminal\n\n");
			w.write(TerminalCore.prompt + " ");
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			lines = Files.readAllLines(log.toPath()).toArray(new String[0]);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		reader = new Thread() {
			public void run() {
				while (true) {
					try {
						lines = Files.readAllLines(log.toPath()).toArray(new String[0]);
						
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		//reader.start();
	}
	
	public String[] getLines() {
		return lines;
	}
	
	public File getLog() {
		return log;
	}
	
	public void select() {
		TerminalCore.selected = this;
	}
	
	public void tick() {
		if (leftClicked())
			select();
		
		// close thread if tab closed
		
		width = Main.explorer.getWidth() / 2;
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
		else
			g.drawLine(x + width, y, x + width, Screen.DECORATION_HEIGHT + HEIGHT);
		
		final int imageSize = 24;
		g.drawImage(sprite, x + 10, y + ((HEIGHT / 2) - (imageSize / 2)) + 1, imageSize, imageSize, null);
		
		Fonts.drawString(name, x + 40, y + (HEIGHT / 2) - 9 /*16 / 2*/, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
	}
}
