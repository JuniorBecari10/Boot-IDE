package screen;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.dnd.DropTarget;

import javax.swing.JFrame;

import ide.input.ComponentInput;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.main.ComponentResizer;
import ide.main.DragListener;
import ide.main.Main;

public class Screen extends Canvas {
    
    private static final long serialVersionUID = 1L;

    public JFrame frame;
    
    // initial dimensions
    
    public static final int DECORATION_HEIGHT = 30;

    public static final int WIDTH =  (int) getScreenSize().getWidth();
    public static final int HEIGHT = (int) getScreenSize().getHeight() - 50;
    
    public static final int MIN_W = 980; // minimized width
    public static final int MIN_H = 520; // minimized height
    
    @SuppressWarnings("unused")
	private DropTarget dt;

    private MouseInput mouseInput;
    private KeyInput keyInput;
    
    private WindowInput windowInput;
    private DragListener dragListener;

    private ComponentInput componentInput;
    
    public ComponentResizer cr;
    
    public Screen(String title) {
    	initWindow(title, new Dimension(MIN_W, MIN_H));

        mouseInput = new MouseInput();
        keyInput = new KeyInput();
        windowInput = new WindowInput();
        dragListener = new DragListener();
        componentInput = new ComponentInput();

        addKeyListener(keyInput);
        addMouseListener(mouseInput);
        addMouseMotionListener(mouseInput);
        addMouseWheelListener(mouseInput);
        addComponentListener(componentInput);
        
        frame.addComponentListener(componentInput);
        
        frame.addWindowListener(windowInput);
        
        dt = new DropTarget(frame, dragListener);
        
        setFocusTraversalKeysEnabled(false);
    }

    private void initWindow(String title, Dimension d) {
        setPreferredSize(d);

        frame = new JFrame(title);
        
        frame.add(this);
		//frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setFocusTraversalKeysEnabled(false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);			// pra fazer a tela de confirma��o o fechamento deve ser feito por c�digo		
		frame.setUndecorated(true);
		
        frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setFocusTraversalKeysEnabled(false); // tem q ter esses dois
		frame.setVisible(true);
		//frame.setUndecorated(true); // ele aqui causa exception
		requestFocus();
    }
    
    public void setFrameIcon(Image icon) {
    	frame.setIconImage(icon);
    }

    public static Dimension getScreenSize() {
    	/*DisplayMode mode = Main.screen.frame.getGraphicsConfiguration().getDevice().getDisplayMode();
    	
        return new Dimension(mode.getWidth(), mode.getHeight());*/
    	return Main.toolkit.getScreenSize();
    }
}