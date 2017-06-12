package eu.dariah.de.minfba.schereg.controller.exceptions;

import javax.activity.InvalidActivityException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

@ControllerAdvice
public class ControllerExceptionHandler {
	protected static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);
	
	public static final String DEFAULT_ERROR_VIEW = "error";

	@ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(InvalidActivityException.class)
    public void handleConflict() {
        // Nothing to do, just to show how individual errors could be handled
    }
	
	@ExceptionHandler(value = Exception.class)
	public Object defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		// If the exception is annotated with @ResponseStatus rethrow it and let
		// the framework handle it - like the OrderNotFoundException example
		// at the start of this post.
		// AnnotationUtils is a Spring Framework utility class.
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}
		logger.error("Caught server error", e);
		
		if (req.getHeader("accept")!=null && req.getHeader("accept").toLowerCase().contains("json")) {
			ModelActionPojo result = new ModelActionPojo(false);
			result.setMessage(new MessagePojo("error", "Code: 500", null));
			result.addObjectError("Error code: 500");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
		
		ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
		mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// Otherwise setup and send the user to a default error-view.
		mav.addObject("errorHeading", "An internal server error has occurred");
		mav.addObject("errorMsg", e.getMessage());
		mav.addObject("url", req.getRequestURL());
		mav.addObject("exception", e);
		
		
		
		return mav;
	}
}
