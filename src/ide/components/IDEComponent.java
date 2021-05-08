package ide.components;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ide.input.MouseInput;
import ide.util.Clickable;
import ide.util.Renderable;
import ide.util.Tickable;

/**
 * A superclasse de todo componente da IDE.
 */
public abstract class IDEComponent implements Tickable, Renderable, Clickable { // uma classe abstrata não pode ser instanciada, é boa para superclasses.

    public static IDEComponent selected;

    public static List<IDEComponent> components = new ArrayList<>();
    
    public static List<IDEComponent> toRemove = new ArrayList<>();
    public static List<IDEComponent> toAdd = new ArrayList<>();

    protected int x, y;
    protected int width, height;

    protected BufferedImage sprite;
    
    protected boolean enabled;

    public IDEComponent(int x, int y, int width, int height, BufferedImage sprite) {
        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.sprite = sprite;
    }
    
    public void setEnabled(boolean b) {
    	enabled = b;
    }
    
    public boolean isEnabled() {
    	return enabled;
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

    @Override
    public void tick() {}
    
    protected static final void addRightClickOption(int x, int y, int width, String text, ExecuteCommand command, String clickArg) {
    	toAdd.add(new RightClickOption(x, y, width, text, command, clickArg));
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    @Override
    public final boolean leftClicked() { // um método final não pode ser sobrescrito. Bom para quem quer que o único método seja esse.
    	return hovered() && MouseInput.isLeftPressed();
    }
    
    @Override
    public final boolean rightClicked() { // um método final não pode ser sobrescrito. Bom para quem quer que o único método seja esse.
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
