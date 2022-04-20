package ide.main;

public class Start {
    public static void main(String[] args) {
    	Main.args = args;
    	
    	Main.main = new Main();
    	Main.main.start();
    }
}
