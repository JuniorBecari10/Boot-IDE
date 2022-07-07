package topcomponents;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;

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
}
