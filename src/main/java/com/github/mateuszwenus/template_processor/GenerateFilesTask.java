package com.github.mateuszwenus.template_processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;

import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

public class GenerateFilesTask extends SwingWorkerWithProgressBar<Void, Void> {

	private File templateFile;
	private DefaultTableModel tableModel;
	private ResourceBundle resourceBundle;

	public GenerateFilesTask(File templateFile, DefaultTableModel tableModel, JProgressBar progressBar, ResourceBundle resourceBundle) {
		super(progressBar, resourceBundle);
		this.templateFile = templateFile;
		this.tableModel = tableModel;
		this.resourceBundle = resourceBundle;
	}

	protected int getProgressBarMaximum() {
		return tableModel.getRowCount();
	}

	protected Void doWork() throws Exception {
		OfficeManager officeManager = null;
		try {
			setProgressBarText(resourceBundle.getString("generateTask.startingOpenOffice"));
			officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
			officeManager.start();
			OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

			DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
			DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
			Set<String> generateFileNames = new HashSet<String>();
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				String path = generateOneFile(template, row, generateFileNames);
				converter.convert(new File(path), new File(createPdfPath(path)));
				generateFileNames.add(path);
				setProgressBarValue(row + 1);
			}
		} finally {
			if (officeManager != null) {
				officeManager.stop();
			}
		}
		return null;
	}

	private String createPdfPath(String path) {
		return path.substring(0, path.length() - 3) + "pdf";
	}

	private String generateOneFile(DocumentTemplate template, int row, Set<String> previousFileNames) throws FileNotFoundException, IOException, DocumentTemplateException {
		String fileName = generateFileName(row, previousFileNames);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("numberToText", new NumberToTextMethod());
		model.put("numberToPLN", new NumberToPlnMethod());
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			String key = tableModel.getColumnName(col);
			Object cellValue = tableModel.getValueAt(row, col);
			String value = cellValue != null ? cellValue.toString() : "";
			model.put(key, value);
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileName);
			template.createDocument(model, out);
			return fileName;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	private String generateFileName(int row, Set<String> previousFileNames) {
		String fileName = "";
		String fileExt = ".odt";
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			String key = tableModel.getColumnName(col);
			if (key.endsWith("_")) {
				Object cellValue = tableModel.getValueAt(row, col);
				String value = cellValue != null ? cellValue.toString() : "";
				if (fileName.length() > 0) {
					fileName += "_";
				}
				fileName += replaceInvalidCharacters(value);
			}
		}
		if (fileName.isEmpty()) {
			fileName = "output";
		}
		if (previousFileNames.contains(fileName + fileExt)) {
			int i = 1;
			while (previousFileNames.contains(fileName + "_" + i + fileExt)) {
				i++;
			}
			fileName = fileName + "_" + i;
		}
		return fileName + fileExt;
	}

	private String replaceInvalidCharacters(String str) {
		return str.replaceAll("[\\\\/:*?\"<>|]", "_");
	}
}
