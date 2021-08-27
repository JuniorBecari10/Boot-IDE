package ide.codeeditor;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

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
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

// Nota: para escrever em vermelho no console, ao invés de digitar System.out.println("texto"); use System.err.println("texto");

public class CodeEditor extends IDEComponent {
	
	public static volatile int FONT_SIZE = 16; // 18, 16 (Padrão: 16)
	
	public Tab editing;
	
	public boolean isMultilineCommenting = false;
	
	public boolean selecting;

	public int line1, line2;
	public int index1, index2;
	
	public boolean alternateTabsMode = false;
	
	public Tab exchanging;
	public Tab exchanged;
	
	public boolean isCssPart;
	public boolean isJSPart;
	public boolean isPhpPart;
	
	private boolean keyTimeout;
	
	public int keyWait = 0, maxKeyWait = 5;
	
	public boolean codeHelpersOn = true;
	
	public String codeType = "";
	public String extType = "";
	
	public boolean isAnotherIteration = false;
	public boolean foundExt = false;
	
	public int cursorX = 0;
	public int cursorY = 1;
	
	public int scrX = 0;
	public int scrY = 0;
	
	private int realcx, realcy; // c = cursor
	private int drawcx = ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, drawcy = MIN_Y + cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2;
	
	private PressedAccent prAcc;
	private boolean pressedAccent = false;
	
	public List<IDELine> lines = new ArrayList<>();
	public List<IDELine> linesToRemove = new ArrayList<>();
	
	/*public static Stack<List<IDELine>> undo = new Stack<>();
	public static Stack<List<IDELine>> redo = new Stack<>();*/
	
	// Undo e Redo não estão disponíveis, talvez na v4.0
	
	public int tabScr = 0;
	
	public List<Tab> tabs;
	public List<Tab> toAdd;
	public List<Tab> toRemove;
	
	public SearchReplaceWindow searchWindow;
	public boolean alreadyAddedFrame = false;
	
	//public static BufferedImage gradient;
	
	public static String clipboard = "";
	
	public static final int MIN_Y = 35;
	
	private boolean showCursor;
	
	private Thread cursorThread;
	private Animation cursor;
	
	public int mx;
	public int my;
	
	public boolean isReadOnly = false;
	
	public List<String> autocomplete = new ArrayList<>();
	public String wordSinceSpace = "";
	
	public int autocompleteindex = 0;
	
	public List<AutoComplete> autocompleteadds = new ArrayList<>();
	public List<AutoComplete> addautocompleteadds = new ArrayList<>();
	
	public List<RightClickOption> autocompletes = new ArrayList<>();
	public List<RightClickOption> toAddAutoCompletes = new ArrayList<>();
	public List<RightClickOption> toRemoveAutoCompletes = new ArrayList<>();
	
	///
	
	public static BufferedImage functions = Main.icons.getSprite(0, 0, 8, 8);
	public static BufferedImage objects = Main.icons.getSprite(8, 0, 8, 8);
	public static BufferedImage keywords = Main.icons.getSprite(16, 0, 8, 8);
	public static BufferedImage variables = Main.icons.getSprite(24, 0, 8, 8);
	
	///
	
	///////
	
	public static final String[] syms = { "(", ")", "[", "]", "{", "}", ",", ".", "<", ">", ";", ":", "?", "/", "|", "+", "-", "*", "=", "&", "%", "$", "#", "!", "@", "`", "´", "^", "~" };
	
	public static final String[] loremWords = { "dolor", "sit", "amet", "consectetur",
			"adipiscing", "elit", "curabitur", "vel", "hendrerit", "libero",
			"eleifend", "blandit", "nunc", "ornare", "odio", "ut",
			"orci", "gravida", "imperdiet", "nullam", "purus", "lacinia",
			"a", "pretium", "quis", "congue", "praesent", "sagittis", 
			"laoreet", "auctor", "mauris", "non", "velit", "eros",
			"dictum", "proin", "accumsan", "sapien", "nec", "massa",
			"volutpat", "venenatis", "sed", "eu", "molestie", "lacus",
			"quisque", "porttitor", "ligula", "dui", "mollis", "tempus",
			"at", "magna", "vestibulum", "turpis", "ac", "diam",
			"tincidunt", "id", "condimentum", "enim", "sodales", "in",
			"hac", "habitasse", "platea", "dictumst", "aenean", "neque",
			"fusce", "augue", "leo", "eget", "semper", "mattis", 
			"tortor", "scelerisque", "nulla", "interdum", "tellus", "malesuada",
			"rhoncus", "porta", "sem", "aliquet", "et", "nam",
			"suspendisse", "potenti", "vivamus", "luctus", "fringilla", "erat",
			"donec", "justo", "vehicula", "ultricies", "varius", "ante",
			"primis", "faucibus", "ultrices", "posuere", "cubilia", "curae",
			"etiam", "cursus", "aliquam", "quam", "dapibus", "nisl",
			"feugiat", "egestas", "class", "aptent", "taciti", "sociosqu",
			"ad", "litora", "torquent", "per", "conubia", "nostra",
			"inceptos", "himenaeos", "phasellus", "nibh", "pulvinar", "vitae",
			"urna", "iaculis", "lobortis", "nisi", "viverra", "arcu",
			"morbi", "pellentesque", "metus", "commodo", "ut", "facilisis",
			"felis", "tristique", "ullamcorper", "placerat", "aenean", "convallis",
			"sollicitudin", "integer", "rutrum", "duis", "est", "etiam",
			"bibendum", "donec", "pharetra", "vulputate", "maecenas", "mi",
			"fermentum", "consequat", "suscipit", "aliquam", "habitant", "senectus",
			"netus", "fames", "quisque", "euismod", "curabitur", "lectus",
			"elementum", "tempor", "risus", "cras" };
	
	public static final String[] javaKeys = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
			"continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
			"for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
			"new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
			"true", "false", "null", "yield" };
	
	public static final String[] tags = { "<!--", "<!doctype", "<?php", "<!DOCTYPE", "<a", "<abbr", "<acronym", "<address", "<applet", "<area", "<article",
			"<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo", "<big", "<blockquote", "<body", "<br", "<button",
			"<canvas", "<caption", "<center", "<cite", "<code", "<col", "<colgroup", "<data", "<datalist", "<dd", "<del",
			"<details", "<dfn", "<dialog", "<dir", "<div", "<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure",
			"<font", "<footer", "<form", "<frame", "<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header",
			"<hr", "<html", "<i", "<iframe", "<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main",
			"<map", "<mark", "<meta", "<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option",
			"<output", "<p", "<param", "<picture", "<pre", "<progress", "<q", "<rp", "<rt", "<ruby", "<s", "<samp", "<script",
			"<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg", "<table",
			"<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr", "<track", "<tt",
			"<u", "<ul", "<var", "<video", "<wbr", "<applet",
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
			"</u", "</ul", "</var", "</video", "</wbr", "</applet" };
	
	public static final String[] nums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
			  "1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a", "0a", // hex
			  "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b", "0b",
			  "1c", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "0c",
			  "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "0d",
			  "1e", "2e", "3e", "4e", "5e", "6e", "7e", "8e", "9e", "0e",
			  "1f", "2f", "3f", "4f", "5f", "6f", "7f", "8f", "9f", "0f",
			  "1l", "2l", "3l", "4l", "5l", "6l", "7l", "8l", "9l", "0l",
			  "1A", "2A", "3A", "4A", "5A", "6A", "7A", "8A", "9A", "0A", // HEX
			  "1B", "2B", "3B", "4B", "5B", "6B", "7B", "8B", "9B", "0B",
			  "1C", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "0C",
			  "1D", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "0D",
			  "1E", "2E", "3E", "4E", "5E", "6E", "7E", "8E", "9E", "0E",
			  "1F", "2F", "3F", "4F", "5F", "6F", "7F", "8F", "9F", "0F",
			  "1L", "2L", "3L", "4L", "5L", "6L", "7L", "8L", "9L", "0L",
			  "1x", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "0x",
			  "1X", "2X", "3X", "4X", "5X", "6X", "7X", "8X", "9X", "0X",
			  "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "0h",
			  "1H", "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "0H"
			  };
	
	public static final String[] phpKeys = { "abstract", "and", "as", "break", "callable", "case", "catch", "class", "clone",
			"const", "continue", "declare", "default", "do", "echo", "else", "elseif", "enddeclare", "endfor",
			"endforeach", "endif", "endswitch", "endwhile", "extends", "final", "finally", "fn", "for", "foreach",
			"function", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof",
			"interface", "match", "namespace", "new", "or", "print", "private", "protected", "public", "require",
			"require_once", "return", "static", "switch", "throw", "trait", "try", "use", "var", "while", "yield",
			"yield from", "__CLASS__", "__DIR__", "__FILE__", "__FUNCTION__", "__LINE__", "__METHOD__", "__NAMESPACE__",
			"__TRAIT__" };
	
	public static final String[] jsKeys = { "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
			"enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function",
			"goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
			"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
			"super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
			"var", "void", "volatile", "while", "with", "yield", "undefined", "of", "async", "window", "document",
			"console", "as", "from", "navigator" };
	
	public static final String[] cssTags = { "a", "abbr", "acronym", "address", "applet", "area", "article",
			"aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
			"canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del",
			"details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
			"font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header",
			"hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main",
			"map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option",
			"output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script",
			"section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table",
			"tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt",
			"u", "ul", "var", "video", "wbr", "applet", "important", "screen", "and", "or", "moz", "webkit", "ms", "mixin",
			"user", "select", "drag" /* TODO colocar mais desses ultimos */, "deg", "rad"
	};
	
	public static final String[] props = { "align-content", "align-items", "all", "animation", "animation-direction",
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
	
	public static final String[] units = { "px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax" };
	
	public static final String[] pyKeys = { "and", "as", "assert", "break", "class",
			"continue", "def", "del", "elif", "else", "except", "False",
			"finally", "for", "from", "global", "if", "import", "in", "is",
			"lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return", "super",
			"True", "try", "while", "with", "yield", "self", "async", "await", "of", "str", "int", "float", "complex", "list", "tuple", "dict", "set", "frozenset", "bool", "bytes", "bytearray", "memoryview" };
	
	public static final String[] dartKeys = { "abstract", "else", "import", "super", "as", "enum", "in",
			"switch", "assert", "export", "interface", "sync", "async", "extends", "is",
			"this", "await", "extension", "library", "throw", "break", "external", "mixin",
			"true", "case", "factory", "new", "try", "class", "final", "catch", "false",
			"null", "typedef", "on", "var", "const", "finally", "operator", "void", "continue",
			"for", "part", "while", "covariant", "Function", "rethrow", "with", "default",
			"get", "return", "yield", "deferred", "hide", "set", "do", "if", "show", "dynamic",
			"implements", "static" };
	
	public static final String[] ldKeys = { "ENTRY", "OUTPUT_FORMAT", "STARTUP", "SEARCH_DIR", "INPUT", "OUTPUT",
			"MEMORY", "SECTIONS", "KEEP" };
	
	public static final String[] pasKeys = { "and", "begin", "boolean", "break", "byte", "continue", "div", "do", "double",
			"else", "end", "false", "if", "integer", "longint", "mod", "not", "or", "repeat", "shl",
			"shortint", "shr", "single", "then", "true", "until", "while", "word", "xor", "function" };
	
	public static final String[] cKeys = { "auto", "break", "case", "char", "const",
			"continue", "default", "do", "double", "else", "enum", "extern",
			"float", "for", "goto", "if", "int", "long", "register", "return",
			"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
			"union", "unsigned", "void", "volatile", "while", "true", "false", "null", "include",
			"bool", "duint", "uint16_t" };
	
	public static final String[] cppKeys = { "auto", "break", "case", "char", "const",
			"continue", "default", "do", "double", "else", "enum", "extern",
			"float", "for", "goto", "if", "int", "long", "register", "return",
			"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
			"union", "unsigned", "void", "volatile", "while",
			"asm", "dynamic_cast", "namespace", "reinterpret_cast", "bool",
			"explicit", "new", "static_cast", "false", "catch", "operator", "template",
			"friend", "private", "class", "this", "inline", "public", "throw", "const_cast",
			"delete", "mutable", "protected", "true", "try", "typeid", "typename", "using", "virtual",
			"wchar_t", "include", "define", "string", "ifdef", "ifndef", "error", "pragma", "endif",
			"override", "std" };
	
	public static final String[] csKeys = { "abstract", "async", "const", "event", "extern", "new",
			"override", "partial", "readonly", "sealed", "static", "unsafe", "virtual",
			"volatile", "public", "private", "internal", "protected", "if", "else", "switch",
			"case", "do", "for", "foreach", "in", "while", "break", "continue", "default", "goto",
			"return", "yield", "throw", "try", "catch", "finally", "checked", "unchecked", "fixed",
			"lock", "params", "ref", "out", "using", "alias", "await", "sizeof", "typeof",
			"stackalloc", "is", "base", "this", "null", "false", "true", "value", "void", "bool", "byte", "interface",
			"char", "class", "decimal", "double", "enum", "float", "int", "long", "sbyte", "short", "string", "super",
			"struct", "uint", "ulong", "ushort", "add", "var", "dynamic", "global", "set", "namespace", "object", "as", "get" };
	
	public static final String[] rKeys = { "if", "else", "repeat", "while", "function", "for", "in", "next", "break",
			"TRUE", "FALSE", "NULL", "Inf", "NaN", "NA", "NA_integer", "NA_real", "NA_complex", "NA_character" };
	
	public static final String[] batCom = { "ver", "assoc", "cd", "cls", "copy", "del", "dir", "date",
			"echo", "@echo", "exit", "md", "move", "path", "pause", "prompt", "rd",
			"rem", "start", "time", "type", "on", "vol", "attrib", "chkdsk", "choice", "cmd",
			"comp", "convert", "driverquery", "expand", "find", "format", "help", "ipconfig",
			"label", "more", "net", "ping", "shutdown", "sort", "subst", "subst", "systeminfo",
			"taskkill", "xcopy", "tree", "fc", "title", "set", "bash", "node", "off", "goto",
			"rmdir", "icacls", "takeown", "if", "for", "else", "git", "npm", "call", "exist", "end",
			"java", "javac", "javaw", "nodemon", "csc", "nasm", "qemu", "gcc", "g++", "python", "lua", "bin",
			"VER", "ASSOC", "CD", "CLS",
			"COPY", "DEL", "DIR", "DATE", "ECHO", "@ECHO", "EXIT", "MD", "MOVE", "PATH", "PAUSE",
			"PROMPT", "RD", "REM", "START", "TIME", "TYPE", "VOL", "ATTRIB", "CHKDSK", "CHOICE",
			"CMD", "COMP", "CONVERT", "ON", "DRIVERQUERY", "EXPAND", "FIND", "FORMAT", "HELP", "IPCONFIG",
			"LABEL", "MORE", "NET", "PING", "SHUTDOWN", "SORT", "SUBST", "SUBST", "SYSTEMINFO",
			"TASKKILL", "XCOPY", "TREE", "FC", "TITLE", "SET", "BASH", "NODE", "OFF", "GOTO",
			"RMDIR", "ICACLS", "TAKEOWN", "IF", "FOR", "ELSE", "GIT", "NPM", "CALL", "EXIST", "END",
			"JAVA", "JAVAC", "JAVAW", "NODEMON", "CSC", "NASM", "QEMU", "GCC", "G++", "PYTHON", "LUA", "BIN" };
	
	// Não vai ter aqui as extensões do word, powerpoint, excel etc.
	public static final String[] extensions = { ".java", ".c", ".cpp", ".cs", ".py", ".js", ".mjs", ".bat", ".cmd", ".com", ".ps1", ".h", ".hpp", ".hxx", ".asm", ".s", ".lua", ".sql", ".swift", ".rs", ".php", ".kt", ".vue", ".rb", ".ino", ".ts", ".go", ".r", ".pl", ".jl", ".has", ".hs", ".fs", ".coffee", ".m", ".pas", ".pp", ".scala", ".dart", ".zig",
			".html", ".htm", ".css", ".scss", ".xml", ".json", ".jsonc", ".md", ".markdown", ".txt", ".log", ".pdf", ".jar", ".svg", ".urna", ".save", ".conf", ".makefile", ".mk", ".make", ".sh", ".gitignore", ".dockerfile", ".class", ".zip", ".bin", ".license", ".cfg", ".config", ".jsx", ".ejs", ".ld", ".lock", ".ini", ".dll", ".url", ".authors", ".img", ".flp",
			".JAVA", ".C", ".CPP", ".CS", ".PY", ".JS", ".BAT", ".CMD", ".COM", ".PS1", ".H", ".HPP", ".HXX", ".ASM", ".S", ".LUA", ".SQL", ".SWIFT", ".RS", ".PHP", ".KT", ".VUE", ".RB", ".INO", ".TS", ".GO", ".R", ".PL", ".JL", ".HAS", ".HS", ".FS", ".COFFEE", ".M", ".PAS", ".PP", ".SCALA", ".DART", ".ZIG",
			".HTML", ".HTM", ".CSS", ".XML", ".JSON", ".JSONC", ".MD", ".MARKDOWN", ".TXT", ".LOG", ".PDF", ".JAR", ".SVG", ".URNA", ".SAVE", ".CONF", ".MAKEFILE", ".MK", ".MAKE", ".SH", ".GITIGNORE", ".DOCKERFILE", ".CLASS", ".ZIP", ".BIN", ".LICENSE", ".CFG", ".CONFIG", ".JSX", ".EJS", ".LD", ".LOCK", ".INI", ".DLL", ".URL", ".AUTHORS", ".IMG", ".FLP"};
	
	public static final String[] luaKeys = { "and", "break", "do", "else", "elseif", "end",
			"false", "for", "function", "if", "in", "local", "nil",
			"not", "or", "repeat", "return", "then", "true", "until", "while",
			"os", "io", "math", "string", "require", "table", "debug" };
	
	public static final String[] zigKeys = { "align", "allowzero", "and", "anyframe", "anytype", "asm", "async", "await",
			"break", "catch", "comptime", "const", "continue", "defer", "else", "enum", "errdefer",
			"error", "export", "extern", "false", "fn", "for", "if", "inline", "noalias",
			"nosuspend", "null", "or", "orelse", "packed", "pub", "resume", "return", "linksection",
			"struct", "suspend", "switch", "test", "threadlocal", "true", "try", "undefined",
			"union", "unreachable", "usingnamespace", "var", "volatile", "while" };
	
	public static final String[] sqlKeys = { "ADD", "ADD CONSTRAINT", "ALTER", "ALTER COLUMN", "ALTER TABLE",
			"ALL", "AND", "ANY", "AS", "ASC", "BACKUP DATABASE", "BETWEEN", "CASE", "CHECK",
			"COLUMN", "CONSTRAINT", "CREATE", "CREATE DATABASE", "CREATE INDEX", "CREATE OR REPLACE VIEW",
			"CREATE TABLE", "CREATE PROCEDURE", "CREATE UNIQUE INDEX", "CREATE VIEW", "DATABASE", "DEFAULT",
			"DELETE", "DESC", "DISTINCT", "DROP", "DROP COLUMN", "DROP CONSTRAINT", "DROP DATABASE",
			"DROP DEFAULT", "DROP INDEX", "DROP TABLE", "DROP VIEW", "EXEC", "EXISTS", "FOREIGN KEY",
			"FROM", "FULL OUTER JOIN", "GROUP BY", "HAVING", "IN", "INDEX", "INNER JOIN", "INSERT INTO",
			"INSERT INTO SELECT", "IS NULL", "IS NOT NULL", "JOIN", "LEFT JOIN", "LIKE", "LIMIT", "NOT",
			"NOT NULL", "OR", "ORDER BY", "OUTER JOIN", "PRIMARY KEY", "PROCEDURE", "RIGHT JOIN", "ROWNUM",
			"SELECT", "SELECT DISTINCT", "SELECT INTO", "SELECT TOP", "SET", "TABLE", "TOP", "TRUNCATE TABLE",
			"UNION", "UNION ALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE", "add", "add constraint", "alter",
			"alter column", "alter table", "all", "and", "any", "as", "asc", "backup database", "between", "case",
			"check", "column", "constraint", "create", "create database", "create index", "create or replace view",
			"create table", "create procedure", "create unique index", "create view", "database", "default",
			"delete", "desc", "distinct", "drop", "drop column", "drop constraint", "drop database",
			"drop default", "drop index", "drop table", "drop view", "exec", "exists", "foreign key",
			"from", "full outer join", "group by", "having", "in", "index", "inner join", "insert into",
			"insert into select", "is null", "is not null", "join", "left join", "like", "limit", "not",
			"not null", "or", "order by", "outer join", "primary key", "procedure", "right join", "rownum",
			"select", "select distinct", "select into", "select top", "set", "table", "top", "truncate table",
			"union", "union all", "unique", "update", "values", "view", "where" };
	
	public static final String[] asmRegs = { "rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rbp", "rsp", "r8", "r9", "r10", "r11", "r12", "r13",
			"r14", "r15", "eax", "ebx", "ecx", "esi", "edi", "ebp", "esp", "r8d", "r9d", "r10d", "r11d", "r12d", "r13d",
			"r14d", "r15d", "ax", "bx", "cx", "dx", "si", "di", "bp", "sp", "r8w", "r9w", "r10w", "r11w", "r12w", "r13w",
			"r14w", "r15w", "al", "bl", "cl", "dl", "sil", "dil", "bpl", "spl", "r8b", "r9b", "r10b", "r11b", "r12b",
			"r13b", "r14b", "r15b", "ah", "bh", "ch", "dh", "edx", "ss", "sp", "ds", "es" };
	
	// não vai colorir keys de uma só letra
	public static final String[] asmKeys = { "global", "define", "db", "dw", "equ", "extern", "include", "times", "org", "bits", "syscall", "aaa", "aad", "aam", "aas", "adc",
			"add", "addpd", "addps", "addressing", "addsd", "addss", "jz", "align", "and", "andnpd", "andnps", "andpd",
			"andps", "arpl", "as", "commandline", "ELFobjectfile", "macroprocessing", "syntaxUNIXversusIntel", "ascii",
			"assemblerSeeasB", "bcd", "binaryarithmeticinstructions", "bitinstructions", "bound", "bsf", "bsr",
			"bss", "bswap", "bt", "btc", "btr", "bts", "byte", "byte", "byte", "byte", "byteinstructionsC", "call",
			"cbtw", "clc", "cld", "clflush", "cli", "cltd", "cltq", "clts", "cmc", "cmova", "cmova", "cmovae",
			"cmovae", "cmovb", "cmovb", "cmovbe", "cmovbe", "cmovc", "cmovc", "cmove", "cmove", "cmovg", "cmovg",
			"cmovge", "cmovge", "cmovl", "cmovl", "cmovle", "cmovle", "cmovna", "cmovna", "cmovnae", "cmovnae",
			"cmovnb", "cmovnb", "cmovnbe", "cmovnbe", "cmovnc", "cmovnc", "cmovne", "cmovne", "cmovng", "cmovng",
			"cmovnge", "cmovnge", "cmovnl", "cmovnl", "cmovnle", "cmovnle", "cmovno", "cmovno", "cmovnp", "cmovnp",
			"cmovns", "cmovns", "cmovnz", "cmovnz", "cmovo", "cmovo", "cmovp", "cmovp", "cmovpe", "cmovpo", "cmovs",
			"cmovz", "cmp", "cmppd", "cmpps", "cmps", "cmpsb", "cmpsd", "cmpsl", "cmpss", "cmpsw", "cmpxchg",
			"cmpxchgb", "comisd", "comiss", "comm", "comment", "controltransferinstructions", "cpp", "cpuid",
			"cqtd", "cqto", "cvtdqpd", "cvtdqps", "cvtpddq", "cvtpdpi", "cvtpdps", "cvtpipd", "cvtpips", "cvtpsdq",
			"cvtpspd", "cvtpspi", "cvtsdsi", "cvtsdss", "cvtsisd", "cvtsiss", "cvtsssd", "cvtsssi", "cvttpddq",
			"cvttpdpi", "cvttpsdq", "cvttpspi", "cvttsdsi", "cvttsssi", "cwtd", "cwtlD", "daa", "das", "data",
			"datatransferinstructions", "dec", "decimalarithmeticinstructions", "directives", "div", "divpd", "divps",
			"divsd", "divss", "doubleE", "ELFobjectfile", "emms", "enter", "even", "extF", "fxm", "fabs", "fadd",
			"faddp", "fbe", "Seeas", "fbld", "fbstp", "fchs", "fclex", "fcmovb", "fcmovbe", "fcmove", "fcmovnb",
			"fcmovnbe", "fcmovne", "fcmovnu", "fcmovu", "fcom", "fcomi", "fcomip", "fcomp", "fcompp", "fcos",
			"fdecstp", "fdiv", "fdivp", "fdivr", "fdivrp", "ffree", "fiadd", "ficom", "ficomp", "fidiv", "fidivr",
			"fild", "file", "fimul", "fincstp", "finit", "fist", "fistp", "fisub", "fisubr", "flagcontrolinstructions",
			"fld", "fld", "fldcw", "fldenv", "fldle", "fldlt", "fldlg", "fldln", "fldpi", "fldz", "float",
			"floating-pointinstructions", "basicarithmetic", "comparison", "control", "datatransfer", "loadconstants",
			"logarithmic", "Seetranscendental", "transcendental", "trigonometric", "Seetranscendental", "fmul",
			"fmulp", "fnclex", "fninit", "fnop", "fnsave", "fnstcw", "fnstenv", "fnstsw", "fpatan", "fprem", "fprem",
			"fptan", "frndint", "frstor", "fsave", "fscale", "fsin", "fsincos", "fsqrt", "fst", "fstcw", "fstenv",
			"fstp", "fstsw", "fsub", "fsubp", "fsubr", "fsubrp", "ftst", "fucom", "fucomi", "fucomip", "fucomp",
			"fucompp", "fwait", "fxam", "fxch", "fxrstor", "fxsave", "fxtract", "fylx", "fylxp", "G", "gas", "globl",
			"group", "H", "hidden", "hlt", "ident", "identifier", "idiv", "imul", "inc", "ins", "insb", "insl",
			"instruction", "format", "suffixes", "instructions", "binaryarithmetic", "bit", "byte", "controltransfer",
			"datatransfer", "decimalarithmetic", "flagcontrol", "floating-point-", "logical", "miscellaneous", "MMX-",
			"operatingsystemsupport-", "Opteron", "rotate", "segmentregister", "shift", "SIMDstatemanagement", "SSE-",
			"SSE-", "string", "insw", "int", "into", "invd", "invlpg", "iretJ", "ja", "jae", "jb", "jbe", "jc", "jcxz",
			"je", "jecxz", "jg", "jge", "jl", "jle", "jmp", "jnae", "jnb", "jnbe", "jnc", "jne", "jng", "jnge", "jnl",
			"jnle", "jno", "jnp", "jns", "jnz", "jo", "jp", "jpe", "jpo", "js", "jzK", "keywordL", "label", "numeric",
			"symbolic", "lahf", "lar", "lcall", "lcomm", "ldmxcsr", "lds", "lea", "leave", "les", "lfence", "lfs",
			"lgdt", "lgs", "lidt", "lldt", "lmsw", "local", "lock", "lods", "lodsb", "lodsl", "lodsw",
			"logicalinstructions", "long", "loop", "loope", "loopne", "loopnz", "loopz", "lret", "lsl", "lss", "ltr",
			"maskmovdqu", "maskmovq", "maxpd", "maxps", "maxsd", "maxss", "mfence", "minpd", "minps", "minsd",
			"minss", "miscellaneousinstructions", "MMXinstructions", "comparison", "conversion", "datatransfer",
			"logical", "packedarithmetic", "rotate", "shift", "statemanagement", "mov", "movabs", "movabsA", "movapd",
			"movaps", "movd", "movdqq", "movdqa", "movdqu", "movhlps", "movhpd", "movhps", "movlhps", "movlpd",
			"movlps", "movmskpd", "movmskps", "movntdq", "movnti", "movntpd", "movntps", "movntq", "movq", "movqdq",
			"movs", "movsb", "movsd", "movsl", "movss", "movsw", "movupd", "movups", "movzb", "movzw", "mul", "mulpd",
			"mulps", "mulsd", "mulss", "neg", "nop", "not", "numbers", "floatingpoint", "integers", "binary",
			"decimal", "hexadecimal", "octal", "operands", "immediate", "indirect", "memory", "addressing",
			"ordering", "register", "operatingsystemsupportinstructions", "Opteroninstructions", "or", "orpd",
			"orps", "out", "outs", "outsb", "outsl", "outswP", "packssdw", "packsswb", "packuswb", "paddb", "paddd",
			"paddq", "paddsb", "paddsw", "paddusb", "paddusw", "paddw", "pand", "pandn", "pause", "pavgb", "pavgw",
			"pcmpeqb", "pcmpeqd", "pcmpeqw", "pcmpgtb", "pcmpgtd", "pcmpgtw", "pextrw", "pinsrw", "pmaddwd", "pmaxsw",
			"pmaxub", "pminsw", "pminub", "pmovmskb", "pmulhuw", "pmulhw", "pmullw", "pmuludq", "pop", "popa", "popal",
			"popaw", "popf", "popfw", "popsection", "por", "prefetchnta", "prefetcht", "prefetcht", "prefetcht",
			"previous", "psadbw", "pshufd", "pshufhw", "pshuflw", "pshufw", "pslld", "pslldq", "psllq", "psllw", "psrad",
			"psraw", "psrld", "psrldq", "psrlq", "psrlw", "psubb", "psubd", "psubq", "psubsb", "psubsw", "psubusb",
			"psubusw", "psubw", "punpckhbw", "punpckhdq", "punpckhqdq", "punpckhwd", "punpcklbw", "punpckldq",
			"punpcklqdq", "punpcklwd", "push", "pusha", "pushal", "pushaw", "pushf", "pushfw", "pushsection",
			"pxor", "quad", "rcl", "rcpps", "rcpss", "rcr", "rdmsr", "rdpmc", "rdtsc", "rel", "rep", "repnz", "repz",
			"ret", "rol", "ror", "rotateinstructions", "rsm", "rsqrtps", "rsqrtss", "sahf", "sal", "sar", "sbb",
			"scas", "scasb", "scasl", "scasw", "section", "segmentregisterinstructions", "set", "seta", "setae",
			"setb", "setbe", "setc", "sete", "setg", "setge", "setl", "setle", "setna", "setnae", "setnb", "setnbe",
			"setnc", "setne", "setng", "setnge", "setnl", "setnle", "setno", "setnp", "setns", "setnz", "seto", "setp",
			"setpe", "setpo", "sets", "setz", "sfence", "sgdt", "shiftinstructions", "shl", "shld", "shr", "shrd",
			"shufpd", "shufps", "sidt", "SIMDstatemanagementinstructions", "skip", "sldt", "sleb", "smovl", "smsw",
			"sqrtpd", "sqrtps", "sqrtsd", "sqrtss", "SSEinstructions", "compare", "conversion", "datatransfer",
			"integer", "logical", "miscellaneous", "MXCSRstatemanagement", "packedarithmetic", "shuffle", "unpack",
			"SSEinstructions", "compare", "conversion", "datamovement", "logical", "miscellaneous", "packedarithmetic",
			"packedsingle-precisionfloating-point", "shuffle", "SIMDintegerinstructions", "unpack", "statement",
			"empty", "stc", "std", "sti", "stmxcsr", "stos", "stosb", "stosl", "stosw",
			"stringinstructions", "sub", "subpd", "subps", "subsd", "subss", "symbolic", "sysenter", "sysexit", "tbss",
			"tcomm", "tdata", "test", "text", "ucomisd", "ucomiss", "ud", "uleb", "unpckhpd", "unpckhps", "unpcklpd",
			"unpcklps", "value", "verr", "verw", "wait", "wbinvd", "weak", "whitespace", "wrmsr", "xadd", "xchg",
			"xchgA", "xlat", "xlatb", "xor", "xorpd", "xorps", "zero" }; // não vai colorir "str", "string"
	
	public static final String[] sections = { "data", "text", "bss", "DATA", "TEXT", "BSS" };
	
	public static final String[] jlKeys = { "baremodule", "begin", "break", "catch", "const", "continue", "do", "else",
			"elseif", "end", "export", "false", "finally", "for", "function", "global", "if", "import",
			"let", "local", "macro", "module", "quote", "return", "struct", "true", "try", "using", "while" };
	
	public static final String[] plKeys = { "-A", "END", "length", "setpgrp", "-B", "endgrent", "link", "setpriority", "-b",
			"endhostnet", "listen", "setprotoent", "-C", "endnetent", "local", "setpwent", "-c", "endprotoent",
			"localtime", "setservent", "-d", "endpwent", "log", "setsockopt", "-e", "endservent", "lstat",
			"shift", "-f", "eof", "map", "shmctl", "-g", "eval", "mkdir", "shmget", "-k", "exec", "msgctl",
			"shmread", "-l", "exists", "msgget", "shmwrite", "-M", "exit", "msgrcv", "shutdown", "-O", "fcntl",
			"msgsnd", "sin", "-o", "fileno", "my", "sleep", "-p", "flock", "next", "socket", "-r", "fork", "not",
			"socketpair", "-R", "format", "oct", "sort", "-S", "formline", "open", "splice", "-s", "getc", "opendir",
			"split", "-T", "getgrent", "ord", "sprintf", "-t", "getgrgid", "our", "sqrt", "-u", "getgrnam", "pack",
			"srand", "-w", "gethostbyaddr", "pipe", "stat", "-W", "gethostbyname", "pop", "state", "-X", "gethostent",
			"pos", "study", "-x", "getlogin", "print", "substr", "-z", "getnetbyaddr", "printf", "symlink", "abs",
			"getnetbyname", "prototype", "syscall", "accept", "getnetent", "push", "sysopen", "alarm", "getpeername",
			"quotemeta", "sysread", "atan2", "getpgrp", "rand", "sysseek", "AUTOLOAD", "getppid", "read", "system",
			"BEGIN", "getpriority", "readdir", "syswrite", "bind", "getprotobyname", "readline", "tell", "binmode",
			"getprotobynumber", "readlink", "telldir", "bless", "getprotoent", "readpipe", "tie", "break", "getpwent",
			"recv", "tied", "caller", "getpwnam", "redo", "time", "chdir", "getpwuid", "ref", "times", "CHECK",
			"getservbyname", "rename", "truncate", "chmod", "getservbyport", "rename", "umask", "chown", "getsockopt",
			"reverse", "undef", "chr", "glob", "rewinddir", "UNITCHECK", "chroot", "gmtime", "rindex", "unlink",
			"close", "goto", "rmdir", "unpack", "closedir", "grep", "say", "unshift", "connect", "hex", "scalar",
			"untie", "cos", "index", "seek", "use", "crypt", "INIT", "seekdir", "utime", "dbmclose", "int",
			"select", "values", "dbmopen", "ioctl", "semctl", "vec", "defined", "join", "semget", "wait", "delete",
			"keys", "semop", "waitpid", "DESTROY", "kill", "send", "wantarray", "die", "last", "setgrent", "warn",
			"dump", "lc", "sethostent", "write", "each", "lcfirst", "setnetent", "__DATA__", "else", "lock", "qw",
			"__END__", "elsif", "lt", "qx", "__FILE__", "eq", "m", "s", "__LINE__", "exp", "ne", "sub", "__PACKAGE__",
			"for", "no", "tr", "and", "foreach", "or", "unless", "cmp", "ge", "package", "until", "continue", "gt",
			"q", "while", "CORE", "if", "qq", "xor", "do", "le", "qr", "y" };
	
	public static final String[] hasKeys = { "as", "case", "of", "class", "data", "family", "data", "instance",
			"default", "deriving", "do", "forall", "foreign", "hiding", "if", "then", "else",
			"import", "infix", "infixl", "infixr", "let", "in", "mdo", "module", "newtype", "proc",
			"qualified", "rec", "type", "where" };
	
	public static final String[] fsKeys = { "abstract", "and", "as", "assert", "base", "begin", "class", "default",
			"delegate", "do", "done", "downcast", "downto", "elif", "else", "end", "exception",
			"extern", "false", "finally", "fixed", "for", "fun", "function", "global", "if", "in",
			"inherit", "inline", "interface", "internal", "lazy", "let", "match", "member", "module",
			"mutable", "namespace", "new", "not", "null", "of", "open", "or", "override", "private",
			"public", "rec", "return", "select", "static", "struct", "then", "to", "true", "try", "type",
			"upcast", "use", "val", "void", "when", "while", "with", "yield", "const", "asr", "land", "lor",
			"lsl", "lsr", "lxor", "mod", "sig", "atomic", "break", "checked", "component", "const", "constraint",
			"constructor", "continue", "eager", "event", "external", "functor", "include", "method", "mixin",
			"object", "parallel", "process", "protected", "pure", "sealed", "tailcall", "trait", "virtual", "volatile" };
	
	public static final String[] cfKeys = { "for", "while", "loop", "by", "in", "of", "break", "continue", "if",
			"then", "else", "unless", "switch", "when", "default", "return", "do", "is", "isnt",
			"and", "or", "not", "true", "yes", "on", "false", "no", "off", "throw", "try", "catch",
			"finally", "new", "delete", "class", "extends", "super", "typeof", "instanceof", "this",
			"arguments", "await", "defer", "yield", "null", "undefined", "Infinity", "NaN", "export",
			"import", "package", "let", "case", "debugger", "function", "var", "with", "private",
			"protected", "public", "native", "static", "const", "implements", "interface", "void", "enum" };
	
	public static final String[] swKeys = { "associatedtype", "class", "deinit", "enum", "extension", "fileprivate",
			"func", "import" , "init", "inout", "internal", "let", "open", "operator", "private",
			"protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var",
			"break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for",
			"guard", "if", "in", "repeat", "return", "switch", "where", "while", "as", "Any", "catch",
			"false", "is", "nil", "super", "self", "self", "throw", "throws", "true", "try", "_",
			"#available", "#colorLiteral", "#column", "#else", "#elseif", "#endif", "#error", "#file",
			"#fileID", "#fileLiteral", "#filePath", "#function", "#if", "#imageLiteral", "#line",
			"#selector", "#sourceLocation", "#warning", "associativity", "convenience", "dynamic",
			"didset", "final", "get", "infix", "indirect", "lazy", "left", "mutating", "none", "nonmutating",
			"optional", "override", "postfix", "precendence", "prefix", "Protocol", "required", "right",
			"set", "Type", "unowned", "weak", "willSet" };
	
	public static final String[] rsKeys = { "as", "break", "const", "continue", "crate", "else", "enum", "extern", "false",
			"fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref",
			"return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe", "use",
			"where", "while", "async", "await", "dyn", "abstract", "become", "box", "do", "final", "macro",
			"override", "priv", "typeof", "unsized", "virtual", "yield", "try", "union", "'static", "dyn" };
	
	
	public static final String[] shKeys = { "pwd", "cd", "ls", "cat", "cp", "mv", "mkdir", "rmdir", "rm", "touch", "locate", "find",
			"grep", "sudo", "df", "du", "head", "tail", "diff", "tar", "chmod", "chown", "jobs", "kill", "ping",
			"wget", "uname", "top", "history", "man", "echo", "zip", "unzip", "hostname", "useradd", "userdel",
			"clear", "git", "npm", "call", "exist", "end", "java", "javac", "javaw", "nodemon", "csc", "node", "nasm", "qemu", "gcc", "g++",
			"python", "lua", "bin", "if", "then", "else", "fi", "date",
			"PWD", "CD", "LS", "CAT", "CP", "MV", "MKDIR", "RMDIR", "RM", "TOUCH", "LOCATE", "FIND",
			"GREP", "SUDO", "DF", "DU", "HEAD", "TAIL", "DIFF", "TAR", "CHMOD", "CHOWN", "JOBS", "KILL", "PING",
			"WGET", "UNAME", "TOP", "HISTORY", "MAN", "ECHO", "ZIP", "UNZIP", "HOSTNAME", "USERADD", "USERDEL",
			"CLEAR", "GIT", "NPM", "CALL", "EXIST", "END",
			"JAVA", "JAVAC", "NODEMON", "CSC", "NODE", "QEMU", "GCC", "G++", "PYTHON", "LUA", "JAVAW", "BIN", "IF", "THEN", "ELSE", "FI", "DATE" };
	
	public static final String[] tsKeys = { "type", "number", "protected", "else", "let", "catch", "if",
			"case", "in", "byte", "double", "var", "module", "enum", "as", "transient", "document",
			"long", "undefined", "default", "goto", "native", "yield", "get", "typeof", "break",
			"abstract", "throw", "char", "return", "synchronized", "debugger", "do", "float", "while",
			"continue", "function", "export", "new", "package", "static", "void", "finally", "this",
			"throws", "eval", "extends", "null", "final", "true", "try", "implements", "private", "const",
			"import", "string", "for", "interface", "delete", "switch", "public", "of", "await", "class",
			"console", "false", "volatile", "any", "int", "instanceof", "super", "with", "async",
			"boolean", "short", "arguments", "window", "as", "from", "navigator" };
	
	public static final String[] ktKeys = { "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
			"if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return", "super",
			"this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while", "by",
			"catch", "constructor", "delegate", "dynamic", "field", "file", "finally", "get", "import",
			"init", "param", "property", "receiver", "set", "setparam", "value", "class", "where", "actual",
			"abstract", "annotation", "companion", "const", "crossinline", "data", "enum", "expect",
			"external", "final", "infix", "inline", "inner", "internal", "lateinit", "noinline", "open",
			"operator", "out", "override", "private", "protected", "public", "reified", "sealed", "suspend",
			"tailrec", "vararg", "field", "it" };
	
	public static final String[] rbKeys = { "_ENCODING_", "_LINE_", "_FILE_", "BEGIN", "END", "alias", "and", "begin",
			"break", "case", "class", "def", "defined?", "do", "else", "elsif", "end", "ensure", "false",
			"for", "if", "in", "module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return",
			"self", "super", "then", "true", "undef", "unless", "until", "when", "while", "yield" };
	
	public static final String[] scaKeys = { "abstract", "finally", "object", "trait", "catch", "forSome", "package",
			"try", "class", "if", "private", "type", "def", "implicit", "protected", "val", "else",
			"lazy", "sealed", "while", "false", "new", "this", "yield", "final", "null", "throw" };
	
	public static final String[] goKeys = { "break", "default", "func", "interface", "select", "case",
			"defer", "go", "map", "struct", "chan", "else", "goto", "package", "switch",
			"const", "fallthrough", "if", "range", "type", "continue", "for", "import", "return", "var" };
	
	public static final String[] objKeys = { "auto", "break", "case", "char", "const", "continue", "default", "do", "double",
			"else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register",
			"restrict", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef",
			"union", "unsigned", "void", "volatile", "while", "_Bool", "_Complex", "_Imaginary", "BOOL",
			"Class", "bycopy", "byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL", "oneway", "out",
			"Protocol", "SEL", "self", "super", "YES", "@interface", "@end", "@implementation", "@protocol",
			"@class", "@public", "@protected", "@private", "@property", "@try", "@throw", "@catch", "@finally",
			"@synthesize", "@dynamic", "@selector", "atomic", "nonatomic", "retain" };
	
	public static final String[] ideConfKeys = { "Arquivo de Configurações da Boot IDE", "Boot IDE Configuration File", "Colors", "Files", "Settings", "default" };
	
	public static final String[] makeKeys = { "if", "else", "make", "echo", "elif", "then", "fi", "exit", "export" };
	
	public static final String[] dkKeys = { "FROM", "RUN", "VOLUME", "WORKDIR", "ADD", "CMD", "ENTRYPOINT", "ENV", "EXPOSE", "MAINTAINER", "USER",
			"from", "run", "volume", "workdir", "add", "cmd", "entrypoint", "env", "expose", "maintainer", "user" };

	public static final String[] specialHtmlVariables = { "html" };
	
	public static final String[] jsonKeys = { "true", "false" };
	
	///////
	
	private static boolean hasPressed = false;
	private static Robot robot;
	
	public CodeEditor(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		
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
		
		cursorThread = new Thread() {
			public void run() {
				cursor.play();
			}
		};
		
		cursorThread.start();
		
		new Thread() {
			public void run() { // 25 pra frente com o explorer desligado, isso é uma gambiarrinha viu
				if (!isReadOnly && !alternateTabsMode) {
				
				int offset = CommandTerminal.expOff ? 280 : 0;
				int lcx = !CommandTerminal.expOff ? 0 : 280;
				
				int lcmx = mx;
				int lcmy = my;
				
				while (true) {
					lcmy = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
					lcmx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE));

					while (((lcx + 40) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						lcmx++;
					
					while (((lcx + 40) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset > MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						lcmx--;
					
					while (MIN_Y + lcmy * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 < MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						lcmy++;
					
					while (MIN_Y + lcmy * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						lcmy--;
					
					if (FONT_SIZE < 13)
						lcmx--;

					lcmx = setWithinBounds(lcmx, lcmy, true);
					lcmy = setWithinBounds(lcmx, lcmy, false);
					
					//////////////
					
					my = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
					mx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE));

					while (((x + 40) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						mx++;
					
					while (((x + 40) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX > MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						mx--;
					
					while (MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 < MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						my++;
					
					while (MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						my--;

					if (FONT_SIZE < 13)
						mx--;
					
					mx = setWithinBounds(mx, my, true);
					my = setWithinBounds(mx, my, false);
					
					if (CommandTerminal.expOff) {
						mx = lcmx;
						my = lcmy;
					}
					
					try {
						Thread.sleep(1000/120);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				}
			}
		}.start();
		
		/*try {
			gradient = ImageIO.read(getClass().getResource("/gradient.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	/**
	 * Retorna true ou false se o char c é um dos permitidos para colorir keywords.
	 * 
	 * @param c - o char
	 * @return true se sim, false se não.
	 */
	public static boolean isSpecialPermitted(char c) {
		return c == '_' || c == '[' || c == ']';
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
			
			if (c == chars) return i;
		}
		
		return -1;
	}
	
	public static <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
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
		}
		catch (Exception e) {
			l = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
			codeType = "ANSI";
		}
			
		if (l.isEmpty()) l.add("");
		
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
		
		new Thread() {				// quando vc deleta as linhas isso acontece mesmo
			public void run() {
				if (editing != null && editing.getRegent() != null && editing.getRegent().getRegent() != null)
				for (IDELine l : lines) {
					l.setFonts(
							automaticColor(
									toCharArray(
											l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
				}
			}
		}.start();
		
		String ext = ListableFile.getFileExtension(file);
		
		if (ext.equalsIgnoreCase(".pdf") || ext.equalsIgnoreCase(".jar") || ext.equalsIgnoreCase(".iso") || ext.equalsIgnoreCase(".img") || ext.equalsIgnoreCase(".flp") || ext.equalsIgnoreCase(".class") || ext.equalsIgnoreCase(".exe") || ext.equalsIgnoreCase(".urna") || ext.equalsIgnoreCase(".save") || ext.equalsIgnoreCase(".docx") || ext.equalsIgnoreCase(".pptx") || ext.equalsIgnoreCase(".one") || ext.equalsIgnoreCase(".psd") || ext.equalsIgnoreCase(".aed") || ext.equalsIgnoreCase(".ai") || ext.equalsIgnoreCase(".indd") || ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".dll") || ext.equalsIgnoreCase(".png") || ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".gif") || ext.equalsIgnoreCase(".bmp") || ext.equalsIgnoreCase(".ico") || ext.equalsIgnoreCase(".webp") || ext.equalsIgnoreCase(".mp4") || ext.equalsIgnoreCase(".wmv") || ext.equalsIgnoreCase(".avi") || ext.equalsIgnoreCase(".wav") || ext.equalsIgnoreCase(".mp3") || ext.equalsIgnoreCase(".ogg") || ext.equalsIgnoreCase(".otf") || ext.equalsIgnoreCase(".ttf") || ext.equalsIgnoreCase(".woff") || ext.equalsIgnoreCase(".woff2") || ext.equalsIgnoreCase(".zip") || ext.equalsIgnoreCase(".rar") || ext.equalsIgnoreCase(".7z") || ext.equalsIgnoreCase(".bin") || editing.isReadOnly) {
			isReadOnly = true;
		}
			
		return ls;
	}
	
	public static String[] mergeStringArrays(String[] arr1, String[] arr2) {
		String[] res = new String[arr1.length + arr2.length];
		
		System.arraycopy(arr1, 0, res, 0, arr1.length);
		System.arraycopy(arr2, 0, res, arr1.length, arr2.length);
		
		return res;
	}
	
	// Se for usar em arquivos que não têm extensão, use o método debaixo desse
	public static String[] getKeywords(String ext) {
		return switch (ext.toLowerCase()) {
			case ".java" -> javaKeys;
			case ".c" -> cKeys;
			case ".cpp" -> cppKeys;
			case ".hpp" -> cppKeys;
			case ".cxx" -> cppKeys;
			case ".hxx" -> cppKeys;
			case ".h" -> cppKeys;
			case ".cs" -> csKeys;
			case ".py" -> pyKeys;
			case ".pyd" -> pyKeys;
			case ".js" -> jsKeys;
			case ".mjs" -> jsKeys;
			case ".bat" -> batCom;
			case ".cmd" -> batCom;
			case ".com" -> batCom;
			case ".asm" -> asmKeys;
			case ".s" -> asmKeys;
			case ".lua" -> luaKeys;
			case ".sql" -> sqlKeys;
			case ".swift" -> swKeys;
			case ".rs" -> rsKeys;
			case ".php" -> phpKeys;
			case ".kt" -> ktKeys;
			case ".vue" -> jsKeys;
			case ".rb" -> rbKeys;
			case ".ino" -> cppKeys;
			case ".ts" -> tsKeys;
			case ".go" -> goKeys;
			case ".r" -> rKeys;
			case ".jl" -> jlKeys;
			case ".pl" -> plKeys;
			case ".has" -> hasKeys;
			case ".hs" -> hasKeys;
			case ".fs" -> fsKeys;
			case ".coffee" -> cfKeys;
			case ".m" -> objKeys;
			case ".pas" -> pasKeys;
			case ".pp" -> pasKeys;
			case ".scala" -> scaKeys;
			case ".dart" -> dartKeys;
			case ".zig" -> zigKeys;
			
			case ".html" -> cssTags;
			case ".htm" -> cssTags;
			case ".css" -> mergeStringArrays(cssTags, props);
			case ".scss" -> mergeStringArrays(cssTags, props);
			case ".json" -> jsonKeys;
			case ".jsonc" -> jsonKeys;
			case ".conf" -> ideConfKeys;
			case ".mk" -> makeKeys;
			case ".make" -> makeKeys;
			case ".makefile" -> makeKeys;
			case ".dockerfile" -> dkKeys;
			case ".jsx" -> jsKeys;
			case ".ps1" -> batCom;
			case ".sh" -> shKeys;
			case ".ejs" -> tags;
			case ".ld" -> ldKeys;
			
			default -> null;
		};
	}
	
	public static String[] getKeywordsSpecial(String filename) {
		return switch (filename.toLowerCase()) {
			case "makefile" -> makeKeys;
			case "dockerfile" -> dkKeys;
			
			default -> null;
		};
	}
	
	public static BufferedImage getAutoCompleteIcon(AutoCompleteType type) {
		return switch (type) {
			case FUNCTION -> functions;
			case OBJECT -> objects;
			case KEYWORD -> keywords;
			case VARIABLE -> variables;
		};
	}
	
	public void addAutoCompleteAdds(List<String> list, AutoCompleteType type) {
		for (String s : list)
			addautocompleteadds.add(new AutoComplete(s, type));
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
	
	public static boolean isCharsEqual(char c1, char c2) {
		String str1 = String.valueOf(c1);
		String str2 = String.valueOf(c2);
		
		return str1.equals(str2);
	}
	
	public List<IDEFont> colorVariablesAndObjects(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if ((ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".c") || ext.equalsIgnoreCase(".cs") || ext.equalsIgnoreCase(".cpp") || ext.equalsIgnoreCase(".cxx") || ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".mjs") ||
				 ext.equalsIgnoreCase(".h") || ext.equalsIgnoreCase(".hpp") || ext.equalsIgnoreCase(".hxx") || ext.equalsIgnoreCase(".lua") || ext.equalsIgnoreCase(".rs") || ext.equalsIgnoreCase(".asm") ||
				 ext.equalsIgnoreCase(".php") || ext.equalsIgnoreCase(".kt") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".rb") || ext.equalsIgnoreCase(".ino") ||
				 ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".swift")  || ext.equalsIgnoreCase(".go") || ext.equalsIgnoreCase(".r") ||
				 ext.equalsIgnoreCase(".jl") || ext.equalsIgnoreCase(".pl") || ext.equalsIgnoreCase(".has") || ext.equalsIgnoreCase(".hs") || ext.equalsIgnoreCase(".fs") || ext.equalsIgnoreCase(".coffee") ||
				 ext.equalsIgnoreCase(".m") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".pas") || ext.equalsIgnoreCase(".pp") || ext.equalsIgnoreCase(".scala") ||
				 ext.equalsIgnoreCase(".dart") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") ||
				 ext.equalsIgnoreCase(".url") || ext.equalsIgnoreCase(".zig") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) { // não verificaremos mais o html aqui kikikikiki
				
			if (!(ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown"))) {
				for (String s : syms) {
					indxs = findWord(new String(chars), s); // antes de
					
					for (Integer i : indxs) {
						int c = i;
						int len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0) {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
						
						//addautocompleteadds.add(new AutoComplete(wordSinceSpace, AutoCompleteType.VARIABLE));
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), s);
					
					int len = 0;

					for (Integer i : indxs) {
						while (i + len < chars.length)
							len++;

						if (i + len < chars.length)
							fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
				}
			
			if (!(ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) {
					indxs = findWord(new String(chars), ")");
					
					for (Integer i : indxs) {
						int c = i;
						int len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), "]");
					
					for (Integer i : indxs) {
						int c = i;
						int len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '[' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
			}
					
					if (!(ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1"))) {
						indxs = findWord(new String(chars), "->");
						
						for (Integer i : indxs) {
							int c = i;
							int len = 0;
								
							while (c < chars.length && 
									c + len < chars.length &&
									c > 0 &&
									chars[c] != ' ' &&
									chars[c] != ']' &&
									chars[c] != ',' &&
									chars[c] != '.' &&
									chars[c] != ':') {
								c--;
								len++;
							}
								
							fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
						}
					}
				}
				
				if (!(ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1"))) {
				
				String[] cll = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
						"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

				for (String s : cll) {
					indxs = findWord(new String(chars), s);
				
				int len = 0;

				String str = new String(chars);
				
				for (Integer i : indxs) {
					
					if (i - 1 > 0 &&
						(str.charAt(i - 1) == 'a' ||
						 str.charAt(i - 1) == 'b' ||
						 str.charAt(i - 1) == 'c' ||
						 str.charAt(i - 1) == 'd' ||
						 str.charAt(i - 1) == 'e' ||
						 str.charAt(i - 1) == 'f' ||
						 str.charAt(i - 1) == 'g' ||
						 str.charAt(i - 1) == 'h' ||
						 str.charAt(i - 1) == 'i' ||
						 str.charAt(i - 1) == 'j' ||
						 str.charAt(i - 1) == 'k' ||
						 str.charAt(i - 1) == 'l' ||
						 str.charAt(i - 1) == 'm' ||
						 str.charAt(i - 1) == 'n' ||
						 str.charAt(i - 1) == 'o' ||
						 str.charAt(i - 1) == 'p' ||
						 str.charAt(i - 1) == 'q' ||
						 str.charAt(i - 1) == 'r' ||
						 str.charAt(i - 1) == 's' ||
						 str.charAt(i - 1) == 't' ||
						 str.charAt(i - 1) == 'u' ||
						 str.charAt(i - 1) == 'v' ||
						 str.charAt(i - 1) == 'w' ||
						 str.charAt(i - 1) == 'x' ||
						 str.charAt(i - 1) == 'y' ||
						 str.charAt(i - 1) == 'z'))
						continue;
					
					while (i + len < chars.length && 
							!isCharsEqual(chars[i + len], ' ') &&
							!isCharsEqual(chars[i + len], '[') &&
							!isCharsEqual(chars[i + len], ']') &&
							!isCharsEqual(chars[i + len], '(') &&
							!isCharsEqual(chars[i + len], ')') &&
							!isCharsEqual(chars[i + len], '{') &&
							!isCharsEqual(chars[i + len], '}') &&
							!isCharsEqual(chars[i + len], ',') &&
							!isCharsEqual(chars[i + len], ';') &&
							!isCharsEqual(chars[i + len], '.') &&
							!isCharsEqual(chars[i + len], ':') &&
							!isCharsEqual(chars[i + len], '=') &&
							!isCharsEqual(chars[i + len], '\"') &&
							!isCharsEqual(chars[i + len], '\'')) {
							len++;
					}

					if (i + len < chars.length) {
						if (ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".makefile") || ext.equalsIgnoreCase(".mk") || ext.equalsIgnoreCase(".make") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))
							fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
						else {
							if (i - 1 > 0 && Character.isLetter(chars[i - 1])) continue;
							
							fs = color(i, i + len, new IDEFont(Fonts.objectsNormal, FONT_SIZE), fs);
						}
					}
				}
			}
			}
				
				if (ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".zig")) {
					indxs = findWord(new String(chars), "@");
					
					int len = 0;

					for (Integer i : indxs) {
						while (i + len < chars.length && 
								!isCharsEqual(chars[i + len], ' ') &&
								!isCharsEqual(chars[i + len], '[') &&
								!isCharsEqual(chars[i + len], ']') &&
								!isCharsEqual(chars[i + len], '(') &&
								!isCharsEqual(chars[i + len], ')') &&
								!isCharsEqual(chars[i + len], ',') &&
								!isCharsEqual(chars[i + len], ';') &&
								!isCharsEqual(chars[i + len], '.') &&
								!isCharsEqual(chars[i + len], ':') &&
								!isCharsEqual(chars[i + len], '=') &&
								!isCharsEqual(chars[i + len], '\"') &&
								!isCharsEqual(chars[i + len], '\'')) {
								len++;
						}

						if (i + len < chars.length)
							fs = color(i, i + len, new IDEFont(Fonts.lightGrayNormal, FONT_SIZE), fs);
					}
				}
			}
		
		return fs;
	}
	
	public List<IDEFont> colorKeywords(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		switch (ext.toLowerCase()) {
		case ".java":
			if (!foundExt) {
				extType = "Java";
				foundExt = true;
			}
			
			for (String s : javaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);			// !(lines.get(getLineIndex(chars)).getFonts().get(i + s.length()).getFont().equals(Fonts.methodsNormal))
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}	
			}
			
			break;
		
		case ".ejs":
			if (!foundExt) {
				extType = "Embedded JavaScript - EJS";
				foundExt = true;
			}
		case ".cfg":
		case ".config":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Configurações" : "Configuration File";
				foundExt = true;
			}
		case ".xml":
			if (!foundExt) {
				extType = "Extensible Markup Language - XML";
				foundExt = true;
			}
		case ".svg":
			if (!foundExt) {
				extType = "Scalable Vector Graphics - SVG";
				foundExt = true;
			}
		case ".htm":	
		case ".html":
			if (!foundExt) {
				extType = "Hyper Text Markup Language - HTML";
				foundExt = true;
			}
			
			for (String s : specialHtmlVariables) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}		 
			
			for (String s : tags) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			if (isCssPart || isJSPart || isPhpPart)
				for (String s : nums) { // colorir números
					indxs = findWord(new String(chars), s);
	
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
				}
			
			indxs = findWord(new String(chars), "0x");
			
			int len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != '(' &&
						chars[i + len] != ')' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
			}
			
			if (ext.equals(".xml") || ext.equals(".svg") || ext.equals(".config") || ext.equals(".cfg")) {
				indxs = findWord(new String(chars), ">"); // colorir final de tags
				
				for (Integer i : indxs) {
					fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "<");
	
				len = 0;
	
				for (Integer i : indxs) {
					while (i + len < chars.length &&
							chars[i + len] != ' ' &&
							chars[i + len] != '[' &&
							chars[i + len] != ']' &&
							chars[i + len] != ',' &&
							chars[i + len] != ';' &&
							chars[i + len] != '.' &&
							chars[i + len] != ':' &&
							chars[i + len] != '>')
							len++;
	
					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "</");
	
				len = 0;
	
				for (Integer i : indxs) {
					while (i + len < chars.length &&
							chars[i + len] != ' ' &&
							chars[i + len] != '[' &&
							chars[i + len] != ']' &&
							chars[i + len] != ',' &&
							chars[i + len] != ';' &&
							chars[i + len] != '.' &&
							chars[i + len] != ':')
							len++;
	
					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			indxs = findWord(new String(chars), "="); // html
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<style");
			
			if (indxs.size() > 0)
				isCssPart = true;
			
			indxs = findWord(new String(chars), "</style");
			
			if (indxs.size() > 0)
				isCssPart = false;
			
			indxs = findWord(new String(chars), "<script");
			
			if (indxs.size() > 0)
				isJSPart = true;
			
			indxs = findWord(new String(chars), "</script");
			
			if (indxs.size() > 0)
				isJSPart = false;
			
			indxs = findWord(new String(chars), "<?php");
			
			if (indxs.size() > 0)
				isPhpPart = true;
			
			indxs = findWord(new String(chars), "?>");
			
			if (indxs.size() > 0)
				isPhpPart = false;
			
			if (isPhpPart) {
				for (String s : phpKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs) {
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
						
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
					}
				}
			}
			
			if (isJSPart) {
				for (String s : jsKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs) {
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
						
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
					}
				}
				
				indxs = findWord(new String(chars), ")");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '(') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					indxs = findWord(new String(chars), ":");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ";");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
					}
					
					indxs = findWord(new String(chars), "[");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), "->");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					
					indxs = findWord(new String(chars), "=");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(' &&
								chars[c] != ':') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
			}
			
			if (isCssPart) {
				for (String s : cssTags) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : props) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : units) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ".");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "#");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), ";");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != '.' &&
							chars[c] != '#' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ',' &&
							chars[c] != ';' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
			}
			
			fs = colorMethods(ext, chars, fs);
			
			break;
			
		case ".scss":
		case ".css":
			if (!foundExt) {
				extType = "Cascading Style Sheets - CSS"; // TODO corrigir o problema do src: 
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != '#' &&
						chars[c] != '!') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}

			for (String s : cssTags) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			for (String s : units) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			for (String s : props) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), ".");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{') 
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "#"); // ids
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "&"); // scss selectors
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "$"); // scss selectors
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ":"); // atributos de tags
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '{')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ";");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ':' &&
						chars[c] != '{') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "]");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			// Eu sei que a linha de código abaixo infringe a lei do Boot de Código-Fonte bem escrito n° 547, e pode accaretar problemas :/
			
			fs = colorMethods(ext, chars, fs);
			
			break;
			
		case ".py":
		case ".pyd":
			if (!foundExt) {
				extType = "Python";
				foundExt = true;
			}
			
			for (String s : pyKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".dart":
			if (!foundExt) {
				extType = "Dart";
				foundExt = true;
			}
			
			for (String s : dartKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".ld":
			if (!foundExt) {
				extType = "LinkerScript";
				foundExt = true;
			}
			
			for (String s : ldKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".pp":
		case ".pas":
			if (!foundExt) {
				extType = "Pascal";
				foundExt = true;
			}
			
			for (String s : pasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".c":
			if (!foundExt) {
				extType = "C";
				foundExt = true;
			}
			
			for (String s : cKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}	
			
			break;
		
		case ".h":
			if (!foundExt) {
				extType = "C/C++ Header";
				foundExt = true;
			}
		case ".ino":
			if (!foundExt) {
				extType = "Arduino";
				foundExt = true;
			}
		case ".hpp":
		case ".hxx":
			if (!foundExt) {
				extType = "C++ Header";
				foundExt = true;
			}
		case ".cxx":
		case ".cpp":
			if (!foundExt) {
				extType = "C++";
				foundExt = true;
			}
			
			for (String s : cppKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".cs":
			if (!foundExt) {
				extType = "C#";
				foundExt = true;
			}
			
			for (String s : csKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".r":
			if (!foundExt) {
				extType = "R";
				foundExt = true;
			}
			
			for (String s : rKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
		
		case ".license":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Licença" : "License File";
				foundExt = true;
			}
			break;
			
		case ".ps1":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do PowerShell" : "PowerShell File";
				foundExt = true;
			}
		case ".cmd":
		case ".com":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do Prompt de Comando" : "Command Prompt File";
				foundExt = true;
			}
		case ".bat":
			if (!foundExt) {
				extType = "Batch";
				foundExt = true;
			}
			
			for (String s : batCom) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			for (String s : extensions) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), "/");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "-");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "%");		// se quiser fazer entre %% tem que fazer uma variável boolean de controle, como o multilinecommenting.
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".jsx":
			if (!foundExt) {
				extType = "React";
				foundExt = true;
			}
		case ".vue":	
			if (!foundExt) {
				extType = "Vue.js";
				foundExt = true;
			}
		case ".mjs":
		case ".js":
			if (!foundExt) {
				extType = "JavaScript";	// TODO - tomar cuidado em colorir tags em HTML mesmo dentro da JSPart ou CssPart viu
				foundExt = true;
			}
			
			for (String s : jsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".lua":
			if (!foundExt) {
				extType = "Lua";
				foundExt = true;
			}
			
			for (String s : luaKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;
			
		case ".zig":
			if (!foundExt) {
				extType = "Zig";
				foundExt = true;
			}
			
			for (String s : zigKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;
			
		case ".sql":
			if (!foundExt) {
				extType = "Structured Query Language - SQL";
				foundExt = true;
			}
			
			for (String s : sqlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;
		
		case ".s":
		case ".asm":
			if (!foundExt) {
				extType = "Assembly";
				foundExt = true;
			}
			
			for (String s : asmRegs) {
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			for (String s : asmKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			for (String s : sections) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}

			indxs = findWord(new String(chars), "db");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "dw");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "equ");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ".");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".jl":
			extType = "Julia";
			foundExt = true;
			
			for (String s : jlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".pl":
			if (!foundExt) {
				extType = "Perl";
				foundExt = true;
			}
			
			for (String s : plKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
					
				}
			}
			
			break;
			
		case ".hs":
		case ".has":
			if (!foundExt) {
				extType = "Haskell";
				foundExt = true;
			}
			
			for (String s : hasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset	
				}
			}
			
			break;
			
		case ".fs":
			if (!foundExt) {
				extType = "F#";
				foundExt = true;
			}
			
			for (String s : fsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".coffee":
			if (!foundExt) {
				extType = "CoffeeScript";
				foundExt = true;
			}
			
			for (String s : cfKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".markdown":
		case ".md":
			if (!foundExt) {
				extType = "Markdown";
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), "#");
			
			for (Integer i : indxs)
				fs = color(i, fs.size(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			
			for (String s : tags) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), "="); // html
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
		
		case ".log":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Log" : "Log File";
				foundExt = true;
			}
		case ".txt":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Texto" : "Text File";
				foundExt = true;
			}
			break;
			
		case ".ini":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Parâmetros de Configurações" : "Configuration Parameters File"; // remover 'arquivo de'
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), "]");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ':') {
					c--;
					len++;
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "=");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".swift":
			if (!foundExt) {
				extType = "Swift";
				foundExt = true;
			}
			
			for (String s : swKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset	
				}
			}
			
			break;
			
		case ".rs":
			if (!foundExt) {
				extType = "Rust";
				foundExt = true;
			}
			
			for (String s : rsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			break;
			
		case ".sh":
			if (!foundExt) {
				extType = "Bourne Again Shell - Bash";
				foundExt = true;
			}
			
			for (String s : shKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			for (String s : extensions) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), "/");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "-");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break; // Release v3.9.1 - 12/08/2021 - 08:03
			
		case ".php":
			if (!foundExt) {
				extType = "Hypertext Preprocessor - PHP"; // será q esse hypertext tá certo?
				foundExt = true;
			}
			
			for (String s : phpKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			for (String s : tags) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			indxs = findWord(new String(chars), "=");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<style");
			
			if (indxs.size() > 0)
				isCssPart = true;
			
			indxs = findWord(new String(chars), "</style");
			
			if (indxs.size() > 0)
				isCssPart = false;
			
			indxs = findWord(new String(chars), "<script");
			
			if (indxs.size() > 0)
				isJSPart = true;
			
			indxs = findWord(new String(chars), "</script");
			
			if (indxs.size() > 0)
				isJSPart = false;
			
			if (isJSPart) {
				
				for (String s : jsKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ")");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '(') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					//boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					indxs = findWord(new String(chars), ":");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ";");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
					}
					
					indxs = findWord(new String(chars), "[");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), "->");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					
					indxs = findWord(new String(chars), "=");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(' &&
								chars[c] != ':') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
			}
			
			if (isCssPart) {
				for (String s : cssTags) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs) {
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
						
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
					}
				}
				
				for (String s : props) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs) {
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
						
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
					}
				}
				
				for (String s : units) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs) {
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
						
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
					}
				}
				
				indxs = findWord(new String(chars), ".");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "#");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), ";");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != '.' &&
							chars[c] != '#' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ',' &&
							chars[c] != ';' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
			}
			
			fs = colorMethods(ext, chars, fs);
			
			break;
			
		case ".ts":
			if (!foundExt) {
				extType = "TypeScript";
				foundExt = true;
			}
			
			for (String s : tsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			break;
		
		case ".jsonc":
			if (!foundExt) {
				extType = "JavaScript Object Notation with Comments - JSONC";
				foundExt = true;
			}
		case ".json":
			if (!foundExt) {
				extType = "JavaScript Object Notation - JSON";
				foundExt = true;
			}
			
			for (String s : jsonKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".kt":
			if (!foundExt) {
				extType = "Kotlin";
				foundExt = true;
			}
			
			for (String s : ktKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			break;
			
		case ".rb":
			if (!foundExt) {
				extType = "Ruby";
				foundExt = true;
			}
			
			for (String s : rbKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".scala":
			if (!foundExt) {
				extType = "Scala";
				foundExt = true;
			}
			
			for (String s : scaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".go":
			if (!foundExt) {
				extType = "Go";
				foundExt = true;
			}
			
			for (String s : goKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".m":
			if (!foundExt) {
				extType = "Objective-C";
				foundExt = true;
			}
			
			for (String s : objKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			break;
			
		case ".jar":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo Jar" : "Jar File";
				foundExt = true;
			}
			break;
			
		case ".iso":
		case ".img":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Imagem de Disco" : "Disc Image File";
				foundExt = true;
			}
			break;
			
		case ".flp":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Disquete" : "Floppy Disk File";
				foundExt = true;
			}
			break;
			
		case ".urna":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Urna Salva do Criador de Urnas" : "Bollot Box Saved from Criador de Urnas"; // vc sabe que nome próprio não se traduz né
				foundExt = true;
			}
			break;
			
		case ".class":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo Bytecode do Java" : "Java Bytecode File";
				foundExt = true;
			}
			break;
			
		case ".save":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Jogo Salvo do World's Hardest Game Maker 2" : "Saved Game from World's Hardest Game Maker 2";
				foundExt = true;
			}
			break;
			
		case ".conf":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Configurações da Boot IDE" : "Boot IDE Configuration File";
				foundExt = true;
			}
			
			for (String s : ideConfKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);			// haha slk mermão colorir coisas de até próprio arquivo de configurações
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
		
		case ".7z":
		case ".rar":
		case ".zip":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo Compactado" : "Zipped File";
				foundExt = true;
			}
			break;
			
		case ".bin":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo Binário" : "Binary File";
				foundExt = true;
			}
			break;
		
		case ".makefile":
		case ".mk":
		case ".make":
			if (!foundExt) {
				extType = "Makefile";
				foundExt = true;
			}
			
			for (String s : makeKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			break;

		case ".ttf":
		case ".otf":
		case ".woff":
		case ".woff2":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo de Fonte" : "Font File";
				foundExt = true;
			}
			break;
			
		case ".dll":
			if (!foundExt) {
				extType = "Dynamic Link Library - DLL";
				foundExt = true;
			}
			
			break;
			
		case ".lock":
			if (!foundExt) {
				extType = "Lock File";
				foundExt = true;
			}
			break;
			
		case ".gitignore":
			if (!foundExt) {
				extType = "Git Ignore";
				foundExt = true;
			}
			
			break;
			
		case ".docx":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Documento do Microsoft Word" : "Microsoft Word Document";
				foundExt = true;
			}
			
			break;
			
		case ".xlsx":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Planilha do Microsoft Excel" : "Microsoft Excel Spreadsheet";
				foundExt = true;
			}
			
			break;
			
		case ".pptx":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Apresentação do Microsoft PowerPoint" : "Microsoft PowerPoint Presentation File";
				foundExt = true;
			}
			
			break;
			
		case ".one":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do Microsoft OneNote" : "Microsoft OneNote File";
				foundExt = true;
			}
			
			break;
			
		case ".psd":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do Photoshop" : "Photoshop File";
				foundExt = true;
			}
			
			break;
			
		case ".aed":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do After Effects" : "After Effects File";
				foundExt = true;
			}
			
			break;
			
		case ".ai":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do Illustrator" : "Illustrator File";
				foundExt = true;
			}
			
			break;
			
		case ".indd":
			if (!foundExt) {
				extType = Main.lang == Language.PORT ? "Arquivo do InDesign" : "InDesign File";
				foundExt = true;
			}
			
			break;
			
		case ".dockerfile":
			if (!foundExt) {
				extType = "Dockerfile";
				foundExt = true;
			}
			
			for (String s : dkKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
			}
			
			break;
			
		case ".url":
			if (!foundExt) {
				extType = "Uniform Resource Locator - URL";
				foundExt = true;
			}
			
			break;
		}
		
		return fs;
	}
	
	public List<IDEFont> colorMethods(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if ((ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".c") || ext.equalsIgnoreCase(".cs") || ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss") || ext.equalsIgnoreCase(".cpp") || ext.equalsIgnoreCase(".cxx") || ext.equalsIgnoreCase(".js") ||
				 ext.equalsIgnoreCase(".h") || ext.equalsIgnoreCase(".hpp") || ext.equalsIgnoreCase(".hxx") || ext.equalsIgnoreCase(".lua") || ext.equalsIgnoreCase(".rs") || ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") ||
				 ext.equalsIgnoreCase(".php") || ext.equalsIgnoreCase(".kt") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".rb") || ext.equalsIgnoreCase(".ino") ||
				 ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".swift") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".go") || ext.equalsIgnoreCase(".r") ||
				 ext.equalsIgnoreCase(".jl") || ext.equalsIgnoreCase(".pl") || ext.equalsIgnoreCase(".has") || ext.equalsIgnoreCase(".hs") || ext.equalsIgnoreCase(".fs") || ext.equalsIgnoreCase(".coffee") ||
				 ext.equalsIgnoreCase(".m") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".pas") || ext.equalsIgnoreCase(".pp") || ext.equalsIgnoreCase(".scala") || ext.equalsIgnoreCase(".dart") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown") ||
				 ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".conf") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".xml") ||
				 ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".makefile") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") ||
				 ext.equalsIgnoreCase(".url") || ext.equalsIgnoreCase(".zig") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) {
			
			if (!(ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) {
				
				// primeira vez usando labels!
				methods:
					if (!(ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown"))) {
						if (ext.equalsIgnoreCase(".html") | ext.equalsIgnoreCase(".htm") | ext.equalsIgnoreCase(".xml") | ext.equalsIgnoreCase(".ejs")) {
							if (!(isCssPart || isJSPart || isPhpPart)) break methods;
						}
						
						indxs = findWord(new String(chars), "(");
						
						for (Integer i : indxs) {
							int c = i;
							int len = 0;
							
							while (c < chars.length && 
									c + len < chars.length &&
									c > 0 &&
									chars[c] != ' ' &&
									chars[c] != '[' &&
									chars[c] != ']' &&
									chars[c] != ',' &&
									chars[c] != ';' &&
									chars[c] != '.' &&
									chars[c] != ':' &&
									chars[c] != '#' &&
									chars[c] != '!') {
								c--;
								len++;
							}
							
							fs = color(c, c + len, new IDEFont(Fonts.methodsNormal, FONT_SIZE), fs);
						}
					}
				}
			}
		
		return fs;
	}
	
	public List<IDEFont> colorNumbers(String ext, char[] chars, List<IDEFont> fs) {
		if (!(ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".c") || ext.equalsIgnoreCase(".cs") || ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss") || ext.equalsIgnoreCase(".cpp") || ext.equalsIgnoreCase(".cxx") || ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".mjs") ||
				 ext.equalsIgnoreCase(".h") || ext.equalsIgnoreCase(".hpp") || ext.equalsIgnoreCase(".hxx") || ext.equalsIgnoreCase(".lua") || ext.equalsIgnoreCase(".rs") || ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") ||
				 ext.equalsIgnoreCase(".php") || ext.equalsIgnoreCase(".kt") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".rb") || ext.equalsIgnoreCase(".ino") ||
				 ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".swift") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".go") || ext.equalsIgnoreCase(".r") ||
				 ext.equalsIgnoreCase(".jl") || ext.equalsIgnoreCase(".pl") || ext.equalsIgnoreCase(".has") || ext.equalsIgnoreCase(".hs") || ext.equalsIgnoreCase(".fs") || ext.equalsIgnoreCase(".coffee") ||
				 ext.equalsIgnoreCase(".m") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".pas") || ext.equalsIgnoreCase(".pp") || ext.equalsIgnoreCase(".scala") || ext.equalsIgnoreCase(".dart") ||
				 ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".conf") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".xml") ||
				 ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".makefile") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") ||
				 ext.equalsIgnoreCase(".url") || ext.equalsIgnoreCase(".zig") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) return fs;
		
		List<Integer> indxs = new ArrayList<>();
		
		if (!(ext.equalsIgnoreCase(".html") | ext.equalsIgnoreCase(".htm") | ext.equalsIgnoreCase(".xml") | ext.equalsIgnoreCase(".ejs") | ext.equalsIgnoreCase(".txt") | ext.equalsIgnoreCase(".log"))) {
			for (String s : nums) { // colorir números
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs) {
					if (ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown")) continue;
					
					if (s.length() > 1)
						if (i + s.length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + s.length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + s.length()] == '_'))) continue;
					
					fs = color(i, i + s.length(), new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
				}
			}
			
			indxs = findWord(new String(chars), "0x");
			
			int len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != '(' &&
						chars[i + len] != ')' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
			}
		}
	
		return fs;
	}
	
	public List<IDEFont> colorSymbols(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if ((ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".c") || ext.equalsIgnoreCase(".cs") || ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss") || ext.equalsIgnoreCase(".cpp") || ext.equalsIgnoreCase(".cxx") || ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".mjs") ||
				 ext.equalsIgnoreCase(".h") || ext.equalsIgnoreCase(".hpp") || ext.equalsIgnoreCase(".hxx") || ext.equalsIgnoreCase(".lua") || ext.equalsIgnoreCase(".rs") || ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") ||
				 ext.equalsIgnoreCase(".php") || ext.equalsIgnoreCase(".kt") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".rb") || ext.equalsIgnoreCase(".ino") ||
				 ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".swift") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".go") || ext.equalsIgnoreCase(".r") ||
				 ext.equalsIgnoreCase(".jl") || ext.equalsIgnoreCase(".pl") || ext.equalsIgnoreCase(".has") || ext.equalsIgnoreCase(".hs") || ext.equalsIgnoreCase(".fs") || ext.equalsIgnoreCase(".coffee") ||
				 ext.equalsIgnoreCase(".m") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".pas") || ext.equalsIgnoreCase(".pp") || ext.equalsIgnoreCase(".scala") || ext.equalsIgnoreCase(".dart") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown") ||
				 ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".conf") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".xml") ||
				 ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".makefile") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") ||
				 ext.equalsIgnoreCase(".url") || ext.equalsIgnoreCase(".zig") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) {
			
			for (String s : syms) {
				indxs = findWord(new String(chars), s);
		
				for (Integer i : indxs)
					fs = color(i, i + 1, new IDEFont(Fonts.symbolsNormal, FONT_SIZE), fs);
			}
		}
		
		return fs;
	}
	public List<IDEFont> colorExtras(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if ((ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".c") || ext.equalsIgnoreCase(".cs") || ext.equalsIgnoreCase(".css") || ext.equalsIgnoreCase(".scss") || ext.equalsIgnoreCase(".cpp") || ext.equalsIgnoreCase(".cxx") || ext.equalsIgnoreCase(".js") || ext.equalsIgnoreCase(".mjs") ||
				 ext.equalsIgnoreCase(".h") || ext.equalsIgnoreCase(".hpp") || ext.equalsIgnoreCase(".hxx") || ext.equalsIgnoreCase(".lua") || ext.equalsIgnoreCase(".rs") || ext.equalsIgnoreCase(".asm") || ext.equalsIgnoreCase(".s") ||
				 ext.equalsIgnoreCase(".php") || ext.equalsIgnoreCase(".kt") || ext.equalsIgnoreCase(".vue") || ext.equalsIgnoreCase(".py") || ext.equalsIgnoreCase(".pyd") || ext.equalsIgnoreCase(".rb") || ext.equalsIgnoreCase(".ino") ||
				 ext.equalsIgnoreCase(".ts") || ext.equalsIgnoreCase(".swift") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".go") || ext.equalsIgnoreCase(".r") ||
				 ext.equalsIgnoreCase(".jl") || ext.equalsIgnoreCase(".pl") || ext.equalsIgnoreCase(".has") || ext.equalsIgnoreCase(".hs") || ext.equalsIgnoreCase(".fs") || ext.equalsIgnoreCase(".coffee") ||
				 ext.equalsIgnoreCase(".m") || ext.equalsIgnoreCase(".jsx") || ext.equalsIgnoreCase(".ld") || ext.equalsIgnoreCase(".pas") || ext.equalsIgnoreCase(".pp") || ext.equalsIgnoreCase(".scala") || ext.equalsIgnoreCase(".dart") || ext.equalsIgnoreCase(".md") || ext.equalsIgnoreCase(".markdown") ||
				 ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".sh") || ext.equalsIgnoreCase(".conf") || ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || ext.equalsIgnoreCase(".xml") ||
				 ext.equalsIgnoreCase(".ini") || ext.equalsIgnoreCase(".ejs") || ext.equalsIgnoreCase(".makefile") || editing.getRegent().getRegent().getName().equalsIgnoreCase("makefile") ||
				 ext.equalsIgnoreCase(".url") || ext.equalsIgnoreCase(".zig") || ext.equalsIgnoreCase(".bat") || ext.equalsIgnoreCase(".com") || ext.equalsIgnoreCase(".cmd") || ext.equalsIgnoreCase(".ps1") || ext.equalsIgnoreCase(".sh"))) {
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), Character.toString((char) 34)); // colorir strings
		
		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), fs);
			
			/*indxs = findWord(new String(chars), "\"");						// colorir comentários multi-linha - caracteres iguais
			
			if (indxs.size() > 0 && !isMultilineString) { // provavelmente esse é o abrimento
				fs = color(indxs.get(0), indxs.size() > 1 ? indxs.get(1) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineString = true;
				
				isAnotherIteration = false;
			}
			
			if (indxs.size() > 0 && isMultilineString && isAnotherIteration) { // provavelmente esse é o fechamento
				fs = color(0, indxs.get(0) + 2, new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineString = false;
			}
			
			isAnotherIteration = true;
			
			if (isMultilineString)
				fs = color(0, fs.size(), new IDEFont(Fonts.stringsNormal, FONT_SIZE), fs);*/

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), Character.toString((char) 39)); // colorir chars

		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), fs);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		for (IDEFont i : fs) {
			i.setSize(FONT_SIZE);
		}
		
		// extras que precisam ser coloridos depois disso
		
		if (ext.equalsIgnoreCase(".json") || ext.equalsIgnoreCase(".jsonc")) {
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
		}
		}
		return fs;
	}
	
	public List<IDEFont> colorNoExtensions(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		if (!foundExt) {//(!foundExt && editing != null) || (extType.equalsIgnoreCase("") || extType == null)) { // TODO o culpado do gitignore estar assim é esse ARRUMAR DEPOIS 
			for (FileType f : ListableFile.types) {
				if (f.getExtension().equalsIgnoreCase(editing.getRegent().getRegent().getName())) { // tenta ver se tem algum especial
					String st = capitalizeFirstLetter(f.getExtension());
					
					switch (st.toLowerCase()) {
					case "dockerfile":
						extType = "Dockerfile";	// talvez alterar depois para Docker File
						foundExt = true;
						
						for (String s : dkKeys) { // colorir keywords
							indxs = findWord(new String(chars), s);
							
							for (Integer i : indxs)
								fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
						}
						
						break;
						
					case "makefile":
						extType = "Makefile";
						foundExt = true;
						
						for (String s : makeKeys) { // colorir keywords
							indxs = findWord(new String(chars), s);
							
							for (Integer i : indxs)
								fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
						}
						
						indxs = findWord(new String(chars), ":");
						
						for (Integer i : indxs) {
							int c = i;
							int len = 0;
							
							while (c < chars.length && 
									c + len < chars.length &&
									c > 0 &&
									chars[c] != ' ' &&
									chars[c] != '[' &&
									chars[c] != ']' &&
									chars[c] != ',' &&
									chars[c] != ';' &&
									chars[c] != '.') {
								c--;
								len++;
							}
							
							fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
						}
						
						break;
						
					case "license":
						if (!foundExt) {
							extType = Main.lang == Language.PORT ? "Arquivo de Licença" : "License File";
							foundExt = true;
						}
						break;
						
					case "authors":
						if (!foundExt) {
							extType = Main.lang == Language.PORT ? "Nomes dos Autores" : "Authors' Names";
							foundExt = true;
						}
						break;
						
					case "gitignore":
						if (!foundExt) {
							extType = "Git Ignore";
							foundExt = true;
						}
						break;
					}
					
					// Comentários de uma linha
					
					switch (st.toLowerCase()) {
					case "dockerfile":
					case "makefile":
						indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
						
						if (indxs.size() != 0)
							fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
						break;
					}
					
					// Comentários Multi-linha
					
					switch (st.toLowerCase()) {
					case "makefile":
						indxs = findWord(new String(chars), "/*");						// colorir comentários multi-linha - caracteres diferentes
						List<Integer> finals = findWord(new String(chars), "*/");
						
						if (indxs.size() > 0) {
							fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
							isMultilineCommenting = true;
						}
						
						if (finals.size() > 0) {
							fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
							isMultilineCommenting = false;
						}
						
						if (isMultilineCommenting)
							fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
						break;
					}
				}
			}
			
			if (extType.equalsIgnoreCase("") || extType == null) {
				String extn = "";
				
				try {
					extn = ListableFile.getFileExtension(editing.getRegent().getRegent()).substring(1); // tenta retornar o nome da extensão
				} catch (Exception e) {
					extn = "Sem Extensão"; // se não der mesmo assim, coloque "Sem Extensão".
				}
				
				extType = extn;
				foundExt = true;
			}
		}
		
		return fs;
	}
	
	public List<IDEFont> colorComments(String ext, char[] chars, List<IDEFont> fs) {
		List<Integer> indxs = new ArrayList<>();
		
		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cs":
		case ".js":
		case ".mjs":
		case ".vue":
		case ".jsx":
		case ".h":
		case ".hpp":
		case ".hxx":
		case ".swift":
		case ".zig":
		case ".rs":
		case ".kt":
		case ".ino":
		case ".ts":
		case ".go":
		case ".fs":
		case ".m":
		case ".pp":
		case ".pas":
		case ".scala":
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
			if (fs.size() == 0) break;
				
			if (indxs.size() != 0)
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			break;
			
		case ".html":
		case ".htm":
		case ".ejs":
			if (!isJSPart) break;
			
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
			if (fs.size() == 0) break;
				
			if (indxs.size() != 0)
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".ps1":
		case ".com":
		case ".bat":
		case ".cmd":
			indxs = findWord(new String(chars), "REM"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".s":
		case ".asm":
			indxs = findWord(new String(chars), ";"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".lua":
		case ".sql":
		case ".has":
		case ".hs":
			indxs = findWord(new String(chars), "--"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".py":
		case ".pyd":
		case ".rb":
		case ".r":
		case ".jl":
		case ".pl":
		case ".coffee":
		case ".make":
		case ".sh":
		case ".gitignore":
		case ".dockerfile":
		case ".config":
		case ".cfg":
		case ".ini":
			indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".php":
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
			if (fs.size() == 0) break;
				
			if (indxs.size() != 0)
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			break;
		
		case ".markdown":
		case ".md":
			indxs = findWord(new String(chars), "[//]: #"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "[]: #"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
		}
		
		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cxx":
		case ".cs":
		case ".js":
		case ".mjs":
		case ".h":
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
		case ".go":
		case ".m":
		case ".ld":
		case ".scala":
		case ".scss":
		case ".css":
		case ".json":
		case ".jsonc":
			indxs = findWord(new String(chars), "/*");						// colorir comentários multi-linha - caracteres diferentes
			List<Integer> finals = findWord(new String(chars), "*/");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "*/");
			
			for (Integer i : indxs) {
				if (i + "*/".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "*/".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "*/".length()] == '_'))) continue;
				
				fs = color(i, i + "*/".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".lua": // Lua
			indxs = findWord(new String(chars), "--[[");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "--]]");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "--]]");
			
			for (Integer i : indxs) {
				if (i + "--]]".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "--]]".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "--]]".length()] == '_'))) continue;
				
				fs = color(i, i + "--]]".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".rb": // Ruby
			indxs = findWord(new String(chars), "=begin");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=end");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "=end");
			
			for (Integer i : indxs) {
				if (i + "=end".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "=end".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "=end".length()] == '_'))) continue;
				
				fs = color(i, i + "=end".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".jl": // Julia
			indxs = findWord(new String(chars), "#=");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=#");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "=#");
			
			for (Integer i : indxs) {
				if (i + "=#".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "=#".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "=#".length()] == '_'))) continue;
				
				fs = color(i, i + "=#".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".has": // Haskell
		case ".hs":
			indxs = findWord(new String(chars), "{-");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-}");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "-}");
			
			for (Integer i : indxs) {
				if (i + "-}".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "-}".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "-}".length()] == '_'))) continue;
				
				fs = color(i, i + "-}".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".fs": // F#
		case ".pas":
		case ".pp":
			indxs = findWord(new String(chars), "(*");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "*)");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			
			indxs = findWord(new String(chars), "*)");
			
			for (Integer i : indxs) {
				if (i + "*)".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "*)".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "*)".length()] == '_'))) continue;
				
				fs = color(i, i + "*)".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".py":
		case ".pyd":
			indxs = findWord(new String(chars), "\'\'\'");						// colorir comentários multi-linha - caracteres iguais
			
			if (indxs.size() > 0 && !isMultilineCommenting) { // provavelmente esse é o abrimento
				fs = color(indxs.get(0), indxs.size() > 1 ? indxs.get(1) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
				
				isAnotherIteration = false;
			}
			
			if (indxs.size() > 0 && isMultilineCommenting && isAnotherIteration) { // provavelmente esse é o fechamento
				fs = color(0, indxs.get(0) + 2, new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			isAnotherIteration = true;
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".ejs":
		case ".xml":
		case ".htm":
		case ".html":
		case ".svg":
			indxs = findWord(new String(chars), "<!--");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-->");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			if (isCssPart || isJSPart || isPhpPart) {
				indxs = findWord(new String(chars), "/*");						// colorir comentários multi-linha - caracteres diferentes
				finals = findWord(new String(chars), "*/");
				
				if (indxs.size() > 0) {
					fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
					isMultilineCommenting = true;
				}
				
				if (finals.size() > 0) {
					fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
					isMultilineCommenting = false;
				}
				
				if (isMultilineCommenting)
					fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				
				indxs = findWord(new String(chars), "*/");
				
				for (Integer i : indxs) {
					if (i + "*/".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "*/".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "*/".length()] == '_'))) continue;
					
					fs = color(i, i + "*/".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
				}
			}
			
			indxs = findWord(new String(chars), "-->");
			
			for (Integer i : indxs) {
				if (i + "-->".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "-->".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "-->".length()] == '_'))) continue;
				
				fs = color(i, i + "-->".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".md":
		case ".markdown":
			indxs = findWord(new String(chars), "<!--");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-->");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "-->");
			
			for (Integer i : indxs) {
				if (i + "-->".length() < chars.length && i - 1 > 0 && (Character.isLetter(chars[i + "-->".length()]) || Character.isLetter(chars[i - 1]) || (chars[i - 1] == '_' || chars[i + "-->".length()] == '_'))) continue;
				
				fs = color(i, i + "-->".length(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
		}
		
		return fs;
	}
	
	public List<IDEFont> automaticColor(char[] chars, String ext) {
		extType = "";
		foundExt = false;
		
		/*isMultilineCommenting = false;
		
		isCssPart = false;
		isJSPart = false;
		isPhpPart = false;*/
		
		List<IDEFont> fs = new ArrayList<>(); // eliminar a necessidade de ter que apertar alguma tecla pra algumas coisas funcionarem
		
		for (int i = 0; i < chars.length; i++)
			fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		
		/////////////////////////////////////////////////////
		
		fs = colorVariablesAndObjects(ext, chars, fs);
		fs = colorMethods(ext, chars, fs);
		fs = colorKeywords(ext, chars, fs);
		fs = colorNumbers(ext, chars, fs);
		fs = colorSymbols(ext, chars, fs);
		fs = colorExtras(ext, chars, fs);
		fs = colorNoExtensions(ext, chars, fs);
		fs = colorComments(ext, chars, fs);
		
		/////////////////////////////////////////////////////
		
		if (isReadOnly && !extType.contains("(" + Texts.readOnly + ")")) extType += " (" + Texts.readOnly + ")";
		
		return fs;
	}
	
	public static String capitalizeFirstLetter(String s) {
		char f = Character.toUpperCase(s.charAt(0));
		String c = f + s.substring(1);
		
		return c;
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
	 * Esse método faz a função que muitos editores de código fazem: adicionar caracteres inteligentes. Se você digita '{', ele
	 * completa com '}', o mesmo vale para '[', '<', '"' (aspas duplas) e ' ' ' (aspas simples).
	 * 
	 * @param pre - O {@code StringBuilder} anterior, a base.
	 * @return O {@code StringBuilder} anterior com as modificações.
	 */
	private StringBuilder addCodeHelps(StringBuilder pre) {
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
	
	public void setCursorWithinBounds() { // o cursorY deve ser feito primeiro
		if (editing == null) return;
		
		try {
			if (cursorY < 1) cursorY = 1;
			if (cursorY + 1 > lines.size()) cursorY = lines.size();
			
			if (cursorX < 0) cursorX = 0;
			if (cursorX > lines.get(cursorY - 1).getChars().size()) cursorX = lines.get(cursorY - 1).getChars().size();
		} catch (Exception e) {}
	}
	
	private int setWithinBounds(int x, int y, boolean isX) {
		try {
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
		} catch (Exception e) {
			return x;
		}
	}
	
	private StringBuilder write(StringBuilder cY, char c) {
		if (c < 32 || c > 1000) {
			cursorX--; // esse é o método gambiarrento, mas depois pode arrumar (ou não kkkkk)
			
			return cY;
		}
		
		if (cY.length() == 0) cY.append(c);
		else if (cursorX <= cY.length()) cY.insert(cursorX, c); // use <= pq se digitar no último n digita pq n bate
															   // com a condição mas mesmo assim aumenta o cursorX e quando dá
															  // o backspace excede o tamanho da linha e dá no que dá né
		return cY;
	}
	
	public void register(StringBuilder cY, int y) { // cY = cursorY
		String gs = cY.toString(); // gen string
		char[] ca = gs.toCharArray(); // char array
		
		List<Character> lc = toCharList(ca); // list char	(Esses comentários são para especificar os nomes das variáveis)
		
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
		}
		
		if (pressedAccent && !(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL)) {
			pressedAccent = false;
			
			switch (prAcc) {
			case ACUTE:
				 if (ch == 'A' || (capsLock && ch == 'a')) return 'Á';
				 if (ch == 'E' || (capsLock && ch == 'e')) return 'É';
				 if (ch == 'I' || (capsLock && ch == 'i')) return 'Í';
				 if (ch == 'O' || (capsLock && ch == 'o')) return 'Ó';
				 if (ch == 'U' || (capsLock && ch == 'u')) return 'Ú';
				 if (ch == 'Y' || (capsLock && ch == 'y')) return 'Ý';
				
				 if (ch == 'a') return 'á';
				 if (ch == 'e') return 'é';
				 if (ch == 'i') return 'í';
				 if (ch == 'o') return 'ó';
				 if (ch == 'u') return 'ú';
				 if (ch == 'y') return 'ý';
				 
				 if (keyCode == KeyEvent.VK_DEAD_ACUTE) return '´';
				break;
			case BACK_QUOTE:
				 if (ch == 'A' || (capsLock && ch == 'a')) return 'À';
				 if (ch == 'E' || (capsLock && ch == 'e')) return 'È';
				 if (ch == 'I' || (capsLock && ch == 'i')) return 'Ì';
				 if (ch == 'O' || (capsLock && ch == 'o')) return 'Ò';
				 if (ch == 'U' || (capsLock && ch == 'u')) return 'Ù';
				
				 if (ch == 'a') return 'à';
				 if (ch == 'e') return 'è';
				 if (ch == 'i') return 'ì';
				 if (ch == 'o') return 'ò';
				 if (ch == 'u') return 'ù';
				 
				 if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown()) return '`';
				break;
			case CIRCUMFLEX:
				 if (ch == 'A' || (capsLock && ch == 'a')) return 'Â';
				 if (ch == 'E' || (capsLock && ch == 'e')) return 'Ê';
				 if (ch == 'I' || (capsLock && ch == 'i')) return 'Î';
				 if (ch == 'O' || (capsLock && ch == 'o')) return 'Ô';
				 if (ch == 'U' || (capsLock && ch == 'u')) return 'Û';
				
				 if (ch == 'a') return 'â';
				 if (ch == 'e') return 'ê';
				 if (ch == 'i') return 'î';
				 if (ch == 'o') return 'ô';
				 if (ch == 'u') return 'û';
				 
				 if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown()) return '^';
				break;
			case TILDE:
				if (ch == 'A' || (capsLock && ch == 'a')) return 'Ã';
				if (ch == 'O' || (capsLock && ch == 'o')) return 'Õ';
				if (ch == 'N' || (capsLock && ch == 'n')) return 'Ñ';
				
				if (ch == 'a') return 'ã';
				if (ch == 'o') return 'õ';
				if (ch == 'n') return 'ñ';
				
				if (keyCode == KeyEvent.VK_DEAD_TILDE) return '~';
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
	
	public void paste() {		// terminar o paste com mais de uma linha
		if (editing == null) return;
		
		CommandTerminal.runCommand("del");
		
		String[] sp = clipboard.split("\n");
		
		int index = 0;
		
		if (sp.length == 1) { // se é só uma linha
			for (String s : sp) {
				StringBuilder b = new StringBuilder(new String(toCharArray(lines.get((cursorY - 1)).getChars())));
				
				b.insert(cursorX, s);
				
				register(b, (cursorY - 1) + index);
				
				cursorX += s.length();
			}
		}
		else {
			for (String s : sp) {
				if (s != sp[0])
					lines.add((cursorY - 1) + index, new IDELine(new ArrayList<>(), new ArrayList<>()));
				
				StringBuilder b = new StringBuilder(new String(toCharArray(lines.get((cursorY - 1) + index).getChars())));
				
				int x = cursorX > lines.get((cursorY - 1) + index).getChars().size() ? lines.get((cursorY - 1) + index).getChars().size() : cursorX; // não pode exceder o index
				
				b.insert(x, s);
				
				register(b, (cursorY - 1) + index);
				
				if (s == sp[sp.length - 1]) {
					cursorX += s.length();
					cursorY += sp.length - 1;
				}
				
				index++;
			}
		}
		
		editing.setSaved(false);
	}
	
	public void addNewLine(int yPos) {
		List<Character> chars = new ArrayList<>();
		List<IDEFont> fs = new ArrayList<>();
		
		chars.add('\0');
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
		CommandTerminal term = new CommandTerminal(Screen.WIDTH / 2 - 250, 25, 500, 30);
		
		if (CommandTerminal.active)
			return;
		
		CommandTerminal.active = true;
		IDEComponent.toAdd.add(term);
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
					pb = new ProcessBuilder("sh", "-c", "start");
				
				File dir = Explorer.scope != null ? Explorer.scope.getRegent() : Main.baseFolder; // eu tava fazendo o equivalente a isso: null.regent != null
				
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
			new Thread() {
				public void run() {
					try {
						Main.desktop.open(editing.getRegent().getRegent());
					} catch (Exception e) {
						setSystemLook();
						
						JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound, JOptionPane.OK_OPTION);
					}
				}
			}.start();
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
				if (Main.baseFolder == null) return;
				
				String path = null;
				
				try {
					path = editing == null ? Explorer.files.get(0).getRegent().getPath() : editing.getRegent().getRegent().getPath();
				} catch (Exception e) { // caiu aqui mt provavelmente é pq não tem itens no explorer
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
			
			SetFileName set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, true);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(set);
			break;
			
		case "newfolder":
			y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;
			
			set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, false);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(set);
			break;
			
		case "searchrep":
			if (editing == null) return; // Vai modificar o que não existe?
			
			RightClickOption.removeAllRightClickOptions(); // arrumar o negócio
			
			if (!alreadyAddedFrame) {
				searchWindow = new SearchReplaceWindow();
				alreadyAddedFrame = true;
			}
			else {
				searchWindow.setState(Frame.NORMAL);
				searchWindow.requestFocus();
				
				searchWindow.txbSearch.requestFocus();
			}
			
			break;
		}
	}
	
	public void verifyDuplicateTabs() { // continuar segundo o TODO
		try {
			if (tabs == null || tabs.size() == 0) return;
			
			for (int i = 0; i < tabs.size(); i++)
				for (int j = 0; j < tabs.size(); j++) {
					Tab tabi = tabs.get(i);
					Tab tabj = tabs.get(j);
					
					if (tabi.getRegent().getRegent().getAbsolutePath().equals(tabj.getRegent().getRegent().getAbsolutePath()) && tabi != tabj) {
						tabi.close();
						
						return;
					}
				}
		} catch (Exception e) {}
	}
	
	public static int ruleOf3(int a, int b, int c) {
		return (b * c) / a;
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
	
	/*public static int ptToPx(int pt) {
		return ruleOf3(12, 16, pt);
	}*/
	
	/**
	 * <!-- Slk mermão vc pode estilizar com css -->
	 * <style>
	 * 	pre {
	 *  	font-family: "Calibri";
	 *  	font-size: 15px;
	 *  }
	 * </style>
	 * 
	 * Gera um Lorem Ipsum aleatório a partir das palavras do array, com pontuação e tudo.
	 * <pre>Ele suporta somente um parágrafo.</pre>
	 * 
	 * @param numWords - O número de palavras no total que o texto vai ter.
	 * @return O texto gerado.
	 */
	public static String generateLoremIpsum(int numWords) {
		String[] points = { ". ", ", ", ", " }; // tem mais chances de ser , do que . (a cada 2 , ocorre 1 .)
		
		Random rd = new Random();
		
		boolean capitalize = false;
		String initialText = "Lorem ipsum dolor sit amet ";
			
		StringBuilder bl = new StringBuilder(initialText);
			
		for (int i = 0; i < numWords; i++) {
			String word = capitalize ? capitalizeFirstLetter(loremWords[rd.nextInt(loremWords.length)]) : loremWords[rd.nextInt(loremWords.length)];
				
			bl.append(word + (i == numWords - 1 ? "." : ""));
				
			capitalize = false;
				
			if (rd.nextInt(100) < 25 && i < numWords - 1) { // 25% de pontuar
				// pontuar!
				String point = points[rd.nextInt(points.length)];
					
				bl.append(point);
					
				if (point.contains(".")) capitalize = true;
			}
			else
				bl.append(" ");
		}
			
		return bl.toString();
	}
	
	/*public static String mergeStringArrays(String[]... arrays) {
		StringBuilder bl = new StringBuilder();
		
		for (int i = 0; i < arrays.length; i++) {
			for (int j = 0; j < arrays[i].length; j++) {
				bl.append(arrays[i][j] + " ");
			}
		}
		
		return bl.toString();
	}*/
	
	public static List<Character> toListChar(char[] ch) {
		List<Character> list = new ArrayList<>();
		
		for (char c : ch)
			list.add(c);
		
		return list;
	}
	
	public synchronized void callAutomaticColor() {
		new Thread() {
			public void run() {
				try {
					if (editing == null) return;
					
					for (IDELine l : lines) {
						l.setFonts(
								automaticColor(
										toCharArray(
												l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
					
					}
				} catch (ConcurrentModificationException e) {}
			}
		}.start();
	}
	
	/* Pseudo-Código -- FUNCIONOU!
	 * 
	 * deletar os chars atrás do cursor, até o tamanho da palavra digitada
	 * ex: cx = 7, pld = 3 | cx = 7 - 3. // cx = cursorx, pld = palavra digitada
	 * 
	 * depois dá um insert na string e
	 * 
	 */
	public void makeChanges(String e) { // [e] é a palavra que vai colocar
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
	 * Hardcoded no cursor
	 */
	public void addAutoCompleteOptions() {
		if (autocomplete.isEmpty()) return;
		
		RightClickOption.removeAllRightClickOptions();
		
		int index = 0;
		
		for (String s : autocomplete) {
			toAddAutoCompletes.add(new RightClickOption(drawcx, (drawcy + FONT_SIZE) + index * 30, 330, 32, 16, s, keywords, (e) -> makeChanges(e), s));
			
			index++;
		}
		
		for (AutoComplete a : autocompleteadds) {
			if (a == null) continue;
			
			toAddAutoCompletes.add(new RightClickOption(drawcx, (drawcy + FONT_SIZE) + index * 30, 330, 32, 16, a.text, getAutoCompleteIcon(a.type), (e) -> makeChanges(e), a.text));
			
			index++;
		}
		
		autocomplete.clear();
		autocompleteadds.clear();
	}
	
	public void tick() {
		if (SetFileName.added || CommandTerminal.active || RenameFile.added) return; // 06/08/2021 - 11:43
		
		if (tabs == null) tabs = new ArrayList<>(); // fazer isso com os autocompletes, se necessário
		
		verifyDuplicateTabs();
		
		if (!cursorThread.isAlive() || cursorThread.getState() == State.TERMINATED) {
			cursorThread = new Thread() {
				public void run() {
					cursor.play();
				}
			};
			
			cursorThread.start();
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
		
		if (lines.size() > 0 && scrY + (FONT_SIZE + (FONT_SIZE / 4)) * 3 > (lines.size() + 2) * (FONT_SIZE + (FONT_SIZE / 4))) {
			scrY = (lines.size() * FONT_SIZE) - (FONT_SIZE * 3);
			
			cursorX = lines.get(lines.size() - 1).getChars().size();
			cursorY = lines.size();
			
			setCursorWithinBounds();
		}
		
		realcx = ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX;
		realcy = MIN_Y + cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2;
		
		//int speed = 10;
		
		/*if (drawcx < realcx) drawcx += speed; // talvez quando for adicionar animação tá aqui pronto
		if (drawcx > realcx) drawcx -= speed;
		
		if (drawcy < realcy) drawcy += speed;
		if (drawcy > realcy) drawcy -= speed;*/
		
		if (MouseInput.isLeftPressed() || (KeyInput.isKeyPressed() && KeyInput.getKeyCodePressed() != KeyEvent.VK_BACK_SPACE) && ((cursorX != index1 && cursorY != line1) && (cursorX != index2 && cursorY != line2)))
			selecting = false;
		
		drawcx = realcx;
		drawcy = realcy;
		
		if (FONT_SIZE < 1)
			FONT_SIZE = 16;
		
		if (MouseInput.leftDragged() && !isReadOnly && !alternateTabsMode && hovered() && !MouseInput.hovered(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22)) {
			selecting = true;
			
			index1 = cursorX;
			line1 = cursorY;
			
			index2 = mx;
			line2 = my;
			
			if (index1 == index2 && line1 == line2)
				selecting = false;
			
			if (line2 < line1) {
				int tempindex1 = index1;
				int templine1 = line1;
				
				index1 = index2;
				index2 = tempindex1;
				
				line1 = line2;
				line2 = templine1;
				
			}
			else if (line2 == line1) {
				if (index2 < index1) {
					int tempindex1 = index1;
					
					index1 = index2;
					index2 = tempindex1;
				}
			}
		}
		
		if (editing != null)
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " • " + editing.getRegent().getRegent().getName() + " - Boot IDE");
		else if (Main.baseFolder != null)
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
		
		try {
			clipboard = (String) Main.toolkit.getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException | IllegalStateException e) {
			// Não é string. Resetando!
			
			clipboard = "";
		}
		
		if (MouseInput.hovered(x, 0, Main.screen.getWidth(), Tab.HEIGHT) && tabs != null && tabs.size() > 0) {
			if (MouseInput.isMouseRolling()) {
				if (MouseInput.wheelUp() && tabScr < 0)
					tabScr += 203;						// 3 é a compensação para as tab n se distanciar
				else if (MouseInput.wheelDown() && (tabs.get(tabs.size() - 1).getX() + tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280)) { // 280
					tabScr -= 203;
				}
				
				for (IDEComponent i : components) {
					if (i instanceof RightClickOption)
						IDEComponent.toRemove.add(i);
				}
			}
		}
		
		if (hovered() && editing != null) {
			if (!isReadOnly && !alternateTabsMode && !RightClickOption.isRightClickActive() && !RightClickOption.isAutoCompleteActive()) {
				Main.screen.setCursor(new Cursor(Cursor.TEXT_CURSOR));	// se for pra descomentar o de baixo, mover a ultima condição (a depois do &&) desse if pra dentro do if, assim o else não verifica essa condição
			}
			else {
				Main.screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
			if (MouseInput.hovered(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22)) {
				Main.screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
			/*if (RightClickOption.isRightClickActive() && RightClickOption.anyRightClickOptionHovered()) {
				Main.screen.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}*/
			
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
							scrY -= (FONT_SIZE + (FONT_SIZE / 4)) * 3;
						else if (MouseInput.wheelDown() && scrY + (FONT_SIZE + (FONT_SIZE / 4)) * 3 < lines.size() * (FONT_SIZE + (FONT_SIZE / 4)))
							scrY += (FONT_SIZE + (FONT_SIZE / 4)) * 3;
					}
					
					return;
					}
				}.start();
			}
			
			if (leftClicked() && !RightClickOption.isRightClickActive() && !RightClickOption.isAutoCompleteActive() && !isReadOnly && !alternateTabsMode && !MouseInput.hovered(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22)) {
				cursorX = mx;
				cursorY = my;
				
				setCursorWithinBounds();
			}
		}
		else
			Main.screen.setCursor(Cursor.getDefaultCursor());
		
		if (rightClicked() && !alternateTabsMode) {
			int width = Main.lang == Language.PORT ? 550 : 510;
			
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), width, Texts.openCmd, (s) -> execute(s), "cmd");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, width, Texts.openTerminal, (s) -> execute(s), "term");
			
			if (Main.baseFolder != null) {
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, width, Texts.openExplorer, (s) -> execute(s), "sysexp");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (isReadOnly ? 90 : (editing != null ? (selecting ? 360 : 240) : 90)), width, Texts.setBaseFolder, (s) -> execute(s), "setbase");
			}
			
			if (!isReadOnly) {
					if (editing != null) {
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 240 : 150), width, Texts.open + " " + Texts.searchReplace, (s) -> execute(s), "searchrep");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 270 : 180), width, Texts.selectLine, (s) -> CommandTerminal.runCommand(s), "selectline");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, width, Texts.save, (s) -> execute(s), "save");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 150 : 120), width, Texts.paste, (s) -> execute(s), "paste");
					
					if (selecting) {
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, width, Texts.copy, (s) -> CommandTerminal.runCommand(s), "copy");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 180, width, Texts.cut, (s) -> CommandTerminal.runCommand(s), "cut");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 210, width, Texts.delete, (s) -> CommandTerminal.runCommand(s), "del");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 330, width, Texts.deselect, (s) -> CommandTerminal.runCommand(s), "deselect");
					}
					
					if (Main.baseFolder != null && editing != null) {
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 300 : 210), width, Texts.selectAll, (s) -> CommandTerminal.runCommand(s), "selectall");
						IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 390 : 270), width, Texts.openDefault, (s) -> execute(s), "opendef");
					}
				}
			}
		}
		
		if (KeyInput.isKeyPressed() && !SetFileName.added && !CommandTerminal.active) { // TODO
			setCursorWithinBounds();
			
			new Thread() {
				public void run() {
					try {
						if (editing == null) return;
						
						for (IDELine l : lines) {
							l.setFonts(
									automaticColor(
											toCharArray(
													l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
						
						}
					} catch (ConcurrentModificationException e) {}
				}
			}.start();
			
			// Detectar atalhos
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE && !isReadOnly && !alternateTabsMode) {
				KeyInput.updateKeys();
				RightClickOption.removeAllRightClickOptions();
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE && alternateTabsMode) {
				alternateTabsMode = false;
				exchanging = null;
			}
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME && !isReadOnly && !alternateTabsMode) { // Ctrl + Home - Começo do Documento
				KeyInput.updateKeys();
				
				scrX = 0;
				scrY = 0;
				
				cursorX = 0;
				cursorY = 0;
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_F && !isReadOnly && !alternateTabsMode) { // Ctrl + F - Abrir janela Localizar/Substituir
				KeyInput.updateKeys();
				
				execute("searchrep");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_H) { // Ctrl + Shift + H - Toggle Read Only
				KeyInput.updateKeys();
				
				editing.save();
				
				CommandTerminal.runCommand("togglereadonly");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_END && !isReadOnly && !alternateTabsMode) { // Ctrl + End - Fim do Documento
				KeyInput.updateKeys();
				
				//scrX = (lines.get(lines.size() - 1).getChars().size() * FONT_SIZE) - FONT_SIZE * 10; // esse - FONT_SIZE * 5 é pra dar um offset para trás e ficar no meio da tela.
				scrY = (lines.size() * FONT_SIZE) - (FONT_SIZE * 3);
				
				cursorX = lines.get(lines.size() - 1).getChars().size();
				cursorY = lines.size();
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME && !isReadOnly && !alternateTabsMode) { // Home - Começo da Linha
				KeyInput.updateKeys();
				
				scrX = 0;
				cursorX = 0;
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END && !isReadOnly && !alternateTabsMode) { // End - Fim da Linha
				KeyInput.updateKeys();
				
				//scrX = (lines.get(cursorY - 1).getChars().size() * FONT_SIZE) - FONT_SIZE * 10;
				cursorX = lines.get(cursorY - 1).getChars().size();
				
				setCursorWithinBounds();
					
				return;
			}
			
			if ((KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_D || KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) && !isReadOnly && !alternateTabsMode) { // Ctrl + D (Desselecionar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("deselect");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_M && !isReadOnly && !alternateTabsMode) { // Ctrl + M (Go To Cursor)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("gotocursor");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X && !isReadOnly && !alternateTabsMode) { // Ctrl + X (Cortar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("cut");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_K) { // Ctrl + K (Alternar Explorador)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("toggleexplorer");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.isAltDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + Alt + T (Fechar Todas as Abas)
				KeyInput.updateKeys();
				
				tabs.clear();
				editing = null;
				
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + T (Fechar Aba)
				KeyInput.updateKeys();
				
				editing.close();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + T (Terminal)
				KeyInput.updateKeys();
					
				execute("term");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_B || KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_WINDOWS) { // Ctrl + B OU Ctrl + Win (Cmd)
				KeyInput.updateKeys();
					
				execute("cmd");
					
				return;
			}
			
			if (editing == null) return;
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_S && !isReadOnly && !alternateTabsMode) { // Ctrl + S (Salvar)
				KeyInput.updateKeys();
					
				editing.save();
					
				return;
			}
			
			if (KeyInput.isControlDown() &&  KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A && !isReadOnly && !alternateTabsMode) { // Ctrl + Shift + A (Selecionar Tudo)
				KeyInput.updateKeys();
					
				cursorX = 0;
				
				CommandTerminal.runCommand("selectline");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A && !isReadOnly && !alternateTabsMode) { // Ctrl + A (Selecionar Linha)
				KeyInput.updateKeys();
				
				cursorX = 0;
				cursorY = 1;
				
				CommandTerminal.runCommand("selectall");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C && !isReadOnly && !alternateTabsMode) { // Ctrl + C (Copiar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("copy");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V && !isReadOnly && !alternateTabsMode) { // Ctrl + V (Colar)
				KeyInput.updateKeys();
					
				paste();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE && !isReadOnly && !alternateTabsMode) { // Ctrl + Delete ou Backspace (Apenas Selecionando) (Deletar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("del");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_P && !isReadOnly && !alternateTabsMode) { // Ctrl + P (Toggle Code Helpers)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("togglecodehelpers");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE && !isReadOnly && !alternateTabsMode) { // Ctrl + Space (Trigger Auto Complete)
				String[] autoc = ListableFile.fileHasExtension(editing.getRegent().getRegent()) ? getKeywords(ListableFile.getFileExtension(editing.getRegent().getRegent())) : getKeywordsSpecial(editing.getRegent().getRegent().getName());
				
				autocomplete.clear();
				
				for (String s : autoc)
					if (s.contains(wordSinceSpace))
						autocomplete.add(s);
				
				autocomplete = removeDuplicates(autocomplete);
				autocompleteadds = removeDuplicates(autocompleteadds);
				
				autocompleteindex = 0;
				
				addAutoCompleteOptions();
			}
			
			/*if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Z) { // Ctrl + Z (Desfazer)
				KeyInput.updateKeys();
				
				if (undo.isEmpty()) return;
				
				List<IDELine> peek = undo.peek();
				
				lines = peek;
				redo.push(peek);
				
				if (!undo.isEmpty())
					undo.pop();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Y) { // Ctrl + Y (Refazer)
				KeyInput.updateKeys();
				
				if (redo.isEmpty()) return;
				
				List<IDELine> peek = redo.peek();
				
				lines = peek;
				undo.push(peek);
					
				if (!redo.isEmpty())
					redo.pop();
				
				return;
			}*/
			
			if (!(KeyInput.isAltDown() || KeyInput.isControlDown()) && !isReadOnly && !alternateTabsMode) { // se ctrl, alt NÃO estão pressionados
				if (!KeyInput.isShiftDown()) {
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
						KeyInput.updateKeys();
						
						cursorY--;
						
						setCursorWithinBounds(); // detectar teclas que usam o alt gr TODO
						
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
				}
				else {
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) { // aperfeiçoar
						KeyInput.updateKeys();
						
						line1--;
						
						selecting = true;
						
						line1 = setWithinBounds(index1, line1, false);
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
						KeyInput.updateKeys();
						
						line2++;
						
						selecting = true;
						
						line2 = setWithinBounds(index2, line2, false);
						
						return;
					}
					
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
						KeyInput.updateKeys();
						
						index1--;
						
						selecting = true;
						
						index1 = setWithinBounds(index1, line1, true);
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
						KeyInput.updateKeys();
						
						index2++;
						
						selecting = true;
						
						index2 = setWithinBounds(index2, line2, true);
						
						return;
					}
				}
			
			KeyInput.updateKeys();
			
			StringBuilder cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
				KeyInput.updateKeys();
				//undo.push(lines);
				
				RightClickOption.removeAllRightClickOptions();
				
				if (wordSinceSpace.length() > 0)
					wordSinceSpace = wordSinceSpace.substring(0, wordSinceSpace.length() - 1);
				
				if (selecting) {
					CommandTerminal.runCommand("del");
					
					return;
				}
				else {
					if (cursorX > 0) {
						cY.deleteCharAt(cursorX - 1);
					
						cursorX--;
					
						setCursorWithinBounds();
	
						editing.setSaved(false);
					
						register(cY, cursorY - 1);
					}
					else if (cursorY > 1) {
						String s = cY.toString();
						
						cursorX = lines.get(cursorY - 2).getChars().size();
						
						lines.remove(cursorY - 1);
						cursorY--;
						
						cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
						
						cY.append(s);
	
						editing.setSaved(false);
						
						register(cY, cursorY - 1);
					}
					
					return;
				}
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				KeyInput.updateKeys();
				//undo.push(lines);
				
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
				//undo.push(lines);
				
				if (!RightClickOption.isAutoCompleteActive()) {
					wordSinceSpace = "";
					RightClickOption.removeAllRightClickOptions();
				
					cY.insert(cursorX, "    ");
					
					cursorX += 4;
					editing.setSaved(false);
				}
				else {
					autocompleteindex++;
					
					if (autocompleteindex == autocompletes.size()) autocompleteindex = 0;
				}
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				KeyInput.updateKeys();
				//undo.push(lines);
				
				if (RightClickOption.isAutoCompleteActive()) {
					autocompletes.get(autocompleteindex).command.execute(autocompletes.get(autocompleteindex).clickArg);
					
					RightClickOption.removeAllRightClickOptions();
					
					return;
				}
				
				wordSinceSpace = "";
				RightClickOption.removeAllRightClickOptions();
				
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
					fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
				
				lines.add(cursorY, new IDELine(toCharList(s.toCharArray()), fs));
				
				register(cY, cursorY - 1);
				
				editing.setSaved(false);
				
				cursorX = nSpaces;
				cursorY++;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SHIFT) return;
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			//System.out.println(c);
			
			c = addAccents(keyCode, c);
			
			cY = write(cY, c);
			
			if (codeHelpersOn)
				cY = addCodeHelps(cY);
			
			register(cY, cursorY - 1);
			
			if (Character.isLetter(c)) wordSinceSpace += c;
			if (keyCode == KeyEvent.VK_SPACE) {
				wordSinceSpace = "";
				RightClickOption.removeAllRightClickOptions();
			}
			
			cursorX++;
			
			setCursorWithinBounds();
			
			// Add AutoComplete
			
			if (Character.isLetter(c) && !isReadOnly && !alternateTabsMode) { // adicionar esse código no backspace, e se tiver espaços na frente, a keyword vai no lugar errado
				String[] autoc = ListableFile.fileHasExtension(editing.getRegent().getRegent()) ? getKeywords(ListableFile.getFileExtension(editing.getRegent().getRegent())) : getKeywordsSpecial(editing.getRegent().getRegent().getName());
				
				if (autoc != null) {
					autocomplete.clear();
				
					for (String s : autoc)
						if (s.contains(wordSinceSpace))
							autocomplete.add(s);
				
					autocomplete = removeDuplicates(autocomplete);
					autocompleteadds = removeDuplicates(autocompleteadds);
				
					autocompleteindex = 0;
				
					addAutoCompleteOptions();
				}
			}
			
			if (!Character.isLetter(c) && KeyInput.getKeyCodePressed() != KeyEvent.VK_TAB) RightClickOption.removeAllRightClickOptions();
			
			if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
			
			//undo.push(lines);
			editing.setSaved(false);
			
		} // <-
		} // não ligue pra isso :)
		
		robot.keyRelease(KeyEvent.VK_F1);
		
		if (!hasPressed) {
			robot.keyPress(KeyEvent.VK_F1);
			
			hasPressed = true;
		}
		
		if (tabs != null) {
			for (Tab t : tabs) {
				//if (t.getX() + tabScr < x || t.getX() + tabScr > Main.screen.getWidth()) continue; // infelizmente vai ter que fazer o tick mesmo assim, bom que não pesa muito
				
				t.tick();
			}
		}
		
		if (editing == null && tabs.size() == 1) tabs.forEach(e -> e.close());
		
		for (RightClickOption r : autocompletes)
			r.tick();
		
		tabs.addAll(toAdd);
		toAdd.clear();
			
		tabs.removeAll(toRemove);
		toRemove.clear();
			
		lines.removeAll(linesToRemove);
		linesToRemove.clear();
			
		autocompleteadds.addAll(addautocompleteadds);
		addautocompleteadds.clear();
		
		autocompletes.addAll(toAddAutoCompletes);
		toAddAutoCompletes.clear();
		
		autocompletes.removeAll(toRemoveAutoCompletes);
		toRemoveAutoCompletes.clear();
		
		if (index1 < 0) index1 = 0;
		if (line1 < 1) line1 = 1;
		
		if (index2 < 0) index2 = 0;
		if (line2 < 1) line2 = 1;
		
		if (scrX < 0) scrX = 0;
		if (scrY < 0) scrY = 0;
	}
	
	public void render(Graphics g) {
		//if (editing == null) return;
		
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorerLight);
		g2.setStroke(new BasicStroke(8f));
		
		//g2.drawLine(x, y, x, height);
		g2.drawLine(x, 30, width, 30);
		
		g.setColor(Colors.background);
		g.fillRect(x, y, width, height);
		
		if (tabs == null || tabs.size() == 0) return;
		
		if (editing != null) {
			g.setColor(Colors.explorer);
			g.fillRect(x, MIN_Y, Main.screen.getWidth(), height);
		}
		
//		if (editing != null &&																	// não vamos mostrar imagens aqui, vai abrir o aplicativo do sistema
//			(ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".png") || // se for uma imagem
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpg") ||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpeg")||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".gif") ||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".bmp"))) {
//			try {
//				BufferedImage get = ImageIO.read(getClass().getResource(editing.getRegent().getRegent().getAbsolutePath())); // esse get tá null
//				
//				g.drawImage(get, (x + (width / 2)) - get.getWidth(), (y + (height / 2)) - get.getHeight(), get.getWidth() * 2, get.getHeight() * 2, null);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			return; // pra n renderizar texto, aquele monte de coisa estranha
//		}
		
		if (!isReadOnly) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, MIN_Y + ((cursorY - 1) * (FONT_SIZE + (FONT_SIZE / 4))) - scrY - 1, Main.screen.getWidth(), FONT_SIZE + (FONT_SIZE / 4) + 1);
		}
		
		try {
			for (int i = 0; i < lines.size(); i++) {
				if (selecting) {
					g.setColor(Colors.selection);
					
					if (i > line1 && i < line2) { // do meio
						g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								(i + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								Main.screen.getWidth() + scrX,
								FONT_SIZE + 4);
					}
				}
			}
			
			for (int i = 0; i < lines.size(); i++) {
				int yr = MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY;
				
				if (yr < 0 || yr > Screen.HEIGHT) continue;
				
				char[] cs = toCharArray(lines.get(i).getChars());
				IDEFont[] fs = toArray(lines.get(i).getFonts());
				
				if (lines.get(i) == null) break;
				
				if (MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY < MIN_Y - 15) continue;
				
				/*if (i == cursorY - 1 && !isReadOnly) {
					g.setColor(Colors.backgroundLight);
					g.fillRect(x, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY - 1, Main.screen.getWidth(), FONT_SIZE + (FONT_SIZE / 4) + 1);
				}*/
				
				if (selecting) {
					g.setColor(Colors.selection);
					
					if (i == line1 - 1) { // - 1 porque a line1 é base 1
						if (i == line2 - 1) {
							g.fillRect(((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do index1 até o index2
								(((line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY) - (FONT_SIZE > 14 ? 5 : 0)),
								(((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) - (((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
						else {
							g.fillRect(((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o fim da linha
								((line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY) - (FONT_SIZE > 14 ? 5 : 0),
								Main.screen.getWidth() + scrX,
								FONT_SIZE + 4);
						}
					}
					if (i == line2 - 1) {
						if (i != line1 - 1) { // do 0 ao index2
							g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								((line2 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 15 ? 5 : 0)) - (FONT_SIZE == 15 ? 4 : 0),
								((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX - (((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
					}
				}
				
				IDEFont font = i == cursorY - 1 ? new IDEFont(Fonts.selectedLineNumberNormal, FONT_SIZE) : new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
				
				if (isReadOnly) font = new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
				
				Fonts.drawChars(cs, (x + 50) - scrX, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY, fs, x + (FONT_SIZE * 3), g);
				
				String nums = String.valueOf(i + 1); // nums = num string
				//int num = i + 1;
				
				int nx = x + 1;
				
				/*if (num < 10) nx = x + 1 + (2 * FONT_SIZE) + 3;
				if (num >= 10 && num < 100) nx = x + 1 + FONT_SIZE + 3 + 3; // não será feito, pelo menos por enquanto
				if (num >= 100 && num < 1000) nx = x + 1 + 6;*/
				
				Fonts.drawString(nums, nx, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY, font, g);
			}
		} catch (Exception e) { }
		
		if (keyTimeout) showCursor = true;
		
		// Desenhar cursor
		if (!isReadOnly)
			if (showCursor && !((cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY < MIN_Y - 40 || ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < x + (FONT_SIZE * 2))) && !WindowInput.isDeactivated()) {
				g.setColor(Colors.cursor);
				g.fillRect(drawcx, drawcy, 2, FONT_SIZE); // * 14
			}
		
		// desenhar barra inferior
		if (editing != null) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22);
			
			Fonts.drawString(codeType + " - " + extType + " | " + "X: " + (cursorX + 1) + ", Y: " + cursorY, x + 10, Main.screen.getHeight() - 20, new IDEFont(Fonts.otherNormal, 16), g);
			
			//Fonts.drawString("X: " + (cursorX + 1) + ", Y: " + cursorY, Main.screen.getWidth() - 170, Main.screen.getHeight() - 20, new IDEFont(Fonts.otherNormal, 16), g);
		}
		
		g.setColor(Colors.background);
		g.fillRect(x, 0, width, 35);
		
		for (Tab t : Main.editor.tabs) {
			if (t.getX() + tabScr < x || t.getX() + tabScr > Main.screen.getWidth()) continue;
			
			t.render(g);
		}
		
		for (RightClickOption r : autocompletes)
			r.render(g);
		
		if (alternateTabsMode) {
			g.setColor(new Color(0, 0, 0, 0.3f));
			g.fillRect(x, y + 35, width, height);
			
			int xdr = MouseInput.getMouseX() + 10;
			int ydr = MouseInput.getMouseY() - 30; // TODO alterar texto "trocar aba" por algo melhor
			
			Fonts.drawString(Texts.selectTabOrder, xdr + 10, ydr, new IDEFont(Fonts.lighterGrayNormal, 16), g); // TODO colocar pra 20
			
			Fonts.drawString(Texts.esc_Cancel, xdr + 10, ydr + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			Fonts.drawString(Texts.leftClickTab, xdr + 10, (ydr + 30) + 18, new IDEFont(Fonts.lighterGrayNormal, 16), g);
		}
		
		if (isReadOnly && hovered() && !(CommandTerminal.active || SetFileName.added || RenameFile.added || alternateTabsMode) && !RightClickOption.isRightClickActive()) {
			int xdr = MouseInput.getMouseX() + 10;
			int ydr = MouseInput.getMouseY() - 30;
			
			final int wdr = 810;
			final int hdr = 80;
			
			Rectangle intersection = new Rectangle(xdr, ydr, wdr, hdr).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
			
			if (!intersection.isEmpty())
				xdr -= intersection.getWidth();
			
			g.setColor(Colors.explorerLight);
			g.fillRect(xdr, MouseInput.getMouseY() - 35, wdr, hdr);
			
			g.setColor(Colors.textLighter);
			g2.setStroke(new BasicStroke(2f));
			g2.drawRect(xdr, MouseInput.getMouseY() - 35, wdr, hdr);
			
			Fonts.drawString(Texts.fileAsReadOnly, xdr + 10, ydr, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			
			Fonts.drawString(Texts.readOnlyText1, xdr + 10, ydr + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			Fonts.drawString(Texts.readOnlyText2, xdr + 10, (ydr + 30) + 18, new IDEFont(Fonts.lighterGrayNormal, 16), g);
		}
	}
}
