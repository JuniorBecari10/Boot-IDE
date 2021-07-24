package ide.util;

public class Texts {

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
			
			readOnly = "Read Only";
			
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
			
			execute = "Executar";
			
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
			
			break;
		}
	}
}
