package eu.dariah.de.minfba.schereg.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.dariah.de.minfba.core.web.controller.BaseNavigatingController;

@Controller
@RequestMapping("/test")
public class TestController extends BaseNavigatingController {
	
	public TestController() {
		super("test");
	}
	
	@RequestMapping("/simple")
	public String testSimple(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/simple");
		return "test/simple";
	}
	@RequestMapping("/simpleFluid")
	public String testSimpleFluid(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/simple/fluid");
		return "test/simpleFluid";
	}
	@RequestMapping("/jumbo")
	public String testJumbo(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/jumbo");
		return "test/jumbo";
	}
	@RequestMapping("/jumboFluid")
	public String testJumboFluid(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/jumbo/fluid");
		return "test/jumboFluid";
	}
	@RequestMapping("/main")
	public String testMain(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/main");
		return "test/main";
	}
	@RequestMapping("/mainFluid")
	public String testMainFluid(Model model, Locale locale) {
		logger.info("Welcome home! The client locale is {}.", locale);
		this.overrideNavItem(model, "test/main/fluid");
		return "test/mainFluid";
	}
}