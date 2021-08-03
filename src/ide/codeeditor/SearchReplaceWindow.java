package ide.codeeditor;

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

import ide.components.CommandTerminal;
import ide.main.Main;
import ide.util.Texts;

public class SearchReplaceWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txbSearch;
	private JTextField txbReplace;
	
	private int occurnum = 0;

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
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 354, 351);
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
		rdbtnSelectedLines.setBounds(10, 147, 166, 23);
		contentPane.add(rdbtnSelectedLines);
		
		rdbtnSelectedLines.setEnabled(CodeEditor.selecting);
		
		ButtonGroup scope = new ButtonGroup();
		scope.add(rdbtnEntireDocument);
		scope.add(rdbtnSelectedLines);
		
		JLabel lblScope = new JLabel(Texts.scope);
		lblScope.setBounds(28, 103, 46, 14);
		contentPane.add(lblScope);
		
		JCheckBox chkCaseSensitive = new JCheckBox(Texts.caseSensitive);
		chkCaseSensitive.setBounds(184, 121, 164, 23);
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
		setLocationRelativeTo(null);
		setResizable(false);
		
		btnSearchNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (txbSearch.getText().equals("")) return;
				
				List<Integer> linesfound = new ArrayList<>();
				
				if (!rdbtnSelectedLines.isSelected()) {
					for (int i = 0; i < CodeEditor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = CodeEditor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				else {
					for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = CodeEditor.lines.get(i);
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
					CodeEditor.cursorY = (linesfound.get(occurnum) - 1) + 2;
				} catch (IndexOutOfBoundsException f) {
					occurnum = 0;
					
					CodeEditor.cursorY = (linesfound.get(occurnum) - 1) + 2;
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
				if (CodeEditor.isReadOnly) return;
				if (txbSearch.getText().equals("")) return;
				
				List<Integer> linesfound = new ArrayList<>();
				
				String text = chkCaseSensitive.isSelected() ? txbSearch.getText() : txbSearch.getText().toLowerCase();
				String replText = txbReplace.getText();
				
				if (!rdbtnSelectedLines.isSelected()) {
					for (int i = 0; i < CodeEditor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = CodeEditor.lines.get(i);
						String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
						
						if (s.contains(text)) linesfound.add(i); // viu pq precisa do numero?
					}
				}
				else {
					for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
						IDELine l = CodeEditor.lines.get(i);
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
					String s = new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars()));
					
					s = s.replaceAll(text, replText);
					
					Main.editor.register(new StringBuilder(s), i);
					
					count++;
				}
				
				CodeEditor.editing.setSaved(false);
				
				CommandTerminal.runCommand("gotocursor");
				
				Main.screen.requestFocus();
				
				CodeEditor.setSystemLook();
				JOptionPane.showMessageDialog(null, Texts.replaced + " " + count + " " + Texts.occurences + ".", Texts.success + "!", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				CodeEditor.alreadyAddedFrame = false;
			}
		});
		
		setTitle(Texts.searchReplace);
		setIconImage(Main.spritesheet.getSprite(32, 0, 16, 16));
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				//CodeEditor.searchWindow.setVisible(false);
				//CodeEditor.alreadyAddedFrame = false;
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				CodeEditor.alreadyAddedFrame = false;
			}
		});
		
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_ESCAPE) {
					CodeEditor.searchWindow.setVisible(false);
					CodeEditor.alreadyAddedFrame = false;
				}
			}
		});
		
		setVisible(true);
		requestFocus();
	}
}
