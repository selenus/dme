package de.dariah.schereg.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.spi.LocaleServiceProvider;

import org.apache.commons.io.FileUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import de.dariah.aai.javasp.base.Role;
import de.dariah.aai.javasp.base.SimpleUserDetails;
import de.dariah.aai.javasp.exception.UserCredentialsException;
import de.dariah.aai.javasp.exception.UserCredentialsException.UserCredentialsExceptionTypes;
import de.dariah.aai.javasp.web.service.RoleService;
import de.dariah.samlsp.orm.model.RoleImpl;
import de.dariah.samlsp.orm.model.UserImpl;
import de.dariah.samlsp.orm.service.UserService;


public class ContextService implements ApplicationContextAware, ResourceLoaderAware {
	
	@Value("${debugMode:false}")
	private boolean debugMode;
	
	private static final Logger logger = LoggerFactory.getLogger(ContextService.class);
	private Properties props;
	
	private static final String PROPERTIES_SUFFIX = ".properties";
	
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private List<String> languages;
	
	private String localizationBasename;
	
	public String getLocalizationBasename() {
		return localizationBasename;
	}

	public void setLocalizationBasename(String localizationBasename) {
		this.localizationBasename = localizationBasename;
	}
	
	public List<String> getLanguages() {
		return languages;
	}

	private static class ContextSingletonHolder {
		static ContextService instance = new ContextService();
	}
	
	public static ContextService getInstance() {
		return ContextSingletonHolder.instance;
	}
		
	
	private ApplicationContext applicationContext;
	private TreeMap<String, Object> beanCache;
	
	private ContextService() {
		this.beanCache = new TreeMap<String, Object>();
	}
	
	public SessionFactory getSessionFactory() {
		return (SessionFactory) getBean("sessionFactory");
	}
	
	public Object getBean(Class<?> clazz) {
		return applicationContext.getBean(clazz);	
	}
	
	public <T> Map<String, T> getBeansOfType(Class<T> type) {
		return applicationContext.getBeansOfType(type);
	}
	
	public Object getBean(String name) {
		if (!beanCache.containsKey(name)) {
			
			Object bean = applicationContext.getBean(name);
			if (bean==null) {
				throw new BeanInitializationException(String.format("Bean %s could not be retrieved from ApplicationContext", name));
			}
			
			beanCache.put(name, bean);
		}
				
		return beanCache.get(name);
		
	}
	
	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}
	
	
	
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		logger.info("*****************************************************");
		logger.info("DARIAH Schema Registry - Application Context set");
		
		cleanupApacheDSWorkDir();
		loadProperties();
		loadAvailableLocales();
	}
	
	
	
	private void loadAvailableLocales() {
		logger.info("Scanning for available i18n resource files in basename: " + localizationBasename);
		
		languages = new ArrayList<String>();   
		int i = 0;
		
		String language, country, variant;
		StringBuilder localeCodeBuilder;
		
		// Loop over all the available locales and see if there is a resource file available
		for (Locale locale : Locale.getAvailableLocales()) {  
			language = locale.getLanguage();
			country = locale.getCountry();
			variant = locale.getVariant();
			localeCodeBuilder = new StringBuilder();
			
			if (language.length() > 0) {
				localeCodeBuilder.append(language);
				addIfLocaleDefined(localeCodeBuilder.toString(), languages);
			}

			localeCodeBuilder.append('_');
			if (country.length() > 0) {
				localeCodeBuilder.append(country);
				addIfLocaleDefined(localeCodeBuilder.toString(), languages);
			}

			if (variant.length() > 0 && (language.length() > 0 || country.length() > 0)) {
				localeCodeBuilder.append('_').append(variant);
				addIfLocaleDefined(localeCodeBuilder.toString(), languages);
			}
			i++;
		}
		
		logger.info(String.format("Scanned for %s language codes, identified %s i18n resources.", i, languages.size()));
	}
	
	private void addIfLocaleDefined(String languageCode, List<String> languages) {
		String resourcePath = String.format("%s_%s%s", localizationBasename, languageCode, PROPERTIES_SUFFIX);
		
		Resource resource = this.resourceLoader.getResource(resourcePath);
		if (!languages.contains(languageCode) && resource.exists()) {
			Locale locale = Locale.forLanguageTag(languageCode);
			if (locale!=null) {
				languages.add(languageCode);
				logger.info(String.format("Found i18n resource for [%s: %s]", languageCode, locale.getDisplayLanguage()));
			}
		}
	}
	
	

	private void loadProperties() {
		try {
			logger.info("Attempting to load main configuration properties.");
			
			String configLocation = (String)this.getBean("configLocation");
			Resource resource = new ClassPathResource(configLocation);
			props = PropertiesLoaderUtils.loadProperties(resource);
			
			logger.info("Properties loaded.");
		} catch (IOException e) {
			logger.error(String.format("Attempting to load main configuration properties failed: ", e.getMessage()), e);
			throw new FatalBeanException("Attempting to load main configuration properties failed", e);
		}
	}
	
	public String getPropertyValue(String key, String defaultResult) {
		return props.getProperty(key, defaultResult);
	}

	private void cleanupApacheDSWorkDir() {
		
            String apacheWorkDir = System.getProperty("apacheDSWorkDir");

            if (apacheWorkDir == null) {
                apacheWorkDir = System.getProperty("java.io.tmpdir") + File.separator + "apacheds-spring-security";
            }

            File workDir = new File(apacheWorkDir);

            if (workDir.exists() && workDir.isDirectory()) {
            	
            	logger.warn("Apache Directory Server needs to be cleaned up due to improper shutdown...");
            	
            	try {
					FileUtils.deleteDirectory(workDir);
					logger.info(String.format("Directory [%s] deleted", apacheWorkDir));
				} catch (IOException e) {
					logger.error(String.format("Failed to delete directory [%s]", apacheWorkDir));
				}
            }
	}
		
	public SimpleUserDetails getCurrentUserDetails() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SimpleUserDetails ud = null;
		if (auth != null) { 
			if (auth.getDetails() instanceof SimpleUserDetails) {
				ud = (SimpleUserDetails)auth.getDetails();
			} 
			// This is for debugging purposes when SAML is not used but simple WebAuth
			else if (debugMode && auth.getDetails() instanceof WebAuthenticationDetails) {
				ud = handleDebugUser(auth);
			}
		}
		if (ud == null || ud.getId()<=0) {
			throw new UserCredentialsException(UserCredentialsExceptionTypes.CREDENTIALS_NOT_AVAILABLE, "Could not fetch valid user details.", ud);
		}
		
		return ud;
	}
		
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}
	
	
	private SimpleUserDetails handleDebugUser(Authentication auth) {
		RoleService roleService = (RoleService)getBean(RoleService.class);
		UserService userService = (UserService)getBean(UserService.class);
		
		Hashtable<String, Role> roles = roleService.getRoles();
		Collection<Role> debugRoles = new ArrayList<Role>();
		UserImpl user = new UserImpl();
		
		if (auth.getPrincipal() instanceof User) {
			User principal = (User)auth.getPrincipal();
			
			if (principal.getAuthorities() != null) {
				for (GrantedAuthority ga : principal.getAuthorities()) {
					Role r = roles.get(ga.getAuthority());
					if (r != null) {
						debugRoles.add(r);
					}
				}
			}
			
			
			List<UserImpl> users = userService.findByProperty("eduPersonPrincipalName", principal.getUsername(), true);
								
			if (users!=null && users.size()>0) {
				user = users.get(0);
			} else {
				user.setEduPersonPrincipalName(principal.getUsername());
				user.setFirstName("John");
				user.setLastName("Dev");
				user.setCommonName(principal.getUsername());
				user.setEndpointId("local");
				user.setEndpointName("local");
				user.setLanguage("de");
				user.setNameId(principal.getUsername());
				
				
				userService.saveOrUpdateUser(user);
			}
			return new SimpleUserDetails(user, debugRoles);
		}
		return null;
	}
}
