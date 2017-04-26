package eu.dariah.de.minfba.schereg.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private ServletContext servletContext;
	
	@Value("#{environment.saml!=null?environment.saml:false}")
	private boolean saml;
	
	/* For now...redirect; in the future a dash-board is intended */
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(HttpServletResponse response) throws IOException  {
		response.sendRedirect("registry/");
		return null;
	}
	
	@RequestMapping("favicon.ico")
    public String forwardFavicon() {
        return "forward:/resources/img/page_icon.png";
    }
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String getLogout(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "url", defaultValue = "/") String url, HttpServletRequest request, HttpServletResponse response, Model model) throws IOException  {	
		if (saml && authInfoHelper.getCurrentUserDetails(request).isAuth()) {
			return "redirect:/saml/logout" + (!url.equals("/") ? "?loginRedirectUrl=" + url : "");
		} else if (!saml && authInfoHelper.getCurrentUserDetails(request).isAuth()) {
			return "redirect:/localsec/logout" + (!url.equals("/") ? "?loginRedirectUrl=" + url : "");
		}
		return "common/logout";
	}
		
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLogin(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "url", defaultValue = "/") String url, HttpServletRequest request, HttpServletResponse response, Model model) throws IOException  {
		if (saml) {
			return "redirect:/saml/login" + (!url.equals("/") ? "?loginRedirectUrl=" + url : "");
		}
		
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