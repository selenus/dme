package de.dariah.schereg.util;

import java.lang.reflect.Type;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ReadableDateTimeSerializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime> {
	
	private Locale currentLocale;
	
	public ReadableDateTimeSerializer(Locale currentLocale) {
		this.currentLocale = currentLocale;
	}
	
	public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	      throws JsonParseException {
		  		  
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SM", this.currentLocale));
		DateTime dateTime = formatter.parseDateTime(json.getAsJsonPrimitive().getAsString());
		  
	    return dateTime;
	}
	
	@Override
	public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SM", this.currentLocale));
		String dateString = formatter.print(src);
		return new JsonPrimitive(dateString);
	}
}
