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
			
			fileAsReadOnly = "This file is as read-only.";
			
			baseFolder_ = "Base Folder:";
			actualFolder_ = "Current Folder:";
			
			esc_Cancel = "[Esc] Cancel";
			
			enter_Create = "[Enter] Create";
			enter_Rename = "[Enter] Rename";
			
			renameFile = "Rename File";
			
			fileExists = "There is already a file in that folder with that name.";
			
			readOnly = "Read Only";
			
			break;
		}
	}
}
