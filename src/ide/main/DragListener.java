package ide.main;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;

public class DragListener implements DropTargetListener {

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
		      Transferable tr = dtde.getTransferable();
		      DataFlavor[] flavors = tr.getTransferDataFlavors();
		      
		      for (int i = 0; i < flavors.length; i++) {
		        //System.out.println("Possible flavor: " + flavors[i].getMimeType());
		        
		        if (flavors[i].isFlavorJavaFileListType()) {
		          dtde.acceptDrop(DnDConstants.ACTION_COPY);
		          
		          System.out.println("Success!");

		          List<Object> list = (List<Object>) tr.getTransferData(flavors[i]);
		          List<File> files = new ArrayList<>();
		          
		          for (Object o : list)
		        	  if (o instanceof File)
		        		  files.add((File) o);
		          
		          if (Main.baseFolder == null) {
						IDEComponent.toAdd.add(Main.oneLevel);
						IDEComponent.toAdd.add(Main.returnBase);
						IDEComponent.toAdd.add(Main.newFile);
						IDEComponent.toAdd.add(Main.newFolder);
						IDEComponent.toAdd.add(Main.reload);
					}
		          
		          ListableFile.files.clear();
				  Explorer.files.clear();
		          
		          		if (files.get(0).isDirectory()) {
		          			Main.baseFolder = files.get(0);
							
							Explorer.scope = null;
			        	  	
			        	  	int index = 0;
							
							for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
								ListableFile.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
								
								index++;
							}
		          			
							dtde.dropComplete(true);
		          			return;
		          		}
		          
		        	  	Main.baseFolder = files.get(0).getParentFile();
		        	  	
		        	  	Explorer.files.clear();
						ListableFile.files.clear();
						
						Explorer.scope = null;
		        	  	
		        	  	int index = 0;
						
						for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
							ListableFile.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
							
							index++;
						}
		          
				int lastX = CodeEditor.tabs.size() > 0 ? CodeEditor.tabs.get(CodeEditor.tabs.size() - 1).getX() : Tab.MIN_X;
	        	
	        	Tab toAdd = new Tab(CodeEditor.tabs.size() > 0 ? (lastX + Tab.WIDTH) + 3 : Tab.MIN_X - Tab.WIDTH, ListableFile.searchListableFiles(files.get(0)));
	        	
  				CodeEditor.cursorX = 0;
  				CodeEditor.cursorY = 1;
  				
  				CodeEditor.scrX = 0;
  				CodeEditor.scrY = 0;
  				
	        	  	CodeEditor.editing = toAdd;
	        	  	CodeEditor.tabs.add(toAdd);
					
	        	  	new Thread() {
						public void run() {
							try {
								CodeEditor.lines = CodeEditor.readFile(files.get(0));
							} catch (IOException e) { // não suportado, se caiu aqui
								return;
							}
						}
					}.start();
	        	  	
					Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
		          
		          dtde.dropComplete(true);
		          return;
		        }
		      }
		      System.out.println("Drop failed: " + dtde);
		      dtde.rejectDrop();
		    } catch (Exception e) {
		      e.printStackTrace();
		      dtde.rejectDrop();
		    }
	}
}
