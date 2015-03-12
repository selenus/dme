package eu.dariah.de.minfba.schereg.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/")
public class HomeController {
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String getHome(HttpServletResponse response) throws IOException  {
		/* TODO: For now...redirect; in the future a ScheReg dashboard is intended */
		response.sendRedirect("schema/");
		return null;
	}
}
