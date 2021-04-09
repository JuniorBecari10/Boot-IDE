package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;

public class Logo extends IDEComponent {

	public Logo(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		super.tick();
		
		if (Main.baseFolder != null)
			IDEComponent.toRemove.add(this);
		
		x = (int) (Main.screen.getWidth() / 2 + 80);
		y = (int) (Main.screen.getHeight() / 2 - 120);
	}
	
	public void render(Graphics g) {
		super.render(g);
		
		Fonts.drawString("Não há nenhuma pasta carregada.", x - 140, y + 170, new IDEFont(Fonts.lighterGrayNormal, 20), g);
		Fonts.drawString("Clique no botão [Selecionar Pasta Base]", x - 190, y + 220, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("para carregar uma.", x - 50, y + 250, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
