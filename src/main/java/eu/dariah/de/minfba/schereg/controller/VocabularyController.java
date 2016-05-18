package eu.dariah.de.minfba.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.controller.DataTableList;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.model.PersistedVocabulary;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedVocabularyService;

@Controller
@RequestMapping(value="/vocabulary")
public class VocabularyController extends BaseTranslationController {
	
	@Autowired private PersistedVocabularyService vocabularyService;
	
	public VocabularyController() {
		super("vocabulary");
	}
	
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String getList(Model model) {
		return "vocabulary/home";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<PersistedVocabulary> getData(Model model, Locale locale, HttpServletRequest request) {
		//AuthPojo auth = authInfoHelper.getAuth(request);
		List<PersistedVocabulary> vocabularies = vocabularyService.findAll();
		return new DataTableList<PersistedVocabulary>(vocabularies);
	}
	
	@Secured("IS_AUTHENTICATED_FULLY")
	@RequestMapping(method=GET, value={"/forms/add"})
	public String getAddForm(Model model, Locale locale) {		
		model.addAttribute("vocabulary", new PersistedVocabulary());
		model.addAttribute("actionPath", "/vocabulary/async/save");
		return "vocabulary/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/edit/{id}"})
	public String getEditForm(@PathVariable String id, Model model, Locale locale) {
		PersistedVocabulary vocabulary = vocabularyService.findById(id);
		model.addAttribute("actionPath", "/vocabulary/async/save");
		model.addAttribute("vocabulary", vocabulary);
		return "vocabulary/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/save"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo saveVocabulary(@Valid PersistedVocabulary vocabulary, BindingResult bindingResult, Locale locale) {
		if (vocabulary.getId()!=null && vocabulary.getId().isEmpty()) {
			vocabulary.setId(null);
		}
		
		PersistedVocabulary saveVocabulary = null;
		if (vocabulary.getId()!=null) {
			saveVocabulary = vocabularyService.findById(vocabulary.getId());
		}
		
		if (saveVocabulary==null) {
			saveVocabulary = vocabulary;
		} else {
			saveVocabulary.setLabel(vocabulary.getLabel());
			saveVocabulary.setDescription(vocabulary.getDescription());
		}
		vocabularyService.save(saveVocabulary);
		
		return new ModelActionPojo(true);
	}
}
