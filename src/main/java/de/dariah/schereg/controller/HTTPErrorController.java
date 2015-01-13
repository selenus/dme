package de.dariah.schereg.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.dariah.aai.web.controller.ExceptionHandlingController;

@Controller
@RequestMapping("/errors")
public class HTTPErrorController extends ExceptionHandlingController {
	
	@RequestMapping(value="/unhandled")
    public String handleUnhandled(@RequestParam(required=false) String code, Model model) {
		if (code!=null) {
			model.addAttribute("htmlerrorcode", code);
		}
    	return "error/unhandled";
    }
}
