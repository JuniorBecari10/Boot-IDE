package topcomponents;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.util.Colors;

/**
 * Um IDEComponent, só que do topo da tela, isso inclui botões da janela (Minimizar, Maximizar e Fechar) e menus (File, Open etc.)
 * 
 * @author Juninho
 *
 */
public abstract class TopComponent extends IDEComponent {
	
	public static List<TopComponent> topComponents = new ArrayList<>();
	
	public TopComponent(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.explorerLighter);
			g.fillRect(x, y, width, height);
		}
		
		g.drawImage(sprite, x + 4, y + 4, width - 8, height - 8, null);
	}
}
