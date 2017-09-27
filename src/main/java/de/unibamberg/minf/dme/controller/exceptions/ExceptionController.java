package de.unibamberg.minf.dme.controller.exceptions;

import javax.servlet.http.HttpServletRequest;
import org.opensaml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.dariah.de.dariahsp.exceptions.UserCredentialsException;
import de.unibamberg.minf.core.web.pojo.MessagePojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

/**
 * 
 * @author tobias
 * 
 * Requires web.xml adaption:
 *	<error-page>
 *		<location>/errors</location>
 *	</error-page>
 *
 */
@Controller
@RequestMapping(value="/errors") 
public class ExceptionController {
	protected static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);
			
	@RequestMapping(value = {"", "/"}, method = {RequestMethod.GET, RequestMethod.POST }, produces="application/json")
	public @ResponseBody ModelActionPojo renderErrorPage1(Model m, HttpServletRequest httpRequest) {	
		int httpErrorCode = getErrorCode(httpRequest);
		
		ModelActionPojo result = new ModelActionPojo(false);
		result.setMessage(new MessagePojo("error", "Code: " + httpErrorCode, null));
		
		Throwable e = getException(httpRequest);
		if (e!=null) {
			result.addObjectError(e.getMessage());
		} else {
			result.addObjectError("Error code: " + httpErrorCode);
		}
		return result;
	}
	
	@RequestMapping(value = {"", "/"}, method = {RequestMethod.GET, RequestMethod.POST })
	public Object renderErrorPage(HttpServletRequest httpRequest) {		
		String errorHeading = "";
		String errorDetail = null;
		String errorLevel = "warning";
		boolean hideHelpText = false;
		Throwable e = null;
		
		int httpErrorCode = getErrorCode(httpRequest);

		switch (httpErrorCode) {
			case 400: {
				errorHeading = "Error 400: Bad Request";
				break;
			}
			case 401: {
				errorHeading = "Error 401: Unauthorized";
				hideHelpText = true;
				errorDetail = "Your account is not authorized for this action. Please contact the DARIAH-DE Helpdesk to seek support.";
				break;
			}
			case 403: {
				errorHeading = "Error 403: Forbidden";
				hideHelpText = true;
				errorDetail = "Your account does not have sufficient privileges. Please contact the DARIAH-DE Helpdesk to seek support.";
				break;
			}
			case 404: {
				errorHeading = "Error 404: Resource not found";
				break;
			}
			default: {
				errorHeading = "Internal Server Error";
				break;
			}
		}
		
		if (httpRequest.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)!=null) {
			Exception authEx = (Exception)httpRequest.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION); 
			if (authEx.getCause()!=null && authEx.getCause() instanceof SAMLException) {
				e = authEx.getCause();
				errorDetail = "A SAML/Shibboleth related error has occurred. Please try again later. If the problem persists, please notify support.";
				errorLevel = "error";
			}
			if (authEx instanceof UserCredentialsException) {
				e = authEx;
			}
		} else {
			e = getException(httpRequest);
		}
		
		if ( (httpRequest.getHeader("accept")!=null && httpRequest.getHeader("accept").toLowerCase().contains("json")) || 
				httpRequest.getServletPath().contains("/async/") || 
				httpRequest.getServletPath().contains("/forms/")) {
			ModelActionPojo result = new ModelActionPojo(false);
			result.setMessage(new MessagePojo("error", "Code: " + httpErrorCode, null));
			result.addObjectError(errorHeading + (errorDetail!=null ? (": " + errorDetail) : ""));
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
		
		ModelAndView m = new ModelAndView("error");
		m.addObject("exception", e);
		m.addObject("errorHeading", errorHeading);
		m.addObject("errorDetail", errorDetail);
		m.addObject("errorLevel", errorLevel);
		m.addObject("hideHelpText", hideHelpText);
		if (e!=null) {
			m.addObject("errorMsg", e.getMessage());
		}
		m.addObject("url", httpRequest.getRequestURL());
		return m;
	}
	
	@RequestMapping(value = "/loginFailed", method = {RequestMethod.GET, RequestMethod.POST })
	public String handleLoginFailed(Model m, HttpServletRequest httpRequest) {
		return "redirect:/login?error=true";
	}
	

	private Throwable getException(HttpServletRequest httpRequest) {
		if (httpRequest.getAttribute("javax.servlet.error.exception")==null) {
			return null;
		}
		return (Throwable) httpRequest.getAttribute("javax.servlet.error.exception");
	}
	
	private int getErrorCode(HttpServletRequest httpRequest) {
		if (httpRequest.getAttribute("javax.servlet.error.status_code")==null) {
			return -1;
		}
		return (Integer) httpRequest.getAttribute("javax.servlet.error.status_code");
	}
	
}
