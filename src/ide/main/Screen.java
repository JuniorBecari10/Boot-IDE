package ide.main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ide.input.KeyInput;
import ide.input.MouseInput;

public class Screen extends Canvas {
    
    private static final long serialVersionUID = 1L;

    public JFrame frame;
    
    // initial dimensions

    public static final int WIDTH =  (int) getScreenSize().getWidth();
    public static final int HEIGHT = (int) getScreenSize().getHeight() - 50;

    public BufferedImage layer;

    private MouseInput mouseInput;
    private KeyInput keyInput;

    public Screen(String title) {
        initWindow(title, new Dimension(Screen.WIDTH, Screen.HEIGHT));

        layer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        mouseInput = new MouseInput();
        keyInput = new KeyInput();

        addMouseListener(mouseInput);
        addMouseMotionListener(mouseInput);
        addMouseWheelListener(mouseInput);

        addKeyListener(keyInput);
    }

    private void initWindow(String title, Dimension d) {
        setPreferredSize(d);

        frame = new JFrame(title);
        
        frame.add(this);
		frame.setResizable(true);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
    }
    
    public void setFrameIcon(Image icon) {
    	frame.setIconImage(icon);
    }

    public static Dimension getScreenSize() {
        return Main.toolkit.getScreenSize();
    }
}
