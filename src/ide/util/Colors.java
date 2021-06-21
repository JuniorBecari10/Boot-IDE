package ide.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Uma classe para organizar as cores. Como se fosse uma paleta. Também tem outras coisas úteis.
 */
public class Colors {
    
    public static Color background = 	  Color.decode("#353b48");
    public static Color backgroundLight = Color.decode("#28394d");
    public static Color explorer = 		  Color.decode("#222f3e");

    public static Color explorerLight =   Color.decode("#2d3f54");
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
    
    public static Color select = 		   Color.decode("#8c8c8c");
    
    public static Color select1 = 		  Color.decode("#ff6961");
    public static Color select2 = 		  Color.decode("#ff5147");
    public static Color selectCursor = 	  			   Color.blue;
    public static Color other =						  Color.white;
    public static Color error =			  Color.decode("#ff6961");
    public static Color cursor = 					  Color.white;
    public static Color lineNumber =			 Colors.textLight;
    public static Color selectedLineNumber=Color.decode("#c5d5ea");
    
    public static void revertColors() {
    	 background = Color.decode("#353b48");
         backgroundLight = Color.decode("#28394d");
         explorer = Color.decode("#222f3e");

         explorerLight = Color.decode("#2d3f54");
         textLight = Color.decode("#95afc0");
         textLighter = Color.decode("#A9B4C2");
        
         objects = 		  Color.decode("#94fa92");
         methods = 		  Color.decode("#e7d789");
         numbers = 		  Color.decode("#5485b6");
         keywords = 		  Color.decode("#95bddc");
         variables = 	  Color.decode("#66e1f8");
        
         comments = 					  textLighter;
         strings = 					    textLight;
         symbols = 						 comments;
        
         /*select1 = 		  Color.decode("#ff6961");
         select2 = 		  Color.decode("#ff5147");
         selectCursor = 	  Color.blue;*/
         other =						  Color.white;
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
}
