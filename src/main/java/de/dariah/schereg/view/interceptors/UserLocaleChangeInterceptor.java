package de.dariah.schereg.view.interceptors;

import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import de.dariah.schereg.util.ContextService;


public class UserLocaleChangeInterceptor extends LocaleChangeInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
		String newLocale = request.getParameter(getParamName());
		if (newLocale == null || newLocale.isEmpty()) {
			return true;
		}
		// TODO: Once user management is implemented in generic search, updating the user is what happens here
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView != null && modelAndView.getModelMap() != null) {	
			Hashtable<String, String> languages = new Hashtable<String, String>();
			for (String code : ContextService.getInstance().getLanguages()) {
				languages.put(code, Locale.forLanguageTag(code).getDisplayLanguage(LocaleContextHolder.getLocale()));
			}
			modelAndView.getModelMap().addAttribute("_LANGUAGES", languages);
		}
	}
}
