package de.dariah.schereg.view.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class MappingAppletFilter implements Filter {

    public void init(FilterConfig fc) throws ServletException {}

    public void destroy() {}
    
    /**
     * Applies the MappingAppletRequestWrapper to Http Requests (from the Mapping Applet)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    		throws IOException, ServletException {
        
    	chain.doFilter(new MappingAppletRequestWrapper((HttpServletRequest)request), response);
    }
}
