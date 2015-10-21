package eu.dariah.de.minfba.schereg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/mapping")
public class MappingController extends BaseScheregController {
	@Autowired private MappingService mappingService;
	@Autowired private SchemaService schemaService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	
	public MappingController() {
		super("mapping");
	}

	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getList(Model model) {
		return "mapping/home";
	}
}
