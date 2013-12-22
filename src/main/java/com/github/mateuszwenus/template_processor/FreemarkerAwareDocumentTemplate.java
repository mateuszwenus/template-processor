package com.github.mateuszwenus.template_processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jooreports.opendocument.OpenDocumentArchive;
import net.sf.jooreports.templates.ZippedDocumentTemplate;
import nu.xom.XPathContext;
import freemarker.template.Configuration;

public class FreemarkerAwareDocumentTemplate extends ZippedDocumentTemplate {

	protected static final String DRAW_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
	protected static final String SCRIPT_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:script:1.0";
	protected static final String TABLE_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
	protected static final String TEXT_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
	protected static final String STYLE_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
	protected static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

	protected static final XPathContext XPATH_CONTEXT = new XPathContext();
	static {
		XPATH_CONTEXT.addNamespace("draw", DRAW_NAMESPACE);
		XPATH_CONTEXT.addNamespace("script", SCRIPT_NAMESPACE);
		XPATH_CONTEXT.addNamespace("table", TABLE_NAMESPACE);
		XPATH_CONTEXT.addNamespace("text", TEXT_NAMESPACE);
		XPATH_CONTEXT.addNamespace("style", STYLE_NAMESPACE);
		XPATH_CONTEXT.addNamespace("xlink", XLINK_NAMESPACE);
	}

	public FreemarkerAwareDocumentTemplate(InputStream in) throws IOException {
		super(in, new Configuration());
	}

	public List<String> getFreemarkerVariableNames() throws Exception {
		List<String> result = new ArrayList<String>();
		OpenDocumentArchive archive = getOpenDocumentArchive();
		for (Iterator<?> it = archive.getEntryNames().iterator(); it.hasNext();) {
			String entryName = (String) it.next();
			if ("content.xml".equals(entryName)) {
				Pattern p = Pattern.compile("\\$\\{.+?\\}");
				BufferedReader in = new BufferedReader(new InputStreamReader(archive.getEntryInputStream(entryName)));
				String line = null;
				while ((line = in.readLine()) != null) {
					Matcher matcher = p.matcher(line);
					while (matcher.find()) {
						String varName = trimFreemarker(matcher.group());
						if (!result.contains(varName)) {
							result.add(varName);
						}
					}
				}
				in.close();
			}
		}
		return result;
	}

	private String trimFreemarker(String group) {
		return group.substring(2, group.length() - 1);
	}
}
