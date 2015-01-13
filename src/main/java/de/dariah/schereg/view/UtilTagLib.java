package de.dariah.schereg.view;

import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;

public class UtilTagLib {
	public static char charAt(String input, int index)
    {
        return input.charAt(index);
    }
	
	public static boolean isBeforeNow(DateTime dateTime, boolean defaultWhenNull) {
		
		if (dateTime != null) {
			return dateTime.isBefore(DateTime.now());
		}
		return defaultWhenNull;
	}
	
	public static boolean contains(Collection<Integer> intList, Integer value) {
		return intList.contains(value);
	}
	
	public static String getType(Object obj) {
		return obj.getClass().getName();
	}
	
	public static String escapeText(String string) {
		return StringEscapeUtils.escapeHtml(string).replace("\n", "<br />");
	}
	
	public static String limitStringSize(String input, Integer maxLength) {
		if (input.length() > maxLength) {
			return input.substring(0, maxLength-3) + "...";
		} else {
			return input;
		}
	}
}
