package com.tlali.api.security;

import com.tlali.api.user.AppUser;
import com.tlali.api.user.AppUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final AppUserService appUserService;
	private final JwtService jwtService;
	private final String frontendAuthCallback;

	public OAuth2LoginSuccessHandler(
			AppUserService appUserService,
			JwtService jwtService,
			@Value("${tlali.security.frontend-auth-callback}") String frontendAuthCallback
	) {
		this.appUserService = appUserService;
		this.jwtService = jwtService;
		this.frontendAuthCallback = frontendAuthCallback;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		String email = oauthUser.getAttribute("email");
		String name = oauthUser.getAttribute("name");

		if (email == null || email.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account has no email");
			return;
		}

		AppUser user = appUserService.createGoogleUserIfMissing(
				email,
				name == null || name.isBlank() ? email : name
		);
		String token = jwtService.createToken(user);
		String redirectUrl = UriComponentsBuilder.fromUriString(frontendAuthCallback)
				.queryParam("token", token)
				.build()
				.toUriString();

		response.sendRedirect(redirectUrl);
	}
}
