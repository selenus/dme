package de.dariah.schereg.controller.base;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import de.dariah.aai.web.controller.ExceptionHandlingController;
import de.dariah.base.model.impl.UserAnnotation;
import de.dariah.base.service.UserAnnotationService;

public abstract class BaseController extends ExceptionHandlingController {

	protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	// TODO: Put this in Ajax-Pocos Package?
	public class Translation {
		private String placeholder;
		private String key;
		private String translation;
		private String[] args;

		public String getPlaceholder() { return placeholder; }
		public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
		
		public String getKey() { return key; }
		public void setKey(String key) { this.key = key; }
		
		public String getTranslation() { return translation; }
		public void setTranslation(String translation) { this.translation = translation; }		
		
		public String[] getArgs() { return args; }
		public void setArgs(String[] args) { this.args = args; }	
	}
	
	@Autowired
	protected MessageSource messageSource;
		
	@Autowired
	protected UserAnnotationService annotationService;
	
	
	protected JsonObject getJsonErrorList(BindingResult bindingResult, Locale locale) {
		JsonObject result = new JsonObject();

		if (bindingResult.hasGlobalErrors()) {
			JsonObject objectErrorList = new JsonObject();
			for (ObjectError error : bindingResult.getGlobalErrors()) {
				objectErrorList.addProperty(error.getObjectName(), messageSource.getMessage(error, locale));
			}
			result.add("objectErrors", objectErrorList);
		}
		
		if (bindingResult.hasFieldErrors()) {
			
			Hashtable<String, ArrayList<String>> hashedFieldErrors = new Hashtable<String, ArrayList<String>>(); 
			for (FieldError error : bindingResult.getFieldErrors()) {
				String key = error.getObjectName() + "_" + error.getField();
				if (!hashedFieldErrors.keySet().contains(key)) {
					hashedFieldErrors.put(key, new ArrayList<String>());
				}
				hashedFieldErrors.get(key).add(messageSource.getMessage(error, locale));
			}
			
			
			JsonArray fieldErrorList = new JsonArray();
			
			for (String key : hashedFieldErrors.keySet()) {
				JsonObject field = new JsonObject();
				field.addProperty("field", key);
				
				JsonArray fieldErrors = new JsonArray();
				for (String error : hashedFieldErrors.get(key)) {
					fieldErrors.add(new JsonPrimitive(error));
				}
				field.add("errors", fieldErrors);
				
				fieldErrorList.add(field);
			}
			result.add("fieldErrors", fieldErrorList);
		}
		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getUserAnnotationsByAggregator", produces = "text/html; charset=utf-8")
	public String getUserAnnotationsByAggregator(@RequestParam String type, @RequestParam int id, Model model, Locale locale) throws ClassNotFoundException {
		
		List<UserAnnotation> annotations = annotationService.getAnnotationsByAggregator(type, id);
		model.addAttribute("annotations", annotations);
		
		return "_common/incl/user_annotations_view";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getUserAnnotationsByObject", produces = "text/html; charset=utf-8")
	public String getUserAnnotationsByObject(@RequestParam String type, @RequestParam int id, Model model, Locale locale) throws ClassNotFoundException {
		
		List<UserAnnotation> annotations = annotationService.getAnnotationsByObject(type, id);
		model.addAttribute("annotations", annotations);
		
		return "_common/incl/user_annotations_view";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/leaveComment", produces = "text/html; charset=utf-8")
	public @ResponseBody String leaveComment(Model model, @Valid UserAnnotation annotation, Locale locale) throws Exception {
		
		if (annotation.getComment()!=null && !annotation.getComment().isEmpty()) {
			if (annotation.getId()>0) {
				annotationService.updateComment(annotation.getId(), annotation.getComment());
			} else {
				UserAnnotation a = annotationService.createAnnotation(annotation.getAnnotatedObjectType(), annotation.getAnnotatedObjectId());
				a.setComment(annotation.getComment());
				annotationService.saveNewAnnotation(a);
			}
		}
			
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();
		result.addProperty("success", true);
		result.addProperty("errorCount", 0);
		
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/getCommentForm", produces = "text/html; charset=utf-8")
	public String getCommentForm(Model model, @RequestParam(required=false) Integer actionId, @RequestParam(required=false) String annotatedObjectType, 
			@RequestParam(required=false) Integer annotatedObjectId, Locale locale) throws Exception {
		
		if (actionId!=null && actionId.intValue()>0) {
			UserAnnotation le = annotationService.getAnnotation(actionId);
			model.addAttribute("annotation", le);
		} else {
			UserAnnotation le = annotationService.createAnnotation(annotatedObjectType, annotatedObjectId);
			model.addAttribute("annotation", le);
			
			if (annotatedObjectType!=null && annotatedObjectId!=null) {
				le.setAnnotatedObjectId(annotatedObjectId);
				le.setAnnotatedObjectType(annotatedObjectType);
			} else {
				throw new Exception("Invalid operation");
			}
		}
		
		return "_common/forms/commentForm";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = {"/ajax/getTranslations"}, produces = "application/json; charset=utf-8")
	public @ResponseBody String getTranslations(Model model, @RequestParam String keys, Locale locale) {	

		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		Type collectionType = new TypeToken<Collection<Translation>>(){}.getType();
		Collection<Translation> translations = gson.fromJson(keys, collectionType);

		for (Translation t : translations) {
			t.setTranslation(messageSource.getMessage(t.getKey(), t.getArgs(), locale));
		}
							
		return gson.toJson(translations, collectionType);
	}
	
	@RequestMapping(value = "/changeLog", method = RequestMethod.GET)
	public String getChangeLog() {
		return "_common/incl/change_log";
	}
	
}
