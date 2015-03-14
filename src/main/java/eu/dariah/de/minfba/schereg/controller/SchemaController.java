package eu.dariah.de.minfba.schereg.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema")
public class SchemaController {
	
	@Autowired private SchemaService schemaService;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(Model model) {
		
		
		XmlSchema s = new XmlSchema();
		s.setExternalLabel("oai_dc");
		s.setLabel("OAI DC");
		s.setRecordPath("oai_dc:dc");
		s.setUuid(UUID.randomUUID().toString());
		s.setNamespaces(new ArrayList<XmlNamespace>());
		
		XmlNamespace xns = new XmlNamespace();
		xns.setKey("dc");
		xns.setUrl("http://dc");
		xns.setSchema(s);
		s.getNamespaces().add(xns);
		
		xns = new XmlNamespace();
		xns.setKey("oai_dc");
		xns.setUrl("http://oai_dc");
		xns.setSchema(s);
		s.getNamespaces().add(xns);
		
		schemaService.saveSchema(s);
		
		
		List<Schema> schemata = schemaService.findAllSchemata();
		
		for (Schema sDel : schemata) {
			//schemaService.deleteSchema(sDel);
			XmlSchema ssDel = (XmlSchema)sDel;
			
			for (XmlNamespace xns2 : ssDel.getNamespaces()) {
				xns2.setKey(xns2.getKey()+ "_");
			}
			
			schemaService.saveSchema(ssDel);
		}
		
		model.addAttribute("schemata", schemata);
		return "schema/home";
	}
}
