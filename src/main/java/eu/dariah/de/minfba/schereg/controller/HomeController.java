package eu.dariah.de.minfba.schereg.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;

@Controller
@RequestMapping(value="/")
public class HomeController {
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(HttpServletResponse response) throws IOException  {
		/* TODO: For now...redirect; in the future a ScheReg dashboard is intended */
		response.sendRedirect("schema/");
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/isAuthenticated")
	public @ResponseBody boolean getIsLoggedIn(HttpServletRequest request) {
		return authInfoHelper.getAuth(request).isAuth();
	}
}
