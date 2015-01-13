package de.dariah.schereg.view;

import java.util.Locale;

import org.joda.time.DateTime;

import com.google.gson.JsonObject;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.util.ReadableDateTimeSerializer;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.util.ImmutableDateTimeSerializer;

public class ModelToJsonConverter {

	public static JsonObject getSchemaAsJson(Schema schema, String status, Locale locale) {
		
		ReadableDateTimeSerializer readableSer = new ReadableDateTimeSerializer(locale);
		ImmutableDateTimeSerializer immutableSer = new ImmutableDateTimeSerializer();
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", schema.getId());
		obj.addProperty("status", status);
		if (status == ScheRegConstants.MODEL_JSON_STATUS_DELETE) {
			return obj;
		}
		
		obj.addProperty("name", schema.getName());
		obj.addProperty("sourceShort", schema.getSourceShort());
		obj.addProperty("type", schema.getType());
		obj.addProperty("state", schema.getState());
		obj.addProperty("modified", readableSer.serialize(schema.getModified(), DateTime.class, null).getAsString());
		obj.addProperty("lookupTimestamp", immutableSer.serialize(schema.getModified(), DateTime.class, null).getAsString());
		obj.addProperty("description", schema.getDescription());
		if (schema.getIsLocked()) {
			obj.addProperty("actions", "FALSE");
		} else {
			obj.addProperty("actions", "TRUE");
		}
		
		return obj;
	}

	public static JsonObject getMappingAsJson(Mapping mapping, String status, Locale locale) {
		
		ReadableDateTimeSerializer readableSer = new ReadableDateTimeSerializer(locale);
		ImmutableDateTimeSerializer immutableSer = new ImmutableDateTimeSerializer();
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", mapping.getId());
		obj.addProperty("status", status);
		
		if (status == ScheRegConstants.MODEL_JSON_STATUS_DELETE) {
			return obj;
		}
		
		obj.addProperty("name", String.format("%s &rarr; %s", mapping.getSource().getName(), mapping.getTarget().getName()));
		obj.addProperty("source.name", mapping.getSource() == null ? "?" :  mapping.getSource().getName());
		obj.addProperty("target.name", mapping.getTarget() == null ? "?" :  mapping.getTarget().getName());
		obj.addProperty("mappingCells", mapping.getMappingCells() == null ? -1 :  mapping.getMappingCells().size());
		obj.addProperty("state", mapping.getState());
		obj.addProperty("lookupTimestamp", immutableSer.serialize(mapping.getModified(), DateTime.class, null).getAsString());
		obj.addProperty("modified", readableSer.serialize(mapping.getModified(), DateTime.class, null).getAsString());
		
		if (mapping.getIsLocked()) {
			obj.addProperty("actions", "FALSE");
		} else {
			obj.addProperty("actions", "TRUE");
		}
		
		
		return obj;
	}
}
