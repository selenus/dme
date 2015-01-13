package de.dariah.schereg.util;

import org.joda.time.format.*;
import org.joda.time.*;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ImmutableDateTimeSerializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime> {
		
	public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	      throws JsonParseException {
		  		  
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSSZ");
		DateTime dateTime = formatter.parseDateTime(json.getAsJsonPrimitive().getAsString());
		  
	    return dateTime;
	}

	@Override
	public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSSZ");
		String dateString = formatter.print(src);
		return new JsonPrimitive(dateString);
	}
}
