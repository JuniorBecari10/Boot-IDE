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
		        	    IDEComponent.toAdd.add(Main.newFile);
						IDEComponent.toAdd.add(Main.newFolder);
						IDEComponent.toAdd.add(Main.oneLevel);
						IDEComponent.toAdd.add(Main.returnBase);
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
	        	
				if (!(files.get(0).getName().equalsIgnoreCase(".pdf") || files.get(0).getName().equalsIgnoreCase(".jar") || files.get(0).getName().equalsIgnoreCase(".iso") || files.get(0).getName().equalsIgnoreCase(".img") || files.get(0).getName().equalsIgnoreCase(".flp") || files.get(0).getName().equalsIgnoreCase(".class") || files.get(0).getName().equalsIgnoreCase(".exe") || files.get(0).getName().equalsIgnoreCase(".urna") || files.get(0).getName().equalsIgnoreCase(".save") || files.get(0).getName().equalsIgnoreCase(".docx") || files.get(0).getName().equalsIgnoreCase(".pptx") || files.get(0).getName().equalsIgnoreCase(".one") || files.get(0).getName().equalsIgnoreCase(".psd") || files.get(0).getName().equalsIgnoreCase(".aed") || files.get(0).getName().equalsIgnoreCase(".ai") || files.get(0).getName().equalsIgnoreCase(".indd") || files.get(0).getName().equalsIgnoreCase(".ini") || files.get(0).getName().equalsIgnoreCase(".dll") || files.get(0).getName().equalsIgnoreCase(".png") || files.get(0).getName().equalsIgnoreCase(".jpg") || files.get(0).getName().equalsIgnoreCase(".jpeg") || files.get(0).getName().equalsIgnoreCase(".gif") || files.get(0).getName().equalsIgnoreCase(".bmp") || files.get(0).getName().equalsIgnoreCase(".ico") || files.get(0).getName().equalsIgnoreCase(".webp") || files.get(0).getName().equalsIgnoreCase(".mp4") || files.get(0).getName().equalsIgnoreCase(".wmv") || files.get(0).getName().equalsIgnoreCase(".avi") || files.get(0).getName().equalsIgnoreCase(".wav") || files.get(0).getName().equalsIgnoreCase(".mp3") || files.get(0).getName().equalsIgnoreCase(".ogg") || files.get(0).getName().equalsIgnoreCase(".otf") || files.get(0).getName().equalsIgnoreCase(".ttf") || files.get(0).getName().equalsIgnoreCase(".woff") || files.get(0).getName().equalsIgnoreCase(".woff2") || files.get(0).getName().equalsIgnoreCase(".zip") || files.get(0).getName().equalsIgnoreCase(".rar") || files.get(0).getName().equalsIgnoreCase(".7z") || files.get(0).getName().equalsIgnoreCase(".bin"))) {
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
				}
				
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
