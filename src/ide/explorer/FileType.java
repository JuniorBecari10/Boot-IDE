package ide.explorer;

import java.awt.image.BufferedImage;

public class FileType {

	private String extension;
	private BufferedImage icon;
	
	public FileType(String extension, BufferedImage icon) {
		this.extension = extension;
		this.icon = icon;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public BufferedImage getIcon() {
		return icon;
	}

	public void setIcon(BufferedImage icon) {
		this.icon = icon;
	}
}
