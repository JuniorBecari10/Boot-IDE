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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RenameFile;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.ComponentInput;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.searchreplace.SearchReplaceCore;
import ide.util.Animation;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Serialization;
import ide.util.Texts;

// Nota: para escrever em vermelho no console, ao inv�s de digitar System.out.println("texto"); use System.err.println("texto");

public class CodeEditor extends IDEComponent {
	
	public static final int TAB_ANIMATION_TIMEOUT = 300;

	public static int FONT_SIZE = 16; // 18, 16 (Padr�o: 16)
	public static int LINE_HEIGHT = FONT_SIZE + (FONT_SIZE / 3);

	final int originalEditorX = 280;

	public Tab editing;

	public boolean isMultilineCommenting = false;

	public boolean selecting;

	public int line1, line2;
	public int index1, index2; // TODO fazer a verifica��o do CSS se est� dentro do seletor, e se tiver, colore
								// números
	
	public static final int MAX_UNDOS = 10;
	
	// valor padrão de LF
	public static LineEnding lineEnding = LineEnding.LF;
	
	public boolean shouldColor = true;

	public boolean isCssPart;
	public boolean isJSPart;
	public boolean isPhpPart;
	
	public static boolean minMode;

	private boolean keyTimeout;

	public int keyWait = 0, maxKeyWait = 5;

	public static boolean automaticallyOpenTabs = true;
	public static boolean showWhitespace = false;

	public boolean codeHelpersOn = true;

	public String codeType = "";
	public String extType = "";
	
	public static boolean indentSpaces = true;
	public static int indentLength = 4; // 4 caracteres (padrão) "_ _ _ _"

	public boolean isAnotherIteration = false;
	public boolean foundExt = false;

	public int cursorX = 0;
	public int cursorY = 1;

	public int scrX = 0;
	public int scrY = 0;

	public Direction directionStarted = Direction.NONE;

	private int realcx, realcy; // c = cursor
	public int drawcx = ((x + (FONT_SIZE * 4)) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX,
			drawcy = MIN_Y + cursorY * (LINE_HEIGHT) - FONT_SIZE - scrY - 2;

	private PressedAccent prAcc;
	private boolean pressedAccent = false;

	public List<IDELine> lines = new ArrayList<>();
	public List<IDELine> linesToRemove = new ArrayList<>();

	public static boolean isAutoCompleteActive = true;

	public Stack<String> undo = new Stack<>();
	public Stack<String> redo = new Stack<>();

	public int tabScr = 0;

	public List<Tab> tabs;
	public List<Tab> toAdd;
	public List<Tab> toRemove;
	
	public boolean alreadyAddedFrame = false;

	// public static BufferedImage gradient;

	public static String clipboard = "";

	public static final int MIN_Y = Screen.DECORATION_HEIGHT + 35;

	public boolean showCursor;

	public Thread cursorThread;
	public Animation cursor;

	// public static boolean putChevronsOnTags = true;

	public int mx;
	public int my;

	public boolean isReadOnly = false;

	public int autocompletescroll = 0;

	// public List<String> autocomplete = new ArrayList<>();
	public String wordSinceSpace = "";
	public int autocompleteindex = 0;

	public List<AutoComplete> autocomplete = new ArrayList<>();
	public List<AutoComplete> addautocomplete = new ArrayList<>();
	public List<AutoComplete> removeautocomplete = new ArrayList<>();

	public List<RightClickOption> autocompletes = new ArrayList<>();
	public List<RightClickOption> toAddAutoCompletes = new ArrayList<>();
	public List<RightClickOption> toRemoveAutoCompletes = new ArrayList<>();

	///

	public static BufferedImage functions = Main.spritesheet.getSprite(176, 0, 8, 8);
	public static BufferedImage objects = Main.spritesheet.getSprite(184, 0, 8, 8);
	public static BufferedImage keywords = Main.spritesheet.getSprite(192, 0, 8, 8);
	public static BufferedImage variables = Main.spritesheet.getSprite(200, 0, 8, 8);

	///

	///////

	public static final String[] syms = { "(", ")", "[", "]", "{", "}", ",", ".", "<", ">", ";", ":", "?", "/", "\\",
			"|", "+", "-", "*", "=", "&", "%", "$", "#", "!", "@", "`", "�", "^", "~" };

	public static final String[] loremWords = { "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
			"curabitur", "vel", "hendrerit", "libero", "eleifend", "blandit", "nunc", "ornare", "odio", "ut", "orci",
			"gravida", "imperdiet", "nullam", "purus", "lacinia", "a", "pretium", "quis", "congue", "praesent",
			"sagittis", "laoreet", "auctor", "mauris", "non", "velit", "eros", "dictum", "proin", "accumsan", "sapien",
			"nec", "massa", "volutpat", "venenatis", "sed", "eu", "molestie", "lacus", "quisque", "porttitor", "ligula",
			"dui", "mollis", "tempus", "at", "magna", "vestibulum", "turpis", "ac", "diam", "tincidunt", "id",
			"condimentum", "enim", "sodales", "in", "hac", "habitasse", "platea", "dictumst", "aenean", "neque",
			"fusce", "augue", "leo", "eget", "semper", "mattis", "tortor", "scelerisque", "nulla", "interdum", "tellus",
			"malesuada", "rhoncus", "porta", "sem", "aliquet", "et", "nam", "suspendisse", "potenti", "vivamus",
			"luctus", "fringilla", "erat", "donec", "justo", "vehicula", "ultricies", "varius", "ante", "primis",
			"faucibus", "ultrices", "posuere", "cubilia", "curae", "etiam", "cursus", "aliquam", "quam", "dapibus",
			"nisl", "feugiat", "egestas", "class", "aptent", "taciti", "sociosqu", "ad", "litora", "torquent", "per",
			"conubia", "nostra", "inceptos", "himenaeos", "phasellus", "nibh", "pulvinar", "vitae", "urna", "iaculis",
			"lobortis", "nisi", "viverra", "arcu", "morbi", "pellentesque", "metus", "commodo", "ut", "facilisis",
			"felis", "tristique", "ullamcorper", "placerat", "aenean", "convallis", "sollicitudin", "integer", "rutrum",
			"duis", "est", "etiam", "bibendum", "donec", "pharetra", "vulputate", "maecenas", "mi", "fermentum",
			"consequat", "suscipit", "aliquam", "habitant", "senectus", "netus", "fames", "quisque", "euismod",
			"curabitur", "lectus", "elementum", "tempor", "risus", "cras" };

	public static final String[] javaKeys = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
			"class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
			"new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
			"true", "false", "null", "yield", "sealed", "var" };

	public static final String[] tags = { "<!--", "<!doctype", "<?php", "<!DOCTYPE", "<a", "<abbr", "<acronym",
			"<address", "<applet", "<area", "<article", "<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo",
			"<big", "<blockquote", "<body", "<br", "<button", "<canvas", "<caption", "<center", "<cite", "<code",
			"<col", "<colgroup", "<data", "<datalist", "<dd", "<del", "<details", "<dfn", "<dialog", "<dir", "<div",
			"<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure", "<font", "<footer", "<form", "<frame",
			"<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header", "<hr", "<html", "<i", "<iframe",
			"<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main", "<map", "<mark", "<meta",
			"<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option", "<output", "<p",
			"<param", "<picture", "<pre", "<progress", "<py-script", "<py-config", "<py-env", "<py-repl", "<q", "<rp", "<rt", 
			"<ruby", "<s", "<samp", "<script",
			"<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg",
			"<table", "<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr",
			"<track", "<tt", "<u", "<ul", "<var", "<video", "<wbr", "<applet", "<webview", "</a", "</abbr", "</acronym",
			"</address", "</applet", "</area", "</article", "</aside", "</audio", "</b", "</base", "</basefont",
			"</bdi", "</bdo", "</big", "</blockquote", "</body", "</br", "</button", "</canvas", "</caption",
			"</center", "</cite", "</code", "</col", "</colgroup", "</data", "</datalist", "</dd", "</del", "</details",
			"</dfn", "</dialog", "</dir", "</div", "</dl", "</dt", "</em", "</embed", "</fieldset", "</figcaption",
			"</figure", "</font", "</footer", "</form", "</frame", "</frameset", "</h1", "</h2", "</h3", "</h4", "</h5",
			"</h6", "</head", "</header", "</hr", "</html", "</i", "</iframe", "</img", "</input", "</ins", "</kbd",
			"</label", "</legend", "</li", "</link", "</main", "</map", "</mark", "</meta", "</meter", "</nav",
			"</noframes", "</noscript", "</object", "</ol", "</optgroup", "</option", "</output", "</p", "</param",
			"</picture", "</pre", "</progress", "</py-script", "</py-config", "</py-env", "</py-repl", "</q", "</rp",
			"</rt", "</ruby", "</s", "</samp", "</script",
			"</section", "</select", "</small", "</source", "</span", "</strike", "</strong", "</style", "</sup",
			"</svg", "</table", "</tbody", "</td", "</template", "</textarea", "</tfoot", "</th", "</thead", "</time",
			"</title", "</tr", "</track", "</tt", "</u", "</ul", "</var", "</video", "</wbr", "</applet", "</webview",
			
			"<!-->", "<!doctype>", "<?php>", "<!DOCTYPE>", "<a>", "<abbr>", "<acronym>",
			"<address>", "<applet>", "<area>", "<article>", "<aside>", "<audio>", "<b>", "<base>", "<basefont>", "<bdi>", "<bdo>",
			"<big>", "<blockquote>", "<body>", "<br>", "<button>", "<canvas>", "<caption>", "<center>", "<cite>", "<code>",
			"<col>", "<colgroup>", "<data>", "<datalist>", "<dd>", "<del>", "<details>", "<dfn>", "<dialog>", "<dir>", "<div>",
			"<dl>", "<dt>", "<em>", "<embed>", "<fieldset>", "<figcaption>", "<figure>", "<font>", "<footer>", "<form>", "<frame>",
			"<frameset>", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>", "<head>", "<header>", "<hr>", "<html>", "<i>", "<iframe>",
			"<img>", "<input>", "<ins>", "<kbd>", "<label>", "<legend>", "<li>", "<link>", "<main>", "<map>", "<mark>", "<meta>",
			"<meter>", "<nav>", "<noframes>", "<noscript>", "<object>", "<ol>", "<optgroup>", "<option>", "<output>", "<p>",
			"<param>", "<picture>", "<pre>", "<progress>", "<py-script>", "<py-config>", "<py-env>", "<py-repl>", "<q>", "<rp>", "<rt>",
			"<ruby>", "<s>", "<samp>", "<script>",
			"<section>", "<select>", "<small>", "<source>", "<span>", "<strike>", "<strong>", "<style>", "<sup>", "<svg>",
			"<table>", "<tbody>", "<td>", "<template>", "<textarea>", "<tfoot>", "<th>", "<thead>", "<time>", "<title>", "<tr>",
			"<track>", "<tt>", "<u>", "<ul>", "<var>", "<video>", "<wbr>", "<applet>", "<webview>", "</a>", "</abbr>", "</acronym>",
			"</address>", "</applet>", "</area>", "</article>", "</aside>", "</audio>", "</b>", "</base>", "</basefont>",
			"</bdi>", "</bdo>", "</big>", "</blockquote>", "</body>", "</br>", "</button>", "</canvas>", "</caption>",
			"</center>", "</cite>", "</code>", "</col>", "</colgroup>", "</data>", "</datalist>", "</dd>", "</del>", "</details>",
			"</dfn>", "</dialog>", "</dir>", "</div>", "</dl>", "</dt>", "</em>", "</embed>", "</fieldset>", "</figcaption>",
			"</figure>", "</font>", "</footer>", "</form>", "</frame>", "</frameset>", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>",
			"</h6>", "</head>", "</header>", "</hr>", "</html>", "</i>", "</iframe>", "</img>", "</input>", "</ins>", "</kbd>",
			"</label>", "</legend>", "</li>", "</link>", "</main>", "</map>", "</mark>", "</meta>", "</meter>", "</nav>",
			"</noframes>", "</noscript>", "</object>", "</ol>", "</optgroup>", "</option>", "</output>", "</p>", "</param>",
			"</picture>", "</pre>", "</progress>", "</q>", "</rp>", "</rt>", "</ruby>", "</s>", "</samp>", "</script>",
			"</section>", "</select>", "</py-script>", "</py-config>", "</py-env>", "</py-repl>", "</small>", "</source>", "</span>",
			"</strike>", "</strong>", "</style>", "</sup>",
			"</svg>", "</table>", "</tbody>", "</td>", "</template>", "</textarea>", "</tfoot>", "</th>", "</thead>", "</time>",
			"</title>", "</tr>", "</track>", "</tt>", "</u>", "</ul>", "</var>", "</video>", "</wbr>", "</applet>", "</webview"};

	public static final String[] nums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"/*, "1a", "2a", "3a", "4a",
			"5a", "6a", "7a", "8a", "9a", "0a", // hex
			"1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b", "0b", "1c", "2c", "3c", "4c", "5c", "6c", "7c", "8c",
			"9c", "0c", "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "0d", "1e", "2e", "3e", "4e", "5e", "6e",
			"7e", "8e", "9e", "0e", "1f", "2f", "3f", "4f", "5f", "6f", "7f", "8f", "9f", "0f", "1l", "2l", "3l", "4l",
			"5l", "6l", "7l", "8l", "9l", "0l", "1A", "2A", "3A", "4A", "5A", "6A", "7A", "8A", "9A", "0A", // HEX
			"1B", "2B", "3B", "4B", "5B", "6B", "7B", "8B", "9B", "0B", "1C", "2C", "3C", "4C", "5C", "6C", "7C", "8C",
			"9C", "0C", "1D", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "0D", "1E", "2E", "3E", "4E", "5E", "6E",
			"7E", "8E", "9E", "0E", "1F", "2F", "3F", "4F", "5F", "6F", "7F", "8F", "9F", "0F", "1L", "2L", "3L", "4L",
			"5L", "6L", "7L", "8L", "9L", "0L", "1x", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "0x", "1X", "2X",
			"3X", "4X", "5X", "6X", "7X", "8X", "9X", "0X", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "0h",
			"1H", "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "0H"*/ };

	public static final String[] phpKeys = { "abstract", "and", "as", "break", "callable", "case", "catch", "class",
			"clone", "const", "continue", "declare", "default", "do", "echo", "else", "elseif", "enddeclare", "endfor",
			"endforeach", "endif", "endswitch", "endwhile", "extends", "final", "finally", "fn", "for", "foreach",
			"function", "each", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof",
			"interface", "match", "namespace", "new", "or", "print", "private", "protected", "public", "require",
			"require_once", "return", "static", "switch", "throw", "trait", "try", "use", "var", "while", "yield",
			"yield from", "__CLASS__", "__DIR__", "__FILE__", "__FUNCTION__", "__LINE__", "__METHOD__", "__NAMESPACE__",
			"__TRAIT__" };

	public static final String[] jsKeys = { "abstract", "arguments", "await", "boolean", "break", "byte", "case",
			"catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
			"enum", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if",
			"implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null",
			"package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized",
			"this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with",
			"yield", "undefined", "of", "async", "window", "document", "console", "as", "from", "navigator",
			"constructor", "debug", "declare", "namespace", "number", "string", "boolean", "array", "object", "any",
			"void", "mutation", "set" };

	public static final String[] cssTags = { "a", "abbr", "acronym", "address", "applet", "area", "article", "aside",
			"audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button", "canvas",
			"caption", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del", "details", "dfn", "dialog",
			"dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure", "font", "footer", "form",
			"frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hr", "html", "i", "iframe",
			"img", "input", "ins", "kbd", "label", "legend", "li", "link", "main", "map", "mark", "meta", "meter",
			"nav", "noframes", "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "picture",
			"pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select", "small", "source",
			"span", "strike", "strong", "style", "sup", "svg", "table", "tbody", "td", "template", "textarea", "tfoot",
			"th", "thead", "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "applet", "moz",
			"webkit", "ms", "mixin", "extend", "webview", "user", "select", "drag", "src" /* TODO colocar mais desses ultimos */ // TODO
																														// talvez
																														// se
																														// der
																														// erro,
																														// colorir
																														// center
																														// de
																														// novo
	};

	public static final String[] cssAdds = { "important", "screen", "and", "or", "from", "to", "rotate", };

	public static final String[] props = { "align-content", "align-items", "all", "animation", "animation-direction",
			"animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name",
			"animation-play-state", "animation-timing-function", "aspect-ratio", "backface-visibility", "background",
			"background-attachment", "background-blend-mode", "background-clip", "background-color", "background-image",
			"background-origin", "background-position", "background-repeat", "background-size", "border",
			"border-bottom", "border-bottom-color", "border-bottom-left-radius", "border-bottom-right-radius",
			"border-bottom-style", "border-bottom-width", "border-collapse", "border-color", "border-image",
			"border-image-outset", "border-image-repeat", "border-image-slice", "border-image-source",
			"border-image-width", "border-radius", "border-right", "border-right-color", "border-right-style",
			"border-right-width", "border-spacing", "border-style", "border-top", "border-top-color",
			"border-top-left-radius", "border-top-right-radius", "border-top-style", "border-top-width", "border-width",
			"bottom", "box-decoration-break", "box-shadow", "box-sizing", "break-after", "break-before", "break-inside",
			"caption-side", "caret-color", "@charset", "clear", "clip", "color", "column-count", "column-fill",
			"column-gap", "column-rule", "column-rule-color", "column-rule-style", "column-rule-width", "column-span",
			"column-width", "columns", "content", "counter-increment", "counter-reset", "cursor", "direction",
			"display", "empty-cells", "filter", "flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow",
			"flex-shrink", "flex-wrap", "float", "font", "@font-face", "font-family", "font-feature-settings",
			"font-feature-values", "font-kerning", "font-language-override", "font-size", "font-size-adjust",
			"font-stretch", "font-style", "font-synthesis", "font-variant", "font-variant-alternates",
			"font-variant-caps", "font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric",
			"font-variant-position", "font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow",
			"grid-auto-rows", "grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template",
			"grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation", "height",
			"hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes", "left",
			"letter-spacing", "line-break", "line-height", "list-style", "list-style-image", "list-style-position",
			"list-style-type", "margin", "margin-bottom", "margin-left", "margin-right", "margin-top", "mask",
			"mask-type", "max-height", "max-width", "@media", "min-height", "min-width", "mix-blend-mode", "object-fit",
			"object-position", "opacity", "order", "orphans", "outline", "outline-color", "outline-offset",
			"outline-style", "outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding",
			"padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after", "page-break-before",
			"page-break-inside", "perspective", "perspective-origin", "pointer-events", "position", "quotes", "resize",
			"right", "row-gap", "scroll-behavior", "tab-size", "table-layout", "text-align", "text-align-last",
			"text-combine-upright", "text-decoration", "text-decoration-color", "text-decoration-line",
			"text-decoration-style", "text-indent", "text-justify", "text-orientation", "text-overflow", "text-shadow",
			"text-transform", "text-underline-position", "top", "transform", "transform-origin", "transform-style",
			"transition", "transition-delay", "transition-duration", "transition-property",
			"transition-timing-function", "unicode-bidi", "user-select", "vertical-align", "visibility", "white-space",
			"widows", "width", "word-break", "word-spacing", "word-wrap", "writing-mode", "z-index" };

	public static final String[] units = { "px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh",
			"vmin", "vmax", "s", "deg", "rad" };

	public static final String[] pyKeys = { "and", "as", "assert", "break", "class", "continue", "def", "del", "elif",
			"else", "except", "False", "finally", "for", "from", "global", "if", "import", "in", "is", "match",
			"lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return", "super", "any", "True", "try",
			"while", "with", "yield", "self", "async", "await", "of", "str", "int", "float", "complex", "list", "tuple",
			"dict", "set", "frozenset", "bool", "bytes", "bytearray", "memoryview" };

	public static final String[] dartKeys = { "abstract", "else", "import", "super", "as", "enum", "in", "switch",
			"assert", "export", "interface", "sync", "async", "extends", "is", "this", "await", "extension", "library",
			"throw", "break", "external", "mixin", "true", "case", "factory", "new", "try", "class", "final", "catch",
			"false", "null", "typedef", "on", "var", "const", "finally", "operator", "void", "continue", "for", "part",
			"while", "covariant", "Function", "rethrow", "with", "default", "get", "return", "yield", "deferred",
			"hide", "set", "do", "if", "show", "dynamic", "implements", "static" };

	public static final String[] ldKeys = { "ENTRY", "OUTPUT_FORMAT", "STARTUP", "SEARCH_DIR", "INPUT", "OUTPUT",
			"MEMORY", "SECTIONS", "KEEP" };

	public static final String[] pasKeys = { "and", "begin", "boolean", "break", "byte", "continue", "div", "do",
			"double", "else", "end", "false", "if", "integer", "longint", "mod", "not", "or", "repeat", "shl",
			"shortint", "shr", "single", "then", "true", "until", "while", "word", "xor", "function" };

	public static final String[] cKeys = { "auto", "break", "case", "char", "const", "continue", "default", "do",
			"double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return",
			"short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void",
			"volatile", "while", "true", "false", "null", "include", "#include", "restrict", "bool", "duint", "uint8_t", "uint16_t",
			"size_t", "NULL" };

	public static final String[] cppKeys = { "auto", "break", "case", "char", "const", "continue", "default", "do",
			"double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return",
			"short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void",
			"volatile", "while", "asm", "dynamic_cast", "namespace", "reinterpret_cast", "bool", "explicit", "new",
			"static_cast", "false", "catch", "operator", "template", "friend", "private", "class", "this", "inline",
			"public", "throw", "const_cast", "delete", "mutable", "protected", "true", "try", "typeid", "typename",
			"using", "virtual", "wchar_t", "#include", "include", "#define", "string", "#ifdef", "#ifndef", "#error", "#pragma", "#endif",
			"override", "std", "size_t", "duint", "uint8_t", "uint16_t", "comment", "lib", "NULL", "alignof", "nullptr" };

	public static final String[] csKeys = { "abstract", "async", "const", "event", "extern", "new", "override",
			"partial", "readonly", "sealed", "static", "unsafe", "virtual", "volatile", "public", "private", "internal",
			"protected", "if", "else", "switch", "case", "do", "for", "foreach", "in", "while", "break", "continue",
			"default", "goto", "return", "yield", "throw", "try", "catch", "finally", "checked", "unchecked", "fixed",
			"lock", "params", "ref", "out", "using", "alias", "await", "sizeof", "typeof", "stackalloc", "is", "base",
			"this", "null", "false", "true", "value", "void", "bool", "byte", "interface", "char", "class", "decimal",
			"double", "enum", "float", "int", "long", "sbyte", "short", "string", "super", "struct", "uint", "ulong",
			"ushort", "add", "var", "dynamic", "global", "set", "namespace", "object", "as", "get", "operator", "nameof" };

	public static final String[] rKeys = { "if", "else", "repeat", "while", "function", "for", "in", "next", "break",
			"TRUE", "FALSE", "NULL", "Inf", "NaN", "NA", "NA_integer", "NA_real", "NA_complex", "NA_character" };

	public static final String[] batCom = { "ver", "assoc", "cd", "cls", "copy", "del", "dir", "date", "echo", "@echo", "mode", "@mode",
			"exit", "md", "move", "path", "pause", "prompt", "rd", "rem", "start", "time", "type", "on", "vol",
			"attrib", "chkdsk", "choice", "cmd", "comp", "convert", "driverquery", "expand", "find", "format", "help",
			"ipconfig", "label", "more", "net", "ping", "shutdown", "sort", "subst", "subst", "systeminfo", "taskkill",
			"xcopy", "tree", "fc", "title", "set", "bash", "node", "off", "goto", "rmdir", "icacls", "takeown", "if",
			"for", "else", "git", "npm", "call", "exist", "end", "java", "javac", "javaw", "nodemon", "csc", "nasm", "pip",
			"pip3", "pipwin", "as", "ld", "7z", "rename", "bash", "export", "vi", "vim", "nano", "clang", "qemu", "qemu-system-x86-64",
			"qemu", "gcc", "g++", "python", "lua", "eject", "tsc", "setlocal", "endlocal", "make", "yarn", "color",
			"VER", "ASSOC", "CD", "CLS", "COPY", "DEL", "DIR", "DATE", "ECHO", "@ECHO", "MODE", "@MODE", "EXIT", "MD", "MOVE", "PATH", "PAUSE",
			"PROMPT", "RD", "REM", "START", "TIME", "TYPE", "VOL", "ATTRIB", "CHKDSK", "CHOICE", "CMD", "COMP",
			"CONVERT", "ON", "DRIVERQUERY", "EXPAND", "FIND", "FORMAT", "HELP", "IPCONFIG", "LABEL", "MORE", "NET",
			"PING", "SHUTDOWN", "SORT", "SUBST", "SUBST", "SYSTEMINFO", "TASKKILL", "XCOPY", "TREE", "FC", "TITLE",
			"SET", "BASH", "NODE", "OFF", "GOTO", "RMDIR", "ICACLS", "TAKEOWN", "IF", "FOR", "ELSE", "GIT", "NPM",
			"CALL", "EXIST", "END", "JAVA", "JAVAC", "JAVAW", "NODEMON", "CSC", "NASM", "QEMU", "GCC", "G++", "PYTHON",
			"PIP", "PIP3", "PIPWIN", "AS", "LD", "7Z", "RENAME", "BASH", "EXPORT", "VI", "VIM", "NANO", "CLANG", "QEMU", "QEMU-SYSTEM-x86-64", "QEMU-SYSTEM-X86-64",
			"LUA", "EJECT", "TSC", "SETLOCAL", "ENDLOCAL", "MAKE", "YARN", "COLOR" };
	
	public static final String[] porKeys = { "programa", "funcao", "inteiro", "caracter", "real", "cadeia", "para", "se", "senao", "enquanto",
			"faca", "inclua", "biblioteca", "retorne" };

	// N�o vai ter aqui as extens�es do word, powerpoint, excel etc.
	/*public static final String[] extensions = { ".java", ".c", ".cpp", ".cc", ".cs", ".py", ".pyx", ".ipynb", ".js",
			".mjs", ".bat", ".cmd", ".com", ".ps1", ".h", ".hh", ".hpp", ".hxx", ".asm", ".s", ".lua", ".sql", ".swift",
			".rs", ".php", ".kt", ".vue", ".rb", ".ino", ".ts", ".tsx", ".go", ".r", ".pl", ".t", ".jl", ".has", ".hs",
			".fs", ".coffee", ".m", ".mm", ".pas", ".lpr", ".pp", ".scala", ".dart", ".zig", ".html", ".xhtml", ".htm",
			".css", ".scss", ".xml", ".json", ".jsonc", ".md", ".markdown", ".txt", ".log", ".pdf", ".jar", ".svg",
			".urna", ".save", ".conf", ".makefile", ".mk", ".mak", ".make", ".sh", ".bash_profile", ".bashrc", ".gitignore", ".dockerfile",
			".class", ".zip", ".bin", ".license", ".cfg", ".config", ".jsx", ".ejs", ".ld", ".lock", ".ini", ".dll",
			".url", ".authors", ".img", ".flp", ".prefs", ".classpath", ".project", ".sln", ".JAVA", ".C", ".CPP",
			".CC", ".CS", ".PY", ".PYX", ".IPYNB", ".JS", ".BAT", ".CMD", ".COM", ".PS1", ".H", ".HH", ".HPP", ".HXX",
			".ASM", ".S", ".LUA", ".SQL", ".SWIFT", ".RS", ".PHP", ".KT", ".VUE", ".RB", ".INO", ".TS", ".TSX", ".GO",
			".R", ".PL", ".T", ".JL", ".HAS", ".HS", ".FS", ".COFFEE", ".M", ".MM", ".PAS", ".LPR", ".PP", ".SCALA",
			".DART", ".ZIG", ".HTML", ".XHTML", ".HTM", ".CSS", ".XML", ".JSON", ".JSONC", ".MD", ".MARKDOWN", ".TXT",
			".LOG", ".PDF", ".JAR", ".SVG", ".URNA", ".SAVE", ".CONF", ".MAKEFILE", ".MK", ".MAK", ".MAKE", ".SH", ".BASH_PROFILE", ".BASHRC",
			".GITIGNORE", ".DOCKERFILE", ".CLASS", ".ZIP", ".BIN", ".LICENSE", ".CFG", ".CONFIG", ".JSX", ".EJS", ".LD",
			".LOCK", ".INI", ".DLL", ".URL", ".AUTHORS", ".IMG", ".FLP", ".PREFS", ".CLASSPATH", ".PROJECT", ".SLN" };*/

	public static final String[] luaKeys = { "and", "break", "do", "else", "elseif", "end", "false", "for", "function",
			"if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while",
			"require", "self", "const" };

	public static final String[] zigKeys = { "align", "allowzero", "and", "anyframe", "anytype", "asm", "async",
			"await", "break", "catch", "comptime", "const", "continue", "defer", "else", "enum", "errdefer", "error",
			"export", "extern", "false", "fn", "for", "if", "inline", "noalias", "nosuspend", "null", "or", "orelse",
			"packed", "pub", "resume", "return", "linksection", "struct", "suspend", "switch", "test", "threadlocal",
			"true", "try", "undefined", "union", "unreachable", "usingnamespace", "var", "volatile", "while" };

	public static final String[] sqlKeys = { "ADD", "ADD CONSTRAINT", "ALTER", "ALTER COLUMN", "ALTER TABLE", "ALL",
			"AND", "ANY", "AS", "ASC", "BACKUP DATABASE", "BETWEEN", "CASE", "CHECK", "COLUMN", "CONSTRAINT", "CREATE",
			"CREATE DATABASE", "CREATE INDEX", "CREATE OR REPLACE VIEW", "CREATE TABLE", "CREATE PROCEDURE",
			"CREATE UNIQUE INDEX", "CREATE VIEW", "DATABASE", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP",
			"DROP COLUMN", "DROP CONSTRAINT", "DROP DATABASE", "DROP DEFAULT", "DROP INDEX", "DROP TABLE", "DROP VIEW",
			"EXEC", "EXISTS", "FOREIGN KEY", "FROM", "FULL OUTER JOIN", "GROUP BY", "HAVING", "IN", "INDEX",
			"INNER JOIN", "INSERT INTO", "INSERT INTO SELECT", "IS NULL", "IS NOT NULL", "JOIN", "LEFT JOIN", "LIKE",
			"LIMIT", "NOT", "NOT NULL", "OR", "ORDER BY", "OUTER JOIN", "PRIMARY KEY", "PROCEDURE", "RIGHT JOIN",
			"ROWNUM", "SELECT", "SELECT DISTINCT", "SELECT INTO", "SELECT TOP", "SET", "TABLE", "TOP", "TRUNCATE TABLE",
			"UNION", "UNION ALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE", "add", "add constraint", "alter",
			"alter column", "alter table", "all", "and", "any", "as", "asc", "backup database", "between", "case",
			"check", "column", "constraint", "create", "create database", "create index", "create or replace view",
			"create table", "create procedure", "create unique index", "create view", "database", "default", "delete",
			"desc", "distinct", "drop", "drop column", "drop constraint", "drop database", "drop default", "drop index",
			"drop table", "drop view", "exec", "exists", "foreign key", "from", "full outer join", "group by", "having",
			"in", "index", "inner join", "insert into", "insert into select", "is null", "is not null", "join",
			"left join", "like", "limit", "not", "not null", "or", "order by", "outer join", "primary key", "procedure",
			"right join", "rownum", "select", "select distinct", "select into", "select top", "set", "table", "top",
			"truncate table", "union", "union all", "unique", "update", "values", "view", "where" };

	public static final String[] asmRegs = { "rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rbp", "rsp", "r8", "r9", "r10",
			"r11", "r12", "r13", "r14", "r15", "eax", "ebx", "ecx", "esi", "edi", "ebp", "esp", "r8d", "r9d", "r10d",
			"r11d", "r12d", "r13d", "r14d", "r15d", "ax", "bx", "cx", "dx", "si", "di", "bp", "sp", "r8w", "r9w",
			"r10w", "r11w", "r12w", "r13w", "r14w", "r15w", "al", "bl", "cl", "dl", "sil", "dil", "bpl", "spl", "r8b",
			"r9b", "r10b", "r11b", "r12b", "r13b", "r14b", "r15b", "ah", "bh", "ch", "dh", "edx", "ss", "sp", "ds",
			"es" };

	// n�o vai colorir keys de uma s� letra
	public static final String[] asmKeys = { "global", "define", "db", "dw", "equ", "extern", "include", "times", "org", // ta faltando hein, "movzx", "mova"
			"bits", "syscall", "aaa", "aad", "aam", "aas", "adc", "add", "addpd", "addps", "addressing", "addsd",
			"addss", "jz", "align", "and", "andnpd", "andnps", "andpd", "andps", "arpl", "as", "commandline",
			"ELFobjectfile", "macroprocessing", "syntaxUNIXversusIntel", "ascii", "assemblerSeeasB", "bcd",
			"binaryarithmeticinstructions", "bitinstructions", "bound", "bsf", "bsr", "bswap", "bt", "btc", "btr",
			"bts", "byte", "byte", "byte", "byte", "byteinstructionsC", "call", "cbtw", "clc", "cld", "clflush", "cli",
			"cltd", "cltq", "clts", "cmc", "cmova", "cmova", "cmovae", "cmovae", "cmovb", "cmovb", "cmovbe", "cmovbe",
			"cmovc", "cmovc", "cmove", "cmove", "cmovg", "cmovg", "cmovge", "cmovge", "cmovl", "cmovl", "cmovle",
			"cmovle", "cmovna", "cmovna", "cmovnae", "cmovnae", "cmovnb", "cmovnb", "cmovnbe", "cmovnbe", "cmovnc",
			"cmovnc", "cmovne", "cmovne", "cmovng", "cmovng", "cmovnge", "cmovnge", "cmovnl", "cmovnl", "cmovnle",
			"cmovnle", "cmovno", "cmovno", "cmovnp", "cmovnp", "cmovns", "cmovns", "cmovnz", "cmovnz", "cmovo", "cmovo",
			"cmovp", "cmovp", "cmovpe", "cmovpo", "cmovs", "cmovz", "cmp", "cmppd", "cmpps", "cmps", "cmpsb", "cmpsd",
			"controltransferinstructions", "cpp", "cpuid", "cqtd", "cqto", "cvtdqpd", "cvtdqps", "cvtpddq", "cvtpdpi",
			"cvtpdps", "cvtpipd", "cvtpips", "cvtpsdq", "cvtpspd", "cvtpspi", "cvtsdsi", "cvtsdss", "cvtsisd",
			"cvtsiss", "cvtsssd", "cvtsssi", "cvttpddq", "cvttpdpi", "cvttpsdq", "cvttpspi", "cvttsdsi", "cvttsssi",
			"cwtd", "cwtlD", "daa", "das", "datatransferinstructions", "dec", "decimalarithmeticinstructions",
			"directives", "div", "divpd", "divps", "divsd", "divss", "doubleE", "ELFobjectfile", "emms", "enter",
			"even", "extF", "fxm", "fabs", "fadd", "faddp", "fbe", "Seeas", "fbld", "fbstp", "fchs", "fclex", "fcmovb",
			"fcmovbe", "fcmove", "fcmovnb", "fcmovnbe", "fcmovne", "fcmovnu", "fcmovu", "fcom", "fcomi", "fcomip",
			"fcomp", "fcompp", "fcos", "fdecstp", "fdiv", "fdivp", "fdivr", "fdivrp", "ffree", "fiadd", "ficom",
			"ficomp", "fidiv", "fidivr", "fild", "file", "fimul", "fincstp", "finit", "fist", "fistp", "fisub",
			"fisubr", "flagcontrolinstructions", "fld", "fld", "fldcw", "fldenv", "fldle", "fldlt", "fldlg", "fldln",
			"fldpi", "fldz", "float", "floating-pointinstructions", "basicarithmetic", "comparison", "control",
			"datatransfer", "loadconstants", "logarithmic", "Seetranscendental", "transcendental", "trigonometric",
			"Seetranscendental", "fmul", "fmulp", "fnclex", "fninit", "fnop", "fnsave", "fnstcw", "fnstenv", "fnstsw",
			"fpatan", "fprem", "fprem", "fptan", "frndint", "frstor", "fsave", "fscale", "fsin", "fsincos", "fsqrt",
			"fst", "fstcw", "fstenv", "fstp", "fstsw", "fsub", "fsubp", "fsubr", "fsubrp", "ftst", "fucom", "fucomi",
			"fucomip", "fucomp", "fucompp", "fwait", "fxam", "fxch", "fxrstor", "fxsave", "fxtract", "fylx", "fylxp",
			"G", "gas", "globl", "group", "H", "hidden", "hlt", "ident", "identifier", "idiv", "imul", "inc", "ins",
			"insb", "insl", "instruction", "format", "suffixes", "instructions", "binaryarithmetic", "bit", "byte",
			"controltransfer", "datatransfer", "decimalarithmetic", "flagcontrol", "floating-point-", "logical",
			"miscellaneous", "MMX-", "operatingsystemsupport-", "Opteron", "rotate", "segmentregister", "shift",
			"SIMDstatemanagement", "SSE-", "SSE-", "string", "insw", "int", "into", "invd", "invlpg", "iretJ", "ja",
			"jae", "jb", "jbe", "jc", "jcxz", "je", "jecxz", "jg", "jge", "jl", "jle", "jmp", "jnae", "jnb", "jnbe",
			"jnc", "jne", "jng", "jnge", "jnl", "jnle", "jno", "jnp", "jns", "jnz", "jo", "jp", "jpe", "jpo", "js",
			"jzK", "keywordL", "label", "numeric", "symbolic", "lahf", "lar", "lcall", "lcomm", "ldmxcsr", "lds", "lea",
			"leave", "les", "lfence", "lfs", "lgdt", "lgs", "lidt", "lldt", "lmsw", "local", "lock", "lods", "lodsb",
			"lodsl", "lodsw", "logicalinstructions", "long", "loop", "loope", "loopne", "loopnz", "loopz", "lret",
			"lsl", "lss", "ltr", "maskmovdqu", "maskmovq", "maxpd", "maxps", "maxsd", "maxss", "mfence", "minpd",
			"minps", "minsd", "minss", "miscellaneousinstructions", "MMXinstructions", "comparison", "conversion",
			"datatransfer", "logical", "packedarithmetic", "rotate", "shift", "statemanagement", "mov", "movl", "movabs",
			"movabsA", "movapd", "movaps", "movd", "movdqq", "movdqa", "movdqu", "movhlps", "movhpd", "movhps",
			"movlhps", "movlpd", "movlps", "movmskpd", "movmskps", "movntdq", "movnti", "movntpd", "movntps", "movntq",
			"movq", "movqdq", "movs", "movsb", "movsd", "movsl", "movss", "movsw", "movupd", "movups", "movzb", "movzw",
			"mul", "mulpd", "mulps", "mulsd", "mulss", "neg", "nop", "not", "numbers", "floatingpoint", "integers",
			"binary", "decimal", "hexadecimal", "octal", "operands", "immediate", "indirect", "memory", "addressing",
			"ordering", "register", "operatingsystemsupportinstructions", "Opteroninstructions", "or", "orpd", "orps",
			"out", "outs", "outsb", "outsl", "outswP", "packssdw", "packsswb", "packuswb", "paddb", "paddd", "paddq",
			"paddsb", "paddsw", "paddusb", "paddusw", "paddw", "pand", "pandn", "pause", "pavgb", "pavgw", "pcmpeqb",
			"pcmpeqd", "pcmpeqw", "pcmpgtb", "pcmpgtd", "pcmpgtw", "pextrw", "pinsrw", "pmaddwd", "pmaxsw", "pmaxub",
			"pminsw", "pminub", "pmovmskb", "pmulhuw", "pmulhw", "pmullw", "pmuludq", "pop", "popa", "popal", "popaw",
			"popf", "popfw", "popsection", "por", "prefetchnta", "prefetcht", "prefetcht", "prefetcht", "previous",
			"psadbw", "pshufd", "pshufhw", "pshuflw", "pshufw", "pslld", "pslldq", "psllq", "psllw", "psrad", "psraw",
			"psrld", "psrldq", "psrlq", "psrlw", "psubb", "psubd", "psubq", "psubsb", "psubsw", "psubusb", "psubusw",
			"psubw", "punpckhbw", "punpckhdq", "punpckhqdq", "punpckhwd", "punpcklbw", "punpckldq", "punpcklqdq",
			"punpcklwd", "push", "pusha", "pushal", "pushaw", "pushf", "pushfw", "pushsection", "pxor", "quad", "rcl",
			"rcpps", "rcpss", "rcr", "rdmsr", "rdpmc", "rdtsc", "rel", "rep", "repnz", "repz", "ret", "retn", "rol",
			"ror", "rotateinstructions", "rsm", "rsqrtps", "rsqrtss", "sahf", "sal", "sar", "sbb", "scas", "scasb",
			"scasl", "scasw", "section", "segmentregisterinstructions", "set", "seta", "setae", "setb", "setbe", "setc",
			"sete", "setg", "setge", "setl", "setle", "setna", "setnae", "setnb", "setnbe", "setnc", "setne", "setng",
			"setnge", "setnl", "setnle", "setno", "setnp", "setns", "setnz", "seto", "setp", "setpe", "setpo", "sets",
			"setz", "sfence", "sgdt", "shiftinstructions", "shl", "shld", "shr", "shrd", "shufpd", "shufps", "sidt",
			"SIMDstatemanagementinstructions", "skip", "sldt", "sleb", "smovl", "smsw", "sqrtpd", "sqrtps", "sqrtsd",
			"sqrtss", "SSEinstructions", "compare", "conversion", "datatransfer", "integer", "logical", "miscellaneous",
			"MXCSRstatemanagement", "packedarithmetic", "shuffle", "unpack", "SSEinstructions", "compare", "conversion",
			"datamovement", "logical", "miscellaneous", "packedarithmetic", "packedsingle-precisionfloating-point",
			"shuffle", "SIMDintegerinstructions", "unpack", "statement", "empty", "stc", "std", "sti", "stmxcsr",
			"stos", "stosb", "stosl", "stosw", "stringinstructions", "sub", "subpd", "subps", "subsd", "subss",
			"symbolic", "sysenter", "sysexit", "tbss", "tcomm", "tdata", "test", "ucomisd", "ucomiss", "ud", "uleb",
			"unpckhpd", "unpckhps", "unpcklpd", "unpcklps", "value", "verr", "verw", "wait", "wbinvd", "weak",
			"whitespace", "wrmsr", "xadd", "xchg", "xchgA", "xlat", "xlatb", "xor", "xorpd", "xorps", "zero" }; // n�o
																												// vai
																												// colorir
																												// "str",
																												// "string"

	public static final String[] cll = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public static final String[] sections = { "data", "text", "bss", "DATA", "TEXT", "BSS" };

	public static final String[] jlKeys = { "baremodule", "begin", "break", "catch", "const", "continue", "do", "else",
			"elseif", "end", "export", "false", "finally", "for", "function", "global", "if", "import", "let", "local",
			"macro", "module", "quote", "return", "struct", "true", "try", "using", "while" };

	public static final String[] plKeys = { "-A", "END", "length", "setpgrp", "-B", "endgrent", "link", "setpriority",
			"-b", "endhostnet", "listen", "setprotoent", "-C", "endnetent", "local", "setpwent", "-c", "endprotoent",
			"localtime", "setservent", "-d", "endpwent", "log", "setsockopt", "-e", "endservent", "lstat", "shift",
			"-f", "eof", "map", "shmctl", "-g", "eval", "mkdir", "shmget", "-k", "exec", "msgctl", "shmread", "-l",
			"exists", "msgget", "shmwrite", "-M", "exit", "msgrcv", "shutdown", "-O", "fcntl", "msgsnd", "sin", "-o",
			"fileno", "my", "sleep", "-p", "flock", "next", "socket", "-r", "fork", "not", "socketpair", "-R", "format",
			"oct", "sort", "-S", "formline", "open", "splice", "-s", "getc", "opendir", "split", "-T", "getgrent",
			"ord", "sprintf", "-t", "getgrgid", "our", "sqrt", "-u", "getgrnam", "pack", "srand", "-w", "gethostbyaddr",
			"pipe", "stat", "-W", "gethostbyname", "pop", "state", "-X", "gethostent", "pos", "study", "-x", "getlogin",
			"print", "substr", "-z", "getnetbyaddr", "printf", "symlink", "abs", "getnetbyname", "prototype", "syscall",
			"accept", "getnetent", "push", "sysopen", "alarm", "getpeername", "quotemeta", "sysread", "atan2",
			"getpgrp", "rand", "sysseek", "AUTOLOAD", "getppid", "read", "system", "BEGIN", "getpriority", "readdir",
			"syswrite", "bind", "getprotobyname", "readline", "tell", "binmode", "getprotobynumber", "readlink",
			"telldir", "bless", "getprotoent", "readpipe", "tie", "break", "getpwent", "recv", "tied", "caller",
			"getpwnam", "redo", "time", "chdir", "getpwuid", "ref", "times", "CHECK", "getservbyname", "rename",
			"truncate", "chmod", "getservbyport", "rename", "umask", "chown", "getsockopt", "reverse", "undef", "chr",
			"glob", "rewinddir", "UNITCHECK", "chroot", "gmtime", "rindex", "unlink", "close", "goto", "rmdir",
			"unpack", "closedir", "grep", "say", "unshift", "connect", "hex", "scalar", "untie", "cos", "index", "seek",
			"use", "crypt", "INIT", "seekdir", "utime", "dbmclose", "int", "select", "values", "dbmopen", "ioctl",
			"semctl", "vec", "defined", "join", "semget", "wait", "delete", "keys", "semop", "waitpid", "DESTROY",
			"kill", "send", "wantarray", "die", "last", "setgrent", "warn", "dump", "lc", "sethostent", "write", "each",
			"lcfirst", "setnetent", "__DATA__", "else", "lock", "qw", "__END__", "elsif", "lt", "qx", "__FILE__", "eq",
			"m", "s", "__LINE__", "exp", "ne", "sub", "__PACKAGE__", "for", "no", "tr", "and", "foreach", "or",
			"unless", "cmp", "ge", "package", "until", "continue", "gt", "q", "while", "CORE", "if", "qq", "xor", "do",
			"le", "qr", "y" };

	public static final String[] hasKeys = { "as", "case", "of", "class", "data", "family", "data", "instance",
			"default", "deriving", "do", "forall", "foreign", "hiding", "if", "then", "else", "import", "infix",
			"infixl", "infixr", "let", "in", "mdo", "module", "newtype", "proc", "qualified", "rec", "type", "where" };

	public static final String[] fsKeys = { "abstract", "and", "as", "assert", "base", "begin", "class", "default",
			"delegate", "do", "done", "downcast", "downto", "elif", "else", "end", "exception", "extern", "false",
			"finally", "fixed", "for", "fun", "function", "global", "if", "in", "inherit", "inline", "interface",
			"internal", "lazy", "let", "match", "member", "module", "mutable", "namespace", "new", "not", "null", "of",
			"open", "or", "override", "private", "public", "rec", "return", "select", "static", "struct", "then", "to",
			"true", "try", "type", "upcast", "use", "val", "void", "when", "while", "with", "yield", "const", "asr",
			"land", "lor", "lsl", "lsr", "lxor", "mod", "sig", "atomic", "break", "checked", "component", "const",
			"constraint", "constructor", "continue", "eager", "event", "external", "functor", "include", "method",
			"mixin", "object", "parallel", "process", "protected", "pure", "sealed", "tailcall", "trait", "virtual",
			"volatile" };

	public static final String[] cfKeys = { "for", "while", "loop", "by", "in", "of", "break", "continue", "if", "then",
			"else", "unless", "switch", "when", "default", "return", "do", "is", "isnt", "and", "or", "not", "true",
			"yes", "on", "false", "no", "off", "throw", "try", "catch", "finally", "new", "delete", "class", "extends",
			"super", "typeof", "instanceof", "this", "arguments", "await", "defer", "yield", "null", "undefined",
			"Infinity", "NaN", "export", "import", "package", "let", "case", "debugger", "function", "var", "with",
			"private", "protected", "public", "native", "static", "const", "implements", "interface", "void", "enum" };

	public static final String[] swKeys = { "associatedtype", "class", "deinit", "enum", "extension", "fileprivate",
			"func", "import", "init", "inout", "internal", "let", "open", "operator", "private", "protocol", "public",
			"rethrows", "static", "struct", "subscript", "typealias", "var", "break", "case", "continue", "default",
			"defer", "do", "else", "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "where",
			"while", "as", "Any", "catch", "false", "is", "nil", "super", "self", "self", "throw", "throws", "true",
			"try", "_", "#available", "#colorLiteral", "#column", "#else", "#elseif", "#endif", "#error", "#file",
			"#fileID", "#fileLiteral", "#filePath", "#function", "#if", "#imageLiteral", "#line", "#selector",
			"#sourceLocation", "#warning", "associativity", "convenience", "dynamic", "didset", "final", "get", "infix",
			"indirect", "lazy", "left", "mutating", "none", "nonmutating", "optional", "override", "postfix",
			"precendence", "prefix", "Protocol", "required", "right", "set", "Type", "unowned", "weak", "willSet" };

	public static final String[] rsKeys = { "as", "break", "const", "continue", "crate", "else", "enum", "extern",
			"false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref",
			"return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe", "use", "where",
			"while", "async", "await", "dyn", "abstract", "become", "box", "do", "final", "macro", "override", "priv",
			"typeof", "unsized", "virtual", "yield", "try", "union", "'static" };

	public static final String[] shKeys = { "pwd", "cd", "ls", "cat", "cp", "mv", "mkdir", "rmdir", "rm", "touch", "case",
			"locate", "find", "grep", "sudo", "su", "df", "du", "head", "tail", "diff", "tar", "chmod", "chown", "jobs",
			"kill", "ping", "wget", "uname", "top", "history", "man", "echo", "zip", "unzip", "hostname", "useradd",
			"userdel", "clear", "git", "npm", "call", "exist", "end", "java", "javac", "javaw", "nodemon", "csc",
			"node", "nasm", "qemu", "gcc", "g++", "python", "lua", "if", "then", "else", "fi", "date", "eject", "tsc",
			"pip", "pip3", "pipwin", "read", "export", "as", "ld", "7z", "rename", "bash", "vi", "vim", "nano", "clang", "qemu", "qemu-system-x86-64",
			"setlocal", "endlocal", "make", "yarn", "color", "for", "PWD", "CD", "LS", "CAT ", "CP", "MV", "MKDIR",
			"RMDIR", "RM", "TOUCH", "LOCATE", "FIND", "GREP", "SUDO", "SU", "DF", "DU", "HEAD", "TAIL", "DIFF", "TAR",
			"CHMOD", "CHOWN", "JOBS", "KILL", "PING", "WGET", "UNAME", "TOP", "HISTORY", "MAN", "ECHO", "ZIP", "UNZIP",
			"HOSTNAME", "USERADD", "EXPORT", "USERDEL", "CLEAR", "GIT", "NPM", "CALL", "EXIST", "END", "EJECT", "SETLOCAL",
			"ENDLOCAL", "FOR", "JAVA", "JAVAC", "NODEMON", "CSC", "NODE", "QEMU", "GCC", "G++", "PYTHON", "LUA",
			"PIP", "PIP3", "PIPWIN", "READ", "AS", "LD", "7Z", "RENAME", "BASH", "VI", "VIM", "NANO", "CLANG", "QEMU", "QEMU-SYSTEM-x86-64", "QEMU-SYSTEM-X86-64",
			"JAVAW", "IF", "THEN", "ELSE", "FI", "DATE", "YARN", "COLOR", "TSC", "MAKE" };

	public static final String[] tsKeys = { "type", "number", "protected", "else", "let", "catch", "if", "case", "in",
			"byte", "double", "var", "module", "enum", "as", "transient", "document", "long", "undefined", "default",
			"goto", "native", "yield", "get", "typeof", "break", "abstract", "throw", "char", "return", "synchronized",
			"debugger", "do", "float", "while", "continue", "function", "export", "new", "package", "static", "void",
			"finally", "this", "throws", "extends", "null", "final", "true", "try", "implements", "private",
			"const", "import", "string", "for", "interface", "delete", "switch", "public", "of", "await", "class",
			"console", "false", "volatile", "any", "int", "instanceof", "super", "with", "async", "declare",
			"namespace", "boolean", "short", "arguments", "window", "as", "from", "navigator", "constructor", "debug",
			"array", "object", "any", "mutation", "set", "bigint" };

	public static final String[] ktKeys = { "as", "as?", "break", "class", "continue", "do", "else", "false", "for",
			"fun", "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return", "super", "this",
			"throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while", "by", "catch", "constructor",
			"delegate", "dynamic", "field", "file", "finally", "get", "import", "init", "param", "property", "receiver",
			"set", "setparam", "value", "class", "where", "actual", "abstract", "annotation", "companion", "const",
			"crossinline", "data", "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
			"lateinit", "noinline", "open", "operator", "out", "override", "private", "protected", "public", "reified",
			"sealed", "suspend", "tailrec", "vararg", "field", "it" };

	public static final String[] rbKeys = { "_ENCODING_", "_LINE_", "_FILE_", "BEGIN", "END", "alias", "and", "begin",
			"break", "case", "class", "def", "defined?", "do", "else", "elsif", "end", "ensure", "false", "for", "if",
			"in", "module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then",
			"true", "undef", "unless", "until", "when", "while", "yield", "it", "each", "before", "new", "puts" };

	public static final String[] scaKeys = { "abstract", "finally", "object", "trait", "catch", "forSome", "package",
			"try", "class", "if", "private", "type", "def", "implicit", "protected", "val", "else", "lazy", "sealed",
			"while", "false", "new", "this", "yield", "final", "null", "throw" };

	public static final String[] goKeys = { "break", "default", "func", "interface", "select", "case", "defer", "go",
			"map", "struct", "chan", "else", "goto", "package", "switch", "const", "fallthrough", "if", "range", "type",
			"continue", "for", "import", "return", "var",
			"bool", "int", "float32", "float64", "string", "uint8", "uint16", "uint32", "uint64", "int8", "int16",
			"int32", "int64", "uint", "uintptr", "complex64", "complex128", "true", "false", "nil", "byte" };

	public static final String[] vbKeys = { "AddHandler", "AddressOf", "Alias", "And", "AndAlso", "As", "Boolean",
			"ByRef", "Byte", "ByVal", "Call", "Case", "Catch", "CBool", "CByte", "CChar", "CDate", "CDbl", "CDec",
			"Char", "CInt", "Class", "Constraint", "Statement", "CLng", "CObj", "Const", "Continue", "CSByte", "CShort",
			"CSng", "CStr", "CType", "CUInt", "CULng", "CUShort", "Date", "Decimal", "Declare", "Default", "Delegate",
			"Dim", "DirectCast", "Do", "Double", "Each", "Else", "ElseIf", "End", "EndIf", "Enum", "Erase", "Error",
			"Event", "Exit", "False", "Finally", "For", "Friend", "Function", "Get", "GetType", "GetXMLNamespace",
			"Global", "GoSub", "GoTo", "Handles", "If", "Implements", "Imports", "In", "Inherits", "Integer", "Is",
			"IsNot", "Let", "Lib", "Like", "Long", "Loop", "Me", "Mod", "Module", "MustInherit", "MustOverride",
			"MyBase", "MyClass", "NameOf", "Namespace", "Narrowing", "New", "Next", "Not", "Nothing", "NotInheritable",
			"NotOverridable", "Object", "Of", "On", "Operator", "Option", "Optional", "Or", "OrElse", "Out",
			"Overloads", "Overridable", "Overrides", "ParamArray", "Partial", "Private", "Property", "Protected",
			"Public", "RaiseEvent", "ReadOnly", "ReDim", "REM", "RemoveHandler", "Resume", "Return", "SByte", "Select",
			"Set", "Shadows", "Shared", "Short", "Single", "Static", "Step", "Stop", "String", "Structure", "Sub",
			"SyncLock", "Then", "Throw", "To", "True", "Try", "TryCast", "TypeOf", "UInteger", "ULong", "UShort",
			"Using", "Variant", "Wend", "When", "While", "Widening", "With", "WithEvents", "WriteOnly", "Xor" };

	public static final String[] objKeys = { "auto", "break", "case", "char", "const", "continue", "default", "do",
			"double", "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register",
			"restrict", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
			"unsigned", "void", "volatile", "while", "_Bool", "_Complex", "_Imaginary", "BOOL", "Class", "bycopy",
			"byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL", "oneway", "out", "Protocol", "SEL", "self",
			"super", "YES", "@interface", "@end", "@implementation", "@protocol", "@class", "@public", "@protected",
			"@private", "@property", "@try", "@throw", "@catch", "@finally", "@synthesize", "@dynamic", "@selector",
			"atomic", "nonatomic", "retain" };

	public static final String[] ideConfKeys = { "Arquivo de Configura��es da Boot IDE", "Boot IDE Configuration File",
			"port", "eng", "PORT", "ENG", "Colors", "Files", "Settings", "default", "true", "false" };

	public static final String[] makeKeys = { "if", "else", "export" };

	public static final String[] dkKeys = { "FROM", "RUN", "VOLUME", "WORKDIR", "ADD", "CMD", "ENTRYPOINT", "ENV",
			"EXPOSE", "MAINTAINER", "USER", "from", "run", "volume", "workdir", "add", "cmd", "entrypoint", "env",
			"expose", "maintainer", "user" };

	public static final String[] specialHtmlVariables = { "html" };

	public static final String[] jsonKeys = { "true", "false", "True", "False", "null" };

	public static final String[] bfKeys = { "+", "-", ">", "<", ".", ",", "[", "]" };

	public static final String[] gdKeys = { "if", "elif", "else", "for", "while", "match", "break", "continue", "pass",
			"return", "class", "class_name", "extends", "is", "as", "self", "tool", "signal", "func", "static", "const",
			"enum", "var", "onready", "export", "setget", "breakpoint", "preload", "yield", "assert", "remote",
			"master", "puppet", "remotesync", "mastersync", "puppetsync", "PI", "TAU", "INF", "NAN", "bool", "int",
			"float", "String", "void", "true", "false" };
	
	public static final String[] mcKeys = { "true", "false", "minecraft", "?", "ability", "advancement", "alwaysday", "attribute", "ban", "ban-ip",
			"banlist", "bossbar", "camerashake", "changesetting", "clear", "clearspawnpoint", "clone", "connect", "data", "datapack", "daylock",
			"debug", "dedicatedwsserver", "defaultgamemode", "deop", "dialogue", "difficulty", "effect", "enchant", "event", "execute", "experience",
			"fill", "fog", "forceload", "function", "gamemode", "gamerule", "gametest", "give", "help", "immutableworld", "item", "jfr", "kick",
			"fill", "list", "locate", "locatebiome", "loot", "me", "mobevent", "msg", "music", "op", "ops", "pardon", "pardon-ip", "particle", "perf",
			"permission", "playanimation", "playsound", "publish", "recipe", "reload", "remove", "replaceitem", "ride", "save", "save-all", "save-off",
			"save-on", "say", "schedule", "scoreboard", "seed", "setblock", "setidletimeout", "setmaxplayers", "setworldspawn", "spawnpoint", "spectate",
			"spreadplayers", "stop", "stopsound", "structure", "summon", "tag", "team", "teammsg", "tell", "tellraw", "testfor", "testforblock",
			"testforblocks", "tickingarea", "time", "title", "titleraw", "tm", "toggledownfall", "tp", "trigger", "w", "wb", "weather", "whitelist",
			"worldborder", "worldbuilder", "wsserver", "xp" };
	
	public static final String[] oCamlKeys = { "and", "as", "assert", "asr", "begin", "class", "constraint", "do", "done", "downto", "else", "end",
			"exception", "external", "false", "for", "fun", "function", "functor", "if", "in", "include", "inherir", "initializer", "land", "lazy",
			"let", "lor", "lsl", "lsr", "lxor", "match", "method", "mod", "module", "mutable", "new", "nonrec", "object", "of", "open", "or", "private",
			"rec", "sig", "struct", "then", "to", "true", "try", "type", "val", "virtual", "when", "while", "with" };
	
	public static final String[] tfKeys = { "resource", "provider", "true", "false", "any", "variable", "string", "number", "bool" };
	
	public static final String[] vKeys = { "as", "asm", "assert", "atomic", "break", "const", "continue", "defer", "else", "embed", "enum", "false", "fn",
			"for", "go", "goto", "if", "import", "in", "interface", "is", "lock", "match", "module", "mut", "none", "or", "pub", "return", "rlock", "select",
			"shared", "sizeof", "static", "struct", "true", "type", "typeof", "union", "unsafe", "volatile", "__offsetof", "bool", "string", "i8", "i16",
			"int", "i64", "i128", "u8", "u16", "u32", "u64", "u128", "rune", "f32", "f64", "isize", "usize", "voidptr", "any" };
	
	public static final String[] basKeys = { "let", "data", "read", "restore", "dim", "if", "then", "else", "for", "to", "step", "next", "while", "wend",
			"repeat", "until", "do", "loop", "until", "goto", "gosub", "on", "def", "fn", "list", "print", "input", "tab", "spc", "abs", "atn", "cos",
			"exp", "int", "log", "rnd", "sin", "sqr", "tan", "rem", "usr", "call", "tron", "troff", "asm", "sub", "as", "poke", "peek", "single", "long",
			"integer", "string", "and", "or", "xor", "not",
			 "LET", "DATA", "READ", "RESTORE", "DIM", "IF", "THEN", "ELSE", "FOR", "TO", "STEP", "NEXT", "WHILE", "WEND", "REPEAT", "UNTIL", "DO", "LOOP",
			 "UNTIL", "GOTO", "GOSUB", "ON", "DEF", "FN", "LIST", "PRINT", "INPUT", "TAB", "SPC", "ABS", "ATN", "COS", "EXP", "INT", "LOG", "RND", "SIN",
			 "SQR", "TAN", "REM", "USR", "CALL", "TRON", "TROFF", "ASM", "SUB", "AS", "POKE", "PEEK", "SINGLE", "LONG", "INTEGER", "STRING",
			 "AND", "OR", "XOR", "NOT" };
	
	public Thread killAllTabs;
	
	public static CommandTerminal terminal;
	
	///////

	public CodeEditor(int x, int y, int width, int height) {
		super(x, y, width, height, null);

		tabs = new ArrayList<>();
		toAdd = new ArrayList<>();
		toRemove = new ArrayList<>();

		cursor = new Animation() { // d� exception por causa de stackoverflow
			private boolean flip = false;

			public void play() {
				while (true) {
					showCursor = !flip;
	
					flip = !flip;
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (Main.main != null)
						Main.main.mainLogic();
				}
			}
		};
		
		killAllTabs = new Thread("killAllTabs") {
			public void run() {
				try {
					Thread.sleep(TAB_ANIMATION_TIMEOUT);
				} catch (InterruptedException e) { // Esperar a anima��o acabar
					e.printStackTrace();
				}
				
				tabs.clear();
			}
		};

		cursorThread = new Thread("cursorThread") { // precisa ser em outra thread
			public void run() {
				cursor.play();
			}
		};

		cursorThread.start();
	}
	
	public synchronized void typeLogic() {
		try {
			if (SetFileName.added || CommandTerminal.active || RenameFile.added || Explorer.selected != null) return;
			
			// o problema � daqui pra baixo, ou � CIMA? cima pq se o loop continuar sem executar a parte de baixo continua alto o uso da cpu, e o break ou return abaixam, e a parte de cima que fica executando sempre, mas se tirar ela e deixar s� a de baixo continua alto mesmo assim
			
			//if (true) break; // - � a presen�a do loop que enche a cpu | veja se usarmos return ou break ao inv�s de continue d� certo
			
			if (KeyInput.isKeyPressed()) {
				if ((!SetFileName.added && !CommandTerminal.active) && (!(KeyInput.isAltDown() || KeyInput.isControlDown()) || KeyInput.isAltGrDown())) {
	    			Main.editor.type();
	    			Main.editor.detectArrows();
				}
				Main.editor.detectShortcuts();
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public synchronized void scrollLogic() {
		Main.editor.scroll();
		Main.editor.scrollTabs();
	}
	
	public synchronized void cursor() {
		int offset = CommandTerminal.expOff ? Main.editor.getX() : 0;
		int lcx = !CommandTerminal.expOff ? 0 : Main.editor.getX();

		int lcmx = mx;
		int lcmy = my;

		if (Main.editor == null) return;

		int off = (FONT_SIZE * 3);

		lcmy = (MouseInput.getMouseY() / (LINE_HEIGHT) - 1)
				+ (scrY / (LINE_HEIGHT));
		lcmx = (((MouseInput.getMouseX() - (Main.editor.getX() + off)) / FONT_SIZE)
				+ (scrX / FONT_SIZE));

		while (((lcx + off) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset < MouseInput
				.getMouseX()) // detecta se a posi��o real do cursor for menor do que a do cursor e fica
			// adicionando enquanto for menor
			lcmx++;

		while (((lcx + off) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset > MouseInput
				.getMouseX()) // detecta se a posi��o real do cursor for menor do que a do cursor e fica
			// adicionando enquanto for menor
			lcmx--;

		while (MIN_Y + lcmy * (LINE_HEIGHT) - FONT_SIZE - scrY - 2 < MouseInput.getMouseY()) // o mesmo para aqui, s� que com o y
			lcmy++;

		while (MIN_Y + lcmy * (LINE_HEIGHT) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, s� que com o y
			lcmy--;

		if (FONT_SIZE < 13)
			lcmx--;
		
		if (CommandTerminal.expOff)
			lcmx--;
		
		//if (CommandTerminal.expOff) lcmx += ruleOf3(16, 22, FONT_SIZE);

		lcmx = setWithinBounds(lcmx, lcmy, true);
		lcmy = setWithinBounds(lcmx, lcmy, false);

		//////////////

		my = (MouseInput.getMouseY() / (LINE_HEIGHT) - 1)
				+ (scrY / (LINE_HEIGHT));
		mx = (((MouseInput.getMouseX() - (Main.editor.getX() + off)) / FONT_SIZE) + (scrX / FONT_SIZE));

		while (((Main.editor.getX() + off) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < MouseInput
				.getMouseX()) // detecta se a posi��o real do cursor for menor do que a do cursor e fica
			// adicionando enquanto for menor
			mx++;

		while (((Main.editor.getX() + off) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX > MouseInput
				.getMouseX()) // detecta se a posi��o real do cursor for menor do que a do cursor e fica
			// adicionando enquanto for menor
			mx--;

		while (MIN_Y + my * (LINE_HEIGHT) - FONT_SIZE - scrY - 2 < MouseInput
				.getMouseY()) // o mesmo para aqui, s� que com o y
			my++;

		while (MIN_Y + my * (LINE_HEIGHT) - FONT_SIZE - scrY - 2 > MouseInput
				.getMouseY()) // o mesmo para aqui, s� que com o y
			my--;

		//if (FONT_SIZE < 13)
		mx--;

		mx = setWithinBounds(mx, my, true);
		my = setWithinBounds(mx, my, false);

		if (CommandTerminal.expOff) {
			mx = lcmx;
			my = lcmy;
		}
		
		/// another (one)
		
		realcx = ((x + (FONT_SIZE * 4)) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX;
		realcy = MIN_Y + ((cursorY - 1) * (LINE_HEIGHT)) - scrY;

		/*
		 * if (drawcx != realcx) { if (drawcx < realcx) drawcx += speed; if (drawcx >
		 * realcx) drawcx -= speed; }
		 * 
		 * if (drawcy != realcy) { if (drawcy < realcy) drawcy += speed; if (drawcy >
		 * realcy) drawcy -= speed; }
		 */

		drawcx = realcx;
		drawcy = realcy;
	}

	public boolean hovered() {
		Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
		Rectangle comp = new Rectangle(x, MIN_Y, width, height);

		return mouse.intersects(comp);
	}

	public int getLineIndex(char[] chars) {
		for (int i = 0; i < lines.size(); i++) {
			IDELine l = lines.get(i);

			char[] c = toCharArray(l.getChars());

			if (c == chars)
				return i;
		}

		return -1;
	}
	
	public static String[] splitByNCharacters(String s, int n) {
		List<String> strings = new ArrayList<String>();
		int index = 0;
		
		while (index < s.length()) {
		    strings.add(s.substring(index, Math.min(index + n, s.length())));
		    index += n;
		}
		
		String[] arr = new String[strings.size()];
		
		int i = 0;
		for (String str : strings)
			arr[i++] = str;
		
		return arr;
	}

	public static <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}
	
	/*public static String prettyBinary(String binary, int blockSize, String separator) {

        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < binary.length()) {
            result.add(binary.substring(index, Math.min(index + blockSize, binary.length())));
            index += blockSize;
        }

        return result.stream().collect(Collectors.joining(separator));
    }*/
	
	public static String getReadModeName(FileReadMode mode) {
		switch (mode) {
		case BIN:
		case BINARY:
			return Main.lang == Language.PORT ? "Bin�rio" : "Binary";
		case HEX:
			return "Hexadecimal";
		default:
			return "";
		
		}
	}
	
	public static String prettyBinary(String binary, int blockSize, String separator) {

        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < binary.length()) {
            result.add(binary.substring(index, Math.min(index + blockSize, binary.length())));
            index += blockSize;
        }

        StringBuilder b = new StringBuilder();
        
        for (String s : result) {
        	b.append(s);
        	b.append(separator);
        }
        
        return b.deleteCharAt(b.length() - 1).toString();
    }
	
	public static String convertStringToBinary(String input) {
        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))   // char -> int, auto-cast
                            .replaceAll(" ", "0")                         // zero pads
            );
        }
        return prettyBinary(result.toString(), 8, "");
    }

	public List<IDELine> readFile(File file) throws IOException {
		Main.editor.line1 = 0;
		Main.editor.line2 = 0;

		Main.editor.index1 = 0;
		Main.editor.index2 = 0;

		Main.editor.selecting = false;

		List<String> l = null;

		Path p = file.toPath();

		isReadOnly = false;

		try {
			l = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
			codeType = "UTF-8";
		} catch (Exception e) {
			l = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
			codeType = "ANSI";
		}

		if (l.isEmpty())
			l.add("");
		
		switch (editing.readMode) {
			/*case ASSEMBLY: // ainda n�o
				break;*/
				
			case BIN:
			case BINARY:
				/*List<String> newL = new ArrayList<>();
				StringBuilder b;
				
				for (String s : l) {
					b = new StringBuilder();
					
					b.append(convertStringToBinary(s));
					b.append(" | ");
					b.append(s);
					
					newL.add(b.toString());
				}
				
				l = newL;*/
				
				/*byte[] bytes = Files.readAllBytes(p);
				List<String> newL = new ArrayList<>();
				StringBuilder b = new StringBuilder();
				int count = 0;
				int notResetCount = 0;
				
				for (byte by : bytes) {
					if (count >= 4) {
						b.append("| ");
						
						for (int i = 0; i < 4; i++) {
							if (notResetCount + i > bytes.length) break;
							
							b.append((char) bytes[notResetCount + i]);
						}
						
						newL.add(b.toString());
						b = new StringBuilder();
						count = 0;
					}
					
					String bin = Integer.toBinaryString(by & 0xFF).replace(' ', '0');
					
					b.append(bin + " ");
					
					count++;
					notResetCount++;
				}
				
				l = newL;*/
				
				l.clear();
				
				String raw = convertFileToBinary(file.toPath());
				String[] lines = splitByNCharacters(raw, 32);
				
				for (int i = 0; i < lines.length; i++) {
					String[] line = new String[32];
					int index = 0;
					
					for (int j = 0; j < lines[i].length(); j++) {
						char c = lines[i].charAt(j);
						
						line[index] += c;
						
						if (j % 8 == 0 && j != 0) index++;
					}
					
					StringBuilder bl = new StringBuilder();
					
					for (String s : line)
						bl.append(s + " ");
					
					lines[i] = bl.toString();
				}
				
				for (String s : lines) {
					s = s.replace("null", "");
					s = s.substring(0, s.length() - 29);
					
					l.add(s);
				}
				
				break;
				
			case HEX:
				l.clear();
				
				raw = convertFileToHex(file.toPath());
				lines = raw.split("\n");
				
				for (String s : lines) {
					StringBuilder b = new StringBuilder(s);
					b.delete(45, 59);
					
					s = b.toString();
					
					l.add(s);
				}
				
				break;
			case NORMAL:
				break;
				
			default:
				break;
		}

		List<IDELine> ls = new ArrayList<>();
		
		for (String s : l) {
			List<Character> cs = new ArrayList<>();
			List<IDEFont> fs = new ArrayList<>();

			for (char c : s.toCharArray())
				cs.add(c);

			for (int i = 0; i < cs.size(); i++)
				fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));

			IDELine gen = new IDELine(cs, fs);

			ls.add(gen);
		}

		new Thread("automaticColor") { // quando vc deleta as linhas ou fecha as tabs isso (exception) acontece mesmo
			public void run() {
				if (editing != null && editing.getRegent() != null && editing.getRegent().getRegent() != null) {
					int i = 0;
					for (IDELine l : lines) {
						int yr = MIN_Y + (i++ * (LINE_HEIGHT)) - scrY;
						
						if (yr < 0 || yr > Main.screen.getHeight())
							continue;
						
						if (editing != null && editing.closing)
							break;

						l.setFonts(automaticColor(toCharArray(l.getChars()),
								ListableFile.getFileExtension(editing.getRegent().getRegent())));

						if (editing != null && editing.closing)
							break;
					}
					restartVariables();
				}
			}
		}.start();
		
		callAutomaticColor();

		String ext = ListableFile.getFileExtension(file);
		
		if (editing != null && (isBinary(ext) || editing.isReadOnly || !file.canWrite())) {
			isReadOnly = true;
			editing.isReadOnly = true;
		}

		return ls;
	}
	
	public static String convertFileToBinary(Path path) {
		StringBuilder bl = new StringBuilder();
		
		try (InputStream str = Files.newInputStream(path)) {
			int c;
			
			while ((c = str.read()) != -1)
				bl.append(Integer.toBinaryString(c));
			
		} catch (IOException e) {}
		
		return bl.toString();
	}
	
	public static String convertFileToHex(Path path) throws IOException { // Fonte: mkyong.com
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File not found! " + path);
        }

        StringBuilder result = new StringBuilder();
        StringBuilder hex = new StringBuilder();
        StringBuilder input = new StringBuilder();

        int count = 0;
        int value;

        // path to inputstream....
        try (InputStream inputStream = Files.newInputStream(path)) {

            while ((value = inputStream.read()) != -1) {

                hex.append(String.format("%02X ", value));

                //If the character is unable to convert, just prints a dot "."
                if (!Character.isISOControl(value)) {
                    input.append((char) value);
                } else {
                    input.append("."); // unknown
                }

                // After 15 bytes, reset everything for formatting purpose
                if (count == 14) {
                    result.append(String.format("%-60s | %s%n", hex, input));
                    hex.setLength(0);
                    input.setLength(0);
                    count = 0;
                } else {
                    count++;
                }

            }

            // if the count>0, meaning there is remaining content
            if (count > 0) {
                result.append(String.format("%-60s | %s%n", hex, input));
            }

        }

        return result.toString();
    }

	public static boolean isNumber(char c) {
		return c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9'
				|| c == '0';
	}
	
	public static <T> T[] removeDuplicates(T[] arr) { // talvez remover duplicadas de qqr array
		Set<T> set = new LinkedHashSet<>();
		
		for (T t : arr)
			set.add(t);
		
		List<T> list = new ArrayList<>();
		list.addAll(set);
		
		int i = 0;
		for (T t : list)
			arr[i++] = t;
		
		return arr;
	}
	
	public static void copyText(String str) {
		StringSelection sel = new StringSelection(str);
		Clipboard clip = Main.toolkit.getSystemClipboard();
		
		clip.setContents(sel, sel);
	}
	
	public static int getNumberKey(int keyCode) {
		return keyCode - 48;
	}
	
	public static boolean isNumber(int keyCode) {
		return keyCode >= 48 && keyCode <= 57;
	}

	public static String[] mergeStringArrays(String[] arr1, String[] arr2) {
		String[] res = new String[arr1.length + arr2.length];

		System.arraycopy(arr1, 0, res, 0, arr1.length);
		System.arraycopy(arr2, 0, res, arr1.length, arr2.length);

		return removeDuplicates(res);
	}

	public boolean autoCompletesEqual() {
		for (AutoComplete c : autocomplete) {
			for (AutoComplete d : autocomplete) {
				if (c.text.equals(d.text))
					return true;
			}
		}

		return false;
	}

	// Se for usar em arquivos que n�o t�m extens�o, use o m�todo debaixo desse, o
	// getKeywordsSpecial().
	public static String[] getKeywords(String ext) {
		switch (ext.toLowerCase()) {
		case ".java": return javaKeys;
		case ".c": return cKeys;
		case ".cpp": return cppKeys;
		case ".cc": return cppKeys;
		case ".hpp": return cppKeys;
		case ".cxx": return cppKeys;
		case ".hxx": return cppKeys;
		case ".h": return cppKeys;
		case ".hh": return cppKeys;
		case ".vb": return vbKeys;
		case ".cs": return csKeys;
		case ".ipynb": return pyKeys;
		case ".py": return pyKeys;
		case ".pyx": return pyKeys;
		case ".pyd": return pyKeys;
		case ".js": return jsKeys;
		case ".mjs": return jsKeys;
		case ".bat": return batCom;
		case ".cmd": return batCom;
		case ".com": return batCom;
		case ".asm": return asmKeys;
		case ".s": return asmKeys;
		case ".lua": return luaKeys;
		case ".sql": return sqlKeys;
		case ".swift": return swKeys;
		case ".rs": return rsKeys;
		case ".php": return mergeStringArrays(phpKeys, cssTags);
		case ".kt": return ktKeys;
		case ".vue": return jsKeys;
		case ".rb": return rbKeys;
		case ".ino": return cppKeys;
		case ".ts": return tsKeys;
		case ".tsx": return tsKeys;
		case ".go": return goKeys;
		case ".r": return rKeys;
		case ".jl": return jlKeys;
		case ".pl": return plKeys;
		case ".t": return plKeys;
		case ".has": return hasKeys;
		case ".hs": return hasKeys;
		case ".fs": return fsKeys;
		case ".coffee": return cfKeys;
		case ".m": return objKeys;
		case ".mm": return objKeys;
		case ".pas": return pasKeys;
		case ".lpr": return pasKeys;
		case ".pp": return pasKeys;
		case ".scala": return scaKeys;
		case ".dart": return dartKeys;
		case ".zig": return zigKeys;
		case ".gd": return gdKeys;
		case ".mcfunction": return mcKeys;
		case ".por": return porKeys;
		case ".cmxa": return oCamlKeys;
		case ".ml": return oCamlKeys;
		case ".mli": return oCamlKeys;
		case ".mly": return oCamlKeys;
		case ".clt": return oCamlKeys;
		case ".vbs": return vbKeys;
		case ".bas": return basKeys;
		
		case ".html": return mergeStringArrays(cssTags, mergeStringArrays(props, mergeStringArrays(jsKeys, phpKeys)));
		case ".svelte": return mergeStringArrays(cssTags, mergeStringArrays(props, mergeStringArrays(jsKeys, phpKeys)));
		case ".xhtml": return mergeStringArrays(cssTags, mergeStringArrays(props, mergeStringArrays(jsKeys, phpKeys))); // ser� que tira o phpkeys? TODO
		case ".htm": return mergeStringArrays(cssTags, mergeStringArrays(props, mergeStringArrays(jsKeys, phpKeys)));
		case ".css": return mergeStringArrays(cssTags, props);
		case ".scss": return mergeStringArrays(cssTags, props);
		case ".json": return jsonKeys;
		case ".jsonc": return jsonKeys;
		case ".conf": return ideConfKeys;
		case ".mk": return mergeStringArrays(makeKeys, shKeys);
		case ".mak": return mergeStringArrays(makeKeys, shKeys);
		case ".make": return mergeStringArrays(makeKeys, shKeys);
		case ".makefile": return mergeStringArrays(makeKeys, shKeys);
		case ".dockerfile": return dkKeys;
		case ".jsx": return jsKeys;
		case ".ps1": return batCom;
		case ".sh": return shKeys;
		case ".ejs": return cssTags;
		case ".ld": return ldKeys;
		case ".bashrc": return shKeys;
		case ".bash_profile": return shKeys;
		case ".tf": return tfKeys;
		case ".v": return vKeys;
		case ".vh": return vKeys;
		case ".vsh": return vKeys;
		case ".mod": return vKeys;
		
		default: return null;
		}
	}

	public static String[] getKeywordsSpecial(String filename) {
		switch (filename.toLowerCase()) {
		case "makefile": return mergeStringArrays(makeKeys, shKeys);
		case "dockerfile": return dkKeys;

		default: return null;
		}
	}

	public static boolean isBinary(String ext) {
		return ext.equalsIgnoreCase(".pdf") || ext.equalsIgnoreCase(".jar") || ext.equalsIgnoreCase(".o")
				|| ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".obj") || ext.equalsIgnoreCase(".iso")
				|| ext.equalsIgnoreCase(".img") || ext.equalsIgnoreCase(".flp") || ext.equalsIgnoreCase(".class")
				|| ext.equalsIgnoreCase(".exe") || ext.equalsIgnoreCase(".urna") || ext.equalsIgnoreCase(".save")
				|| ext.equalsIgnoreCase(".docx") || ext.equalsIgnoreCase(".pptx") || ext.equalsIgnoreCase(".one")
				|| ext.equalsIgnoreCase(".psd") || ext.equalsIgnoreCase(".aed") || ext.equalsIgnoreCase(".ai")
				|| ext.equalsIgnoreCase(".indd") || ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".dll")
				|| ext.equalsIgnoreCase(".png") || ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".jpeg")
				|| ext.equalsIgnoreCase(".gif") || ext.equalsIgnoreCase(".bmp") || ext.equalsIgnoreCase(".ico")
				|| ext.equalsIgnoreCase(".webp") || ext.equalsIgnoreCase(".mp4") || ext.equalsIgnoreCase(".wmv")
				|| ext.equalsIgnoreCase(".avi") || ext.equalsIgnoreCase(".wav") || ext.equalsIgnoreCase(".mp3")
				|| ext.equalsIgnoreCase(".ogg") || ext.equalsIgnoreCase(".otf") || ext.equalsIgnoreCase(".ttf")
				|| ext.equalsIgnoreCase(".woff") || ext.equalsIgnoreCase(".woff2") || ext.equalsIgnoreCase(".zip")
				|| ext.equalsIgnoreCase(".rar") || ext.equalsIgnoreCase(".7z") || ext.equalsIgnoreCase(".bin")
				|| ext.equalsIgnoreCase(".gz") || ext.equalsIgnoreCase(".rtf");
	}

	public static boolean isFormatSupported(String format) {
		for (FileType f : ListableFile.types) {
			if (f.getExtension().equals(format))
				return true;
		}

		return false;
	}

	public static BufferedImage getAutoCompleteIcon(AutoCompleteType type) {
		switch (type) {
		//case FUNCTION: return functions;
		//case OBJECT: return objects;
		case KEYWORD: return keywords;
		//case VARIABLE: return variables;
		
		default: return keywords;
		}
	}

	public void addautocomplete(List<String> list, AutoCompleteType type) {
		for (String s : list)
			addautocomplete.add(new AutoComplete(s, type));
	}

	public static List<Integer> findWord(String textString, String word) { // Fonte: baeldung.com
		List<Integer> indexes = new ArrayList<Integer>();

		String lowerCaseTextString = textString;// .toLowerCase(); // n�o vai ter lowercase,
		String lowerCaseWord = word;// .toLowerCase(); //tem q ter diferen�a de letras capitais

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

	public boolean isInside(int index, char charLeft, char charRight, String s) {
		/*List<Integer> indxs = findWord(s, Character.toString(ch));

		boolean foundt = false; // tras
		boolean foundf = false; // frente

		for (Integer i : indxs) {
			int ct = i;
			int cf = i;

			while (ct > 0) {
				ct--;

				if (s.charAt(ct) == ch) {
					foundt = true;

					break;
				}
			}

			while (cf < s.length()) {
				cf++;

				if (cf < s.length() && s.charAt(cf) == ch) {
					foundf = true;

					break;
				}
			}
		}

		return foundt && foundf;*/
		
		return howManyBefore(s, index, charLeft) % 2 != 0 && howManyAfter(s, index, charRight) % 2 != 0;
	}
	
	public int countIndexDistance(int i1, int i2, int l1, int l2) {
		try {
			if (l1 == l2)
				return i2 - i1;
			else {
				int count = 0;
				for (int i = l1 - 1; i < l2; i++) {
					if (i == l1 - 1) {
						count += lines.get(i).getChars().size() - i1;
						continue;
					}
	
					else if (i == l2 - 1) {
						count += i2;
						continue;
					} else
						count += lines.get(i).getChars().size();
				}
	
				return count;
			}
		} catch (ArrayIndexOutOfBoundsException e) { return 0; }
	}

	public static String getLowerBarFileName(String ext) {
		// casos espec�ficos
		
		if (Main.editor.editing.getRegent().getRegent().getName().equalsIgnoreCase("readme.md"))
			return Main.lang == Language.PORT ? "Leia-Me (Markdown)" : "Read Me (Markdown)";
		
		switch (ext.toLowerCase()) {
		case ".java": return "Java";
		case ".class": return minMode ? "Class" :  (Main.lang == Language.PORT ? "Arquivo Bytecode do Java" : "Java Bytecode File");
		case ".c": return "C";
		case ".cpp": return "C++";
		case ".cc": return "C++";
		case ".cxx": return "C++";
		case ".cs": return "C#";
		case ".py": return "Python";
		case ".pyx": return "Python";
		case ".pyd": return "Python";
		case ".js": return minMode ? "JS" : "JavaScript";
		case ".mjs": return minMode ? "JS" : "JavaScript";
		case ".bat": return minMode ? "Bat" : "Batch";
		case ".com": return minMode ? "Com" : (Main.lang == Language.PORT ? "Arquivo do Prompt de Comando" : "Command Prompt File");
		case ".cmd": return minMode ? "Cmd" : (Main.lang == Language.PORT ? "Arquivo do Prompt de Comando" : "Command Prompt File");
		case ".h": return minMode ? "H" : "C/C++ Header";
		case ".hh": return minMode ? "HH" : "C++ Header";
		case ".hxx": return minMode ? "Hxx" : "C++ Header";
		case ".hpp": return minMode ? "Hpp" : "C++ Header";
		case ".asm": return minMode ? "Asm" : "Assembly";
		case ".s": return minMode ? "Asm" : "Assembly";
		case ".lua": return "Lua";
		case ".sql": return minMode ? "SQL" : "Structured Query Language - SQL";
		case ".swift": return "Swift";
		case ".rs": return "Rust";
		case ".php": return minMode ? "PHP" : "Hyper Text Preprocessor - PHP";
		case ".kt": return minMode ? "Kt" : "Kotlin";
		case ".vue": return minMode ? "Vue" : "Vue.js";
		case ".rb": return "Ruby";
		case ".ino": return "Arduino";
		case ".ts": return minMode ? "TS" : "TypeScript";
		case ".tsx": return minMode ? "TSX" : "TypeScript React";
		case ".go": return "Go";
		case ".r": return "R";
		case ".jl": return "Julia";
		case ".pl": return "Perl";
		case ".t": return "Perl";
		case ".has": return "Haskell";
		case ".hs": return "Haskell";
		case ".fs": return "F#";
		case ".coffee": return "CoffeeScript";
		case ".m": return minMode ? "Obj-C" : "Objective-C";
		case ".mm": return minMode ? "Obj-C++" : "Objective-C++";
		case ".pas": return "Pascal";
		case ".lpr": return "Pascal";
		case ".pp": return "Pascal";
		case ".scala": return "Scala";
		case ".dart": return "Dart";
		case ".zig": return "Zig";
		case ".scss": return minMode ? "SCSS" : "Synctactically Awesome Style Sheets - SCSS";
		case ".ipynb": return "Jupyter Notebook";
		case ".vb": return "Visual Basic";
		case ".bf": return "Brainfuck";
		case ".gd": return "GDScript";
		case ".mcfunction": return minMode ? "MC Function" : "Minecraft Function";
		case ".por": return "Portugol";
		case ".cmxa": return minMode ? "OCaml" : "Objective Caml - OCaml";
		case ".ml": return minMode ? "OCaml" : "Objective Caml - OCaml";
		case ".mli": return minMode ? "OCaml" : "Objective Caml - OCaml";
		case ".mly": return minMode ? "OCaml" : "Objective Caml - OCaml";
		case ".clt": return minMode ? "OCaml" : "Objective Caml - OCaml";
		case ".vbs": return minMode ? "VBScript" : "Visual Basic Script - VBScript";
		case ".v": return "V";
		case ".vh": return "V";
		case ".vsh": return "V";
		case ".mod": return "V";
		case ".bas": return minMode ? "BASIC" : "Beginners' All-Purpose Symbolic Instruction Code - BASIC";
		
		case ".html": return minMode ? "HTML" : "Hyper Text Markup Language - HTML";
		case ".xhtml": return minMode ? "HTML" : "Hyper Text Markup Language - HTML";
		case ".htm": return minMode ? "HTML" : "Hyper Text Markup Language - HTML";
		case ".svelte": return "Svelte";
		case ".css": return minMode ? "CSS" : "Cascading Style Sheets - CSS";
		case ".xml": return minMode ? "XML" : "Extensible Markup Language - XML";
		case ".sln": return minMode ? (Main.lang == Language.PORT ? "Solu��o do VS" : "VS Solution") : (Main.lang == Language.PORT ? "Solu��o do Microsoft Visual Studio"
				: "Microsoft Visual Studio Solution");
		case ".json": return minMode ? "JSON" : "JavaScript Object Notation - JSON";
		case ".jsonc": return minMode ? "JSONC" : "JavaScript Object Notation with Comments - JSONC";
		case ".md": return "Markdown";
		case ".markdown": return "Markdown";
		case ".txt": return minMode ? (Main.lang == Language.PORT ? "Texto" : "Text") : (Main.lang == Language.PORT ? "Arquivo de Texto" : "Text File");
		case ".log": return minMode ? "Log" : (Main.lang == Language.PORT ? "Arquivo de Log" : "Log File");
		case ".pdf": return minMode ? "PDF" : "Portable Document Format - PDF";
		case ".jar": return minMode ? "Jar" : (Main.lang == Language.PORT ? "Arquivo Jar" : "Jar File");
		case ".exe": return minMode ? "EXE" : (Main.lang == Language.PORT ? "Execut�vel do Windows - EXE" : "Windows Executable - EXE");
		case ".classpath": return (Main.lang == Language.PORT ? "Caminho da Classe" : "Class Path");
		case ".csproj": return (Main.lang == Language.PORT ? "Projeto C# do Visual Studio" : "Visual Studio C# Project");
		case ".project": return (Main.lang == Language.PORT ? "Arquivo de Projeto" : "Project File");
		case ".svg": return minMode ? "SVG" : "Scalable Vector Graphics - SVG";
		case ".urna": return (Main.lang == Language.PORT ? "Urna Salva do Criador de Urnas"
				: "Saved Bollot Box from Criador de Urnas");
		case ".save": return (Main.lang == Language.PORT ? "Jogo Salvo do World's Hardest Game Maker 2" // fazer desses tbm
				: "Saved Game from World's Hardest Game Maker 2");
		case ".conf": return (Main.lang == Language.PORT ? "Arquivo de Configura��es da Boot IDE"
				: "Boot IDE Configuration File");
		case Main.SETTINGS_FILE_EXTENSION: return (Main.lang == Language.PORT ? "Arquivo de Configura��es da Boot IDE"
				: "Boot IDE Configuration File");
		case ".rtf": return "Rich Text Format";
		case ".mk": return "Makefile";
		case ".mak": return "Makefile";
		case ".make": return "Makefile";
		case ".sh": return minMode ? "Bash" : "Bourne-Again Shell - Bash";
		case ".gitignore": return "Git Ignore";
		case ".dockerfile": return "Dockerfile";
		case ".jsx": return minMode ? "JSX" : "JavaScript React";
		case ".config": return (Main.lang == Language.PORT ? "Arquivo de Configura��es" : "Configuration File");
		case ".cfg": return (Main.lang == Language.PORT ? "Arquivo de Configura��es" : "Configuration File");
		case ".ps1": return (Main.lang == Language.PORT ? "Arquivo do PowerShell" : "PowerShell File");
		case ".license": return (Main.lang == Language.PORT ? "Arquivo de Licen�a" : "License File");
		case ".docx": return (Main.lang == Language.PORT ? "Documento do Microsoft Word" : "Microsoft Word Document");
		case ".pptx": return (Main.lang == Language.PORT ? "Apresenta��o do Microsoft PowerPoint"
				: "Microsoft PowerPoint Presentation");
		case ".xlsx": return (Main.lang == Language.PORT ? "Planilha do Microsoft Excel" : "Microsoft Excel Spreadsheet");
		case ".one": return (Main.lang == Language.PORT ? "Arquivo do Microsoft OneNote" : "Microsoft OneNote File");
		case ".psd": return (Main.lang == Language.PORT ? "Arquivo do Adobe Photoshop" : "Adobe Photoshop File");
		case ".aed": return (Main.lang == Language.PORT ? "Arquivo do Adobe After Effects" : "Adobe After Effects File");
		case ".ai": return (Main.lang == Language.PORT ? "Arquivo do Adobe Illustrator" : "Adobe Illustrator File");
		case ".indd": return (Main.lang == Language.PORT ? "Arquivo do Adobe InDesign" : "Adobe InDesign File");
		case ".ejs": return "Embedded JavaScript - EJS";
		case ".ld": return "LinkerScript";
		case ".lock": return "Lock";
		case ".ini": return (Main.lang == Language.PORT ? "Arquivo de Par�metros de Configura��es"
				: "Configuration Parameters File");
		case ".dll": return minMode ? "DLL" : "Dynamic Link Library - DLL";
		case ".makefile": return "Makefile";
		case ".url": return minMode ? "URL" : "Uniform Resource Locator - URL";
		case ".prefs": return (Main.lang == Language.PORT ? "Arquivo de Prefer�ncias" : "Preferences File");
		case ".bashrc": return minMode ? "Bashrc" : (Main.lang == Language.PORT ? "Arquivo de Configura��es Bash" : "Bash Configuration File");
		case ".bash_profile": return (Main.lang == Language.PORT ? "Perfil Bash" : "Bash Profile");
		case ".toml": return minMode ? "Toml" : (Main.lang == Language.PORT ? "Arquivo de Configura��es do Rust" : "Rust Configuration File");
		case ".tf": return "Terraform";
		
		case ".png": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");
		case ".jpg": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");
		case ".jpeg": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");
		case ".gif": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");
		case ".bmp": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");
		case ".ico": return (Main.lang == Language.PORT ? "Arquivo de �cone" : "Icon File");
		case ".webp": return (Main.lang == Language.PORT ? "Arquivo de Imagem" : "Image File");

		case ".mp4": return (Main.lang == Language.PORT ? "Arquivo de V�deo" : "Video File");
		case ".wmv": return (Main.lang == Language.PORT ? "Arquivo de V�deo" : "Video File");
		case ".avi": return (Main.lang == Language.PORT ? "Arquivo de V�deo" : "Video File");

		case ".wav": return (Main.lang == Language.PORT ? "Arquivo de �udio" : "Audio File");
		case ".mp3": return (Main.lang == Language.PORT ? "Arquivo de �udio" : "Audio File");
		case ".ogg": return (Main.lang == Language.PORT ? "Arquivo de �udio" : "Audio File");

		case ".otf": return (Main.lang == Language.PORT ? "Arquivo de Fonte" : "Font File");
		case ".ttf": return (Main.lang == Language.PORT ? "Arquivo de Fonte" : "Font File");
		case ".woff": return (Main.lang == Language.PORT ? "Arquivo de Fonte" : "Font File");
		case ".woff2": return (Main.lang == Language.PORT ? "Arquivo de Fonte" : "Font File");

		case ".zip": return (Main.lang == Language.PORT ? "Arquivo Compactado" : "Zipped File");
		case ".gz": return (Main.lang == Language.PORT ? "Arquivo Compactado" : "Zipped File");
		case ".rar": return (Main.lang == Language.PORT ? "Arquivo Compactado" : "Zipped File");
		case ".7z": return (Main.lang == Language.PORT ? "Arquivo Compactado" : "Zipped File");

		case ".bin": return (Main.lang == Language.PORT ? "Arquivo Bin�rio" : "Binary File");
		case ".img": return (Main.lang == Language.PORT ? "Arquivo de Imagem de Disco" : "Disc Image File");
		case ".iso": return (Main.lang == Language.PORT ? "Arquivo de Imagem de Disco" : "Disc Image File");
		case ".flp": return (Main.lang == Language.PORT ? "Arquivo de Disquete" : "Floppy Disk File");
		case ".o": return (Main.lang == Language.PORT ? "Arquivo de Objeto" : "Object File");
		case ".out": return (Main.lang == Language.PORT ? "Arquivo de Sa�da" : "Output File");
		case ".obj": return (Main.lang == Language.PORT ? "Arquivo de Objeto" : "Object File");

		default: return capitalizeFirstLetter(
				ListableFile.getFileExtension(Main.editor.editing.getRegent().getRegent().getName()).substring(1).toLowerCase());
		}
	}

	public static String getLowerBarFileNameWithoutExtension(String filename) {
		switch (filename.toLowerCase()) {
		case "makefile": return  "Makefile";
		case "dockerfile": return  "Dockerfile";
		case "license": return  (Main.lang == Language.PORT ? "Arquivo de Licen�a" : "License File");
		case "authors": return  (Main.lang == Language.PORT ? "Nomes dos Autores" : "Authors' Names");
		case "gitignore": return  "Git Ignore";

		default: return  (Main.lang == Language.PORT ? "Sem Extens�o" : "No Extension");
		}
	}

	public static List<IDEFont> color(int s, int e, IDEFont color, List<IDEFont> fs) {
		if (e < s)
			e = s;// throw new IllegalArgumentException("o start n�o pode ser maior que o
					// final!");
		if (e > fs.size())
			e = fs.size();// throw new IndexOutOfBoundsException("o final n�o pode ser maior que o final
							// da fonte!");
		
		if (s < 0) s = 0;
		if (e < 0) e = 0;

		for (int i = s; i < e; i++) {
			if (i >= fs.size())
				break;
			fs.set(i, color);
		}

		return fs;
	}

	public static boolean isCharsEqual(char c1, char c2) {
		String str1 = String.valueOf(c1);
		String str2 = String.valueOf(c2);

		return str1.equals(str2);
	}

	public static <T> T[] clearArray(T[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = null;
		}

		return arr;
	}

	public static char[] sliceCharArray(int s, int e, char[] array) {
		if (e < s)
			throw new RuntimeException("O index final n�o pode ser menor que o inicial!");
		
		return Arrays.copyOfRange(array, s, e);
	}

	public List<IDEFont> colorVariablesAndObjects(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".txt")
				|| ext.equalsIgnoreCase(".log") || ext.equalsIgnoreCase(".obj") || ext.equalsIgnoreCase(".bf")
				|| ext.equalsIgnoreCase(".conf")
				|| ext.equalsIgnoreCase(".gitignore")
				|| editing.getRegent().getRegent().getName().equalsIgnoreCase("gitignore") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc")
				|| ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".ps1"))
			return fs;

		if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
				|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
				|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
				|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
				|| ext.equalsIgnoreCase(".project")) {
			indxs = findWord(new String(chars), "<");
			List<Integer> finals = findWord(new String(chars), ">");

			for (int i = 0; i < indxs.size(); i++) {
				try {
					fs = color(indxs.get(i), finals.get(i), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				} catch (Exception e) {
					continue;
				}
			}
			
			for (Integer i : indxs) {
				int len = 0;
				
				i++;
				
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '<' && chars[i + len] != '>') {
					len++;
				}
				
				try {
					fs = color(i - 1, i + 1, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
					fs = color(i, i + len, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				} catch (Exception e) {
					continue;
				}
			}
			/*
			indxs = findWord(new String(chars), "="); // antes de <palavra>

			for (Integer i : indxs) {
				int c = i;
				int len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0) {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (hasSpace)
							break;

						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}
			*/
		}
		
		if (ext.equalsIgnoreCase(".por")) {
			for (String s : porKeys) {
				indxs = findWord(new String(chars), s); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length)
						len++;

					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
		}
		
		if (ext.equalsIgnoreCase(".go")) {
			for (String s : porKeys) {
				indxs = findWord(new String(chars), s); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length)
						len++;

					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
		}
		
		if (ext.equalsIgnoreCase(".bas")) {
			for (String s : basKeys) {
				indxs = findWord(new String(chars), s); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length)
						len++;

					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
		}
		
		if (ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s")) {
			for (String s : asmKeys) {
				indxs = findWord(new String(chars), s); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length)
						len++;

					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
		}
		
		if (ext.equalsIgnoreCase(".mk") || ext.equalsIgnoreCase(".make") || ext.equalsIgnoreCase(".makefile")) {
			indxs = findWord(new String(chars), "="); // antes de <palavra>

			for (Integer i : indxs) {
				int c = i;
				int len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0) {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (hasSpace)
							break;

						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}
		}
		
		if (ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s")) {
			for (String s : asmKeys) {
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i != 0)
						continue;
	
					fs = color(i, fs.size(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
		}
		
		if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")) {
			indxs = findWord(new String(chars), "["); // depois de <palavra>

			int len = 0;
			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ']')
					len++;

				fs = color(i, i + len + 1, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ">");
			
			for (Integer i : indxs) {
				if (i != 0)
					continue;

				fs = color(i, fs.size(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), "**");

			for (int i = 0; i < indxs.size() - 1; i += 2)
				fs = color(indxs.get(i), indxs.get(i + 1) + 2, new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "__");

			for (int i = 0; i < indxs.size() - 1; i += 2)
				fs = color(indxs.get(i), indxs.get(i + 1) + 2, new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
			
			/*String s = "/>";
			
			indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
			// s.length()).getFont().equals(Fonts.methodsEditor))

			for (Integer i : indxs) {
				if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
						|| ((i + s.length() < chars.length)
								&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()])))) // ta como keyword, mas se for coloca s�mbolo
					continue;

				fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
			}*/
		}
		
		if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")) return fs;
		
		if (isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent()))) {
			if (ext.equalsIgnoreCase(".prefs")) { // || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") ||
													// ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".com") ||
													// ext.equalsIgnoreCase(".ps1") // talvez colocar
				indxs = findWord(new String(chars), "="); // antes de <palavra>

				for (Integer i : indxs) {
					int c = i;
					int len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0) {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "="); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length)
						len++;

					fs = color(i, i + len, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}

				return fs;
			}
			
			if (ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".mjs")
				|| ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".tsx") || ext.equalsIgnoreCase(".lua")) {
				
				indxs = findWord(new String(chars), "return"); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
			
			if (ext.equalsIgnoreCase(".go")) {
					indxs = findWord(new String(chars), "return"); // depois de <palavra>

					int len = 0;

					for (Integer i : indxs) {
						len = 0;

						while (i + len < chars.length)
							len++;

						// if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
					
					///
					
					indxs = findWord(new String(chars), "package"); // depois de <palavra>

					len = 0;

					for (Integer i : indxs) {
						len = 0;

						while (i + len < chars.length)
							len++;

						// if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
					
					///
					
					indxs = findWord(new String(chars), "var"); // depois de <palavra>

					len = 0;

					for (Integer i : indxs) {
						len = 0;

						while (i + len < chars.length)
							len++;

						// if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
				}

			if (ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".pyx")
					|| ext.equalsIgnoreCase(".ipynb")) {
				indxs = findWord(new String(chars), "f\""); // antes de <palavra>

				for (Integer i : indxs) {
					int c = i;
					int len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0) {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					//addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len, chars)), AutoCompleteType.VARIABLE));
					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "f'"); // antes de <palavra>

				for (Integer i : indxs) {
					int c = i;
					int len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0) {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					// addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len,
					// chars)), AutoCompleteType.VARIABLE));
					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "f`"); // antes de <palavra>

				for (Integer i : indxs) {
					int c = i;
					int len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0) {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					// addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len,
					// chars)), AutoCompleteType.VARIABLE));
					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "import"); // depois de <palavra>

				int len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "return"); // depois de <palavra>

				len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "global"); // depois de <palavra>

				len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "from"); // depois de <palavra>

				len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}

			if (!(ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
					|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
					|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
					|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
					|| ext.equalsIgnoreCase(".project"))) {
				for (String s : syms) {
					indxs = findWord(new String(chars), s); // antes de <palavra>

					for (Integer i : indxs) {
						int c = i;
						int len = 0;

						boolean foundLetter = false;
						boolean hasSpace = false;

						while (c < chars.length && c + len < chars.length && c > 0) {
							c--;
							len++;

							if (Character.isLetter(chars[c]))
								foundLetter = true; // arrumar

							if (chars[c] == ' ' && !foundLetter) {
								if (hasSpace)
									break;

								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}

						// addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len,
						// chars)), AutoCompleteType.VARIABLE));
						fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}

					indxs = findWord(new String(chars), s); // depois de <palavra>

					int len = 0;

					for (Integer i : indxs) {
						len = 0;

						while (i + len < chars.length)
							len++;

						// if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
				}

				if (ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyx") || ext.equalsIgnoreCase(".pyd")) { // TODO
																													// fazer
																													// para
																													// outras
																													// linguagens
																													// tbm
					indxs = findWord(new String(chars), "in"); // antes de <palavra>

					for (Integer i : indxs) {
						int c = i;
						int len = 0;

						boolean hasSpace = false;

						while (c < chars.length && c + len < chars.length && c > 0) {
							c--;
							len++;

							if (chars[c] == ' ') {
								if (hasSpace)
									break;

								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}

						// addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len,
						// chars)), AutoCompleteType.VARIABLE));
						fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
				}

				if (ext.equalsIgnoreCase(".vb")) {
					indxs = findWord(new String(chars), "As"); // antes de <palavra>

					for (Integer i : indxs) {
						int c = i;
						int len = 0;

						boolean hasSpace = false;

						while (c < chars.length && c + len < chars.length && c > 0) {
							c--;
							len++;

							if (chars[c] == ' ') {
								if (hasSpace)
									break;

								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}

						// addautocomplete.add(new AutoComplete(new String(sliceCharArray(c, c + len,
						// chars)), AutoCompleteType.VARIABLE));
						fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
					}
				}
			}
		}

		return fs;
	}

	public List<IDEFont> colorObjects(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if (isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent()))) {
			if (!(ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
					|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
					|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
					|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
					|| ext.equalsIgnoreCase(".project")
					|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")
					|| ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc") || ext.equalsIgnoreCase(".com")
					|| ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".lock") || ext.equalsIgnoreCase(".toml"))) {
				for (String s : cll) {
					indxs = findWord(new String(chars), s);
	
					int len = 0;
	
					String str = new String(chars);
	
					for (Integer i : indxs) {
						if (i > 0 && isNumber(chars[i - 1]))
							continue;
	
						if (i - 1 > 0 && (str.charAt(i - 1) == 'a' || str.charAt(i - 1) == 'b' || str.charAt(i - 1) == 'c'
								|| str.charAt(i - 1) == 'd' || str.charAt(i - 1) == 'e' || str.charAt(i - 1) == 'f'
								|| str.charAt(i - 1) == 'g' || str.charAt(i - 1) == 'h' || str.charAt(i - 1) == 'i'
								|| str.charAt(i - 1) == 'j' || str.charAt(i - 1) == 'k' || str.charAt(i - 1) == 'l'
								|| str.charAt(i - 1) == 'm' || str.charAt(i - 1) == 'n' || str.charAt(i - 1) == 'o'
								|| str.charAt(i - 1) == 'p' || str.charAt(i - 1) == 'q' || str.charAt(i - 1) == 'r'
								|| str.charAt(i - 1) == 's' || str.charAt(i - 1) == 't' || str.charAt(i - 1) == 'u'
								|| str.charAt(i - 1) == 'v' || str.charAt(i - 1) == 'w' || str.charAt(i - 1) == 'x'
								|| str.charAt(i - 1) == 'y' || str.charAt(i - 1) == 'z'))
							continue;
	
						while (i + len < chars.length && !isCharsEqual(chars[i + len], ' ')
								&& !isCharsEqual(chars[i + len], '[') && !isCharsEqual(chars[i + len], ']')
								&& !isCharsEqual(chars[i + len], '(') && !isCharsEqual(chars[i + len], ')')
								&& !isCharsEqual(chars[i + len], '{') && !isCharsEqual(chars[i + len], '}')
								&& !isCharsEqual(chars[i + len], '<') && !isCharsEqual(chars[i + len], '>')
								&& !isCharsEqual(chars[i + len], ',') && !isCharsEqual(chars[i + len], ';')
								&& !isCharsEqual(chars[i + len], '.') && !isCharsEqual(chars[i + len], ':')
								&& !isCharsEqual(chars[i + len], '=') && !isCharsEqual(chars[i + len], '\"')
								&& !isCharsEqual(chars[i + len], '\'')) {
							len++;
						}
						
						//if (isInside(i, '>', '<', str)) continue; // n�o vai funcionar
	
						// if (i + len < chars.length) {
						if (ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") || ext.equalsIgnoreCase(".ld")
								|| ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss")
								|| ext.equalsIgnoreCase(".sql") || ext.equalsIgnoreCase(".makefile")
								|| ext.equalsIgnoreCase(".mk") || ext.equalsIgnoreCase(".mak")
								|| ext.equalsIgnoreCase(".make")
								|| editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile")
								|| ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com")
								|| ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1")
								|| ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc") || ext.equalsIgnoreCase(".project")
								|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
								|| ext.equalsIgnoreCase(".svg") || ext.equalsIgnoreCase(".xml")
								|| ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss") || ext.equalsIgnoreCase(".json")
								|| ext.equalsIgnoreCase(".jsonc") || ext.equalsIgnoreCase(".mcfunction"))
							fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						else {
							if (i - 1 > 0 && Character.isLetter(chars[i - 1]))
								continue;
							
							//addautocomplete.add(new AutoComplete(new String(sliceCharArray(i, i + len, chars)), AutoCompleteType.OBJECT));
							fs = color(i, i + len, new IDEFont(Fonts.objectsEditor, FONT_SIZE), fs);
						}
					}
				}
			}
		}
		return fs;
	}

	public List<IDEFont> colorKeywords(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		if (ext.equalsIgnoreCase(".bf"))
			return fs;

		List<Integer> indxs = new ArrayList<>();

		switch (ext.toLowerCase()) {
		case ".java":
			for (String s : javaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
														// s.length()).getFont().equals(Fonts.methodsEditor))

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".vb":
			for (String s : vbKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
														// s.length()).getFont().equals(Fonts.methodsEditor))

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".bas":
			for (String s : basKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
														// s.length()).getFont().equals(Fonts.methodsEditor))

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".v":
		case ".vh":
		case ".vsh":
		case ".mod":
			for (String s : vKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
														// s.length()).getFont().equals(Fonts.methodsEditor))

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".vbs":
			for (String s : vbKeys) { // colorir keywords
				indxs = findWord(new String(chars), s); // !(lines.get(getLineIndex(chars)).getFonts().get(i +
														// s.length()).getFont().equals(Fonts.methodsEditor))

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".por":
			for (String s : porKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".tf":
			for (String s : tfKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;
			
		case ".cmxa":
		case ".ml":
		case ".mli":
		case ".mly":
		case ".clt":
			for (String s : oCamlKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".ejs":
		case ".cfg":
		case ".config":
		case ".xml":
		case ".sln":
		case ".svg":
		case ".classpath":
		case ".csproj":
		case ".project":
		case ".htm":
		case ".xhtml":
		case ".html":
		case ".svelte":
			if (isCssPart || isJSPart || isPhpPart)
				for (String s : nums) { // colorir n�meros
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
				}

			indxs = findWord(new String(chars), "0x");

			int len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']'
						&& chars[i + len] != '(' && chars[i + len] != ')' && chars[i + len] != ','
						&& chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
			}

		/*
		 * if (ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg") ||
		 * ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") ||
		 * ext.equalsIgnoreCase(".cfg") || ext.equalsIgnoreCase(".classpath") ||
		 * ext.equalsIgnoreCase(".csproj") || ext.equalsIgnoreCase(".project"))
		 */  // era aqui

			indxs = findWord(new String(chars), "="); // html

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
						&& chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.'
						&& chars[c] != ':') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "<style");

			if (indxs.size() > 0)
				isCssPart = true;

			indxs = findWord(new String(chars), "<script");

			if (indxs.size() > 0)
				isJSPart = true;

			indxs = findWord(new String(chars), "<?php");

			if (indxs.size() > 0)
				isPhpPart = true;
			
			indxs = findWord(new String(chars), "{%");

			if (indxs.size() > 0)
				isPhpPart = true;

			if (isPhpPart) {
				for (String s : phpKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
					}
				}
			}

			if (isJSPart) {
				for (String s : cll) {
					indxs = findWord(new String(chars), s); // se colocar const abelha = () => { return a }; o 'a' do return a n�o colore como vari�vel em html

					len = 0;

					String str = new String(chars);

					for (Integer i : indxs) {
						if (i > 0 && isNumber(chars[i - 1]))
							continue;

						if (i - 1 > 0 && (str.charAt(i - 1) == 'a' || str.charAt(i - 1) == 'b' || str.charAt(i - 1) == 'c'
								|| str.charAt(i - 1) == 'd' || str.charAt(i - 1) == 'e' || str.charAt(i - 1) == 'f'
								|| str.charAt(i - 1) == 'g' || str.charAt(i - 1) == 'h' || str.charAt(i - 1) == 'i'
								|| str.charAt(i - 1) == 'j' || str.charAt(i - 1) == 'k' || str.charAt(i - 1) == 'l'
								|| str.charAt(i - 1) == 'm' || str.charAt(i - 1) == 'n' || str.charAt(i - 1) == 'o'
								|| str.charAt(i - 1) == 'p' || str.charAt(i - 1) == 'q' || str.charAt(i - 1) == 'r'
								|| str.charAt(i - 1) == 's' || str.charAt(i - 1) == 't' || str.charAt(i - 1) == 'u'
								|| str.charAt(i - 1) == 'v' || str.charAt(i - 1) == 'w' || str.charAt(i - 1) == 'x'
								|| str.charAt(i - 1) == 'y' || str.charAt(i - 1) == 'z'))
							continue;

						while (i + len < chars.length && !isCharsEqual(chars[i + len], ' ')
								&& !isCharsEqual(chars[i + len], '[') && !isCharsEqual(chars[i + len], ']')
								&& !isCharsEqual(chars[i + len], '(') && !isCharsEqual(chars[i + len], ')')
								&& !isCharsEqual(chars[i + len], '{') && !isCharsEqual(chars[i + len], '}')
								&& !isCharsEqual(chars[i + len], '<') && !isCharsEqual(chars[i + len], '>')
								&& !isCharsEqual(chars[i + len], ',') && !isCharsEqual(chars[i + len], ';')
								&& !isCharsEqual(chars[i + len], '.') && !isCharsEqual(chars[i + len], ':')
								&& !isCharsEqual(chars[i + len], '=') && !isCharsEqual(chars[i + len], '\"')
								&& !isCharsEqual(chars[i + len], '\'')) {
							len++;
						}

						// if (i + len < chars.length) {
						if (ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") || ext.equalsIgnoreCase(".ld")
								|| ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss")
								|| ext.equalsIgnoreCase(".sql") || ext.equalsIgnoreCase(".makefile")
								|| ext.equalsIgnoreCase(".mk") || ext.equalsIgnoreCase(".mak")
								|| ext.equalsIgnoreCase(".make")
								|| editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile")
								|| ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com")
								|| ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1")
								|| ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc") || ext.equalsIgnoreCase(".project")
								|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
								|| ext.equalsIgnoreCase(".svg") || ext.equalsIgnoreCase(".xml")
								|| ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss"))
							fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						else {
							if (i - 1 > 0 && Character.isLetter(chars[i - 1]))
								continue;
							
							fs = color(i, i + len, new IDEFont(Fonts.objectsEditor, FONT_SIZE), fs);
						}
					}
				}
				
				indxs = findWord(new String(chars), ")");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "return"); // depois de <palavra>

				len = 0;

				for (Integer i : indxs) {
					len = 0;

					while (i + len < chars.length)
						len++;

					// if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "]");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
				indxs = findWord(new String(chars), ":");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(') {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ".");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
							&& chars[c] != ']' && chars[c] != ',' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ";");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
							&& chars[c] != ']' && chars[c] != ',' && chars[c] != '.' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ".");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
							&& chars[c] != ']' && chars[c] != ',' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // mais tarde arrumar os
																								// outros bugs, ou em
																								// outra update
				}

				indxs = findWord(new String(chars), "[");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != ']'
							&& chars[c] != ',' && chars[c] != '.' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "->");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != ']'
							&& chars[c] != ',' && chars[c] != '.' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "=");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					boolean hasSpace = false;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(' && chars[c] != ':') {
						c--;
						len++;

						if (chars[c] == ' ') {
							if (hasSpace)
								break;

							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				fs = colorMethods(ext, chars, fs);

				for (String s : jsKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}
			}

			if (isCssPart) {
				indxs = findWord(new String(chars), ":");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
							&& chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.'
							&& chars[c] != '#' && chars[c] != '!') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				String ss = "*";

				indxs = findWord(new String(chars), ss);

				for (Integer i : indxs) {
					if ((i + ss.length() < chars.length && i - 1 > 0
							&& (((Character.isLetter(chars[i + ss.length()])
									|| (chars[i - 1] == '_' || chars[i + ss.length()] == '_')))))
							|| (i > 0 && Character.isLetter(chars[i - 1])))
						continue;

					fs = color(i, i + ss.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
																										// offset
				}

				for (String s : units) { // colorir tags
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}

				for (String s : cssTags) { // colorir tags | "em" est� aqui
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}

				for (String s : props) { // colorir tags
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}

				indxs = findWord(new String(chars), ".");

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "#"); // ids

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "&"); // scss selectors

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "$"); // scss selectors

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ":"); // atributos de tags

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				/*
				 * indxs = findWord(new String(chars), ";");
				 * 
				 * for (Integer i : indxs) { int c = i; len = 0;
				 * 
				 * while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '['
				 * && chars[c] != ']' && chars[c] != ':' && chars[c] != '{') { c--; len++; }
				 * 
				 * fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
				 */

				indxs = findWord(new String(chars), ";");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
							&& chars[c] != '.' && chars[c] != '#' && chars[c] != ':' && !isNumber(chars[c - 1])) {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "]");

				for (Integer i : indxs) {
					int c = i;
					len = 0;

					while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
							&& chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':') {
						c--;
						len++;
					}

					fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				// Eu sei que a linha de c�digo abaixo infringe a lei do Boot de C�digo-Fonte
				// bem escrito n� 547, e pode accaretar problemas :/

				fs = colorMethods(ext, chars, fs);

				for (String s : cssAdds) { // colorir tags
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}
			}
			break;

		case ".scss":
		case ".css":
			indxs = findWord(new String(chars), ":");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
						&& chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != '#'
						&& chars[c] != '!') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			for (String s : units) { // colorir tags
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
																										// offset
				}
			}

			for (String s : cssTags) { // colorir tags
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			for (String s : props) { // colorir tags
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			indxs = findWord(new String(chars), ".");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "#"); // ids

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "&"); // scss selectors

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "$"); // scss selectors

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), ":"); // atributos de tags

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			/*
			 * indxs = findWord(new String(chars), ";");
			 * 
			 * for (Integer i : indxs) { int c = i; len = 0;
			 * 
			 * while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '['
			 * && chars[c] != ']' && chars[c] != ':' && chars[c] != '{') { c--; len++; }
			 * 
			 * fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
			 */

			indxs = findWord(new String(chars), ";");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
						&& chars[c] != '.' && chars[c] != '#' && chars[c] != ':' && !isNumber(chars[c - 1])) {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "]");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
						&& chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			// Eu sei que a linha de c�digo abaixo infringe a lei do Boot de C�digo-Fonte
			// bem escrito n� 547, e pode accaretar problemas :/

			fs = colorMethods(ext, chars, fs);

			for (String s : cssAdds) { // colorir tags
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".ipynb":
		case ".py":
		case ".pyx":
		case ".pyd":
			for (String s : pyKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".dart":
			for (String s : dartKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".ld":
			for (String s : ldKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas n�o colorem

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".pp":
		case ".pas":
		case ".lpr":
			for (String s : pasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas n�o colorem

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".c":
			for (String s : cKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".h":
		case ".ino":
		case ".hh":
		case ".hpp":
		case ".hxx":
		case ".cxx":
		case ".cpp":
		case ".cc":
			for (String s : cppKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".cs":
			for (String s : csKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".r":
			for (String s : rKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".ps1":
		case ".cmd":
		case ".com":
		case ".bat":
			for (String s : batCom) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {

					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;
					// if (i > 0 && (chars[i - 1] != ' ' || chars[i - 1] != '\t')) continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			/*
			 * for (String s : extensions) { // colorir keywordss indxs = findWord(new
			 * String(chars), s);
			 * 
			 * for (Integer i : indxs) { if (((i - 1 > 0) && (chars[i - 1] == '_' ||
			 * Character.isLetter(chars[i - 1]))) || ((i + s.length() < chars.length) &&
			 * (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
			 * continue;
			 * 
			 * fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE),
			 * fs); // tem q dar offset } }
			 */

			/*
			 * indxs = findWord(new String(chars), "/");
			 * 
			 * len = 0;
			 * 
			 * for (Integer i : indxs) { while (i + len < chars.length && chars[i + len] !=
			 * ' ') len++;
			 * 
			 * if (i + len < chars.length) fs = color(i, i + len, new
			 * IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
			 * 
			 * indxs = findWord(new String(chars), "-");
			 * 
			 * len = 0;
			 * 
			 * for (Integer i : indxs) { while (i + len < chars.length && chars[i + len] !=
			 * ' ') len++;
			 * 
			 * if (i + len < chars.length) fs = color(i, i + len, new
			 * IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
			 */

			indxs = findWord(new String(chars), "%"); // se quiser fazer entre %% tem que fazer uma vari�vel boolean de
														// controle, como o multilinecommenting.

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;

		case ".jsx":
		case ".vue":
		case ".mjs":
		case ".js":
			for (String s : jsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // TODO - tomar cuidado em colorir tags em HTML mesmo dentro da
														// JSPart ou CssPart viu

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			String str = "exports"; // gambiarra pura

			indxs = findWord(new String(chars), str); // TODO - tomar cuidado em colorir tags em HTML mesmo dentro da
														// JSPart ou CssPart viu

			for (Integer i : indxs) {
				if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
						|| ((i + str.length() < chars.length)
								&& (chars[i + str.length()] == '_' || Character.isLetter(chars[i + str.length()]))))
					continue;

				fs = color(i, i + str.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar offset
			}

			break;

		case ".lua":
			for (String s : luaKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;

		case ".gd":
			for (String s : gdKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;
			
		case ".mcfunction":
			for (String s : mcKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;

		case ".zig":
			for (String s : zigKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;

		case ".sql":
			for (String s : sqlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;

		case ".s":
		case ".asm":
			for (String s : asmRegs) {
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
																										// offset
			}

			for (String s : asmKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			for (String s : sections) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
																										// offset
				}
			}

			indxs = findWord(new String(chars), "db");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
						&& chars[c] != ';' && chars[c] != '.' && chars[c] != ':') {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "dw");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
						&& chars[c] != ';' && chars[c] != '.' && chars[c] != ':') {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "equ");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
						&& chars[c] != ';' && chars[c] != '.' && chars[c] != ':') {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), ".");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;

		case ".jl":
			for (String s : jlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".pl":
		case ".t":
			for (String s : plKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset

				}
			}

			break;

		case ".hs":
		case ".has":
			for (String s : hasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".fs":
			for (String s : fsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".coffee":
			for (String s : cfKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".markdown":
		case ".md":
			indxs = findWord(new String(chars), "#");

			for (Integer i : indxs) {
				if (i != 0)
					continue;

				fs = color(i, fs.size(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
			}

			for (String s : tags) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".ini":
			indxs = findWord(new String(chars), "]");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ':') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
			}

			indxs = findWord(new String(chars), "=");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(' && chars[c] != ':') {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (hasSpace)
							break;

						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;

		case ".swift":
			for (String s : swKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".rs":
			for (String s : rsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}

			break;

		case ".sh":
		case ".bashrc":
		case ".bash_profile":
			for (String s : shKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}

			/*
			 * for (String s : extensions) { // colorir keywordss indxs = findWord(new
			 * String(chars), s);
			 * 
			 * for (Integer i : indxs) { if (((i - 1 > 0) && (chars[i - 1] == '_' ||
			 * Character.isLetter(chars[i - 1]))) || ((i + s.length() < chars.length) &&
			 * (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
			 * continue;
			 * 
			 * fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE),
			 * fs); // tem q dar offset } }
			 */

			/*
			 * indxs = findWord(new String(chars), "/");
			 * 
			 * len = 0;
			 * 
			 * for (Integer i : indxs) { while (i + len < chars.length && chars[i + len] !=
			 * ' ') len++;
			 * 
			 * if (i + len < chars.length) fs = color(i, i + len, new
			 * IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
			 * 
			 * indxs = findWord(new String(chars), "-");
			 * 
			 * len = 0;
			 * 
			 * for (Integer i : indxs) { while (i + len < chars.length && chars[i + len] !=
			 * ' ') len++;
			 * 
			 * if (i + len < chars.length) fs = color(i, i + len, new
			 * IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); }
			 */

			break; // Release v3.9.1 - 12/08/2021 - 08:03

		case ".php":
			for (String s : phpKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}

			for (String s : tags) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}

			break;

		case ".tsx":
		case ".ts":
			for (String s : tsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			str = "exports";

			indxs = findWord(new String(chars), str); // TODO - tomar cuidado em colorir tags em HTML mesmo dentro da
														// JSPart ou CssPart viu

			for (Integer i : indxs) {
				if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
						|| ((i + str.length() < chars.length)
								&& (chars[i + str.length()] == '_' || Character.isLetter(chars[i + str.length()]))))
					continue;

				fs = color(i, i + str.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar offset
			}

			break;

		case ".jsonc":
		case ".json":
			for (String s : jsonKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".kt":
			for (String s : ktKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}
			break;

		case ".rb":
			for (String s : rbKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".scala":
			for (String s : scaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".go":
			for (String s : goKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".m":
		case ".mm":
			for (String s : objKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			break;

		case ".conf":
			for (String s : ideConfKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // haha slk merm�o colorir coisas de at� pr�prio arquivo de
														// configura��es

				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
			}

			indxs = findWord(new String(chars), ":");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
						&& chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;

		case ".makefile":
		case ".mk":
		case ".mak":
		case ".make":
			for (String s : mergeStringArrays(makeKeys, shKeys)) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			indxs = findWord(new String(chars), ":");

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '['
						&& chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.') {
					c--;
					len++;
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;

		case ".dockerfile":
			for (String s : dkKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}

			indxs = findWord(new String(chars), "="); // antes de <palavra>

			for (Integer i : indxs) {
				int c = i;
				len = 0;

				boolean hasSpace = false;

				while (c < chars.length && c + len < chars.length && c > 0) {
					c--;
					len++;

					if (chars[c] == ' ') {
						if (hasSpace)
							break;

						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}

				fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			}

			break;
		}

		return fs;
	}

	public List<IDEFont> colorMethods(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".bf")
				|| ext.equalsIgnoreCase(".obj") || ext.equalsIgnoreCase(".conf") || ext.equalsIgnoreCase(".txt")
				|| ext.equalsIgnoreCase(".log"))
			return fs;

		if (isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent()))) {

			if (!(ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd")
					|| ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc"))) {

				// primeira vez usando labels!
				methods: if (!(ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown"))) {
					if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
							|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
							|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
							|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
							|| ext.equalsIgnoreCase(".project")) {
						if (!(isCssPart || isJSPart || isPhpPart))
							break methods;
					}

					indxs = findWord(new String(chars), "(");

					for (Integer i : indxs) {
						int c = i;
						int len = 0;

						boolean hasSpace = false;

						while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']'
								&& chars[c] != ')' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.'
								&& chars[c] != '=' && chars[c] != ':' && chars[c] != '#' && chars[c] != '$'
								&& chars[c] != '/' && chars[c] != '>' && // creio que n�o precisa verificar < tbm
								chars[c] != '\\' && (chars[i - 1] != '+' && chars[i - 1] != '-' && chars[i - 1] != '*'
										&& chars[i - 1] != '/')) {
							c--;
							len++;

							if (Character.isLetter(chars[c]))
								hasSpace = true;

							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								else
									hasSpace = true;
							}
						}

						/*
						 * String methodname = new String(sliceCharArray(c + 1, c + len, chars));
						 * 
						 * try { if (methodname.contains(" ")) methodname = methodname.split(" ")[1]; }
						 * catch (Exception e) {}
						 */

						// if (!autoCompletesEqual())
						// addautocomplete.add(new AutoComplete(methodname, AutoCompleteType.FUNCTION));

						fs = color(c, c + len, new IDEFont(Fonts.methodsEditor, FONT_SIZE), fs);
					}
				}
			}
		}

		return fs;
	}

	public List<IDEFont> colorNumbers(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		if (!isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent())))
			return fs;

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".md")
				|| ext.equalsIgnoreCase(".markdown") || ext.equalsIgnoreCase(".bf") || ext.equalsIgnoreCase(".obj")
				|| ext.equalsIgnoreCase(".lock") || ext.equalsIgnoreCase(".toml"))
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if ((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
				|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
				|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
				|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
				|| ext.equalsIgnoreCase(".project") || ext.equalsIgnoreCase(".txt")
				|| ext.equalsIgnoreCase(".log")) && !(isCssPart || isJSPart || isPhpPart))
			return fs;

		// boolean toContinue = false;

		for (String s : nums) { // colorir n�meros
			indxs = findWord(new String(chars), s); // TODO

			for (Integer i : indxs) {
				if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown"))
					continue;

				if ((((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
						|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
						|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
						|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
						|| ext.equalsIgnoreCase(".project") && isCssPart))
						|| (ext.equalsIgnoreCase(".css") | ext.equalsIgnoreCase(".scss")))
						&& hasAfter(new String(chars), i, '{'))
					continue;
				
				/*int len = 0;
				
				for (Integer j : indxs) {
					while (j + len < chars.length && chars[j + len] != ' ' && chars[j + len] != '[' && chars[j + len] != ']'
							&& chars[j + len] != '(' && chars[j + len] != ')' && chars[j + len] != ',' && chars[j + len] != ';'
							&& chars[j + len] != '.' && chars[j + len] != ':')
						len++;
				}
				
				char[] chs = (" " + new String(chars)).toCharArray();
				
				int c = i;
				
				while (c > 0 && chars[c] != ' ') {
					c--;
				}
				
				if (chs[c] == ' ') {
					if (Character.isLetter(chars[c + 1])) break; // no index0 o problema ainda existe
				}*/

				/*
				 * if (Character.isLetter(chars[i - 1])) for (int j = i; i > 1; i--) { if
				 * (chars[j - 1] == ' ') { if (!isNumber(chars[j])) { toContinue = true; break;
				 * } } }
				 * 
				 * if (toContinue) { toContinue = false;
				 * 
				 * continue; }
				 */

				// if ((i > 0 && (Character.isLetter(chars[i - 1]) && !fs.get(i -
				// 1).getColor().equals(Colors.numbers))) && (ext.equalsIgnoreCase(".html") |
				// ext.equalsIgnoreCase(".xhtml")) continue;
				// if ((i + s.length() < chars.length && i - 1 > 0 &&
				// (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i -
				// 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) &&
				// !(ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss"))) continue;
				// if (Character.isLetter(chars[i - 1]) || Character.isLetter(chars[i +
				// s.length()])) continue;

				fs = color(i, i + s.length(), new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
			}
		}

		indxs = findWord(new String(chars), "0x");

		int len = 0;

		for (Integer i : indxs) {
			while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']'
					&& chars[i + len] != '(' && chars[i + len] != ')' && chars[i + len] != ',' && chars[i + len] != ';'
					&& chars[i + len] != '.' && chars[i + len] != ':')
				len++;

			fs = color(i, i + len, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
		}

		indxs = findWord(new String(chars), "#");

		len = 0;

		for (Integer i : indxs) {
			while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']'
					&& chars[i + len] != '(' && chars[i + len] != ')' && chars[i + len] != ',' && chars[i + len] != ';'
					&& chars[i + len] != '.' && chars[i + len] != ':')
				len++;

			fs = color(i, i + len, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
		}

		// fs = colorObjects(ext, chars, fs); // infelizmente esse bug ainda existe

		return fs;
	}

	public List<IDEFont> colorSymbols(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".bf")
				|| ext.equalsIgnoreCase(".obj") || ext.equalsIgnoreCase(".txt") || ext.equalsIgnoreCase(".log"))
			return fs;

		if (isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent()))) {
			
			int index = 0;
			for (String s : syms) {
				indxs = findWord(new String(chars), s);
				if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")) // resolver isso aqui
					continue;
				
				if (s == ">" && index == 0 && ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")) continue;
				
				if (((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
						|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
						|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
						|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
						|| ext.equalsIgnoreCase(".project"))
						&& !(isCssPart || isJSPart || isPhpPart)) && (s != "="))
					continue;
				// if (!(isCssPart || isJSPart || isPhpPart) && ((ext.equalsIgnoreCase(".html")
				// || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".xhtml") ||
				// ext.equalsIgnoreCase(".ejs")) && !isJSPart && (s != "<" && s != ">" && s !=
				// "/" && s != "="))) continue;
				if (((ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss")
						|| (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
				|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
				|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
				|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
				|| ext.equalsIgnoreCase(".project")) && isCssPart))
						&& (s == "*"))
					continue;
				if ((ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".bash_profile") || ext.equalsIgnoreCase(".bashrc") || ext.equalsIgnoreCase(".com")
						|| ext.equalsIgnoreCase(".cmd")) && (s == "+" || s == "@"))
					continue;
				if ((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
						|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
						|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
						|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
						|| ext.equalsIgnoreCase(".project")) && (s == "@" || s == "#"))
					continue;
				
				if ((ext.equalsIgnoreCase(".mcfunction")) && (s == "@"))
					continue;
				
				// if ((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") ||
				// ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") ||
				// ext.equalsIgnoreCase(".project") || ext.equalsIgnoreCase(".classpath") ||
				// ext.equalsIgnoreCase(".xhtml")) && (s == "-")) continue;

				// Remover #, mas n�o da lista
				if (s == "#")
					continue; // talvez remover \

				for (Integer i : indxs)
					fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
				
				index++;
			}
		}

		return fs;
	}
	

	public List<IDEFont> colorExtras(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".obj"))
			return fs;

		if (isFormatSupported(ListableFile.getFileExtension(editing.getRegent().getRegent()))) {

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			{
				indxs = findWord(new String(chars), "\""); // colorir strings

				List<Integer> removeIndxs = new ArrayList<>();

				for (Integer i : indxs) {
					if (i <= 0)
						continue;

					if (new String(chars).charAt(i - 1) == '\\')
						removeIndxs.add(i);
				}

				indxs.removeAll(removeIndxs);

				for (int i = 0; i < indxs.size() - 1; i += 2)
					fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);

				///
				
				if (ext.equalsIgnoreCase(".cmxa") || ext.equalsIgnoreCase(".ml") || ext.equalsIgnoreCase(".mli") || ext.equalsIgnoreCase(".mly") || ext.equalsIgnoreCase(".clt")) return fs;

				indxs = findWord(new String(chars), "`"); // colorir strings

				removeIndxs = new ArrayList<>();

				for (Integer i : indxs) {
					if (i <= 0)
						continue;

					if (new String(chars).charAt(i - 1) == '\\')
						removeIndxs.add(i);
				}

				indxs.removeAll(removeIndxs);

				for (int i = 0; i < indxs.size() - 1; i += 2)
					fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
				
				///
				indxs = findWord(new String(chars), "```");
				
				//System.out.println(indxs.size());
				
				for (int i = 0; i < indxs.size() - 1; i++)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);

				/*
				 * indxs = findWord(new String(chars), "\""); // colorir coment�rios multi-linha
				 * - caracteres iguais
				 * 
				 * if (indxs.size() > 0 && !isMultilineString) { // provavelmente esse � o
				 * abrimento fs = color(indxs.get(0), indxs.size() > 1 ? indxs.get(1) :
				 * fs.size(), new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
				 * isMultilineString = true;
				 * 
				 * isAnotherIterationString = false; }
				 * 
				 * if (indxs.size() > 0 && isMultilineString && isAnotherIterationString) { //
				 * provavelmente esse � o fechamento fs = color(0, indxs.get(0) + 2, new
				 * IDEFont(Fonts.stringsEditor, FONT_SIZE), fs); isMultilineString = false; }
				 * 
				 * isAnotherIterationString = true;
				 * 
				 * if (isMultilineString) fs = color(0, fs.size(), new
				 * IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
				 */

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				if (!(ext.equalsIgnoreCase(".vb") || ext.equalsIgnoreCase(".vbs"))) {
					indxs = findWord(new String(chars), "'"); // colorir chars
	
					removeIndxs = new ArrayList<>();
	
					for (Integer i : indxs) {
						if (i <= 0)
							continue;
	
						if (new String(chars).charAt(i - 1) == '\\')
							removeIndxs.add(i);
					}
	
					for (int i = 0; i < indxs.size() - 1; i += 2)
						fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE), fs);
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for (IDEFont i : fs) {
				i.setSize(FONT_SIZE);
			}

			// extras que precisam ser coloridos depois disso

			if (ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc")) {
				indxs = findWord(new String(chars), ":");
				
				for (Integer i : indxs) {
					if (isInside(i, '\"', '\"', new String(chars)) || isInside(i, '\'', '\'', new String(chars)) || isInside(i, '`', '`', new String(chars))) continue;
					
					int c = i;
					int len = 0;
					
					boolean isSymbol = false;

					while (c < chars.length &&
						   c + len < chars.length &&
						   c > 0 && chars[c] != ' ' &&
						   chars[c] != '[' &&
						   chars[c] != ']' &&
						   chars[c] != '{' &&
						   chars[c] != '}' &&
						   chars[c] != ',' &&
						   chars[c] != ';') {
						c--;
						len++;
						
						isSymbol = true;
					}

					fs = color(isSymbol ? c + 1 : c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}
			}
			if (ext.equalsIgnoreCase(".bf")) {
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				
				indxs = findWord(new String(chars), "[");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "]");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ">");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "<");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ".");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), ",");

				for (Integer i : indxs) {
					// if (i + 1 < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + 1]) ||
					// Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + 1] ==
					// '_'))) continue;

					fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
				}
			}

			if (ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".mjs")
					|| ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".tsx")) {
				for (String s : tags) { // colorir tags
					indxs = findWord(new String(chars), s);

					for (Integer i : indxs) {
						if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
								|| ((i + s.length() < chars.length)
										&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
							continue;

						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar
																											// offset
					}
				}

				// colorir manualmente <>

				String s = "<";

				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs); // tem q dar offset
				}

				s = ">";

				indxs = findWord(new String(chars), s);

				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;

					fs = color(i, i + s.length(), new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}

			if (ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyx")
					|| ext.equalsIgnoreCase(".ipynb") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".zig")) {
				indxs = findWord(new String(chars), "@");

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && !isCharsEqual(chars[i + len], ' ')
							&& !isCharsEqual(chars[i + len], '[') && !isCharsEqual(chars[i + len], ']')
							&& !isCharsEqual(chars[i + len], '(') && !isCharsEqual(chars[i + len], ')')
							&& !isCharsEqual(chars[i + len], ',') && !isCharsEqual(chars[i + len], ';')
							&& !isCharsEqual(chars[i + len], '.') && !isCharsEqual(chars[i + len], ':')
							&& !isCharsEqual(chars[i + len], '=') && !isCharsEqual(chars[i + len], '\"')
							&& !isCharsEqual(chars[i + len], '\'')) {
						len++;
					}

					fs = color(i, i + len, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
				}
			}
			
			if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
					|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
					|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
					|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
					|| ext.equalsIgnoreCase(".project")) { // colorir tags din�micas
				indxs = findWord(new String(chars), ">"); // colorir final de tags

				for (Integer i : indxs) {
					fs = color(i, i + 1, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "<");

				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']'
							&& chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.'
							&& chars[i + len] != ':' && chars[i + len] != '>' && chars[i + len] != '<')
						len++;
					
					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}

				indxs = findWord(new String(chars), "</");

				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']'
							&& chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.'
							&& chars[i + len] != ':')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
				}
			}
			
		}
		return fs;
	}

	public List<IDEFont> colorNoExtensions(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".bf") || ext.equalsIgnoreCase(".out")
				|| ext.equalsIgnoreCase(".obj"))
			return fs;

		if (!foundExt) {// (!foundExt && editing != null) || (extType.equalsIgnoreCase("") || extType ==
						// null)) { // TODO o culpado do gitignore estar assim � esse ARRUMAR DEPOIS
			for (FileType f : ListableFile.types) {
				if (f.getExtension().equalsIgnoreCase(editing.getRegent().getRegent().getName())) { // tenta ver se tem
																									// algum especial
					String st = capitalizeFirstLetter(f.getExtension());

					switch (st.toLowerCase()) {
					case "dockerfile":
						for (String s : dkKeys) { // colorir keywords
							indxs = findWord(new String(chars), s);

							for (Integer i : indxs)
								fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs);
						}

						indxs = findWord(new String(chars), "="); // antes de <palavra>

						for (Integer i : indxs) {
							int c = i;
							int len = 0;

							boolean hasSpace = false;

							while (c < chars.length && c + len < chars.length && c > 0) {
								c--;
								len++;

								if (chars[c] == ' ') {
									if (hasSpace)
										break;

									if (!hasSpace)
										hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
								}
							}

							fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						}

						break;
						
					case "authors":
						indxs = findWord(new String(chars), "<");
						List<Integer> finals = findWord(new String(chars), ">");

						for (int i = 0; i < indxs.size(); i++) {
							try {
								fs = color(indxs.get(i), finals.get(i) + 1, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
							} catch (Exception e) {
								continue;
							}
						}
						
						String withSpace = " " + new String(chars);
						char[] chs = withSpace.toCharArray();
						
						indxs = findWord(new String(chs), "#"); // colorir coment�rios de uma linha
						
						if (fs.size() == 0)
							break;

						for (Integer i : indxs) {
							if (!indxs.isEmpty()) {
								boolean br = false;
								
								if (i >= indxs.size()) i = indxs.size() - 1;
								
								if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
									br = true;
									
									break;
								}
								
								if (br) break;
							}
							
							for (int j = 0; j < indxs.size(); j++)
								indxs.set(j, indxs.get(j) - 1);
							
							if (indxs.size() != 0)
								fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
						}
						
						break;

					case "makefile":
						for (String s : mergeStringArrays(makeKeys, shKeys)) { // colorir keywords
							indxs = findWord(new String(chars), s);

							for (Integer i : indxs)
								fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem
																													// q
																													// dar
																													// offset
						}

						indxs = findWord(new String(chars), ":");

						for (Integer i : indxs) {
							int c = i;
							int len = 0;

							while (c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' '
									&& chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ';'
									&& chars[c] != '.') {
								c--;
								len++;
							}

							fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						}
						
						indxs = findWord(new String(chars), "="); // antes de <palavra>

						for (Integer i : indxs) {
							int c = i;
							int len = 0;

							boolean hasSpace = false;

							while (c < chars.length && c + len < chars.length && c > 0) {
								c--;
								len++;

								if (chars[c] == ' ') {
									if (hasSpace)
										break;

									if (!hasSpace)
										hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
								}
							}

							fs = color(c, c + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						}
						
						indxs = findWord(new String(chars), "("); // depois de <palavra>

						int len = 0;

						for (Integer i : indxs) {
							while (i + len < chars.length && chars[i + len] != ')')
								len++;

							fs = color(i, i + len, new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
						}
						
						break;
					}

					if (editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile")
							|| editing.getRegent().getRegent().getName().equalsIgnoreCase("dockerfile")) {
						for (String s : nums) { // colorir n�meros
							indxs = findWord(new String(chars), s); // TODO

							for (Integer i : indxs) {
								if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown"))
									continue;

								if (((((ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
										|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
										|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
										|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
										|| ext.equalsIgnoreCase(".project")) && isCssPart))
										|| (ext.equalsIgnoreCase(".css") | ext.equalsIgnoreCase(".scss")))
										&& hasAfter(new String(chars), i, '{'))
									continue;

								// if ((i > 0 && (Character.isLetter(chars[i - 1]) && !fs.get(i -
								// 1).getColor().equals(Colors.numbers))) && (ext.equalsIgnoreCase(".html") |
								// ext.equalsIgnoreCase(".xhtml")) continue;
								// if ((i + s.length() < chars.length && i - 1 > 0 &&
								// (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i -
								// 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) &&
								// !(ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss"))) continue;
								// if (Character.isLetter(chars[i - 1]) || Character.isLetter(chars[i +
								// s.length()])) continue;

								fs = color(i, i + s.length(), new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
							}
						}

						indxs = findWord(new String(chars), "0x");

						int len = 0;

						for (Integer i : indxs) {
							while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '['
									&& chars[i + len] != ']' && chars[i + len] != '(' && chars[i + len] != ')'
									&& chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.'
									&& chars[i + len] != ':')
								len++;

							fs = color(i, i + len, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
						}

						indxs = findWord(new String(chars), "#");

						len = 0;

						for (Integer i : indxs) {
							while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '['
									&& chars[i + len] != ']' && chars[i + len] != '(' && chars[i + len] != ')'
									&& chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.'
									&& chars[i + len] != ':')
								len++;

							fs = color(i, i + len, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
						}

						////

						for (String s : syms) {
							indxs = findWord(new String(chars), s);

							/*
							 * // Remover #, mas n�o da lista if (s == "#") continue; // talvez remover \
							 */

							for (Integer i : indxs)
								fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
						}

						//

						indxs = findWord(new String(chars), Character.toString((char) 34)); // colorir strings

						List<Integer> removeIndxs = new ArrayList<>();

						for (Integer i : indxs) {
							if (i <= 0)
								continue;

							if (new String(chars).charAt(i - 1) == '\\')
								removeIndxs.add(i);
						}

						indxs.removeAll(removeIndxs);

						for (int i = 0; i < indxs.size() - 1; i += 2)
							fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE),
									fs);

						///

						indxs = findWord(new String(chars), "`"); // colorir strings

						removeIndxs = new ArrayList<>();

						for (Integer i : indxs) {
							if (i <= 0)
								continue;

							if (new String(chars).charAt(i - 1) == '\\')
								removeIndxs.add(i);
						}

						indxs.removeAll(removeIndxs);

						for (int i = 0; i < indxs.size() - 1; i += 2)
							fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE),
									fs);

						removeIndxs = new ArrayList<>();

						for (Integer i : indxs) {
							if (i <= 0)
								continue;

							if (new String(chars).charAt(i - 1) == '\\')
								removeIndxs.add(i);
						}

						for (int i = 0; i < indxs.size() - 1; i += 2)
							fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE),
									fs);

						indxs = findWord(new String(chars), Character.toString((char) 39)); // colorir chars

						removeIndxs = new ArrayList<>();

						for (Integer i : indxs) {
							if (i <= 0)
								continue;

							if (new String(chars).charAt(i - 1) == '\\')
								removeIndxs.add(i);
						}

						for (int i = 0; i < indxs.size() - 1; i += 2)
							fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsEditor, FONT_SIZE),
									fs);
					}

					// Coment�rios de uma linha

					switch (st.toLowerCase()) {
					case "dockerfile":
					case "makefile":
					case "gitignore":
						indxs = findWord(new String(chars), "#"); // colorir coment�rios de uma linha

						if (indxs.size() != 0)
							fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
						break;
					}

					// Coment�rios Multi-linha

					switch (st.toLowerCase()) {
					case "makefile":
						indxs = findWord(new String(chars), "/*"); // colorir coment�rios multi-linha - caracteres
																	// diferentes
						List<Integer> finals = findWord(new String(chars), "*/");

						if (indxs.size() > 0) {
							fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
									new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
							isMultilineCommenting = true;
						}

						if (finals.size() > 0) {
							fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
									new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
							isMultilineCommenting = false;
						}

						if (isMultilineCommenting)
							fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
						break;
					}
				}
			}

			/*
			 * if (extType.equalsIgnoreCase("") || extType == null) { String extn = "";
			 * 
			 * try { extn =
			 * ListableFile.getFileExtension(editing.getRegent().getRegent()).substring(1);
			 * // tenta retornar o nome da extens�o } catch (Exception e) { extn = Main.lang
			 * == Language.PORT ? "Sem Extens�o" : "No Extension"; // se n�o der mesmo
			 * assim, coloque "Sem Extens�o". }
			 * 
			 * extType = extn; foundExt = true; }
			 */
		}

		return fs;
	}

	public List<IDEFont> colorComments(String ext, char[] chars, List<IDEFont> fs) {
		if (editing == null)
			return fs;

		List<Integer> indxs = new ArrayList<>();

		if (ext.equalsIgnoreCase(".o") || ext.equalsIgnoreCase(".bf") || ext.equalsIgnoreCase(".out") || ext.equalsIgnoreCase(".obj"))
			return fs;

		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cc":
		case ".cs":
		case ".js":
		case ".mjs":
		case ".vue":
		case ".jsx":
		case ".h":
		case ".hh":
		case ".hpp":
		case ".hxx":
		case ".swift":
		case ".zig":
		case ".rs":
		case ".kt":
		case ".ino":
		case ".ts":
		case ".tsx":
		case ".go":
		case ".json":
		case ".fs":
		case ".m":
		case ".mm":
		case ".pp":
		case ".pas":
		case ".lpr":
		case ".scala":
		case ".por":
		case ".v":
		case ".vh":
		case ".vsh":
		case ".mod":
			String withSpace = " " + new String(chars);
			char[] chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "//"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if (isInside(i, '\"', '\"', withSpace) && isInside(i, '\'', '\'', withSpace) && isInside(i, '`', '`', withSpace)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
					}
					
					if (i > 1 && chars[i - 2] == ':') continue;
					
					if (br) continue;
				}
				
				for (int j = 0; j < indxs.size(); j++) // para colorir o primeiro /
					indxs.set(j, indxs.get(j) - 1);
				
				if (i < 0) i = 0;
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;
		
		case ".vb":
		case ".vbs":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "'"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;

		case ".xhtml": // para o js
		case ".html":
		case ".svelte":
		case ".htm":
		case ".ejs":
			if (!isJSPart)
				break;
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "//"); // colorir coment�rios de uma linha
			List<Integer> indxs2 = findWord(new String(chs), "</script>");
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) {
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				for (int j = 0; j < indxs2.size(); j++)
					indxs2.set(j, indxs2.get(j) - 1);
				
				int finalIndex = indxs2.isEmpty() ? fs.size() : indxs2.get(0);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), finalIndex, new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}

			break;

		case ".ps1":
		case ".com":
		case ".bat":
		case ".cmd":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "REM"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			///////////////
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "rem"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;

		case ".s":
		case ".asm":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), ";"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "//"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if (isInside(i, '\"', '\"', withSpace) && isInside(i, '\'', '\'', withSpace) && isInside(i, '`', '`', withSpace)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						//System.out.println(br);
						
						//continue;
					}
					
					if (br) continue;
				}
				
				for (int j = 0; j < indxs.size(); j++) // para colorir o primeiro /
					indxs.set(j, indxs.get(j) - 1);
				
				if (i < 0) i = 0;
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;

		case ".lua":
		case ".sql":
		case ".has":
		case ".hs":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "--"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;
			
		case ".tf":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "//"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if (isInside(i, '\"', '\"', withSpace) && isInside(i, '\'', '\'', withSpace) && isInside(i, '`', '`', withSpace)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						//System.out.println(br);
						
						//continue;
					}
					
					if (br) continue;
				}
				
				for (int j = 0; j < indxs.size(); j++) // para colorir o primeiro /
					indxs.set(j, indxs.get(j) - 1);
				
				if (i < 0) i = 0;
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "#"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			
			break;

		case ".py":
		case ".pyx":
		case ".ipynb":
		case ".pyd":
		case ".rb":
		case ".r":
		case ".jl":
		case ".pl":
		case ".t":
		case ".coffee":
		case ".make":
		case ".sh":
		case ".bash_profile":
		case ".bashrc":
		case ".gitignore":
		case ".dockerfile":
		case ".config":
		case ".cfg":
		case ".ini":
		case ".lock":
		case ".toml":
		case ".gd":
		case ".mcfunction":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "#"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;

		case ".php":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "//"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			//////////
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "#"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;

		case ".markdown":
		case ".md":
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "[//]: #"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			
			///////////////
			
			withSpace = " " + new String(chars);
			chs = withSpace.toCharArray();
			
			indxs = findWord(new String(chs), "[]: #"); // colorir coment�rios de uma linha
			
			if (fs.size() == 0)
				break;

			for (Integer i : indxs) {
				if (!indxs.isEmpty()) {
					boolean br = false;
					
					if (i >= indxs.size()) i = indxs.size() - 1;
					
					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
						br = true;
						
						break;
					}
					
					if (br) break;
				}
				
				for (int j = 0; j < indxs.size(); j++)
					indxs.set(j, indxs.get(j) - 1);
				
				if (indxs.size() != 0)
					fs = color(indxs.get(i), fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
			}
			
			break;
		}
		
		////////////////////////////////////////////////////////////////////////

		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cc":
		case ".cxx":
		case ".cs":
		case ".js":
		case ".mjs":
		case ".h":
		case ".hh":
		case ".hpp":
		case ".hxx":
		case ".sql":
		case ".swift":
		case ".rs":
		case ".php":
		case ".kt":
		case ".vue":
		case ".jsx":
		case ".ino":
		case ".ts":
		case ".tsx":
		case ".go":
		case ".m":
		case ".mm":
		case ".ld":
		case ".scala":
		case ".scss":
		case ".css":
		case ".json":
		case ".jsonc":
		case ".por":
		case ".tf":
		case ".v":
		case ".vh":
		case ".vsh":
		case ".mod":
			indxs = findWord(new String(chars), "/*"); // colorir coment�rios multi-linha - caracteres diferentes
			List<Integer> finals = findWord(new String(chars), "*/");
			
			List<Integer> rm = new ArrayList<>();
			
			for (Integer i : indxs) {
				if (howManyBefore(new String(chars), i, '\"') % 2 != 0 || howManyBefore(new String(chars), i, '\'') % 2 != 0 || howManyBefore(new String(chars), i, '`') % 2 != 0)
					rm.add(i);
			}
			
			indxs.removeAll(rm);
			
			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			*/
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = finals;

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "*/".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "*/".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "*/".length()] == '_')))
					continue;

				fs = color(i, i + "*/".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			
//			String withSpace = " " + new String(chars);
//			char[] chs = withSpace.toCharArray();
//			
//			indxs = findWord(new String(chs), "/*"); // colorir coment�rios multilinha - caracteres diferentes
//			List<Integer> finals = findWord(new String(chs), "*/");
//			
//			if (fs.size() == 0)
//				break;
//			
//			int count = 0;
//			for (Integer i : indxs) {
//				if (!indxs.isEmpty()) {
//					boolean br = false;
//					
//					if (i >= indxs.size()) i = indxs.size() - 1;
//					
//					if ((howManyBefore(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyBefore(new String(chs), indxs.get(i), '`') % 2 != 0) && (howManyAfter(new String(chs), indxs.get(i), '\"') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '\'') % 2 != 0 || howManyAfter(new String(chs), indxs.get(i), '`') % 2 != 0)) { // se colocar 2 // na mesma linha o anterior � desfeito
//						br = true;
//						
//						break;
//					}
//					
//					if (br) break;
//				}
//				
//				for (int j = 0; j < indxs.size(); j++)
//					indxs.set(j, indxs.get(j) - 1);
//				
//				int finalIndex = finals.isEmpty() ? fs.size() : finals.get(count++);
//				
//				if (indxs.size() != 0) {
//					fs = color(indxs.get(i), finalIndex + 1, new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
//					
//					continue;
//				}
//				
//				isMultilineCommenting = finals.isEmpty();
//				
//				if (indxs.isEmpty() && finals.isEmpty()) {
//					fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
//				}
//			}

			break;
			
		/*case ".md":
		case ".markdown":
			indxs = findWord(new String(chars), "["); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "]");

			if (indxs.size() > 0) {
				fs = color(indxs.get(0) + 1, finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) + 1 : 0, finals.get(0),
						new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs);
			break;*/

		case ".lua": // Lua
			indxs = findWord(new String(chars), "--[["); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "--]]");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = findWord(new String(chars), "--]]");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "--]]".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "--]]".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "--]]".length()] == '_')))
					continue;

				fs = color(i, i + "--]]".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar
																										// offset
			}
			break;

		case ".rb": // Ruby
			indxs = findWord(new String(chars), "=begin"); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=end");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = findWord(new String(chars), "=end");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "=end".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "=end".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "=end".length()] == '_')))
					continue;

				fs = color(i, i + "=end".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar
																										// offset
			}
			break;

		case ".jl": // Julia
			indxs = findWord(new String(chars), "#="); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=#");

			for (Integer i : indxs)
				if (isBetween(new String(chars), i, '"', '"'))
					return fs;

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = findWord(new String(chars), "=#");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "=#".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "=#".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "=#".length()] == '_')))
					continue;

				fs = color(i, i + "=#".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			break;

		case ".has": // Haskell
		case ".hs":
			indxs = findWord(new String(chars), "{-"); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-}");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = findWord(new String(chars), "-}");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "-}".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "-}".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "-}".length()] == '_')))
					continue;

				fs = color(i, i + "-}".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			break;

		case ".fs": // F#
		case ".pas":
		case ".lpr":
		case ".pp":
		case ".cmxa":
		case ".ml":
		case ".mli":
		case ".mly":
		case ".clt":
			indxs = findWord(new String(chars), "(*"); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "*)");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			// if (indxs.size() == 0 && finals.size() == 0)
			// isMultilineCommenting = false;

			indxs = findWord(new String(chars), "*)");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "*)".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "*)".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "*)".length()] == '_')))
					continue;

				fs = color(i, i + "*)".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			break;

		case ".py":
		case ".pyx":
		case ".ipynb":
		case ".pyd":
			indxs = findWord(new String(chars), "\'\'\'"); // colorir coment�rios multi-linha - caracteres iguais

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */
			if (indxs.size() > 0 && !isMultilineCommenting) { // provavelmente esse � o abrimento
				fs = color(indxs.get(0), indxs.size() > 1 ? indxs.get(1) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;

				isAnotherIteration = false;
			}

			if (indxs.size() > 0 && isMultilineCommenting && isAnotherIteration) { // provavelmente esse � o fechamento
				fs = color(0, indxs.get(0) + 2, new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			isAnotherIteration = true;

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			break;

		case ".ejs":
		case ".xml":
		case ".sln":
		case ".classpath":
		case ".project":
		case ".htm":
		case ".xhtml":
		case ".html":
		case ".svelte":
		case ".svg":
			indxs = findWord(new String(chars), "<!--"); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-->");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			if (isCssPart || isJSPart || isPhpPart) {
				indxs = findWord(new String(chars), "/*"); // colorir coment�rios multi-linha - caracteres diferentes
				finals = findWord(new String(chars), "*/");

				for (Integer i : indxs)
					if (isBetween(new String(chars), i, '"', '"'))
						return fs;

				if (indxs.size() > 0) {
					fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
							new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
					isMultilineCommenting = true;
				}

				if (finals.size() > 0) {
					fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
							new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
					isMultilineCommenting = false;
				}

				if (isMultilineCommenting)
					fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

				indxs = findWord(new String(chars), "*/");

				/*
				 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
				 * fs;
				 */

				for (Integer i : indxs) {
					if (i + "*/".length() < chars.length && i - 1 > 0
							&& (Character.isLetter(chars[i + "*/".length()]) || Character.isLetter(chars[i - 1])
									|| (chars[i - 1] == '_' || chars[i + "*/".length()] == '_')))
						continue;

					fs = color(i, i + "*/".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar
																										// offset
				}
			}

			indxs = findWord(new String(chars), "-->");

			for (Integer i : indxs)
				if (isBetween(new String(chars), i, '"', '"'))
					return fs;

			for (Integer i : indxs) {
				if (i + "-->".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "-->".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "-->".length()] == '_')))
					continue;

				fs = color(i, i + "-->".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			break;

		case ".md":
		case ".markdown":
			indxs = findWord(new String(chars), "<!--"); // colorir coment�rios multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-->");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}

			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0),
						new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}

			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs);

			indxs = findWord(new String(chars), "-->");

			/*
			 * for (Integer i : indxs) if (isBetween(new String(chars), i, '"', '"')) return
			 * fs;
			 */

			for (Integer i : indxs) {
				if (i + "-->".length() < chars.length && i - 1 > 0
						&& (Character.isLetter(chars[i + "-->".length()]) || Character.isLetter(chars[i - 1])
								|| (chars[i - 1] == '_' || chars[i + "-->".length()] == '_')))
					continue;

				fs = color(i, i + "-->".length(), new IDEFont(Fonts.commentsEditor, FONT_SIZE), fs); // tem q dar offset
			}
			break;
		}

		return fs;
	}
	
	public List<IDEFont> colorOtherModes(String ext, char[] chars, List<IDEFont> fs) {
		if (fs.size() == 0) return fs;
		
		List<Integer> indxs = new ArrayList<>();
		
		switch (editing.readMode) {
		/*case ASSEMBLY: // muito menos aqui
			break;*/
		
		case BIN:
		case BINARY:
			/*indxs = findWord(new String(chars), "|");
			
			System.out.println(indxs);
			
			fs = color(0, indxs.get(0) - 1, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
			fs = color(indxs.get(0), indxs.get(0) + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);*/
			
			fs = color(0, fs.size(), new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
			break;
			
		case HEX:
			indxs = findWord(new String(chars), "|");
			
			fs = color(0, indxs.get(0) - 1, new IDEFont(Fonts.numbersEditor, FONT_SIZE), fs);
			fs = color(indxs.get(0), indxs.get(0) + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
			break;
			
		case NORMAL: // n�o deve cair aqui
			break;
			
		default:
			break; // nem aqui
		
		}
		
		return fs;
	}
	
	public List<IDEFont> colorWhitespaces(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		indxs = findWord(new String(chars), " "); // colorir espa�os

		for (Integer i : indxs) {
			fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
		}
		
		indxs = findWord(new String(chars), "\t"); // colorir tabs

		for (Integer i : indxs) {
			fs = color(i, i + 1, new IDEFont(Fonts.symbolsEditor, FONT_SIZE), fs);
		}
		
		return fs;
	}

	public void resetHTML(char[] chars) {
		List<Integer> indxs = new ArrayList<>();

		indxs = findWord(new String(chars), "</style");

		if (indxs.size() > 0)
			isCssPart = false;

		indxs = findWord(new String(chars), "</script");

		if (indxs.size() > 0)
			isJSPart = false;

		indxs = findWord(new String(chars), "?>");

		if (indxs.size() > 0)
			isPhpPart = false;
		
		indxs = findWord(new String(chars), "%}");

		if (indxs.size() > 0)
			isPhpPart = false;
	}
	
	public void setExtType(String ext) {
		extType = "";
		foundExt = false;
		
		if (ListableFile.fileHasExtension(ext))
			extType = getLowerBarFileName(ext);
		else
			extType = getLowerBarFileNameWithoutExtension(editing.getRegent().getRegent().getName());
		
		if ((isReadOnly || editing.isReadOnly) && !extType.contains("(" + Texts.readOnly + ")"))
			extType += " (" + Texts.readOnly + ")";
	}
	
	public List<IDEFont> colorHTMLTags(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".xhtml") || ext.equalsIgnoreCase(".svelte") || ext.equalsIgnoreCase(".htm")
				|| ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".xml") || ext.equalsIgnoreCase(".svg")
				|| ext.equalsIgnoreCase(".sln") || ext.equalsIgnoreCase(".config") || ext.equalsIgnoreCase(".cfg")
				|| ext.equalsIgnoreCase(".classpath") || ext.equalsIgnoreCase(".csproj")
				|| ext.equalsIgnoreCase(".project")) {
			for (String s : specialHtmlVariables) { // colorir tags
				indxs = findWord(new String(chars), s);
	
				for (Integer i : indxs) {
					if (((i - 1 > 0) && (chars[i - 1] == '_' || Character.isLetter(chars[i - 1])))
							|| ((i + s.length() < chars.length)
									&& (chars[i + s.length()] == '_' || Character.isLetter(chars[i + s.length()]))))
						continue;
	
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesEditor, FONT_SIZE), fs); // tem q dar
					// offset
				}
			}
	
			for (String s : tags) { // colorir tags
				indxs = findWord(new String(chars), s);
	
				for (Integer i : indxs) {
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsEditor, FONT_SIZE), fs); // tem q dar offset
				}
			}
		}
		
		return fs;
	}

	// my precious
	public List<IDEFont> automaticColor(char[] chars, String ext) {
		/*
		 * isMultilineCommenting = false;
		 * 
		 * isCssPart = false; isJSPart = false; isPhpPart = false;
		 * 
		 * isMultilineString = false; isAnotherIterationString = false;
		 */

		List<IDEFont> fs = new ArrayList<>();

		for (int i = 0; i < chars.length; i++)
			fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		
		if (!ListableFile.fileHasExtension(ext))
			ext = editing.getRegent().getRegent().getName();
		
		if (editing.readMode != FileReadMode.NORMAL) {
			fs = colorOtherModes(ext, chars, fs);
			
			return fs;
		}
		
		fs = colorNoExtensions(ext, chars, fs);

		if (editing == null)
			return fs;
		
		if ((isBinary(ext) || !isFormatSupported(ext)) || ext.equalsIgnoreCase(".setconf") && !(ext.equalsIgnoreCase(".ini")
				&& ext.equalsIgnoreCase(".make") && ext.equalsIgnoreCase(".mk") && ext.equalsIgnoreCase(".mak")
				&& editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") && ext.equalsIgnoreCase(".txt") && ext.equalsIgnoreCase(".log")
				&& editing.getRegent().getRegent().getName().equalsIgnoreCase("dockerfile"))) {
			fs = colorWhitespaces(ext, chars, fs);
			
			return fs;
		}
		
		if (ext.equalsIgnoreCase(".txt") || ext.equalsIgnoreCase(".log")) {
			fs = colorWhitespaces(ext, chars, fs);
			
			return fs;
		}

		/////////////////////////////////////////////////////

		fs = colorVariablesAndObjects(ext, chars, fs);
		fs = colorObjects(ext, chars, fs);
		fs = colorMethods(ext, chars, fs);
		fs = colorKeywords(ext, chars, fs);
		fs = colorNumbers(ext, chars, fs);
		fs = colorSymbols(ext, chars, fs);
		fs = colorHTMLTags(ext, chars, fs);
		fs = colorExtras(ext, chars, fs);
		fs = colorWhitespaces(ext, chars, fs);
		fs = colorComments(ext, chars, fs);

		/////////////////////////////////////////////////////

		resetHTML(chars);

		/*
		 * for (AutoComplete c : autocomplete) { for (AutoComplete d : autocomplete) {
		 * if (c.text.equals(d.text)) autocomplete.remove(c); } }
		*/

		return fs;
	}

	public static String capitalizeFirstLetter(String s) {
		char f = Character.toUpperCase(s.charAt(0));
		String c = f + s.substring(1);

		return c;
	}

	public boolean hasAfter(String s, int initialIndex, char target) {
		for (int i = initialIndex; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == target)
				return true;
		}

		return false;
	}

	public boolean hasBefore(String s, int initialIndex, char target) {
		for (int i = initialIndex; i > 0; i--) {
			char c = s.charAt(i);

			if (c == target)
				return true;
		}

		return false;
	}
	
	public int howManyBefore(String s, int initialIndex, char target) {
		int count = 0;
		
		for (int i = initialIndex; i > 0; i--) {
			char c = s.charAt(i);

			if (c == target)
				count++;
		}

		return count;
	}
	
	public int howManyAfter(String s, int initialIndex, char target) {
		int count = 0;
		
		for (int i = initialIndex; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == target)
				count++;
		}

		return count;
	}

	public boolean isBetween(String s, int index, char charBefore, char charAfter) {
		return hasBefore(s, index, charBefore) && hasAfter(s, index, charAfter);
	}

	/**
	 * Conta quantos caracteres {@code c} tem na String {@code str}. Adicional:
	 * conta desde o come�o at� n�o ter mais daquele char
	 * 
	 * @param str - A String que vai contar
	 * @param c   - O caractere que vai ser contado
	 * @return O n�mero de vezes que o caractere {@code c} aparece na String
	 *         {@code str}.
	 */
	public static int countChar(String str, char c) { // Fonte: StackOverflow, de novo :/
		int count = 0;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != c)
				break;

			if (str.charAt(i) == c)
				count++;
		}

		return count;
	}
	
	public static int countAbsoluteChar(String str, char c) { // Fonte: StackOverflow, de novo :/
		int count = 0;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c)
				count++;
		}

		return count;
	}

	public static char[] toCharArray(List<Character> list) {
		if (list.size() == 0)
			return new char[0];

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
	 * Esse m�todo faz a fun��o que muitos editores de c�digo fazem: adicionar
	 * caracteres inteligentes. Se voc� digita '{', ele completa com '}', o mesmo
	 * vale para '[', '<', '"' (aspas duplas) e ' ' ' (aspas simples).
	 * 
	 * @param pre - O {@code StringBuilder} anterior, a base.
	 * @return O {@code StringBuilder} anterior com as modifica��es.
	 */
	private StringBuilder addCodeHelps(StringBuilder pre) {
		switch (KeyInput.getCharPressed()) {
		case '{':
			if (pre.length() == 0 || cursorX == pre.length())
				pre.append('}');
			else
				pre.insert(cursorX + 1, '}');
			break;

		case '(':
			if (pre.length() == 0 || cursorX == pre.length())
				pre.append(')');
			else
				pre.insert(cursorX + 1, ')');
			break;

		case '[':
			if (pre.length() == 0 || cursorX == pre.length())
				pre.append(']');
			else
				pre.insert(cursorX + 1, ']');
			break;

		case '<':
			if (!(ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".ejs")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".cfg")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".config")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".xml")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".sln")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".svg")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".classpath")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".csproj")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".project")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".htm")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".html")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".svelte")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".xhtml")))
				if (cursorX > 0 && !Character
						.isLetter(new String(toCharArray(lines.get(cursorY - 1).getChars())).charAt(cursorX - 1)))
					return pre;

			if (pre.length() == 0 || cursorX == pre.length())
				pre.append('>');
			else
				pre.insert(cursorX + 1, '>');
			break;

		case '"':
			if (pre.length() == 0 || cursorX == pre.length())
				pre.append('"'); // arrumar uns bug ae
			else
				pre.insert(cursorX + 1, '"');
			break;

		case 39: // -> ( ' ) Aspas Simples
			if (ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".cmxa") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".ml") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".mli") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".mly") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".clt")) break;
			
			if (pre.length() == 0 || cursorX == pre.length())
				pre.append((char) 39);
			else
				pre.insert(cursorX + 1, (char) 39);
			break;
		}

		return pre;
	}

	public void setCursorWithinBounds() { // o cursorY deve ser feito primeiro
		if (editing == null)
			return;

		try {
			if (cursorY < 1)
				cursorY = 1;
			if (cursorY + 1 > lines.size())
				cursorY = lines.size();

			if (cursorX < 0)
				cursorX = 0;
			if (cursorX > lines.get(cursorY - 1).getChars().size())
				cursorX = lines.get(cursorY - 1).getChars().size();
		} catch (Exception e) {
		}
	}

	private int setWithinBounds(int x, int y, boolean isX) {
		try {
			if (isX) {
				if (y < 1)
					y = 1;
				if (y + 1 > lines.size())
					y = lines.size();

				if (x < 0)
					x = 0;
				if (x > lines.get(y - 1).getChars().size())
					x = lines.get(y - 1).getChars().size();

				return x;
			} else {
				if (y < 1)
					y = 1;
				if (y + 1 > lines.size())
					y = lines.size();

				return y;
			}
		} catch (Exception e) {
			return x;
		}
	}

	private StringBuilder write(StringBuilder cY, char c) {
		if (c < 32 || c > 1000) {
			cursorX--; // esse � o m�todo gambiarrento, mas depois pode arrumar (ou n�o kkkkk)

			return cY;
		}

		if (cY.length() == 0)
			cY.append(c);
		else if (cursorX <= cY.length())
			cY.insert(cursorX, c); // use <= pq se digitar no �ltimo n digita pq n bate
									// com a condi��o mas mesmo assim aumenta o cursorX e quando d�
									// o backspace excede o tamanho da linha e d� no que d� n�
		return cY;
	}

	public void register(StringBuilder cY, int y) { // cY = cursorY
		String gs = cY.toString(); // gen string
		char[] ca = gs.toCharArray(); // char array

		List<Character> lc = toCharList(ca); // list char (Esses coment�rios s�o para especificar os nomes das
												// vari�veis)

		lines.get(y).getChars().clear();
		lines.get(y).getFonts().clear();

		for (Character c : lc) {
			lines.get(y).getChars().add(c);
			lines.get(y).getFonts().add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		}
	}

	public char addAccents(int keyCode, char ch) {
		boolean capsLock = Main.toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

		if (!pressedAccent) {
			if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown()) { // ^ Circunflexo
				prAcc = PressedAccent.CIRCUMFLEX;
				pressedAccent = true;

				return ch;
			} else if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown()) { // ` Crase
				prAcc = PressedAccent.BACK_QUOTE;
				pressedAccent = true;

				return ch;
			} else if (keyCode == KeyEvent.VK_DEAD_ACUTE) { // � Acento Agudo
				prAcc = PressedAccent.ACUTE;
				pressedAccent = true;

				return ch;
			} else if (keyCode == KeyEvent.VK_DEAD_TILDE) { // ~ Til
				prAcc = PressedAccent.TILDE;
				pressedAccent = true;

				return ch;
			}

			else if (keyCode == 168) { // � Trema
				prAcc = PressedAccent.UMLAUT;
				pressedAccent = true;

				// System.out.println(prAcc);

				return ch;
			}
		}

		if (pressedAccent && !(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL)) {
			pressedAccent = false;

			switch (prAcc) {
			case ACUTE:
				if (ch == 'A' || (capsLock && ch == 'a'))
					return 193;
				if (ch == 'E' || (capsLock && ch == 'e'))
					return 201;
				if (ch == 'I' || (capsLock && ch == 'i'))
					return 205;
				if (ch == 'O' || (capsLock && ch == 'o'))
					return 211;
				if (ch == 'U' || (capsLock && ch == 'u'))
					return 218;
				if (ch == 'Y' || (capsLock && ch == 'y'))
					return 221;

				if (ch == 'a')
					return 225;
				if (ch == 'e')
					return 233;
				if (ch == 'i')
					return 237;
				if (ch == 'o')
					return 243;
				if (ch == 'u')
					return 250;
				if (ch == 'y')
					return 253;

				if (keyCode == KeyEvent.VK_DEAD_ACUTE)
					return 180;
				break;
			case BACK_QUOTE:
				if (ch == 'A' || (capsLock && ch == 'a'))
					return 192;
				if (ch == 'E' || (capsLock && ch == 'e'))
					return 200;
				if (ch == 'I' || (capsLock && ch == 'i'))
					return 204;
				if (ch == 'O' || (capsLock && ch == 'o'))
					return 210;
				if (ch == 'U' || (capsLock && ch == 'u'))
					return 217;

				if (ch == 'a')
					return 224;
				if (ch == 'e')
					return 232;
				if (ch == 'i')
					return 236;
				if (ch == 'o')
					return 242;
				if (ch == 'u')
					return 249;

				if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown())
					return 96;
				break;
			case CIRCUMFLEX:
				if (ch == 'A' || (capsLock && ch == 'a'))
					return 194;
				if (ch == 'E' || (capsLock && ch == 'e'))
					return 202;
				if (ch == 'I' || (capsLock && ch == 'i'))
					return 206;
				if (ch == 'O' || (capsLock && ch == 'o'))
					return 212;
				if (ch == 'U' || (capsLock && ch == 'u'))
					return 219;

				if (ch == 'a')
					return 226;
				if (ch == 'e')
					return 234;
				if (ch == 'i')
					return 238;
				if (ch == 'o')
					return 244;
				if (ch == 'u')
					return 251;

				if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown())
					return 94;
				break;
			case TILDE:
				if (ch == 'A' || (capsLock && ch == 'a'))
					return 195;
				if (ch == 'O' || (capsLock && ch == 'o'))
					return 213;
				if (ch == 'N' || (capsLock && ch == 'n'))
					return 209;

				if (ch == 'a')
					return 227;
				if (ch == 'o')
					return 245;
				if (ch == 'n')
					return 241;

				if (keyCode == KeyEvent.VK_DEAD_TILDE)
					return 126;
				break;

			case UMLAUT:
				if (ch == 'A' || (capsLock && ch == 'a'))
					return 196;
				if (ch == 'E' || (capsLock && ch == 'e'))
					return 203;
				if (ch == 'I' || (capsLock && ch == 'i'))
					return 207;
				if (ch == 'O' || (capsLock && ch == 'o'))
					return 214;
				if (ch == 'U' || (capsLock && ch == 'u'))
					return 220;
				// if (ch == 'Y' || (capsLock && ch == 'y')) return '�Y';

				if (ch == 'a')
					return 228;
				if (ch == 'e')
					return 235;
				if (ch == 'i')
					return 239;
				if (ch == 'o')
					return 246;
				if (ch == 'u')
					return 252;
				if (ch == 'y')
					return 255;

				if (keyCode == 168)
					return 168;
				break;
			}
		}

		return ch;
	}

	public static List<Character> delete(int start, int end, List<Character> list) {
		List<Character> result = new ArrayList<>();

		result.addAll(list.subList(0, start));
		result.addAll(list.subList(end, list.size()));

		return result;
	}

	public void paste() { // terminar o paste com mais de uma linha
		if (editing == null)
			return;

		CommandTerminal.runCommand("del"); // se colar deleta o que t� selecionado

		String[] sp = clipboard.split("\n");

		int index = 0;

		if (sp.length == 1) { // se � s� uma linha
			for (String s : sp) {
				StringBuilder b = new StringBuilder(new String(toCharArray(lines.get((cursorY - 1)).getChars())));
				StringBuilder c = b;

				b.insert(cursorX, s);

				if (!c.equals(b))
					return;

				register(b, (cursorY - 1) + index);

				cursorX += s.length();
			}
		} else { // se n�o � s� uma linha
			for (String s : sp) {
				if (s != sp[0])
					lines.add((cursorY - 1) + index, new IDELine(new ArrayList<>(), new ArrayList<>()));

				StringBuilder b = new StringBuilder(
						new String(toCharArray(lines.get((cursorY - 1) + index).getChars())));
				StringBuilder c = b;

				int x = cursorX > lines.get((cursorY - 1) + index).getChars().size()
						? lines.get((cursorY - 1) + index).getChars().size()
						: cursorX; // n�o pode exceder o index

				b.insert(x, s);

				register(b, (cursorY - 1) + index);

				if (!c.equals(b))
					return;

				if (s == sp[sp.length - 1]) {
					cursorX += s.length();
					cursorY += sp.length - 1;
				}

				index++;
			}
		}
		
		//CommandTerminal.runCommand("gotocursor");
		
		setCursorWithinBounds();
		editing.setSaved(false);
	}

	public void addNewLine(int yPos) {
		List<Character> chars = new ArrayList<>();
		List<IDEFont> fs = new ArrayList<>();

		//chars.add('\0');
		fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));

		lines.add(yPos, new IDELine(chars, fs));
	}

	public void addNewLine(int yPos, String initialText) {
		List<Character> chars = new ArrayList<>();
		List<IDEFont> fs = new ArrayList<>();

		char[] arr = initialText.toCharArray();

		for (char c : arr) {
			chars.add(c);
			fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		}

		lines.add(yPos, new IDELine(chars, fs));
	}

	public static void setSystemLook() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public static void execTerminal() {
		terminal = new CommandTerminal(Screen.WIDTH / 2 - 250, Screen.DECORATION_HEIGHT + 40 /*30*/, 500, 30); // 25

		if (CommandTerminal.active)
			return;

		CommandTerminal.active = true;
		IDEComponent.toAdd.add(terminal);
	}

	public void execute(String arg) {
		switch (arg) {
		case "cmd":
			try {
				boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

				ProcessBuilder pb = null;

				if (isWindows)
					pb = new ProcessBuilder("cmd", "/c", "start");
				else
					pb = new ProcessBuilder("/usr/bin/xterm");

				File dir = Explorer.scope != null ? Explorer.scope.getRegent() : Main.baseFolder; // eu tava fazendo o
																									// equivalente a
																									// isso: null.regent
																									// != null

				pb.directory(dir);
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case "paste":
			paste();
			break;

		case "setbase":
			Main.baseFolder = new File(Explorer.getScopePath());
			Explorer.folderPath = "";

			ListableFile.files = ListableFile.loadFolder(null);

			break;

		case "opendef":
			new Thread("open default program") {
				public void run() {
					try {
						Main.desktop.open(editing.getRegent().getRegent());
					} catch (Exception e) {
						setSystemLook();

						JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound,
								JOptionPane.OK_OPTION);
					}
				}
			}.start();
			break;

		case "save":
			if (editing == null)
				return;

			editing.save();
			break;

		/*case "clr":
			if (editing == null)
				return;

			lines.get(cursorY - 1).getChars().clear();
			lines.get(cursorY - 1).getFonts().clear();

			editing.setSaved(false);

			setCursorWithinBounds();
			break;*/

		case "sysexp":
			try {
				if (Main.baseFolder == null)
					return;

				String path = null;

				try {
					path = editing == null ? Explorer.files.get(0).getRegent().getPath()
							: editing.getRegent().getRegent().getPath();
				} catch (Exception e) { // caiu aqui mt provavelmente � pq n�o tem itens no explorer
					if (editing != null)
						path = editing.getRegent().getRegent().getPath();
					else
						path = Explorer.getScopePath();
				}

				Main.desktop.open(new File(path).getParentFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case "term":
			execTerminal();
			break;

		case "newfile":
			int y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;
			
			Explorer.setFileName = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, true);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(Explorer.setFileName);
			break;

		case "newfolder":
			y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;
			
			Explorer.setFileName = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, false);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(Explorer.setFileName);
			break;

		case "searchrep":
			if (editing == null || CommandTerminal.expOff)
				return; // Vai modificar o que n�o existe?

			RightClickOption.removeAllRightClickOptions(); // arrumar o neg�cio
			
			Explorer.searchReplaceActive = true;
			SearchReplaceCore.init();
			
			/*if (!alreadyAddedFrame) {
				searchWindow = new SearchReplaceWindow();
				alreadyAddedFrame = true;
			} else {
				searchWindow.setState(Frame.NORMAL);
				searchWindow.requestFocus();

				searchWindow.txbSearch.requestFocus();
			}*/

			break;
		}
	}

	public void verifyDuplicateTabs() { // continuar segundo o TODO
		try {
			if (tabs == null || tabs.size() == 0)
				return;

			for (int i = 0; i < tabs.size(); i++)
				for (int j = 0; j < tabs.size(); j++) {
					Tab tabi = tabs.get(i);
					Tab tabj = tabs.get(j);

					if (tabi.getRegent().getRegent().getAbsolutePath()
							.equals(tabj.getRegent().getRegent().getAbsolutePath()) && tabi != tabj) {
						tabi.close();

						return;
					}
				}
		} catch (Exception e) {
		}
	}

	/**
	 * Faz a conta de Regra de Tr�s, com os n�meros dados no argumento.
	 * 
	 * <br />
	 * 
	 * Pode-se pensar nessa conta da seguinte maneira: se o n�mero em a equivale, na
	 * mesma propor��o, ao n�mero em b, se der um n�mero em c, quantos ser� o n�mero
	 * em d?
	 * 
	 * @param a - o n�mero 1
	 * @param b - o n�mero 2
	 * @param c - o n�mero 3
	 * @return O resultado, como se fosse a letra d dos argumentos
	 */
	public static int ruleOf3(int a, int b, int c) {
		return (b * c) / a;
	}

	/**
	 * Faz a conta de Regra de Tr�s Inversa, com os n�meros dados no argumento.
	 * 
	 * <br />
	 * 
	 * Pode-se pensar nessa conta da seguinte maneira: se o n�mero em a equivale, na
	 * propor��o inversa, ao n�mero em b, se der um n�mero em c, quantos ser� o
	 * n�mero em d?
	 * 
	 * @param a - o n�mero 1
	 * @param b - o n�mero 2
	 * @param c - o n�mero 3
	 * @return O resultado, como se fosse a letra d dos argumentos
	 */
	public static int inverseRuleOf3(int a, int b, int c) {
		return (a * b) / c;
	}

	public static String arrayToStr(String[] array) {
		StringBuilder result = new StringBuilder();

		for (String s : array)
			result.append(s);

		return result.toString();
	}

	public static int pxToPt(int px) {
		return ruleOf3(16, 12, px);
	}

	/*
	 * public static int ptToPx(int pt) { return ruleOf3(12, 16, pt); }
	 */

	/**
	 * <!-- Slk merm�o vc pode estilizar com css --> <style> pre { font-family:
	 * "Calibri"; font-size: 15px; } </style>
	 * 
	 * Gera um Lorem Ipsum aleat�rio a partir das palavras do array, com pontua��o e
	 * tudo.
	 * 
	 * <pre>
	 * Ele suporta somente um par�grafo.
	 * </pre>
	 * 
	 * @param numWords - O n�mero de palavras no total que o texto vai ter.
	 * @return O texto gerado.
	 */
	public static String generateLoremIpsum(int numWords) {
		String[] points = { ". ", ", ", ", " }; // tem mais chances de ser , do que . (a cada 2 , ocorre 1 .)

		Random rd = new Random();

		boolean capitalize = false;
		String initialText = "Lorem ipsum dolor sit amet ";

		StringBuilder bl = new StringBuilder(initialText);

		for (int i = 0; i < numWords; i++) {
			String word = capitalize ? capitalizeFirstLetter(loremWords[rd.nextInt(loremWords.length)])
					: loremWords[rd.nextInt(loremWords.length)];

			bl.append(word + (i == numWords - 1 ? "." : ""));

			capitalize = false;

			if (rd.nextInt(100) < 25 && i < numWords - 1) { // 25% de pontuar
				// pontuar!
				String point = points[rd.nextInt(points.length)];

				bl.append(point);

				if (point.contains("."))
					capitalize = true;
			} else
				bl.append(" ");
		}

		return bl.toString();
	}

	/*
	 * public static String mergeStringArrays(String[]... arrays) { StringBuilder bl
	 * = new StringBuilder();
	 * 
	 * for (int i = 0; i < arrays.length; i++) { for (int j = 0; j <
	 * arrays[i].length; j++) { bl.append(arrays[i][j] + " "); } }
	 * 
	 * return bl.toString(); }
	 */

	public static List<Character> toListChar(char[] ch) {
		List<Character> list = new ArrayList<>();

		for (char c : ch)
			list.add(c);

		return list;
	}
	
	public void restartVariables() {
		isMultilineCommenting = false;
		isCssPart = false;
		isJSPart = false;
		isPhpPart = false;
	}

	public synchronized void callAutomaticColor() {
		try {
			new Thread("automaticColor method call") {
				public void run() {
					try {
						if (editing == null)
							return;
						
						int i = 0;
						for (IDELine l : lines) {
							int yr = MIN_Y + (i++ * (LINE_HEIGHT)) - scrY;
							
							if (yr < 0 || yr > Main.screen.getHeight())
								continue;
							
							l.setFonts(automaticColor(toCharArray(l.getChars()),
									ListableFile.getFileExtension(editing.getRegent().getRegent())));
	
						}
						restartVariables();
					} catch (Exception e) {
						return;
					}
				}
			}.start();
			
		} catch (Exception e) {
			return;
		}
	}

	/*
	 * Pseudo-C�digo -- FUNCIONOU!
	 * 
	 * deletar os chars atr�s do cursor, at� o tamanho da palavra digitada ex: cx =
	 * 7, pld = 3 | cx = 7 - 3. // cx = cursorx, pld = palavra digitada
	 * 
	 * depois d� um insert na string e
	 * 
	 */
	public void makeChanges(String e) { // [e] � a palavra que vai colocar
		String s = new String(toCharArray(lines.get(cursorY - 1).getChars()));
		StringBuilder sb = new StringBuilder(s);

		sb.delete(cursorX - wordSinceSpace.length(), cursorX);
		cursorX -= wordSinceSpace.length();
		sb.insert(cursorX, e);

		cursorX += e.length();

		register(sb, cursorY - 1);

		callAutomaticColor();
		setCursorWithinBounds();
	}

	/**
	 * Hardcoded no cursor (de texto, n�o � do mouse n�o)
	 */
	public void addAutoCompleteOptions() {
		addSpecificAutoCompletes(editing.getRegent().getRegent().getName());

		// if (autocomplete.isEmpty()) return;

		if (!isAutoCompleteActive) {
			// autocomplete.clear();
			autocomplete.clear();

			return;
		}

		RightClickOption.removeAllRightClickOptions();

		int index = 0;

		int height = 30;
		// ruleOf3(16, 30, FONT_SIZE);
		
		autocomplete = removeDuplicates(autocomplete);

		for (AutoComplete a : autocomplete) {
			if (a == null)
				continue;

			String change = a.text;
			
			toAddAutoCompletes.add(new RightClickOption(drawcx + (Main.editor.getX() - originalEditorX),
					(drawcy + FONT_SIZE /* + 2 */) + index * height, 330, 32, 16, a.text, getAutoCompleteIcon(a.type),
					(e) -> makeChanges(e), change));

			index++;
		}

		// autocomplete.clear();
		autocomplete.clear();
	}

	public void addSpecificAutoCompletes(String filename) {
		filename = ListableFile.fileHasExtension(filename) ? ListableFile.getFileExtension(filename) : filename;

		// pode colocar tudo no mesmo switch, tipo .asm e makefile podem ser colocados
		// juntos

		switch (filename.toLowerCase()) {
		case ".s":
		case ".asm":
			Set<AutoComplete> asm = new LinkedHashSet<>();

			for (String s : asmRegs) {
				if (s.contains(wordSinceSpace))
					asm.add(new AutoComplete(s, AutoCompleteType.VARIABLE));
			}

			for (String s : sections) {
				if (s.contains(wordSinceSpace))
					asm.add(new AutoComplete(s, AutoCompleteType.VARIABLE));
			}

			autocomplete.addAll(asm);

			break;

		case ".scss":
		case ".css":
			Set<AutoComplete> css = new LinkedHashSet<>();

			for (String s : units) {
				if (s.contains(wordSinceSpace))
					css.add(new AutoComplete(s, AutoCompleteType.VARIABLE));
			}

			autocomplete.addAll(css);

			break;
		}
	}

	private StringBuilder addExtraCode(StringBuilder cY, char digit) {
		if (!codeHelpersOn) return cY;
		
		char[] chars = cY.toString().toCharArray();
		
		if (editing != null)
			if ((ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".html")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".xhtml")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".svelte")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".htm")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".ejs")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".xml")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".svg")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".sln")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".config")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".cfg")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".classpath")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".csproj")
					|| ListableFile.getFileExtension(editing.getRegent().getRegent())
							.equalsIgnoreCase(".project"))) {
				if (cursorX > 0 && chars[cursorX] == '>' && digit == '>') { // isso tem um bug mt chato que quando apaga a tag de fechar e tenta por de novo estraga td, talvez adicionar um keystroke que se apertado ele n detecta
					if (cursorX > 0 && lines.get(cursorY - 1).getChars().get(cursorX - 1) == '>') return cY;
					
					List<Integer> indxs = findWord(new String(chars), "<"); // antes de <palavra>
					cursorX++;
					
					for (Integer i : indxs) {
						int c = i;
						int len = 0;
			
						while (c < chars.length && c + len < chars.length && c > 0) {
							c--;
							len++;
						}
			
						// c, c + len
			
						len = 0;
			
						while (c + len < chars.length - 1)
							len++;
			
						if (chars[c + len] == ' ' || chars[c + len] == '>') {
							char[] tagArray = Arrays.copyOfRange(chars, c, c + len);
			
							String tagStr = new String(tagArray).replaceAll(" ", "").substring(1);
			
							tagStr = "</" + tagStr + ">"; // o fechamento da outra
			
							tagArray = tagStr.toCharArray();
			
							for (char ch : tagArray) {
								cY = write(cY, ch);
			
								cursorX++;
							}
			
							cursorX++;
						}
					}
				}
			}
		
		return cY;
	}
	
	public void type() {
		if (!isReadOnly) {
			KeyInput.updateKeys();
			callAutomaticColor();

			StringBuilder cY = null;

			try {
				cY = new StringBuilder(new String(toCharArray(lines.get(cursorY - 1).getChars())));
			} catch (Exception e) {
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
				KeyInput.updateKeys();
				addToUndo();
				
				CommandTerminal.runCommand("gotocursor");

				RightClickOption.removeAllRightClickOptions();

				if (wordSinceSpace.length() > 0)
					wordSinceSpace = wordSinceSpace.substring(0, wordSinceSpace.length() - 1);
				else
					wordSinceSpace = "";

				if (selecting) {
					CommandTerminal.runCommand("del");

					return;
				} else {
					if (cursorX > 0) {
						cY.deleteCharAt(cursorX - 1);

						cursorX--;

						setCursorWithinBounds();

						editing.setSaved(false);

						register(cY, cursorY - 1);
					} else if (cursorY > 1) {
						String s = cY.toString();

						cursorX = lines.get(cursorY - 2).getChars().size();

						lines.remove(cursorY - 1);
						cursorY--;

						cY = new StringBuilder(new String(toCharArray(lines.get(cursorY - 1).getChars())));

						cY.append(s);

						editing.setSaved(false);

						register(cY, cursorY - 1);
					}

					return;
				}
			}

			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				KeyInput.updateKeys();
				 addToUndo();
				 
				 CommandTerminal.runCommand("gotocursor");

				if (cursorX < cY.length()) {
					cY.deleteCharAt(cursorX);

					setCursorWithinBounds();

					editing.setSaved(false);

					register(cY, cursorY - 1);
				}

				return;
			}

			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB) {
				KeyInput.updateKeys();
				 addToUndo();
				 
				 CommandTerminal.runCommand("gotocursor");
				 
				 if (!KeyInput.isShiftDown()) {
					 if (!RightClickOption.isAutoCompleteActive()) {
						wordSinceSpace = "";
						
						RightClickOption.removeAllRightClickOptions();
						
						String indentation = "\t";
						
						if (indentSpaces) {
							indentation = "";
							StringBuilder b = new StringBuilder();
							
							for (int i = 0; i < indentLength; i++) {
								b.append(' ');
							}
							
							indentation = b.toString();
						}
						
						cY.insert(cursorX, indentation);
	
						cursorX += indentSpaces ? indentLength : 1;
						editing.setSaved(false);
					} else {
						autocompleteindex++;
	
						if (autocompleteindex == autocompletes.size()) {
							autocompleteindex = 0;
							autocompletescroll = 0;
						}
						
						if (autocompletes.get(autocompleteindex).getY() >= height)
							autocompletescroll += 90;
					}
				 } else {
					 wordSinceSpace = "";
						RightClickOption.removeAllRightClickOptions();
						
						String indentation = "\t";
						
						if (!indentSpaces) {
							indentation = "";
							StringBuilder b = new StringBuilder();
							
							for (int i = 0; i < indentLength; i++) {
								b.append(' ');
							}
							
							indentation = b.toString();
						}
						
						cY.insert(cursorX, indentation);
	
						cursorX += !indentSpaces ? indentLength : 1;
						
						editing.setSaved(false);
				 }
			}

			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				KeyInput.updateKeys();
				 addToUndo();
				 
				 CommandTerminal.runCommand("gotocursor");

				if (RightClickOption.isAutoCompleteActive()) {
					autocompletes.get(autocompleteindex).command
							.execute(autocompletes.get(autocompleteindex).clickArg);

					RightClickOption.removeAllRightClickOptions();

					return;
				}

				wordSinceSpace = "";
				RightClickOption.removeAllRightClickOptions();

				StringBuilder spaces = new StringBuilder();
				String s = cY.substring(cursorX);

				for (int i = 0; i < countChar(cY.toString(), ' '); i++)
					spaces.append(' ');

				for (int i = 0; i < countChar(cY.toString(), (char) 9); i++) // char 9 � o tab
					spaces.append('\t');

				int nSpaces = spaces.length();

				spaces.append(s);
				s = spaces.toString();

				cY.delete(cursorX, cY.length());

				List<IDEFont> fs = new ArrayList<>();

				for (int i = 0; i < s.length(); i++)
					fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));

				lines.add(cursorY, new IDELine(toCharList(s.toCharArray()), fs));

				register(cY, cursorY - 1);

				editing.setSaved(false);

				cursorX = nSpaces;
				cursorY++;

				return;
			}

			if (KeyInput.getKeyCodePressed() != KeyEvent.VK_SHIFT) {
				KeyInput.updateKeys();
				
				int keyCode = KeyInput.getKeyCodePressed();
				char c = KeyInput.getCharPressed();
				
				c = addAccents(keyCode, c);
				cY = write(cY, c);
				
				if (!(c < 32 || c > 1000))
					wordSinceSpace += c;
				if (keyCode == KeyEvent.VK_SPACE) {
					wordSinceSpace = "";
					RightClickOption.removeAllRightClickOptions(); // aqui
				}
				
				if (editing != null)
					if (ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".html")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".xhtml")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".svelte")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".htm")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".ejs")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".xml")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".svg")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".sln")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".config")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".cfg")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".classpath")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".csproj")
							|| ListableFile.getFileExtension(editing.getRegent().getRegent())
									.equalsIgnoreCase(".project")) {
							if (c == '<') {
								wordSinceSpace = "";
								RightClickOption.removeAllRightClickOptions(); // aqui
							}
						}

				if (codeHelpersOn)
					cY = addCodeHelps(cY);
				
				register(cY, cursorY - 1);
				
				cY = addExtraCode(cY, c);
				register(cY, cursorY - 1);

				cursorX++;

				setCursorWithinBounds();
				
				// Add AutoComplete

				if ((Character.isLetter(c) || isNumber(c) || KeyInput.getCharPressed() == 46) && !isReadOnly
					&& editing != null) { // adicionar esse c�digo no backspace, e se
																	// tiver espa�os na frente, a keyword vai no
																	// lugar errado
					String[] autoc = ListableFile.fileHasExtension(editing.getRegent().getRegent())
							? getKeywords(ListableFile.getFileExtension(editing.getRegent().getRegent()))
							: getKeywordsSpecial(editing.getRegent().getRegent().getName());
					
					try {
						for (AutoComplete a : addautocomplete) {
							if (a.text.contains(wordSinceSpace)) {
								autocomplete.add(a);
							}
						}
					} catch (ConcurrentModificationException e) {} // remover duplicatas de textos

					if (autoc != null) {
						for (String s : autoc)
							if (s.contains(wordSinceSpace))
								autocomplete.add(new AutoComplete(s, AutoCompleteType.KEYWORD));
						
						autocompleteindex = 0;
						
						addAutoCompleteOptions();
					}
				}

				if (!Character.isLetter(c) && KeyInput.getKeyCodePressed() != KeyEvent.VK_TAB && KeyInput.getKeyCodePressed() != KeyEvent.VK_UP && KeyInput.getKeyCodePressed() != KeyEvent.VK_DOWN/* && KeyInput.getKeyCodePressed() != KeyEvent.VK_LEFT && KeyInput.getKeyCodePressed() != KeyEvent.VK_RIGHT*/
						&& KeyInput.getKeyCodePressed() != KeyEvent.VK_SPACE && KeyInput.getCharPressed() != 46
						&& !KeyInput.isShiftDown())
					RightClickOption.removeAllRightClickOptions(); // 46 � o ponto (.) // aqui
				// if (KeyInput.getKeyCodePressed() == KeyEvent.)

				if (!(KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256
						|| KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE)) {
					
					 addToUndo();
					 
					 CommandTerminal.runCommand("gotocursor");
					if (editing != null)
						editing.setSaved(false);
				}
			} // chama o automaticcolor aqui
		}
	}
	
	public void detectArrows() {
		if ((!(KeyInput.isAltDown() || KeyInput.isControlDown()) || KeyInput.isAltGrDown())) { // se ctrl, alt N�O est�o pressionados, ou se alt gr est� pressionado
			try {
				if (!RightClickOption.isRightClickActive()) {
					showCursor = true;
					
					if (!KeyInput.isShiftDown()) {
						if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							if (RightClickOption.isAutoCompleteActive()) {
								autocompleteindex--;
								
								if (autocompleteindex < 0) {
									autocompleteindex = 0;
									autocompletescroll = 0;
								}
								
								if (autocompletes.get(autocompleteindex).getY() <= y)
									autocompletescroll -= 90;
								
								return;
							}
							
							wordSinceSpace = "";
							
							if (cursorY == 1) cursorX = 0;
							
							cursorY--;

							if (selecting) {
								cursorX = index1;
								cursorY = line1;
							}

							CommandTerminal.runCommand("deselect");
							setCursorWithinBounds();

							return;
						}

						else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							if (RightClickOption.isAutoCompleteActive()) {
								autocompleteindex++;
								
								if (autocompleteindex == autocompletes.size()) {
									autocompleteindex = 0;
									autocompletescroll = 0;
								}
								
								if (autocompletes.get(autocompleteindex).getY() >= height)
									autocompletescroll += 90;
								
								return;
							}
							
							wordSinceSpace = "";
							
							if (cursorY == lines.size()) cursorX = lines.get(cursorY - 1).getChars().size();
							
							cursorY++;

							if (selecting) {
								cursorX = index2;
								cursorY = line2;
							}

							CommandTerminal.runCommand("deselect");
							setCursorWithinBounds();

							return;
						}

						if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							cursorX--;

							if (selecting) {
								cursorX = index1;
								cursorY = line1;
							}

							CommandTerminal.runCommand("deselect");
							setCursorWithinBounds();

							return;
						}

						else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							cursorX++;

							if (selecting) {
								cursorX = index2;
								cursorY = line2;
							}

							CommandTerminal.runCommand("deselect");
							setCursorWithinBounds();

							return;
						}
					} else { // isShiftDown()
						if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							if (noneSelected())
								directionStarted = Direction.UP;

							if (directionStarted != Direction.DOWN) // n�o verificar se foi up, verificar se n�o foi
																	// down, ou colocar um else e repetir a condi��o
																	// do if
								line1--;
							else// if (directionStarted == Direction.DOWN)
								line2--;

							selecting = true;

							line1 = setWithinBounds(index1, line1, false);

							return;
						}

						else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							if (noneSelected())
								directionStarted = Direction.DOWN;

							if (directionStarted != Direction.UP)
								line2++;
							else// if (directionStarted == Direction.UP)
								line1++;

							selecting = true;

							line2 = setWithinBounds(index2, line2, false);

							return;
						}

						if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							if (noneSelected())
								directionStarted = Direction.LEFT;

							if (directionStarted != Direction.RIGHT)
								index1--;
							else// if (directionStarted == Direction.RIGHT)
								index2--;

							selecting = true;

							index1 = setWithinBounds(index1, line1, true);

							return;
						}

						else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
							KeyInput.updateKeys();
							
							CommandTerminal.runCommand("gotocursor");
							
							wordSinceSpace = "";
							
							if (noneSelected())
								directionStarted = Direction.RIGHT;

							if (directionStarted != Direction.LEFT)
								index2++;
							else// if (directionStarted == Direction.LEFT)
								index1++;

							selecting = true;

							index2 = setWithinBounds(index2, line2, true);

							return;
						}
					}
				}
			} catch (Exception e) {
				CommandTerminal.runCommand("deselect");
			}
			
			//type();
		}
	}
	
	public void detectShortcuts() {
		// Detectar atalhos
		
		if (KeyInput.isAltDown()) { // BASE 1
			KeyInput.updateKeys();
			
			int indexSelected = tabs.indexOf(editing);
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && indexSelected > 0) {
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("selecttab " + (indexSelected - 1));
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT && indexSelected < tabs.size() - 1) {
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("selecttab " + (indexSelected + 1));
			}
		}
		
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
						KeyInput.updateKeys();

						RightClickOption.removeAllRightClickOptions();
						CommandTerminal.runCommand("deselect");

						return;
					}
					
					if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 61 ) { // 61 = + | Ctrl + Increase Font Size
						KeyInput.updateKeys();

						CommandTerminal.runCommand("setfontsize " + (FONT_SIZE + 1));

						return;
					}
					
					if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_MINUS ) { // Ctrl - Decrease Font Size
						KeyInput.updateKeys();
						
						CommandTerminal.runCommand("setfontsize " + (FONT_SIZE - 1));

						return;
					}
					
					if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME ) { // Ctrl
																																// +
																																// Home
																																// -
																																// Come�o
																																// do
																																// Documento
						KeyInput.updateKeys();

						scrX = 0;
						scrY = 0;

						cursorX = 0;
						cursorY = 1;

						setCursorWithinBounds();

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_END) { // Ctrl + End - Fim do Documento
						KeyInput.updateKeys();

						// scrX = (lines.get(lines.size() - 1).getChars().size() * FONT_SIZE) -
						// FONT_SIZE * 10; // esse - FONT_SIZE * 5 � pra dar um offset para tr�s e ficar
						// no meio da tela.

						cursorX = lines.get(lines.size() - 1).getChars().size();
						cursorY = lines.size();

						setCursorWithinBounds();
						
						CommandTerminal.runCommand("gotocursor");

						return;
					}

					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) { // Home - Come�o da Linha
						KeyInput.updateKeys();

						scrX = 0;
						cursorX = 0;

						setCursorWithinBounds();

						return;
					}

					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END) { // End - Fim da Linha
						KeyInput.updateKeys();

						// scrX = (lines.get(cursorY - 1).getChars().size() * FONT_SIZE) - FONT_SIZE *
						// 10;
						cursorX = lines.get(cursorY - 1).getChars().size();
						
						setCursorWithinBounds();
						CommandTerminal.runCommand("gotocursor");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_F) { // Ctrl
																																// +
																																// F
																																// -
																																// Abrir
																																// janela
																																// Localizar/Substituir
						KeyInput.updateKeys();

						execute("searchrep");

						return;
					}
					
					else if (Explorer.searchReplaceActive && Explorer.selected == null && KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Y) { // Ctrl + Shift + Y (Selecionar a caixa Search)
						KeyInput.updateKeys();
						
						Explorer.selected = Explorer.search;
						
						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_L && !isReadOnly
							) { // Ctrl + L - Deletar Linha
						KeyInput.updateKeys();

						CommandTerminal.runCommand("selectline");
						CommandTerminal.runCommand("del");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_J) { // Ctrl
																																// +
																																// J
																																// -
																																// Executar
						KeyInput.updateKeys();

						if (ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".bat")
								|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".com")
								|| ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".cmd"))
							editing.execute("run");
						else if (ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".sh") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".bash_profile") || ListableFile.getFileExtension(editing.getRegent().getRegent()).equalsIgnoreCase(".bashrc"))
							editing.execute("runbash");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_R && !isReadOnly
							) { // Ctrl + R - Refresh Auto Complete
						KeyInput.updateKeys();

						wordSinceSpace = "";

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.isShiftDown()
							&& KeyInput.getKeyCodePressed() == KeyEvent.VK_H) { // Ctrl + Shift + H - Toggle Read Only
						KeyInput.updateKeys();

						editing.save();

						CommandTerminal.runCommand("togglereadonly");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_D ) { // Ctrl
																																// +
																																// D
																																// ou
																																// Esc
																																// (Desselecionar)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("deselect");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_M ) { // Ctrl
																																// +
																																// M
																																// (Go
																																// To
																																// Cursor)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("gotocursor");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X && !isReadOnly
							) { // Ctrl + X (Cortar)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("cut");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_K) { // Ctrl + K (Alternar
																											// Explorador)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("toggleexplorer");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.isAltDown()
							&& KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + Alt + T (Fechar Todas as Abas)
						KeyInput.updateKeys();

						tabs.clear();
						editing = null;

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.isShiftDown()
							&& KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + T (Fechar Aba)
						KeyInput.updateKeys();

						editing.close();

						return;
					}
					
					/*
					 * else if (KeyInput.isControlDown() && KeyInput.isShiftDown() &&
					 * KeyInput.getKeyCodePressed() == KeyEvent.VK_R) { // Ctrl + Shift + R (Close
					 * Other Tabs) KeyInput.updateKeys();
					 * 
					 * execute("closeother");
					 * 
					 * return; }
					 */ // larga de m�o

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + T (Terminal)
						KeyInput.updateKeys();

						execute("term");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_B
							|| KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_WINDOWS) { // Ctrl + B OU
																													// Ctrl +
																													// Win (Cmd)
						KeyInput.updateKeys();

						execute("cmd");

						return;
					}

					else if (editing == null)
						return;

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_S && !isReadOnly
							) { // Ctrl + S (Salvar)
						KeyInput.updateKeys();

						editing.save();

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A
							) { // Ctrl + Shift + A (Selecionar Tudo)
						KeyInput.updateKeys();

						cursorX = 0;

						CommandTerminal.runCommand("selectline");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A ) { // Ctrl
																																// +
																																// A
																																// (Selecionar
																																// Linha)
						KeyInput.updateKeys();

						cursorX = 0;
						cursorY = 1;

						CommandTerminal.runCommand("selectall");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C ) { // Ctrl
																																// +
																																// C
																																// (Copiar)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("copy");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V && !isReadOnly
							) { // Ctrl + V (Colar)
						KeyInput.updateKeys();

						paste();

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE && !isReadOnly
							) { // Ctrl + Delete ou Backspace (Apenas Selecionando) (Deletar)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("del");

						return;
					}
					
					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_O && !isReadOnly) { // Ctrl + O - Toggle Whitespaces
						KeyInput.updateKeys();

						CommandTerminal.runCommand("togglewhitespaces");

						return;
					}

					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_P && !isReadOnly
							) { // Ctrl + P (Toggle Code Helpers)
						KeyInput.updateKeys();

						CommandTerminal.runCommand("togglecodehelpers");

						return;
					}

					// Lembrando que isso aqui s� ativa quando o que vc digitou est� dentro dos
					// conformes
					else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE && !isReadOnly
							) { // Ctrl + Space (Trigger Auto Complete)
						String[] autoc = ListableFile.fileHasExtension(editing.getRegent().getRegent())
								? getKeywords(ListableFile.getFileExtension(editing.getRegent().getRegent()))
								: getKeywordsSpecial(editing.getRegent().getRegent().getName());
						// Set<AutoComplete> autocc = autocomplete;

						// autocomplete.clear();
						autocomplete.clear();

						for (String s : autoc)
							if (s.contains(wordSinceSpace))
								autocomplete.add(new AutoComplete(s, AutoCompleteType.KEYWORD));

						/*
						 * for (AutoComplete c : autocc) else if (c.text.contains(wordSinceSpace))
						 * autocomplete.add(c);
						 */

						// autocomplete = removeDuplicates(autocomplete);
						// autocomplete = removeDuplicates(autocomplete);

						autocompleteindex = 0;

						addAutoCompleteOptions();

						return;
					}

					if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Z) { // Ctrl + Z (Desfazer)
						KeyInput.updateKeys();
						
						RightClickOption.removeAllRightClickOptions();
						
						if (undo.isEmpty()) return;
						
						List<IDELine> peek = peekUndo();
						redo.push(undo.pop());
						
						// define lines
						//System.out.println(peek.get(0).getFonts().isEmpty());
						Main.editor.lines = defineLines(peek);
						
						//System.out.println(peek.get(0).getChars());
						
						setCursorWithinBounds();
						editing.setSaved(false);
						
						return;
					}

					if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Y) { // Ctrl + Y (Refazer)
						KeyInput.updateKeys();
						
						RightClickOption.removeAllRightClickOptions();
						
						if (redo.isEmpty()) return;
						
						List<IDELine> peek = peekRedo();
						undo.push(redo.pop());
						
						this.lines = defineLines(peek);
						
						setCursorWithinBounds();
						editing.setSaved(false);
						
						return;
					}
				}
	
	public void scrollTabs() {
		if (MouseInput.hovered(x, 0, Main.screen.getWidth(), Tab.HEIGHT) && tabs != null && tabs.size() > 0) {
			if (MouseInput.isMouseRolling()) {
				if (MouseInput.wheelUp() && tabScr < 0) {
					MouseInput.updateMouseRoll();
					
					tabScr += 203; // 3 � a compensa��o para as tab n se distanciar
				}
				else if (MouseInput.wheelDown()
						&& (tabs.get(tabs.size() - 1).getX() + tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280)) { // 280
					MouseInput.updateMouseRoll();
					
					tabScr -= 203;
				}

				for (IDEComponent i : components) {
					if (i instanceof RightClickOption)
						IDEComponent.toRemove.add(i);
				}
			}
		}
	}
	
	public void scroll() {
		if (MouseInput.isMouseRolling()) { // resolver isso aqui
			//new Thread("scroll") {
				//public void run() {
					if (Main.editor.hovered()) {
						if (RightClickOption.isAutoCompleteActive()) {
							if (KeyInput.isControlDown() && KeyInput.isShiftDown()) {
								if (MouseInput.wheelUp() && autocompletes.get(0).getY() + 30 < (y + height) - 30) { // TODO aaaaaaaaaa
									MouseInput.updateMouseRoll();
									
									autocompletescroll -= 30;
								}
								else if (MouseInput.wheelDown()
										&& autocompletes.get(autocompletes.size() - 1).getY() > MIN_Y) {
									MouseInput.updateMouseRoll();
									
									autocompletescroll += 30;
								}
							}
						}

						if (KeyInput.isShiftDown() && !KeyInput.isControlDown()) { // isso n�o pode acontecer com o x por causa dos autocompletes
							if (MouseInput.wheelUp() && scrX > 0) {
								MouseInput.updateMouseRoll();
								
								scrX -= FONT_SIZE * 3;
							}
							else if (MouseInput.wheelDown()) {
								MouseInput.updateMouseRoll();
								
								scrX += FONT_SIZE * 3;
							}
						}

						if (!KeyInput.isShiftDown()) {
							if (!KeyInput.isControlDown()) {
								if (MouseInput.wheelUp() && scrY > 0) {
									MouseInput.updateMouseRoll();
									
									scrY -= (LINE_HEIGHT) * 3;
								}
								else if (MouseInput.wheelDown() && scrY + (LINE_HEIGHT) * 3 < lines.size()
										* (LINE_HEIGHT)) {
									MouseInput.updateMouseRoll();
									
									scrY += (LINE_HEIGHT) * 3;
								}
							}
							else {
								if (MouseInput.wheelUp() && scrY > 0) {
									MouseInput.updateMouseRoll();
									
									scrY -= (LINE_HEIGHT) * 6;
								}
								else if (MouseInput.wheelDown() && scrY + (LINE_HEIGHT) * 3 < lines.size() 
										* (LINE_HEIGHT)) {
									MouseInput.updateMouseRoll();
									
									scrY += (LINE_HEIGHT) * 6;
							}
							}
						}

						return;
					}
				//}
			//}.start();
		}
	}
	
	public List<IDELine> defineLines(List<IDELine> lines) {
		List<IDELine> ls = new ArrayList<>();
		
		for (IDELine l : lines) {
			List<Character> chs = new ArrayList<>(l.getChars());
			List<IDEFont> fnt = new ArrayList<>(l.getFonts());
			
			ls.add(new IDELine(chs, fnt));
		}
		
		return ls;
	}

	public boolean noneSelected() {
		return index1 == index2 && line1 == line2;
	}
	
	/*public synchronized void defineLines(List<IDELine> lines) {
		this.lines.clear();
		
		//this.lines.addAll(lines);
		
		int index = 0;
		
		for (IDELine l : lines) {
			addNewLine(index);
			register(new StringBuilder(new String(toCharArray(l.getChars()))), index++);
		}
	}*/
	
	public List<IDELine> getLines() {
		List<IDELine> ls = new ArrayList<>();
		
		for (IDELine l : lines)
			ls.add(l);
		
		return ls;
	}
	
	private void onClick() {
		Explorer.selected = null;
		
		if (selecting && leftClicked())
			CommandTerminal.runCommand("deselect");
		
		if (!RightClickOption.isRightClickActive() && !RightClickOption.isAutoCompleteActive() // TODO se quiser alterar o select, altere de leftclicked para dragged, e o cursor vai te seguir
				&& !MouseInput.hovered(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22)) {
			cursorX = mx;
			cursorY = my;
			
			wordSinceSpace = ""; // se n funcionar corre aqui e nas setas e deleta ta

			setCursorWithinBounds();
		}
		
		if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered()))) {
			int width = Main.lang == Language.PORT ? 550 : 510;
			List<RightClickOption> list = new ArrayList<>();
			
			list.add(new RightClickOption(0, 0, width, Texts.openCmd, (s) -> execute(s), "cmd"));
			list.add(new RightClickOption(0, 0, width, Texts.openTerminal, (s) -> execute(s), "term"));
			
			if (Main.baseFolder != null) {
				list.add(new RightClickOption(0, 0, width, Texts.openExplorer, (s) -> execute(s), "sysexp"));
				list.add(new RightClickOption(0, 0, width, Texts.setBaseFolder, (s) -> execute(s), "setbase"));
				
				if (editing != null) {
					list.add(new RightClickOption(0, 0, width, Texts.openDefault, (s) -> execute(s), "opendef"));
					
					list.add(new RightClickOption(0, 0, width, !Explorer.searchReplaceActive, Texts.open + " " + Texts.searchReplace, (s) -> execute(s), "searchrep"));
					list.add(new RightClickOption(0, 0, width, Texts.selectLine, (s) -> CommandTerminal.runCommand(s), "selectline"));
					list.add(new RightClickOption(0, 0, width, Texts.selectAll, (s) -> CommandTerminal.runCommand(s), "selectall"));
					
					if (!isReadOnly)
						list.add(new RightClickOption(0, 0, width, Texts.save, (s) -> execute(s), "save"));
					
					if (selecting) {
						list.add(new RightClickOption(0, 0, width, Texts.deselect, (s) -> CommandTerminal.runCommand(s), "deselect"));
						list.add(new RightClickOption(0, 0, width, Texts.copy, (s) -> CommandTerminal.runCommand(s), "copy"));
					}
					
					if (!isReadOnly) {
						list.add(new RightClickOption(0, 0, width, Texts.paste, (s) -> CommandTerminal.runCommand(s), "paste"));
						
						if (selecting) {
							list.add(new RightClickOption(0, 0, width, Texts.cut, (s) -> CommandTerminal.runCommand(s), "cut"));
							list.add(new RightClickOption(0, 0, width, Texts.delete, (s) -> CommandTerminal.runCommand(s), "del"));
						}
					}
				}
			}
			
			IDEComponent.addRightClickOptions(MouseInput.getMouseX(), MouseInput.getMouseY(), list.toArray(new RightClickOption[list.size()]));
		}
		
		if (MouseInput.isLeftPressed() || (KeyInput.isKeyPressed() && KeyInput.getKeyCodePressed() != KeyEvent.VK_BACK_SPACE) && ((cursorX != index1 && cursorY != line1) && (cursorX != index2 && cursorY != line2) && !RightClickOption.anyRightClickOptionHovered())) {
			if (Main.explorer.hovered() && !Explorer.searchReplaceActive)
				CommandTerminal.runCommand("deselect");
		}
	}
	
	public List<IDELine> peekUndo() {
		try {
			// pegar o peek real, como string
			String peek = undo.peek();
			
			// pegar a lista de objetos como strings
			List<String> objs = Serialization.deserializeList(peek);
			
			List<IDELine> output = new ArrayList<>();
			
			// deserializar cada objeto e colocar na lista output
			
			for (String s : objs) {
				IDELine l = (IDELine) Serialization.objectFromString(s);
				output.add(l);
			}
			
			return output;
		} catch (Exception e) {
			//System.out.println("Exception: " + Main.getStackTrace(e));
			return null;
		}
	}
	
	public List<IDELine> peekRedo() {
		try {
			// pegar o peek real, como string
			String peek = redo.peek();
			
			// pegar a lista de objetos como strings
			List<String> objs = Serialization.deserializeList(peek);
			
			List<IDELine> output = new ArrayList<>();
			
			// deserializar cada objeto e colocar na lista output
			
			for (String s : objs) {
				IDELine l = (IDELine) Serialization.objectFromString(s);
				output.add(l);
			}
			
			return output;
		} catch (Exception e) {
			//System.out.println("Exception: " + Main.getStackTrace(e));
			return null;
		}
	}
	
	public void addToUndo() {
		new Thread() {
			public void run() {
				try {
					// converter os objetos da lista em strings e colocar numa lista de strings, que s�o os objetos
					List<String> objs = new ArrayList<>();
					
					for (IDELine l : lines) {
						String s = Serialization.objectToString(l);
						objs.add(s);
					}
					
					// converter a lista de strings em uma string
					String list = Serialization.serializeList(objs);
					
					// colocar a string no undo
					undo.push(list);
				} catch (Exception e) {
					//System.out.println("Exception: " + Main.getStackTrace(e));
					return;
				} finally {
					if (undo.size() > MAX_UNDOS) {
						undo.remove(0);
					}
				}
			}
		}.start();
	}

	public void tick() {
		if (SetFileName.added || CommandTerminal.active || RenameFile.added)
			return;
		
		if (tabs == null)
			tabs = new ArrayList<>(); // fazer isso com os autocompletes, se necess�rio
		
		if (editing != null)
			isReadOnly = editing.isReadOnly;
		
		if (tabs.size() > 0)
			if (tabs.get(0).getX() + tabScr > x + 10)
				CommandTerminal.runCommand("resettabscroll"); // colocar no onmouseroll

		if (tabs.size() == 0)
			CommandTerminal.runCommand("resettabscroll");
		
		if (Main.editor.tabs.isEmpty())
			SearchReplaceCore.dispose();
		
		height = Main.screen.getHeight();
		LINE_HEIGHT = FONT_SIZE + (FONT_SIZE / 3);
		
		/*if (editing != null && !tabs.isEmpty() && tabs.indexOf(editing) < 0)
			tabs.get(0).select();*/
		
		setCursorWithinBounds();
		
		if (leftClicked() || rightClicked())
			onClick();
		
		callAutomaticColor(); // tem que ficar rodando
		
		width = Main.screen.getWidth() - x;
		
		// colocar isso numa variavel constante
		minMode = width < 800; // 850 - original, (selecting ? 800 : 600)
		
		// Scroll by Keyboard
		if (KeyInput.isKeyPressed() && KeyInput.isControlDown()) {
			if (!KeyInput.isShiftDown()) {
				if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP && scrY > 0)
					scrY -= (LINE_HEIGHT) * 3;
				else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN && scrY + (LINE_HEIGHT) * 3 < lines.size() * (LINE_HEIGHT))
					scrY += (LINE_HEIGHT) * 3;
				
				if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && scrX > 0)
					scrX -= (LINE_HEIGHT) * 3;
				else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT)
					scrX += (LINE_HEIGHT) * 3;
			}
			else {
				if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP && scrY > 0)
					scrY -= (LINE_HEIGHT) * 6;
				else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN && scrY + (LINE_HEIGHT) * 6 < lines.size() * (LINE_HEIGHT))
					scrY += (LINE_HEIGHT) * 6;
				
				if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && scrX > 0)
					scrX -= (LINE_HEIGHT) * 6;
				else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT)
					scrX += (LINE_HEIGHT) * 6;
			}
		}
		
		// Set Lower Bar values
		if (editing != null && editing.getRegent() != null) {
			switch (editing.readMode) {
			case BIN:
			case BINARY:
				codeType = minMode ? "Bin" : (Main.lang == Language.PORT ? "Bin�rio" : "Binary");
				break;
				
			case HEX:
				codeType = "Hex";
				break;
			/*case NORMAL:
				break;*/
			default:
				break;
			}
			
			if (ListableFile.fileHasExtension(ListableFile.getFileExtension(editing.getRegent().getRegent())))
				extType = getLowerBarFileName(ListableFile.getFileExtension(editing.getRegent().getRegent()));
			else
				extType = getLowerBarFileNameWithoutExtension(editing.getRegent().getRegent().getName());
			
			if (editing != null && (isReadOnly || editing.isReadOnly) && !extType.contains("(" + Texts.readOnly + ")"))
				extType += " (" + Texts.readOnly + ")";
			
			if (ComponentInput.windowResized() && editing != null) {
				if (ListableFile.fileHasExtension(ListableFile.getFileExtension(editing.getRegent().getRegent())))
					extType = getLowerBarFileName(ListableFile.getFileExtension(editing.getRegent().getRegent()));
				else
					extType = getLowerBarFileNameWithoutExtension(editing.getRegent().getRegent().getName());
			}
		}

		/*
		 * for (Tab i : tabs) { for (Tab j : tabs) { if (i.getRegent() == j.getRegent()
		 * && i != j) toRemove.add(i); } }
		 */

		if (tabs.size() > 0)
			if (tabs.get(0).getX() > x + 30)
				tabScr = 0; // ver o problema da tab

		verifyDuplicateTabs();

		if (Explorer.dragging)
			CommandTerminal.runCommand("deselect");

		int index = 0;

		for (RightClickOption r : autocompletes) {
			r.setY(((drawcy - autocompletescroll) + FONT_SIZE) + index * 30);

			index++;
		}
		
		index = 0;
		
		for (RightClickOption r : autocompletes) {
			r.setX(drawcx);

			index++;
		}

		if (KeyInput.isKeyPressed())
			keyTimeout = true;

		if (keyTimeout) {
			keyWait++;

			if (keyWait >= maxKeyWait) {
				keyWait = 0;

				keyTimeout = false;
			}
		}

		if (!selecting) {
			index1 = cursorX;
			line1 = cursorY;

			index2 = cursorX;
			line2 = cursorY;
		}

		if (lines.size() > 0
				&& scrY + (LINE_HEIGHT) * 3 > (lines.size() + 2) * (LINE_HEIGHT)) {
			scrY = (lines.size() * FONT_SIZE) - (FONT_SIZE * 3);

			cursorX = lines.get(lines.size() - 1).getChars().size();
			cursorY = lines.size();

			setCursorWithinBounds();
		}

		// int speed = 10;

		/*
		 * if (drawcx < realcx) drawcx += speed; // talvez quando for adicionar anima��o
		 * t� aqui pronto if (drawcx > realcx) drawcx -= speed;
		 * 
		 * if (drawcy < realcy) drawcy += speed; if (drawcy > realcy) drawcy -= speed;
		 */

		if (FONT_SIZE < 1)
			FONT_SIZE = 16;

		if (MouseInput.leftDragged()  && Tab.dragging == null && hovered()
				&& !MouseInput.hovered(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22)) {
			selecting = true;

			index1 = cursorX;
			line1 = cursorY;

			index2 = mx;
			line2 = my; // TODO definir o direction aqui

			if (index1 == index2 && line1 == line2)
				selecting = false;

			if (line2 < line1) {
				int tempindex1 = index1;
				int templine1 = line1;

				index1 = index2;
				index2 = tempindex1;

				line1 = line2;
				line2 = templine1;

			} else if (line2 == line1) {
				if (index2 < index1) {
					int tempindex1 = index1;

					index1 = index2;
					index2 = tempindex1;
				}
			}
		}

		if (editing != null && editing.getRegent() != null) {
			if (Main.baseFolder != null)
				Main.screen.frame.setTitle(Main.baseFolder.getName() + " | " + editing.getRegent().getRegent().getName() + " - " + Main.PROGRAM_NAME);
			else
				Main.screen.frame.setTitle(editing.getRegent().getRegent().getName() + " - " + Main.PROGRAM_NAME);
		}
		else if (Main.baseFolder != null)
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " - " + Main.PROGRAM_NAME);

		try {
			clipboard = (String) Main.toolkit.getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException | IllegalStateException e) {
			// N�o � string. Resetando!

			clipboard = "";
		}

		// Aqui ficava o scroll das tabs

		if (editing != null) {
			if (MouseInput.hovered(x, y + 30, width, height - 20) && !MouseInput.hovered(Main.explorer.getX() + Main.explorer.getWidth() - 5, Main.explorer.getY(), 10, Main.explorer.getHeight()) && !RightClickOption.isAutoCompleteActive() && !RightClickOption.isRightClickActive() && !Explorer.dragging)
				Main.screen.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			
			if (((MouseInput.hovered(x, height - 20, width, 20) || MouseInput.hovered(x, 0, width, 30)) || !Explorer.dragging && RightClickOption.isAutoCompleteActive() && !RightClickOption.isRightClickActive() && !RightClickOption.anyRightClickOptionHovered()) && !Tab.isTabHovered() && !MouseInput.hovered(Main.explorer.getX() + Main.explorer.getWidth() - 5, Main.explorer.getY(), 10, Main.explorer.getHeight()))
				Main.screen.setCursor(Cursor.getDefaultCursor());
			
			/*if (RightClickOption.anyRightClickOptionHovered() && !MouseInput.hovered(Main.explorer.getX() + Main.explorer.getWidth() - 5, Main.explorer.getY(), 10, Main.explorer.getHeight()))
				Main.screen.setCursor(new Cursor(Cursor.HAND_CURSOR));*/
		} else
			Main.screen.setCursor(Cursor.getDefaultCursor());

		if (!selecting)
			directionStarted = Direction.NONE;

		if (KeyInput.isKeyPressed() && !SetFileName.added && !CommandTerminal.active) { // TODO -- essa
			setCursorWithinBounds();

			new Thread("automaticcolor 2") {
				public void run() {
					try {
						if (editing == null)
							return;
						
						int i = 0;
						for (IDELine l : lines) {
							int yr = MIN_Y + (i++ * (LINE_HEIGHT)) - scrY;
							
							if (yr < 0 || yr > Main.screen.getHeight())
								continue;
							
							l.setFonts(automaticColor(toCharArray(l.getChars()),
									ListableFile.getFileExtension(editing.getRegent().getRegent())));

						}
						restartVariables();
					} catch (ConcurrentModificationException e) {
					}
				}
			}.start();	
		}

		if (tabs != null) {
			for (Tab t : tabs) {
				// if (t.getX() + tabScr < x || t.getX() + tabScr > Main.screen.getWidth())
				// continue; // infelizmente vai ter que fazer o tick mesmo assim, bom que n�o
				// pesa muito

				t.tick();
			}
		}

		if (editing == null && tabs.size() == 1)
			tabs.forEach(e -> e.close());

		for (RightClickOption r : autocompletes)
			r.tick();

		tabs.addAll(toAdd);
		toAdd.clear();

		tabs.removeAll(toRemove);
		toRemove.clear();

		lines.removeAll(linesToRemove);
		linesToRemove.clear();

		/*autocomplete.addAll(addautocomplete);
		addautocomplete.clear();*/

		autocomplete.removeAll(removeautocomplete);
		removeautocomplete.clear();

		autocompletes.addAll(toAddAutoCompletes);
		toAddAutoCompletes.clear();

		autocompletes.removeAll(toRemoveAutoCompletes);
		toRemoveAutoCompletes.clear();

		if (index1 < 0)
			index1 = 0;
		if (line1 < 1)
			line1 = 1;

		if (index2 < 0)
			index2 = 0;
		if (line2 < 1)
			line2 = 1;

		if (scrX < 0)
			scrX = 0;
		if (scrY < 0)
			scrY = 0;

		if (tabs.size() == 0)
			editing = null;

		if (editing == null) selecting = false;
		
		cursor();
		//typeLogic();
	}

	public synchronized void render(Graphics g) {
		try {
			// if (editing == null) return;
	
			Graphics2D g2 = (Graphics2D) g;
	
			g.setColor(Colors.explorerLight);
			g2.setStroke(new BasicStroke(8f));
	
			// g2.drawLine(x, y, x, height);
			g2.drawLine(x, 30, width, 30);
	
			g.setColor(Colors.background);
			g.fillRect(x, y, width, height);
	
			if (tabs == null || tabs.size() == 0)
				return;
	
			if (editing != null) {
				g.setColor(Colors.codeEditor);
				g.fillRect(x, MIN_Y, Main.screen.getWidth(), height);
			}
	
	//		if (editing != null &&																	// n�o vamos mostrar imagens aqui, vai abrir o aplicativo do sistema
	//			(ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".png") || // se for uma imagem
	//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpg") ||
	//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpeg")||
	//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".gif") ||
	//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".bmp"))) {
	//			try {
	//				BufferedImage get = ImageIO.read(getClass().getResource(editing.getRegent().getRegent().getAbsolutePath())); // esse get t� null
	//				
	//				g.drawImage(get, (x + (width / 2)) - get.getWidth(), (y + (height / 2)) - get.getHeight(), get.getWidth() * 2, get.getHeight() * 2, null);
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//			
	//			return; // pra n renderizar texto, aquele monte de coisa estranha
	//		}
	
			g.setColor(Colors.backgroundLight); // TODO � essa aqui a linha que atravessa a tela no cursor
			g.fillRect(x, MIN_Y + ((cursorY - 1) * (LINE_HEIGHT)) - scrY, Main.screen.getWidth(),
					LINE_HEIGHT);
	
			/*
			 * g.setColor(Colors.backgroundLight); g.fillRect(x, MIN_Y + ((cursorY - 1) *
			 * (LINE_HEIGHT)) - scrY - 1, 49, LINE_HEIGHT +
			 * 1);
			 */
	
			try {
				for (int i = 0; i < lines.size(); i++) {
					if (selecting) {
						g.setColor(Colors.selection);
						
						if (i > line1 && i < line2) { // do meio
							g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do 0 at� o index2
									// (i + 1) * (LINE_HEIGHT) - scrY,
									MIN_Y + ((i - 1) * (LINE_HEIGHT)) - scrY,
									// Main.screen.getWidth() + scrX,
									((x + (FONT_SIZE * 4)) + (lines.get(i - 1).getChars().size()) * (FONT_SIZE - (FONT_SIZE / 4))) - scrX
											- (((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
									LINE_HEIGHT);
						}
					}
				}
	
				for (int i = 0; i < lines.size(); i++) {
					int yr = MIN_Y + (i * (LINE_HEIGHT)) - scrY;
					
					if (yr < 0 || yr > Main.screen.getHeight()) {
						continue;
					}
					
					char[] cs = toCharArray(lines.get(i).getChars());
					IDEFont[] fs = toArray(lines.get(i).getFonts());
	
					if (lines.get(i) == null)
						break;
	
					if (MIN_Y + (i * (LINE_HEIGHT)) - scrY < MIN_Y - 15)
						continue;
	
					/*
					 * if (i == cursorY - 1 && !isReadOnly) { g.setColor(Colors.backgroundLight);
					 * g.fillRect(x, MIN_Y + (i * (LINE_HEIGHT)) - scrY - 1,
					 * Main.screen.getWidth(), LINE_HEIGHT + 1); }
					 */
	
					if (selecting) {
						g.setColor(Colors.selection);
	
						if (i == line1 - 1) { // - 1 porque a line1 � base 1
							if (i == line2 - 1) {
								g.fillRect(((x + (FONT_SIZE * 4)) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do
																										// index1 at� o
																										// index2
										MIN_Y + ((line1 - 1) * (LINE_HEIGHT)) - scrY,
										(((x + (FONT_SIZE * 4)) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX)
												- (((x + (FONT_SIZE * 4)) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
										LINE_HEIGHT);
							} else {
								g.fillRect(((x + (FONT_SIZE * 4)) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do
																										// index1 at� o fim
																										// da linha
										MIN_Y + ((line1 - 1) * (LINE_HEIGHT)) - scrY,
										((((x + (FONT_SIZE * 4)) + (lines.get(line1 - 1).getChars().size() - index1)
												* (FONT_SIZE - (FONT_SIZE / 4))) - scrX)
												- (((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX)),
										LINE_HEIGHT);
							}
						}
						if (i == line2 - 1) {
							if (i != line1 - 1) { // do 0 ao index2
								g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher at� o index2
										MIN_Y + ((line2 - 1) * (LINE_HEIGHT)) - scrY,
										((x + (FONT_SIZE * 4)) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX
												- (((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
										LINE_HEIGHT);
							}
						}
					}
	
					IDEFont font = i == cursorY - 1 ? new IDEFont(Fonts.selectedLineNumberNormal, FONT_SIZE)
							: new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
	
					// if (isReadOnly) font = new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
	
					Fonts.drawChars(cs, (x + (FONT_SIZE * 4)) - scrX, MIN_Y + (i * (LINE_HEIGHT)) - scrY, fs, x + (FONT_SIZE * 4), Main.screen.getWidth(), g);
	
					String nums = String.valueOf(i + 1); // nums = num string
					// int num = i + 1;
	
					int nx = x + 1;
	
					/*
					 * if (num < 10) nx = x + 1 + (2 * FONT_SIZE) + 3; if (num >= 10 && num < 100)
					 * nx = x + 1 + FONT_SIZE + 3 + 3; // n�o ser� feito, pelo menos por enquanto if
					 * (num >= 100 && num < 1000) nx = x + 1 + 6;
					 */
	
					Color c = i != cursorY - 1 ? Colors.codeEditor : Colors.explorerLight;
	
					g.setColor(c);
					
					g.fillRect(x, MIN_Y + (i * (LINE_HEIGHT)) - scrY, FONT_SIZE * 4, LINE_HEIGHT); // linha do num da linha
	
					Fonts.drawString(nums, nx, MIN_Y + (i * (LINE_HEIGHT)) - scrY, font, g);
				}
			} catch (Exception e) {
			}
	
			if (keyTimeout)
				showCursor = true;
	
			// Desenhar cursor
			if (showCursor && !WindowInput.isDeactivated() && drawcx > x + (FONT_SIZE * 4) - 1 && Explorer.selected == null) {
				g.setColor(Colors.cursor);
				g.fillRect(drawcx, drawcy, // na posi��o x 12 ele aparece um pouco encima dos numeros
						FONT_SIZE > 10 ? 2 : 1, LINE_HEIGHT);
			}
	
			for (RightClickOption r : autocompletes)
				r.render(g);
	
			// Desenhar background
			g.setColor(Colors.background);
			g.fillRect(x, 0, width, 35);
	
			for (Tab t : Main.editor.tabs) {
				if (t.getX() + tabScr < x - 100 || t.getX() + tabScr > Main.screen.getWidth())
					continue; // o render da Tab vai ter que ficar aqui mesmo
	
				t.render(g);
			}
			
			if (Tab.dragging != null)
	        	Tab.dragging.render(g);
			
			if (editing != null) { // linha encima do editor
				g.setColor(Colors.textLight);
				g2.setStroke(new BasicStroke(3f));
				
				g.drawLine(x, MIN_Y - 2, Main.screen.getWidth(), MIN_Y - 2);
			}
			
			g.setColor(editing.hovered() ? Colors.explorerLight : Colors.codeEditor); // draw encima da tab pra parecer o chrome
			g2.setStroke(new BasicStroke(3f));
			g2.fillRect(editing.getX() + 2 + tabScr, Tab.Y + Tab.HEIGHT - 2, editing.drawW - 3, 4);
	
			// desenhar barra inferior
			if (editing != null) {
				g.setColor(Colors.lowerBar);
				g.fillRect(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22);
				
				String selectingText = selecting ? " | " + Texts.selecting + ": " + countIndexDistance(index1, index2, line1, line2) : "";
							
				if (minMode)
					selectingText = selecting ? (" | " + countIndexDistance(index1, index2, line1, line2)) : "";
				
				Fonts.drawString(codeType + " - " + extType + " | " + (cursorX + 1) + ":" + cursorY
						+ selectingText,
						x + 10, Main.screen.getHeight() - 20, new IDEFont(Fonts.otherNormal, 16), g);
	
				// Fonts.drawString("X: " + (cursorX + 1) + ", Y: " + cursorY,
				// Main.screen.getWidth() - 170, Main.screen.getHeight() - 20, new
				// IDEFont(Fonts.otherNormal, 16), g);
			}
		} catch (Exception e) {}
	}
}
