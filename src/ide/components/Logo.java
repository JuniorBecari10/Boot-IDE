package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.codeeditor.CodeEditor;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;

public class Logo extends IDEComponent {
	
	private boolean showMessage1 = true;
	private boolean show = true;

	public Logo(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		super.tick();
		
		if (Main.baseFolder != null)
			showMessage1 = false;
		
		show = CodeEditor.editing == null;
		
		x = (int) (Main.screen.getWidth() / 2 + 80);
		y = (int) (Main.screen.getHeight() / 2 - 120);
	}
	
	public void render(Graphics g) {
		if (!show) return;
		
		super.render(g);
		
		if (showMessage1) {
			Fonts.drawString("Não há nenhuma pasta carregada.", x - 140, y + 170, new IDEFont(Fonts.lighterGrayNormal, 20), g);
			Fonts.drawString("Clique no botão [Selecionar Pasta Base]", x - 190, y + 220, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString("para carregar uma.", x - 50, y + 250, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
		else {
			Fonts.drawString("[Ctrl + T] Terminal de Comando", x - 140, y + 190, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString("[Ctrl + Windows] Prompt de Comando", x - 140, y + 220, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString("[Clique Direito] Mais Opções", x - 140, y + 250, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
