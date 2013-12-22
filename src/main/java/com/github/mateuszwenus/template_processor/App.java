package com.github.mateuszwenus.template_processor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.List;

import javax.swing.BoxLayout;
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

public class App {

	private static final String OPEN_FILE = "Otwórz szablon";
	private static final String ADD_ROW = "Dodaj wiersz";
	private static final String GENERATE = "Generuj ODT i PDF";

	private static final String ICON_ADD = "add.png";
	private static final String ICON_OPEN = "open.png";
	private static final String ICON_SAVE = "save.png";

	private JFrame frame;
	private JProgressBar progressBar;
	private JTable table;

	private File currentFile;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new App();
	}

	public App() {
		frame = new JFrame("Template");
		frame.setLayout(new FlowLayout());
		frame.add(createFramePanel());
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private JPanel createFramePanel() {
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.Y_AXIS));
		framePanel.add(createUpPanel());
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setString("");
		framePanel.add(progressBar);
		return framePanel;
	}

	private Component createUpPanel() {
		JPanel upPanel = new JPanel();
		upPanel.setLayout(new FlowLayout());
		upPanel.add(createTablePanel());
		upPanel.add(createButtonsPanel());
		return upPanel;
	}

	private JScrollPane createTablePanel() {
		table = new JTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
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
		gbc.ipady = 0;
		buttonsPane.add(createOpenButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createAddRowButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createGenerateButton(), gbc);
		gbc.gridy++;
		return buttonsPane;
	}

	private JButton createOpenButton() {
		JButton addButton = createButton(OPEN_FILE, ICON_OPEN);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ODT files", "odt");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					currentFile = chooser.getSelectedFile();
					List<String> currentVariables = loadTemplateFields(currentFile);
					refreshTable(currentVariables);
				}
			}

		});
		return addButton;
	}

	private JButton createAddRowButton() {
		JButton addRowButton = createButton(ADD_ROW, ICON_ADD);
		addRowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object[] data = null;
				model.addRow(data);
			}

		});
		return addRowButton;
	}

	private JButton createGenerateButton() {
		JButton addRowButton = createButton(GENERATE, ICON_SAVE);
		addRowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GenerateFilesTask task = new GenerateFilesTask(currentFile, (DefaultTableModel) table.getModel(), progressBar);
				task.execute();
			}

		});
		return addRowButton;
	}

	private List<String> loadTemplateFields(File file) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			FreemarkerAwareDocumentTemplate tpl = new FreemarkerAwareDocumentTemplate(in);
			return tpl.getFreemarkerVariableNames();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error " + e.getMessage());
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
