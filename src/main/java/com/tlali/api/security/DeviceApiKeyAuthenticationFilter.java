package com.tlali.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class DeviceApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String HEADER_NAME = "X-Tlali-Device-Key";
	private static final String SENSOR_READINGS_PATH = "/api/v1/sensor-readings";

	private final String deviceApiKey;

	public DeviceApiKeyAuthenticationFilter(
			@Value("${tlali.security.device-api-key}") String deviceApiKey
	) {
		this.deviceApiKey = deviceApiKey;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (isSensorReadingIngest(request)) {
			String providedKey = request.getHeader(HEADER_NAME);
			if (deviceApiKey.equals(providedKey)) {
				SecurityContextHolder.getContext().setAuthentication(new DeviceAuthentication());
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isSensorReadingIngest(HttpServletRequest request) {
		return HttpMethod.POST.matches(request.getMethod())
				&& SENSOR_READINGS_PATH.equals(request.getRequestURI());
	}

	private static class DeviceAuthentication extends AbstractAuthenticationToken {

		DeviceAuthentication() {
			super(List.of(new SimpleGrantedAuthority("ROLE_DEVICE")));
			setAuthenticated(true);
		}

		@Override
		public Object getCredentials() {
			return "";
		}

		@Override
		public Object getPrincipal() {
			return "tlali-device";
		}
	}
}
