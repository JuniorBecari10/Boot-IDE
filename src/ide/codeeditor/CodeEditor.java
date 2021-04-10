package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;

// Nota: para escrever em vermelho no console, ao invés de digitar System.out.println("texto"); use System.err.println("texto");

public class CodeEditor extends IDEComponent {
	
	public static int FONT_SIZE = 16; // 18, 16
	
	public static final IDEFont DEFAULT_FONT = new IDEFont(Fonts.normal, FONT_SIZE);
	
	public static Tab editing;
	
	private boolean showCursorData = false;
	
	public static boolean selectMode;
	public static boolean isSelectingFirst = true;
	
	public static boolean selecting;

	public static int line1, line2;
	public static int index1, index2;
	
	private PressedAccent prAcc;
	private boolean pressedAccent = false;
	
	public static List<IDELine> lines = new ArrayList<>();
	
	public static int cursorX = 0;
	public static int cursorY = 1;
	
	public static int scrX = 0;
	public static int scrY = 0;
	
	public static int tabScr = 0;
	
	public static List<Tab> tabs;
	public static List<Tab> toAdd;
	public static List<Tab> toRemove;
	
	public static BufferedImage gradient;
	
	public static String clipboard = "";
	
	public static final int MIN_Y = 35;
	
	private boolean showCursor;
	
	private static Animation cursor;

	public CodeEditor(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		tabs = new ArrayList<>();
		toAdd = new ArrayList<>();
		toRemove = new ArrayList<>();
		
		cursor = new Animation(2, true) {
			private boolean flip = false;
			
			public void play() {
				showCursor = !flip;
				
				flip = !flip;
				
				super.play();
			}
		};
		
		new Thread() {
			public void run() {
				cursor.play();
			}
		}.start();
		
		try {
			gradient = ImageIO.read(getClass().getResource("/gradient.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hovered() {
		Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
        Rectangle comp = new Rectangle(x, MIN_Y, width, height);

        return mouse.intersects(comp);
	}
	
	public static List<IDELine> readFile(File file) throws IOException {
		CodeEditor.line1 = 0;
		CodeEditor.line2 = 0;
		
		CodeEditor.index1 = 0;
		CodeEditor.index2 = 0;
		
		CodeEditor.selecting = false;
		
		List<String> l = null;
		
		Path p = file.toPath();
			
		/*
		try {														// tenta ler em todos os tipos de codificação, mas n dá
			l = Files.readAllLines(p, StandardCharsets.UTF_8);
		} catch (MalformedInputException a) {
			try {
				l = Files.readAllLines(p, StandardCharsets.UTF_16);
			} catch (MalformedInputException b) {
				try {
					l = Files.readAllLines(p, StandardCharsets.UTF_16BE);
				} catch (MalformedInputException c) {
					try {
						l = Files.readAllLines(p, StandardCharsets.UTF_16LE);
					} catch (MalformedInputException d) {
						try {
							l = Files.readAllLines(p, StandardCharsets.US_ASCII);
						} catch (MalformedInputException e) {
							l = Files.readAllLines(p, StandardCharsets.ISO_8859_1);
						}
					}
				}
			}
		}
		*/
		
		try {
			l = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
		}
		catch (Exception e) {
			l = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
		}
			
		if (l.isEmpty()) l.add("");
		
		List<IDELine> ls = new ArrayList<>();
		
		for (String s : l) {
			List<Character> cs = new ArrayList<>();
			List<IDEFont> fs = new ArrayList<>();
			
			for (char c : s.toCharArray())
				cs.add(c);
			
			for (int i = 0; i < cs.size(); i++)
				fs.add(DEFAULT_FONT);
			
			IDELine gen = new IDELine(cs, fs);
			
			ls.add(gen);
		}
		
		return ls;
			
	}
	
	public static List<Integer> findWord(String textString, String word) { // Fonte: baeldung.com
        List<Integer> indexes = new ArrayList<Integer>();
        
        String lowerCaseTextString = textString;//.toLowerCase();		// não vai ter lowercase,
        String lowerCaseWord = word;//.toLowerCase();					//tem q ter diferença de letras capitais

        int index = 0;
        
        while (index != -1) {
            index = lowerCaseTextString.indexOf(lowerCaseWord, index);
            
            if (index != -1) {
                indexes.add(index);
                
                index++;
            }
        }
        return indexes;
    }
	
	public static List<IDEFont> color(int s, int e, IDEFont color, List<IDEFont> fs) {
		if (e < s) return fs;
		
		for (int i = s; i < e; i++)
			fs.set(i, color);
		
		return fs;
	}
	
	public static List<IDEFont> automaticColor(char[] chars, String ext) { // otimizar esses algoritmos
		List<IDEFont> fs = new ArrayList<>();
		
		for (int i = 0; i < chars.length; i++)
			fs.add(DEFAULT_FONT);
		
		List<Integer> indxs = new ArrayList<>();
		
		String[] nums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
				  "1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a", "0a", // hex
				  "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b", "0b",
				  "1c", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "0c",
				  "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "0d",
				  "1e", "2e", "3e", "4e", "5e", "6e", "7e", "8e", "9e", "0e",
				  "1f", "2f", "3f", "4f", "5f", "6f", "7f", "8f", "9f", "0f",
				  "1l", "2l", "3l", "4l", "5l", "6l", "7l", "8l", "9l", "0l"}; // long
		
		if ((ext.equals(".java") || ext.equals(".c") || ext.equals(".cs") || ext.equals(".cpp") || ext.equals(".js") || ext.equals(".h") || ext.equals(".lua"))) {			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			String[] cl = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
					"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

			for (String s : cl)
				indxs = Stream.concat(indxs.stream(), findWord(new String(chars), s).stream()).collect(Collectors.toList());

			int len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != ')')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.objectsNormal, FONT_SIZE), fs);
			}
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		indxs = findWord(new String(chars), "(");
		
		for (Integer i : indxs) {
			int c = i;
			int len = 0;
			
			while (c < chars.length && c > 0 && (chars[c] != ' ' || chars[c] != '(' || chars[c] != '[' || chars[i + len] != ',' || chars[i + len] != ';')) {
				c--;
				len++;
			}
			
			fs = color(c, c + len, new IDEFont(Fonts.methodsNormal, FONT_SIZE), fs);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		for (String s : nums) { // colorir números
			indxs = findWord(new String(chars), s);

			for (Integer i : indxs)
				fs = color(i, i + s.length(), new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		String[] gens = { " ", "(", ")", "[", "]", "{", "}", ",", ".", "<", ">", ";", ":", "?", "/", "|", "+", "-", "*", "=", "&", "%", "$", "#", "!" };
		
		for (String s : gens) {
			indxs = findWord(new String(chars), s);

			for (Integer i : indxs)
				fs = color(i, i + s.length(), new IDEFont(Fonts.normal, FONT_SIZE), fs);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), new Character((char) 34).toString()); // colorir strings

		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.lightGrayNormal, FONT_SIZE), fs);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), new Character((char) 39).toString()); // colorir chars

		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.lightGrayNormal, FONT_SIZE), fs);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		switch (ext) {
		case ".java":
			String[] javaKeys = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
					"continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
					"for", "goto", "if", "implements", "import", "instanceof", "int ","interface ","long", "native",
					"new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
					"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
					"true", "false", "null", "@interface" };
			
			for (String s : javaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
			if (!(fs.size() == 0 || indxs.size() == 0))
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			break;		// depois colorir comentários multi-linha
			
		case ".html":
			String[] tags = { "<!--", "<!doctype", "<!DOCTYPE", "<a", "<abbr", "<acronym", "<address", "<applet", "<area", "<article",
					"<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo", "<big", "<blockquote", "<body", "<br", "<button",
					"<canvas", "<caption", "<center", "<cite", "<code", "<col", "<colgroup", "<data", "<datalist", "<dd", "<del",
					"<details", "<dfn", "<dialog", "<dir", "<div", "<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure",
					"<font", "<footer", "<form", "<frame", "<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header",
					"<hr", "<html", "<i", "<iframe", "<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main",
					"<map", "<mark", "<meta", "<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option",
					"<output", "<p", "<param", "<picture", "<pre", "<progress", "<q", "<rp", "<rt", "<ruby", "<s", "<samp", "<script",
					"<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg", "<table",
					"<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr", "<track", "<tt",
					"<u", "<ul", "<var", "<video", "<wbr",
					"</a", "</abbr", "</acronym", "</address", "</applet", "</area", "</article",
					"</aside", "</audio", "</b", "</base", "</basefont", "</bdi", "</bdo", "</big", "</blockquote", "</body", "</br", "</button",
					"</canvas", "</caption", "</center", "</cite", "</code", "</col", "</colgroup", "</data", "</datalist", "</dd", "</del",
					"</details", "</dfn", "</dialog", "</dir", "</div", "</dl", "</dt", "</em", "</embed", "</fieldset", "</figcaption", "</figure",
					"</font", "</footer", "</form", "</frame", "</frameset", "</h1", "</h2", "</h3", "</h4", "</h5", "</h6", "</head", "</header",
					"</hr", "</html", "</i", "</iframe", "</img", "</input", "</ins", "</kbd", "</label", "</legend", "</li", "</link", "</main",
					"</map", "</mark", "</meta", "</meter", "</nav", "</noframes", "</noscript", "</object", "</ol", "</optgroup", "</option",
					"</output", "</p", "</param", "</picture", "</pre", "</progress", "</q", "</rp", "</rt", "</ruby", "</s", "</samp", "</script",
					"</section", "</select", "</small", "</source", "</span", "</strike", "</strong", "</style", "</sup", "</svg", "</table",
					"</tbody", "</td", "</template", "</textarea", "</tfoot", "</th", "</thead", "</time", "</title", "</tr", "</track", "</tt",
					"</u", "</ul", "</var", "</video", "</wbr" };
			
			for (String s : tags) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ">"); // colorir final de tags
			
			for (Integer i : indxs) {
				fs = color(i, i + 1, new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".htm":
			String[] tagss = { "<!--", "<!doctype", "<!DOCTYPE", "<a", "<abbr", "<acronym", "<address", "<applet", "<area", "<article",
					"<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo", "<big", "<blockquote", "<body", "<br", "<button",
					"<canvas", "<caption", "<center", "<cite", "<code", "<col", "<colgroup", "<data", "<datalist", "<dd", "<del",
					"<details", "<dfn", "<dialog", "<dir", "<div", "<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure",
					"<font", "<footer", "<form", "<frame", "<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header",
					"<hr", "<html", "<i", "<iframe", "<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main",
					"<map", "<mark", "<meta", "<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option",
					"<output", "<p", "<param", "<picture", "<pre", "<progress", "<q", "<rp", "<rt", "<ruby", "<s", "<samp", "<script",
					"<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg", "<table",
					"<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr", "<track", "<tt",
					"<u", "<ul", "<var", "<video", "<wbr",
					"</a", "</abbr", "</acronym", "</address", "</applet", "</area", "</article",
					"</aside", "</audio", "</b", "</base", "</basefont", "</bdi", "</bdo", "</big", "</blockquote", "</body", "</br", "</button",
					"</canvas", "</caption", "</center", "</cite", "</code", "</col", "</colgroup", "</data", "</datalist", "</dd", "</del",
					"</details", "</dfn", "</dialog", "</dir", "</div", "</dl", "</dt", "</em", "</embed", "</fieldset", "</figcaption", "</figure",
					"</font", "</footer", "</form", "</frame", "</frameset", "</h1", "</h2", "</h3", "</h4", "</h5", "</h6", "</head", "</header",
					"</hr", "</html", "</i", "</iframe", "</img", "</input", "</ins", "</kbd", "</label", "</legend", "</li", "</link", "</main",
					"</map", "</mark", "</meta", "</meter", "</nav", "</noframes", "</noscript", "</object", "</ol", "</optgroup", "</option",
					"</output", "</p", "</param", "</picture", "</pre", "</progress", "</q", "</rp", "</rt", "</ruby", "</s", "</samp", "</script",
					"</section", "</select", "</small", "</source", "</span", "</strike", "</strong", "</style", "</sup", "</svg", "</table",
					"</tbody", "</td", "</template", "</textarea", "</tfoot", "</th", "</thead", "</time", "</title", "</tr", "</track", "</tt",
					"</u", "</ul", "</var", "</video", "</wbr" };
			
			for (String s : tagss) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ">"); // colorir final de tags
			
			for (Integer i : indxs) {
				fs = color(i, i + 1, new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "-->"); // colorir final de tags de comentário
			
			for (Integer i : indxs) {
				fs = color(i, i + 3, new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".css":
			String[] tagsss = { "!--", "!doctype", "!DOCTYPE", "a", "abbr", "acronym", "address", "applet", "area", "article",
					"aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
					"canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del",
					"details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
					"font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header",
					"hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main",
					"map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option",
					"output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script",
					"section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table",
					"tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt",
					"u", "ul", "var", "video", "wbr" };
			
			String[] props = { "align-content", "align-items", "all", "animation", "animation-direction",
					"animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name",
					"animation-play-state", "animation-timing-function", "backface-visibility", "background",
					"background-attachment", "background-blend-mode", "background-clip", "background-color",
					"background-image", "background-origin", "background-position", "background-repeat",
					"background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
					"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
					"border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice",
					"border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color",
					"border-right-style", "border-right-width", "border-spacing", "border-style", "border-top",
					"border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style",
					"border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing",
					"break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear",
					"clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color",
					"column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content",
					"counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter",
					"flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
					"float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values",
					"font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch",
					"font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps",
					"font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
					"font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows",
					"grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template",
					"grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation",
					"height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes",
					"left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image",
					"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left",
					"margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media",
					"min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity",
					"order", "orphans", "outline", "outline-color", "outline-offset", "outline-style",
					"outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding",
					"padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after",
					"page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events",
					"position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout",
					"text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color",
					"text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation",
					"text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform",
					"transform-origin", "transform-style", "transition", "transition-delay", "transition-duration",
					"transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align",
					"visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap",
					"writing-mode", "z-index" };
			
			for (String s : tagsss) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			for (String s : props) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".py":
			String[] pyKeys = { "and", "as", "assert", "break", "class",
					"continue", "def", "del", "elif", "else", "except ", "False",
					"finally", "for", "from", "global", "if", "import ", "in", "is",
					"lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return",
					"True", "try", "while ", "with ", "yield" };
			for (String s : pyKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
				indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
				
				if (fs.size() == 0 || indxs.size() == 0) break;
				
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			
			break;
			
		case ".c":
			String[] cKeys = { "auto", "break", "case", "char", "const",
					"continue", "default", "do", "double", "else", "enum", "extern",
					"float", "for", "goto", "if", "int", "long", "register", "return",
					"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
					"union", "unsigned", "void", "volatile", "while", "true", "false", "null" };
			
			for (String s : cKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}	
				indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
				
				if (fs.size() == 0 || indxs.size() == 0) break;
				
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			break;
			
		case ".cpp":
			String[] cppKeys = { "auto", "break", "case", "char", "const",
					"continue", "default", "do", "double", "else", "enum", "extern",
					"float", "for", "goto", "if", "int", "long", "register", "return",
					"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
					"union", "unsigned", "void", "volatile", "while",
					"asm", "dynamic_cast", "namespace", "reinterpret_cast", "bool",
					"explicit", "new", "static_cast", "false", "catch", "operator", "template",
					"friend", "private", "class", "this", "inline", "public", "throw", "const_cast",
					"delete", "mutable", "protected", "true", "try", "typeid", "typename", "using", "virtual", "wchar_t"};
			
			for (String s : cppKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
				indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
				
				if (fs.size() == 0 || indxs.size() == 0) break;
				
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			break;
			
		case ".cs":
			String[] csKeys = { "abstract", "async", "const", "event", "extern", "new",
					"override", "partial", "readonly", "sealed", "static", "unsafe", "virtual",
					"volatile", "public", "private", "internal", "protected", "if", "else", "switch",
					"case", "do", "for", "foreach", "in", "while", "break", "continue", "default", "goto",
					"return", "yield", "throw", "try", "catch", "finally", "checked", "unchecked", "fixed",
					"lock", "params", "ref", "out", "using", "alias", "await", "sizeof", "typeof",
					"stackalloc", "is", "base", "this", "null", "false", "true", "value", "void", "bool", "byte",
					"char", "class", "decimal", "double", "enum", "float", "int", "long", "sbyte", "short", "string",
					"struct", "uint", "ulong", "ushort", "add", "var", "dynamic", "global", "set", "namespace" };
			
			for (String s : csKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
				indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
				
				if (fs.size() == 0 || indxs.size() == 0) break;
				
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			break;
			
		case ".bat":
			String[] batCom = { "ver", "assoc", "cd", "cls", "copy", "del", "dir", "date",
					"echo", "@echo", "exit", "md", "move", "path", "pause", "prompt", "rd",
					"rem", "start", "time", "type", "vol", "attrib", "chkdsk", "choice", "cmd",
					"comp", "convert", "driverquery", "expand", "find", "format", "help", "ipconfig",
					"label", "more", "net", "ping", "shutdown", "sort", "subst", "subst", "systeminfo",
					"taskkill", "xcopy", "tree", "fc", "title", "set", "VER", "ASSOC", "CD", "CLS",
					"COPY", "DEL", "DIR", "DATE", "ECHO", "@ECHO", "EXIT", "MD", "MOVE", "PATH", "PAUSE",
					"PROMPT", "RD", "REM", "START", "TIME", "TYPE", "VOL", "ATTRIB", "CHKDSK", "CHOICE",
					"CMD", "COMP", "CONVERT", "DRIVERQUERY", "EXPAND", "FIND", "FORMAT", "HELP", "IPCONFIG",
					"LABEL", "MORE", "NET", "PING", "SHUTDOWN", "SORT", "SUBST", "SUBST", "SYSTEMINFO",
					"TASKKILL", "XCOPY", "TREE", "FC", "TITLE", "SET" };
			
			for (String s : batCom) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), "REM"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "rem"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			break;
			
		case ".js":
			String[] jsKeys = { "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
					"char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
					"enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function",
					"goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
					"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
					"super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
					"var", "void", "volatile", "while", "with", "yield" };
			
			for (String s : jsKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			break;
			
		case ".lua":
			String[] luaKeys = { "and", "break", "do", "else", "elseif", "end",
					"false", "for", "function", "if", "in", "local", "nil",
					"not", "or", "repeat", "return", "then", "true", "until", "while" };
			
			for (String s : luaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), "--"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			break;
			
		case ".sql":
			String[] sqlKeys = { "ADD", "ADD CONSTRAINT", "ALTER", "ALTER COLUMN", "ALTER TABLE",
					"ALL", "AND", "ANY", "AS", "ASC", "BACKUP DATABASE", "BETWEEN", "CASE", "CHECK",
					"COLUMN", "CONSTRAINT", "CREATE", "CREATE DATABASE", "CREATE INDEX", "CREATE OR REPLACE VIEW",
					"CREATE TABLE", "CREATE PROCEDURE", "CREATE UNIQUE INDEX", "CREATE VIEW", "DATABASE", "DEFAULT",
					"DELETE", "DESC", "DISTINCT", "DROP", "DROP COLUMN", "DROP CONSTRAINT", "DROP DATABASE",
					"DROP DEFAULT", "DROP INDEX", "DROP TABLE", "DROP VIEW", "EXEC", "EXISTS", "FOREIGN KEY",
					"FROM", "FULL OUTER JOIN", "GROUP BY", "HAVING", "IN", "INDEX", "INNER JOIN", "INSERT INTO",
					"INSERT INTO SELECT", "IS NULL", "IS NOT NULL", "JOIN", "LEFT JOIN", "LIKE", "LIMIT", "NOT",
					"NOT NULL", "OR", "ORDER BY", "OUTER JOIN", "PRIMARY KEY", "PROCEDURE", "RIGHT JOIN", "ROWNUM",
					"SELECT", "SELECT DISTINCT", "SELECT INTO", "SELECT TOP", "SET", "TABLE", "TOP", "TRUNCATE TABLE",
					"UNION", "UNION ALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE" };
			
			for (String s : sqlKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), "--"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			break;
			
		case ".asm":
			String[] asmKeys = { "add", "sub", "mov", "mul", "imul", "div", "idiv",
					"cmp", "jmp", "call", "jxx", "je", "jb", "jbe", "ja", "jae", "jz",
					"jne", "jnae", "jna", "jnbe", "jnb", "jnz", "jl", "jle", "jg", "jge",
					"jnl", "jng", "jnge", "dec", "inc", "loop", "loope", "loopz", "loopne", "loopnz" };
			
			for (String s : asmKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ";"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), fs);
			break;
		}
		
		return fs; // terminar
	}
	
	/**
	 * Conta quantos caracteres {@code c} tem na String {@code str}. 
	 * Adicional: conta desde o começo até não ter mais daquele char
	 * 
	 * @param str - A String que vai contar
	 * @param c - O caractere que vai ser contado
	 * @return O número de vezes que o caractere {@code c} aparece na String {@code str}.
	 */
	public int countChar(String str, char c) { // Fonte: StackOverflow, de novo :/
	    int count = 0;

	    for (int i = 0; i < str.length(); i++) {
	    	if (str.charAt(i) != c) break;
	    	
	    	if (str.charAt(i) == c)
	            count++;
	    }

	    return count;
	}
	
	public char[] toCharArray(List<Character> list) {
		StringBuilder sb = new StringBuilder();
		
        for (Character ch : list) {
            sb.append(ch);
        }
 
        String str = sb.toString();
        
        return str.toCharArray();
	}
	
	public List<Character> toCharList(char[] array) {
		List<Character> cl = new ArrayList<>();
		
		for (char c : array)
			cl.add(c);
		
		return cl;
	}
	
	public IDEFont[] toArray(List<IDEFont> list) {
		IDEFont[] a = new IDEFont[list.size()];
		
		for (int i = 0; i < list.size(); i++)
			a[i] = list.get(i);
		
		return a;
	}
	
	
	/**
	 * Esse método faz a função que muitos editores de código fazem: adicionar caracteres inteligentes. Se você digita '{', ele
	 * completa com '}', o mesmo vale para '[', '<', '"' (aspas duplas) e ' ' ' (aspas simples).
	 * 
	 * @param pre - O {@code StringBuilder} anterior, a base.
	 * @return O {@code StringBuilder} anterior com as modificações.
	 */
	private StringBuilder addCodeHints(StringBuilder pre) {
		switch (KeyInput.getCharPressed()) {
		case '{':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('}');
			else pre.insert(cursorX + 1, '}');
			break;
			
		case '(':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append(')');
			else pre.insert(cursorX + 1, ')');
			break;
			
		case '[':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append(']');
			else pre.insert(cursorX + 1, ']');
			break;
			
		case '<':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('>');
			else pre.insert(cursorX + 1, '>');
			break;
			
		case '"':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('"'); // arrumar uns bug ae
			else pre.insert(cursorX + 1, '"');
			break;
			
		case 39: // -> ( ' ) Aspas Simples
			if (pre.length() == 0 || cursorX == pre.length()) pre.append((char) 39);
			else pre.insert(cursorX + 1, (char) 39);
			break;
		}
		
		return pre;
	}
	
	private void setCursorWithinBounds() { // o cursorY deve ser feito primeiro
		if (editing == null) return;
		
		try {
			if (cursorY < 1) cursorY = 1;
			if (cursorY + 1 > lines.size()) cursorY = lines.size();
			
			if (cursorX < 0) cursorX = 0;
			if (cursorX > lines.get(cursorY - 1).getChars().size()) cursorX = lines.get(cursorY - 1).getChars().size();
		} catch (Exception e) {}
	}
	
	private int setWithinBounds(int x, int y, boolean isX) {
		if (isX) {
			if (y < 1) y = 1;
			if (y + 1 > lines.size()) y = lines.size();
			
			if (x < 0) x = 0;
			if (x > lines.get(y - 1).getChars().size()) x = lines.get(y - 1).getChars().size();
			
			return x;
		}
		else {
			if (y < 1) y = 1;
			if (y + 1 > lines.size()) y = lines.size();
			
			return y;
		}
	}
	
	private StringBuilder write(StringBuilder cY, char c) {
		if (c < 32 || c > 1000) {
			cursorX--; // esse é o método gambiarrento, mas depois pode arrumar (ou não kkkkk)
			
			return cY;
		}
		
		if (cY.length() == 0) cY.append(c);
		else if (cursorX <= cY.length()) cY.insert(cursorX, c); // use <= pq se digitar no último n digita pq n bate com a condição mas mesmo assim aumenta o cursorX e quando dá o backspace excede o tamanho da linha e dá no que dá né
		
		return cY;
	}
	
	private void register(StringBuilder cY) { // cY = cursorY
		String gs = cY.toString(); // gen string
		char[] ca = gs.toCharArray(); // char array
		
		List<Character> lc = toCharList(ca); // list char	(Esses comentários são para especificar os nomes das variáveis)
		
		lines.get(cursorY - 1).getChars().clear();
		lines.get(cursorY - 1).getFonts().clear();
		
		for (Character c : lc) {
			lines.get(cursorY - 1).getChars().add(c);
			lines.get(cursorY - 1).getFonts().add(DEFAULT_FONT);
		}
	}
	
	public char addAccents(int keyCode, char ch) {
		if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown()) { // ^ Circunflexo
			prAcc = PressedAccent.CIRCUMFLEX;
			pressedAccent = true;
			
			return ch;
		}
		else if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown()) { // ` Crase
			prAcc = PressedAccent.BACK_QUOTE;
			pressedAccent = true;
			
			return ch;
		}
		else if (keyCode == KeyEvent.VK_DEAD_ACUTE) { // ´ Acento Agudo
			prAcc = PressedAccent.ACUTE;
			pressedAccent = true;
			
			return ch;
		}
		else if (keyCode == KeyEvent.VK_DEAD_TILDE) { // ~ Til
			prAcc = PressedAccent.TILDE;
			pressedAccent = true;
			
			return ch;
		}
		
		if (pressedAccent && !(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL)) { // acabar os acentos e adicionar coloração automática
			pressedAccent = false;
			
			switch (prAcc) {
			case ACUTE:
				if (ch == 'A') return 'Á';
				 if (ch == 'E') return 'É';
				 if (ch == 'I') return 'Í';
				 if (ch == 'O') return 'Ó';
				 if (ch == 'U') return 'Ú';
				 if (ch == 'Y') return 'Ý';
				
				 if (ch == 'a') return 'á';
				 if (ch == 'e') return 'é';
				 if (ch == 'i') return 'í';
				 if (ch == 'o') return 'ó';
				 if (ch == 'u') return 'ú';
				 if (ch == 'y') return 'ý';
				break;
			case BACK_QUOTE:
				if (ch == 'A') return 'À';
				 if (ch == 'E') return 'È';
				 if (ch == 'I') return 'Ì';
				 if (ch == 'O') return 'Ò';
				 if (ch == 'U') return 'Ù';
				
				 if (ch == 'a') return 'à';
				 if (ch == 'e') return 'è';
				 if (ch == 'i') return 'ì';
				 if (ch == 'o') return 'ò';
				 if (ch == 'u') return 'ù';
				break;
			case CIRCUMFLEX:
				if (ch == 'A') return 'Â';
				 if (ch == 'E') return 'Ê';
				 if (ch == 'I') return 'Î';
				 if (ch == 'O') return 'Ô';
				 if (ch == 'U') return 'Û';
				
				 if (ch == 'a') return 'â';
				 if (ch == 'e') return 'ê';
				 if (ch == 'i') return 'î';
				 if (ch == 'o') return 'ô';
				 if (ch == 'u') return 'û';
				break;
			case TILDE:
				if (ch == 'a') return 'ã';
				else if (ch == 'A') return 'Ã';
				
				if (ch == 'O') return 'Õ';
				else if (ch == 'N') return 'Ñ';
				
				if (ch == 'o') return 'õ';
				else if (ch == 'n') return 'ñ';
				break;
			}
		}
		
		return ch;
	}
	
	private void copy() {
		if (editing == null) return;
		
		StringSelection sl = new StringSelection(new String(toCharArray(lines.get(cursorY - 1).getChars())));
		
		Clipboard cl = Main.toolkit.getSystemClipboard();
		cl.setContents(sl, null);
	}
	
	private void paste() {
		if (editing == null) return;
		
		String[] sp = clipboard.split("\n");
		StringBuilder[] bs = new StringBuilder[sp.length];
		
		for (int i = 0; i < sp.length; i++) {
			int x = cursorX == 0 ? cursorX : 0;
			
			bs[i] = new StringBuilder(sp[i]);
			bs[i].insert(x, sp[i]);
		}
			
		editing.setSaved(false);
		
		for (int i = 0; i < sp.length; i++)
			register(bs[i]);
	}
	
	public static void execTerminal() {
		CommandTerminal term = new CommandTerminal(Screen.WIDTH / 2 - 250, 25, 500, 30);
		
		if (CommandTerminal.active)
			return;
		
		CommandTerminal.active = true;
		IDEComponent.toAdd.add(term);
	}
	
	private void execute(String arg) {
		switch (arg) {
		case "cmd":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start");
				
				File dir = Explorer.scope != null ? Explorer.scope.getRegent() : Main.baseFolder; // eu tava fazendo o equivalente a isso: null.regent != null
				
				pb.directory(dir);
				
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "copy":
			copy();
			break;
			
		case "paste":
			paste();
			break;
			
		case "save":
			if (editing == null) return;
			
			editing.save();
			break;
			
		case "clr":
			if (editing == null) return;
			
			lines.get(cursorY - 1).getChars().clear();
			lines.get(cursorY - 1).getFonts().clear();
			
			editing.setSaved(false);
			
			setCursorWithinBounds();
			break;
			
		case "sysexp":
			try {
				Runtime.getRuntime().exec("explorer.exe /select," + editing.getRegent().getRegent().getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "term":
			execTerminal();
			break;
		}
	}
	
	public void tick() {
		super.tick();
		
		showCursorData = false;
		
		if (KeyInput.isAltDown() && editing != null && hovered()) { // TODO mudar texturas dos acentos
			KeyInput.updateKeys();
			
			showCursorData = true;
		}
		
		if (KeyInput.isKeyPressed() && hovered() && editing != null) {
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_Z && KeyInput.isControlDown()) // Ctrl + Z
				selectMode = true;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE)
				selectMode = false;
		}
		
		if (selectMode && leftClicked()) {
			selecting = true;
			
			MouseInput.updateMouse();
			
			int mx = 0;
			int my = 0;
			
			my = (MouseInput.getMouseY() / (FONT_SIZE + 4) - 1) + (scrY / (FONT_SIZE + 4)); // resolver seta do terminal de comando
			mx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
			
			double offset = mx * 0.7;
			offset = Math.ceil(offset);
			offset = mx - offset;
			
			mx += (int) offset;
			mx++;
			
			mx = setWithinBounds(mx, my, true);
			my = setWithinBounds(mx, my, false);
			
			/*if (index2 < index1) {
				int temp = index2;
				
				index2 = index1;
				index1 = temp;
			}
			
			if (line2 < line1) {
				int temp = line2;
				
				line2 = line1;
				line1 = temp;
			}*/
			
			if (isSelectingFirst) {
				line1 = my;
				index1 = mx;
				
				isSelectingFirst = false;
			}
			else {
				line2 = my;
				index2 = mx;
				
				selectMode = false;
				isSelectingFirst = true;
			}
		}
		
		try {
			clipboard = (String) Main.toolkit.getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException | IllegalStateException e) {
			System.err.println("Não é string. Resetando!");
			clipboard = "";
		}
		
		if (MouseInput.hovered(x, 0, width, Tab.HEIGHT)) {
			if (MouseInput.isMouseRolling()) {
				if (MouseInput.wheelUp() && tabScr < 0)
					tabScr += 200;
				else if (MouseInput.wheelDown()) {
					tabScr -= 200;
				}
			}
		}
		
		if (hovered() && editing != null) {
			Main.screen.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			
			if (MouseInput.isMouseRolling()) {
				new Thread() {
					public void run() {
					if (KeyInput.isShiftDown()) {
						if (MouseInput.wheelUp() && scrX > 0)
							scrX -= FONT_SIZE * 3;
						else if (MouseInput.wheelDown())
							scrX += FONT_SIZE * 3;
					}
					else {
						if (MouseInput.wheelUp() && scrY > 0)
							scrY -= (FONT_SIZE + 4) * 3;
						else if (MouseInput.wheelDown())
							scrY += (FONT_SIZE + 4) * 3;
					}
					
					return;
					}
				}.start();
			}
			
			if (leftClicked() && !RightClickOption.isRightClickActive() && !selectMode) {
				cursorY = (MouseInput.getMouseY() / (FONT_SIZE + 4) - 1) + (scrY / (FONT_SIZE + 4)); // resolver seta do terminal de comando
				cursorX = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
				
				double offset = cursorX * 0.7;
				offset = Math.ceil(offset);
				offset = cursorX - offset;
				
				cursorX += (int) offset;
				cursorX += cursorX < 2 ? 0 : 2;
				
				setCursorWithinBounds();
			}
		}
		else
			Main.screen.setCursor(Cursor.getDefaultCursor());
		
		if (rightClicked()) {
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), 430, "Abrir Prompt de Comando", (s) -> execute(s), "cmd");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, 430, "Abrir Terminal de Comando", (s) -> execute(s), "term");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, 430, "Abrir Explorador de Arquivos", (s) -> execute(s), "sysexp");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, 430, "Copiar linha", (s) -> execute(s), "copy");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, 430, "Colar", (s) -> execute(s), "paste");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 150, 430, "Salvar", (s) -> execute(s), "save");
		}
		
		if (KeyInput.isKeyPressed() && !SetFileName.added && !CommandTerminal.active && !selectMode) {
			
			// Detectar atalhos
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + T (Terminal)
				KeyInput.updateKeys();
					
				execute("term");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_WINDOWS) { // Ctrl + Win (Cmd)
				KeyInput.updateKeys();
					
				execute("cmd");
					
				return;
			}
			
			if (editing == null) return;
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_S) { // Ctrl + S (Salvar)
				KeyInput.updateKeys();
					
				editing.save();
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C (Copiar)
				KeyInput.updateKeys();
					
				copy();
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V (Colar)
				KeyInput.updateKeys();
					
				paste();
					
				return;
			}
			
			if (!(KeyInput.isAltDown() || KeyInput.isAltGrDown() || KeyInput.isControlDown())) { // se ctrl, alt e alt gr NÃO estão pressionados
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
				KeyInput.updateKeys();
				
				cursorY--;
				
				setCursorWithinBounds();
				
				return;
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
				KeyInput.updateKeys();
				
				cursorY++;
				
				setCursorWithinBounds();
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
				KeyInput.updateKeys();
				
				cursorX--;
				
				setCursorWithinBounds();
				
				return;
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
				KeyInput.updateKeys();
				
				cursorX++;
				
				setCursorWithinBounds();
				
				return;
			}
			
			KeyInput.updateKeys();
			
			StringBuilder cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
				KeyInput.updateKeys();
				
				if (cursorX > 0) {
					cY.deleteCharAt(cursorX - 1);
				
					cursorX--;
				
					setCursorWithinBounds();

					editing.setSaved(false);
				
					register(cY);
				}
				else if (cursorY > 1) {
					String s = cY.toString();
					
					cursorX = lines.get(cursorY - 2).getChars().size();
					
					lines.remove(cursorY - 1);
					cursorY--;
					
					cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
					
					cY.append(s);

					editing.setSaved(false);
					
					register(cY);
				}
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				KeyInput.updateKeys();
				
				if (cursorX < cY.length()) {
					cY.deleteCharAt(cursorX);
				
					setCursorWithinBounds();

					editing.setSaved(false);
				
					register(cY);
				}
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER || KeyInput.getCharPressed() == (char) 9) {
				KeyInput.updateKeys();
				
				StringBuilder spaces = new StringBuilder();
				String s = cY.substring(cursorX);
				
				for (int i = 0; i < countChar(cY.toString(), ' '); i++)
					spaces.append(' ');
				
				for (int i = 0; i < countChar(cY.toString(), (char) 9); i++) // char 9 é o tab
					spaces.append(' ');
				
				int nSpaces = spaces.length();
				
				spaces.append(s);
				s = spaces.toString();
				
				cY.delete(cursorX, cY.length());
				
				List<IDEFont> fs = new ArrayList<>();
				
				for (int i = 0; i < s.length(); i++)
					fs.add(DEFAULT_FONT);
				
				lines.add(cursorY, new IDELine(toCharList(s.toCharArray()), fs));
				
				register(cY);
				
				editing.setSaved(false);
				
				cursorX = nSpaces;
				cursorY++;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SHIFT || KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB) return;
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = addAccents(keyCode, c);
			
			cY = write(cY, c);
			cY = addCodeHints(cY);
			
			register(cY);
			
			cursorX++;
			
			setCursorWithinBounds();
			
			if (KeyInput.getCharPressed() < 33 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
		
			editing.setSaved(false);
		}
		}

		new Thread() {
			public void run() {
				for (IDELine l : lines) {
					l.setFonts(
							automaticColor(
									toCharArray(
											l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
				
				}
			}
		}.start();
		
		for (Tab t : tabs)
			t.tick();
		
		tabs.addAll(toAdd);
		toAdd.clear();
		
		tabs.removeAll(toRemove);
		toRemove.clear();
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorerLight);
		g2.setStroke(new BasicStroke(8f));
		
		g2.drawLine(x, y, x, height);
		g2.drawLine(x, 30, width, 30);
		
		g.setColor(Colors.background);
		g.fillRect(x, y, width, height);
		
		if (tabs.size() == 0) return;
		
		for (Tab t : tabs)
			t.render(g);
		
		if (editing != null) {
			g.setColor(Colors.explorer);
			g.fillRect(x, MIN_Y, width, height);
		}
		
		for (int i = 0; i < lines.size(); i++) {
			char[] cs = toCharArray(lines.get(i).getChars());
			IDEFont[] fs = toArray(lines.get(i).getFonts());
			
			if (lines.get(i) == null) break;
			
			if (MIN_Y + (i * (FONT_SIZE + 4)) - scrY < MIN_Y) continue;
			
			Fonts.drawString(String.valueOf(i + 1), x, MIN_Y + (i * (FONT_SIZE + 4)) - scrY, new IDEFont(Fonts.lightGrayNormal, FONT_SIZE), g);
			Fonts.drawChars(cs, (x + 40) - scrX, MIN_Y + (i * (FONT_SIZE + 4)) - scrY, fs, x + (FONT_SIZE * 2), g);
		}
		
		if (cursorY * (FONT_SIZE + 4) - FONT_SIZE - scrY < MIN_Y - 40 || ((x + 40) + cursorX * (FONT_SIZE - 4)) - scrX < x + (FONT_SIZE * 2)) return;
		
		if (showCursor) {
			g.setColor(Color.white);
			g.fillRect(((x + 40) + cursorX * (FONT_SIZE - 4)) - scrX, MIN_Y + cursorY * (FONT_SIZE + 4) - FONT_SIZE - scrY - 2, 2, FONT_SIZE); // * 14
		}
		
		if (showCursorData) {
			KeyInput.updateKeys();
			
			g.setColor(new Color(0, 0, 0, 0.3f));
			g.fillRect(0, 0, Main.screen.getWidth(), Main.screen.getHeight());
			
			Fonts.drawString("Cursor X: " + (cursorX + 1), MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
			Fonts.drawString("Cursor Y: " + cursorY, MouseInput.getMouseX() + 10, MouseInput.getMouseY() + FONT_SIZE + 3, new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
		}
		
		int mx = 0;
		int my = 0;
		
		if (selecting) {
			g.setColor(Color.red);
			g.fillRect(((x + 40) + index1 * (FONT_SIZE - 4)) - scrX, MIN_Y + line1 * (FONT_SIZE + 4) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
			
			g.setColor(Color.red);
			g.fillRect(((x + 40) + index2 * (FONT_SIZE - 4)) - scrX, MIN_Y + line2 * (FONT_SIZE + 4) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
		}
		
		my = (MouseInput.getMouseY() / (FONT_SIZE + 4) - 1) + (scrY / (FONT_SIZE + 4));
		mx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
		
		double offset = mx * 0.7;
		offset = Math.ceil(offset);
		offset = mx - offset;
		
		mx += (int) offset;
		mx++;
		
		mx = setWithinBounds(mx, my, true);
		my = setWithinBounds(mx, my, false);
		
		if (selectMode) {
			g.drawImage(gradient, x, 0, width, 130, null);
			
			g.setColor(Color.blue);
			g.fillRect(((x + 40) + mx * (FONT_SIZE - 4)) - scrX, MIN_Y + my * (FONT_SIZE + 4) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
			
			Fonts.drawString("[Esc] Cancelar", MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
			Fonts.drawString("[Click Direito] Selecionar", MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 55, new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
			
			if (isSelectingFirst)
				Fonts.drawString("Selecione a primeira posição", MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
		
			else
				Fonts.drawString("Selecione a segunda posição", MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, FONT_SIZE), g);
		}
	}
}
