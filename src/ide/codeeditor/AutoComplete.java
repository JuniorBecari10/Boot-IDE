package ide.codeeditor;

public class AutoComplete {

	public String text;
	public AutoCompleteType type;
	
	public AutoComplete(String text, AutoCompleteType type) {
		this.text = text;
		this.type = type;
	}
}
