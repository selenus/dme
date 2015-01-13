package de.dariah.schereg.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.federation.model.HeartbeatPojo;

@Controller
@RequestMapping("/api")
public class RootApiController {

	@Autowired SessionFactory sessionFactory;
	
	@RequestMapping(value={"/", ""}, method=RequestMethod.GET)
	public @ResponseBody HeartbeatPojo getHeartbeat(HttpServletRequest request) {
		HeartbeatPojo result = new HeartbeatPojo();
		result.setResponsive(true);
		result.setServiceUrl(request.getRequestURL().toString());
		
		Session s = sessionFactory.getCurrentSession();
		if (s.isConnected()) {
			result.setConnected(true);
		}
		
		return result;
	}
}
