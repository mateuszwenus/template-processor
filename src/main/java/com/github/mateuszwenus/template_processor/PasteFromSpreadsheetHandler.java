package com.github.mateuszwenus.template_processor;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

public class PasteFromSpreadsheetHandler implements ActionListener {

	private final JTable table;
	private final ResourceBundle resourceBundle;

	private PasteFromSpreadsheetHandler(JTable table, ResourceBundle resourceBundle) {
		this.table = table;
		this.resourceBundle = resourceBundle;
	}

	public static void registerPasteFromSpreadsheetHandler(JTable table, ResourceBundle resourceBundle) {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK);
		table.registerKeyboardAction(new PasteFromSpreadsheetHandler(table, resourceBundle), keyStroke, JComponent.WHEN_FOCUSED);
	}

	public void actionPerformed(ActionEvent e) {
		int startRow = table.getSelectedRow();
		int startCol = table.getSelectedColumn();
		if (startRow != -1 && startCol != -1) {
			try {
				String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this)
						.getTransferData(DataFlavor.stringFlavor);
				String[] rows = clipboard.split("\n");
				for (int row = 0; row < Math.min(rows.length, table.getRowCount() - startRow); row++) {
					String[] cols = rows[row].split("\t");
					for (int col = 0; col < Math.min(cols.length, table.getColumnCount() - startCol); col++) {
						table.setValueAt(cols[col], startRow + row, startCol + col);
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, resourceBundle.getString("app.error") + ": " + ex.getMessage());
			}
		}
	}

}
