package de.dariah.schereg.base.model.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import de.dariah.schereg.base.model.Mapping;

@Component
public class MappingValidator extends AbstractValidator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Mapping.class.equals(clazz);
	}

	
	protected void addExtraValidation(Object object, Errors errors) {
		
		Mapping m = (Mapping) object;

		if (m.getSourceId() == m.getTargetId()) {
			errors.rejectValue("source", "validation.model.mapping.target");
			errors.rejectValue("target", "validation.model.mapping.target");
        } 
	}
}
