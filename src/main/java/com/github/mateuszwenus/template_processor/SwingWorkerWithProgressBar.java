package com.github.mateuszwenus.template_processor;

import java.util.ResourceBundle;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public abstract class SwingWorkerWithProgressBar<T, V> extends SwingWorker<T, V> {

	private JProgressBar progressBar;
	private ResourceBundle resourceBundle;

	public SwingWorkerWithProgressBar(JProgressBar progressBar, ResourceBundle resourceBundle) {
		this.progressBar = progressBar;
		this.resourceBundle = resourceBundle;
	}

	protected T doInBackground() throws Exception {
		boolean success = false;
		try {
			resetProgressBar();
			T result = doWork();
			success = true;
			return result;
		} catch (final Exception e) {
			showErrorOnProgressBar(e);
			return null;
		} finally {
			if (success) {
				showSuccessOnProgressBar();
			}
		}
	}

	protected abstract int getProgressBarMaximum();

	protected abstract T doWork() throws Exception;

	protected void setProgressBarValue(final int value) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(value);
				progressBar.setString(value + "/" + progressBar.getMaximum());
			}
		});
	}

	protected void setProgressBarText(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setString(text);
			}
		});
	}

	private void showSuccessOnProgressBar() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setString(resourceBundle.getString("progressBar.success"));
			}
		});
	}

	private void showErrorOnProgressBar(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString(resourceBundle.getString("progressBar.error") + ": " + e.getMessage());
			}
		});
	}

	private void resetProgressBar() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(0);
				progressBar.setString("");
				progressBar.setMaximum(getProgressBarMaximum());
			}
		});
	}
}
