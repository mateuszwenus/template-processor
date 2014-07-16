package com.github.mateuszwenus.template_processor;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class App {

	private static final String ICON_ADD = "add.png";
	private static final String ICON_DELETE = "delete.png";
	private static final String ICON_OPEN = "open.png";
	private static final String ICON_SAVE = "save.png";

	private JFrame frame;
	private JProgressBar progressBar;
	private JTable table;
	private File currentFile;
	private ResourceBundle resourceBundle;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new App();
	}

	public App() {
		resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
		frame = new JFrame(resourceBundle.getString("app.title"));
		createFramePanel(frame.getContentPane());
		frame.pack();
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void createFramePanel(Container framePane) {
		framePane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 10;
		gbc.ipady = 10;
		gbc.weightx = 1;
		gbc.weighty = 1;
		framePane.add(createTablePanel(), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0;
		framePane.add(createButtonsPanel(), gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weighty = 0;
		framePane.add(createProgressBar(), gbc);
	}

	private JProgressBar createProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setString("");
		return progressBar;
	}

	private JScrollPane createTablePanel() {
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		PasteFromSpreadsheetHandler.registerPasteFromSpreadsheetHandler(table, resourceBundle);
		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

	private JPanel createButtonsPanel() {
		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 10;
		gbc.ipady = 10;
		buttonsPane.add(createLoadTemplateButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createAddRowButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createAddFiveRowsButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createRemoveSelectedRowsButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createGenerateButton(), gbc);
		gbc.gridy++;
		return buttonsPane;
	}

	private JButton createLoadTemplateButton() {
		JButton btn = createButton(resourceBundle.getString("action.loadTemplate"), ICON_OPEN);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ODT files", "odt");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					currentFile = chooser.getSelectedFile();
					List<String> currentVariables = loadTemplateFields(currentFile);
					refreshTable(currentVariables);
					frame.setTitle(resourceBundle.getString("app.title") + " - " + currentFile.getName());
				}
			}

		});
		return btn;
	}

	private JButton createAddRowButton() {
		JButton btn = createButton(resourceBundle.getString("action.addRow"), ICON_ADD);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object[] data = null;
				model.addRow(data);
			}

		});
		return btn;
	}

	private JButton createAddFiveRowsButton() {
		JButton btn = createButton(resourceBundle.getString("action.addFiveRows"), ICON_ADD);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object[] data = null;
				for (int i = 0; i < 5; i++) {
					model.addRow(data);
				}
			}

		});
		return btn;
	}

	private JButton createRemoveSelectedRowsButton() {
		JButton btn = createButton(resourceBundle.getString("action.removeSelectedRows"), ICON_DELETE);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rowIdxs = table.getSelectedRows();
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				for (int i = 0; i < rowIdxs.length; i++) {
					model.removeRow(rowIdxs[i] - i);
				}
			}

		});
		return btn;
	}

	private JButton createGenerateButton() {
		JButton btn = createButton(resourceBundle.getString("action.generate"), ICON_SAVE);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GenerateFilesTask task = new GenerateFilesTask(currentFile, (DefaultTableModel) table.getModel(), progressBar,
						resourceBundle);
				task.execute();
			}

		});
		return btn;
	}

	private List<String> loadTemplateFields(File file) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			FreemarkerAwareDocumentTemplate tpl = new FreemarkerAwareDocumentTemplate(in);
			return tpl.getFreemarkerVariableNames();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, resourceBundle.getString("app.error") + ": " + e.getMessage());
			return Collections.emptyList();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void refreshTable(List<String> currentVariables) {
		DefaultTableModel dataModel = new DefaultTableModel();
		table.setModel(dataModel);
		for (String variable : currentVariables) {
			dataModel.addColumn(variable);
		}
		setPreferredWidths();
	}

	private void setPreferredWidths() {
		Enumeration<TableColumn> e = table.getColumnModel().getColumns();
		while (e.hasMoreElements()) {
			e.nextElement().setPreferredWidth(150);
		}
	}

	private JButton createButton(String text, String iconPath) {
		JButton btn = new JButton(text, createIcon(iconPath));
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		return btn;
	}

	private Icon createIcon(String path) {
		URL imgURL = getClass().getResource("/" + path);
		return new ImageIcon(imgURL);
	}
}
