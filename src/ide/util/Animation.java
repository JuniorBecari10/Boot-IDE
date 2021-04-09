package ide.util;

/**
 * Um jeito fácil de rodar as animações.
 * 
 * @author Juninho
 *
 */
public abstract class Animation {

	private int fps;
	private boolean isLoop;
	
	public Animation(int fps, boolean isLoop) {
		this.fps = fps;
		this.isLoop = isLoop;
	}
	
	public void play() {
		applyConfig();
	}
	
	private final void applyConfig() {
		try {
			Thread.sleep(1000/fps);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (isLoop) play();		// para dar loop utilize recursão
	}
}
