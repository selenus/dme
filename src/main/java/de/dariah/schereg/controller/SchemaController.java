package de.dariah.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.xml.sax.InputSource;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.model.validation.SchemaValidator;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.controller.base.BaseController;
import de.dariah.schereg.importers.schema.XmlSchemaImporter;
import de.dariah.schereg.util.ContextService;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.util.MessageContextHolder;

/**
 * Controller for the Schema Registry
 * 
 * @author Tobias Gradl
 * 
 */
@Controller
@RequestMapping("/schema")
public class SchemaController extends BaseController {

	@Autowired
	private MappingService mappingService;
	
	@Autowired
	private SchemaService schemaService;
		
	@Autowired
    private SchemaValidator schemaValidator;
	
	/**
	 * Default get operation (1 of 2) for this controller, lists all schemas.
	 * 
	 * @param model Holder for relevant model attributes
	 * @return tiles-definition (see views.xml)
	 */
	@RequestMapping(method=GET, value={"", "/"})
	public String listSchemas(@RequestParam(value="v", required=false) Integer view, Model model) {
		if (view == null) {
			view = 0;
		}
		model.addAttribute("schemas", schemaService.listSchemas());
		model.addAttribute("view", view);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		
		return "schema/list";
	}
	
	/**
	 * Default get operation (1 of 2) for this controller, lists all schemas and 
	 * 	provides detailed info on one selected schema.
	 * 
	 * @param model Holder for relevant model attributes
	 * @param id Identifier of the selected schema
	 * @return tiles-definition (see views.xml)
	 */
	@RequestMapping(method=GET, value={"", "/"}, params="schema")
	public String listSchemas(@RequestParam("schema") int id, @RequestParam(value="v", required=false) Integer view, Model model) {
		if (view == null) {
			view = 0;
		}
		model.addAttribute("schemas", schemaService.listSchemas());
		model.addAttribute("view", view);
		
		// TODO: Aggregate details?!
		model.addAttribute("selectedSchema", schemaService.getSchema(id));
		
		Collection<Mapping> mappings = mappingService.getMappingsBySchema(id);
		List<Mapping> mappingsFrom = new ArrayList<Mapping>();
		List<Mapping> mappingsTo = new ArrayList<Mapping>();
	
		if (mappings != null && mappings.size() > 0) {
			for (Mapping mapping : mappings) {
				if (mapping.getSourceId() == id) {
					mappingsFrom.add(mapping);
				} else {
					mappingsTo.add(mapping);
				}
			}
		}
		
		model.addAttribute("selectedSchemaMappingsFrom", mappingsFrom);
		model.addAttribute("selectedSchemaMappingsTo", mappingsTo);
		
		return "schema/list";
	}
	
	/**
	 * New operation, creates a new Schema and presents it in the form
	 * 
	 * @param model Holder for relevant model attributes
	 * @return tiles-definition (see views.xml)
	 */
	@RequestMapping(method=GET, value="/new")
	public String createSchema(Model model) {
		model.addAttribute("schema", new Schema());				
		model.addAttribute("schemaTypes", schemaService.getSupportedSchemaTypes());
		
		return "schema/edit";
	}
	
	/**
	 * Edit operation, loads a Schema and presents it in the form
	 * 
	 * @param id Identifier of the selected schema
	 * @param model Holder for relevant model attributes
	 * @return tiles-definition (see views.xml)
	 */
	@RequestMapping(method=GET, value="/edit")
	public String editSchema(@RequestParam("schema") int id, Model model) {
		Schema schema = schemaService.getSchema(id);
		if (schema==null) {
			return "redirect:/schema";
		}
		
		schema.rememberSource();
		
		model.addAttribute("schema", schema);
		model.addAttribute("schemaTypes", schemaService.getSupportedSchemaTypes());
		return "schema/edit";
	}
	
	/**
	 * Delete operation, deletes a Schema by an Identifier
	 * 
	 * @param id Identifier of the schema to delete
	 * @return redirect to default schema view
	 */
	@RequestMapping(method=GET, value="/delete")
	public String deleteSchema(@RequestParam("schema") int id) {
		
		Collection<Mapping> assocMappings = mappingService.getMappingsBySchema(id);
		
		if (assocMappings==null || assocMappings.size()==0) {
			schemaService.removeSchema(id);
		}
		return "redirect:/schema";
	}
	
	/**
	 * Operation that 'saves' the submitted schema
	 * TODO: Dirty save!
	 * 
	 * @param request MultipartHttpServletRequest with attached Schema file
	 * @param schema Validated Schema
	 * @param bindingResult BindingResult required to see if schema has validated correctly 
	 * @param command
	 * @param model
	 * @return
	 */
	@RequestMapping(method=POST, value="/save")
	public String saveSchema(MultipartHttpServletRequest request, @Valid Schema schema, BindingResult bindingResult, Model model) {		
		MultiValueMap<String, MultipartFile> multipartMap = request.getMultiFileMap();
		CommonsMultipartFile file = null;
		if (multipartMap != null && multipartMap.size()>0 && multipartMap.containsKey("file"))
		{
			List<MultipartFile> fileList = multipartMap.get("file");
			if (fileList.size()==1 && fileList.get(0)!=null) {
				file = (CommonsMultipartFile)fileList.get(0);
				
				// 'empty' file
				if (file.getOriginalFilename() == null || file.getSize() == 0) {
					file = null;
				}
				
			}
		}
		
		// If this is a new schema, a source needs to be provided
		if (schema.getId() <= 0 && file == null) {
			String[] codes = {"validation.model.schema.source.missing"};
			bindingResult.addError(new FieldError("schema", "source", null, true, codes, null, "For the registration of a new schema a source is required."));
		}
						
		if (file != null && !bindingResult.hasErrors()) {
			try {			
				//schemaService.importAsync(schema, file.getBytes());
				SecurityContext context = SecurityContextHolder.getContext();
				XmlSchemaImporter importer = new XmlSchemaImporter(schema.getId(), new InputSource(new ByteArrayInputStream(file.getBytes())), context);
				importer.setUp();
				importer.start();
				
				MessageContextHolder.addMessage("schemaRegistry.message.import_started", "schemaRegistry.message.import_started");
				
				//SchemaImportThread t = new SchemaImportThread(schema, new InputSource(new ByteArrayInputStream(file.getBytes())), schemaService);
				//t.start();	
			} catch (Exception e) {
				
				String[] codes = {"validation.model.schema.source.exception"};
				Object[] args = {e.getMessage()};
				
				bindingResult.addError(new FieldError("schema", "source", null, true, codes, args, "Error occured while importing file, Type: " + e.getMessage()));
				
				model.addAttribute("schema", schema);
				model.addAttribute("schemaTypes", schemaService.getSupportedSchemaTypes());
				return "schema/edit";
			}
		} else if (!bindingResult.hasErrors()) {
			// This means -> update in meta-data, just save it...
			schema.setState(ScheRegConstants.STATE_OK);
			schemaService.saveSchema(schema);
		} else {
			model.addAttribute("schema", schema);
			model.addAttribute("schemaTypes", schemaService.getSupportedSchemaTypes());
			return "schema/edit";
		}
		return "redirect:/schema";
	}
		
	/**
	 * Assigns custom validators to the provided WebDataBinder 
	 * @param binder Binds request parameters to objects
	 */
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(schemaValidator);
    }

}