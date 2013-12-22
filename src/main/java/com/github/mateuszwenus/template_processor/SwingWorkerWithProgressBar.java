package com.github.mateuszwenus.template_processor;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public abstract class SwingWorkerWithProgressBar<T, V> extends SwingWorker<T, V> {

	private JProgressBar progressBar;

	public SwingWorkerWithProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
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
				progressBar.setString("Gotowe");
			}
		});
	}

	private void showErrorOnProgressBar(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString("Błąd: " + e.getMessage());
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
