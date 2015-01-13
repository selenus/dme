package de.dariah.schereg.util;

import org.joda.time.format.*;
import org.joda.time.*;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateTimeDeserializer implements JsonDeserializer<Date> {
	  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	      throws JsonParseException {
		  		  
		DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:ss");
		DateTime dateTime = formatter.parseDateTime(json.getAsJsonPrimitive().getAsString());

		  
	    return dateTime.toDate();
	  }
	}
