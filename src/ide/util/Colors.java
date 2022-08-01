package ide.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import ide.explorer.ListableFile;
import ide.main.Main;

/**
 * Uma classe para organizar as cores. Como se fosse uma paleta. Tamb�m tem outras coisas �teis.
 */
public class Colors {
    
	public static final Color textLightDefault = Color.decode("#95afc0");
	
    public static Color background = 	  Color.decode("#353b48");
    public static Color background2 = 	  Color.decode("#29394a");
    public static Color backgroundLight = Color.decode("#28394d");
    public static Color explorer = 		  Color.decode("#222f3e");
    public static Color codeEditor = 	  explorer;

    public static Color explorerLight =   Color.decode("#2d3f54"); // #2d3f54 // #32475e
    public static Color explorerLighter = Color.decode("#354d69");
    public static Color textLight = 	  Color.decode("#95afc0");
    public static Color textLighter =     Color.decode("#A9B4C2");
    
    public static Color objects = 		  Color.decode("#94fa92");
    public static Color methods = 		  Color.decode("#e7d789");
    public static Color numbers = 		  Color.decode("#5485b6");
    public static Color keywords = 		  Color.decode("#95bddc");
    public static Color variables = 	  Color.decode("#80d1f2"); // 66e1f8
    
    public static Color comments = 					  textLighter;
    public static Color strings = 					    textLight;
    public static Color symbols = 						 comments;
    
    public static Color selection = 	  Color.decode("#8c8c8c");
    
    public static Color other =						  Color.white;
    public static Color error =			  Color.decode("#ff6961");
    public static Color cursor = 					  Color.white;
    public static Color lowerBar = 		   Colors.backgroundLight;
    public static Color lineNumber =			 Colors.textLight;
    public static Color selectedLineNumber=Color.decode("#c5d5ea");
    
    public static void revertColors() {
    	Color textLightOld = textLight;
    	
        background = 	  Color.decode("#353b48");
        background2 = 	  Color.decode("#29394a");
        backgroundLight = Color.decode("#28394d");
        explorer = 		  Color.decode("#222f3e");
        codeEditor = 	  explorer;

        explorerLight =   Color.decode("#2d3f54"); // #2d3f54 // #32475e
        explorerLighter = Color.decode("#354d69");
        textLight = 	  Color.decode("#95afc0");
        textLighter =     Color.decode("#A9B4C2");
        
        objects = 		  Color.decode("#94fa92");
        methods = 		  Color.decode("#e7d789");
        numbers = 		  Color.decode("#5485b6");
        keywords = 		  Color.decode("#95bddc");
        variables = 	  Color.decode("#80d1f2"); // 66e1f8
        
        comments = 					  textLighter;
        strings = 					    textLight;
        symbols = 						 comments;
        
        selection = 	  Color.decode("#8c8c8c");
        
        other =						  Color.white;
        error =			  Color.decode("#ff6961");
        cursor = 					  Color.white;
        lowerBar = 		   Colors.backgroundLight;
        lineNumber =			 Colors.textLight;
        selectedLineNumber=Color.decode("#c5d5ea");
         
         ////
         
         Main.baseFolderSpr = Colors.swapColor(Main.baseFolderSpr, textLightOld, Colors.textLight);
         
         Main.newFileSpr = Colors.swapColor(Main.newFileSpr, textLightOld, Colors.textLight);
         Main.newFolderSpr = Colors.swapColor(Main.newFolderSpr, textLightOld, Colors.textLight);
         Main.folderUp = Colors.swapColor(Main.folderUp, textLightOld, Colors.textLight);
         Main.backBaseFolder = Colors.swapColor(Main.backBaseFolder, textLightOld, Colors.textLight);
         Main.reloadSpr = Colors.swapColor(Main.reloadSpr, textLightOld, Colors.textLight);
         
         Main.star = Colors.swapColor(Main.star, textLightOld, Colors.textLight);
         Main.folder = Colors.swapColor(Main.folder, textLightOld, Colors.textLight);
         
         Main.closeTab = Colors.swapColor(Main.closeTab, textLightOld, Colors.textLight);
         Main.notSavedTab = Colors.swapColor(Main.notSavedTab, textLightOld, Colors.textLight);
         
         Main.lock = Colors.swapColor(Main.lock, textLightOld, Colors.textLight);
         
         ListableFile.generateLocalConfigFile(Main.defaultConfigFile);
         Main.load(Main.conffile);
         
         if (Main.editor.editing.getRegent().getRegent().equals(Main.defaultConfigFile)) {
			try {
				Main.editor.lines = Main.editor.readFile(Main.defaultConfigFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
         }
    }
    
    /**
     * Troca a cor especificada em {@code target} na cor especificada em {@code out}.
     * 
     * @param img - A imagem
     * @param out - A cor que vai trocar
     * @param target - A cor que vai ser trocada
     * 
     * @return A imagem com as cores trocadas.
     */
    public static BufferedImage swapColor(BufferedImage img, Color target, Color out) {
    	for (int x = 0; x < img.getWidth(); x++)
    		for (int y = 0; y < img.getHeight(); y++)
    			if (img.getRGB(x, y) == target.getRGB())
    				img.setRGB(x, y, out.getRGB());
    	
    	return img;
    }
    
    public static String toHex(Color c) {
    	String r = Integer.toHexString(c.getRed());
    	String g = Integer.toHexString(c.getGreen());
    	String b = Integer.toHexString(c.getBlue());
    	
    	return "#" + r + g + b;
    }
    
    public static int[] getColors(BufferedImage img) {
    	int[] pixels = new int[img.getWidth() * img.getHeight()];
    	img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
    	
    	return pixels;
    }
    
    public static Color setAlpha(Color c, int alpha) {
    	return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
    
    /*public static BufferedImage brightness(BufferedImage img, float scaleFactor) {
    	RescaleOp op = new RescaleOp(scaleFactor, 0, null);
    	
    	return op.filter(img, null);
    }*/
    
    /*public static BufferedImage darker(BufferedImage img) {
    	for (int x = 0; x < img.getWidth(); x++)
    		for (int y = 0; y < img.getHeight(); y++)
    			img.setRGB(x, y, new Color(img.getRGB(x, y)).darker().getRGB());
    	
    	return img;
    }*/
}
