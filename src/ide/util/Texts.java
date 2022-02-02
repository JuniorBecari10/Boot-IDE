package ide.util;

import ide.main.Main;

/**
 * Uma classe que lista todos os textos e palavras que estão na Boot IDE. Somente não listam os nomes das linguagens que aparecem embaixo da tela. Temos em português e inglês aqui.
 * 
 * Linguagens:
 * 	Português
 * 	Inglês
 * 
 * Tradutores de Inglês: Boot (eu hehe) e <a href="https://www.deepl.com/translator">DeepL</a>.
 * 
 * @author junio
 *
 */
public final class Texts {
	
	private Texts() {} // não vai instanciar não viu

	// --------------------------------------
	
	public static String explorerText;
	
	public static String selectBaseFolder;
	
	public static String createFile;
	public static String createFolder;
	public static String oneFolderUp;
	public static String returnBaseFolder;
	public static String reload;
	
	public static String codeHelpersOn;
	public static String codeHelpersOff;
	
	public static String fileAsReadOnly;
	
	public static String baseFolder_; // _ é dois pontos :
	public static String actualFolder_;
	
	public static String esc_Cancel;
	public static String enter_Create;
	public static String enter_Rename;
	
	public static String renameFile;
	
	public static String fileExists;
	
	public static String readOnly;
	
	public static String readOnlyText1;
	public static String readOnlyText2;
	
	public static String insertCommand;
	
	public static String enter_Execute;
	public static String ctrl_del_Clear;
	
	public static String open;
	
	public static String delete;
	public static String rename;
	public static String openCmd;
	public static String openTerminal;
	public static String openExplorer;
	public static String setBaseFolder;
	public static String openDefault;
	
	public static String execute;
	
	public static String selectLine;
	public static String selectAll;
	
	public static String save;
	
	public static String copy;
	public static String paste;
	public static String cut;
	public static String deselect;
	
	public static String closeTab;
	public static String closeAllTabs;
	public static String closeWithoutSave;
	public static String closeOtherTabs;
	
	public static String openBootExplorer;
	
	public static String orderTabs;
	
	public static String selectTabOrder;
	public static String leftClickTab;
	
	public static String noFolderLoadedLogoText;
	public static String clickOnButton;
	public static String loadOne;
	
	public static String ctrl_Win_Prompt;
	public static String ctrl_T_terminal;
	public static String rightClick_Options;
	
	public static String yes;
	public static String no;
	
	public static String sureDelete;
	public static String confirmDelete;
	
	public static String delError;
	public static String cantDelete;
	
	public static String cantFindDefault;
	public static String nothingFound;
	
	public static String cancel;
	
	public static String theFile;
	public static String isNotSaved;
	
	public static String cannotBeOnlySpaces;
	public static String fileNameIllegal;
	
	public static String confirmSave;
	
	public static String configFileNotChanged;
	public static String didNothing;
	
	public static String searchReplace;
	
	public static String search;
	public static String replace;
	
	public static String entireDocument;
	public static String selectedLines;
	
	public static String caseSensitive;
	
	public static String scope;
	public static String options;
	
	public static String searchNext;
	public static String replaceNext;
	public static String replaceAll;
	
	public static String close;
	
	public static String cannotFindWord;
	
	public static String didNotFindAfterThat;
	public static String itsTheEnd;
	
	public static String replaced;
	public static String occurences;
	
	public static String in;
	public static String lines;
	
	public static String success;
	
	public static String dont;
	
	public static String tab_Cycle;
	
	public static String cannotBeEmpty;
	
	public static String wantOpenFile;
	public static String openFolder;
	public static String wouldEdit;
//	public static String openInDefaultEditor;
	
	public static String openInEditor;
	
	public static String cannotEndDot;
	
	public static String anErrorOccurred;
	public static String errorCreatingFile;
	
	public static String fontBelowMinimum;
	public static String belowMinimum;
	
	public static String version;
	
	public static String selecting;
	
	public static String getProperty;
	public static String setProperty;
	
	public static String valueOfTheProperty;
	public static String newValueOfTheProperty;
	
	public static String is;
	
	public static String propertyDoesntExist;
	
	public static String restartRequired;
	public static String pleaseRestart;
	
	public static String getFontSize;
	public static String fontSizeIs;
	
	public static String getLang;
	public static String langIs;
	
	public static synchronized void setTexts(Language lang) {
		switch (lang) {
		case PORT:
			explorerText = "Explorador";
			
			selectBaseFolder = "Selecionar Pasta Base";
			
			createFile = "Criar Novo Arquivo";
			createFolder = "Criar Nova Pasta";
			oneFolderUp = "Uma Pasta Acima";
			returnBaseFolder = "Retornar à Pasta Base";
			reload = "Recarregar";
			
			codeHelpersOn = "Os CodeHelpers estão ativados.";
			codeHelpersOff = "Os CodeHelpers estão desativados.";
			
			fileAsReadOnly = "Esse arquivo está como somente leitura.";
			
			baseFolder_ = "Pasta Base:";
			actualFolder_ = "Pasta Atual:";
			
			esc_Cancel = "[Esc] Cancelar";
			
			enter_Create = "[Enter] Criar";
			enter_Rename = "[Enter] Renomear";
			
			renameFile = "Renomear Arquivo";
			
			fileExists = "Já existe um arquivo nessa pasta com esse nome.";
			
			readOnly = "Somente Leitura";
			
			readOnlyText1 = "Para alternar os modos Somente Leitura, aperte";
			readOnlyText2 = "Ctrl + Shift + H, ou digite togglereadonly no Terminal de Comando.";
			
			insertCommand = "Insira o comando";
			
			enter_Execute = "[Enter] Executar";
			ctrl_del_Clear = "[Ctrl + Delete] Limpar";
			
			open = "Abrir";
			
			delete = "Deletar";
			rename = "Renomear";
			openCmd = "Abrir Prompt de Comando";
			openTerminal = "Abrir Terminal de Comando";
			openExplorer = "Abrir no Explorador de Arquivos";
			setBaseFolder = "Definir pasta atual como Pasta Base";
			openDefault = "Abrir arquivo com o programa padrão";
			
			execute = "Executar";
			
			selectLine = "Selecionar Linha";
			selectAll = "Selecionar Tudo";
			
			save = "Salvar";
			
			copy = "Copiar";
			paste = "Colar";
			cut = "Cortar";
			deselect = "Desselecionar";
			
			closeTab = "Fechar aba";
			closeAllTabs = "Fechar todas as abas";
			closeWithoutSave = "Fechar sem salvar";
			closeOtherTabs = "Fechar outras abas";
			
			openBootExplorer = "Abrir no Explorador";
			
			orderTabs = "Ordenar Abas";
			
			selectTabOrder = "Selecione a aba que deseja trocar:";
			leftClickTab = "[Clique Esquerdo -> Aba] Trocar";
			
			noFolderLoadedLogoText = "Não há nenhuma pasta carregada.";
			clickOnButton = "Clique no botão [Selecionar Pasta Base]";
			loadOne = "para carregar uma.";
			
			ctrl_Win_Prompt = "[Ctrl + Windows / B] Prompt de Comando";
			ctrl_T_terminal = "[Ctrl + T] Terminal de Comando";
			rightClick_Options = "[Clique Direito] Mais Opções";
			
			yes = "   Sim   ";
			no = "   Não   ";
			
			sureDelete = "Tem certeza de que deseja deletar o arquivo";
			confirmDelete = "Confirmar Exclusão";
			
			delError = "Ocorreu um erro ao deletar. Lembre-se de que pastas não podem ser excluídas se não estiverem vazias!";
			cantDelete = "Não foi possível deletar.";
			
			cantFindDefault = "O sistema não encontrou um programa padrão para abrir esse arquivo.";
			nothingFound = "Não encontrou nada!";
			
			cancel = "Cancelar";
			
			theFile = "O arquivo";
			isNotSaved = "não está salvo. Deseja salvá-lo antes de fechar?";
			
			fileNameIllegal = "O nome do arquivo contém caracteres não permitidos.";
			cannotBeOnlySpaces = "O nome do arquivo não pode ser somente espaços!";
			
			confirmSave = "Confirmar Salvamento do Arquivo";
			
			configFileNotChanged = "O Arquivo de Configurações foi carregado, mas nada foi alterado. Deseja que ele continue carregado?";
			didNothing = "Não fez nada!";
			
			searchReplace = "Localizar/Substituir";
			
			search = "Localizar";
			replace = "Substituir";
			
			entireDocument = "Documento Inteiro";
			selectedLines = "Linhas Selecionadas";
			
			scope = "Local";
			options = "Opções";
			
			searchNext = "Localizar Próximo";
			replaceNext = "Substituir Próximo";
			replaceAll = "Substituir Todos";
			
			close = "Fechar";
			
			caseSensitive = "Sensível a letras maiúsculas";
			
			cannotFindWord = "O Localizar/Substituir não encontrou nesse documento as palavras solicitadas, usando as configurações atuais.";
			
			didNotFindAfterThat = "Não foram mais encontradas palavras correspondentes.";
			itsTheEnd = "É o fim!";
			
			replaced = "Substituídas";
			occurences = "ocorrência(s)";
			
			success = "Sucesso";
			
			dont = "Não";
			
			tab_Cycle = "[Tab] Alternar Entre Opções";
			
			cannotBeEmpty = "O nome do arquivo não pode ser vazio!";
			
			wantOpenFile = "Você deseja abrir o arquivo para editá-lo?";
			openFolder = "Abrir Pasta";
//			openInDefaultEditor = "Abrir no Editor Padrão";
			wouldEdit = "Gostaria de editar o arquivo?";
			
			in = "em";
			lines = "linhas";
			
			openInEditor = "Abrir no Editor";
			
			cannotEndDot = "O nome do arquivo não pode terminar com um ponto!";
			
			anErrorOccurred = "Um erro ocorreu.";
			errorCreatingFile = "Um erro ocorreu ao criar o arquivo.";
			
			fontBelowMinimum = "O tamanho da fonte está abaixo do mínimo! (Mínimo: 8)";
			belowMinimum = "Abaixo do mínimo!";
			
			version = "Versão";
			
			selecting = "Selecionando";
			
			getProperty = "Pegar Propriedade";
			setProperty = "Definir Propriedade";
			
			valueOfTheProperty = "O valor da propriedade";
			newValueOfTheProperty = "O novo valor da propriedade";
			
			is = "é";
			
			propertyDoesntExist = "Essa propriedade não existe, ou um erro ocorreu.";
			
			restartRequired = "Reinicialização Necessária!";
			pleaseRestart = "Por favor reinicie a " + Main.PROGRAM_NAME + " para que todas as mudanças sejam aplicadas.";
			
			getFontSize = "Pegar Tamanho da Fonte";
			fontSizeIs = "O tamanho da fonte é de";
			
			getLang = "Pegar Idioma";
			langIs = "O idioma é";
			
			break;
		
		case ENG:
			explorerText = "Explorer";
			
			selectBaseFolder = "Select Base Folder";
			
			createFile = "Create New File";
			createFolder = "Create New Folder";
			oneFolderUp = "One Folder Up";
			returnBaseFolder = "Return to Base Folder";
			reload = "Reload";
			
			codeHelpersOn = "The CodeHelpers are enabled.";
			codeHelpersOff = "The CodeHelpers are disabled.";
			
			fileAsReadOnly = "This file is as Read-Only.";
			
			baseFolder_ = "Base Folder:";
			actualFolder_ = "Current Folder:";
			
			esc_Cancel = "[Esc] Cancel";
			
			enter_Create = "[Enter] Create";
			enter_Rename = "[Enter] Rename";
			
			renameFile = "Rename File";
			
			fileExists = "There is already a file in this folder with this name.";
			
			readOnly = "Read-Only";
			
			readOnlyText1 = "To toggle the Read-Only modes, press";
			readOnlyText2 = "Ctrl + Shift + H, or type togglereadonly in the Command Terminal.";
			
			insertCommand = "Insert command:";
			
			enter_Execute = "[Enter] Execute";
			ctrl_del_Clear = "[Ctrl + Delete] Clear";
			
			open = "Open";
			
			delete = "Delete";
			rename = "Rename";
			openCmd = "Open Command Prompt";
			openTerminal = "Open Command Terminal";
			openExplorer = "Open in File Explorer";
			setBaseFolder = "Set current folder as Base Folder";
			openDefault = "Open file with default program";
			
			execute = "Execute";
			
			selectLine = "Select Line";
			selectAll = "Select All";
			
			save = "Save";
			
			copy = "Copy";
			paste = "Paste";
			cut = "Cut";
			deselect = "Deselect";
			
			closeTab = "Close tab";
			closeAllTabs = "Close all tabs";
			closeWithoutSave = "Close without save";
			closeOtherTabs = "Close other tabs";
			
			openBootExplorer = "Open in Explorer";
			
			orderTabs = "Order tabs";
			
			selectTabOrder = "Select the tab you want to swap:";
			leftClickTab = "[Left Click -> Tab] Swap";
			
			noFolderLoadedLogoText = "There is no folder loaded.";
			clickOnButton = "Click on button [Select Base Folder]";
			loadOne = "to load one.";
			
			ctrl_Win_Prompt = "[Ctrl + Windows / B] Command Prompt";
			ctrl_T_terminal = "[Ctrl + T] Command Terminal";
			rightClick_Options = "[Right Click] More Options";
			
			yes = "   Yes   ";
			no = "   No   ";
			
			sureDelete = "Are you sure you want to delete the file";
			confirmDelete = "Confirm Deletion";
			
			delError = "An error has occurred while deleting. Remember that folders cannot be deleted if they are not empty!";
			cantDelete = "Could not delete.";
			
			cantFindDefault = "The system could not find a default program to open this file.";
			nothingFound = "Nothing found!";
			
			cancel = "Cancel";
			
			theFile = "The file";
			isNotSaved = "is not saved. Do you want to save it before you close?";
			
			fileNameIllegal = "The file name contains non-permitted characters.";
			cannotBeOnlySpaces = "The file name cannot be only spaces!";
			
			confirmSave = "Confirm File Save";
			
			configFileNotChanged = "The Configuration File has been loaded, but nothing has changed. Do you want it to continue loaded?";
			didNothing = "It did nothing!";
			
			searchReplace = "Search/Replace";
			
			search = "Search";
			replace = "Replace";
			
			entireDocument = "Entire Document";
			selectedLines = "Selected Lines";
			
			scope = "Scope";
			options = "Options";
			
			searchNext = "Search Next";
			replaceNext = "Replace Next";
			replaceAll = "Replace All";
			
			close = "Close";
			
			caseSensitive = "Case Sensitive";
			
			cannotFindWord = "Search/Replace could not find the requested words in this document using the current settings.";
			
			didNotFindAfterThat = "No more matching words were found.";
			itsTheEnd = "It's the end!";
			
			replaced = "Replaced";
			occurences = "ocurrence(s)";
			
			success = "Success";
			
			dont = "Don't";
			
			tab_Cycle = "[Tab] Cycle Through Options";
			
			cannotBeEmpty = "The file name cannot be empty!";
			
			wantOpenFile = "Do you want to open the file to edit it?";
			openFolder = "Open Folder";
//			openInDefaultEditor = "Open In Default Editor";
			wouldEdit = "Would like to edit the file?";
			
			in = "in";
			lines = "lines";
			
			openInEditor = "Open in Editor";
			
			cannotEndDot = "The file name cannot end with a dot!"; // period
			
			anErrorOccurred = "An error occurred.";
			errorCreatingFile = "An error occurred while creating the file.";
			
			fontBelowMinimum = "The font size is below minimum! (Minimum: 8)";
			belowMinimum = "Below minimum!";
			
			version = "Version";
			
			selecting = "Selecting";
			
			getProperty = "Get Property";
			setProperty = "Set Property";
			
			valueOfTheProperty = "The value of the property";
			newValueOfTheProperty = "The new value of the property";
			
			is = "is";
			
			propertyDoesntExist = "This property doesn't exist, or an error occurred.";
			
			restartRequired = "Restart Required!";
			pleaseRestart = "Please restart " + Main.PROGRAM_NAME + " for all the changes to be applied.";
			
			getFontSize = "Get Font Size";
			fontSizeIs = "The font size is";
			
			getLang = "Get Language";
			langIs = "The language is";
			
			break;
		}
	}
}
