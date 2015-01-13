package de.dariah.schereg.base.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationHandlerService {
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception);
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}
