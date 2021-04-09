/**
 * Boot IDE
 * 
 * Actual Version: Release 1.2
 * 
 * Changelog:
 * 
 * 1.0
 * 
 * - Release
 * 
 * 1.1
 * 
 * - Adicionada opção "Limpar linha".
 * - Adicionado suporte para a linguagem Lua.
 * - Terminado coloração de comentários de uma linha só pra todas as linguagens.
 * - Ainda não há coloração para comentários multi-linha
 * 
 * 1.2
 * 
 * - Corrigido Bugs:
 *  * Alternar para outra guia sem salvar o arquivo o corrompe;
 *  * Deletar o arquivo e a sua guia correspondente ficar aberta;
 *  * <Ainda não corrigido> Clicar em um arquivo que já tem guia aberta e cria de novo outra guia com o mesmo arquivo. // Esse pode para corrigir mais pra frente pois ele não é crítico.
 *  
 *  - Adicionado suporte para a linguagem SQL.
 */

package ide.main;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.ArrayList;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.components.Logo;
import ide.components.NewFileButton;
import ide.components.NewFolderButton;
import ide.components.OneLevelAboveButton;
import ide.components.OpenBaseFolderButton;
import ide.components.ReloadButton;
import ide.components.ReturnToBaseFolderButton;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.input.MouseInput;
import ide.util.Colors;
import ide.util.Spritesheet;
import ide.util.Tickable;

public class Main implements Runnable, Tickable {

    private boolean running = false;

    public static Screen screen;
    public static Toolkit toolkit;

    private Thread t;

    public static Explorer explorer;
    public static CodeEditor editor;
    
    public static Spritesheet spritesheet;
    public static Logo logo;
    
    public static OpenBaseFolderButton openBase;
    
    public static OneLevelAboveButton oneLevel;
    public static ReturnToBaseFolderButton returnBase;
    public static NewFileButton newFile;
    public static NewFolderButton newFolder;
    public static ReloadButton reload;
    
    public static File baseFolder;
    
    public Main() {
        toolkit = Toolkit.getDefaultToolkit();
        screen = new Screen("Boot IDE");
        
        spritesheet = new Spritesheet("/spritesheet.png");

        explorer = new Explorer(0, 0, 280, Screen.HEIGHT);
        editor = new CodeEditor(280, 0, Screen.WIDTH - 280, Screen.HEIGHT);
        
        logo = new Logo(Screen.WIDTH / 2 + 80, Screen.HEIGHT / 2 - 120, 160, 160, spritesheet.getSprite(32, 0, 16, 16));
        
        screen.setFrameIcon(spritesheet.getSprite(32, 0, 16, 16));
        
        openBase = new OpenBaseFolderButton(20, 70, 48, 48, spritesheet.getSprite(0, 0, 16, 16));
        oneLevel = new OneLevelAboveButton(160, 85, 32, 32, spritesheet.getSprite(64, 0, 16, 16));
        returnBase = new ReturnToBaseFolderButton(200, 85, 32, 32, spritesheet.getSprite(80, 0, 16, 16));
        newFile = new NewFileButton(80, 85, 32, 32, spritesheet.getSprite(96, 0, 16, 16));
        newFolder = new NewFolderButton(120, 85, 32, 32, spritesheet.getSprite(112, 0, 16, 16));
        reload = new ReloadButton(240, 85, 32, 32, spritesheet.getSprite(128, 0, 16, 16));

        
        IDEComponent.components.add(logo);
        
        IDEComponent.components.add(explorer);
        IDEComponent.components.add(editor);
        
        IDEComponent.components.add(openBase);
    }

    public synchronized void start() {
        running = true;

        t = new Thread(this);
        t.start();
    }

    @Override
    public void tick() {
        for (IDEComponent c : IDEComponent.components)
            c.tick();
        
        for (ListableFile f : Explorer.files)
        	f.tick();
        
        MouseInput.updateMouse();
        
        IDEComponent.components.removeAll(IDEComponent.toRemove);
        IDEComponent.toRemove.clear();
        
        IDEComponent.components.addAll(IDEComponent.toAdd);
        IDEComponent.toAdd.clear();
        
        Explorer.files.removeAll(Explorer.toRemove);
        Explorer.toRemove.clear();
        
        if (!ListableFile.files.isEmpty())
        	Explorer.files = new ArrayList<>(ListableFile.files);
    }

    public void render() {
        BufferStrategy bs = screen.getBufferStrategy();

        if (bs == null) {
            screen.createBufferStrategy(3);
            return;
        }

        Graphics g = screen.layer.getGraphics();

        g.setColor(Colors.background);
        g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);

        for (IDEComponent c : IDEComponent.components)
            c.render(g);
        
        for (ListableFile f : Explorer.files)
        	f.render(g);
        
        g.dispose();
        g = bs.getDrawGraphics();

        g.drawImage(screen.layer, 0, 0, Screen.WIDTH, Screen.HEIGHT, null);

        bs.show();
    }

    @Override
    public void run() {
        while (running) {
            tick();
            render();
            
            try {
				Thread.sleep(1000/120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}
