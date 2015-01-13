package de.dariah.schereg.controller;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.dariah.aai.javasp.base.SimpleUserDetails;
import de.dariah.base.model.impl.UserAnnotation;
import de.dariah.samlsp.orm.service.UserService;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.controller.base.BaseController;
import de.dariah.schereg.util.ContextService;

@Controller
@RequestMapping("/")
public class HomeController extends BaseController {

	@Autowired private UserService userService;
	@Autowired private SchemaService schemaService;
	@Autowired private MappingService mappingService;

	@Autowired private PasswordEncoder passwordEncoder;
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// Conversion between name representation and Role.class
		//binder.registerCustomEditor(RoleImpl.class, new RoleEditor(userService.getAllRoles()));
		// Trim string values and set "" to null - helps if Null values are allowed
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@RequestMapping(value = {"/", "/user"}, method = RequestMethod.GET)
	public String showUserHome(Model model, Locale locale) {
		DateTime schemaModified = schemaService.getLastModified();
		DateTime mappingModified = mappingService.getLastModified();
		
		model.addAttribute("schema_count", schemaService.getSchemaCount());
		model.addAttribute("schema_modified", schemaModified == null ? "---" : DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SM", locale)).print(schemaModified));
		model.addAttribute("crosswalk_count", mappingService.getMappingCount());
		model.addAttribute("crosswalk_modified", mappingModified == null ? "---" : DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SM", locale)).print(mappingModified));
		
		List<UserAnnotation> logEntries = annotationService.getAllAnnotations(10);
		model.addAttribute("annotations", logEntries);
		
		return "home";
	}
	
	@RequestMapping(value = "/user/redirect", method = RequestMethod.GET)
	public String redirectUser(HttpServletRequest request, Model model) {
		
		//SimpleUserDetails user = ContextService.getCurrentUserDetails();
		
		//model.addAttribute("homeOrganisationName", user.getEndpointName());
		model.addAttribute("homeOrganisationEntityID", request.getSession().getAttribute("entityId"));
		model.addAttribute("reqestedURL", request.getSession().getAttribute("originalRequestURI"));
		
		return "user/redirect";
	}

	@Secured(value="IS_AUTHENTICATED_FULLY")
	@RequestMapping(value = "/user/profile", method = RequestMethod.GET)
	public String showProfile(Model model) {
		SimpleUserDetails user = ContextService.getInstance().getCurrentUserDetails();
		if (user != null) {
			model.addAttribute("user", userService.findById(user.getId()));
			model.addAttribute("authorityList", user.getAuthorities());
			return "user/profile";
		} else {
			// Redirect to login (should be caught by filters anyway)
			return "redirect:/saml/login";
		}
	}

	/*@Secured(value="IS_AUTHENTICATED_FULLY")
	@RequestMapping(value = "/user/profile", method = RequestMethod.POST)
	public String saveProfile(@Valid UserImpl modifiedUser, BindingResult bindingResult, Model model, Locale locale) {
		if (modifiedUser.getId()!=ContextService.getInstance().getCurrentUserDetails().getId()) {
			//logger.warn("Profile to save and current login information did not match. Possible inconsistency or hack attempt. Redirecting to login!");
			return "redirect:/saml/login";
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("persistent", true);
			//model.addAttribute("authorityList", modifiedUser.getAuthorities());
			return "user/profile";
		}
		
		// Only changing the attributes that the user can actually change
		User u = userService.findById(modifiedUser.getId());
		u.setLastName(modifiedUser.getLastName());
		u.setFirstName(modifiedUser.getFirstName());
		//u.setOrganization(modifiedUser.getOrganization());
		u.setEmail(modifiedUser.getEmail());
		//u.setPhone(modifiedUser.getPhone());
		u.setLanguage(modifiedUser.getLanguage());
		/*if (modifiedUser.getPassword()!=null) {
			// Using the localUserName as salt
			if (u.getEmail() != null && !u.getEmail().isEmpty()) {
				u.setPassword(passwordEncoder.encodePassword(modifiedUser.getPassword(), u.getEmail()));
			}
		}
		
		userService.saveOrUpdateUser(u);
		//ContextService.setCurrentPersistedUser(u);
		
		MessageContextHolder.addMessage("~validation.auth.user.profile.updated", "~validation.auth.user.profile.updated");
		return showUserHome(model, locale);
	}*/
}
