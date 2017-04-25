package eu.dariah.de.minfba.schereg.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.dariahsp.web.AuthInfoHelper;


@Controller
@RequestMapping(value="")
public class HomeController {
	@Autowired(required=false) protected AuthInfoHelper authInfoHelper;
	
	@Autowired private ServletContext servletContext;
	
	/* For now...redirect; in the future a dash-board is intended */
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(HttpServletResponse response) throws IOException  {
		response.sendRedirect("registry/");
		return null;
	}
		
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLogin(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "url", defaultValue = "/") String url, HttpServletResponse response, Model model) throws IOException  {
		if (error != null) {
			model.addAttribute("error", true);
		}
		
		String ctx = servletContext.getContextPath();
		if (url.startsWith(ctx)) {
			url = url.substring(ctx.length());
		}

		model.addAttribute("redirectUrl", url);
		return "common/login";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/isAuthenticated")
	public @ResponseBody boolean getIsLoggedIn(HttpServletRequest request) {
		return authInfoHelper.getAuth(request).isAuth();
	}
}