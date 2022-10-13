package ide.components;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Clickable;
import ide.util.Renderable;
import ide.util.Tickable;

/**
 * A superclasse de todo componente da IDE.
 */
public abstract class IDEComponent implements Tickable, Renderable, Clickable { // uma classe abstrata nao pode ser instanciada, a boa para superclasses.

    public static transient List<IDEComponent> components = new ArrayList<>();
    
    public static transient List<IDEComponent> toRemove = new ArrayList<>();
    public static transient List<IDEComponent> toAdd = new ArrayList<>();

    protected int x, y;
    protected int width, height;

    protected transient BufferedImage sprite;

    public IDEComponent(int x, int y, int width, int height, BufferedImage sprite) {
        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.sprite = sprite;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public BufferedImage getSprite() {
        return sprite;
    }
    
    public void type() {}
    
    @Override
    public void tick() {}
    
    public static final void addRightClickOptions(int initialX, int initialY, RightClickOption... options) {
    	if (RightClickOption.isRightClickActive()) return;
    	
    	int width = options[0].getWidth();
    	
    	while (initialX + width > Main.screen.getWidth() - 1)
    		initialX--;
    	
    	while (initialY + (RightClickOption.HEIGHT * options.length) > Main.screen.getHeight() - 1)
    		initialY--;
    	
    	for (int i = 0; i < options.length; i++) {
    		options[i].setX(initialX);
    		options[i].setY(initialY + i * RightClickOption.HEIGHT);
    		
    		toAdd.add(options[i]);
    	}
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    @Override
    public final boolean leftClicked() { // um matodo final nao pode ser sobrescrito. Bom para quem quer que o anico matodo seja esse.
    	return hovered() && MouseInput.isLeftPressed();
    }
    
    @Override
    public final boolean rightClicked() { // um matodo final nao pode ser sobrescrito. Bom para quem quer que o anico matodo seja esse.
    	return hovered() && MouseInput.isRightPressed();
    }

    @Override
    public boolean hovered() {
        Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
        Rectangle comp = new Rectangle(x, y, width, height);

        return mouse.intersects(comp);
    }

    @Override
    public final boolean dragged() {
        return hovered() && MouseInput.isMouseDragged();
    }
    
}
