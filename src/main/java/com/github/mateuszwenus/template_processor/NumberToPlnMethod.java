package com.github.mateuszwenus.template_processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.google.common.base.CharMatcher;
import com.ibm.icu.text.RuleBasedNumberFormat;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class NumberToPlnMethod implements TemplateMethodModel {

	@SuppressWarnings("rawtypes")
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments == null || arguments.size() != 1) {
			return "-";
		}
		String numStr = CharMatcher.DIGIT.or(CharMatcher.anyOf(".,"))
				.retainFrom(String.valueOf(arguments.get(0))).replace(',', '.');
		if (numStr.isEmpty()) {
			return "-";
		}
		try {
			BigDecimal num = new BigDecimal(numStr);
			int zl = num.intValue();
			int gr = num.subtract(BigDecimal.valueOf(zl)).multiply(BigDecimal.valueOf(100)).intValue();
			RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(Locale.getDefault(), RuleBasedNumberFormat.SPELLOUT);
			return fmt.format(zl) + " z\u0142 " + fmt.format(gr) + " gr";
		} catch (NumberFormatException e) {
			return "[invalid number " + numStr + "]";
		}
	}
}
