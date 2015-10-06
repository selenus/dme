package eu.dariah.de.minfba.schereg.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;

@Controller
@RequestMapping(value="/sessions")
public class SessionsController {
	
	@Autowired protected AuthInfoHelper authInfoHelper;
	@Autowired private PersistedSessionService sessionService;
	
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public ModelActionPojo getHome(HttpServletResponse response)  {
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/deleteSession")
	public @ResponseBody ModelActionPojo deleteSession(@RequestParam String entityId, HttpServletRequest request, Locale locale) {
		sessionService.deleteSession(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/loadSession")
	public String getFormLoadSession(@RequestParam String entityId, Model model, HttpServletRequest request, Locale locale) {
		List<PersistedSession> allUserSessions = sessionService.findAllByUser(entityId, authInfoHelper.getUserId(request));		
		Collections.sort(allUserSessions);
		Collections.reverse(allUserSessions);
		
		model.addAttribute("locale", locale);
		model.addAttribute("prevSessions", allUserSessions);
		model.addAttribute("actionPath", "/sessions/async/loadSession");
		return "session/forms/load";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/saveSession")
	public String getFormSaveSession(@RequestParam String entityId, Model model, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		
		model.addAttribute("session", s);
		model.addAttribute("actionPath", "/sessions/async/saveSession");
		return "session/forms/save";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveSession")
	public @ResponseBody ModelActionPojo saveSession(@RequestBody PersistedSession session, HttpServletRequest request, Locale locale) {
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/loadSession")
	public @ResponseBody ModelActionPojo loadSession(@RequestParam String sessionId, HttpServletRequest request, Locale locale) {
		sessionService.reassignPersistedSession(request.getSession().getId(), authInfoHelper.getUserId(request), sessionId);
		
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getLog")
	public @ResponseBody Collection<LogEntryPojo> getLog(@RequestParam String entityId, @RequestParam(defaultValue="10") Integer maxEntries, @RequestParam(required=false) Long tsMin, HttpServletRequest request) {
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		List<LogEntryPojo> log = session.getSortedSessionLog();
		if (tsMin!=null && log.size()>0 && log.get(0).getNumericTimestamp()<=tsMin) {
			return new ArrayList<LogEntryPojo>();
		}
		
		if (log.size() > maxEntries) {
			return log.subList(0, maxEntries);
		}
		return log;
	}
}
