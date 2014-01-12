package com.github.mateuszwenus.template_processor;

import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.RuleBasedNumberFormat;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class NumberToTextMethod implements TemplateMethodModel {

	@SuppressWarnings("rawtypes")
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments == null || arguments.size() != 1) {
			return "-";
		}
		String numStr = String.valueOf(arguments.get(0));
		try {
			int num = Integer.parseInt(numStr);
			return new RuleBasedNumberFormat(Locale.getDefault(), RuleBasedNumberFormat.SPELLOUT).format(num);
		} catch (NumberFormatException e) {
			return e.getMessage();
		}
	}

}
