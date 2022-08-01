package ide.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Spritesheet {

    private BufferedImage spr;

    public Spritesheet(String path) {
    	try {
            spr = ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Spritesheet(File file) {
    	try {
			spr = ImageIO.read(file);
		} catch (IOException e) {
			// n�o tem
			
			return;
		}
    }
    
    /*public Spritesheet(BufferedImage spr) {
    	this.spr = spr;
    }*/

    /**
     * Recorta a Spritesheet e retorna a parte recortada.
     * 
     * @param x - A posição x do recorte
     * @param y - A posição y do recorte
     * @param w - A largura do recorte
     * @param h - A altura do recorte
     * 
     * @return Uma {@code BufferedImage} com a parte recortada.
     */
    public BufferedImage getSprite(int x, int y, int w, int h) {
        return spr.getSubimage(x, y, w, h);
    }
}
