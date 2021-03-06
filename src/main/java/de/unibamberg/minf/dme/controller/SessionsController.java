package de.unibamberg.minf.dme.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.LogEntry;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import eu.dariah.de.dariahsp.web.AuthInfoHelper;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/sessions")
public class SessionsController {
	
	@Autowired protected AuthInfoHelper authInfoHelper;
	@Autowired private PersistedSessionService sessionService;
	@Autowired protected MessageSource messageSource;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public ModelActionPojo getHome(HttpServletResponse response)  {
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/deleteSession")
	public @ResponseBody ModelActionPojo deleteSession(@RequestParam String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		PersistedSession sCurrent = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (sCurrent==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		sessionService.deleteSession(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/newSession")
	public @ResponseBody ModelActionPojo newSession(@RequestParam String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) throws GenericScheregException {
		String userId = authInfoHelper.getUserId(request);
		PersistedSession sCurrent = sessionService.access(entityId, request.getSession().getId(), userId);
		if (sCurrent==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		// Unassign current session
		sCurrent.setHttpSessionId(null);
		sessionService.saveSession(sCurrent);
		
		// New session
		sessionService.createAndSaveSession(entityId, request.getSession().getId(), userId, messageSource, locale);
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/loadSession")
	public String getFormLoadSession(@RequestParam String entityId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		PersistedSession sCurrent = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (sCurrent==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		List<PersistedSession> userSessions = sessionService.findAllByUser(entityId, authInfoHelper.getUserId(request));
		List<PersistedSession> savedSessions = new ArrayList<PersistedSession>();
		List<PersistedSession> transientSessions = new ArrayList<PersistedSession>();
		
		for (PersistedSession s : userSessions) {
			if (s.isNotExpiring()) {
				savedSessions.add(s);
			} else {
				transientSessions.add(s);
			}
		}
		Collections.sort(savedSessions);
		Collections.reverse(savedSessions);
		
		Collections.sort(transientSessions);
		Collections.reverse(transientSessions);
		
		model.addAttribute("locale", locale);
		model.addAttribute("savedSessions", savedSessions);
		model.addAttribute("transientSessions", transientSessions);
		model.addAttribute("currentSessionId", sCurrent.getId());
		model.addAttribute("actionPath", "/sessions/async/loadSession");
		return "session/forms/load";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/saveSession")
	public String getFormSaveSession(@RequestParam String entityId, Model model, HttpServletRequest request, Locale locale, HttpServletResponse response) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		model.addAttribute("session", s);
		model.addAttribute("actionPath", "/sessions/async/saveSession");
		return "session/forms/save";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveSession")
	public @ResponseBody ModelActionPojo saveSession(@Valid PersistedSession session, HttpServletRequest request, Locale locale, HttpServletResponse response) {
		PersistedSession saveSession = sessionService.access(session.getEntityId(), request.getSession().getId(), authInfoHelper.getUserId(request));
		if (saveSession!=null) {
			saveSession.setLabel(session.getLabel());
			saveSession.setNotExpiring(true);
			sessionService.saveSession(saveSession);
			return new ModelActionPojo(true);
		}
		response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
		return new ModelActionPojo(false);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/loadSession")
	public @ResponseBody ModelActionPojo loadSession(@RequestParam String sessionId, HttpServletRequest request, Locale locale, HttpServletResponse response) {
		PersistedSession s = sessionService.reassignPersistedSession(request.getSession().getId(), authInfoHelper.getUserId(request), sessionId);
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return new ModelActionPojo(false);
		}
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getLog")
	public @ResponseBody Collection<LogEntry> getLog(@RequestParam String entityId, @RequestParam(defaultValue="10") Integer maxEntries, @RequestParam(required=false) Long tsMin, HttpServletRequest request, HttpServletResponse response) {
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		List<LogEntry> log = session.getSortedSessionLog();
		if (tsMin!=null && log.size()>0 && log.get(0).getNumericTimestamp()<=tsMin) {
			return new ArrayList<LogEntry>();
		}
		
		if (log.size() > maxEntries) {
			return log.subList(0, maxEntries);
		}
		return log;
	}
}
