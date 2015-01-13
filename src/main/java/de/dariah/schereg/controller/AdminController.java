package de.dariah.schereg.controller;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.dariah.schereg.base.model.LoggingEvent;
import de.dariah.schereg.base.service.LoggingEventService;
import de.dariah.schereg.controller.base.BaseController;


@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired private LoggingEventService loggingEventService;
	
	@RequestMapping(value = "/log", method = RequestMethod.GET)
	public String getLog(@RequestParam(value="limit", required=false) Integer limit, ModelMap model) {		
		Collection<LoggingEvent> loggingEvents = loggingEventService.getLatest(30);
		model.put("loggingEvents", loggingEvents);
		
		return "admin/log";
	}
	
	@RequestMapping(value = "/log/refresh", method = RequestMethod.GET)
	public String refreshLogAsync(@RequestParam String currentLatestItem, Model model) {
		int id = Integer.parseInt(currentLatestItem);
		Collection<LoggingEvent> newerEntries = loggingEventService.listNewerLogEntries(30, id);
		model.addAttribute("loggingEvents", newerEntries);
		return "admin/log/tr";
	}

	@RequestMapping(value = "/userlog", method = RequestMethod.GET)
	public String getUserLog(@RequestParam(value="limit", required=false) Integer limit, ModelMap model) {
		logger.debug("getUserLog(..) requested");
		return "admin/userlog";
	}
}
