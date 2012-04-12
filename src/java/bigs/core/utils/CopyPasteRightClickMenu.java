package bigs.core.utils;

import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

public class CopyPasteRightClickMenu extends PopupMenu
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextComponent jTextComponent;
	private String cut;
	private String copy;
	private String paste;
	private String delete;
	private String selectAll;

	
	public CopyPasteRightClickMenu(JTextComponent jTextComponent)
	{
		this(jTextComponent,"Cut", "Copy","Paste","Delete","Select all");
	}
	
	public CopyPasteRightClickMenu(JTextComponent jTextComponent, String cut, String copy, String paste, String delete, String selectAll)
	{
		super();
		this.jTextComponent = jTextComponent;
		this.cut = cut;
		this.copy = copy;
		this.paste = paste;
		this.delete = delete;
		this.selectAll = selectAll;
		jTextComponent.add(this);
		
		MyListner myListner = new MyListner();
		jTextComponent.addMouseListener(myListner);
		addActionListener(myListner);
	}

	private void resetItem()
	{
		removeAll();
		
		boolean isTestSel = jTextComponent.getSelectedText()!=null;
		boolean isEditable = jTextComponent.isEditable();

		addMenuItem(cut, isTestSel && isEditable);
		addMenuItem(copy, isTestSel);
		addMenuItem(paste, isEditable);
		addMenuItem(delete, isEditable);
		addSeparator();
		addMenuItem(selectAll, jTextComponent.isEnabled());
	}
	
	
	private void addMenuItem(String label, boolean isEnabled)
	{
		MenuItem menuItem = new MenuItem(label);
		menuItem.setEnabled(isEnabled);
		add(menuItem);
	}

	private void copy()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		TransferHandler transferHandler = jTextComponent.getTransferHandler();
		transferHandler.exportToClipboard(jTextComponent, clipboard, TransferHandler.COPY);
	}
	
	private void paste()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		TransferHandler transferHandler = jTextComponent.getTransferHandler();
		transferHandler.importData(jTextComponent, clipboard.getContents(null));
	}

	private class MyListner extends MouseAdapter implements ActionListener
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			if(e.getButton() ==3)
			{
				resetItem();
				Point point = jTextComponent.getMousePosition();
				if(point!=null)
					show(jTextComponent,point.x,point.y);
			}
		}
		
		public void actionPerformed(ActionEvent e)
		{
			String source = e.getActionCommand();
			if(source.equals(copy))
				copy();
		
			else if(source.equals(paste))
				paste();
			
			else if(source.equals(cut))
			{
				copy();
				jTextComponent.replaceSelection("");
			}
			
			else if(source.equals(delete))
				jTextComponent.replaceSelection("");
			
			else if(source.equals(selectAll))
				jTextComponent.selectAll();
		}
	}
}
