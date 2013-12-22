package com.github.mateuszwenus.template_processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	public GenerateFilesTask(File templateFile, DefaultTableModel tableModel, JProgressBar progressBar) {
		super(progressBar);
		this.templateFile = templateFile;
		this.tableModel = tableModel;
	}

	protected int getProgressBarMaximum() {
		return tableModel.getRowCount();
	}

	protected Void doWork() throws Exception {
		OfficeManager officeManager = null;
		try {
			setProgressBarText("Startowanie Open Office...");
			officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
			officeManager.start();
			OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

			DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
			DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				String path = generateOneFile(template, row);
				converter.convert(new File(path), new File(createPdfPath(path)));
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

	private String generateOneFile(DocumentTemplate template, int row) throws FileNotFoundException, IOException, DocumentTemplateException {
		String fileName = "output_";
		Map<String, String> model = new HashMap<String, String>();
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			String key = tableModel.getColumnName(col);
			Object cellValue = tableModel.getValueAt(row, col);
			String value = cellValue != null ? cellValue.toString() : "";
			model.put(key, value);
			if (key.endsWith("_")) {
				fileName += value + "_";
			}
		}
		FileOutputStream out = null;
		try {
			String path = fileName + row + ".odt";
			out = new FileOutputStream(path);
			template.createDocument(model, out);
			return path;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
