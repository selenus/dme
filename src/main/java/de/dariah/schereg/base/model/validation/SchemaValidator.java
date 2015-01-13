package de.dariah.schereg.base.model.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import de.dariah.schereg.base.model.File;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.FileService;
import de.dariah.schereg.base.service.SchemaService;

@Component
public class SchemaValidator extends AbstractValidator {

	@Autowired
	private SchemaService schemaService;
	
	@Autowired
	private FileService fileService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Schema.class.equals(clazz);
	}

	@Override
	protected void addExtraValidation(Object object, Errors errors) {
		
		Schema schema = (Schema)object;
		
		int previousFileId = -1;
		
		if (schema.getId() > 0) {
			Schema oldSchema = schemaService.getSchema(schema.getId());
			previousFileId = oldSchema.getFile().getId();
			
			// TODO: Problem if deleted in form?!
			if (schema.getFile().getId()!=0 && (previousFileId != schema.getFile().getId())) {
				errors.rejectValue("source", "validation.model.schema.source.changenotallowed");
			}
		} else {
			if (schema.getFile().getId() <= 0) {
				errors.rejectValue("source", "validation.model.schema.source.missing");
			} else {
				File f = fileService.getFile(schema.getFile().getId());
				
				if (!f.isValidated()) {
					errors.rejectValue("source", "validation.model.schema.source.notvalidated");
				}
			}
		}

		List<Schema> sListByName = schemaService.findByName(schema.getName(), true);
		if (sListByName.size() > 1 || (sListByName.size() == 1 && sListByName.get(0).getId()!=schema.getId()) ) {
			errors.rejectValue("name", "validation.model.schema.name.duplicate");
		}
	}
}
