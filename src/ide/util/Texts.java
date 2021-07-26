package ide.util;

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
	
	public static String configFileLoaded;
	public static String noConfigFileLoaded;
	
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
			
			configFileLoaded = "Arquivo de Configurações carregado.";
			noConfigFileLoaded = "Não há nenhum Arquivo de Configurações carregado.";
			
			codeHelpersOn = "Os CodeHelpers estão ativados.";
			codeHelpersOff = "Os CodeHelpers estão desativados";
			
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
			closeOtherTabs = "Fechar outras abas";
			
			openBootExplorer = "Abrir no Explorador";
			
			orderTabs = "Ordenar Abas";
			
			selectTabOrder = "Selecione a aba que deseja trocar:";
			leftClickTab = "[Clique Esquerdo -> Aba] Trocar";
			
			noFolderLoadedLogoText = "Não há nenhuma pasta carregada.";
			clickOnButton = "Clique no botão [Selecionar Pasta Base]";
			loadOne = "para carregar uma.";
			
			ctrl_Win_Prompt = "[Ctrl + Windows] Prompt de Comando";
			ctrl_T_terminal = "[Ctrl + T] Terminal de Comando";
			rightClick_Options = "[Clique Direito] Mais Opções";
			
			yes = "Sim";
			no = "Não";
			
			sureDelete = "Tem certeza de que deseja deletar esse arquivo?";
			confirmDelete = "Confirmar Exclusão";
			
			delError = "Ocorreu um erro ao deletar. Lembre-se que pastas não podem ser excluídas se não estiverem vazias!";
			cantDelete = "Não foi possível deletar.";
			
			break;
		
		case ENG:
			explorerText = "Explorer";
			
			selectBaseFolder = "Select Base Folder";
			
			createFile = "Create New File";
			createFolder = "Create New Folder";
			oneFolderUp = "One Folder Up";
			returnBaseFolder = "Return to Base Folder";
			reload = "Reload";
			
			configFileLoaded = "Configuration File loaded.";
			noConfigFileLoaded = "There is no Configuration File loaded.";
			
			codeHelpersOn = "The CodeHelpers are enabled.";
			codeHelpersOff = "The CodeHelpers are disabled.";
			
			fileAsReadOnly = "This file is as Read-Only.";
			
			baseFolder_ = "Base Folder:";
			actualFolder_ = "Current Folder:";
			
			esc_Cancel = "[Esc] Cancel";
			
			enter_Create = "[Enter] Create";
			enter_Rename = "[Enter] Rename";
			
			renameFile = "Rename File";
			
			fileExists = "There is already a file in that folder with that name.";
			
			readOnly = "Read-Only";
			
			readOnlyText1 = "To toggle the Read Only modes, press";
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
			closeOtherTabs = "Close other tabs";
			
			openBootExplorer = "Open in Explorer";
			
			orderTabs = "Order tabs";
			
			selectTabOrder = "Select the tab you want to order:";
			leftClickTab = "[Left Click -> Tab] Order";
			
			noFolderLoadedLogoText = "There is no folder loaded.";
			clickOnButton = "Click on button [Select Base Folder]";
			loadOne = "to load one.";
			
			ctrl_Win_Prompt = "[Ctrl + Windows] Command Prompt";
			ctrl_T_terminal = "[Ctrl + T] Command Terminal";
			rightClick_Options = "[Right Click] More Options";
			
			yes = "Yes";
			no = "No";
			
			sureDelete = "Are you sure you want to delete this file?";
			confirmDelete = "Confirm Deletion";
			
			delError = "An error has occurred while deleting. Remember that folders cannot be deleted if they are not empty!";
			cantDelete = "Could not delete.";
			
			break;
		}
	}
}
