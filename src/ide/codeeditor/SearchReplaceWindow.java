package ide.codeeditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import ide.components.CommandTerminal;
import ide.main.Main;
import ide.util.Texts;

/**
 * Classe da janela do Search/Replace. É feita com Java Swing, mas não se preocupe, você não vai ver aquele ui feia do swing, eu alterei
 * pra vc ver como a ui do sistema.
 * 
 * Feito com a ajuda do WindowBuilder, plugin do Eclipse.
 * 
 * @author junio
 *
 */
public class SearchReplaceWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	public  JTextField txbSearch;
	private JTextField txbReplace;
	
	private int occurnum = 0;
	
	public static boolean active = false;
	
	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchReplaceWindow frame = new SearchReplaceWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */
	public SearchReplaceWindow() {
		active = true;
		
		//this.setAlwaysOnTop(true);
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 364, 361); // deixa assim mesmo
		contentPane = new JPanel();
		contentPane.setBackground(UIManager.getColor("InternalFrame.borderColor"));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSearch = new JLabel(Texts.search + ":");
		lblSearch.setBounds(10, 15, 64, 14);
		contentPane.add(lblSearch);
		
		txbSearch = new JTextField();
		txbSearch.setBounds(68, 12, 270, 19);
		contentPane.add(txbSearch);
		txbSearch.setColumns(10);
		txbSearch.requestFocus();
		
		JLabel lblReplace = new JLabel(Texts.replace + ":");
		lblReplace.setBounds(10, 45, 85, 14);
		contentPane.add(lblReplace);
		
		txbReplace = new JTextField();
		txbReplace.setColumns(10);
		txbReplace.setBounds(68, 42, 270, 19);
		contentPane.add(txbReplace);
		
		JRadioButton rdbtnEntireDocument = new JRadioButton(Texts.entireDocument);
		rdbtnEntireDocument.setBounds(10, 121, 142, 23);
		contentPane.add(rdbtnEntireDocument);
		rdbtnEntireDocument.doClick();
		
		JRadioButton rdbtnSelectedLines = new JRadioButton(Texts.selectedLines);
		rdbtnSelectedLines.setBounds(10, 147, 155, 23);
		contentPane.add(rdbtnSelectedLines);
		
		rdbtnSelectedLines.setEnabled(Main.editor.selecting);
		
		ButtonGroup scope = new ButtonGroup();
		scope.add(rdbtnEntireDocument);
		scope.add(rdbtnSelectedLines);
		
		JLabel lblScope = new JLabel(Texts.scope);
		lblScope.setBounds(28, 103, 46, 14);
		contentPane.add(lblScope);
		
		JCheckBox chkCaseSensitive = new JCheckBox(Texts.caseSensitive);
		chkCaseSensitive.setBounds(184, 121, 158, 23);
		contentPane.add(chkCaseSensitive);
		
		JLabel lblNewLabel = new JLabel(Texts.options);
		lblNewLabel.setBounds(209, 103, 46, 14);
		contentPane.add(lblNewLabel);
		
		JButton btnSearchNext = new JButton(Texts.searchNext);
		btnSearchNext.setBounds(10, 208, 328, 23);
		contentPane.add(btnSearchNext);
		
		JButton btnReplaceAll = new JButton(Texts.replaceAll);
		btnReplaceAll.setBounds(10, 242, 328, 23);
		contentPane.add(btnReplaceAll);
		
		JButton btnClose = new JButton(Texts.close);
		btnClose.setBounds(10, 288, 328, 23);
		contentPane.add(btnClose);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(5, 89, 164, 93);
		contentPane.add(panel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(181, 89, 165, 67);
		contentPane.add(panel_1);
		setLocationRelativeTo(null);
		setResizable(false);
		
		if (Main.editor.isReadOnly) {
			txbReplace.setEnabled(false);
			btnReplaceAll.setEnabled(false);
		}
		
		btnSearchNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (txbSearch.getText().equals("")) return;
				
				List<Integer> linesfound = new ArrayList<>();
				
				if (!rdbtnSelectedLines.isSelected()) {
					for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = Main.editor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				else {
					for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = Main.editor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				
				if (linesfound.size() == 0) {
					CodeEditor.setSystemLook();
					JOptionPane.showMessageDialog(null, Texts.cannotFindWord, Texts.nothingFound, JOptionPane.WARNING_MESSAGE);
					
					return;
				}
				
				try {
					Main.editor.cursorY = (linesfound.get(occurnum) - 1) + 2;
				} catch (IndexOutOfBoundsException f) {
					occurnum = 0;
					
					Main.editor.cursorY = (linesfound.get(occurnum) - 1) + 2;
					CommandTerminal.runCommand("gotocursor");
					
					CodeEditor.setSystemLook();
					JOptionPane.showMessageDialog(null, Texts.didNotFindAfterThat, Texts.itsTheEnd, JOptionPane.INFORMATION_MESSAGE);
					
				}
				
				occurnum++;
				
				CommandTerminal.runCommand("gotocursor");
				
				//setVisible(false);
				Main.screen.requestFocus();
			}
		});
		
		btnReplaceAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.editor.isReadOnly) return;
				if (txbSearch.getText().equals("")) return;
				
				List<Integer> linesfound = new ArrayList<>();
				
				String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
				String replText = txbReplace.getText();
				
				if (!rdbtnSelectedLines.isSelected()) {
					for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = Main.editor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				else {
					for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = Main.editor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				
				if (linesfound.size() == 0) {
					CodeEditor.setSystemLook();
					JOptionPane.showMessageDialog(null, Texts.cannotFindWord, Texts.nothingFound, JOptionPane.WARNING_MESSAGE);
					
					return;
				}
				
				int count = 0;
				
				for (Integer i : linesfound) {
					String s = new String(CodeEditor.toCharArray(Main.editor.lines.get(i).getChars()));
					
					s = s.replaceAll(text, replText);
					
					Main.editor.register(new StringBuilder(s), i);
					
					count++;
				}
				
				/*int i = 0;
				for (IDELine l : Main.editor.lines) {
					String s = new String(CodeEditor.toCharArray(l.getChars()));
					String replText = txbReplace.getText();
					
					String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
					
					s = s.replaceAll(text, replText);
					
					Main.editor.register(new StringBuilder(s), i++);
				}*/
				
				Main.editor.editing.setSaved(false);
				Main.editor.undo.push(new ArrayList<>(Main.editor.getLines()));
				
				CommandTerminal.runCommand("gotocursor");
				CommandTerminal.runCommand("deselect");
				
				Main.screen.requestFocus();
				
				Main.editor.callAutomaticColor();
				
				CodeEditor.setSystemLook();
				JOptionPane.showMessageDialog(null, Texts.replaced + " " + Texts.occurences + " " + Texts.in +  " " + count + " " + Texts.lines + ".", Texts.success + "!", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				Main.editor.alreadyAddedFrame = false;
				
				active = false;
			}
		});
		
		setTitle(Texts.searchReplace);
		setIconImage(Main.spritesheet.getSprite(32, 0, 16, 16));
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				//Main.editor.searchWindow.setVisible(false);
				//Main.editor.alreadyAddedFrame = false;
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				Main.editor.alreadyAddedFrame = false;
				active = false;
			}
		});
		
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_ESCAPE) {
					Main.editor.searchWindow.setVisible(false);
					Main.editor.alreadyAddedFrame = false;
					active = false;
				}
				
				if (keyCode == KeyEvent.VK_ENTER) {
					btnSearchNext.doClick();
				}
			}
		});
		
		txbSearch.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_ESCAPE) {
					Main.editor.searchWindow.setVisible(false);
					Main.editor.alreadyAddedFrame = false;
					active = false;
				}
				
				if (keyCode == KeyEvent.VK_ENTER) {
					btnSearchNext.doClick();
				}
			}
		});
		
		txbReplace.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_ESCAPE) {
					Main.editor.searchWindow.setVisible(false);
					Main.editor.alreadyAddedFrame = false;
					active = false;
				}
				
				if (keyCode == KeyEvent.VK_ENTER) {
					btnSearchNext.doClick();
				}
			}
		});
		
		setVisible(true);
		txbSearch.requestFocus();
	}
}
