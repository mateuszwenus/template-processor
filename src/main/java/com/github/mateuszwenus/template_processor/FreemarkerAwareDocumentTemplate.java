package com.github.mateuszwenus.template_processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jooreports.opendocument.OpenDocumentArchive;
import net.sf.jooreports.templates.ZippedDocumentTemplate;
import nu.xom.XPathContext;

import org.apache.commons.io.IOUtils;

import freemarker.template.Configuration;

public class FreemarkerAwareDocumentTemplate extends ZippedDocumentTemplate {

	private static final String CONTENT_XML = "content.xml";
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
		Set<String> variables = new HashSet<String>();
		OpenDocumentArchive archive = getOpenDocumentArchive();
		if (archive.getEntryNames().contains(CONTENT_XML)) {
			variables.addAll(getFmVariableNamesFromContent(archive));
		}
		ArrayList<String> result = new ArrayList<String>(variables);
		Collections.sort(result);
		return result;
	}

	private Collection<String> getFmVariableNamesFromContent(OpenDocumentArchive archive) throws IOException {
		Collection<String> result = new ArrayList<String>();
		BufferedReader in = null;
		try {
			Pattern p = Pattern.compile("\\$\\{.+?\\}");
			in = new BufferedReader(archive.getEntryReader(CONTENT_XML));
			String line = null;
			while ((line = in.readLine()) != null) {
				Matcher matcher = p.matcher(line);
				while (matcher.find()) {
					result.add(tryGetFmVariableName(matcher.group()));
				}
			}
			return result;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private String tryGetFmVariableName(String str) {
		if (str.matches("\\$\\{.+\\(.+\\)\\}")) {
			return getFmVariableNameFromFunctionCall(str);
		} else {
			return simpleGetFmVariableName(str);
		}
	}

	private String getFmVariableNameFromFunctionCall(String str) {
		return str.substring(str.indexOf('(') + 1, str.length() - 2);
	}

	private String simpleGetFmVariableName(String str) {
		return str.substring(2, str.length() - 1);
	}
}
